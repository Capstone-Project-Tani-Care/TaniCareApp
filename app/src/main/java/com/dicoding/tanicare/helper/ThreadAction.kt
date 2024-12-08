package com.dicoding.tanicare.helper

import android.content.Context
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ThreadActions {

    // Fungsi untuk memposting like (upvote thread)
    fun postLike(context: Context, threadId: String, sharedPreferences: SharedPreferencesManager) {
        val authToken = sharedPreferences.getAuthToken() ?: return
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.upvoteThread("Bearer $authToken", threadId).enqueue(object :
            Callback<UpvoteResponse> {
            override fun onResponse(call: Call<UpvoteResponse>, response: Response<UpvoteResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Thread liked!", Toast.LENGTH_SHORT).show()
                } else {
                    deleteLike(context, threadId, sharedPreferences)
                }
            }

            override fun onFailure(call: Call<UpvoteResponse>, t: Throwable) {
                Toast.makeText(context, "Failed to like thread", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi untuk menghapus like (unlike thread)
    fun deleteLike(context: Context, threadId: String, sharedPreferences: SharedPreferencesManager) {
        val authToken = sharedPreferences.getAuthToken() ?: return
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.deleteLike("Bearer $authToken", threadId).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Thread Unliked", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to unlike thread: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                Toast.makeText(context, "Failed to unlike thread: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi untuk memposting bookmark
    fun postBookmark(context: Context, threadId: String, sharedPreferences: SharedPreferencesManager) {
        val authToken = sharedPreferences.getAuthToken() ?: return
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val requestBody = mapOf("threadId" to threadId)
        apiService.postBookmarks("Bearer $authToken", requestBody).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Bookmark Success", Toast.LENGTH_SHORT).show()
                } else {
                    deleteBookmark(context, threadId, sharedPreferences)
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(context, "Bookmark failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi untuk menghapus bookmark
    fun deleteBookmark(context: Context, threadId: String, sharedPreferences: SharedPreferencesManager) {
        val authToken = sharedPreferences.getAuthToken() ?: return
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.deleteBookmarks("Bearer $authToken", threadId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val message = responseBody?.get("message") as? String ?: "Bookmark removed successfully"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to remove bookmark: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(context, "Failed to remove bookmark: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
