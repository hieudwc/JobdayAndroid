package vn.jobday.app.startup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import vn.jobday.app.databinding.FragmentStartupBinding

class StartupFragment : Fragment() {

    private var _binding: FragmentStartupBinding? = null

    private val binding: FragmentStartupBinding
        get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStartupBinding.inflate(
            inflater,
            container,
            false,
        )

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}