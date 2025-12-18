package pe.edu.idat.dsi.dami.idatgram.ui.screens.story

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pe.edu.idat.dsi.dami.idatgram.ui.viewmodel.StoryViewerViewModel
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewerScreen(
    userId: String,
    onClose: () -> Unit,
    viewModel: StoryViewerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) { viewModel.load(userId) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            state.error != null -> {
                Text(
                    text = state.error!!,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            state.stories.isEmpty() -> {
                Text(
                    text = "No hay stories",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                val current = state.stories[state.currentIndex]

                // Imagen
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(current.story.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(state.currentIndex) {
                            detectTapGestures { offset ->
                                val w = size.width
                                if (offset.x < w * 0.35f) {
                                    viewModel.prev()
                                } else {
                                    viewModel.next(onFinished = onClose)
                                }
                            }
                        }
                )

                // Barra superior (progress + close)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
//                        .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                        .padding(top = 20.dp, start = 12.dp, end = 12.dp)
                ) {
                    // Progress segmentado simple (no animado aún)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(state.stories.size) { i ->
                            LinearProgressIndicator(
                                progress = when {
                                    i < state.currentIndex -> 1f
                                    i == state.currentIndex -> 0.5f
                                    else -> 0f
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "@${current.user.username}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
