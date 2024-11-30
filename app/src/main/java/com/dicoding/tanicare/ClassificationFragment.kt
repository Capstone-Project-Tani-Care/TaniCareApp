package com.dicoding.tanicare

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.databinding.FragmentClassificationBinding

class ClassificationFragment : Fragment() {

    private var _binding: FragmentClassificationBinding? = null
    private val binding get() = _binding!!

    private var receivedImage: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menerima gambar yang dikirim dari InputClassificationFragment
        val imageBytes = arguments?.getByteArray("selected_image")
        if (imageBytes != null) {
            receivedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            binding.imagePreview.setImageBitmap(receivedImage) // Tampilkan gambar jika diperlukan
        }

        // Navigasi ke ResultClassificationFragment sesuai tombol yang ditekan
        binding.btnPadi.setOnClickListener {
            navigateToResult("padi")
        }

        binding.btnJagung.setOnClickListener {
            navigateToResult("jagung")
        }

        binding.btnKetela.setOnClickListener {
            navigateToResult("ketela")
        }

        binding.btnTomat.setOnClickListener {
            navigateToResult("tomat")
        }

        binding.btnKentang.setOnClickListener {
            navigateToResult("kentang")
        }

        // Tombol kembali
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun navigateToResult(plantType: String) {
        val bundle = Bundle().apply {
            putString("plantType", plantType)
            // Anda juga dapat menambahkan gambar jika perlu diteruskan
            arguments?.getByteArray("selected_image")?.let {
                putByteArray("selected_image", it)
            }
        }
        findNavController().navigate(R.id.action_ClassificationFragment_to_ResultClassificationFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
