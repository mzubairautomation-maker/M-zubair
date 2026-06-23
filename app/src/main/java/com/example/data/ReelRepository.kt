package com.example.data

import kotlinx.coroutines.flow.Flow

class ReelRepository(private val reelDao: ReelDao) {
    val allReels: Flow<List<Reel>> = reelDao.getAllReels()

    fun getCommentsForReel(reelId: Long): Flow<List<Comment>> {
        return reelDao.getCommentsForReel(reelId)
    }

    suspend fun insertReel(reel: Reel): Long {
        return reelDao.insertReel(reel)
    }

    suspend fun updateReel(reel: Reel) {
        reelDao.updateReel(reel)
    }

    suspend fun toggleLike(reel: Reel) {
        val updated = reel.copy(
            isLiked = !reel.isLiked,
            likesCount = if (reel.isLiked) reel.likesCount - 1 else reel.likesCount + 1
        )
        reelDao.updateReel(updated)
    }

    suspend fun addComment(reelId: Long, author: String, text: String): Long {
        val comment = Comment(reelId = reelId, author = author, text = text)
        return reelDao.insertComment(comment)
    }

    suspend fun removeComment(commentId: Long) {
        reelDao.deleteComment(commentId)
    }
}
