package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Comment
import com.example.data.Reel
import com.example.data.ReelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class AppTab {
    FEED, LIVE, UPLOAD, PROFILE
}

class ReelViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = ReelRepository(database.reelDao())

    private val sharedPrefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _username = MutableStateFlow(sharedPrefs.getString("username", "") ?: "")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow(sharedPrefs.getString("email", "") ?: "")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phoneNumber = MutableStateFlow(sharedPrefs.getString("phone_number", "") ?: "")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    val reels: StateFlow<List<Reel>> = repository.allReels
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentTab = MutableStateFlow(AppTab.FEED)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _uploadStatus = MutableStateFlow<UploadStatus>(UploadStatus.Idle)
    val uploadStatus: StateFlow<UploadStatus> = _uploadStatus.asStateFlow()

    private val _selectedReelIdForComments = MutableStateFlow<Long?>(null)
    val selectedReelIdForComments: StateFlow<Long?> = _selectedReelIdForComments.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeComments: StateFlow<List<Comment>> = _selectedReelIdForComments
        .flatMapLatest { reelId ->
            if (reelId != null) {
                repository.getCommentsForReel(reelId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun signup(user: String, mail: String, phone: String) {
        sharedPrefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("username", user)
            putString("email", mail)
            putString("phone_number", phone)
            apply()
        }
        _username.value = user
        _email.value = mail
        _phoneNumber.value = phone
        _isLoggedIn.value = true
    }

    fun logout() {
        sharedPrefs.edit().apply {
            putBoolean("is_logged_in", false)
            putString("username", "")
            putString("email", "")
            putString("phone_number", "")
            apply()
        }
        _username.value = ""
        _email.value = ""
        _phoneNumber.value = ""
        _isLoggedIn.value = false
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleLike(reel: Reel) {
        viewModelScope.launch {
            repository.toggleLike(reel)
        }
    }

    fun showCommentsForReel(reelId: Long?) {
        _selectedReelIdForComments.value = reelId
    }

    fun addComment(reelId: Long, author: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(reelId, author, text)
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            repository.removeComment(commentId)
        }
    }

    fun uploadReel(
        uri: Uri,
        title: String,
        description: String,
        creatorName: String
    ) {
        viewModelScope.launch {
            _uploadStatus.value = UploadStatus.Loading("Copying video file...")
            try {
                val context = getApplication<Application>().applicationContext
                val localFile = saveVideoLocally(context, uri)
                if (localFile != null && localFile.exists()) {
                    val localVideoUrl = localFile.absolutePath
                    _uploadStatus.value = UploadStatus.Loading("Saving to database...")
                    val newReel = Reel(
                        videoUrl = localVideoUrl,
                        title = title,
                        description = description,
                        creatorName = creatorName.ifBlank { "Anonymous Creator" },
                        creatorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80"
                    )
                    repository.insertReel(newReel)
                    _uploadStatus.value = UploadStatus.Success
                    _currentTab.value = AppTab.FEED // switch back to home feed!
                } else {
                    _uploadStatus.value = UploadStatus.Error("Failed to access/save the selected video file.")
                }
            } catch (e: Exception) {
                _uploadStatus.value = UploadStatus.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

    fun resetUploadStatus() {
        _uploadStatus.value = UploadStatus.Idle
    }

    private suspend fun saveVideoLocally(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val fileName = "reel_${System.currentTimeMillis()}.mp4"
            val destinationFile = File(context.filesDir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4KB buffer
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

sealed interface UploadStatus {
    object Idle : UploadStatus
    data class Loading(val message: String) : UploadStatus
    object Success : UploadStatus
    data class Error(val message: String) : UploadStatus
}
