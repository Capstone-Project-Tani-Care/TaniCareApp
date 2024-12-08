package com.dicoding.tanicare

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.databinding.FragmentClassificationBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

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

        // Menangani tombol untuk memilih jenis tanaman dan melakukan prediksi
        binding.btnPadi.setOnClickListener {
            predictCrop("rice")
        }

        binding.btnJagung.setOnClickListener {
            predictCrop("corn")
        }

        binding.btnKedelai.setOnClickListener {
            predictCrop("soybean")
        }

        binding.btnTomat.setOnClickListener {
            predictCrop("tomato")
        }

        binding.btnKentang.setOnClickListener {
            predictCrop("potato")
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun predictCrop(cropType: String) {
        if (receivedImage != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            receivedImage?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()

            // Mengonversi byte array ke MultipartBody.Part
            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)

            val part = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

            // Membuat instance ApiService dan melakukan request
            val apiService = ApiClient.getClient().create(ApiService::class.java)
            apiService.predictCrop(cropType, part).enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        val data = result?.get("data") as? Map<String, Any>

                        val plant = data?.get("plant") as? String
                        val predictedClass = data?.get("predicted_class") as? String
                        val treatment = data?.get("treatment") as? String

                        // Menampilkan hasil prediksi di UI
                        if (plant != null && predictedClass != null && treatment != null) {
                            showPredictionResult(predictedClass, treatment, imageBytes)
                        }
                    } else {
                        Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(context, "Gagal menghubungi server: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showPredictionResult(predictedClass: String, treatment: String, imageBytes: ByteArray) {
        // Tampilkan hasil prediksi dan saran pengobatan
        val resultMessage = "Prediksi: $predictedClass\nPerawatan: $treatment"
        //Toast.makeText(context, resultMessage, Toast.LENGTH_LONG).show()

        val bundle = Bundle().apply {
            putString("predicted_class", predictedClass)
            putString("treatment", treatment)
            putByteArray("selected_image", imageBytes)
        }
        findNavController().navigate(R.id.action_ClassificationFragment_to_ResultClassificationFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
