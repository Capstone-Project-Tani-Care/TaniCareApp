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



data class CommentsResponse(
    val data: CommentsData,
    val message: String,
    val status: String
)

data class CommentsData(
    val comments: List<CommentItem>
)

data class CommentItem(
    val content: String,
    val createdAt: String,
    val id: String,
    val owner: OwnerData,
    val threadId: String,
    val upVotesBy: List<String>
)

data class OwnerData(
    val id: String,
    val name: String,
    val photoProfileUrl: String?
)


data class ApiResponse(
    val data: ThreadData,
    val message: String,
    val status: String
)

data class ThreadResponse(
    val data: ThreadData?,
    val status: String?,
    val message: String?
)

data class ThreadData(
    val thread: ThreadDetail?
)

data class ThreadDetail(
    val body: String?,
    val createdAt: String?,
    val id: String,
    val ownerId: String?,
    val photoUrl: String?,
    val totalComments: Int?,
    val upVotes: Int?,
    val username: String?,
    val photoProfileUrl: String?
)

data class MainThreadResponse( // Ganti dari ThreadResponse
    val data: MainThreadData?,
    val status: String?,
    val message: String?
)

data class MainThreadData( // Ganti dari ThreadData
    val threads: List<MainThreadDetail>? // Ganti dari ThreadDetail
)

data class MainThreadDetail( // Ganti dari ThreadDetail
    val body: String?,
    val createdAt: String?,
    val id: String,
    val ownerId: String?,
    val photoUrl: String?,
    val totalComments: Int?,
    val upVotes: Int?,
    val username: String?,
    val photoProfileUrl: String?
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

data class UpvoteResponse(
    val status: String,
    val data: UpvoteData
)

data class UpvoteData(
    val upvotes: Int
)

data class DeleteResponse(
    val status: String,
    val data: DeleteData
)

data class DeleteData(
    val upvotes: Int
)
