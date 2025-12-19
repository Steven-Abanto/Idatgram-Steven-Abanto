package pe.edu.idat.dsi.dami.idatgram.ui.screens.story

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import pe.edu.idat.dsi.dami.idatgram.data.entity.StoryWithUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewerScreen(
    stories: List<StoryWithUser>,
    startIndex: Int = 0,
    durationMs: Int = 5000, // 5s por story
    onClose: () -> Unit,
    onStoryViewed: (String) -> Unit = {} // storyId
) {
    if (stories.isEmpty()) {
        // No hay stories => cerrar
        LaunchedEffect(Unit) { onClose() }
        return
    }

    var currentIndex by remember { mutableIntStateOf(startIndex.coerceIn(0, stories.lastIndex)) }
    var isPaused by remember { mutableStateOf(false) }

    val progress = remember { Animatable(0f) }

    // Cada vez que cambia de story: marca visto y reinicia animación
    LaunchedEffect(currentIndex) {
        val currentStoryId = stories[currentIndex].story.id
        onStoryViewed(currentStoryId)

        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMs)
        )

        // Cuando termina el progreso => siguiente o cerrar
        if (currentIndex < stories.lastIndex) {
            currentIndex++
        } else {
            onClose()
        }
    }

    // Pausa / Reanuda (mantiene el progreso donde quedó)
    LaunchedEffect(isPaused) {
        if (isPaused) {
            progress.stop()
        } else {
            // reanuda desde el progreso actual
            val remaining = ((1f - progress.value) * durationMs).toInt().coerceAtLeast(1)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(remaining)
            )
            if (currentIndex < stories.lastIndex) currentIndex++ else onClose()
        }
    }

    val current = stories[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Imagen
        coil.compose.AsyncImage(
            model = current.story.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Barra(s) de progreso
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            StoryProgressBars(
                count = stories.size,
                currentIndex = currentIndex,
                currentProgress = progress.value
            )

            Spacer(Modifier.height(10.dp))

            // Header (usuario + cerrar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = current.user.username,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }
        }

        // Zonas táctiles (izq = atrás, der = siguiente) + press-and-hold para pausar
        Row(Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(currentIndex) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = {
                                if (currentIndex > 0) currentIndex--
                            }
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(currentIndex) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = {
                                if (currentIndex < stories.lastIndex) currentIndex++
                                else onClose()
                            }
                        )
                    }
            )
        }
    }
}

@Composable
private fun StoryProgressBars(
    count: Int,
    currentIndex: Int,
    currentProgress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(count) { index ->
            val value = when {
                index < currentIndex -> 1f
                index == currentIndex -> currentProgress
                else -> 0f
            }

            LinearProgressIndicator(
                progress = { value },
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.35f)
            )
        }
    }
}