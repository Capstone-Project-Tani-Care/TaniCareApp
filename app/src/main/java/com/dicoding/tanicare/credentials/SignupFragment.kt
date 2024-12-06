package com.dicoding.tanicare.credentials

import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.FragmentLoginBinding
import com.dicoding.tanicare.databinding.FragmentSignupBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (findNavController().currentDestination?.id == R.id.signupFragment) {
                    findNavController().navigateUp()
                }
            }
        })

        // Set click listener untuk tombol signup
        binding.signupButton.setOnClickListener {
            val name = binding.usernameField.text.toString()
            val email = binding.emailField.text.toString()
            val password = binding.passwordField.text.toString()
            val confirmPassword = binding.cPasswordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword){
                    val signupRequest = mapOf(
                        "email" to email,
                        "password" to password,
                        "name" to name
                    )
                    performSignup(signupRequest)
                } else {
                    Toast.makeText(requireContext(), "Confirm Password is not match", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listener untuk tombol login
        binding.loginText.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        setupPasswordVisibilityToggle(binding.passwordField)
        setupPasswordVisibilityToggle(binding.cPasswordField)
    }

    private fun performSignup(signupRequest: Map<String, String>) {
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.signup(signupRequest).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val signupResponse = response.body()
                    if (signupResponse != null && signupResponse["error"] == false) {
                        Toast.makeText(requireContext(), "Signup Success", Toast.LENGTH_SHORT).show()

                        // Navigasi ke halaman utama
                        findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
                    } else {
                        Toast.makeText(requireContext(), "Signup failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Signup failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Signup failed: ${t.message}", Toast.LENGTH_SHORT).show()
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