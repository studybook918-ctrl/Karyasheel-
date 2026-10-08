package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel

/**
 * Universal Auth Dialog supporting:
 * 1. 🔵 Google Sign-In via Credential Manager
 * 2. ✉️ Email & Password Login
 * 3. ➕ Create New Account (Name, Email, Password, Confirm Password)
 * 4. 🔑 Forgot Password (Password Reset Email)
 *
 * Shows clear loading indicators, friendly Hindi error messages, and disables multiple concurrent submissions.
 */
@Composable
fun AuthModalDialog(
    viewModel: MarketplaceViewModel,
    initialMode: AuthDialogMode = AuthDialogMode.LOGIN,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(initialMode) }

    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authLoading by viewModel.authLoading.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Automatically close dialog if login succeeded
    LaunchedEffect(currentUser) {
        if (currentUser != null && !authLoading) {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!authLoading) {
                viewModel.clearAuthError()
                onDismiss()
            }
        },
        title = {
            Text(
                text = when (mode) {
                    AuthDialogMode.LOGIN -> "लॉगिन करें (Login)"
                    AuthDialogMode.REGISTER -> "नया खाता बनाएँ (Sign Up)"
                    AuthDialogMode.FORGOT_PASSWORD -> "पासवर्ड रीसेट करें (Reset)"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Error message banner
                if (authErrorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEE2E2),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Text(
                            text = authErrorMessage ?: "",
                            color = RubyAlert,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // GOOGLE SIGN IN BUTTON (Only in Login / Register mode)
                if (mode != AuthDialogMode.FORGOT_PASSWORD) {
                    Button(
                        onClick = { viewModel.performGoogleLogin(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dialog_google_login_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder(),
                        enabled = !authLoading
                    ) {
                        Text(text = "🔵", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google खाते से जारी रखें",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = " या ईमेल द्वारा ",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }
                }

                // REGISTER: Name Field
                if (mode == AuthDialogMode.REGISTER) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("आपका नाम (Full Name)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SaffronPrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !authLoading
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Email Field
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("ईमेल पता (Email)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SaffronPrimary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !authLoading
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Password Field (Login & Register mode only)
                if (mode != AuthDialogMode.FORGOT_PASSWORD) {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("पासवर्ड (Password)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SaffronPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !authLoading
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // REGISTER: Confirm Password Field
                if (mode == AuthDialogMode.REGISTER) {
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("पासवर्ड दोबारा दर्ज करें") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SaffronPrimary) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_confirm_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !authLoading
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Secondary switch links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (mode == AuthDialogMode.LOGIN) {
                        TextButton(
                            onClick = {
                                viewModel.clearAuthError()
                                mode = AuthDialogMode.FORGOT_PASSWORD
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("पासवर्ड भूल गए?", fontSize = 11.sp, color = TextMuted)
                        }

                        TextButton(
                            onClick = {
                                viewModel.clearAuthError()
                                mode = AuthDialogMode.REGISTER
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+ नया खाता बनाएँ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        }
                    } else if (mode == AuthDialogMode.REGISTER) {
                        TextButton(
                            onClick = {
                                viewModel.clearAuthError()
                                mode = AuthDialogMode.LOGIN
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("पहले से खाता है? लॉगिन करें", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        }
                    } else if (mode == AuthDialogMode.FORGOT_PASSWORD) {
                        TextButton(
                            onClick = {
                                viewModel.clearAuthError()
                                mode = AuthDialogMode.LOGIN
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("« वापस लॉगिन पर जाएँ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (mode) {
                        AuthDialogMode.LOGIN -> {
                            viewModel.performEmailLogin(emailInput, passwordInput)
                        }
                        AuthDialogMode.REGISTER -> {
                            if (passwordInput != confirmPasswordInput) {
                                Toast.makeText(context, "पासवर्ड और कन्फर्म पासवर्ड मेल नहीं खा रहे हैं।", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.performEmailRegistration(nameInput, emailInput, passwordInput)
                        }
                        AuthDialogMode.FORGOT_PASSWORD -> {
                            viewModel.performPasswordReset(emailInput) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) {
                                    mode = AuthDialogMode.LOGIN
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("auth_submit_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                enabled = !authLoading
            ) {
                if (authLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("सत्यापित कर रहे हैं...")
                } else {
                    Text(
                        text = when (mode) {
                            AuthDialogMode.LOGIN -> "लॉगिन करें"
                            AuthDialogMode.REGISTER -> "खाता बनाएँ"
                            AuthDialogMode.FORGOT_PASSWORD -> "रीसेट लिंक भेजें"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.clearAuthError()
                    onDismiss()
                },
                enabled = !authLoading
            ) {
                Text("रद्द करें")
            }
        }
    )
}

enum class AuthDialogMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}
