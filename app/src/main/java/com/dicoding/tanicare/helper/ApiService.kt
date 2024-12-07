package com.dicoding.tanicare.helper

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

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

    @POST("edit-profile/about")
    fun editProfileAbout(
        @Header("Authorization") token: String,
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
    fun getComments(): Call<List<Map<String, Any>>>

    @POST("up-vote")
    fun upVote(
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Call<Map<String, Any>>

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
        @Query("thread_id") thread_id: String
    ): Call<ThreadResponse>

    @GET("threads")
    fun getAllThread(
        @Header("Authorization") token: String?,
    ): Call<ThreadResponse>
}
