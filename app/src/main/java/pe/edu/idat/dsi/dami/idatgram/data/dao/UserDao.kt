package pe.edu.idat.dsi.dami.idatgram.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pe.edu.idat.dsi.dami.idatgram.data.entity.User
import pe.edu.idat.dsi.dami.idatgram.data.entity.UserFollow
import pe.edu.idat.dsi.dami.idatgram.data.entity.UserProfile

/**
 * DAO para operaciones relacionadas con usuarios
 * Incluye CRUD básico y consultas complejas para relaciones
 */
@Dao
interface UserDao {
    
    // CRUD Básico
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    // Inserta sin reemplazar (si existe en local, no hace nada)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsersIgnore(users: List<User>): List<Long>

    @Update
    suspend fun updateUser(user: User)

    //Update en batch
    @Update
    suspend fun updateUsers(users: List<User>)

    @Delete
    suspend fun deleteUser(user: User)
    
    // Búsqueda de Usuarios
    
    @Query("""
        SELECT * FROM users 
        WHERE username LIKE '%' || :query || '%' 
        OR displayName LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN username LIKE :query || '%' THEN 1 ELSE 2 END,
            followersCount DESC
        LIMIT :limit
    """)
    suspend fun searchUsers(query: String, limit: Int = 20): List<User>
    
    @Query("""
        SELECT * FROM users 
        ORDER BY followersCount DESC 
        LIMIT :limit
    """)
    suspend fun getPopularUsers(limit: Int = 10): List<User>
    
    // Relaciones de Seguimiento
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followUser(userFollow: UserFollow)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun followUsersIgnore(follows: List<UserFollow>): List<Long>
    
    @Query("DELETE FROM user_follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun unfollowUser(followerId: String, followingId: String)
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM user_follows 
            WHERE followerId = :followerId AND followingId = :followingId
        )
    """)
    suspend fun isFollowing(followerId: String, followingId: String): Boolean
    
    @Query("""
        SELECT u.* FROM users u
        INNER JOIN user_follows uf ON u.id = uf.followingId
        WHERE uf.followerId = :userId
        ORDER BY uf.createdAt DESC
    """)
    fun getUserFollowing(userId: String): Flow<List<User>>
    
    @Query("""
        SELECT u.* FROM users u
        INNER JOIN user_follows uf ON u.id = uf.followerId
        WHERE uf.followingId = :userId
        ORDER BY uf.createdAt DESC
    """)
    fun getUserFollowers(userId: String): Flow<List<User>>
    
    @Query("SELECT COUNT(*) FROM user_follows WHERE followerId = :userId")
    suspend fun getFollowingCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM user_follows WHERE followingId = :userId")
    suspend fun getFollowersCount(userId: String): Int
    
    // Perfil Completo
    
    @Query("""
        SELECT 
            u.*,
            CASE WHEN uf1.followerId IS NOT NULL THEN 1 ELSE 0 END as isFollowing,
            CASE WHEN uf2.followingId IS NOT NULL THEN 1 ELSE 0 END as isFollowedBy,
            (
                SELECT COUNT(*) FROM user_follows uf3
                INNER JOIN user_follows uf4 ON uf3.followingId = uf4.followingId
                WHERE uf3.followerId = :currentUserId AND uf4.followerId = :targetUserId
            ) as mutualFollowersCount
        FROM users u
        LEFT JOIN user_follows uf1 ON uf1.followerId = :currentUserId AND uf1.followingId = u.id
        LEFT JOIN user_follows uf2 ON uf2.followerId = u.id AND uf2.followingId = :currentUserId
        WHERE u.id = :targetUserId
    """)
    suspend fun getUserProfile(currentUserId: String, targetUserId: String): UserProfile?
    
    // Sugerencias
    
    @Query("""
        SELECT DISTINCT u.* FROM users u
        INNER JOIN user_follows uf1 ON u.id = uf1.followingId
        INNER JOIN user_follows uf2 ON uf1.followerId = uf2.followingId
        WHERE uf2.followerId = :userId
        AND u.id != :userId
        AND u.id NOT IN (
            SELECT followingId FROM user_follows WHERE followerId = :userId
        )
        ORDER BY u.followersCount DESC
        LIMIT :limit
    """)
    suspend fun getSuggestedUsers(userId: String, limit: Int = 10): List<User>
    
    // Estadísticas
    
    @Query("""
        UPDATE users SET 
            followersCount = (SELECT COUNT(*) FROM user_follows WHERE followingId = :userId),
            followingCount = (SELECT COUNT(*) FROM user_follows WHERE followerId = :userId),
            postsCount = (SELECT COUNT(*) FROM posts WHERE userId = :userId)
        WHERE id = :userId
    """)
    suspend fun updateUserCounts(userId: String)
}