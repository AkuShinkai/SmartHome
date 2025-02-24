package com.example.smarthome.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarthome.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

@Composable
fun MeScreen(navController: NavController?) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    var user by remember { mutableStateOf<FirebaseUser?>(null) }
    val context = LocalContext.current

    var name by remember { mutableStateOf("Loading...") }
    var birthDate by remember { mutableStateOf("Select Birth") }
    var gender by remember { mutableStateOf("Select Gender") }

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(name) }
    var editedBirthDate by remember { mutableStateOf(birthDate) }
    var editedGender by remember { mutableStateOf(gender) }

    LaunchedEffect(Unit) {
        user = auth.currentUser
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("name") ?: "Unknown"
                        birthDate = document.getString("birthDate") ?: "Select Birth"
                        gender = document.getString("gender") ?: "Select Gender"

                        editedName = name
                        editedBirthDate = birthDate
                        editedGender = gender
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0E48A1), Color(0xff7e02d6), Color(0xffd705fc))
                        ),
                        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileTextField(
                    label = "First Name",
                    text = editedName,
                    isEditing = isEditing,
                    onValueChange = { editedName = it }
                )
                ProfileTextField(
                    label = "Email",
                    text = user?.email ?: "johndoe@gmail.com",
                    isEditing = false,
                    onValueChange = {}
                )

                ProfileDatePicker(
                    label = "Birth",
                    selectedDate = editedBirthDate,
                    isEditing = isEditing,
                    onDateSelected = { editedBirthDate = it }
                )

                ProfileDropdownField(
                    label = "Gender",
                    selectedValue = editedGender,
                    isEditing = isEditing,
                    onValueSelected = { editedGender = it }
                )

                Spacer(modifier = Modifier.height(5.dp))

                AnimatedVisibility(visible = !isEditing) {
                    Button(
                        onClick = { isEditing = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile", color = Color.White)
                    }
                }

                AnimatedVisibility(visible = isEditing) {
                    Column {
                        // Tombol Simpan Perubahan
                        Button(
                            onClick = {
                                isEditing = false
                                user?.uid?.let { uid ->
                                    firestore.collection("users").document(uid)
                                        .update("name", editedName, "birthDate", editedBirthDate, "gender", editedGender)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Changes", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Tombol Batal Edit
                        OutlinedButton(
                            onClick = {
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
                            Text("Cancel Changes", color = Color.Gray)

                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { /* Change password */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password", color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        auth.signOut()
                        navController?.navigate("auth_screen")
                    },
                    border = BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", color = Color.Red)
                }
            }
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
    val genderOptions = listOf("Male", "Female", "Rather Not Say")

    val backgroundColor = if (isEditing) Color.White else Color(0xFFD3D3D3)

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(backgroundColor)
                    .clickable(enabled = isEditing) { expanded = true }
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(selectedValue, color = Color.DarkGray)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(label: String, text: String, isEditing: Boolean, onValueChange: (String) -> Unit) {
    val backgroundColor = if (isEditing) Color.White else Color(0xFFD3D3D3) // Abu-abu jika tidak sedang edit

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        TextField(
            value = text,
            onValueChange = onValueChange,
            readOnly = !isEditing,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor), // Warna berubah sesuai mode edit
            colors = TextFieldDefaults.textFieldColors(
                containerColor = backgroundColor,
                disabledTextColor = Color.Black, // Warna teks tetap jelas
                unfocusedTextColor = Color.Black
            )
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun ProfileSelectableField(label: String, text: String, isEditing: Boolean, onValueChange: (String) -> Unit) {
    val backgroundColor = if (isEditing) Color.White else Color(0xFFD3D3D3)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
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
            Text(text, color = Color.DarkGray)
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Preview(showBackground = true)
@Composable
fun MeScreenPreview() {
    MeScreen(navController = null)
}
