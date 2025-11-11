package eu.tutorials.gooddeedproject.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Badge(val imageRes: Int)

class ProfileViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val document = db.collection("users").document(userId).get().await()
                if (document.exists()) {
                    val userProfile = document.toObject(UserProfile::class.java)
                    _profile.value = userProfile
                } else {
                    Log.e("ProfileViewModel", "User document does not exist for UID: $userId")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching user profile", e)
            }
        }
    }
}