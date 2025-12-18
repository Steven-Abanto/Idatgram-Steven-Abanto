package pe.edu.idat.dsi.dami.idatgram.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pe.edu.idat.dsi.dami.idatgram.data.dao.StoryDao
import pe.edu.idat.dsi.dami.idatgram.data.entity.Story
import pe.edu.idat.dsi.dami.idatgram.data.entity.StoryView
import pe.edu.idat.dsi.dami.idatgram.data.entity.StoryWithUser
import pe.edu.idat.dsi.dami.idatgram.data.remote.IdatgramApiService
import pe.edu.idat.dsi.dami.idatgram.data.remote.dto.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepository @Inject constructor(
    private val storyDao: StoryDao,
    private val userRepository: UserRepository,
    private val apiService: IdatgramApiService
) {

    fun getFeedStories(): Flow<List<StoryWithUser>> {
        val currentId = userRepository.getCurrentUserId()
            ?: return flowOf(emptyList())
        // viewerId = currentUserId
        return storyDao.getFeedStories(currentId, currentId)
    }

    suspend fun refreshStories(): Result<Unit> {
        return try {
            val currentId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // 1) Trae remoto
            val remoteStories = apiService.getStories()
            val storyEntities = remoteStories.map { it.toEntity() }

            // 2) Limpia expiradas (local)
            storyDao.deleteExpiredStoryViews()
            storyDao.deleteExpiredStories()

            // 3) Guarda en Room (IGNORE + update batch)
            val insertResults = storyDao.insertStoriesIgnore(storyEntities)

            val toUpdate = mutableListOf<Story>()
            for (i in insertResults.indices) {
                if (insertResults[i] == -1L) {
                    toUpdate.add(storyEntities[i])
                }
            }
            if (toUpdate.isNotEmpty()) {
                storyDao.updateStories(toUpdate)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markViewed(storyId: String): Result<Unit> {
        return try {
            val viewerId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            storyDao.viewStory(StoryView(storyId = storyId, viewerId = viewerId))
            storyDao.updateStoryViewsCount(storyId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserStories(userId: String): Result<List<StoryWithUser>> {
        return try {
            val viewerId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val list = storyDao.getUserStoriesWithDetails(userId, viewerId)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasAnyLocalStories(): Boolean {
        // “¿existen o no?” a nivel local (lo que verá la UI)
        val currentId = userRepository.getCurrentUserId() ?: return false
        // si quieres: usa getAllActiveStoriesWithUser(currentId).first().isNotEmpty()
        // o cuenta por usuarios que sigues (más exacto)
        return true
    }
}

