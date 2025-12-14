package pe.edu.idat.dsi.dami.idatgram.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.idat.dsi.dami.idatgram.R
import pe.edu.idat.dsi.dami.idatgram.ui.components.*
import pe.edu.idat.dsi.dami.idatgram.ui.theme.*

/**
 * Pantalla de Login de Instagram
 * 
 * Implementa la interfaz de autenticación con:
 * - Gradiente de marca Instagram
 * - Logo y branding
 * - Campos de email y contraseña
 * - Botones de login y registro
 * - Opciones de recuperación de contraseña
 */
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    modifier: Modifier = Modifier,

    // ✅ NUEVO: estado controlado desde el ViewModel
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onErrorShown: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
//    var isLoading by remember { mutableStateOf(false) }

    // Snackbar host para mostrar errores
    val snackbarHostState = remember { SnackbarHostState() }

    // Cuando llega un error, se muestra
    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message = errorMessage)
            onErrorShown()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        InstagramBlue,
                        InstagramPurple,
                        InstagramPink
                    )
                )
            )
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // === Logo de Instagram ===
            Text(
                text = "Idatgram",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            // === Formulario de Login ===
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    Text(
                        text = stringResource(R.string.login_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    
                    // Campo Email
                    IdatgramTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(R.string.login_email_label),
                        placeholder = stringResource(R.string.login_email_placeholder),
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    
                    // Campo Contraseña
                    IdatgramPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.login_password_label),
                        placeholder = stringResource(R.string.login_password_placeholder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )
                    
                    // Botón de Login
                    IdatgramButton(
                        text = stringResource(R.string.login_button),
                        onClick = {
//                          isLoading = true
                            onLoginClick(email, password)
                        },
                        enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                        isLoading = isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    
                    // Link de contraseña olvidada
                    TextButton(
                        onClick = onForgotPasswordClick,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login_forgot_password),
                            color = Primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // Divisor
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(R.string.login_or),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    }
                    
                    // Botón de Registro
                    IdatgramOutlinedButton(
                        text = stringResource(R.string.login_register_button),
                        onClick = onRegisterClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // === Información adicional ===
            Text(
                text = stringResource(R.string.login_footer),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .alpha(0.8f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    IdatgramTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true, name = "Dark Theme")
@Composable
fun LoginScreenDarkPreview() {
    IdatgramTheme(darkTheme = true) {
        LoginScreen()
    }
}