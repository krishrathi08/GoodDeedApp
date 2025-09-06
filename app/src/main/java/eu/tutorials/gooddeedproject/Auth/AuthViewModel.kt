package eu.tutorials.gooddeedproject.auth

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import eu.tutorials.gooddeedproject.data.UserDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ktx.firestore

data class AuthState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore
    private val dataStore = UserDataStore(application)

    // --- State for Auth Flow ---
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val savedEmail = dataStore.getEmail

    // --- State for Multi-Step Sign Up Form ---
    private val _signUpState = MutableStateFlow(UserSignUpState())
    val signUpState: StateFlow<UserSignUpState> = _signUpState.asStateFlow()



    init {
        auth.addAuthStateListener {
            _currentUser.value = it.currentUser
        }
    }

    // Update the signIn function to handle the "Remember Me" logic
    fun signIn(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            // Save or clear the email before attempting to sign in
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

    fun onSignUpInfoChanged(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        city: String? = null,
        pincode: String? = null,
        dob: String? = null,
        availability: Set<String>? = null,
        skills: Set<String>? = null,
        profilePicUri: Uri? = null // ADD THIS LINE
    ) {
        _signUpState.update {
            it.copy(
                name = name ?: it.name,
                email = email ?: it.email,
                phone = phone ?: it.phone,
                city = city ?: it.city,
                pincode = pincode ?: it.pincode,
                dob = dob ?: it.dob,
                availability = availability ?: it.availability,
                skills = skills ?: it.skills,
                profilePicUri = profilePicUri ?: it.profilePicUri // ADD THIS LINE
            )
        }
    }

    // This function now uses the data from the signUpState
    fun signUpWithDetails(password: String) {
        val state = _signUpState.value
        if (state.name.isBlank() || state.email.isBlank() || password.isBlank() || state.profilePicUri == null) {
            _authState.value = AuthState(error = "Please fill in all required fields, including profile picture.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                val result = auth.createUserWithEmailAndPassword(state.email, password).await()
                val user = result.user

                if (user != null) {
                    val userProfile = hashMapOf(
                        "uid" to user.uid,
                        "name" to state.name,
                        "email" to state.email,
                        "phone" to state.phone,
                        "city" to state.city,
                        "pincode" to state.pincode,
                        "dob" to state.dob,
                        "availability" to state.availability.toList(),
                        "skills" to state.skills.toList(),
                        "joinDate" to System.currentTimeMillis()
                        // TODO: Upload profilePicUri to Firebase Storage and save the URL
                    )

                    db.collection("users").document(user.uid).set(userProfile).await()
                }

                _authState.value = AuthState(isLoading = false, success = true)
            } catch (e: Exception) {
                _authState.value = AuthState(isLoading = false, error = e.message)
            }
        }
    }

    fun signUp(email: String, password: String) {
        // SignUp logic remains the same
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
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
    }

    data class UserSignUpState(
        val name: String = "",
        val email: String = "",
        val phone: String = "",
        val city: String = "",
        val pincode: String = "",
        val dob: String = "",
        val availability: Set<String> = emptySet(),
        val skills: Set<String> = emptySet(),
        val profilePicUri: Uri? = null
    )
}