package com.dicoding.tanicare.credentials

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.FragmentLoginBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (findNavController().currentDestination?.id == R.id.loginFragment) {
                    findNavController().navigateUp()
                }
            }
        })

        // Set click listener untuk tombol signup
        binding.signup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        // Set click listener untuk tombol login
        binding.loginButton.setOnClickListener {
            val email = binding.usernameField.text.toString()
            val password = binding.passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Ubah LoginRequest menjadi Map<String, String>
                val loginRequest = mapOf(
                    "email" to email,
                    "password" to password
                )
                performLogin(loginRequest)
            } else {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
        setupPasswordVisibilityToggle(binding.passwordField)
    }

    private fun performLogin(loginRequest: Map<String, String>) {
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.login(loginRequest).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && loginResponse["error"] == false) {
                        val token = loginResponse["idToken"] as String
                        val userId = loginResponse["userId"] as String
                        val refreshToken = loginResponse["refreshToken"] as String

                        sharedPreferencesManager.saveAuthToken(token)
                        sharedPreferencesManager.saveUserId(userId)
                        sharedPreferencesManager.saveRefreshToken(refreshToken)

                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } else {
                        Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupPasswordVisibilityToggle(editText: EditText) {
        val visibilityIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_visibility)
        val visibilityOffIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_visibility_off)

        val existingStartDrawable = editText.compoundDrawablesRelative[0] // Drawable di posisi Start

        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= editText.right - editText.compoundPaddingRight) {
                    // Toggle password visibility
                    if (editText.transformationMethod is PasswordTransformationMethod) {
                        // Show password
                        editText.transformationMethod = null
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            existingStartDrawable, null, visibilityOffIcon, null
                        )
                    } else {
                        // Hide password
                        editText.transformationMethod = PasswordTransformationMethod.getInstance()
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            existingStartDrawable, null, visibilityIcon, null
                        )
                    }
                    editText.setSelection(editText.text.length) // Keep cursor at the end
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
