package com.dicoding.tanicare

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.databinding.FragmentInputClassificationBinding
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class InputClassificationFragment : Fragment(R.layout.fragment_input_classification) {

    private lateinit var binding: FragmentInputClassificationBinding
    private var selectedImage: Bitmap? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInputClassificationBinding.bind(view)

        // Aksi untuk memilih gambar
        binding.iconPlus.setOnClickListener {
            openGallery()
        }

        // Navigasi ke fragment_classification dan menyimpan gambar
        binding.btnCekPenyakit.setOnClickListener {
            if (selectedImage != null) {
                navigateToClassificationFragment()
            } else {
                Toast.makeText(context, "Silakan pilih gambar terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                val imageUri = data?.data
                val inputStream = requireContext().contentResolver.openInputStream(imageUri!!)
                selectedImage = BitmapFactory.decodeStream(inputStream)
                binding.iconPlus.setImageBitmap(selectedImage) // Tampilkan gambar di ImageView
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal memuat gambar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToClassificationFragment() {
        // Convert bitmap ke byte array agar dapat diteruskan melalui Bundle
        val byteArrayOutputStream = ByteArrayOutputStream()
        selectedImage?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        // Buat Bundle dan navigasi ke fragment_classification
        val bundle = Bundle().apply {
            putByteArray("selected_image", imageBytes)
        }
        findNavController().navigate(R.id.action_inputClassificationFragment_to_classificationFragment, bundle)
    }
}
