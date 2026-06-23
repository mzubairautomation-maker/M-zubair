package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reels")
data class Reel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoUrl: String,
    val title: String,
    val description: String,
    val creatorName: String,
    val creatorAvatar: String = "",
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val sharesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
