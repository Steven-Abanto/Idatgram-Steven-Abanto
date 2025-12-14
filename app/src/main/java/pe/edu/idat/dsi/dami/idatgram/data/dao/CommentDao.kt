package pe.edu.idat.dsi.dami.idatgram.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pe.edu.idat.dsi.dami.idatgram.data.entity.Comment
import pe.edu.idat.dsi.dami.idatgram.data.entity.CommentLike
import pe.edu.idat.dsi.dami.idatgram.data.entity.CommentWithUser

/**
 * DAO para operaciones relacionadas con comentarios
 * Incluye CRUD, likes, respuestas y threads
 */
@Dao
interface CommentDao {
    
    // CRUD Básico
    
    @Query("SELECT * FROM comments WHERE id = :commentId")
    suspend fun getCommentById(commentId: String): Comment?
    
    @Query("SELECT * FROM comments WHERE postId = :postId AND parentCommentId IS NULL ORDER BY createdAt ASC")
    fun getPostComments(postId: String): Flow<List<Comment>>
    
    @Query("SELECT * FROM comments WHERE parentCommentId = :parentCommentId ORDER BY createdAt ASC")
    suspend fun getCommentReplies(parentCommentId: String): List<Comment>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<Comment>)
    
    @Update
    suspend fun updateComment(comment: Comment)
    
    @Delete
    suspend fun deleteComment(comment: Comment)
    
    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: String)
    
    // Comentarios con Usuario
    
    @Query("""
        SELECT 
            c.*,
            u.id as user_id,
            u.username as user_username,
            u.email as user_email,
            u.displayName as user_displayName,
            u.bio as user_bio,
            u.profileImageUrl as user_profileImageUrl,
            u.followersCount as user_followersCount,
            u.followingCount as user_followingCount,
            u.postsCount as user_postsCount,
            u.isVerified as user_isVerified,
            u.isPrivate as user_isPrivate,
            u.website as user_website,
            u.createdAt as user_createdAt,
            u.updatedAt as user_updatedAt,
            CASE WHEN cl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked
        FROM comments c
        INNER JOIN users u ON c.userId = u.id
        LEFT JOIN comment_likes cl ON c.id = cl.commentId AND cl.userId = :currentUserId
        WHERE c.postId = :postId AND c.parentCommentId IS NULL
        ORDER BY c.createdAt ASC
    """)
    fun getPostCommentsWithUser(postId: String, currentUserId: String): Flow<List<CommentWithUser>>
    
    @Query("""
        SELECT 
            c.*,
            u.id as user_id,
            u.username as user_username,
            u.email as user_email,
            u.displayName as user_displayName,
            u.bio as user_bio,
            u.profileImageUrl as user_profileImageUrl,
            u.followersCount as user_followersCount,
            u.followingCount as user_followingCount,
            u.postsCount as user_postsCount,
            u.isVerified as user_isVerified,
            u.isPrivate as user_isPrivate,
            u.website as user_website,
            u.createdAt as user_createdAt,
            u.updatedAt as user_updatedAt,
            CASE WHEN cl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked
        FROM comments c
        INNER JOIN users u ON c.userId = u.id
        LEFT JOIN comment_likes cl ON c.id = cl.commentId AND cl.userId = :currentUserId
        WHERE c.parentCommentId = :parentCommentId
        ORDER BY c.createdAt ASC
    """)
    suspend fun getCommentRepliesWithUser(
        parentCommentId: String, 
        currentUserId: String
    ): List<CommentWithUser>
    
    @Query("""
        SELECT 
            c.*,
            u.id as user_id,
            u.username as user_username,
            u.email as user_email,
            u.displayName as user_displayName,
            u.bio as user_bio,
            u.profileImageUrl as user_profileImageUrl,
            u.followersCount as user_followersCount,
            u.followingCount as user_followingCount,
            u.postsCount as user_postsCount,
            u.isVerified as user_isVerified,
            u.isPrivate as user_isPrivate,
            u.website as user_website,
            u.createdAt as user_createdAt,
            u.updatedAt as user_updatedAt,
            CASE WHEN cl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked
        FROM comments c
        INNER JOIN users u ON c.userId = u.id
        LEFT JOIN comment_likes cl ON c.id = cl.commentId AND cl.userId = :currentUserId
        WHERE c.userId = :userId
        ORDER BY c.createdAt DESC
        LIMIT :limit
    """)
    suspend fun getUserComments(
        userId: String, 
        currentUserId: String, 
        limit: Int = 50
    ): List<CommentWithUser>
    
    // Likes de Comentarios
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun likeComment(commentLike: CommentLike)
    
    @Query("DELETE FROM comment_likes WHERE commentId = :commentId AND userId = :userId")
    suspend fun unlikeComment(commentId: String, userId: String)
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM comment_likes 
            WHERE commentId = :commentId AND userId = :userId
        )
    """)
    suspend fun isCommentLiked(commentId: String, userId: String): Boolean
    
    @Query("SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId")
    suspend fun getCommentLikesCount(commentId: String): Int
    
    @Query("""
        SELECT u.* FROM users u
        INNER JOIN comment_likes cl ON u.id = cl.userId
        WHERE cl.commentId = :commentId
        ORDER BY cl.createdAt DESC
        LIMIT :limit
    """)
    suspend fun getCommentLikers(commentId: String, limit: Int = 50): List<pe.edu.idat.dsi.dami.idatgram.data.entity.User>
    
    // Estadísticas
    
    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun getPostCommentsCount(postId: String): Int
    
    @Query("SELECT COUNT(*) FROM comments WHERE parentCommentId = :parentCommentId")
    suspend fun getCommentRepliesCount(parentCommentId: String): Int
    
    @Query("SELECT COUNT(*) FROM comments WHERE userId = :userId")
    suspend fun getUserCommentsCount(userId: String): Int
    
    @Query("""
        UPDATE comments SET 
            likesCount = (SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId),
            repliesCount = (SELECT COUNT(*) FROM comments WHERE parentCommentId = :commentId)
        WHERE id = :commentId
    """)
    suspend fun updateCommentCounts(commentId: String)
    
    // Búsqueda de Comentarios
    
    @Query("""
        SELECT 
            c.*,
            u.id as user_id,
            u.username as user_username,
            u.email as user_email,
            u.displayName as user_displayName,
            u.bio as user_bio,
            u.profileImageUrl as user_profileImageUrl,
            u.followersCount as user_followersCount,
            u.followingCount as user_followingCount,
            u.postsCount as user_postsCount,
            u.isVerified as user_isVerified,
            u.isPrivate as user_isPrivate,
            u.website as user_website,
            u.createdAt as user_createdAt,
            u.updatedAt as user_updatedAt,
            CASE WHEN cl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked
        FROM comments c
        INNER JOIN users u ON c.userId = u.id
        LEFT JOIN comment_likes cl ON c.id = cl.commentId AND cl.userId = :currentUserId
        WHERE c.text LIKE '%' || :query || '%'
        OR u.username LIKE '%' || :query || '%'
        ORDER BY c.likesCount DESC, c.createdAt DESC
        LIMIT :limit
    """)
    suspend fun searchComments(
        query: String, 
        currentUserId: String, 
        limit: Int = 50
    ): List<CommentWithUser>
    
    // Menciones en Comentarios
    
    @Query("""
        SELECT 
            c.*,
            u.id as user_id,
            u.username as user_username,
            u.email as user_email,
            u.displayName as user_displayName,
            u.bio as user_bio,
            u.profileImageUrl as user_profileImageUrl,
            u.followersCount as user_followersCount,
            u.followingCount as user_followingCount,
            u.postsCount as user_postsCount,
            u.isVerified as user_isVerified,
            u.isPrivate as user_isPrivate,
            u.website as user_website,
            u.createdAt as user_createdAt,
            u.updatedAt as user_updatedAt,
            CASE WHEN cl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked
        FROM comments c
        INNER JOIN users u ON c.userId = u.id
        LEFT JOIN comment_likes cl ON c.id = cl.commentId AND cl.userId = :currentUserId
        WHERE c.text LIKE '%@' || :username || '%'
        ORDER BY c.createdAt DESC
        LIMIT :limit
    """)
    suspend fun getCommentsMentioning(
        username: String, 
        currentUserId: String, 
        limit: Int = 50
    ): List<CommentWithUser>
    
    // Comentarios Recientes
    
    @Query("""
        SELECT 
            c.*,
            u.id as user_id,
            u.username as user_username,
            u.email as user_email,
            u.displayName as user_displayName,
            u.bio as user_bio,
            u.profileImageUrl as user_profileImageUrl,
            u.followersCount as user_followersCount,
            u.followingCount as user_followingCount,
            u.postsCount as user_postsCount,
            u.isVerified as user_isVerified,
            u.isPrivate as user_isPrivate,
            u.website as user_website,
            u.createdAt as user_createdAt,
            u.updatedAt as user_updatedAt,
            CASE WHEN cl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked
        FROM comments c
        INNER JOIN users u ON c.userId = u.id
        LEFT JOIN comment_likes cl ON c.id = cl.commentId AND cl.userId = :currentUserId
        WHERE c.postId IN (
            SELECT id FROM posts WHERE userId = :userId
        )
        AND c.userId != :userId
        ORDER BY c.createdAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentCommentsOnUserPosts(
        userId: String, 
        currentUserId: String, 
        limit: Int = 20
    ): List<CommentWithUser>
}