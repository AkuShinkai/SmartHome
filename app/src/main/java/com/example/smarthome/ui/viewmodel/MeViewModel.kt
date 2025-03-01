package com.example.smarthome.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val user = auth.currentUser

    private val _name = MutableStateFlow("Loading...")
    val name: StateFlow<String> = _name

    private val _birthDate = MutableStateFlow("Select Birth")
    val birthDate: StateFlow<String> = _birthDate

    private val _gender = MutableStateFlow("Select Gender")
    val gender: StateFlow<String> = _gender

    private val _profileImage = MutableStateFlow<String?>(null)
    val profileImage: StateFlow<String?> = _profileImage

    init {
        listenForUserData()
    }

    private fun listenForUserData() {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .addSnapshotListener { document, _ ->
                    if (document != null && document.exists()) {
                        _name.value = document.getString("name") ?: "Unknown"
                        _birthDate.value = document.getString("birthDate") ?: "Select Birth"
                        _gender.value = document.getString("gender") ?: "Select Gender"
                        _profileImage.value = document.getString("profileImage")
                    }
                }
        }
    }

    fun updateProfile(name: String, birthDate: String, gender: String) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .update("name", name, "birthDate", birthDate, "gender", gender)
        }
    }

    fun updateProfileImage(imageUrl: String) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .update("profileImage", imageUrl)
                .addOnSuccessListener { _profileImage.value = imageUrl }
        }
    }
}
