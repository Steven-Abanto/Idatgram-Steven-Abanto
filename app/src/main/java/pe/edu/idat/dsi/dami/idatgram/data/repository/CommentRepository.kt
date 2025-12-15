package pe.edu.idat.dsi.dami.idatgram.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pe.edu.idat.dsi.dami.idatgram.data.dao.CommentDao
import pe.edu.idat.dsi.dami.idatgram.data.dao.PostDao
import pe.edu.idat.dsi.dami.idatgram.data.entity.Comment
import pe.edu.idat.dsi.dami.idatgram.data.entity.CommentWithUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val commentDao: CommentDao,
    private val postDao: PostDao,
    private val userRepository: UserRepository
) {

    fun getPostCommentsWithUser(postId: String): Flow<List<CommentWithUser>> {
        val currentUserId = userRepository.getCurrentUserId() ?: return flowOf(emptyList())
        return commentDao.getPostCommentsWithUser(postId, currentUserId)
    }

    suspend fun addComment(
        postId: String,
        text: String,
        parentCommentId: String? = null
    ): Result<Comment> {
        return try {
            val currentUserId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val cleanText = text.trim()
            if (cleanText.isBlank()) {
                return Result.failure(Exception("El comentario no puede estar vacío"))
            }

            val comment = Comment(
                id = "comment_${System.currentTimeMillis()}",
                postId = postId,
                userId = currentUserId,
                text = cleanText,
                parentCommentId = parentCommentId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            commentDao.insertComment(comment)

            // ✅ Recalcular contador de comentarios del post
            postDao.updatePostCounts(postId)

            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
