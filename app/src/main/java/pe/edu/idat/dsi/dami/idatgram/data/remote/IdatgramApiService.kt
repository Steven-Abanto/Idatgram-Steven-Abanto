package pe.edu.idat.dsi.dami.idatgram.data.remote

import pe.edu.idat.dsi.dami.idatgram.data.remote.dto.PostDto
import pe.edu.idat.dsi.dami.idatgram.data.remote.dto.UserDto
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

    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}
