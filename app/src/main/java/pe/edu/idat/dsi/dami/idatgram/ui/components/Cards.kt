package pe.edu.idat.dsi.dami.idatgram.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pe.edu.idat.dsi.dami.idatgram.data.entity.Post
import pe.edu.idat.dsi.dami.idatgram.data.entity.PostWithUser
import pe.edu.idat.dsi.dami.idatgram.data.entity.User
import pe.edu.idat.dsi.dami.idatgram.ui.theme.*

/**
 * Componente para mostrar un post completo en el feed
 */
@Composable
fun PostCard(
    postWithUser: PostWithUser,
    onUserClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    onSaveClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Header del post
            PostHeader(
                user = postWithUser.user,
                location = postWithUser.post.location,
                onUserClick = { onUserClick(postWithUser.user.id) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            
            // Imagen del post
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(postWithUser.post.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagen del post",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            
            // Acciones del post
            PostActions(
                isLiked = postWithUser.isLiked,
                isSaved = postWithUser.isSaved,
                onLikeClick = { onLikeClick(postWithUser.post.id) },
                onCommentClick = { onCommentClick(postWithUser.post.id) },
                onShareClick = { onShareClick(postWithUser.post.id) },
                onSaveClick = { onSaveClick(postWithUser.post.id) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Información del post
            PostInfo(
                post = postWithUser.post,
                user = postWithUser.user,
                onUserClick = { onUserClick(postWithUser.user.id) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Header del post con avatar y usuario
 */
@Composable
private fun PostHeader(
    user: User,
    location: String,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar del usuario
        UserAvatar(
            imageUrl = user.profileImageUrl,
            size = 32.dp,
            onClick = onUserClick
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Información del usuario
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onUserClick() }
                )
                
                if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verificado",
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            if (location.isNotEmpty()) {
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Menú de opciones
        IdatgramIconButton(
            icon = Icons.Default.MoreVert,
            onClick = { /* TODO: Implementar menú */ },
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Acciones del post (like, comment, share, save)
 */
@Composable
private fun PostActions(
    isLiked: Boolean,
    isSaved: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            IdatgramIconButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                onClick = onLikeClick,
                tint = if (isLiked) LikeRed else MaterialTheme.colorScheme.onSurface,
                contentDescription = if (isLiked) "Quitar like" else "Dar like"
            )
            
            IdatgramIconButton(
                icon = Icons.Default.ChatBubbleOutline,
                onClick = onCommentClick,
                contentDescription = "Comentar"
            )
            
            IdatgramIconButton(
                icon = Icons.Default.Send,
                onClick = onShareClick,
                contentDescription = "Compartir"
            )
        }
        
        IdatgramIconButton(
            icon = if (isSaved) Icons.Default.Star else Icons.Default.Add,
            onClick = onSaveClick,
            contentDescription = if (isSaved) "Quitar de guardados" else "Guardar"
        )
    }
}

/**
 * Información del post (likes, caption, comentarios)
 */
@Composable
private fun PostInfo(
    post: Post,
    user: User,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Número de likes
        if (post.likesCount > 0) {
            Text(
                text = "${post.likesCount} Me gusta",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Caption
        if (post.caption.isNotEmpty()) {
            Row {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onUserClick() }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Número de comentarios
        if (post.commentsCount > 0) {
            Text(
                text = "Ver los ${post.commentsCount} comentarios",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { /* TODO: Ir a comentarios */ }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Tiempo transcurrido (placeholder)
        Text(
            text = "hace 2 horas",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Avatar de usuario con posible gradiente para stories
 */
@Composable
fun UserAvatar(
    imageUrl: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    hasStory: Boolean = false,
    isStoryViewed: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val borderModifier = if (hasStory) {
        val colors = if (isStoryViewed) {
            listOf(MaterialTheme.colorScheme.outline)
        } else {
            listOf(StoryGradientStart, StoryGradientEnd)
        }
        Modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(colors),
            shape = CircleShape
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = CircleShape
        )
    }
    
    Box(
        modifier = modifier
            .size(size)
            .then(borderModifier)
            .padding(2.dp)
            .clip(CircleShape)
            .let { if (onClick != null) it.clickable { onClick() } else it }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar del usuario",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            fallback = null // TODO: Agregar imagen por defecto
        )
    }
}

/**
 * Tarjeta pequeña de usuario para listas
 */
@Composable
fun UserCard(
    user: User,
    onUserClick: (String) -> Unit,
    onFollowClick: (String) -> Unit,
    isFollowing: Boolean = false,
    modifier: Modifier = Modifier,
    showFollowButton: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onUserClick(user.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                imageUrl = user.profileImageUrl,
                size = 48.dp,
                onClick = { onUserClick(user.id) }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verificado",
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (user.bio.isNotEmpty()) {
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            if (showFollowButton) {
                Spacer(modifier = Modifier.width(8.dp))
                
                if (isFollowing) {
                    IdatgramOutlinedButton(
                        text = "Siguiendo",
                        onClick = { onFollowClick(user.id) },
                        modifier = Modifier.width(100.dp)
                    )
                } else {
                    IdatgramButton(
                        text = "Seguir",
                        onClick = { onFollowClick(user.id) },
                        modifier = Modifier.width(100.dp)
                    )
                }
            }
        }
    }
}

// === Previews ===

@Preview(showBackground = true)
@Composable
private fun PostCardPreview() {
    IdatgramTheme {
        val sampleUser = User(
            id = "1",
            username = "john_doe",
            email = "john@example.com",
            displayName = "John Doe",
            bio = "Photographer",
            profileImageUrl = "https://picsum.photos/200/200?random=1",
            isVerified = true
        )
        
        val samplePost = Post(
            id = "1",
            userId = "1",
            caption = "Beautiful sunset at the beach! 🌅 #photography #nature",
            imageUrl = "https://picsum.photos/400/400?random=1",
            likesCount = 245,
            commentsCount = 12,
            location = "Malibu Beach, CA"
        )
        
        val postWithUser = PostWithUser(
            post = samplePost,
            user = sampleUser,
            isLiked = false,
            isSaved = false
        )
        
        PostCard(
            postWithUser = postWithUser,
            onUserClick = { },
            onLikeClick = { },
            onCommentClick = { },
            onShareClick = { },
            onSaveClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserCardPreview() {
    IdatgramTheme {
        val sampleUser = User(
            id = "1",
            username = "jane_smith",
            email = "jane@example.com",
            displayName = "Jane Smith",
            bio = "Digital Artist | Coffee lover ☕",
            profileImageUrl = "https://picsum.photos/200/200?random=2",
            isVerified = false
        )
        
        UserCard(
            user = sampleUser,
            onUserClick = { },
            onFollowClick = { },
            isFollowing = false
        )
    }
}

/**
 * Componente para mostrar stories circulares
 */
@Composable
fun StoryCircle(
    imageUrl: String,
    username: String,
    hasNewStory: Boolean = false,
    isOwnStory: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            UserAvatar(
                imageUrl = imageUrl,
                size = 64.dp,
                hasStory = hasNewStory,
                onClick = onClick
            )
            
            if (isOwnStory) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar historia",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = username,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StoryCirclePreview() {
    IdatgramTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StoryCircle(
                imageUrl = "https://picsum.photos/100/100?random=1",
                username = "Tu historia",
                isOwnStory = true,
                onClick = {}
            )
            StoryCircle(
                imageUrl = "https://picsum.photos/100/100?random=2",
                username = "john_doe",
                hasNewStory = true,
                onClick = {}
            )
        }
    }
}