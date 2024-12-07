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
import com.dicoding.tanicare.helper.DeleteResponse
import com.dicoding.tanicare.helper.SharedPreferencesManager
import com.dicoding.tanicare.helper.Thread
import com.dicoding.tanicare.helper.ThreadAdapter
import com.dicoding.tanicare.helper.ThreadResponse
import com.dicoding.tanicare.helper.UpvoteResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileFragment : Fragment(R.layout.fragment_profile), ThreadAdapter.ThreadActionListener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var adapter: ThreadAdapter
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private val threadList = mutableListOf<Thread>()
    private lateinit var apiService: ApiService
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        binding = FragmentProfileBinding.bind(view)

        binding.bottomNav.selectedItemId = R.id.profileFragment
        binding.threadRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ThreadAdapter(emptyList(), sharedPreferencesManager, this) // Adapter kosong awal
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

        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.historyFragment -> {
                    findNavController().navigate(R.id.action_profileFragment_to_historyFragment)
                    true
                }
                R.id.homeFragment -> {
                    findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onLikeClicked(threadId: String) {
        Log.d("ProfileFragment", "onLikeClicked triggered for thread: $threadId")
        val authToken = sharedPreferencesManager.getAuthToken() // Ambil token otorisasi
        if (authToken.isNullOrEmpty()) {
            Log.e("AuthToken", "Authorization token is missing")
            Toast.makeText(requireContext(), "Authorization token is missing", Toast.LENGTH_SHORT).show()
            return
        }
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.upvoteThread("Bearer $authToken", threadId).enqueue(object : Callback<UpvoteResponse> {
            override fun onResponse(call: Call<UpvoteResponse>, response: Response<UpvoteResponse>) {
                Log.d("ProfileFragment", "Response: ${response.code()} - ${response.message()}")
                if (response.isSuccessful) {
                    val upvoteCount = response.body()?.data?.upvotes ?: 0
                    Log.d("ProfileFragment", "Upvotes: $upvoteCount")
                    Toast.makeText(requireContext(), "Thread upvoted. Total Upvotes: $upvoteCount", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ProfileFragment", "Upvote failed: ${response.message()}")
                    deleteLike(threadId)
                }
            }

            override fun onFailure(call: Call<UpvoteResponse>, t: Throwable) {
                Log.e("ProfileFragment", "Upvote request failed: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    override fun onCommentClicked(threadId: String) {
        val bundle = Bundle()
        bundle.putString("threadId", threadId)
        findNavController().navigate(R.id.action_global_commentFragment, bundle)
    }

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

    private fun deleteLike(threadId: String){
        Log.d("ProfileFragment", "Delete Triggered: $threadId")
        val authToken = sharedPreferencesManager.getAuthToken()
        if (authToken.isNullOrEmpty()) {
            Log.e("AuthToken", "Authorization token is missing")
            Toast.makeText(requireContext(), "Authorization token is missing", Toast.LENGTH_SHORT).show()
            return
        }
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.deleteLike("Bearer $authToken", threadId).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                Log.d("ProfileFragment", "Response: ${response.code()} - ${response.message()}")
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Thread Unliked", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ProfileFragment", "Unlike failed: ${response.message()}")
                    Toast.makeText(requireContext(), "Failed to unlike thread: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                Log.e("ProfileFragment", "Upvote request failed: ${t.message}")
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
                        if (response.isSuccessful) {
                            val threadDetail = response.body()?.data?.thread
                            if (threadDetail != null) {
                                val thread = Thread(
                                    username = threadDetail.username ?: "Unknown User",
                                    timestamp = threadDetail.createdAt ?: "Unknown Time",
                                    content = threadDetail.body ?: "",
                                    imageUrl = threadDetail.photoUrl,
                                    likeCount = threadDetail.upVotes ?: 0,
                                    commentCount = threadDetail.totalComments ?: 0,
                                    profileImage = threadDetail.photoProfileUrl,
                                    idThread = threadDetail.id
                                )
                                threadList.add(thread)
                                threadList.sortByDescending { it.timestamp }
                                adapter.updateData(threadList)
                            } else {
                                Log.e("ThreadError", "Thread detail is null for thread ID.")
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
