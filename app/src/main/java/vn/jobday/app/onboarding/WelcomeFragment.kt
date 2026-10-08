package vn.jobday.app.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import vn.jobday.app.R
import vn.jobday.app.databinding.FragmentWelcomeBinding
import vn.jobday.app.main.MainViewModel
import vn.jobday.core.model.auth.AccountType
import vn.jobday.feature.auth.ARG_ACCOUNT_TYPE

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null

    private val binding: FragmentWelcomeBinding
        get() = checkNotNull(_binding)

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWelcomeBinding.inflate(
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

        showLogoIfAvailable()

        binding.exploreButton.setOnClickListener {
            mainViewModel.continueAsGuest()
        }

        binding.employerLoginButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_welcomeFragment_to_authFragment,
                Bundle().apply {
                    putString(
                        ARG_ACCOUNT_TYPE,
                        AccountType.EMPLOYER.name,
                    )
                },
            )
        }
    }

    private fun showLogoIfAvailable() {
        val logoResourceId = resources.getIdentifier(
            "ic_logo_start",
            "drawable",
            requireContext().packageName,
        )

        if (logoResourceId != 0) {
            binding.logoImageView.setImageResource(logoResourceId)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
