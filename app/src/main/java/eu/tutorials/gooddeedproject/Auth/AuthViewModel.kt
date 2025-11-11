package eu.tutorials.gooddeedproject.auth

import android.app.Application
import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import eu.tutorials.gooddeedproject.data.UserDataStore
import eu.tutorials.gooddeedproject.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.parcelize.Parcelize
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.storage.ktx.storage
import java.util.UUID

// AuthState remains the same
data class AuthState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@Parcelize
data class UserSignUpState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val pincode: String = "",
    val dob: String = "",
    val availability: Set<String> = emptySet(),
    val skills: Set<String> = emptySet(),
    val profilePicUriString: String? = null // Changed from Uri to String
) : Parcelable

@Parcelize
data class OrganizerSignUpState(
    val name: String = "",
    val email: String = "",
    val regId: String = "",
    val phone: String = "",
    val city: String = "",
    val bio: String = "",
    val categories: Set<String> = emptySet(),
    val profilePicUriString: String? = null // Changed from Uri to String
) : Parcelable


// Change to ViewModel and inject SavedStateHandle
class AuthViewModel(
    application: Application, // Kept AndroidViewModel for your DataStore and Utils
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore
    private val dataStore = UserDataStore(application)

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    val savedEmail = dataStore.getEmail

    // The key change: State is now fetched from SavedStateHandle
    val userSignUpState: StateFlow<UserSignUpState> = savedStateHandle.getStateFlow("userSignUpState", UserSignUpState())
    val organizerSignUpState: StateFlow<OrganizerSignUpState> = savedStateHandle.getStateFlow("organizerSignUpState", OrganizerSignUpState())

    private var profileListenerRegistration: ListenerRegistration? = null

    val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                listenToUserProfile(firebaseUser.uid)
            } else {
                _userProfile.value = null
                removeUserProfileListener()
            }
            _currentUser.value = firebaseUser
        }
    }

    fun listenToUserProfile(uid: String) {
        // This function remains the same
        profileListenerRegistration?.remove()
        profileListenerRegistration = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AuthViewModel", "Profile listen failed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    _userProfile.value = snapshot.toObject<UserProfile>()
                }
            }
    }

    fun removeUserProfileListener() {
        // This function remains the same
        profileListenerRegistration?.remove()
        profileListenerRegistration = null
    }

    fun onUserSignUpInfoChanged(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        city: String? = null,
        pincode: String? = null,
        dob: String? = null,
        availability: Set<String>? = null,
        skills: Set<String>? = null,
        profilePicUri: Uri? = null
    ) {
        // Update the state in the SavedStateHandle
        val currentState = userSignUpState.value
        savedStateHandle["userSignUpState"] = currentState.copy(
            name = name ?: currentState.name,
            email = email ?: currentState.email,
            phone = phone ?: currentState.phone,
            city = city ?: currentState.city,
            pincode = pincode ?: currentState.pincode,
            dob = dob ?: currentState.dob,
            availability = availability ?: currentState.availability,
            skills = skills ?: currentState.skills,
            profilePicUriString = profilePicUri?.toString() ?: currentState.profilePicUriString
        )
    }

    fun onOrganizerSignUpInfoChanged(
        name: String? = null,
        email: String? = null,
        regId: String? = null,
        phone: String? = null,
        city: String? = null,
        bio: String? = null,
        categories: Set<String>? = null,
        profilePicUri: Uri? = null
    ) {
        // Update the state in the SavedStateHandle
        val currentState = organizerSignUpState.value
        savedStateHandle["organizerSignUpState"] = currentState.copy(
            name = name ?: currentState.name,
            email = email ?: currentState.email,
            regId = regId ?: currentState.regId,
            phone = phone ?: currentState.phone,
            city = city ?: currentState.city,
            bio = bio ?: currentState.bio,
            categories = categories ?: currentState.categories,
            profilePicUriString = profilePicUri?.toString() ?: currentState.profilePicUriString
        )
    }

    // This function remains largely the same, just reads from the new state holders

    fun signUpWithDetails(password: String, userType: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val email: String
            val name: String
            val profilePicUriString: String?

            if (userType == "USER") {
                val state = userSignUpState.value
                email = state.email; name = state.name; profilePicUriString = state.profilePicUriString
            } else {
                val state = organizerSignUpState.value
                email = state.email; name = state.name; profilePicUriString = state.profilePicUriString
            }

            if (name.isBlank() || email.isBlank() || password.isBlank() || profilePicUriString == null) {
                _authState.value = AuthState(error = "Please fill all fields, including profile picture.")
                return@launch
            }

            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {
                    // --- IMAGE UPLOAD LOGIC ---
                    val profilePicUri = profilePicUriString.toUri()
                    val storageRef = Firebase.storage.reference
                    val imageRef = storageRef.child("profile_pics/${user.uid}")

                    // Upload the file and get the download URL
                    val downloadUrl = imageRef.putFile(profilePicUri).await()
                        .storage.downloadUrl.await().toString()
                    // --- IMAGE UPLOAD LOGIC END ---

                    val userProfile = if (userType == "USER") {
                        val state = userSignUpState.value
                        UserProfile(
                            uid = user.uid, name = state.name, email = state.email,
                            joinDate = System.currentTimeMillis(), phone = state.phone, city = state.city,
                            availability = state.availability.toList(), skills = state.skills.toList(),
                            userType = "USER",
                            profilePicUrl = downloadUrl // ✅ Save the actual URL
                        )
                    } else {
                        val state = organizerSignUpState.value
                        UserProfile(
                            uid = user.uid, name = state.name, email = state.email,
                            joinDate = System.currentTimeMillis(), phone = state.phone, city = state.city,
                            userType = "ORGANIZER",
                            profilePicUrl = downloadUrl // ✅ Save the actual URL
                        )
                    }

                    db.collection("users").document(user.uid).set(userProfile).await()
                    listenToUserProfile(user.uid)
                }

                _authState.value = AuthState(isLoading = false, success = true)
            } catch (e: Exception) {
                _authState.value = AuthState(isLoading = false, error = e.message)
                Log.e("AuthViewModel", "Sign up failed", e)
            }
        }
    }

    // All functions below remain exactly the same as your original code
    fun signIn(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            if (rememberMe) {
                dataStore.saveEmail(email)
            } else {
                dataStore.clearEmail()
            }
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState(isLoading = false, success = true)
            } catch (e: Exception) {
                _authState.value = AuthState(isLoading = false, error = e.message)
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState()
    }

    fun signOut() {
        auth.signOut()
        removeUserProfileListener()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}