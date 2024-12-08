package com.dicoding.tanicare.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.databinding.FragmentSettingsBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            if (isChecked) {
                // Aktifkan dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                // Aktifkan light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainChangeEmail.setOnClickListener{
            binding.changeEmailLayout.visibility = View.VISIBLE
        }
        binding.mainChangePassword.setOnClickListener{
            binding.changePasswordLayout.visibility = View.VISIBLE
        }
        binding.sendButtonEmail.setOnClickListener {
            putEmail()
        }
        binding.sendButtonPassword.setOnClickListener {
            putPassword()
        }
    }

    private fun putEmail(){
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val token = sharedPreferencesManager.getAuthToken()
        if (token == null){
            return
        }
        val email = binding.editTextEmail.text.toString()
        val body = mapOf("email" to email)
        apiService.updateEmail("Bearer $token", body).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody["status"] == "success") {
                        Toast.makeText(requireContext(), "Email updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Email update failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun putPassword(){
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val token = sharedPreferencesManager.getAuthToken()
        if (token == null){
            return
        }
        val password = binding.editTextPassword.text.toString()
        val body = mapOf("password" to password)
        apiService.updatePassword("Bearer $token", body).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody["status"] == "success") {
                        Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Password update failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }
}

