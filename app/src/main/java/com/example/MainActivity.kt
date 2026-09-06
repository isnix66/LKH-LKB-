package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LkhViewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: LkhViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    if (uiState.isLoggedIn) {
                        MainScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                    } else {
                        LoginScreen(
                            savedUsername = uiState.savedUsername,
                            savedPassword = uiState.savedPassword,
                            initialRememberMe = uiState.rememberMe,
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.errorMessage,
                            scriptUrl = uiState.scriptUrl,
                            onLogin = { username, password, remember ->
                                viewModel.login(username, password, remember)
                            },
                            onSaveScriptUrl = { newUrl ->
                                viewModel.setScriptUrl(newUrl)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
