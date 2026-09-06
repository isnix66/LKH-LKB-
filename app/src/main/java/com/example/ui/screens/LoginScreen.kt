package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.NetworkUtils

@Composable
fun LoginScreen(
    savedUsername: String,
    savedPassword: String,
    initialRememberMe: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    scriptUrl: String,
    onLogin: (username: String, password: String, remember: Boolean) -> Unit,
    onSaveScriptUrl: (String) -> Unit
) {
    val context = LocalContext.current
    var username by remember(savedUsername) { mutableStateOf(savedUsername) }
    var password by remember(savedPassword) { mutableStateOf(savedPassword) }
    var rememberMe by remember(initialRememberMe) { mutableStateOf(initialRememberMe) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Observe network connectivity in real-time
    val isNetworkConnected by produceState(initialValue = NetworkUtils.isConnected(context)) {
        NetworkUtils.observeNetworkConnectivity(context).collect { isConnected ->
            value = isConnected
        }
    }

    var showOfflineWarning by remember { mutableStateOf(false) }

    fun handleLoginAttempt() {
        val connected = NetworkUtils.isConnected(context)
        if (!connected) {
            showOfflineWarning = true
            return
        }
        showOfflineWarning = false
        if (username.isNotBlank() && password.isNotBlank()) {
            onLogin(username, password, rememberMe)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0B1320)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
            border = BorderStroke(2.dp, NokiaCyan.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Nokia Belle signature metallic cyan & deep navy accent gradient header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    NokiaCyan,
                                    NokiaCyanGlow,
                                    NokiaNavy
                                )
                            )
                        )
                )

                // Network Offline Notice Banner
                AnimatedVisibility(
                    visible = !isNetworkConnected || showOfflineWarning,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        color = NokiaRedLight,
                        border = BorderStroke(1.5.dp, NokiaRed.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("offline_network_notice"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NokiaRed,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Silahkan Hubungkan Ke Internet",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NokiaRedDark,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = "Koneksi internet diperlukan untuk proses masuk dan sinkronisasi data kegiatan.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NokiaTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo / Symbol Squircle Badge (Nokia Belle Squircle)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = NokiaNavy,
                        border = BorderStroke(2.dp, NokiaCyanGlow),
                        modifier = Modifier.size(68.dp),
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = NokiaCyanGlow,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Aplikasi LKH & LKB",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NokiaTextPrimary,
                            letterSpacing = (-0.5).sp,
                            fontSize = 23.sp
                        )
                    )
                    Text(
                        text = "Laporan Kinerja Harian & Bulanan Pegawai",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NokiaTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = NokiaCyanLight,
                        border = BorderStroke(1.dp, NokiaCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "🔐 Masuk ke Akun Anda",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NokiaCyanDark,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            showOfflineWarning = false
                        },
                        label = { Text("Username", fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("Masukkan username", color = NokiaTextTertiary) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = NokiaCyan)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedLabelColor = NokiaCyanDark,
                            unfocusedLabelColor = NokiaTextSecondary,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = NokiaCardSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            showOfflineWarning = false
                        },
                        label = { Text("Password", fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("Masukkan password", color = NokiaTextTertiary) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NokiaCyan)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password",
                                    tint = NokiaTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { handleLoginAttempt() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedLabelColor = NokiaCyanDark,
                            unfocusedLabelColor = NokiaTextSecondary,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = NokiaCardSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            modifier = Modifier.testTag("login_remember_checkbox"),
                            colors = CheckboxDefaults.colors(
                                checkedColor = NokiaCyan,
                                uncheckedColor = NokiaTextTertiary
                            )
                        )
                        Text(
                            text = "Simpan Username & Password",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = NokiaTextPrimary,
                                fontSize = 13.5.sp
                            )
                        )
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NokiaRedLight,
                            border = BorderStroke(1.dp, NokiaRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage,
                                color = NokiaRedDark,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { handleLoginAttempt() },
                        enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NokiaCyan,
                            contentColor = Color.White,
                            disabledContainerColor = NokiaCyan.copy(alpha = 0.4f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Menghubungkan...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        } else {
                            Text(
                                text = "Masuk",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.3.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

