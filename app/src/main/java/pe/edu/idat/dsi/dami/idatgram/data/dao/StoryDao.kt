package pe.edu.idat.dsi.dami.idatgram.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pe.edu.idat.dsi.dami.idatgram.data.entity.Story
import pe.edu.idat.dsi.dami.idatgram.data.entity.StoryView
import pe.edu.idat.dsi.dami.idatgram.data.entity.StoryWithUser
import pe.edu.idat.dsi.dami.idatgram.data.entity.UserStories

/**
 * DAO para operaciones relacionadas con stories
 * Incluye CRUD, visualizaciones y agrupamiento por usuario
 */
@Dao
interface StoryDao {
    
    // CRUD Básico
    
    @Query("SELECT * FROM stories WHERE id = :storyId")
    suspend fun getStoryById(storyId: String): Story?
    
    @Query("SELECT * FROM stories WHERE userId = :userId AND expiresAt > :currentTime ORDER BY createdAt DESC")
    suspend fun getUserActiveStories(userId: String, currentTime: Long = System.currentTimeMillis()): List<Story>
    
    @Query("SELECT * FROM stories WHERE expiresAt > :currentTime ORDER BY createdAt DESC")
    fun getAllActiveStories(currentTime: Long = System.currentTimeMillis()): Flow<List<Story>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: Story)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<Story>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStoriesIgnore(stories: List<Story>): List<Long>
    
    @Update
    suspend fun updateStory(story: Story)

    @Update
    suspend fun updateStories(stories: List<Story>)
    
    @Delete
    suspend fun deleteStory(story: Story)
    
    @Query("DELETE FROM stories WHERE id = :storyId")
    suspend fun deleteStoryById(storyId: String)
    
    @Query("DELETE FROM stories WHERE expiresAt <= :currentTime")
    suspend fun deleteExpiredStories(currentTime: Long = System.currentTimeMillis())
    
    // Stories con Usuario
    
    @Query("""
        SELECT 
            s.*,
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
            CASE WHEN sv.viewerId IS NOT NULL THEN 1 ELSE 0 END as isViewed
        FROM stories s
        INNER JOIN users u ON s.userId = u.id
        LEFT JOIN story_views sv ON s.id = sv.storyId AND sv.viewerId = :viewerId
        WHERE s.expiresAt > :currentTime
        ORDER BY s.createdAt DESC
    """)
    fun getAllActiveStoriesWithUser(
        viewerId: String, 
        currentTime: Long = System.currentTimeMillis()
    ): Flow<List<StoryWithUser>>
    
    @Query("""
        SELECT 
            s.*,
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
            CASE WHEN sv.viewerId IS NOT NULL THEN 1 ELSE 0 END as isViewed
        FROM stories s
        INNER JOIN users u ON s.userId = u.id
        LEFT JOIN story_views sv ON s.id = sv.storyId AND sv.viewerId = :viewerId
        WHERE s.userId = :userId AND s.expiresAt > :currentTime
        ORDER BY s.createdAt ASC
    """)
    suspend fun getUserStoriesWithDetails(
        userId: String, 
        viewerId: String, 
        currentTime: Long = System.currentTimeMillis()
    ): List<StoryWithUser>
    
    // Stories Agrupadas por Usuario
    
    @Query("""
        SELECT DISTINCT
            u.*,
            (
                SELECT COUNT(*) > 0 FROM stories s2
                LEFT JOIN story_views sv2 ON s2.id = sv2.storyId AND sv2.viewerId = :viewerId
                WHERE s2.userId = u.id 
                AND s2.expiresAt > :currentTime 
                AND sv2.viewerId IS NULL
            ) as hasUnviewedStories
        FROM users u
        INNER JOIN stories s ON u.id = s.userId
        INNER JOIN user_follows uf ON u.id = uf.followingId
        WHERE uf.followerId = :currentUserId
        AND s.expiresAt > :currentTime
        ORDER BY hasUnviewedStories DESC, u.username ASC
    """)
    suspend fun getFollowingUsersWithStories(
        currentUserId: String,
        viewerId: String,
        currentTime: Long = System.currentTimeMillis()
    ): List<UserStories>
    
    @Query("""
        SELECT 
            u.*,
            0 as hasUnviewedStories
        FROM users u
        WHERE u.id = :userId
    """)
    suspend fun getUserForStories(userId: String): UserStories?
    
    // Visualizaciones de Stories
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun viewStory(storyView: StoryView)
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM story_views 
            WHERE storyId = :storyId AND viewerId = :viewerId
        )
    """)
    suspend fun hasViewedStory(storyId: String, viewerId: String): Boolean
    
    @Query("SELECT COUNT(*) FROM story_views WHERE storyId = :storyId")
    suspend fun getStoryViewsCount(storyId: String): Int
    
    @Query("""
        SELECT u.* FROM users u
        INNER JOIN story_views sv ON u.id = sv.viewerId
        WHERE sv.storyId = :storyId
        ORDER BY sv.viewedAt DESC
        LIMIT :limit
    """)
    suspend fun getStoryViewers(storyId: String, limit: Int = 100): List<pe.edu.idat.dsi.dami.idatgram.data.entity.User>
    
    @Query("""
        SELECT COUNT(DISTINCT sv.viewerId) FROM story_views sv
        INNER JOIN stories s ON sv.storyId = s.id
        WHERE s.userId = :userId AND s.expiresAt > :currentTime
    """)
    suspend fun getUserStoriesViewsCount(
        userId: String, 
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    // Stories del Feed
    
    @Query("""
        SELECT 
            s.*,
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
            CASE WHEN sv.viewerId IS NOT NULL THEN 1 ELSE 0 END as isViewed
        FROM stories s
        INNER JOIN users u ON s.userId = u.id
        LEFT JOIN story_views sv ON s.id = sv.storyId AND sv.viewerId = :viewerId
        WHERE s.userId IN (
            SELECT followingId FROM user_follows WHERE followerId = :currentUserId
            UNION SELECT :currentUserId
        )
        AND s.expiresAt > :currentTime
        ORDER BY 
            CASE WHEN s.userId = :currentUserId THEN 0 ELSE 1 END,
            sv.viewerId IS NULL DESC,
            s.createdAt DESC
    """)
    fun getFeedStories(
        currentUserId: String,
        viewerId: String,
        currentTime: Long = System.currentTimeMillis()
    ): Flow<List<StoryWithUser>>
    
    // Estadísticas
    
    @Query("""
        UPDATE stories SET 
            viewsCount = (SELECT COUNT(*) FROM story_views WHERE storyId = :storyId)
        WHERE id = :storyId
    """)
    suspend fun updateStoryViewsCount(storyId: String)
    
    @Query("SELECT COUNT(*) FROM stories WHERE userId = :userId AND expiresAt > :currentTime")
    suspend fun getUserActiveStoriesCount(
        userId: String, 
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    // Limpieza Automática
    
    @Query("DELETE FROM story_views WHERE storyId IN (SELECT id FROM stories WHERE expiresAt <= :currentTime)")
    suspend fun deleteExpiredStoryViews(currentTime: Long = System.currentTimeMillis())
    
    // Stories Destacadas (Highlights) - Para futuras funcionalidades
    
    @Query("""
        SELECT s.* FROM stories s
        WHERE s.userId = :userId
        AND s.id IN (:highlightedStoryIds)
        ORDER BY s.createdAt DESC
    """)
    suspend fun getHighlightedStories(userId: String, highlightedStoryIds: List<String>): List<Story>
}