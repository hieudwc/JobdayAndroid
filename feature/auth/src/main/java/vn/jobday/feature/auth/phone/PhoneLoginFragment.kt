package vn.jobday.feature.auth.phone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import vn.jobday.feature.auth.ARG_ACCOUNT_TYPE
import vn.jobday.feature.auth.ARG_CHALLENGE_ID
import vn.jobday.feature.auth.ARG_DEBUG_OTP
import vn.jobday.feature.auth.ARG_PHONE_NUMBER
import vn.jobday.feature.auth.ARG_RETRY_AFTER_SECONDS
import vn.jobday.feature.auth.R
import vn.jobday.feature.auth.databinding.FragmentPhoneLoginBinding

@AndroidEntryPoint
class PhoneLoginFragment : Fragment() {

    private var _binding: FragmentPhoneLoginBinding? = null

    private val binding: FragmentPhoneLoginBinding
        get() = checkNotNull(_binding)

    private val viewModel: PhoneLoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPhoneLoginBinding.inflate(
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

        setupListeners()
        observeViewModel()

        if (savedInstanceState == null) {
            focusPhoneInput()
        }
    }

    private fun setupListeners() {
        binding.closeButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.phoneInputLayout.setEndIconOnClickListener {
            binding.phoneEditText.text?.clear()
        }

        binding.phoneEditText.doAfterTextChanged {
            viewModel.onAction(
                PhoneLoginAction.PhoneChanged(
                    value = it?.toString().orEmpty(),
                ),
            )
        }

        binding.phoneEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onAction(PhoneLoginAction.Submit)
                true
            } else {
                false
            }
        }

        binding.continueButton.setOnClickListener {
            viewModel.onAction(PhoneLoginAction.Submit)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                launch {
                    viewModel.uiState.collect(::render)
                }

                launch {
                    viewModel.effect.collect(::handleEffect)
                }
            }
        }
    }

    private fun render(state: PhoneLoginUiState) {
        if (
            binding.phoneEditText.text?.toString() !=
            state.phoneNumber
        ) {
            binding.phoneEditText.setText(state.phoneNumber)
            binding.phoneEditText.setSelection(
                state.phoneNumber.length,
            )
        }

        binding.phoneInputLayout.error = state.phoneError
        binding.phoneInputLayout.isEndIconVisible =
            state.phoneNumber.isNotEmpty()

        binding.errorTextView.text = state.errorMessage
        binding.errorTextView.isVisible =
            state.errorMessage != null

        binding.continueButton.isEnabled = !state.isLoading
        binding.continueButton.text = if (state.isLoading) {
            ""
        } else {
            getString(R.string.continue_label)
        }
        binding.progressBar.isVisible = state.isLoading
    }

    private fun handleEffect(effect: PhoneLoginEffect) {
        when (effect) {
            is PhoneLoginEffect.NavigateToOtp -> {
                findNavController().navigate(
                    R.id.action_authPhoneFragment_to_authOtpFragment,
                    Bundle().apply {
                        putString(
                            ARG_CHALLENGE_ID,
                            effect.challenge.challengeId,
                        )
                        putString(ARG_PHONE_NUMBER, effect.phoneNumber)
                        putString(ARG_ACCOUNT_TYPE, effect.accountType.name)
                        putInt(
                            ARG_RETRY_AFTER_SECONDS,
                            effect.challenge.retryAfterSeconds,
                        )
                        putString(ARG_DEBUG_OTP, effect.challenge.debugOtp)
                    },
                )
            }
        }
    }

    private fun focusPhoneInput() {
        binding.phoneEditText.requestFocus()
        binding.phoneEditText.post {
            requireContext()
                .getSystemService(InputMethodManager::class.java)
                .showSoftInput(
                    binding.phoneEditText,
                    InputMethodManager.SHOW_IMPLICIT,
                )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
