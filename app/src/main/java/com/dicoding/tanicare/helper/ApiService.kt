package com.dicoding.tanicare.helper

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("signup")
    fun signup(@Body requestBody: Map<String, String>): Call<Map<String, Any>>

    @POST("login")
    fun login(@Body requestBody: Map<String, String>): Call<Map<String, Any>>

    @PUT("edit-profile/name")
    fun editProfileName(
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Call<Map<String, Any>>

    @PUT("edit-profile/location")
    fun editProfileLocation(
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Call<Map<String, Any>>

    @PUT("account/update-email")
    fun updateEmail(
        @Header("Authorization") token: String,  // Bearer token
        @Body body: Map<String, String>           // Body request
    ): Call<Map<String, Any>>

    @PUT("account/update-password")
    fun updatePassword(
        @Header("Authorization") token: String,  // Bearer token
        @Body body: Map<String, String>           // Body request
    ): Call<Map<String, Any>>

    @POST("edit-profile/about")
    fun editProfileAbout(
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Call<Map<String, Any>>

    @POST("refresh-token")
    fun refreshToken(
        @Body requestBody: Map<String, String>
    ): Call<Map<String, Any>>

    @Multipart
    @POST("threads")
    fun createThread(
        @Header("Authorization") token: String,
        @Part("body") body: RequestBody,
        @Part photo: MultipartBody.Part
    ): Call<Map<String, Any>>




    @GET("threads")
    fun getThreads(): Call<List<Map<String, Any>>>

    @POST("comments")
    fun postComment(
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Call<Map<String, Any>>

    @GET("comments")
    fun getComments(
        @Query("threadId") threadId: String
    ): Call<CommentsResponse>

    @POST("bookmarks")
    fun postBookmarks(
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Call<Map<String, Any>>

    @GET("bookmarks")
    fun getBookmarks(
        @Header("Authorization") token: String
    ): Call<Map<String, Any>>

    @DELETE("bookmarks/{threadId}")
    fun deleteBookmarks(
        @Header("Authorization") token: String,
        @Path("threadId") threadId: String
    ): Call<Map<String, Any>>

    @POST("/threads/{threadId}/upvote")
    fun upvoteThread(
        @Header("Authorization") token: String,
        @Path("threadId") threadId: String
    ): Call<UpvoteResponse>

    @DELETE("up-vote/{threadId}")
    fun deleteLike(
        @Header("Authorization") token: String,
        @Path("threadId") threadId: String
    ): Call<DeleteResponse>

    @GET("up-vote")
    fun getUpVotes(): Call<Map<String, Any>>

    @Multipart
    @POST("predict/{cropType}")
    fun predictCrop(
        @Path("cropType") cropType: String,
        @Part image: MultipartBody.Part
    ): Call<Map<String, Any>>

    @GET("region_name")
    fun getRegionName(@Query("query") query: String): Call<Map<String, Any>>

    @GET("region_code")
    fun getRegionCode(@Query("query") query: String): Call<Map<String, Any>>

    @GET("profile")
    fun getUserInfo(@Query("userId") userId: String): Call<Map<String, Any>>


    @GET("profile")
    fun getProfile(@Query("userId") userId: String): Call<ProfileResponse>

    /*@GET("threads")
    fun getThreadDetail(@Query("thread_id") threadId: String): Call<ThreadResponse>*/

    @GET("threads")
    fun getThreadDetailWithAuth(
        @Header("Authorization") token: String?,
        @Query("thread_id") threadId: String
    ): Call<ThreadResponse>

    @GET("threads")
    fun getThreadsWithPagination(
        @Header("Authorization") authHeader: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Call<MainThreadResponse>

    @GET("threads")
    fun getAllThread(
        @Header("Authorization") token: String?,
    ): Call<ThreadResponse>



}
