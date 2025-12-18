package pe.edu.idat.dsi.dami.idatgram.data.remote

import pe.edu.idat.dsi.dami.idatgram.data.remote.dto.CommentDto
import pe.edu.idat.dsi.dami.idatgram.data.remote.dto.PostDto
import pe.edu.idat.dsi.dami.idatgram.data.remote.dto.StoryDto
import pe.edu.idat.dsi.dami.idatgram.data.remote.dto.UserDto
import pe.edu.idat.dsi.dami.idatgram.data.remote.dto.UserFollowDto
import retrofit2.http.GET

/**
 * API Service para My JSON Server (Retrofit)
 *
 * IMPORTANTE:
 * - My JSON Server suele exponer endpoints tipo /users, /posts, etc.
 * - Si en tu JSON Server se llaman distinto (ej: /publicaciones), cambia las rutas.
 */
interface IdatgramApiService {

    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("user_follows")
    suspend fun getFollows(): List<UserFollowDto>

    @GET("posts")
    suspend fun getPosts(): List<PostDto>

    @GET("comments")
    suspend fun getComments(): List<CommentDto>

    @GET("stories")
    suspend fun getStories(): List<StoryDto>
}
