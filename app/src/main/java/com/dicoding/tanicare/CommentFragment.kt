package com.dicoding.tanicare

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dicoding.tanicare.databinding.FragmentCommentBinding
import com.dicoding.tanicare.databinding.FragmentProfileBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.SharedPreferencesManager
import com.dicoding.tanicare.helper.Thread
import com.dicoding.tanicare.helper.ThreadResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CommentFragment : Fragment() {
    private lateinit var binding: FragmentCommentBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var apiService: ApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        binding = FragmentCommentBinding.bind(view)
        apiService = ApiClient.getClient().create(ApiService::class.java)
        val threadId = arguments?.getString("threadId")
        if (threadId != null){
            fetchThreads(threadId)
        }
        binding.backButton.setOnClickListener{
            findNavController().navigateUp()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun fetchThreads(threadIds: String) {
        val authToken = sharedPreferencesManager.getAuthToken() // Ambil token otorisasi

        if (authToken.isNullOrEmpty()) {
            Log.e("AuthToken", "Authorization token is missing")
            Toast.makeText(requireContext(), "Authorization token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.getThreadDetailWithAuth("Bearer $authToken", threadIds)
            .enqueue(object : Callback<ThreadResponse> {
                override fun onResponse(call: Call<ThreadResponse>, response: Response<ThreadResponse>) {
                    if (response.isSuccessful) {
                        val threadDetail = response.body()?.data?.thread
                        if (threadDetail != null) {
                            // Bind data ke UI
                            binding.tvName.text = threadDetail.username ?: "Unknown User"
                            binding.tvTimestamp.text = threadDetail.createdAt ?: "Unknown Time"
                            binding.tvPostDescription.text = threadDetail.body ?: ""

                            // Set image profile jika ada
                            Glide.with(requireContext())
                                .load(threadDetail.photoProfileUrl)
                                .into(binding.ivProfile)

                            // Set post image jika ada
                            if (!threadDetail.photoUrl.isNullOrEmpty()) {
                                binding.ivPostImage.visibility = View.VISIBLE
                                Glide.with(requireContext())
                                    .load(threadDetail.photoUrl)
                                    .into(binding.ivPostImage)
                            } else {
                                binding.ivPostImage.visibility = View.GONE
                            }
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