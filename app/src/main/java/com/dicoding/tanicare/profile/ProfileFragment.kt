package com.dicoding.tanicare.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.FragmentProfileBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiResponse
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.SharedPreferencesManager
import com.dicoding.tanicare.helper.Thread
import com.dicoding.tanicare.helper.ThreadAdapter
import com.dicoding.tanicare.helper.ThreadResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var adapter: ThreadAdapter
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private val threadList = mutableListOf<Thread>()
    private lateinit var apiService: ApiService
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        binding = FragmentProfileBinding.bind(view)

        // Setup RecyclerView
        binding.threadRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ThreadAdapter(emptyList()) // Adapter kosong awal
        binding.threadRecyclerView.adapter = adapter

        binding.profileName.text = sharedPreferencesManager.getUsername()
        binding.location.text = sharedPreferencesManager.getZoneName()
        binding.aboutDescription.text = sharedPreferencesManager.getAbout()

        val imageUrl = sharedPreferencesManager.getImageUrl()
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.ic_profile_placeholder) // Gambar placeholder
            .error(R.drawable.ic_profile_placeholder) // Gambar jika gagal memuat
            .into(binding.profileImage)
        apiService = ApiClient.getClient().create(ApiService::class.java)

        getThreadList()
        //test()
        //checkThreadResponse("thread-3a2e5ae6-b574-4db9-be56-07d4cb060e3c")



        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    findNavController().navigateUp()
            }
        })
        // Navigasi tombol back
        binding.iconBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Navigasi ke halaman pengaturan
        binding.iconSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        // Navigasi ke halaman edit profil
        binding.editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }


   /* private fun loadUserInfo() {
        binding.profileName.text = sharedPreferencesManager.getUsername()
        binding.location.text = sharedPreferencesManager.getZoneName()
        binding.aboutDescription.text = sharedPreferencesManager.getAbout()

        Glide.with(requireContext())
            .load(sharedPreferencesManager.getImageUrl())
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(binding.profileImage)
    }*/


    private fun getThreadList() {
        val userId = sharedPreferencesManager.getUserId() ?: run {
            Toast.makeText(requireContext(), "User ID is missing", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("ProfileFragment", "User ID: $userId")

        // Panggil API untuk mendapatkan informasi user
        apiService.getUserInfo(userId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    Log.d("ProfileFragment", "API Response: ${response.body()}")
                    val data = response.body()?.get("data") as? Map<String, Any>
                    if (data != null) {
                        val createdThreads = data["created_threads"] as? List<String>
                        if (!createdThreads.isNullOrEmpty()) {
                            Log.d("ProfileFragment", "Created Threads: $createdThreads")
                            fetchThreads(createdThreads) // Lanjutkan ke fetchThreads
                        } else {
                            Toast.makeText(requireContext(), "No threads found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to parse user data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch user info: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchThreads(threadIds: List<String>) {
        val authToken = sharedPreferencesManager.getAuthToken() // Ambil token otorisasi

        if (authToken.isNullOrEmpty()) {
            Log.e("AuthToken", "Authorization token is missing")
            Toast.makeText(requireContext(), "Authorization token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Loop untuk setiap threadId yang ada di dalam threadIds
        threadIds.forEach { threadId ->
            apiService.getThreadDetailWithAuth("Bearer $authToken", threadId)
                .enqueue(object : Callback<ThreadResponse> {
                    override fun onResponse(call: Call<ThreadResponse>, response: Response<ThreadResponse>) {
                        Log.d("ResponseStatus", "Response Code: ${response.code()}")
                        Log.d("ResponseBody", "Response Body: ${response.body()}")
                        if (response.isSuccessful) {
                            val threadData = response.body()?.data
                            if (threadData != null) {
                                // Bangun objek Thread dari data yang diterima
                                val thread = Thread(
                                    username = sharedPreferencesManager.getUsername(), // Tambahkan data user jika tersedia
                                    timestamp = threadData.createdAt,
                                    content = threadData.body,
                                    imageUrl = threadData.photoUrl, // Bisa null
                                    likeCount = threadData.upVotesBy.size, // Jumlah upVotes
                                    commentCount = threadData.totalComments // Jumlah komentar
                                )

                                // Tambahkan thread ke list
                                threadList.add(thread)

                                // Update adapter dengan data terbaru
                                adapter.updateData(threadList)
                            } else {
                                Log.e("ThreadError", "Thread data is null for thread ID: $threadId")
                            }
                        } else {
                            Log.e("ThreadError", "Request failed with status: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<ThreadResponse>, t: Throwable) {
                        t.printStackTrace()
                        Log.e("Request Failure", "Error: ${t.message}")
                    }
                })
        }
    }

}
