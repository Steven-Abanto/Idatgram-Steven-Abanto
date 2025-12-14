package pe.edu.idat.dsi.dami.idatgram.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import pe.edu.idat.dsi.dami.idatgram.R

/**
 * Bottom Navigation Bar para las pestañas principales de Idatgram
 * 
 * Proporciona navegación entre Home, Search, Add Post, Activity y Profile
 * con iconos que cambian según el estado seleccionado
 */
@Composable
fun IdatgramBottomNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Definir las pestañas con sus iconos y rutas
    val bottomNavItems = listOf(
        BottomNavItem(
            route = IdatgramRoutes.HOME,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            label = "Inicio"
        ),
        BottomNavItem(
            route = IdatgramRoutes.SEARCH,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
            label = "Buscar" // stringResource(R.string.search)
        ),
        BottomNavItem(
            route = IdatgramRoutes.ADD_POST,
            selectedIcon = Icons.Filled.Add,
            unselectedIcon = Icons.Outlined.Add,
            label = "Agregar" // stringResource(R.string.add_post)
        ),
        BottomNavItem(
            route = IdatgramRoutes.ACTIVITY,
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder,
            label = "Actividad" // stringResource(R.string.activity)
        ),
        BottomNavItem(
            route = IdatgramRoutes.PROFILE,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            label = "Perfil" // stringResource(R.string.profile)
        )
    )
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { 
                it.route == item.route 
            } == true
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    navigateToBottomNavDestination(navController, item.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

/**
 * Función de navegación optimizada para Bottom Navigation
 * Evita crear múltiples copias del mismo destino en el back stack
 */
private fun navigateToBottomNavDestination(
    navController: NavHostController,
    route: String
) {
    navController.navigate(route) {
        // Pop hasta el destino inicial del grafo para evitar acumulación
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        // Evitar múltiples copias del mismo destino
        launchSingleTop = true
        // Restaurar estado cuando volvemos a una pestaña anterior
        restoreState = true
    }
}

/**
 * Data class para definir items del Bottom Navigation
 */
private data class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

/**
 * Determina si el Bottom Navigation debe mostrarse en la pantalla actual
 */
fun shouldShowBottomNavigation(currentRoute: String?): Boolean {
    return when (currentRoute) {
        IdatgramRoutes.HOME,
        IdatgramRoutes.SEARCH,
        IdatgramRoutes.ADD_POST,
        IdatgramRoutes.ACTIVITY,
        IdatgramRoutes.PROFILE -> true
        else -> false
    }
}

/**
 * Top App Bar personalizada para Idatgram
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdatgramTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        modifier = modifier,
        navigationIcon = {
            navigationIcon?.let { icon ->
                IconButton(onClick = { onNavigationClick?.invoke() }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Navegación"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Scaffold personalizado que combina Top Bar y Bottom Navigation
 */
@Composable
fun IdatgramScaffold(
    currentRoute: String?,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    topBarTitle: String = "",
    showTopBar: Boolean = true,
    topBarNavigationIcon: ImageVector? = null,
    onTopBarNavigationClick: (() -> Unit)? = null,
    topBarActions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (showTopBar) {
                IdatgramTopAppBar(
                    title = topBarTitle,
                    navigationIcon = topBarNavigationIcon,
                    onNavigationClick = onTopBarNavigationClick,
                    actions = topBarActions
                )
            }
        },
        bottomBar = {
            if (shouldShowBottomNavigation(currentRoute)) {
                IdatgramBottomNavigation(navController = navController)
            }
        },
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        content = content
    )
}