package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reelId: Long,
    val author: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
