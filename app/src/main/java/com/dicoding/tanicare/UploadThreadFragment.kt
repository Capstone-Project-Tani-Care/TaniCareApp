package com.dicoding.tanicare

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

import com.dicoding.tanicare.databinding.FragmentUploadThreadBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.FileUtil
import com.dicoding.tanicare.helper.SharedPreferencesManager
import com.dicoding.tanicare.helper.getMimeTypeFromFile
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException


class UploadThreadFragment : Fragment() {

    private var imageUri: Uri? = null
    private lateinit var binding: FragmentUploadThreadBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private val pickImageRequestCode = 100
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadThreadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        apiService = ApiClient.getClient().create(ApiService::class.java)

        binding.tvUsername.text = sharedPreferencesManager.getUsername()
        Glide.with(requireContext())
            .load(sharedPreferencesManager.getImageUrl())
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(binding.profilePicture)
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.addPhoto.setOnClickListener {
            openGallery()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
        binding.btnPost.setOnClickListener {
            val description = binding.etThreadBody.text.toString()
            if (description.isNotEmpty()) {
                uploadThread(description)
            } else {
                Toast.makeText(requireContext(), "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, pickImageRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageRequestCode && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.addPhoto.setImageURI(imageUri) // Tampilkan gambar yang dipilih
        }
    }

    private fun uploadThread(description: String) {

        val token = "Bearer ${sharedPreferencesManager.getAuthToken()}"
        val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        if (imageUri != null) {
            try {
                // Konversi Uri menjadi File
                val file = FileUtil.from(requireContext(), imageUri!!)
                Log.d("FilePath", "File path: ${file.absolutePath}")
                Log.d("ImageUri", "URI: $imageUri")
                val mimeType = getMimeTypeFromFile(file)
                val requestImageFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val imageMultipart = MultipartBody.Part.createFormData("photo", file.name, requestImageFile)
                Log.d("MultipartFile", "File name: ${file.name}, Size: ${file.length()}")
                apiService.createThread(token, descriptionBody, imageMultipart)
                    .enqueue(object : Callback<Map<String, Any>> {
                        override fun onResponse(
                            call: Call<Map<String, Any>>,
                            response: Response<Map<String, Any>>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody?.get("status") == "success") {
                                    Toast.makeText(context, "Thread berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                    file.delete()
                                    findNavController().navigateUp()
                                } else {
                                    Toast.makeText(context, "Gagal membuat thread: ${responseBody?.get("message")}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Gagal membuat thread, coba lagi.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                            Toast.makeText(context, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
                            Log.e("UploadThread", "Error: ${t.message}")
                        }
                    })
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memproses gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("UploadThread", "Error: ${e.message}")
            }
        } else {
            apiService.createThread(token, descriptionBody, MultipartBody.Part.createFormData("photo", "")).enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody?.get("status") == "success") {
                            Toast.makeText(context, "Thread berhasil dibuat!", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        } else {
                            Toast.makeText(context, "Gagal membuat thread: ${responseBody?.get("message")}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Gagal membuat thread, coba lagi.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(context, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UploadThread", "Error: ${t.message}")
                }
            })
        }
    }
}
