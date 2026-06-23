package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Reel::class, Comment::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reelDao(): ReelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reels_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.reelDao())
                }
            }
        }

        suspend fun populateDatabase(reelDao: ReelDao) {
            // Seed sample reels if database is empty
            val sampleReels = listOf(
                Reel(
                    videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    title = "Urban Exploring",
                    description = "Chasing sunsets and cityscapes. The concrete jungle never sleeps! #explore #citylife",
                    creatorName = "Alex Traveler",
                    creatorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                    likesCount = 1240,
                    sharesCount = 380,
                    isLiked = false
                ),
                Reel(
                    videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    title = "Tech Setup",
                    description = "My minimalist workspace setup for 2026. Loving the natural light! #workspace #coding #setup",
                    creatorName = "Dev_Zubair",
                    creatorAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=150&q=80",
                    likesCount = 3405,
                    sharesCount = 512,
                    isLiked = true
                ),
                Reel(
                    videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    title = "Bunny Adventures",
                    description = "Throwback to the classic animated comedy. Animation that defined an era! #bunny #animation #fun",
                    creatorName = "Cinephile Club",
                    creatorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
                    likesCount = 982,
                    sharesCount = 205,
                    isLiked = false
                ),
                Reel(
                    videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    title = "Surreal Dreams",
                    description = "Step into a world of machinery and endless dreams. Cinematic history in the making. #dreams #fantasy",
                    creatorName = "DreamWeaver",
                    creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                    likesCount = 571,
                    sharesCount = 89,
                    isLiked = false
                )
            )

            for (reel in sampleReels) {
                val reelId = reelDao.insertReel(reel)
                // Seed some comments for each reel
                reelDao.insertComment(
                    Comment(
                        reelId = reelId,
                        author = "Sarah K.",
                        text = "Wow, this looks absolutely stunning! What camera did you use?"
                    )
                )
                reelDao.insertComment(
                    Comment(
                        reelId = reelId,
                        author = "Marcus Tech",
                        text = "Clean aesthetics! Loved the transition."
                    )
                )
            }
        }
    }
}
