package pe.edu.idat.dsi.dami.idatgram.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pe.edu.idat.dsi.dami.idatgram.data.entity.PostWithUser
import pe.edu.idat.dsi.dami.idatgram.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
    ) {
        // Search bar
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Buscar usuarios o posts...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            singleLine = true
        )

        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        val isSearching = state.query.trim().isNotBlank()

        if (isSearching) {
            // Resultados: Users + Posts
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                item {
                    if (state.users.isNotEmpty()) {
                        Text(
                            text = "Usuarios",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                items(state.users.size) { index ->
                    val u = state.users[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUserClick(u.id) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(u.profileImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(text = u.username, style = MaterialTheme.typography.bodyLarge)
                            Text(text = u.displayName, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item {
                    if (state.posts.isNotEmpty()) {
                        Text(
                            text = "Posts",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                items(state.posts.size) { index ->
                    val p = state.posts[index]
                    SearchPostRow(post = p, onClick = { onPostClick(p.post.id) })
                }
            }
        } else {
            // Explore grid
            ExploreGrid(
                posts = state.explorePosts,
                onPostClick = onPostClick,
                onRefresh = viewModel::refreshExplore
            )
        }
    }
}

@Composable
private fun SearchPostRow(post: PostWithUser, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(post.post.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(text = "@${post.user.username}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = post.post.caption,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ExploreGrid(
    posts: List<PostWithUser>,
    onPostClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    // Simple botón refresh (puedes cambiarlo por pull-to-refresh si quieres)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onRefresh) { Text("Actualizar") }
    }

    if (posts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin contenido para explorar")
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(posts, key = { it.post.id }) { p ->
            Card(
                onClick = { onPostClick(p.post.id) },
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(0.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(p.post.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
