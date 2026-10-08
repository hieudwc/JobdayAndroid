package vn.jobday.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import vn.jobday.core.model.auth.AccountType
import vn.jobday.feature.home.databinding.FragmentHomeBinding

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding: FragmentHomeBinding
        get() = checkNotNull(_binding)

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(
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

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigationHome -> {
                    showHome()
                    true
                }

                R.id.navigationAccount -> {
                    showAccount()
                    true
                }

                // Ba tab này sẽ được nối khi feature tương ứng hoàn thành.
                R.id.navigationProfile,
                R.id.navigationJobs,
                R.id.navigationNotifications,
                -> false

                else -> false
            }
        }

        binding.loginButton.setOnClickListener {
            (activity as? HomeNavigation)?.openCandidateLogin()
        }

        observeViewModel()
    }

    private fun showHome() {
        binding.homeContent.isVisible = true
        binding.accountContent.isVisible = false
    }

    private fun showAccount() {
        binding.homeContent.isVisible = false
        binding.accountContent.isVisible = true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: HomeUiState) {
        val user = state.user

        binding.loggedOutContent.isVisible = user == null
        binding.loggedInContent.isVisible = user != null

        if (user == null) return

        binding.accountNameTextView.text =
            user.displayName ?: getString(R.string.home_account_default_name)
        binding.accountPhoneTextView.text = user.phoneNumber
        binding.accountTypeTextView.text = when (user.accountType) {
            AccountType.CANDIDATE -> getString(R.string.home_account_candidate)
            AccountType.EMPLOYER -> getString(R.string.home_account_employer)
            AccountType.ADMIN -> getString(R.string.home_account_admin)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

interface HomeNavigation {
    fun openCandidateLogin()
}
