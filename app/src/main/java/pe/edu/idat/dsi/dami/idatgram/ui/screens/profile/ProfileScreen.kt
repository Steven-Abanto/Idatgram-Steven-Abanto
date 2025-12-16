package pe.edu.idat.dsi.dami.idatgram.ui.screens.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pe.edu.idat.dsi.dami.idatgram.R
import pe.edu.idat.dsi.dami.idatgram.data.entity.*
import pe.edu.idat.dsi.dami.idatgram.ui.components.*
import pe.edu.idat.dsi.dami.idatgram.ui.theme.*

/**
 * Pantalla de perfil de usuario de Instagram
 * 
 * Implementa:
 * - Información del usuario (avatar, nombre, bio, stats)
 * - Botones de acción (seguir, mensaje, opciones)
 * - Grid de fotos del usuario
 * - Pestañas para diferentes tipos de contenido
 * - Highlights de historias
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    userPosts: List<PostWithUser> = emptyList(),
    isOwnProfile: Boolean = false,
    isFollowing: Boolean = false,
    onFollowClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onPostClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onOptionsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        
        // === Barra Superior ===
        TopAppBar(
            title = {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                if (!isOwnProfile) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                }
            },
            actions = {
                if (isOwnProfile) {
                    IconButton(onClick = { /* TODO: Crear post */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.create_post)
                        )
                    }
                }
                IconButton(onClick = onOptionsClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // === Información del Perfil ===
        ProfileHeader(
            user = user,
            isOwnProfile = isOwnProfile,
            isFollowing = isFollowing,
            onFollowClick = onFollowClick,
            onMessageClick = onMessageClick,
            onEditProfileClick = onEditProfileClick,
            modifier = Modifier.padding(16.dp)
        )
        
        // === Pestañas ===
        var selectedTab by remember { mutableIntStateOf(0) }
        
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = stringResource(R.string.posts_tab)
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = {
                    Icon(
                        imageVector = Icons.Default.PersonPin,
                        contentDescription = stringResource(R.string.tagged_tab)
                    )
                }
            )
        }
        
        // === Contenido de las Pestañas ===
        when (selectedTab) {
            0 -> {
                // Grid de Posts
                if (userPosts.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(
                            items = userPosts,
                            key = { post -> post.post.id }
                        ) { postWithUser ->
                            PostGridItem(
                                post = postWithUser.post,
                                onClick = { onPostClick(postWithUser.post.id) }
                            )
                        }
                    }
                } else {
                    EmptyPostsGrid(
                        isOwnProfile = isOwnProfile,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    )
                }
            }
            1 -> {
                // Posts donde está etiquetado
                EmptyTaggedGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            }
        }
    }
}

/**
 * Header del perfil con información del usuario
 */
@Composable
private fun ProfileHeader(
    user: User,
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        
        // === Fila superior: Avatar y Stats ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            // Avatar
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profileImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.profile_picture, user.username),
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Stats
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    count = user.postsCount,
                    label = stringResource(R.string.posts_count)
                )
                ProfileStat(
                    count = user.followersCount,
                    label = stringResource(R.string.followers_count)
                )
                ProfileStat(
                    count = user.followingCount,
                    label = stringResource(R.string.following_count)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // === Información del usuario ===
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.verified_account),
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            if (user.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (user.website.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.website,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // === Botones de Acción ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isOwnProfile) {
                IdatgramOutlinedButton(
                    text = stringResource(R.string.edit_profile),
                    onClick = onEditProfileClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                IdatgramButton(
                    text = if (isFollowing) {
                        stringResource(R.string.following)
                    } else {
                        stringResource(R.string.follow)
                    },
                    onClick = onFollowClick,
                    modifier = Modifier.weight(1f)
                )
                
                IdatgramOutlinedButton(
                    text = stringResource(R.string.message),
                    onClick = onMessageClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Componente individual de estadística
 */
@Composable
private fun ProfileStat(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Item individual del grid de posts
 */
@Composable
private fun PostGridItem(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(post.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = post.caption,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Grid vacío cuando no hay posts
 */
@Composable
private fun EmptyPostsGrid(
    isOwnProfile: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isOwnProfile) {
                stringResource(R.string.no_posts_own)
            } else {
                stringResource(R.string.no_posts_user)
            },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isOwnProfile) {
                stringResource(R.string.no_posts_own_subtitle)
            } else {
                stringResource(R.string.no_posts_user_subtitle)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Grid vacío para posts etiquetados
 */
@Composable
private fun EmptyTaggedGrid(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonPin,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.no_tagged_posts),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.no_tagged_posts_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Formatea números grandes (ej: 1.2K, 1.5M)
 */
private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${(count / 1_000_000.0).format(1)}M"
        count >= 1_000 -> "${(count / 1_000.0).format(1)}K"
        else -> count.toString()
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this).trimEnd('0').trimEnd('.')

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    IdatgramTheme {
        ProfileScreen(
            user = sampleUser(),
            userPosts = sampleUserPosts(),
            isOwnProfile = true
        )
    }
}

// Datos de ejemplo para preview
private fun sampleUser(): User {
    return User(
        id = "1",
        username = "johndoe",
        email = "john@example.com",
        displayName = "John Doe",
        bio = "Fotógrafo profesional 📸\nAmante de la naturaleza 🌿\nLima, Perú 🇵🇪",
        profileImageUrl = "https://via.placeholder.com/150",
        followersCount = 1250,
        followingCount = 340,
        postsCount = 89,
        isVerified = true,
        website = "johndoe.photography"
    )
}

private fun sampleUserPosts(): List<PostWithUser> {
    val user = sampleUser()
    val posts = (1..9).map { index ->
        Post(
            id = index.toString(),
            userId = "1",
            caption = "Post número $index",
            imageUrl = "https://via.placeholder.com/300",
            likesCount = (50..200).random(),
            commentsCount = (5..50).random()
        )
    }
    
    return posts.map { post ->
        PostWithUser(
            post = post,
            user = user,
            isLiked = false,
            isSaved = false
        )
    }
}