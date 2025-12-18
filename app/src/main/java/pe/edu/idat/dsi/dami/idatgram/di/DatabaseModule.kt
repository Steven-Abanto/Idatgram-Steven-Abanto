package pe.edu.idat.dsi.dami.idatgram.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pe.edu.idat.dsi.dami.idatgram.data.dao.CommentDao
import pe.edu.idat.dsi.dami.idatgram.data.dao.PostDao
import pe.edu.idat.dsi.dami.idatgram.data.dao.StoryDao
import pe.edu.idat.dsi.dami.idatgram.data.dao.UserDao
import pe.edu.idat.dsi.dami.idatgram.data.database.IdatgramDatabase
import pe.edu.idat.dsi.dami.idatgram.data.repository.LocalDatabaseRepository
import pe.edu.idat.dsi.dami.idatgram.data.session.SessionManager
import javax.inject.Singleton

/**
 * Módulo Hilt para la configuración de la base de datos
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideIdatgramDatabase(@ApplicationContext context: Context): IdatgramDatabase {
        return IdatgramDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideUserDao(database: IdatgramDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun providePostDao(database: IdatgramDatabase): PostDao {
        return database.postDao()
    }
    
    @Provides
    fun provideCommentDao(database: IdatgramDatabase): CommentDao {
        return database.commentDao()
    }
    
    @Provides
    fun provideStoryDao(database: IdatgramDatabase): StoryDao {
        return database.storyDao()
    }

    //SessionManager para manejar DataStore
    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager = SessionManager(context)

    //LocalDatabaseRepository para operaciones de base de datos locales
    @Provides
    @Singleton
    fun provideLocalDatabaseRepository(
        userDao: UserDao,
        postDao: PostDao
    ): LocalDatabaseRepository = LocalDatabaseRepository(userDao, postDao)
}