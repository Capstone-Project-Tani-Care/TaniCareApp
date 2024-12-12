package com.dicoding.tanicare

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.tanicare.databinding.FragmentCommentBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.CommentsResponse
import com.dicoding.tanicare.helper.ItemCommentAdapter
import com.dicoding.tanicare.helper.SharedPreferencesManager
import com.dicoding.tanicare.helper.ThreadAdapter
import com.dicoding.tanicare.helper.ThreadResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CommentFragment : Fragment() {
    private lateinit var binding: FragmentCommentBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var apiService: ApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemCommentAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        binding = FragmentCommentBinding.bind(view)
        apiService = ApiClient.getClient().create(ApiService::class.java)
        adapter = ItemCommentAdapter(emptyList())
        val threadId = arguments?.getString("threadId")
        binding.rvComment.adapter = adapter
        binding.rvComment.layoutManager = LinearLayoutManager(context)
        if (threadId != null){
            fetchThreads(threadId)
            fetchComments(threadId)
        }
        binding.backButton.setOnClickListener{
            findNavController().navigateUp()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
        binding.editComment.setOnTouchListener { v, event ->
            val drawableEnd = binding.editComment.compoundDrawables[2]  // 2 untuk drawableEnd

            if (drawableEnd != null && event.action == MotionEvent.ACTION_UP) {
                val drawableEndRight = binding.editComment.right - binding.editComment.paddingRight - binding.editComment.paddingRight
                val drawableEndLeft = drawableEndRight - drawableEnd.intrinsicWidth
                val drawableEndTop = binding.editComment.paddingTop
                val drawableEndBottom = binding.editComment.bottom - binding.editComment.paddingBottom

                if (event.x >= drawableEndLeft && event.x <= drawableEndRight &&
                    event.y >= drawableEndTop && event.y <= drawableEndBottom) {

                    if(threadId != null){
                        postComment(threadId)
                    }
                    return@setOnTouchListener true
                }
            }

            return@setOnTouchListener false
        }
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

    private fun fetchComments(threadId: String) {
        apiService.getComments(threadId).enqueue(object : Callback<CommentsResponse> {
            override fun onResponse(
                call: Call<CommentsResponse>,
                response: Response<CommentsResponse>
            ) {
                Log.d("Response", "Response: $response")
                if (response.isSuccessful) {
                    val commentsResponse = response.body()
                    Log.d("Response", "Response Body: ${commentsResponse}") // Menambahkan log untuk mengecek response body

                    // Mengecek apakah commentsResponse atau data nya null
                    if (commentsResponse == null) {
                        Log.e("Response", "Received response body is null.")
                        return
                    }

                    if (commentsResponse.status == "success") {
                        val comments = commentsResponse.data.comments

                        // Mengecek apakah komentar kosong atau null
                        if (comments.isNullOrEmpty()) {
                            Log.e("Response", "No comments found in the response.")
                        } else {
                            // Menambahkan data komentar ke dalam adapter
                            adapter = ItemCommentAdapter(comments)
                            binding.rvComment.adapter = adapter
                            Log.d("Response", "Comments loaded: ${comments.size} items.")
                        }
                    } else {
                        // Menangani jika status tidak "success"
                        Toast.makeText(requireContext(), "Error: ${commentsResponse.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Menangani kesalahan status HTTP
                    Toast.makeText(requireContext(), "API call failed with code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CommentsResponse>, t: Throwable) {
                // Menangani kegagalan pada API call
                Toast.makeText(requireContext(), "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API Failure", "Error: ${t.message}")
            }
        })
    }

    private fun postComment(threadId: String){
        val content = binding.editComment.text.toString()
            val authToken = sharedPreferencesManager.getAuthToken()
            if (authToken == null){
                return
            }
            val requestBody = mapOf(
                "threadId" to threadId,
                "content" to content
            )
            val call = apiService.postComment("Bearer $authToken", requestBody)
            call.enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        // Response sukses, periksa apakah statusnya success
                        val responseBody = response.body()
                        val status = responseBody?.get("status") as? String

                        if (status == "success") {
                            // Berhasil
                            println("Komentar berhasil diposting!")
                            fetchComments(threadId)
                            binding.editComment.setText("")
                        } else {
                            // Gagal
                            println("Gagal memposting komentar, status: $status")
                        }
                    } else {
                        // Gagal jika status code bukan 2xx
                        println("Gagal memposting komentar, error code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    // Tangani kegagalan jika terjadi error dalam pengiriman request
                    println("Error: ${t.localizedMessage}")
                }
            })
    }

}