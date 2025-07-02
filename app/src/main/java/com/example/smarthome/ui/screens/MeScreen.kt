package com.example.smarthome.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smarthome.session.SessionManager
import com.example.smarthome.ui.viewmodel.MeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Calendar
import java.util.UUID

@Composable
fun MeScreen(navController: NavController?, meViewModel: MeViewModel = viewModel()) {

    val BackgroundColor = Color(0xFFF3F3F3)

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    val name by meViewModel.name.collectAsState()
    val birthDate by meViewModel.birthDate.collectAsState()
    val gender by meViewModel.gender.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedBirthDate by remember { mutableStateOf("") }
    var editedGender by remember { mutableStateOf("") }

    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var showConfirmLogoutDialog by remember { mutableStateOf(false) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    val profileImage by meViewModel.profileImage.collectAsState()

    val faceLabels by meViewModel.faceLabels.collectAsState()

    var pendingAddedFaces by remember { mutableStateOf(mutableMapOf<String, Any>()) }
    var pendingEditedFaces by remember { mutableStateOf(mutableMapOf<String, Pair<String, Any>>()) }
    var pendingDeletedFaces by remember { mutableStateOf(mutableSetOf<String>()) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            newImageUri = it
            showConfirmDialog = true
        }
    }

    LaunchedEffect(Unit) {
        meViewModel.fetchFaceEmbeddings()
        meViewModel.loadFaceLabels()
    }

    var isLoading by remember { mutableStateOf(true) }
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Change") },
            text = { Text("Are you sure you want to change your profile picture?") },
            confirmButton = {
                Button(onClick = {
                    newImageUri?.let { uploadImageToFirebase(context, it, meViewModel) }
                    showConfirmDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    var showConfirmEditDialog by remember { mutableStateOf(false) }
    val isChanged = name != editedName || birthDate != editedBirthDate || gender != editedGender ||
            pendingAddedFaces.isNotEmpty() || pendingEditedFaces.isNotEmpty() || pendingDeletedFaces.isNotEmpty()

    LaunchedEffect(name, birthDate, gender) {
        editedName = name
        editedBirthDate = birthDate
        editedGender = gender
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0E48A1), Color(0xff7e02d6), Color(0xffd705fc))
                        ),
                        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(130.dp) // Ukuran lebih besar dari gambar
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profileImage.isNullOrEmpty()) {
                            AsyncImage(
                                model = profileImage,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.Gray, CircleShape),
                                contentScale = ContentScale.Crop,
                                onLoading = { isLoading = true },
                                onSuccess = { isLoading = false }
                            )
                        } else {
                            isLoading = false
                            // Gunakan ikon default jika gambar null atau kosong
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Default Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                        // Animasi loading (progress indicator)
                        this@Column.AnimatedVisibility(visible = isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.Gray,
                                strokeWidth = 4.dp
                            )
                        }
                    }
                    this@Column.AnimatedVisibility(visible = isEditing) {
                        Row {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color.Gray, CircleShape)
                                    .clickable { launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile Picture",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color.Gray, CircleShape)
                                    .clickable {
                                        showConfirmDeleteDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Delete Profile Picture",
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (showConfirmDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showConfirmDeleteDialog = false },
                                    title = { Text("Confirm Deletion") },
                                    text = { Text("Are you sure you want to delete your profile picture?") },
                                    confirmButton = {
                                        Button(onClick = {
                                            deleteProfileImage(meViewModel)
                                            showConfirmDeleteDialog = false
                                        }) {
                                            Text("Yes, Delete")
                                        }
                                    },
                                    dismissButton = {
                                        OutlinedButton(onClick = { showConfirmDeleteDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileTextField("Nama", editedName, isEditing) { editedName = it }
                ProfileTextField("Email", auth.currentUser?.email ?: "user@gmail.com", false) {}

                ProfileDatePicker("Tanggal Lahir", editedBirthDate, isEditing) { editedBirthDate = it }
                ProfileDropdownField("Jenis Kelamin", editedGender, isEditing) { editedGender = it }

                Spacer(modifier = Modifier.height(5.dp))

                RegisteredFacesSection(
                    navController = navController,
                    faceLabels = faceLabels,
                    isEditing = isEditing,
                    pendingAddedFaces = pendingAddedFaces,
                    pendingDeletedFaces = pendingDeletedFaces,
                    faceEmbeddings = meViewModel.faceEmbeddings.value,
                    onAddFace = { label, embedding -> pendingAddedFaces[label] = embedding },
                    onDeleteFace = { label -> pendingDeletedFaces.add(label) }
                )

                AnimatedVisibility(visible = !isEditing) {
                    Button(
                        onClick = { isEditing = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profil", color = Color.White)
                    }
                }

                if (showConfirmEditDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmEditDialog = false },
                        title = { Text("Simpan Perubahan") },
                        text = { Text("Apakah kamu yakin ingin menyimpan perubahan?") },
                        confirmButton = {
                            Button(onClick = {
                                isEditing = false
                                // Update profil
                                meViewModel.updateProfile(editedName, editedBirthDate, editedGender)

                                pendingAddedFaces.forEach { (label, embedding) ->
                                    meViewModel.addFaceEmbedding(label, embedding)
                                }
                                pendingEditedFaces.forEach { (oldLabel, pair) ->
                                    val (newLabel, embedding) = pair
                                    meViewModel.editFaceEmbedding(oldLabel, newLabel, embedding)
                                }
                                pendingDeletedFaces.forEach { label ->
                                    meViewModel.deleteFaceEmbedding(label)
                                }

                                pendingAddedFaces.clear()
                                pendingEditedFaces.clear()
                                pendingDeletedFaces.clear()

                                Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()

                                showConfirmEditDialog = false
                            }) {
                                Text("Ya")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showConfirmEditDialog = false }) {
                                Text("Batal")
                            }
                        }
                    )
                }

                AnimatedVisibility(visible = isEditing) {
                    Column {
                        Button(onClick = { showConfirmEditDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isChanged)
                        {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simpan Perubahan", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(onClick = {
                            editedName = name
                            editedBirthDate = birthDate
                            editedGender = gender
                            isEditing = false
                        },
                            border = BorderStroke(1.dp, Color.Gray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Batalkan Perubahan", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                ChangePasswordButton(
                    email = FirebaseAuth.getInstance().currentUser?.email ?: "",
                    navController = navController
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (showConfirmLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmLogoutDialog = false },
                        title = { Text("Konfirmasi Logout") },
                        text = { Text("Apakah kamu yakin ingin logout?") },
                        confirmButton = {
                            Button(onClick = {
                                coroutineScope.launch {
                                    sessionManager.logout()
                                    navController?.navigate("auth_screen") {
                                        popUpTo("home_screen") { inclusive = true }
                                    }
                                }
                                showConfirmLogoutDialog = false
                            }) {
                                Text("Ya, Logout")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showConfirmLogoutDialog = false }) {
                                Text("Batal")
                            }
                        }
                    )
                }

                OutlinedButton(
                    onClick = { showConfirmLogoutDialog = true },
                    border = BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChangePasswordButton(
    email: String,
    navController: NavController?
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Ganti Password") },
            text = {
                Text("Jika Anda melakukan reset password, akun akan logout dan Anda harus login ulang. Lanjutkan?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Email reset dikirim", Toast.LENGTH_LONG).show()
                                coroutineScope.launch {
                                    sessionManager.logout()
                                    navController?.navigate("auth_screen") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Gagal: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }) {
                    Text("Ya")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Button(
        onClick = { showDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Ganti Password", color = Color.White)
    }
}

@Composable
fun RegisteredFacesSection(
    navController: NavController?,
    faceLabels: List<String>,
    isEditing: Boolean,
    pendingAddedFaces: MutableMap<String, Any>,
    pendingDeletedFaces: MutableSet<String>,
    faceEmbeddings: Map<String, Any>,
    onAddFace: (String, Any) -> Unit,
    onDeleteFace: (String) -> Unit
) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var labelToDelete by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            "Wajah Terdaftar",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (faceLabels.isEmpty()) {
            Text(
                "Belum ada wajah yang didaftarkan.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            faceLabels.forEach { label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = null,
                        tint = Color(0xFF6C63FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp
                    )

                    if (isEditing) {
                        IconButton(onClick = {
                            labelToDelete = label
                            showDeleteDialog = true
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = Color.Red
                            )
                        }
                    }
                }

                Divider(color = Color.LightGray.copy(alpha = 0.4f))
            }
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { navController?.navigate("add_face_screen") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Wajah Baru")
            }
        }
    }

    // Dialog konfirmasi hapus
    if (showDeleteDialog && labelToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                labelToDelete = null
            },
            title = { Text("Hapus Wajah?") },
            text = { Text("Yakin ingin menghapus wajah \"$labelToDelete\"?") },
            confirmButton = {
                Button(onClick = {
                    val label = labelToDelete
                    if (uid != null && label != null) {
                        db.collection("users").document(uid)
                            .update("faceEmbeddings.$label", FieldValue.delete())
                            .addOnSuccessListener {
                                onDeleteFace(label)
                                Toast.makeText(context, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    showDeleteDialog = false
                    labelToDelete = null
                }) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showDeleteDialog = false
                    labelToDelete = null
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

fun deleteProfileImage(viewModel: MeViewModel) {
    val currentProfileImage = viewModel.profileImage.value
    if (!currentProfileImage.isNullOrEmpty()) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentProfileImage)
        storageRef.delete()
            .addOnSuccessListener {
                // Hapus field profileImage dari Firestore
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("profileImage", FieldValue.delete()) // Menghapus field profileImage
                    .addOnSuccessListener {
                        viewModel.updateProfileImage("") // Pastikan UI membaca sebagai kosong
                    }
            }
            .addOnFailureListener {
                // Handle error jika penghapusan gagal
            }
    }
}

@Composable
fun ProfileDatePicker(label: String, selectedDate: String, isEditing: Boolean, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val formattedDate = "$day/${month + 1}/$year"
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    ProfileSelectableField(label = label, text = selectedDate, isEditing = isEditing) {
        if (isEditing) datePickerDialog.show()
    }
}

@Composable
fun ProfileDropdownField(label: String, selectedValue: String, isEditing: Boolean, onValueSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Laki - Laki", "Perempuan", "Tidak ingin sebut")

    val backgroundColor = if (isEditing) Color.White else Color(0xFFD3D3D3)

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = label, fontSize = 15.sp, color = Color.Gray)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(backgroundColor)
                    .clickable(enabled = isEditing) { expanded = true }
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(selectedValue, color = Color.Black)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(344.dp) // Sesuaikan dengan lebar field Gender
                .background(Color.White)
                .clip(RoundedCornerShape(10.dp))
        ) {
            genderOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

fun resizeImage(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int, quality: Int = 80): ByteArray {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()

    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, maxWidth, maxHeight, true)

    val outputStream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

    return outputStream.toByteArray()
}

fun uploadImageToFirebase(context: Context, uri: Uri, viewModel: MeViewModel) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

    userRef.get().addOnSuccessListener { document ->
        val oldImageUrl = document.getString("profileImage")

        // Resize gambar sebelum upload
        val resizedImage = resizeImage(context, uri, 512, 512)

        // Buat referensi baru untuk gambar
        val newImageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/${UUID.randomUUID()}.jpg")
        val uploadTask = newImageRef.putBytes(resizedImage)

        uploadTask.addOnSuccessListener {
            newImageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                // Perbarui URL foto profil di Firestore
                userRef.update("profileImage", imageUrl.toString())
                    .addOnSuccessListener {
                        viewModel.updateProfileImage(imageUrl.toString())

                        // Hapus foto lama jika ada
                        if (!oldImageUrl.isNullOrEmpty()) {
                            FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl).delete()
                                .addOnSuccessListener {
                                    println("Foto lama berhasil dihapus")
                                }
                                .addOnFailureListener {
                                    println("Gagal menghapus foto lama: ${it.message}")
                                }
                        }
                    }
            }
        }.addOnFailureListener {
            println("Gagal mengunggah foto: ${it.message}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(label: String, text: String, isEditing: Boolean, onValueChange: (String) -> Unit) {
    val backgroundColor = if (isEditing) Color.White else Color(0xFFD3D3D3) // Abu-abu jika tidak sedang edit

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 15.sp, color = Color.Gray)
        TextField(
            value = text,
            onValueChange = { newText ->
                if (!newText.contains("\n")) {
                    onValueChange(newText)
                }
            },
            readOnly = !isEditing,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = backgroundColor,
                disabledTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            )
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun ProfileSelectableField(label: String, text: String, isEditing: Boolean, onValueChange: (String) -> Unit) {
    val backgroundColor = if (isEditing) Color.White else Color(0xFFD3D3D3)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 15.sp, color = Color.Gray)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor)
                .clickable(enabled = isEditing) { onValueChange("New Value") }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, color = Color.Black)
            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

