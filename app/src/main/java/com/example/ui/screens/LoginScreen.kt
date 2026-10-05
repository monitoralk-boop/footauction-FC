package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (managerName: String, clubName: String, avatar: String) -> Unit,
    onFirebaseSignUp: (suspend (email: String, password: String) -> Result<Any>)? = null,
    onFirebaseSignIn: (suspend (email: String, password: String) -> Result<Any>)? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var managerName by remember { mutableStateOf("") }
    var clubName by remember { mutableStateOf("") }
    val badges = listOf("\uD83D\uDC51", "\uD83E\uDD81", "\u26A1", "\uD83E\uDD85", "\uD83D\uDC3A", "\uD83C\uDFC6", "\u26BD", "\uD83D\uDD25", "\uD83D\uDC8E", "\u2694\uFE0F")
    var selectedBadge by remember { mutableStateOf("\uD83D\uDC51") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(true) } // true = Sign Up, false = Sign In
    var isAuthenticated by remember { mutableStateOf(false) } // After auth success, show profile setup
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A0F1D),
                        Color(0xFF0F1E19),
                        Color(0xFF050B08)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Crest
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = CardSurfaceDark,
                border = BorderStroke(2.dp, GoldPrimary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "\u26BD", fontSize = 38.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "FOOTAUCTION FC",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = GoldPrimary,
                letterSpacing = 1.5.sp
            )
            Text(
                text = if (!isAuthenticated) {
                    if (isSignUp) "Create Your Account" else "Sign In to Your Account"
                } else "Set Up Your Manager Profile",
                fontSize = 13.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!isAuthenticated) {
                // ========== AUTH STEP ==========
                // Sign Up / Sign In Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isSignUp = true; errorMessage = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSignUp) GoldPrimary else CardSurfaceElevated,
                            contentColor = if (isSignUp) PitchBlack else TextSecondaryDark
                        )
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { isSignUp = false; errorMessage = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isSignUp) GoldPrimary else CardSurfaceElevated,
                            contentColor = if (!isSignUp) PitchBlack else TextSecondaryDark
                        )
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email & Password Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, CardBorderGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("EMAIL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("your@email.com", color = TextSecondaryDark, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0xFF2A3A40),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Min 6 characters", color = TextSecondaryDark, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0xFF2A3A40),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        if (isSignUp) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("CONFIRM PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = { Text("Re-enter password", color = TextSecondaryDark, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = Color(0xFF2A3A40),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = errorMessage!!, color = Color(0xFFFF5252), fontSize = 12.sp, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Auth Button
                Button(
                    onClick = {
                        errorMessage = null
                        if (email.isBlank() || !email.contains("@")) {
                            errorMessage = "Please enter a valid email address"
                            return@Button
                        }
                        if (password.length < 6) {
                            errorMessage = "Password must be at least 6 characters"
                            return@Button
                        }
                        if (isSignUp && password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@Button
                        }

                        isLoading = true
                        scope.launch {
                            val result = if (isSignUp) {
                                onFirebaseSignUp?.invoke(email, password)
                            } else {
                                onFirebaseSignIn?.invoke(email, password)
                            }

                            isLoading = false
                            if (result != null) {
                                if (result.isSuccess) {
                                    if (isSignUp) {
                                        // New user: show profile setup
                                        isAuthenticated = true
                                    } else {
                                        // Existing user: skip profile if already exists, or show setup
                                        isAuthenticated = true
                                    }
                                } else {
                                    val ex = result.exceptionOrNull()
                                    errorMessage = when {
                                        ex?.message?.contains("email address is already", ignoreCase = true) == true -> "This email is already registered. Try signing in instead."
                                        ex?.message?.contains("password is invalid", ignoreCase = true) == true -> "Incorrect password. Please try again."
                                        ex?.message?.contains("no user record", ignoreCase = true) == true -> "No account found with this email. Sign up first!"
                                        ex?.message?.contains("network", ignoreCase = true) == true -> "Network error. Check your internet connection."
                                        ex?.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true -> "Firebase not configured. Using offline mode."
                                        else -> ex?.message ?: "Authentication failed. Please try again."
                                    }
                                }
                            } else {
                                // No Firebase handler provided - use offline mode
                                isAuthenticated = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PitchBlack, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        if (isSignUp) Icons.Default.PersonAdd else Icons.Default.Login,
                        contentDescription = null,
                        tint = PitchBlack
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSignUp) "CREATE ACCOUNT" else "SIGN IN",
                        color = PitchBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Offline Guest Play
                OutlinedButton(
                    onClick = {
                        val rand = kotlin.random.Random.nextInt(100, 999)
                        onLoginSuccess("Manager #$rand", "FC United #$rand", badges.random())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TextSecondaryDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(text = "\uD83D\uDD12 Play Offline (Guest Mode)", fontSize = 12.sp)
                }
            } else {
                // ========== PROFILE SETUP STEP ==========
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13231D)),
                    border = BorderStroke(1.dp, Color(0xFF1E4535))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "\uD83C\uDF81", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "\u2705 Account Created!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPitch
                            )
                            Text(
                                text = "Now set up your manager profile and draft a unique Starting 11 (60-70 OVR) from worldwide leagues!",
                                fontSize = 10.sp,
                                color = Color(0xFFD0E0D8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, CardBorderGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("MANAGER NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = managerName,
                            onValueChange = { managerName = it },
                            placeholder = { Text("e.g. Pep, Alex, Mohamed...", color = TextSecondaryDark, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0xFF2A3A40),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("CLUB NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = clubName,
                            onValueChange = { clubName = it },
                            placeholder = { Text("e.g. Royal Madrid, Red Devils FC...", color = TextSecondaryDark, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0xFF2A3A40),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("SELECT CLUB CREST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            badges.take(5).forEach { badge ->
                                val isSelected = selectedBadge == badge
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (isSelected) GoldPrimary.copy(alpha = 0.2f) else CardSurfaceElevated,
                                            CircleShape
                                        )
                                        .border(
                                            BorderStroke(
                                                if (isSelected) 2.dp else 1.dp,
                                                if (isSelected) GoldPrimary else Color.Transparent
                                            ),
                                            CircleShape
                                        )
                                        .clickable { selectedBadge = badge },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = badge, fontSize = 20.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            badges.drop(5).take(5).forEach { badge ->
                                val isSelected = selectedBadge == badge
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (isSelected) GoldPrimary.copy(alpha = 0.2f) else CardSurfaceElevated,
                                            CircleShape
                                        )
                                        .border(
                                            BorderStroke(
                                                if (isSelected) 2.dp else 1.dp,
                                                if (isSelected) GoldPrimary else Color.Transparent
                                            ),
                                            CircleShape
                                        )
                                        .clickable { selectedBadge = badge },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = badge, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = errorMessage!!, color = Color(0xFFFF5252), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (managerName.trim().isBlank()) {
                            errorMessage = "Please enter a Manager Name"
                            return@Button
                        }
                        if (clubName.trim().isBlank()) {
                            errorMessage = "Please enter a Club Name"
                            return@Button
                        }
                        onLoginSuccess(managerName.trim(), clubName.trim(), selectedBadge)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = PitchBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CREATE CLUB & DRAFT SQUAD",
                        color = PitchBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
