package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserAccount
import com.example.ui.components.AdminUserDialog
import com.example.ui.components.SectionHeader
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueLight

@Composable
fun AdminScreen(
    users: List<UserAccount>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onAddUser: (UserAccount) -> Unit,
    onUpdateUser: (originalUsername: String, user: UserAccount) -> Unit,
    onDeleteUser: (username: String) -> Unit
) {
    val context = LocalContext.current
    var showAddModal by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserAccount?>(null) }
    var userToDelete by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "⚙️ Manajemen User (Admin)",
                trailingContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh user")
                        }
                        Button(
                            onClick = { showAddModal = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_admin_add_user"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah User", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            )
        }

        if (users.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada user terdaftar. Klik 'Tambah User' untuk menambahkan.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        } else {
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.displayName.ifBlank { user.username },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (user.role == "admin") MaterialTheme.colorScheme.tertiaryContainer else RoyalBlueLight
                                ) {
                                    Text(
                                        text = user.role.uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (user.role == "admin") MaterialTheme.colorScheme.onTertiaryContainer else RoyalBlue
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "Username: ${user.username}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (user.expireDate.isNotBlank()) {
                                Text(
                                    text = "Aktif s.d: ${user.expireDate}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            if (user.spreadsheetUrl.isNotBlank() && user.spreadsheetUrl != "#") {
                                TextButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(user.spreadsheetUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Buka Spreadsheet", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { userToEdit = user }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit User", tint = RoyalBlue)
                            }
                            IconButton(onClick = { userToDelete = user.username }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus User", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddModal) {
        AdminUserDialog(
            userToEdit = null,
            onDismiss = { showAddModal = false },
            onSubmit = { _, newUser -> onAddUser(newUser) }
        )
    }

    if (userToEdit != null) {
        AdminUserDialog(
            userToEdit = userToEdit,
            onDismiss = { userToEdit = null },
            onSubmit = { orig, updatedUser ->
                if (orig != null) {
                    onUpdateUser(orig, updatedUser)
                }
            }
        )
    }

    if (userToDelete != null) {
        val target = userToDelete ?: ""
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Hapus User", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus user \"$target\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(target)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
