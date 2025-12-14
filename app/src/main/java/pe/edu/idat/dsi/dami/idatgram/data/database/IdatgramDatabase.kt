package pe.edu.idat.dsi.dami.idatgram.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import pe.edu.idat.dsi.dami.idatgram.data.dao.CommentDao
import pe.edu.idat.dsi.dami.idatgram.data.dao.PostDao
import pe.edu.idat.dsi.dami.idatgram.data.dao.StoryDao
import pe.edu.idat.dsi.dami.idatgram.data.dao.UserDao
import pe.edu.idat.dsi.dami.idatgram.data.entity.*

/**
 * Base de datos de la aplicación Idatgram
 * 
 * Incluye todas las entidades y DAOs necesarios para:
 * - Gestión de usuarios y followers
 * - Posts con likes y guardados
 * - Comentarios con respuestas
 * - Stories con visualizaciones
 * 
 * Configurada con migraciones automáticas y datos de ejemplo para pruebas
 */
@Database(
    entities = [
        User::class,
        UserFollow::class,
        Post::class,
        PostLike::class,
        SavedPost::class,
        Comment::class,
        CommentLike::class,
        Story::class,
        StoryView::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IdatgramDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun storyDao(): StoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: IdatgramDatabase? = null
        
        fun getDatabase(context: Context): IdatgramDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IdatgramDatabase::class.java,
                    "idatgram_database"
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(*getAllMigrations())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Los datos de ejemplo se insertarán via Repository en la primera ejecución
            }
        }

        private fun getAllMigrations(): Array<Migration> {
            return arrayOf(
                // Ejemplo de migración para versión futura
                // MIGRATION_1_2,
                // MIGRATION_2_3,
            )
        }

        // Ejemplo de migración para versiones futuras
        /*
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar nueva columna
                database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT DEFAULT ''")
            }
        }
        */
    }
}

object DatabaseSeeder {
    suspend fun seedDatabase(database: IdatgramDatabase) {

        seedUsers(database.userDao())

        seedPosts(database.postDao())

        seedFollows(database.userDao())
    }
    private suspend fun seedUsers(userDao: UserDao) {
        val sampleUsers = listOf(
            User(
                id = "current_user",
                username = "mi_usuario",
                email = "yo@idatgram.com",
                displayName = "Mi Perfil",
                bio = "Estudiante de desarrollo móvil 📱💻",
                profileImageUrl = "https://picsum.photos/200/200?random=0",
                followersCount = 25,
                followingCount = 89,
                postsCount = 3
            ),
            User(
                id = "user_1",
                username = "ana_garcia",
                email = "ana@example.com",
                displayName = "Ana García",
                bio = "Fotógrafa profesional 📸✨",
                profileImageUrl = "https://picsum.photos/200/200?random=1",
                followersCount = 1250,
                followingCount = 320,
                postsCount = 45,
                isVerified = true
            ),
            User(
                id = "user_2",
                username = "carlos_mendez",
                email = "carlos@example.com",
                displayName = "Carlos Méndez",
                bio = "Chef & Food blogger 🍽️👨‍�",
                profileImageUrl = "https://picsum.photos/200/200?random=2",
                followersCount = 890,
                followingCount = 150,
                postsCount = 67
            ),
            User(
                id = "user_3",
                username = "sofia_lopez",
                email = "sofia@example.com",
                displayName = "Sofía López",
                bio = "Fitness trainer & wellness coach 💪✨",
                profileImageUrl = "https://picsum.photos/200/200?random=3",
                followersCount = 2100,
                followingCount = 89,
                postsCount = 134,
                isVerified = true
            ),
            User(
                id = "user_4",
                username = "diego_ruiz",
                email = "diego@example.com",
                displayName = "Diego Ruiz",
                bio = "Viajero & aventurero 🌍⛰️",
                profileImageUrl = "https://picsum.photos/200/200?random=4",
                followersCount = 567,
                followingCount = 234,
                postsCount = 89
            )
        )
        
        sampleUsers.forEach { user ->
            userDao.insertUser(user)
        }
    }

    private suspend fun seedPosts(postDao: PostDao) {
        val samplePosts = listOf(
            Post(
                id = "post_1",
                userId = "user_1",
                caption = "Atardecer mágico en la playa 🌅 No hay nada como la belleza natural para inspirar nuevas ideas. #fotografía #naturaleza #atardecer",
                imageUrl = "https://picsum.photos/800/800?random=101",
                likesCount = 234,
                commentsCount = 12,
                location = "Playa Miraflores, Lima"
            ),
            Post(
                id = "post_2",
                userId = "user_2",
                caption = "Mi nueva receta de pasta con mariscos 🍝🦐 ¡Perfecta para una cena especial! #chef #cocina #pasta #mariscos",
                imageUrl = "https://picsum.photos/800/800?random=102",
                likesCount = 189,
                commentsCount = 24,
                location = "Mi Cocina"
            ),
            Post(
                id = "post_3",
                userId = "user_3",
                caption = "Entrenamiento matutino terminado ✅ ¡Empezar el día con energía es la clave del éxito! 💪 #fitness #training #motivation",
                imageUrl = "https://picsum.photos/800/800?random=103",
                likesCount = 445,
                commentsCount = 31,
                location = "Gym PowerFit"
            ),
            Post(
                id = "post_4",
                userId = "user_4",
                caption = "Machu Picchu al amanecer 🏔️ Una experiencia que marca la vida. Perú, eres increíble! #travel #machupicchu #peru #aventura",
                imageUrl = "https://picsum.photos/800/800?random=104",
                likesCount = 678,
                commentsCount = 45,
                location = "Machu Picchu, Cusco"
            ),
            Post(
                id = "post_5",
                userId = "user_1",
                caption = "Detalles que importan ✨ La fotografía macro nos revela un mundo invisible a simple vista #macro #photography #art",
                imageUrl = "https://picsum.photos/800/800?random=105",
                likesCount = 156,
                commentsCount = 8,
                location = "Estudio Fotográfico"
            ),
            Post(
                id = "post_6",
                userId = "user_2",
                caption = "Brunch dominical 🥞🥓 Nada mejor que empezar el fin de semana con buena comida y mejor compañía #brunch #sunday #foodie",
                imageUrl = "https://picsum.photos/800/800?random=106",
                likesCount = 267,
                commentsCount = 19,
                location = "Café Central"
            )
        )
        
        samplePosts.forEach { post ->
            postDao.insertPost(post)
        }
    }

    private suspend fun seedFollows(userDao: UserDao) {
        val follows = listOf(
            UserFollow("current_user", "user_1"),
            UserFollow("current_user", "user_2"),
            UserFollow("current_user", "user_3"),
            UserFollow("user_1", "current_user"),
            UserFollow("user_2", "current_user"),
            UserFollow("user_1", "user_3"),
            UserFollow("user_3", "user_1"),
            UserFollow("user_2", "user_4"),
            UserFollow("user_4", "user_2")
        )
        
        follows.forEach { follow ->
            userDao.followUser(follow)
        }
    }
}