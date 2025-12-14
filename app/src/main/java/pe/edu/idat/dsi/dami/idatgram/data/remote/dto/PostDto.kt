package pe.edu.idat.dsi.dami.idatgram.data.remote.dto

import com.google.gson.annotations.SerializedName
import pe.edu.idat.dsi.dami.idatgram.data.entity.Post

/**
 * DTO que viene del API.
 * Ajusta nombres si tu JSON usa otras keys.
 */
data class PostDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("caption") val caption: String? = "",
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("likesCount") val likesCount: Int? = 0,
    @SerializedName("commentsCount") val commentsCount: Int? = 0,
    @SerializedName("location") val location: String? = "",
    @SerializedName("createdAt") val createdAt: Long? = null
)

fun PostDto.toEntity(): Post {
    return Post(
        id = id,
        userId = userId,
        caption = caption ?: "",
        imageUrl = imageUrl,
        likesCount = likesCount ?: 0,
        commentsCount = commentsCount ?: 0,
        location = location ?: "",
        createdAt = createdAt ?: System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
