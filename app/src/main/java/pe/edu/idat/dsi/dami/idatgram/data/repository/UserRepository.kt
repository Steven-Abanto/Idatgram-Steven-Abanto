package pe.edu.idat.dsi.dami.idatgram.data.repository

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.Flow
import pe.edu.idat.dsi.dami.idatgram.data.dao.UserDao
import pe.edu.idat.dsi.dami.idatgram.data.entity.User
import pe.edu.idat.dsi.dami.idatgram.data.entity.UserFollow
import pe.edu.idat.dsi.dami.idatgram.data.entity.UserProfile
import pe.edu.idat.dsi.dami.idatgram.data.session.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestión de usuarios
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager : SessionManager
) {
    
    // Gestión de Usuario Actual

//    private var currentUserId: String? = "current_user" // Temporal para pruebas
    private var currentUserId: String? = null // Temporal para pruebas

    fun getCurrentUserId(): String? = currentUserId

    val currentUserIdFlow = sessionManager.currentUserIdFlow

    suspend fun loadSession() {
        // Toma el primer valor de DataStore y lo guarda en memoria
        val savedId = sessionManager.currentUserIdFlow.firstOrNull()
        currentUserId = savedId
    }

    suspend fun setCurrentUser(userId: String) {
        currentUserId = userId
        sessionManager.setCurrentUserId(userId)
    }
    
    suspend fun getCurrentUser(): User? {
        return currentUserId?.let { userDao.getUserById(it) }
    }
    
    // CRUD de Usuarios
    
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }
    
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }
    
    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun insertRemoteUsersSafe(users: List<User>) {
        val results = userDao.insertUsersIgnore(users)

        val toUpdate = mutableListOf<User>()
        for (i in results.indices) {
            if (results[i] == -1L) {
                toUpdate.add(users[i])
            }
        }

        if (toUpdate.isNotEmpty()) {
            userDao.updateUsers(toUpdate)
        }
    }
    
    suspend fun createUser(user: User): Result<User> {
        return try {
            // Validaciones
            if (user.username.isBlank()) {
                return Result.failure(Exception("El nombre de usuario es requerido"))
            }
            if (user.email.isBlank()) {
                return Result.failure(Exception("El email es requerido"))
            }
            
            // Verificar que no exista otro usuario con el mismo username o email
            userDao.getUserByUsername(user.username)?.let {
                return Result.failure(Exception("El nombre de usuario ya está en uso"))
            }
            
            userDao.getUserByEmail(user.email)?.let {
                return Result.failure(Exception("El email ya está registrado"))
            }
            
            userDao.insertUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(user: User): Result<User> {
        return try {
            userDao.updateUser(user)
            userDao.updateUserCounts(user.id)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteUser(user: User): Result<Unit> {
        return try {
            userDao.deleteUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Perfil de Usuario
    
    suspend fun getUserProfile(targetUserId: String): UserProfile? {
        val currentId = currentUserId ?: return null
        return userDao.getUserProfile(currentId, targetUserId)
    }
    
    suspend fun updateUserProfile(
        bio: String? = null,
        website: String? = null,
        displayName: String? = null,
        isPrivate: Boolean? = null
    ): Result<User> {
        return try {
            val currentId = currentUserId ?: return Result.failure(Exception("Usuario no autenticado"))
            val currentUser = userDao.getUserById(currentId) 
                ?: return Result.failure(Exception("Usuario no encontrado"))
            
            val updatedUser = currentUser.copy(
                bio = bio ?: currentUser.bio,
                website = website ?: currentUser.website,
                displayName = displayName ?: currentUser.displayName,
                isPrivate = isPrivate ?: currentUser.isPrivate,
                updatedAt = System.currentTimeMillis()
            )
            
            userDao.updateUser(updatedUser)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Relaciones de Seguimiento
    
    suspend fun followUser(targetUserId: String): Result<Boolean> {
        return try {
            val currentId = currentUserId ?: return Result.failure(Exception("Usuario no autenticado"))
            if (currentId == targetUserId) {
                return Result.failure(Exception("No puedes seguirte a ti mismo"))
            }
            
            val targetUser = userDao.getUserById(targetUserId)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            
            val userFollow = UserFollow(
                followerId = currentId,
                followingId = targetUserId
            )
            
            userDao.followUser(userFollow)
            userDao.updateUserCounts(currentId)
            userDao.updateUserCounts(targetUserId)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun unfollowUser(targetUserId: String): Result<Boolean> {
        return try {
            val currentId = currentUserId ?: return Result.failure(Exception("Usuario no autenticado"))
            
            userDao.unfollowUser(currentId, targetUserId)
            userDao.updateUserCounts(currentId)
            userDao.updateUserCounts(targetUserId)
            
            Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun isFollowing(targetUserId: String): Boolean {
        val currentId = currentUserId ?: return false
        return userDao.isFollowing(currentId, targetUserId)
    }
    
    fun getUserFollowers(userId: String): Flow<List<User>> {
        return userDao.getUserFollowers(userId)
    }
    
    fun getUserFollowing(userId: String): Flow<List<User>> {
        return userDao.getUserFollowing(userId)
    }
    
    suspend fun getFollowersCount(userId: String): Int {
        return userDao.getFollowersCount(userId)
    }
    
    suspend fun getFollowingCount(userId: String): Int {
        return userDao.getFollowingCount(userId)
    }
    
    // Búsqueda y Descubrimiento
    
    suspend fun searchUsers(query: String, limit: Int = 20): List<User> {
        if (query.isBlank()) return emptyList()
        return userDao.searchUsers(query, limit)
    }
    
    suspend fun getPopularUsers(limit: Int = 10): List<User> {
        return userDao.getPopularUsers(limit)
    }
    
    suspend fun getSuggestedUsers(limit: Int = 10): List<User> {
        val currentId = currentUserId ?: return emptyList()
        return userDao.getSuggestedUsers(currentId, limit)
    }
    
    // Autenticación Simulada
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // TODO: Conectar con API
            val user = userDao.getUserByEmail(email)
                ?: return Result.failure(Exception("Email o contraseña incorrectos"))
            
//            currentUserId = user.id
            setCurrentUser(user.id)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(
        username: String,
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            val userId = "user_${System.currentTimeMillis()}"
            val newUser = User(
                id = userId,
                username = username.trim().lowercase(),
                email = email.trim().lowercase(),
                displayName = displayName.trim(),
                createdAt = System.currentTimeMillis()
            )
            
            createUser(newUser).fold(
                onSuccess = { user ->
//                    currentUserId = user.id
                    setCurrentUser(user.id)
                    Result.success(user)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


//  fun logout() {
    suspend fun logout() {
        currentUserId = null
        sessionManager.setCurrentUserId(null)
    }
    
    fun isLoggedIn(): Boolean = currentUserId != null
    
    // Estadísticas
    
    suspend fun updateUserStatistics(userId: String) {
        userDao.updateUserCounts(userId)
    }
    
    // Cache y Sincronización

    suspend fun insertRemoteUsers(users: List<User>) {
        userDao.insertUsers(users)
    }

    suspend fun refreshUser(userId: String): Result<User> {
        return try {
            // En el futuro, aquí se haría una llamada a la API
            // y se actualizaría la cache local
            val user = userDao.getUserById(userId)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncFollowRelations(): Result<Unit> {
        return try {
            // Sincronización futura con backend
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}