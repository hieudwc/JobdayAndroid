package vn.jobday.feature.auth.onboarding

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import vn.jobday.feature.auth.R
import vn.jobday.feature.auth.databinding.FragmentCandidateNameBinding

@AndroidEntryPoint
class CandidateNameFragment : Fragment() {

    private var _binding: FragmentCandidateNameBinding? = null
    private val binding: FragmentCandidateNameBinding
        get() = checkNotNull(_binding)

    private val viewModel: CandidateNameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCandidateNameBinding.inflate(
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
            focusNameInput()
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.nameInputLayout.setEndIconOnClickListener {
            binding.nameEditText.text?.clear()
        }

        binding.nameEditText.doAfterTextChanged {
            viewModel.onAction(
                CandidateNameAction.NameChanged(
                    it?.toString().orEmpty(),
                ),
            )
        }

        binding.nameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onAction(CandidateNameAction.Submit)
                true
            } else {
                false
            }
        }

        binding.continueButton.setOnClickListener {
            viewModel.onAction(CandidateNameAction.Submit)
        }

        binding.successContinueButton.setOnClickListener {
            (activity as? CandidateOnboardingNavigation)
                ?.finishCandidateOnboarding()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: CandidateNameUiState) {
        if (binding.nameEditText.text?.toString() != state.name) {
            binding.nameEditText.setText(state.name)
            binding.nameEditText.setSelection(state.name.length)
        }

        binding.nameInputLayout.error = state.nameError
        binding.nameInputLayout.isEndIconVisible = state.name.isNotEmpty()

        binding.errorTextView.text = state.errorMessage
        binding.errorTextView.isVisible = state.errorMessage != null

        binding.continueButton.isEnabled = !state.isLoading
        binding.continueButton.text = if (state.isLoading) {
            ""
        } else {
            getString(R.string.continue_label)
        }
        binding.progressBar.isVisible = state.isLoading

        binding.nameFormContent.isVisible = !state.isCompleted
        binding.successContent.isVisible = state.isCompleted

        if (state.isCompleted) {
            hideKeyboard()
        }
    }

    private fun focusNameInput() {
        binding.nameEditText.requestFocus()
        binding.nameEditText.post {
            requireContext()
                .getSystemService(InputMethodManager::class.java)
                .showSoftInput(
                    binding.nameEditText,
                    InputMethodManager.SHOW_IMPLICIT,
                )
        }
    }

    private fun hideKeyboard() {
        requireContext()
            .getSystemService(InputMethodManager::class.java)
            .hideSoftInputFromWindow(binding.nameEditText.windowToken, 0)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

interface CandidateOnboardingNavigation {
    fun finishCandidateOnboarding()
}
