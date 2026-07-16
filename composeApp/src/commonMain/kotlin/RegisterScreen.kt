import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    val performRegistration = {
        when {
            username.isEmpty() -> errorMessage = "Enter username"
            email.isEmpty() -> errorMessage = "Enter email"
            password.isEmpty() -> errorMessage = "Enter password"
            password != confirmPassword -> errorMessage = "Passwords do not match"
            password.length < 6 -> errorMessage = "Password must be at least 6 characters"
            else -> {
                isLoading = true
                errorMessage = ""
                scope.launch {
                    val result = repository.register(email, password, username)
                    if (result.isSuccess) {
                        errorMessage = "Check your email to verify your account!"
                        delay(2000)
                        onRegisterSuccess()
                    } else errorMessage = if (result.exceptionOrNull()?.message == "Username already taken")
                        "Username already taken!" else "Registration failed"
                    isLoading = false
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground).padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Box(
                modifier = Modifier.size(126.dp).clip(RoundedCornerShape(16.dp)).background(PurpleMain),
                contentAlignment = Alignment.Center
            ) { Text("M", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Elite Community Match Tracking", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Create Account", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Join the community and track your game history.", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Username", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = username, onValueChange = { username = it }, placeholder = { Text("Don_Corleone", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Email Address", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, placeholder = { Text("name@example.com", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Password", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Text(if (passwordVisible) "\uD83D\uDE48" else "\uD83D\uDC41\uFE0F") } }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Confirm Password", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Text(if (confirmPasswordVisible) "\uD83D\uDE48" else "\uD83D\uDC41\uFE0F") } }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(24.dp))

                    if (errorMessage.isNotEmpty()) {
                        Text(text = errorMessage, color = if (errorMessage.contains("Check your email")) Color.Green else Color.Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    Button(onClick = { performRegistration() }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000)), shape = RoundedCornerShape(12.dp)) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        else Text("Create Account ->", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Already have an account? ", color = Color.Gray)
                        Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBackToLogin() })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("© 2026 WEXFORD MAFIA RATING TRACKER • PRIVATE COMMUNITY", color = Color.DarkGray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
