package pe.edu.idat.dsi.dami.idatgram.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pe.edu.idat.dsi.dami.idatgram.ui.components.*
import pe.edu.idat.dsi.dami.idatgram.ui.theme.*
import pe.edu.idat.dsi.dami.idatgram.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onUserProfileClick: (String) -> Unit = {},
    onStoryClick: (String) -> Unit = {},
    onCameraClick: () -> Unit = {},
    onDirectMessagesClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Barra Superior
        TopAppBar(
            title = {
                Text(
                    text = "Idatgram",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = onCameraClick) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Cámara",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                IconButton(onClick = onDirectMessagesClick) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Mensajes directos",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Contenido
        when {
            uiState.isLoading && uiState.posts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.posts.isEmpty() && !uiState.isLoading -> {
                // Estado vacío
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "¡Aún no hay publicaciones!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sé el primero en compartir algo",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                // Lista de posts
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    
                    // Sección de Stories (simulada)
                    item {
                        LazyRow(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            // Tu historia
                            item {
                                StoryCircle(
                                    imageUrl = "https://picsum.photos/100/100?random=100",
                                    username = "Tu historia",
                                    hasNewStory = false,
                                    isOwnStory = true,
                                    onClick = { onStoryClick("own") }
                                )
                            }
                            
                            // Stories de otros usuarios (simuladas)
                            items(5) { index ->
                                StoryCircle(
                                    imageUrl = "https://picsum.photos/100/100?random=${200 + index}",
                                    username = "user_${index + 1}",
                                    hasNewStory = true,
                                    onClick = { onStoryClick("user_${index + 1}") }
                                )
                            }
                        }
                    }
                    
                    // Posts
                    items(uiState.posts) { postWithUser ->
                        PostCard(
                            postWithUser = postWithUser,
                            onLikeClick = { 
                                viewModel.toggleLike(postWithUser.post.id)
                            },
                            onCommentClick = { /* TODO: Implementar comentarios */ },
                            onShareClick = { /* TODO: Implementar compartir */ },
                            onSaveClick = { /* TODO: Implementar guardar */ },
                            onUserClick = { onUserProfileClick(postWithUser.user.id) }
                        )
                    }
                    
                    // Espaciado al final
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Mostrar error si existe
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                viewModel.clearError()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    IdatgramTheme {
        HomeScreen()
    }
}