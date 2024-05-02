package com.example.musicapp.models

data class User(
    val uid: String,
    val email: String,
    val name: String,
    val profileImage: String,
    val userType: String,
    val timestamp: Long,
    val birthday: String,
    val checkFingerprint: Boolean,
) : java.io.Serializable