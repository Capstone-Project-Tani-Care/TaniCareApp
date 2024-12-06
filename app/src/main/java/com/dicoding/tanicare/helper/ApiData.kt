package com.dicoding.tanicare.helper

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val token: String,
    val userId: String
)

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String
)

data class SignupResponse(
    val error: String,
    val message: String,
    val name: String,
    val userId: String
)

data class ThreadResponse(
    val data: ThreadData,
    val message: String,
    val status: String
)

/*data class ThreadData(
    val id: String,
    val body: String,
    val createdAt: String,
    val ownerId: String,
    val photoUrl: String?,
    val totalComments: Int,
    val upVotesBy: List<String>
)*/

data class ApiResponse(
    val data: ThreadData,
    val message: String,
    val status: String
)

data class ThreadData(
    val body: String,
    val createdAt: String,
    val id: String,
    val ownerId: String,
    val photoUrl: String?,
    val totalComments: Int,
    val upVotesBy: List<String>
)


data class ProfileResponse(
    val data: ProfileData,
    val status: String
)

data class ProfileData(
    val about: String,
    val created_threads: List<String>,
    val location: String,
    val name: String,
    val profile_photo: String,
    val region_name: String
)