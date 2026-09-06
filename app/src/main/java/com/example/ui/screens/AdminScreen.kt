package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.ui.components.AdminUserDialog
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

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
            .background(NokiaBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "⚙️ Manajemen Pengguna",
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh user", tint = NokiaCyanGlow, modifier = Modifier.size(20.dp))
                        }
                        Button(
                            onClick = { showAddModal = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NokiaCyan, contentColor = Color.White),
                            modifier = Modifier.testTag("btn_admin_add_user"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah User", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            )
        }

        if (users.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                    border = BorderStroke(1.5.dp, NokiaBorder),
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
                            style = MaterialTheme.typography.bodyMedium.copy(color = NokiaTextSecondary, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        } else {
            items(users) { user ->
                val isAdmin = user.role.equals("admin", ignoreCase = true)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                    border = BorderStroke(1.5.dp, if (isAdmin) NokiaOrange.copy(alpha = 0.5f) else NokiaCyan.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isAdmin) NokiaOrangeLight else NokiaNavy,
                                border = BorderStroke(1.dp, if (isAdmin) NokiaOrange else NokiaCyanGlow),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isAdmin) NokiaOrangeDark else NokiaCyanGlow,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = user.displayName.ifBlank { user.username },
                                        modifier = Modifier.weight(1f, fill = false),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NokiaTextPrimary,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = if (isAdmin) NokiaOrangeLight else NokiaCyanLight,
                                        border = BorderStroke(1.dp, if (isAdmin) NokiaOrange.copy(alpha = 0.4f) else NokiaCyan.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = user.role.uppercase(),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isAdmin) NokiaOrangeDark else NokiaCyanDark,
                                                fontSize = 10.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Text(
                                    text = "Username: ${user.username}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NokiaTextSecondary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                if (user.expireDate.isNotBlank()) {
                                    Text(
                                        text = "Aktif s.d: ${user.expireDate}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NokiaRedDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
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
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp), tint = NokiaCyanDark)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Buka Spreadsheet",
                                            style = MaterialTheme.typography.labelSmall.copy(color = NokiaCyanDark, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { userToEdit = user },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit User", tint = NokiaCyanDark, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { userToDelete = user.username },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus User", tint = NokiaRed, modifier = Modifier.size(18.dp))
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
            shape = RoundedCornerShape(20.dp),
            containerColor = NokiaCardSurface,
            title = { Text("Hapus User", fontWeight = FontWeight.Bold, color = NokiaTextPrimary) },
            text = { Text("Apakah Anda yakin ingin menghapus user \"$target\"?", color = NokiaTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(target)
                        userToDelete = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NokiaRed, contentColor = Color.White)
                ) {
                    Text("Ya, Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { userToDelete = null },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NokiaBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaTextSecondary)
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

