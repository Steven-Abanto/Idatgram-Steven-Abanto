package pe.edu.idat.dsi.dami.idatgram.data.repository

import pe.edu.idat.dsi.dami.idatgram.data.dao.PostDao
import pe.edu.idat.dsi.dami.idatgram.data.dao.UserDao
import pe.edu.idat.dsi.dami.idatgram.data.entity.Post
import pe.edu.idat.dsi.dami.idatgram.data.entity.User
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio LOCAL (Room) para operaciones de base de datos comunes.
 *
 * ✅ Útil para Sync remoto -> local:
 * - upsert masivo de usuarios/posts
 * - centraliza operaciones de Room (en vez de repetir en cada repo)
 */
@Singleton
class LocalDatabaseRepository @Inject constructor(
    private val userDao: UserDao,
    private val postDao: PostDao
) {
    // ===== Usuarios =====
    suspend fun upsertUsers(users: List<User>) {
        userDao.insertUsers(users) // opción antigua (se mantiene)
        }

    // ===== Posts =====
    suspend fun upsertPosts(posts: List<Post>) {
        postDao.insertPosts(posts)
    }
}
