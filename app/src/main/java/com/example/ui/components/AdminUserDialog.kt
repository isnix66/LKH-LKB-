package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserAccount
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDialog(
    userToEdit: UserAccount? = null,
    onDismiss: () -> Unit,
    onSubmit: (originalUsername: String?, user: UserAccount) -> Unit
) {
    val isEdit = userToEdit != null
    var username by remember { mutableStateOf(userToEdit?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf(userToEdit?.displayName ?: "") }
    var role by remember { mutableStateOf(userToEdit?.role ?: "user") }
    var expireDate by remember { mutableStateOf(userToEdit?.expireDate ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
            border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isEdit) "✏️ Edit Pengguna" else "➕ Tambah Pengguna Baru",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NokiaTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_username_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NokiaCyan,
                        unfocusedBorderColor = NokiaBorder,
                        focusedTextColor = NokiaTextPrimary,
                        unfocusedTextColor = NokiaTextPrimary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = NokiaCardSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isEdit) "Password baru (kosongkan jika tidak diubah)" else "Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NokiaCyan,
                        unfocusedBorderColor = NokiaBorder,
                        focusedTextColor = NokiaTextPrimary,
                        unfocusedTextColor = NokiaTextPrimary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = NokiaCardSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nama Lengkap / Tampilan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_display_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NokiaCyan,
                        unfocusedBorderColor = NokiaBorder,
                        focusedTextColor = NokiaTextPrimary,
                        unfocusedTextColor = NokiaTextPrimary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = NokiaCardSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = expireDate,
                    onValueChange = { expireDate = it },
                    label = { Text("Masa Berlaku (YYYY-MM-DD)") },
                    placeholder = { Text("Kosongkan untuk tanpa batas", color = NokiaTextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_expire_date_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NokiaCyan,
                        unfocusedBorderColor = NokiaBorder,
                        focusedTextColor = NokiaTextPrimary,
                        unfocusedTextColor = NokiaTextPrimary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = NokiaCardSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Peran (Role)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NokiaTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                var roleExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (role == "admin") "Admin" else "User Biasa",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = NokiaCardSurfaceVariant
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("User Biasa", color = NokiaTextPrimary, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                role = "user"
                                roleExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Admin", color = NokiaOrangeDark, fontWeight = FontWeight.Bold) },
                            onClick = {
                                role = "admin"
                                roleExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, NokiaBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaTextSecondary)
                    ) {
                        Text("Batal", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (username.isNotBlank() && displayName.isNotBlank()) {
                                onSubmit(
                                    userToEdit?.username,
                                    UserAccount(
                                        username = username.trim(),
                                        password = password.trim(),
                                        displayName = displayName.trim(),
                                        role = role,
                                        expireDate = expireDate.trim()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("admin_btn_save_user"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NokiaCyan, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(if (isEdit) "Simpan" else "Tambah", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

