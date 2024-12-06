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
