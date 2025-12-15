package pe.edu.idat.dsi.dami.idatgram.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pe.edu.idat.dsi.dami.idatgram.data.entity.Post
import pe.edu.idat.dsi.dami.idatgram.data.entity.PostLike
import pe.edu.idat.dsi.dami.idatgram.data.entity.PostWithUser
import pe.edu.idat.dsi.dami.idatgram.data.entity.SavedPost

/**
 * DAO para operaciones relacionadas con posts
 * Incluye feed, likes, guardados y estadísticas
 */
@Dao
interface PostDao {
    
    // CRUD Básico
    
    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: String): Post?
    
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUser(userId: String): Flow<List<Post>>
    
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<Post>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    // Inserta sin reemplazar (si existe en local, no hace nada)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPostsIgnore(posts: List<Post>): List<Long>
    
    @Update
    suspend fun updatePost(post: Post)

    // Update en batch
    @Update
    suspend fun updatePosts(posts: List<Post>)
    
    @Delete
    suspend fun deletePost(post: Post)
    
    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)
    
    // Feed de Posts con Usuarios
    
    @Query("""
        SELECT 
            p.*,
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
            CASE WHEN pl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked,
            CASE WHEN sp.userId IS NOT NULL THEN 1 ELSE 0 END as isSaved
        FROM posts p
        INNER JOIN users u ON p.userId = u.id
        LEFT JOIN post_likes pl ON p.id = pl.postId AND pl.userId = :currentUserId
        LEFT JOIN saved_posts sp ON p.id = sp.postId AND sp.userId = :currentUserId
        WHERE p.userId IN (
            SELECT followingId FROM user_follows WHERE followerId = :currentUserId
            UNION SELECT :currentUserId
        )
        ORDER BY p.createdAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getFeedPosts(
        currentUserId: String, 
        limit: Int = 20, 
        offset: Int = 0
    ): List<PostWithUser>
    
    @Query("""
        SELECT 
            p.*,
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
            CASE WHEN pl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked,
            CASE WHEN sp.userId IS NOT NULL THEN 1 ELSE 0 END as isSaved
        FROM posts p
        INNER JOIN users u ON p.userId = u.id
        LEFT JOIN post_likes pl ON p.id = pl.postId AND pl.userId = :currentUserId
        LEFT JOIN saved_posts sp ON p.id = sp.postId AND sp.userId = :currentUserId
        WHERE p.userId = :userId
        ORDER BY p.createdAt DESC
    """)
    fun getUserPostsWithDetails(userId: String, currentUserId: String): Flow<List<PostWithUser>>
    
    @Query("""
        SELECT 
            p.*,
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
            CASE WHEN pl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked,
            1 as isSaved
        FROM posts p
        INNER JOIN users u ON p.userId = u.id
        INNER JOIN saved_posts sp ON p.id = sp.postId AND sp.userId = :currentUserId
        LEFT JOIN post_likes pl ON p.id = pl.postId AND pl.userId = :currentUserId
        ORDER BY sp.createdAt DESC
    """)
    fun getSavedPosts(currentUserId: String): Flow<List<PostWithUser>>
    
    // Likes
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun likePost(postLike: PostLike)
    
    @Query("DELETE FROM post_likes WHERE postId = :postId AND userId = :userId")
    suspend fun unlikePost(postId: String, userId: String)
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM post_likes 
            WHERE postId = :postId AND userId = :userId
        )
    """)
    suspend fun isPostLiked(postId: String, userId: String): Boolean
    
    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
    suspend fun getPostLikesCount(postId: String): Int
    
    @Query("""
        SELECT u.* FROM users u
        INNER JOIN post_likes pl ON u.id = pl.userId
        WHERE pl.postId = :postId
        ORDER BY pl.createdAt DESC
        LIMIT :limit
    """)
    suspend fun getPostLikers(postId: String, limit: Int = 100): List<pe.edu.idat.dsi.dami.idatgram.data.entity.User>
    
    // Posts Guardados
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePost(savedPost: SavedPost)
    
    @Query("DELETE FROM saved_posts WHERE postId = :postId AND userId = :userId")
    suspend fun unsavePost(postId: String, userId: String)
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM saved_posts 
            WHERE postId = :postId AND userId = :userId
        )
    """)
    suspend fun isPostSaved(postId: String, userId: String): Boolean
    
    // Búsqueda y Exploración
    
    @Query("""
        SELECT 
            p.*,
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
            CASE WHEN pl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked,
            CASE WHEN sp.userId IS NOT NULL THEN 1 ELSE 0 END as isSaved
        FROM posts p
        INNER JOIN users u ON p.userId = u.id
        LEFT JOIN post_likes pl ON p.id = pl.postId AND pl.userId = :currentUserId
        LEFT JOIN saved_posts sp ON p.id = sp.postId AND sp.userId = :currentUserId
        WHERE p.caption LIKE '%' || :query || '%'
        OR u.username LIKE '%' || :query || '%'
        OR p.location LIKE '%' || :query || '%'
        ORDER BY p.likesCount DESC, p.createdAt DESC
        LIMIT :limit
    """)
    suspend fun searchPosts(query: String, currentUserId: String, limit: Int = 50): List<PostWithUser>
    
    @Query("""
        SELECT 
            p.*,
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
            CASE WHEN pl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked,
            CASE WHEN sp.userId IS NOT NULL THEN 1 ELSE 0 END as isSaved
        FROM posts p
        INNER JOIN users u ON p.userId = u.id
        LEFT JOIN post_likes pl ON p.id = pl.postId AND pl.userId = :currentUserId
        LEFT JOIN saved_posts sp ON p.id = sp.postId AND sp.userId = :currentUserId
        ORDER BY p.likesCount DESC, p.createdAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getExplorePosts(
        currentUserId: String, 
        limit: Int = 30, 
        offset: Int = 0
    ): List<PostWithUser>
    
    // Estadísticas
    
    @Query("""
        UPDATE posts SET 
            likesCount = (SELECT COUNT(*) FROM post_likes WHERE postId = :postId),
            commentsCount = (SELECT COUNT(*) FROM comments WHERE postId = :postId)
        WHERE id = :postId
    """)
    suspend fun updatePostCounts(postId: String)
    
    @Query("SELECT COUNT(*) FROM posts WHERE userId = :userId")
    suspend fun getUserPostsCount(userId: String): Int
    
    @Query("""
        SELECT SUM(likesCount) FROM posts WHERE userId = :userId
    """)
    suspend fun getUserTotalLikes(userId: String): Int
    
    // Posts por Ubicación
    
    @Query("""
        SELECT 
            p.*,
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
            CASE WHEN pl.userId IS NOT NULL THEN 1 ELSE 0 END as isLiked,
            CASE WHEN sp.userId IS NOT NULL THEN 1 ELSE 0 END as isSaved
        FROM posts p
        INNER JOIN users u ON p.userId = u.id
        LEFT JOIN post_likes pl ON p.id = pl.postId AND pl.userId = :currentUserId
        LEFT JOIN saved_posts sp ON p.id = sp.postId AND sp.userId = :currentUserId
        WHERE p.location = :location
        ORDER BY p.createdAt DESC
        LIMIT :limit
    """)
    suspend fun getPostsByLocation(
        location: String, 
        currentUserId: String, 
        limit: Int = 20
    ): List<PostWithUser>
}