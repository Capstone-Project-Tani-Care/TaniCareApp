package com.dicoding.tanicare.thread

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.FragmentMainThreadBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.DeleteResponse
import com.dicoding.tanicare.helper.MainThreadResponse
import com.dicoding.tanicare.helper.SharedPreferencesManager
import com.dicoding.tanicare.helper.Thread
import com.dicoding.tanicare.helper.ThreadAdapter
import com.dicoding.tanicare.helper.UpvoteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainThreadFragment : Fragment(), ThreadAdapter.ThreadActionListener {
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var binding: FragmentMainThreadBinding
    private lateinit var apiService: ApiService
    private lateinit var adapter: ThreadAdapter

    private var isLoading = false
    private var currentPage = 1
    private val threadList = mutableListOf<Thread>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Gunakan view binding untuk menginflate layout
        binding = FragmentMainThreadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        apiService = ApiClient.getClient().create(ApiService::class.java)
        adapter = ThreadAdapter(emptyList(), sharedPreferencesManager, this)

        // Set up RecyclerView dengan adapter dan layout manager
        binding.recyclerViewPosts.adapter = adapter
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(context)

        // Setup back button behavior
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
        // Panggil fungsi untuk memuat data pertama kali
        fetchThreads(currentPage)

        // Tambahkan scroll listener untuk paginasi
        setupScrollListener()
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
                    deleteLike(threadId)
                }
            }

            override fun onFailure(call: Call<UpvoteResponse>, t: Throwable) {
                Log.e("ProfileFragment", "Upvote request failed: ${t.message}")
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

    override fun onCommentClicked(threadId: String) {
        val bundle = Bundle()
        bundle.putString("threadId", threadId)
        findNavController().navigate(R.id.action_global_commentFragment, bundle)
    }

    private fun fetchThreads(page: Int = 1, limit: Int = 10) {
        val authToken = sharedPreferencesManager.getAuthToken()

        if (authToken.isNullOrEmpty()) {
            Log.e("AuthToken", "Authorization token is missing")
            Toast.makeText(requireContext(), "Authorization token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.getThreadsWithPagination("Bearer $authToken", page, limit)
            .enqueue(object : Callback<MainThreadResponse> { // Ganti ke MainThreadResponse
                override fun onResponse(call: Call<MainThreadResponse>, response: Response<MainThreadResponse>) {
                    if (response.isSuccessful) {
                        // Parsing daftar threads
                        val threads = response.body()?.data?.threads // Ganti ke MainThreadData dan MainThreadDetail
                        Log.d("Test", "Response: $response")
                        Log.d("Test", "Threads: $threads")

                        if (!threads.isNullOrEmpty()) {
                            // Tambahkan data baru ke daftar
                            threads.forEach { threadDetail ->
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
                            }
                            threadList.sortByDescending { it.timestamp } // Urutkan berdasarkan timestamp
                            adapter.updateData(threadList) // Perbarui adapter
                        } else {
                            Log.d("Threads", "No threads found on page $page.")
                        }
                    } else {
                        Log.e("ThreadError", "Request failed with status: ${response.code()}")
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<MainThreadResponse>, t: Throwable) { // Ganti ke MainThreadResponse
                    t.printStackTrace()
                    Log.e("Request Failure", "Error: ${t.message}")
                    isLoading = false
                }
            })
    }



    private fun setupScrollListener() {
        binding.recyclerViewPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Mendapatkan layout manager yang digunakan oleh RecyclerView
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // Mengecek apakah posisi item terakhir yang terlihat sudah mencapai item terakhir di daftar
                // dan memastikan tidak dalam status loading
                if (lastVisibleItemPosition == totalItemCount - 1 && !isLoading) {
                    // Menandai bahwa sedang memuat data
                    isLoading = true
                    // Menambah halaman yang sedang diminta
                    currentPage++
                    // Memanggil fungsi untuk memuat lebih banyak data
                    fetchThreads(page = currentPage, limit = 10)
                }
            }
        })
    }

}
