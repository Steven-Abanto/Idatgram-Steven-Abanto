package pe.edu.idat.dsi.dami.idatgram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.Embedded
import androidx.room.Ignore

/**
 * Entidad Story para Room Database
 * Representa las historias de Instagram
 */
@Entity(
    tableName = "stories",
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
data class Story(
    @PrimaryKey
    val id: String,
    val userId: String,
    val imageUrl: String,
    val text: String = "",
    val backgroundColor: String = "#000000",
    val textColor: String = "#FFFFFF",
    val viewsCount: Int = 0,
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 horas
    val createdAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt
}

/**
 * Modelo para mostrar stories con información del usuario
 */
data class StoryWithUser(
    @Embedded val story: Story,
    @Embedded(prefix = "user_") val user: User,
    val isViewed: Boolean = false
)

/**
 * Entidad para agrupar stories por usuario
 */
data class UserStories(
    @Embedded val user: User,
    @Ignore val stories: List<Story>,
    val hasUnviewedStories: Boolean = false
) {
    // Constructor para Room
    constructor(
        user: User,
        hasUnviewedStories: Boolean
    ) : this(user, emptyList(), hasUnviewedStories)
}

/**
 * Entidad para rastrear qué stories ha visto cada usuario
 */
@Entity(
    tableName = "story_views",
    primaryKeys = ["storyId", "viewerId"],
    foreignKeys = [
        ForeignKey(
            entity = Story::class,
            parentColumns = ["id"],
            childColumns = ["storyId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["viewerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["storyId"]), Index(value = ["viewerId"])]
)
data class StoryView(
    val storyId: String,
    val viewerId: String,
    val viewedAt: Long = System.currentTimeMillis()
)