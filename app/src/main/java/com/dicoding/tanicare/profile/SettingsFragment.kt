package com.dicoding.tanicare.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.FragmentSettingsBinding
import com.dicoding.tanicare.helper.SharedPreferencesManager

class SettingsFragment : Fragment() {
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        // Load current dark mode state
        val isDarkMode = sharedPreferencesManager.isDarkModeEnabled()
        binding.darkModeSwitch.isChecked = isDarkMode

        // Listen for dark mode toggle
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferencesManager.setDarkModeEnabled(isChecked)
            requireActivity().recreate() // Restart activity to apply theme
        }

        binding.logoutLayout.setOnClickListener{
            sharedPreferencesManager.clearUserId()
            sharedPreferencesManager.clearAuthToken()
            requireActivity().recreate()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        return binding.root
    }
}

