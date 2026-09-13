package com.example.aquitabom.data.model

data class Post(
    val id: Int,
    val userName: String,
    val userProfilePic: String,
    val restaurantName: String,
    val location: String,
    val rating: Double,
    val postImage: String,
    val status: String, // e.g., "CHEIO QUE SÓ"
    val description: String,
    val likes: Int,
    val comments: Int,
    val timeAgo: String,
    val waitTime: String? = null
)
