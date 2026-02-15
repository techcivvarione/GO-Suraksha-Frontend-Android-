package com.gosuraksha.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.auth.model.AuthViewModel
import com.gosuraksha.app.auth.model.SignupRequest

@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF061A2B),
            Color(0xFF0B2C45)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ---------------- LOGO ----------------
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "GO Suraksha Logo",
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(140.dp),
                contentScale = ContentScale.Fit
            )


            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Already have a Suraksha Account? ",
                    color = Color(0xFFB0BEC5),
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = { onNavigateToLogin() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Sign in Securely",
                        color = Color(0xFF2CC5A8),
                        fontSize = 14.sp
                    )
                }
            }

            // ---------------- CARD ----------------
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF102A43)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
            ) {

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    Spacer(Modifier.height(24.dp))

                    ModernField(name, { name = it }, "Full Name")
                    Spacer(Modifier.height(16.dp))

                    ModernField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email Address",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(16.dp))

                    ModernField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "Phone Number",
                        keyboardType = KeyboardType.Phone
                    )

                    Spacer(Modifier.height(16.dp))

                    ModernField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        isPassword = true
                    )

                    Spacer(Modifier.height(16.dp))

                    ModernField(
                        value = confirm,
                        onValueChange = { confirm = it },
                        placeholder = "Confirm Password",
                        isPassword = true
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            error = null
                            loading = true

                            viewModel.signup(
                                request = SignupRequest(
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    password = password,
                                    confirm_password = confirm
                                ),
                                onSuccess = {
                                    loading = false
                                    onSignupSuccess()
                                },
                                onError = { message ->
                                    loading = false
                                    error = message
                                }
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2CC5A8)
                        ),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Create Account", fontSize = 16.sp)
                        }
                    }

                    error?.let {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Divider(color = Color(0xFF1E3A5F))

                    Spacer(Modifier.height(16.dp))

                    // ---------------- SECURITY POINTS ----------------
                    SecurityPoint("Encrypted authentication")
                    SecurityPoint("We never store your password")
                    SecurityPoint("Privacy-first architecture")
                }
            }
        }
    }
}

/* ---------------- MODERN FIELD ---------------- */

@Composable
fun ModernField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        visualTransformation = if (isPassword)
            PasswordVisualTransformation()
        else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2CC5A8),
            unfocusedBorderColor = Color(0xFF3A556A),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF2CC5A8)
        )
    )
}

/* ---------------- SECURITY BULLET ---------------- */

@Composable
fun SecurityPoint(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF2CC5A8),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = Color(0xFFB0BEC5),
            fontSize = 13.sp
        )
    }
}
