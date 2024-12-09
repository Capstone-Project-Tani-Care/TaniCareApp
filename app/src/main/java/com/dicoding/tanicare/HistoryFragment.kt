package com.dicoding.tanicare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.tanicare.databinding.FragmentHistoryBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.SharedPreferencesManager
import com.dicoding.tanicare.helper.Thread
import com.dicoding.tanicare.helper.ThreadActions
import com.dicoding.tanicare.helper.ThreadAdapter
import com.dicoding.tanicare.helper.ThreadResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryFragment : Fragment(), ThreadAdapter.ThreadActionListener {
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var adapter: ThreadAdapter
    private val threadList = mutableListOf<Thread>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHistoryBinding.bind(view)
        binding.bottomNav.selectedItemId = R.id.historyFragment
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        adapter = ThreadAdapter(emptyList(), sharedPreferencesManager, this)
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPosts.adapter = adapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        binding.backButton.setOnClickListener{
            findNavController().navigateUp()
        }
        // Ambil daftar bookmark
        getBookmarksList()


        // Handle navigation item selection
        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profileFragment -> {
                    findNavController().navigate(R.id.action_historyFragment_to_profileFragment)
                    true
                }
                R.id.homeFragment -> {
                    findNavController().navigate(R.id.action_historyFragment_to_homeFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onLikeClicked(threadId: String) {
        // Gunakan fungsi dari ThreadActions untuk menambahkan like
        ThreadActions.postLike(requireContext(), threadId, sharedPreferencesManager)
    }

    override fun onBookmarkClicked(threadId: String) {
        // Gunakan fungsi dari ThreadActions untuk menambahkan bookmark
        ThreadActions.postBookmark(requireContext(), threadId, sharedPreferencesManager)
    }

    override fun onCommentClicked(threadId: String) {
        val bundle = Bundle().apply { putString("threadId", threadId) }
        findNavController().navigate(R.id.action_global_commentFragment, bundle)
    }

    private fun getBookmarksList() {
        val authToken = sharedPreferencesManager.getAuthToken() ?: run {
            Toast.makeText(requireContext(), "Auth Missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Panggil API untuk mendapatkan informasi bookmark
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.getBookmarks("Bearer $authToken").enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val data = response.body()?.get("data") as? List<Map<String, Any>>
                    if (data != null && data.isNotEmpty()) {
                        val bookmarkThreads = data.mapNotNull { it["threadId"] as? String }
                        if (bookmarkThreads.isNotEmpty()) {
                            fetchThreads(bookmarkThreads)
                        } else {
                            Toast.makeText(requireContext(), "No threadIds found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "No bookmarks found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch bookmarks: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchThreads(threadIds: List<String>) {
        val authToken = sharedPreferencesManager.getAuthToken()
        if (authToken.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Authorization token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Loop untuk setiap threadId yang ada di dalam threadIds
        threadIds.forEach { threadId ->
            val apiService = ApiClient.getClient().create(ApiService::class.java)
            apiService.getThreadDetailWithAuth("Bearer $authToken", threadId)
                .enqueue(object : Callback<ThreadResponse> {
                    override fun onResponse(call: Call<ThreadResponse>, response: Response<ThreadResponse>) {
                        if (response.isSuccessful) {
                            val threadDetail = response.body()?.data?.thread
                            threadDetail?.let {
                                val thread = Thread(
                                    username = it.username ?: "Unknown User",
                                    timestamp = it.createdAt ?: "Unknown Time",
                                    content = it.body ?: "",
                                    imageUrl = it.photoUrl,
                                    likeCount = it.upVotes ?: 0,
                                    commentCount = it.totalComments ?: 0,
                                    profileImage = it.photoProfileUrl,
                                    idThread = it.id
                                )
                                threadList.add(thread)
                                threadList.sortByDescending { it.timestamp }
                                adapter.updateData(threadList)
                            }
                        }
                    }

                    override fun onFailure(call: Call<ThreadResponse>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
        }
    }
}

