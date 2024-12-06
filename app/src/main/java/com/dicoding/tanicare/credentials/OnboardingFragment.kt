package com.dicoding.tanicare.credentials

import android.os.Bundle
import android.util.Log
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
        // Meng-inflate layout dan menghubungkan dengan ViewBinding
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnNext.setOnClickListener {
            try {
                // Pastikan NavController ditemukan
                findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                // Menambahkan log untuk debugging
                Log.e("NavController", "NavController tidak ditemukan: ${e.message}")
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Pastikan untuk membebaskan binding saat view dihancurkan
        _binding = null
    }
}
