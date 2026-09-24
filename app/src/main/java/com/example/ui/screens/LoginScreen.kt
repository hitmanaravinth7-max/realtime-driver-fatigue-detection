package com.example.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AssistiveSafetyNotice
import com.example.ui.theme.CardSlate
import com.example.ui.theme.DeepCockpit
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TechCyan

@Composable
fun LoginScreen(
    onLoginSuccess: (email: String, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("driver@college.edu") }
    var password by remember { mutableStateOf("driver123") }
    var name by remember { mutableStateOf("Scholar Driver") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotDialogMessage by remember { mutableStateOf<String?>(null) }

    fun validateInputs(): Boolean {
        var isValid = true

        if (email.trim().isEmpty()) {
            emailError = "Please enter your email address."
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            emailError = "Please enter a valid email (e.g., driver@college.edu)."
            isValid = false
        } else {
            emailError = null
        }

        if (password.isEmpty()) {
            passwordError = "Password cannot be empty."
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters."
            isValid = false
        } else {
            passwordError = null
        }

        if (isRegisterMode) {
            if (name.trim().isEmpty()) {
                nameError = "Driver name cannot be empty."
                isValid = false
            } else {
                nameError = null
            }

            if (confirmPassword != password) {
                confirmPasswordError = "Passwords do not match."
                isValid = false
            } else {
                confirmPasswordError = null
            }
        }

        return isValid
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpit)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hero Image Card
            Card(
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.driver_hud_hero_1790267666433),
                        contentDescription = "Driver Monitoring HUD",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, DeepCockpit.copy(alpha = 0.9f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = TechCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan)
                        ) {
                            Text(
                                text = "COLLEGE CAPSTONE PROJECT",
                                color = TechCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Real-Time Driver Fatigue Detection",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRegisterMode) "Create Driver Account" else "Driver Authentication",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isRegisterMode) "Register your profile for fatigue monitoring telemetry" else "Sign in to access real-time computer vision dashboard",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Quick Fill Demo Credentials button
                    OutlinedButton(
                        onClick = {
                            email = "driver@college.edu"
                            password = "driver123"
                            name = "Scholar Driver"
                            emailError = null
                            passwordError = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fill_demo_credentials_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TechCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fill Demo Credentials", color = TechCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (nameError != null) nameError = null
                            },
                            label = { Text("Full Name / Driver ID") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            isError = nameError != null,
                            supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechCyan,
                                unfocusedBorderColor = SurfaceBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) emailError = null
                        },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        isError = emailError != null,
                        supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TechCyan,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null) passwordError = null
                        },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = { passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TechCyan,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )

                    if (isRegisterMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                if (confirmPasswordError != null) confirmPasswordError = null
                            },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = confirmPasswordError != null,
                            supportingText = { confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_password_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechCyan,
                                unfocusedBorderColor = SurfaceBorder
                            )
                        )
                    }

                    if (!isRegisterMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    forgotEmail = email
                                    forgotDialogMessage = null
                                    showForgotPasswordDialog = true
                                },
                                modifier = Modifier.testTag("forgot_password_button")
                            ) {
                                Text("Forgot Password?", fontSize = 12.sp, color = TechCyan)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Main Action Button
                    Button(
                        onClick = {
                            if (validateInputs()) {
                                onLoginSuccess(email.trim(), if (isRegisterMode) name.trim() else "Scholar Driver")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button")
                    ) {
                        Text(
                            text = if (isRegisterMode) "Register & Open Dashboard" else "Login to Dashboard",
                            color = DeepCockpit,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle mode button
                    TextButton(
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            emailError = null
                            passwordError = null
                            confirmPasswordError = null
                            nameError = null
                        },
                        modifier = Modifier.testTag("toggle_create_account_button")
                    ) {
                        Text(
                            text = if (isRegisterMode) "Already have an account? Sign In" else "New Driver? Create Account",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            AssistiveSafetyNotice()
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = { Text("Password Recovery") },
                text = {
                    Column {
                        Text(
                            text = "Enter your registered email address to receive password reset instructions.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = { Text("Registered Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (forgotDialogMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = forgotDialogMessage!!,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (forgotEmail.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(forgotEmail.trim()).matches()) {
                                forgotDialogMessage = "Password reset link simulated: Sent to $forgotEmail"
                            } else {
                                forgotDialogMessage = "Please enter a valid email."
                            }
                        }
                    ) {
                        Text("Send Reset Link")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
