package com.dicoding.tanicare.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.FragmentProfileBinding
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

        setupUI()
        setupNavigation()
        apiService = ApiClient.getClient().create(ApiService::class.java)

        getThreadList()
    }

    private fun setupUI() {
        binding.profileName.text = sharedPreferencesManager.getUsername()
        binding.location.text = sharedPreferencesManager.getZoneName()
        binding.aboutDescription.text = sharedPreferencesManager.getAbout()

        Glide.with(requireContext())
            .load(sharedPreferencesManager.getImageUrl())
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(binding.profileImage)

        binding.threadRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ThreadAdapter(emptyList(), sharedPreferencesManager, this)
        binding.threadRecyclerView.adapter = adapter
    }

    private fun setupNavigation() {
        binding.bottomNav.selectedItemId = R.id.profileFragment
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

        binding.iconBack.setOnClickListener { findNavController().navigateUp() }
        binding.iconSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }
        binding.editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    override fun onLikeClicked(threadId: String) {
        ThreadActions.postLike(requireContext(), threadId, sharedPreferencesManager)
    }

    override fun onCommentClicked(threadId: String) {
        val bundle = Bundle()
        bundle.putString("threadId", threadId)
        findNavController().navigate(R.id.action_global_commentFragment, bundle)
    }

    override fun onBookmarkClicked(threadId: String) {
        ThreadActions.postBookmark(requireContext(), threadId, sharedPreferencesManager)
    }

    private fun getThreadList() {
        val userId = sharedPreferencesManager.getUserId() ?: return
        apiService.getUserInfo(userId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val data = response.body()?.get("data") as? Map<String, Any>
                    val createdThreads = data?.get("created_threads") as? List<String>
                    if (!createdThreads.isNullOrEmpty()) {
                        fetchThreads(createdThreads)
                    } else {
                        Toast.makeText(requireContext(), "No threads found", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchThreads(threadIds: List<String>) {
        val authToken = sharedPreferencesManager.getAuthToken() ?: return
        threadIds.forEach { threadId ->
            apiService.getThreadDetailWithAuth("Bearer $authToken", threadId)
                .enqueue(object : Callback<ThreadResponse> {
                    override fun onResponse(call: Call<ThreadResponse>, response: Response<ThreadResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.data?.thread?.let { threadDetail ->
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
                            }
                        }
                    }

                    override fun onFailure(call: Call<ThreadResponse>, t: Throwable) {
                        Log.e("ProfileFragment", "Error fetching thread: ${t.message}")
                    }
                })
        }
    }
}
