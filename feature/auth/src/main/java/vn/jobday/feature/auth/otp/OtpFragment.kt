package vn.jobday.feature.auth.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import vn.jobday.feature.auth.R
import vn.jobday.feature.auth.databinding.FragmentOtpBinding

@AndroidEntryPoint
class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null

    private val binding: FragmentOtpBinding
        get() = checkNotNull(_binding)

    private val viewModel: OtpViewModel by viewModels()

    private var lastSubmittedCode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOtpBinding.inflate(
            inflater,
            container,
            false,
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.codeEditText.doAfterTextChanged {
            val code = it
                ?.toString()
                .orEmpty()
                .filter(Char::isDigit)
                .take(6)

            viewModel.onAction(
                OtpAction.CodeChanged(
                    value = code,
                ),
            )

            if (code.length < 6) {
                lastSubmittedCode = null
            } else if (lastSubmittedCode != code) {
                lastSubmittedCode = code
                viewModel.onAction(OtpAction.Submit)
            }
        }

        binding.resendButton.setOnClickListener {
            viewModel.onAction(OtpAction.Resend)
        }

        observeViewModel()
        focusOtpInput()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: OtpUiState) {
        if (
            binding.codeEditText.text?.toString() !=
            state.code
        ) {
            binding.codeEditText.setText(state.code)
            binding.codeEditText.setSelection(state.code.length)
        }

        binding.phoneTextView.text = getString(
            R.string.otp_description,
            state.phoneNumber,
        )

        renderOtpDigits(state.code)

        val errorMessage = state.codeError ?: state.errorMessage
        binding.errorTextView.text = errorMessage
        binding.errorTextView.isVisible =
            errorMessage != null

        binding.debugOtpTextView.text =
            state.debugOtp?.let {
                getString(R.string.otp_local, it)
            }

        binding.debugOtpTextView.isVisible =
            state.debugOtp != null

        val loading =
            state.isSubmitting || state.isResending

        binding.progressBar.isVisible = loading
        binding.codeEditText.isEnabled =
            !loading && !state.isVerified

        binding.resendButton.isEnabled =
            !loading &&
                !state.isVerified &&
                state.resendSeconds == 0

        binding.resendButton.text =
            if (state.resendSeconds > 0) {
                getString(
                    R.string.otp_resend_countdown,
                    state.resendSeconds / 60,
                    state.resendSeconds % 60,
                )
            } else {
                getString(R.string.otp_resend)
            }

        binding.successOverlay.isVisible = state.isVerified

        if (state.isVerified) {
            hideKeyboard()
        }
    }

    private fun renderOtpDigits(code: String) {
        val digitViews: List<TextView> = listOf(
            binding.otpDigit1,
            binding.otpDigit2,
            binding.otpDigit3,
            binding.otpDigit4,
            binding.otpDigit5,
            binding.otpDigit6,
        )

        val activeIndex = if (code.isEmpty()) {
            0
        } else {
            code.length.coerceAtMost(6) - 1
        }

        digitViews.forEachIndexed { index, digitView ->
            digitView.text = code.getOrNull(index)?.toString().orEmpty()
            digitView.setBackgroundResource(
                if (index == activeIndex) {
                    R.drawable.bg_otp_box_active
                } else {
                    R.drawable.bg_otp_box
                },
            )
        }
    }

    private fun focusOtpInput() {
        binding.codeEditText.requestFocus()
        binding.codeEditText.post {
            requireContext()
                .getSystemService(InputMethodManager::class.java)
                .showSoftInput(
                    binding.codeEditText,
                    InputMethodManager.SHOW_IMPLICIT,
                )
        }
    }

    private fun hideKeyboard() {
        requireContext()
            .getSystemService(InputMethodManager::class.java)
            .hideSoftInputFromWindow(
                binding.codeEditText.windowToken,
                0,
            )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
