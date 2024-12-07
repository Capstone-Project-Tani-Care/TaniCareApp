package com.dicoding.tanicare

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.databinding.FragmentResultClassificationBinding

class ResultClassificationFragment : Fragment() {

    private var _binding: FragmentResultClassificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultClassificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menerima data yang diteruskan dari ClassificationFragment
        val predictedClass = arguments?.getString("predicted_class")
        val treatment = arguments?.getString("treatment")
        val imageBytes = arguments?.getByteArray("selected_image")

        // Menampilkan data pada UI jika ada
        predictedClass?.let {
            binding.tvDiseaseName.text = it
        }
        treatment?.let {
            binding.tvDiseaseDescription.text = it
        }
        imageBytes?.let {
            val receivedImage = BitmapFactory.decodeByteArray(it, 0, it.size)
            binding.diseaseImage.setImageBitmap(receivedImage)
        }

        // Tombol kembali
        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_global_homeFragment)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_global_homeFragment)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
