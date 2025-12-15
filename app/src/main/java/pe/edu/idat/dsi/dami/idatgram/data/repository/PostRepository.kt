package pe.edu.idat.dsi.dami.idatgram.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pe.edu.idat.dsi.dami.idatgram.data.dao.PostDao
import pe.edu.idat.dsi.dami.idatgram.data.entity.Post
import pe.edu.idat.dsi.dami.idatgram.data.entity.PostLike
import pe.edu.idat.dsi.dami.idatgram.data.entity.PostWithUser
import pe.edu.idat.dsi.dami.idatgram.data.entity.SavedPost
import pe.edu.idat.dsi.dami.idatgram.data.remote.IdatgramApiService
import pe.edu.idat.dsi.dami.idatgram.data.remote.dto.toEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestión de posts
 */
@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val userRepository: UserRepository,
    private val apiService: IdatgramApiService
) {
    
    // CRUD de Posts
    
    suspend fun getPostById(postId: String): Post? {
        return postDao.getPostById(postId)
    }
    
    fun getPostsByUser(userId: String): Flow<List<Post>> {
        return postDao.getPostsByUser(userId)
    }
    
    fun getUserPostsWithDetails(userId: String): Flow<List<PostWithUser>> {
        val currentUserId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return postDao.getUserPostsWithDetails(userId, currentUserId)
    }
    
    suspend fun createPost(
        caption: String,
        imageUrl: String,
        location: String = ""
    ): Result<Post> {
        return try {
            val currentUserId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))
            
            val postId = "post_${System.currentTimeMillis()}"
            val newPost = Post(
                id = postId,
                userId = currentUserId,
                caption = caption.trim(),
                imageUrl = imageUrl,
                location = location.trim(),
                createdAt = System.currentTimeMillis()
            )
            
            postDao.insertPost(newPost)
            userRepository.updateUserStatistics(currentUserId)
            
            Result.success(newPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePost(
        postId: String,
        caption: String? = null,
        location: String? = null
    ): Result<Post> {
        return try {
            val currentUserId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))
            
            val existingPost = postDao.getPostById(postId)
                ?: return Result.failure(Exception("Post no encontrado"))
            
            if (existingPost.userId != currentUserId) {
                return Result.failure(Exception("No tienes permisos para editar este post"))
            }
            
            val updatedPost = existingPost.copy(
                caption = caption ?: existingPost.caption,
                location = location ?: existingPost.location,
                updatedAt = System.currentTimeMillis()
            )
            
            postDao.updatePost(updatedPost)
            Result.success(updatedPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val currentUserId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))
            
            val post = postDao.getPostById(postId)
                ?: return Result.failure(Exception("Post no encontrado"))
            
            if (post.userId != currentUserId) {
                return Result.failure(Exception("No tienes permisos para eliminar este post"))
            }
            
            postDao.deletePost(post)
            userRepository.updateUserStatistics(currentUserId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Feed de Posts
    
    suspend fun getFeedPosts(limit: Int = 20, offset: Int = 0): List<PostWithUser> {
        val currentUserId = userRepository.getCurrentUserId() ?: return emptyList()
        return postDao.getFeedPosts(currentUserId, limit, offset)
    }

// Trae los posts de manera local
//    suspend fun refreshFeed(): Result<List<PostWithUser>> {
//        return try {
//            val posts = getFeedPosts(limit = 50, offset = 0)
//            Result.success(posts)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

//Trae los posts de manera remota y los guarda en la base de datos local
    suspend fun refreshFeed(): Result<List<PostWithUser>> {
        return try {
            // Recupera desde el API
            val remoteUsers = apiService.getUsers()
            val remotePosts = apiService.getPosts()

            // Guarda en local si no existe, si existe, se ignora
            val userEntities = remoteUsers.map { it.toEntity() }
            userRepository.insertRemoteUsersSafe(userEntities)

            val postEntities = remotePosts.map { it.toEntity() }
            val insertResults = postDao.insertPostsIgnore(postEntities)

            val toUpdate = mutableListOf<Post>()
            for (i in insertResults.indices) {
                if (insertResults[i] == -1L) {
                    toUpdate.add(postEntities[i])
                }
            }

            if (toUpdate.isNotEmpty()) {
                postDao.updatePosts(toUpdate)
            }

            postEntities.forEach { post ->
                postDao.updatePostCounts(post.id)
            }

            // Carga el feed desde local
            val posts = getFeedPosts(limit = 50, offset = 0)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Likes

    suspend fun toggleLike(postId: String): Result<Boolean> {
        return try {
            val currentUserId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val isLiked = postDao.isPostLiked(postId, currentUserId)
            if (isLiked) {
                postDao.unlikePost(postId, currentUserId)
            } else {
                postDao.likePost(
                    PostLike(
                        postId = postId,
                        userId = currentUserId
                    )
                )
            }
            postDao.updatePostCounts(postId)
            Result.success(!isLiked)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    
    suspend fun isPostLiked(postId: String): Boolean {
        val currentUserId = userRepository.getCurrentUserId() ?: return false
        return postDao.isPostLiked(postId, currentUserId)
    }
    
    suspend fun getPostLikesCount(postId: String): Int {
        return postDao.getPostLikesCount(postId)
    }
    
    suspend fun getPostLikers(postId: String, limit: Int = 100): List<pe.edu.idat.dsi.dami.idatgram.data.entity.User> {
        return postDao.getPostLikers(postId, limit)
    }
    
    // Posts Guardados
    
    suspend fun toggleSavePost(postId: String): Result<Boolean> {
        return try {
            val currentUserId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val isSaved = postDao.isPostSaved(postId, currentUserId)

            if (isSaved) {
                postDao.unsavePost(postId, currentUserId)
            } else {
                postDao.savePost(
                    SavedPost(
                        postId = postId,
                        userId = currentUserId
                    )
                )
            }

            Result.success(!isSaved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun isPostSaved(postId: String): Boolean {
        val currentUserId = userRepository.getCurrentUserId() ?: return false
        return postDao.isPostSaved(postId, currentUserId)
    }
    
    fun getSavedPosts(): Flow<List<PostWithUser>> {
        val currentUserId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return postDao.getSavedPosts(currentUserId)
    }
    
    // Exploración y Búsqueda
    
    suspend fun searchPosts(query: String, limit: Int = 50): List<PostWithUser> {
        if (query.isBlank()) return emptyList()
        val currentUserId = userRepository.getCurrentUserId() ?: return emptyList()
        return postDao.searchPosts(query, currentUserId, limit)
    }
    
    suspend fun getExplorePosts(limit: Int = 30, offset: Int = 0): List<PostWithUser> {
        val currentUserId = userRepository.getCurrentUserId() ?: return emptyList()
        return postDao.getExplorePosts(currentUserId, limit, offset)
    }
    
    suspend fun getPostsByLocation(location: String, limit: Int = 20): List<PostWithUser> {
        if (location.isBlank()) return emptyList()
        val currentUserId = userRepository.getCurrentUserId() ?: return emptyList()
        return postDao.getPostsByLocation(location, currentUserId, limit)
    }
    
    // Estadísticas
    
    suspend fun getUserPostsCount(userId: String): Int {
        return postDao.getUserPostsCount(userId)
    }
    
    suspend fun getUserTotalLikes(userId: String): Int {
        return postDao.getUserTotalLikes(userId)
    }
    
    suspend fun updatePostStatistics(postId: String) {
        postDao.updatePostCounts(postId)
    }
    
    // Validaciones
    
    fun validatePost(caption: String, imageUrl: String): Result<Unit> {
        return when {
            imageUrl.isBlank() -> Result.failure(Exception("La imagen es requerida"))
            caption.length > 2200 -> Result.failure(Exception("El caption no puede exceder 2200 caracteres"))
            else -> Result.success(Unit)
        }
    }
    
    // Cache y Optimizaciones
    
    suspend fun preloadFeedPosts(): Result<Unit> {
        return try {
            // Pre-cargar posts para mejorar la experiencia
            getFeedPosts(limit = 10, offset = 0)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshPost(postId: String): Result<Post> {
        return try {
            val post = postDao.getPostById(postId)
                ?: return Result.failure(Exception("Post no encontrado"))
            
            // En el futuro, aquí se sincronizaría con el backend
            postDao.updatePostCounts(postId)
            
            val updatedPost = postDao.getPostById(postId)!!
            Result.success(updatedPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Métodos auxiliares
    
    suspend fun getPostWithUser(postId: String): PostWithUser? {
        val currentUserId = userRepository.getCurrentUserId() ?: return null
        val posts = postDao.searchPosts("", currentUserId, limit = 1000)
        return posts.find { it.post.id == postId }
    }
    
    // Análisis y Métricas
    
    suspend fun getPostEngagementRate(postId: String): Double {
        return try {
            val post = postDao.getPostById(postId) ?: return 0.0
            val user = userRepository.getUserById(post.userId) ?: return 0.0
            
            if (user.followersCount == 0) return 0.0
            
            val engagement = post.likesCount + post.commentsCount
            (engagement.toDouble() / user.followersCount.toDouble()) * 100
        } catch (e: Exception) {
            0.0
        }
    }
    
    suspend fun getUserTopPosts(userId: String, limit: Int = 5): List<Post> {
        return try {
            val allPosts = postDao.getPostsByUser(userId)
            // Simulamos obtener los primeros posts (en una implementación real se ordenarían por engagement)
            emptyList() // Por ahora retornamos lista vacía, se implementará con Flow transformation
        } catch (e: Exception) {
            emptyList()
        }
    }
}