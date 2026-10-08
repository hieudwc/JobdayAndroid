package vn.jobday.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import vn.jobday.app.databinding.ActivityMainBinding
import vn.jobday.app.main.MainUiState
import vn.jobday.app.main.MainViewModel
import vn.jobday.core.model.auth.AccountType
import vn.jobday.feature.auth.ARG_ACCOUNT_TYPE
import vn.jobday.feature.auth.onboarding.CandidateOnboardingNavigation
import vn.jobday.feature.home.HomeNavigation

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    HomeNavigation,
    CandidateOnboardingNavigation {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupNavigation()
        observeUiState()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars(),
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom,
            )

            insets
        }
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.navHostFragment,
            ) as NavHostFragment

        navController = navHostFragment.navController
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    renderUiState(uiState)
                }
            }
        }
    }

    private fun renderUiState(
        uiState: MainUiState,
    ) {
        when (uiState) {
            MainUiState.CheckingSession -> {
                // Giữ nguyên StartupFragment.
            }

            MainUiState.Unauthenticated -> {
                showWelcomeIfNeeded()
            }

            MainUiState.Guest -> {
                showRootDestination(
                    destinationId = R.id.homeFragment,
                )
            }

            is MainUiState.AuthenticatedNeedsOnboarding -> {
                showRootDestination(
                    destinationId = R.id.candidateNameFragment,
                )
            }

            is MainUiState.Authenticated -> {
                if (
                    navController.currentDestination?.id !=
                    R.id.candidateNameFragment
                ) {
                    showRootDestination(
                        destinationId = R.id.homeFragment,
                    )
                }
            }
        }
    }

    private fun showWelcomeIfNeeded() {
        when (navController.currentDestination?.id) {
            R.id.startupFragment,
            R.id.homeFragment -> showRootDestination(
                destinationId = R.id.welcomeFragment,
            )

            // Nếu người dùng đang ở trong auth flow thì giữ nguyên
            // back stack Phone -> OTP.
            else -> Unit
        }
    }

    private fun showRootDestination(
        destinationId: Int,
    ) {
        if (navController.currentDestination?.id == destinationId) {
            return
        }

        val graph = navController.navInflater.inflate(
            R.navigation.app_navigation,
        )

        graph.setStartDestination(destinationId)
        navController.graph = graph
    }

    override fun openCandidateLogin() {
        navController.navigate(
            R.id.authFragment,
            Bundle().apply {
                putString(
                    ARG_ACCOUNT_TYPE,
                    AccountType.CANDIDATE.name,
                )
            },
        )
    }

    override fun finishCandidateOnboarding() {
        showRootDestination(
            destinationId = R.id.homeFragment,
        )
    }
}
