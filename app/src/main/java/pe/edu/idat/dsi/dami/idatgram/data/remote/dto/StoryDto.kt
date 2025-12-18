package pe.edu.idat.dsi.dami.idatgram.data.remote.dto

import com.google.gson.annotations.SerializedName
import pe.edu.idat.dsi.dami.idatgram.data.entity.Story

data class StoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("text") val text: String? = "",
    @SerializedName("backgroundColor") val backgroundColor: String? = "#000000",
    @SerializedName("textColor") val textColor: String? = "#FFFFFF",
    @SerializedName("viewsCount") val viewsCount: Int? = 0,
    @SerializedName("expiresAt") val expiresAt: Long? = null,
    @SerializedName("createdAt") val createdAt: Long? = null
)

fun StoryDto.toEntity(): Story = Story(
    id = id,
    userId = userId,
    imageUrl = imageUrl,
    text = text ?: "",
    backgroundColor = backgroundColor ?: "#000000",
    textColor = textColor ?: "#FFFFFF",
    viewsCount = viewsCount ?: 0,
    expiresAt = expiresAt ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000),
    createdAt = createdAt ?: System.currentTimeMillis()
)
