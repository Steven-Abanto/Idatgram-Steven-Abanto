package pe.edu.idat.dsi.dami.idatgram.data.remote.dto

import com.google.gson.annotations.SerializedName
import pe.edu.idat.dsi.dami.idatgram.data.entity.User

/**
 * DTO que viene del API.
 * Ajusta nombres si tu JSON usa otras keys.
 */
data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("bio") val bio: String? = "",
    @SerializedName("profileImageUrl") val profileImageUrl: String? = "",
    @SerializedName("followersCount") val followersCount: Int? = 0,
    @SerializedName("followingCount") val followingCount: Int? = 0,
    @SerializedName("postsCount") val postsCount: Int? = 0,
    @SerializedName("isVerified") val isVerified: Boolean? = false,
    @SerializedName("isPrivate") val isPrivate: Boolean? = false
)

fun UserDto.toEntity(): User {
    return User(
        id = id,
        username = username,
        email = email,
        displayName = displayName,
        bio = bio ?: "",
        profileImageUrl = profileImageUrl ?: "",
        followersCount = followersCount ?: 0,
        followingCount = followingCount ?: 0,
        postsCount = postsCount ?: 0,
        isVerified = isVerified ?: false,
        isPrivate = isPrivate ?: false
    )
}
