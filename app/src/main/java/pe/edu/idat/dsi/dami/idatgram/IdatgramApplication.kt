package pe.edu.idat.dsi.dami.idatgram

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pe.edu.idat.dsi.dami.idatgram.data.database.DatabaseSeeder
import pe.edu.idat.dsi.dami.idatgram.data.database.IdatgramDatabase
import javax.inject.Inject

/**
 * Clase Application principal de Idatgram
 * 
 * Configurada con Hilt para inyección de dependencias
 * Inicializa la base de datos con datos de ejemplo en el primer arranque
 */
@HiltAndroidApp
class IdatgramApplication : Application() {
    
    @Inject
    lateinit var database: IdatgramDatabase
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar datos de ejemplo en background
        CoroutineScope(Dispatchers.IO).launch {
            initializeDatabase()
        }
    }
    
    /**
     * Inicializa la base de datos con datos de ejemplo si está vacía
     */
    private suspend fun initializeDatabase() {
        try {
            // Verificar si ya hay usuarios
            val users = database.userDao().getAllUsers().first()
            
            // Si no hay usuarios, insertar datos de ejemplo
            if (users.isEmpty()) {
                DatabaseSeeder.seedDatabase(database)
            }
        } catch (e: Exception) {
            // TODO: Implementar logging
            e.printStackTrace()
        }
    }
}