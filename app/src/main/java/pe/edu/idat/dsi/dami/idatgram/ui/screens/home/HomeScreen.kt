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
    onCommentsClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val stories = uiState.stories

    Scaffold(
        modifier = modifier.fillMaxSize(),
        //Barra superior
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Idatgram",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCameraClick) {
                        Icon(Icons.Default.Camera, contentDescription = "Cámara")
                    }
                },
                actions = {
                    IconButton(onClick = onDirectMessagesClick) {
                        Icon(Icons.Default.Send, contentDescription = "Mensajes directos")
                    }
                }
            )
        }
    ) { padding ->
        //Contenido
        when {
            uiState.isLoading && uiState.posts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.posts.isEmpty() && !uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("¡Aún no hay publicaciones!")
                        Text("Sé el primero en compartir algo")
                    }
                }
            }
            
            else -> {
                // Lista de posts
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding()
                ),
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

                            items(stories, key = { it.story.id }) { item ->
                                StoryCircle(
                                    imageUrl = item.user.profileImageUrl,
                                    username = item.user.username,
                                    hasNewStory = !item.isViewed,
                                    onClick = { onStoryClick(item.user.id) }
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
//                            onCommentClick = { /* TODO: Implementar comentarios */ },
                            onCommentClick = {
                                onCommentsClick(postWithUser.post.id)
                            },
                            onShareClick = { /* TODO: Implementar compartir */ },
                            onSaveClick = {
                                viewModel.toggleSave(postWithUser.post.id)
                            },
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