package com.example.smarthome.ui.viewmodel

import android.util.Log
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

    private val _faceEmbeddings = MutableStateFlow<Map<String, Any>>(emptyMap())
    val faceEmbeddings: StateFlow<Map<String, Any>> = _faceEmbeddings

    private val _faceLabels = MutableStateFlow<List<String>>(emptyList())
    val faceLabels: StateFlow<List<String>> = _faceLabels

    init {
        listenForUserData()
        loadFaceLabels()
        fetchFaceEmbeddings()
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

    fun loadFaceLabels() {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Ambil field faceEmbeddings yang berupa Map<String, Any>?
                        val faceEmbeddingsMap = document.get("faceEmbeddings") as? Map<String, Any>

                        // Ambil key dari map sebagai label wajah
                        val labels = faceEmbeddingsMap?.keys?.toList() ?: emptyList()

                        _faceLabels.value = labels
                        Log.d("MeViewModel", "Loaded labels: $labels")
                    } else {
                        _faceLabels.value = emptyList()
                        Log.d("MeViewModel", "Document not found or empty")
                    }
                }
                .addOnFailureListener { exception ->
                    _faceLabels.value = emptyList()
                    Log.e("MeViewModel", "Failed to load labels", exception)
                }
        }
    }

    fun fetchFaceEmbeddings() {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    val embeddings = doc.get("faceEmbeddings") as? Map<String, Any>
                    _faceEmbeddings.value = embeddings ?: emptyMap()
                    Log.d("MeViewModel", "Fetched embeddings: $_faceEmbeddings")
                }
                .addOnFailureListener {
                    _faceEmbeddings.value = emptyMap()
                    Log.e("MeViewModel", "Failed to fetch embeddings", it)
                }
        }
    }

    fun addFaceEmbedding(label: String, embedding: Any) {
        user?.uid?.let { uid ->
            val updateData = mapOf("faceEmbeddings.$label" to embedding)

            firestore.collection("users").document(uid)
                .update(updateData)
                .addOnSuccessListener { fetchFaceEmbeddings() }
                .addOnFailureListener {
                    // Jika dokumen belum ada, buat dengan struktur awal
                    firestore.collection("users").document(uid)
                        .set(mapOf("faceEmbeddings" to mapOf(label to embedding)))
                        .addOnSuccessListener { fetchFaceEmbeddings() }
                        .addOnFailureListener { e ->
                            Log.e("MeViewModel", "Failed to create initial embedding document", e)
                        }
                }
        }
    }

    fun editFaceEmbedding(oldLabel: String, newLabel: String, embedding: Any) {
        user?.uid?.let { uid ->
            val userRef = firestore.collection("users").document(uid)
            firestore.runBatch { batch ->
                batch.update(userRef, "faceEmbeddings.$newLabel", embedding)
                batch.update(userRef, "faceEmbeddings.$oldLabel", com.google.firebase.firestore.FieldValue.delete())
            }.addOnSuccessListener {
                fetchFaceEmbeddings()
            }.addOnFailureListener {
                Log.e("MeViewModel", "Failed to edit embedding", it)
            }
        }
    }

    fun deleteFaceEmbedding(label: String) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .update("faceEmbeddings.$label", com.google.firebase.firestore.FieldValue.delete())
                .addOnSuccessListener {
                    fetchFaceEmbeddings()
                }.addOnFailureListener {
                    Log.e("MeViewModel", "Failed to delete embedding", it)
                }
        }
    }
}
