package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserAccount
import com.example.ui.theme.DeepNavy

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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isEdit) "✏️ Edit User" else "➕ Tambah User Baru",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_username_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
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
                    shape = RoundedCornerShape(10.dp)
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
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = expireDate,
                    onValueChange = { expireDate = it },
                    label = { Text("Masa Berlaku (YYYY-MM-DD)") },
                    placeholder = { Text("Kosongkan untuk tanpa batas") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_expire_date_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Role",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("User Biasa") },
                            onClick = {
                                role = "user"
                                roleExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Admin") },
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
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
                            .testTag("admin_btn_save_user"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isEdit) "Simpan" else "Tambah", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
