package com.dicoding.tanicare.credentials

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runAnimations()

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboarding_to_loginFragment)
        }
    }
    private fun runAnimations() {

        val titleAnimator = ObjectAnimator.ofFloat(binding.titleText, View.ALPHA, 0f, 1f).apply {
            duration = 1500
        }


        val subtitleAnimator = ObjectAnimator.ofFloat(binding.subtitleText, View.TRANSLATION_Y, 100f, 0f).apply {
            duration = 1500
        }


        val buttonAnimator = ObjectAnimator.ofFloat(binding.registerButton, View.ALPHA, 0f, 1f).apply {
            duration = 1500
        }


        AnimatorSet().apply {
            playTogether(titleAnimator, subtitleAnimator, buttonAnimator)
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
