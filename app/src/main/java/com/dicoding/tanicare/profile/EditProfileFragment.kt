package com.dicoding.tanicare.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.FragmentEditProfileBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.FileUtil
import com.dicoding.tanicare.helper.SharedPreferencesManager
import com.dicoding.tanicare.helper.getMimeTypeFromFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var apiService: ApiService
    private var imageUri: Uri? = null
    private val pickImageRequestCode = 100

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditProfileBinding.bind(view)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
        val imageUrl = sharedPreferencesManager.getImageUrl()
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(binding.profileImage)
        binding.usernameInput.setText(sharedPreferencesManager.getUsername())
        binding.aboutInput.setText(sharedPreferencesManager.getAbout())

        apiService = ApiClient.getClient().create(ApiService::class.java)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val username = binding.usernameInput.text.toString()
                val about = binding.aboutInput.text.toString()
                if (sharedPreferencesManager.getUsername() == username || sharedPreferencesManager.getAbout() == about){
                    if (username.isEmpty() || about.isEmpty()) {
                        Toast.makeText(context, "Username and About cannot be empty", Toast.LENGTH_SHORT).show()
                    } else {
                        uploadChange(username, about)
                        findNavController().navigateUp()
                    }
                } else {
                    findNavController().navigateUp()
                }
            }
        })

        binding.backButton.setOnClickListener{
            val username = binding.usernameInput.text.toString()
            val about = binding.aboutInput.text.toString()
            if (sharedPreferencesManager.getUsername() != username || sharedPreferencesManager.getAbout() != about){
                if (username.isEmpty() || about.isEmpty()) {
                    Toast.makeText(context, "Username and About cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    uploadChange(username, about)
                    findNavController().navigateUp()
                }
            } else {
                findNavController().navigateUp()
            }
        }
        binding.profileImage.setOnClickListener{
            openGallery()
        }
    }

    private fun uploadChange(username: String, about: String) {
        sharedPreferencesManager.saveUserEdit(username, about)
        val authToken = sharedPreferencesManager.getAuthToken()
        val userMap = mapOf("name" to username)
        val aboutMap = mapOf("about" to about)
        apiService.editProfileName("Bearer $authToken", userMap).enqueue(object :
            Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    // Mengambil data dari response
                    val data = response.body()?.get("data") as? Map<*, *>
                    val name = data?.get("name")
                    val uid = data?.get("uid")

                    // Menampilkan pesan sukses dan data nama
                    Log.d("ProfileUpdate", "Name updated successfully: $name, UID: $uid")
                } else {
                    // Menangani error jika request gagal
                    Log.e("ProfileUpdate", "Failed to update name: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Log.e("ProfileUpdate", "Request failed: ${t.message}")
            }
        })

        apiService.editProfileAbout("Bearer $authToken", aboutMap).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    // Mengambil data dari response
                    val data = response.body()?.get("data") as? Map<*, *>
                    val about = data?.get("about")

                    // Menampilkan pesan sukses dan data about
                    Log.d("ProfileUpdate", "About updated successfully: $about")
                } else {
                    // Menangani error jika request gagal
                    Log.e("ProfileUpdate", "Failed to update about: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Log.e("ProfileUpdate", "Request failed: ${t.message}")
            }
        })
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
            binding.profileImage.setImageURI(imageUri) // Tampilkan gambar yang dipilih
            uploadProfilePhoto()
        }
    }

    private fun uploadProfilePhoto() {

        val token = "Bearer ${sharedPreferencesManager.getAuthToken()}"
        if (imageUri != null) {
            try {
                // Konversi Uri menjadi File
                val file = FileUtil.from(requireContext(), imageUri!!)
                Log.d("FilePath", "File path: ${file.absolutePath}")
                Log.d("ImageUri", "URI: $imageUri")
                val mimeType = getMimeTypeFromFile(file)
                val requestImageFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val imageMultipart =
                    MultipartBody.Part.createFormData("photo", file.name, requestImageFile)
                Log.d("MultipartFile", "File name: ${file.name}, Size: ${file.length()}")
                apiService.postProfileImage(token, imageMultipart)
                    .enqueue(object : Callback<Map<String, Any>> {
                        override fun onResponse(
                            call: Call<Map<String, Any>>,
                            response: Response<Map<String, Any>>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody?.get("status") == "success") {
                                    Toast.makeText(
                                        context,
                                        "Photo profil berhasil diubah!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    file.delete()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Gagal mengubah photo profile: ${responseBody?.get("message")}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Gagal mengubah foto profil.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                            Toast.makeText(
                                context,
                                "Terjadi kesalahan: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("ProfilePic", "Error: ${t.message}")
                        }
                    })
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memproses gambar: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("ProfilePic", "Error: ${e.message}")
            }
        }
    }

}