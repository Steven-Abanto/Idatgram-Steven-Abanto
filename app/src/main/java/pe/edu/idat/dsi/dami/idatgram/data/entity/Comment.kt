package pe.edu.idat.dsi.dami.idatgram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.Embedded
import androidx.room.Ignore

/**
 * Entidad Comment para Room Database
 * Representa comentarios en las publicaciones
 */
@Entity(
    tableName = "comments",
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
data class Comment(
    @PrimaryKey
    val id: String,
    val postId: String,
    val userId: String,
    val text: String,
    val likesCount: Int = 0,
    val repliesCount: Int = 0,
    val parentCommentId: String? = null, // Para respuestas a comentarios
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Modelo para mostrar comentarios con información del usuario
 */
data class CommentWithUser(
    @Embedded val comment: Comment,
    @Embedded(prefix = "user_") val user: User,
    val isLiked: Boolean = false,
    @Ignore val replies: List<CommentWithUser> = emptyList()
) {
    // Constructor para Room
    constructor(
        comment: Comment,
        user: User,
        isLiked: Boolean
    ) : this(comment, user, isLiked, emptyList())
}

/**
 * Entidad para los likes de comentarios
 */
@Entity(
    tableName = "comment_likes",
    primaryKeys = ["commentId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Comment::class,
            parentColumns = ["id"],
            childColumns = ["commentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["commentId"]), Index(value = ["userId"])]
)
data class CommentLike(
    val commentId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)