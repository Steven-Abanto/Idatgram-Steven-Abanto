package pe.edu.idat.dsi.dami.idatgram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pe.edu.idat.dsi.dami.idatgram.navigation.IdatgramNavGraph
import pe.edu.idat.dsi.dami.idatgram.navigation.IdatgramRoutes
import pe.edu.idat.dsi.dami.idatgram.navigation.IdatgramScaffold
import pe.edu.idat.dsi.dami.idatgram.ui.theme.IdatgramTheme
import pe.edu.idat.dsi.dami.idatgram.ui.viewmodel.SessionViewModel

/**
 * Activity principal de Idatgram
 * 
 * Configurada con Hilt para inyección de dependencias
 * Maneja la navegación principal y el tema de la aplicación
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            IdatgramTheme {
                IdatgramApp()
            }
        }
    }
}

/**
 * Composable principal que configura la navegación y el scaffold
 */
@Composable
fun IdatgramApp() {
//Se añaden validaciones de sesión
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()

// Se actualiza por ruta dinámica
    val startDestination = if (isLoggedIn) IdatgramRoutes.HOME else IdatgramRoutes.LOGIN

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
//    // Determinar el título de la barra superior según la ruta actual
//    val topBarTitle = when (currentRoute) {
//        IdatgramRoutes.HOME -> "Idatgram"
//        IdatgramRoutes.SEARCH -> "Buscar"
//        IdatgramRoutes.ADD_POST -> "Nueva Publicación"
//        IdatgramRoutes.ACTIVITY -> "Actividad"
//        IdatgramRoutes.PROFILE -> "Perfil"
//        IdatgramRoutes.LOGIN -> "Iniciar Sesión"
//        IdatgramRoutes.REGISTER -> "Registrarse"
//        IdatgramRoutes.COMMENTS -> "Comentarios"
//        else -> "Idatgram"
//    }
//
//    // Determinar si mostrar la barra superior
//    val showTopBar = when (currentRoute) {
//        IdatgramRoutes.LOGIN,
//        IdatgramRoutes.REGISTER -> false
//        else -> true
//    }
    
    IdatgramScaffold(
        currentRoute = currentRoute,
        navController = navController,
        modifier = Modifier.fillMaxSize(),
//        topBarTitle = topBarTitle,
//        showTopBar = showTopBar
    ) { paddingValues ->
        IdatgramNavGraph(
            navController = navController,
            // startDestination = IdatgramRoutes.HOME
            startDestination = startDestination // Se actualiza por ruta dinámica
        )
    }
}