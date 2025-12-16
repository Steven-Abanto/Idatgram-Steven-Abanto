package pe.edu.idat.dsi.dami.idatgram.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pe.edu.idat.dsi.dami.idatgram.ui.screens.home.HomeScreen
import pe.edu.idat.dsi.dami.idatgram.ui.screens.addpost.AddPostScreen
import pe.edu.idat.dsi.dami.idatgram.ui.screens.auth.LoginScreen
import pe.edu.idat.dsi.dami.idatgram.ui.screens.comments.CommentsScreen
import pe.edu.idat.dsi.dami.idatgram.ui.screens.profile.ProfileScreen
import pe.edu.idat.dsi.dami.idatgram.ui.viewmodel.LoginViewModel
import pe.edu.idat.dsi.dami.idatgram.ui.viewmodel.ProfileViewModel
import pe.edu.idat.dsi.dami.idatgram.ui.viewmodel.SessionViewModel

/**
 * Navigation Graph principal de Idatgram
 * 
 * Define todas las rutas y transiciones entre pantallas
 * Integra autenticación, pantallas principales y navegación detallada
 */
@Composable
fun IdatgramNavGraph(
    navController: NavHostController,
    startDestination: String = IdatgramRoutes.HOME
//    startDestination: String = IdatgramRoutes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        
        // Pantallas de Autenticación

        composable(route = IdatgramRoutes.LOGIN) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val loginState by loginViewModel.uiState.collectAsState()

            // PlaceholderScreen(
            //     title = "Iniciar Sesión",
            //     onNavigate = { navController.navigate(IdatgramRoutes.HOME) }
            // )

            LoginScreen(
                onLoginClick = { email, password ->
                    loginViewModel.login(email, password)
                },
                onRegisterClick = {
                    navController.navigate(IdatgramRoutes.REGISTER)
                },
                onForgotPasswordClick = {
                    // TODO: implementar luego si lo necesitas
                },

                isLoading = loginState.isLoading,
                errorMessage = loginState.errorMessage,
                onErrorShown = {
                    loginViewModel.clearError()
                }
            )

            // Si login fue exitoso, navega y limpia el backstack del login
            LaunchedEffect(loginState.loginSuccess) {
                if (loginState.loginSuccess) {
                    loginViewModel.consumeLoginSuccess()
                    navController.navigate(IdatgramRoutes.HOME) {
                        popUpTo(IdatgramRoutes.LOGIN) { inclusive = true }
                    }
                }
            }
        }
        
        composable(route = IdatgramRoutes.REGISTER) {
            // TODO: Implementar RegisterScreen
            PlaceholderScreen(
                title = "Registrarse",
                onNavigate = { navController.navigate(IdatgramRoutes.LOGIN) }
            )
        }
        
        // Pantallas Principales
        
        composable(route = IdatgramRoutes.HOME) {
            HomeScreen(
                onUserProfileClick = { userId ->
//                    navController.navigate("${IdatgramRoutes.USER_PROFILE}/$userId")
                    navController.navigate(IdatgramRoutes.userProfile(userId))
                },
                onStoryClick = { userId ->
//                    navController.navigate("${IdatgramRoutes.STORY_VIEWER}/$userId")
                    navController.navigate(IdatgramRoutes.storyViewer(userId))
                },
                onCameraClick = {
                    navController.navigate(IdatgramRoutes.ADD_POST)
                },
                onCommentsClick = { postId ->
                    navController.navigate(IdatgramRoutes.comments(postId))
                },
                onDirectMessagesClick = {
                    // TODO: Implementar mensajes directos
                }
            )
        }
        
        composable(route = IdatgramRoutes.SEARCH) {
            // TODO: Implementar SearchScreen
            PlaceholderScreen(
                title = "Buscar",
                subtitle = "Explorar usuarios y contenido"
            )
        }
        
        composable(route = IdatgramRoutes.ADD_POST) {
            AddPostScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPostCreated = {
                    navController.navigate(IdatgramRoutes.HOME) {
                        popUpTo(IdatgramRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }
        
        composable(route = IdatgramRoutes.ACTIVITY) {
            // TODO: Implementar ActivityScreen
            PlaceholderScreen(
                title = "Actividad",
                subtitle = "Notificaciones e interacciones"
            )
        }

        composable(route = IdatgramRoutes.PROFILE) {
            // TODO: Implementar ProfileScreen
//            PlaceholderScreen(
//                title = "Mi Perfil",
//                subtitle = "Información personal y publicaciones"
//            )

            val sessionVm: SessionViewModel = hiltViewModel()
            val myId by sessionVm.currentUserId.collectAsState()
            val loaded by sessionVm.sessionLoaded.collectAsState()

            if (!loaded) {
                PlaceholderScreen(title = "Cargando sesión...", subtitle = null)
                return@composable
            }

            // Si no hay sesión, manda a login
            if (myId.isNullOrBlank()) {
                LaunchedEffect(Unit) {
                    navController.navigate(IdatgramRoutes.LOGIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                }
                return@composable
            }

            val profileVm: ProfileViewModel = hiltViewModel()
            val state by profileVm.uiState.collectAsState()

            LaunchedEffect(myId) { profileVm.load(myId!!) }

            val user = state.user ?: run {
                PlaceholderScreen(title = "Cargando perfil...", subtitle = null)
                return@composable
            }

            ProfileScreen(
                user = user,
                userPosts = state.userPosts,
                isOwnProfile = true,
                isFollowing = false,
                onEditProfileClick = { navController.navigate(IdatgramRoutes.EDIT_PROFILE) },
                onPostClick = { postId -> navController.navigate(IdatgramRoutes.postDetail(postId)) },
                onOptionsClick = { navController.navigate(IdatgramRoutes.SETTINGS) }
            )
        }
        
        // Pantallas de Detalle
        
        composable(
            route = IdatgramRoutes.USER_PROFILE,
            arguments = listOf(
                navArgument(IdatgramArgs.USER_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(IdatgramArgs.USER_ID) ?: return@composable
            // TODO: Implementar UserProfileScreen
//            PlaceholderScreen(
//                title = "Perfil de Usuario",
//                subtitle = "ID: $userId",
//                onNavigate = { navController.popBackStack() }
//            )

            val profileVm: ProfileViewModel = hiltViewModel()
            val state by profileVm.uiState.collectAsState()

            LaunchedEffect(userId) { profileVm.load(userId) }

            val user = state.user ?: run {
                PlaceholderScreen(title = "Cargando perfil...", subtitle = null)
                return@composable
            }

            ProfileScreen(
                user = user,
                userPosts = state.userPosts,
                isOwnProfile = state.isOwnProfile,
                isFollowing = state.isFollowing,
                onFollowClick = { profileVm.onFollowClick(userId) },
                onPostClick = { postId -> navController.navigate(IdatgramRoutes.postDetail(postId)) },
                onBackClick = { navController.popBackStack() },
                onOptionsClick = { navController.navigate(IdatgramRoutes.SETTINGS) }
            )
        }
        
        composable(
            route = IdatgramRoutes.POST_DETAIL,
            arguments = listOf(
                navArgument(IdatgramArgs.POST_ID) { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString(IdatgramArgs.POST_ID) ?: ""
            // TODO: Implementar PostDetailScreen
            PlaceholderScreen(
                title = "Detalle del Post",
                subtitle = "ID: $postId",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        composable(
            route = IdatgramRoutes.COMMENTS,
            arguments = listOf(
                navArgument(IdatgramArgs.POST_ID) { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString(IdatgramArgs.POST_ID) ?: ""
            // TODO: Implementar CommentsScreen
//            PlaceholderScreen(
//                title = "Comentarios",
//                subtitle = "Post ID: $postId",
//                onNavigate = { navController.popBackStack() }
//            )
            CommentsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = IdatgramRoutes.STORY_VIEWER,
            arguments = listOf(
                navArgument(IdatgramArgs.USER_ID) { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(IdatgramArgs.USER_ID) ?: ""
            // TODO: Implementar StoryViewerScreen
            PlaceholderScreen(
                title = "Historia",
                subtitle = "Usuario: $userId",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        composable(
            route = IdatgramRoutes.FOLLOWERS,
            arguments = listOf(
                navArgument(IdatgramArgs.USER_ID) { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(IdatgramArgs.USER_ID) ?: ""
            // TODO: Implementar FollowersScreen
            PlaceholderScreen(
                title = "Seguidores",
                subtitle = "Usuario: $userId",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        composable(
            route = IdatgramRoutes.FOLLOWING,
            arguments = listOf(
                navArgument(IdatgramArgs.USER_ID) { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(IdatgramArgs.USER_ID) ?: ""
            // TODO: Implementar FollowingScreen
            PlaceholderScreen(
                title = "Siguiendo",
                subtitle = "Usuario: $userId",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        composable(route = IdatgramRoutes.EDIT_PROFILE) {
            // TODO: Implementar EditProfileScreen
            PlaceholderScreen(
                title = "Editar Perfil",
                subtitle = "Modificar información personal",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        composable(route = IdatgramRoutes.SETTINGS) {
            // TODO: Implementar SettingsScreen
            PlaceholderScreen(
                title = "Configuración",
                subtitle = "Preferencias de la aplicación",
                onNavigate = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Pantalla temporal para mostrar mientras se implementan las pantallas reales
 * Útil para desarrollo y testing de navegación
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String? = null,
    onNavigate: (() -> Unit)? = null
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        androidx.compose.material3.Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
        )
        
        subtitle?.let {
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.height(8.dp)
            )
            androidx.compose.material3.Text(
                text = it,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        onNavigate?.let { navigate ->
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.height(24.dp)
            )
            androidx.compose.material3.Button(
                onClick = navigate
            ) {
                androidx.compose.material3.Text("Continuar")
            }
        }
    }
}