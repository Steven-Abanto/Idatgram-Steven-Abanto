package pe.edu.idat.dsi.dami.idatgram.ui.screens.addpost

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pe.edu.idat.dsi.dami.idatgram.R
import pe.edu.idat.dsi.dami.idatgram.ui.components.IdatgramButton
import pe.edu.idat.dsi.dami.idatgram.ui.components.IdatgramTextField
import pe.edu.idat.dsi.dami.idatgram.ui.theme.IdatgramTheme
import pe.edu.idat.dsi.dami.idatgram.ui.viewmodel.AddPostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    viewModel: AddPostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Navegar de vuelta cuando el post se crea exitosamente
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPostCreated()
        }
    }

    // Mostrar error si existe
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // En una app real, mostrarías un Snackbar aquí
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            
            Text(
                text = "Nueva publicación",
                style = MaterialTheme.typography.titleLarge
            )
            
            TextButton(
                onClick = { viewModel.createPost() },
                enabled = !uiState.isLoading && uiState.imageUrl.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Compartir")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de imagen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (uiState.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uiState.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Seleccionar imagen",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para seleccionar imagen (simulado)
        IdatgramButton(
            text = if (uiState.imageUrl.isBlank()) "Seleccionar imagen" else "Cambiar imagen",
            onClick = { viewModel.selectSampleImage() },
            icon = Icons.Default.Image,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo de descripción
        IdatgramTextField(
            value = uiState.caption,
            onValueChange = { viewModel.updateCaption(it) },
            label = "Escribe una descripción...",
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de ubicación
        IdatgramTextField(
            value = uiState.location,
            onValueChange = { viewModel.updateLocation(it) },
            label = "Agregar ubicación (opcional)",
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Default.LocationOn
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de crear post
        IdatgramButton(
            text = if (uiState.isLoading) "Publicando..." else "Compartir publicación",
            onClick = { viewModel.createPost() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.imageUrl.isNotBlank() && uiState.caption.isNotBlank(),
            isLoading = uiState.isLoading
        )

        // Mostrar error si existe
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddPostScreenPreview() {
    IdatgramTheme {
        AddPostScreen(
            onNavigateBack = {},
            onPostCreated = {}
        )
    }
}