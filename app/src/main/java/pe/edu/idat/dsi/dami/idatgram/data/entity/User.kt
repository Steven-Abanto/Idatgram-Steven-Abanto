package pe.edu.idat.dsi.dami.idatgram.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded

/**
 * Entidad User para Room Database
 * Representa un usuario en la aplicación Instagram Clone
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val bio: String = "",
    val profileImageUrl: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val isVerified: Boolean = false,
    val isPrivate: Boolean = false,
    val website: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Modelo de datos para el perfil de usuario (UI)
 * Contiene información adicional para la presentación
 */
data class UserProfile(
    @Embedded val user: User,
    val isFollowing: Boolean = false,
    val isFollowedBy: Boolean = false,
    val mutualFollowersCount: Int = 0
)

/**
 * Entidad para relaciones de seguimiento entre usuarios
 */
@Entity(
    tableName = "user_follows",
    primaryKeys = ["followerId", "followingId"]
)
data class UserFollow(
    val followerId: String,
    val followingId: String,
    val createdAt: Long = System.currentTimeMillis()
)