package com.dicoding.tanicare.thread

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.FragmentMainThreadBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.MainThreadResponse
import com.dicoding.tanicare.helper.SharedPreferencesManager
import com.dicoding.tanicare.helper.Thread
import com.dicoding.tanicare.helper.ThreadActions
import com.dicoding.tanicare.helper.ThreadAdapter
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
        binding = FragmentMainThreadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        apiService = ApiClient.getClient().create(ApiService::class.java)
        adapter = ThreadAdapter(emptyList(), sharedPreferencesManager, this)

        binding.recyclerViewPosts.adapter = adapter
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(context)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        binding.backButton.setOnClickListener{
            findNavController().navigateUp()
        }

        fetchThreads(currentPage)

        setupScrollListener()
    }

    // ThreadActions listener methods
    override fun onLikeClicked(threadId: String) {
        ThreadActions.postLike(requireContext(), threadId, sharedPreferencesManager)
    }

    override fun onCommentClicked(threadId: String) {
        val bundle = Bundle().apply {
            putString("threadId", threadId)
        }
        findNavController().navigate(R.id.action_global_commentFragment, bundle)
    }

    override fun onBookmarkClicked(threadId: String) {
        ThreadActions.postBookmark(requireContext(), threadId, sharedPreferencesManager)
    }

    private fun fetchThreads(page: Int = 1, limit: Int = 10) {
        val authToken = sharedPreferencesManager.getAuthToken()
        if (authToken.isNullOrEmpty()) {
            Log.e("AuthToken", "Authorization token is missing")
            Toast.makeText(requireContext(), "Authorization token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.getThreadsWithPagination("Bearer $authToken", page, limit).enqueue(object : Callback<MainThreadResponse> {
            override fun onResponse(call: Call<MainThreadResponse>, response: Response<MainThreadResponse>) {
                if (response.isSuccessful) {
                    val threads = response.body()?.data?.threads
                    if (!threads.isNullOrEmpty()) {
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
                    }
                } else {
                    Log.e("ThreadError", "Request failed with status: ${response.code()}")
                }
                isLoading = false
            }

            override fun onFailure(call: Call<MainThreadResponse>, t: Throwable) {
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

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisibleItemPosition == totalItemCount - 1 && !isLoading) {
                    isLoading = true
                    currentPage++
                    fetchThreads(page = currentPage, limit = 10)
                }
            }
        })
    }
}

