package pe.edu.idat.dsi.dami.idatgram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.Embedded

/**
 * Entidad Post para Room Database
 * Representa una publicación en el feed de Instagram
 */
@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Post(
    @PrimaryKey
    val id: String,
    val userId: String,
    val caption: String = "",
    val imageUrl: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val location: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Modelo para mostrar posts con información del usuario
 */
data class PostWithUser(
    @Embedded val post: Post,
    @Embedded(prefix = "user_") val user: User,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false
)

/**
 * Entidad para los likes de los posts
 */
@Entity(
    tableName = "post_likes",
    primaryKeys = ["postId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["postId"]), Index(value = ["userId"])]
)
data class PostLike(
    val postId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entidad para guardar posts (bookmark)
 */
@Entity(
    tableName = "saved_posts",
    primaryKeys = ["postId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["postId"]), Index(value = ["userId"])]
)
data class SavedPost(
    val postId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)