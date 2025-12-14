package pe.edu.idat.dsi.dami.idatgram.navigation

/**
 * Definición de rutas de navegación para la aplicación Idatgram
 * 
 * Incluye rutas para autenticación, pantallas principales y navegación detallada
 * con parámetros para IDs de usuarios, posts, etc.
 */
object IdatgramRoutes {
    
    // === Autenticación ===
    const val LOGIN = "login"
    const val REGISTER = "register"
    
    // === Navegación Principal ===
    const val HOME = "home"
    const val SEARCH = "search"
    const val ADD_POST = "add_post"
    const val ACTIVITY = "activity"
    const val PROFILE = "profile"
    
    // === Pantallas de Detalle ===
    const val USER_PROFILE = "user_profile/{userId}"
    const val POST_DETAIL = "post_detail/{postId}"
    const val COMMENTS = "comments/{postId}"
    const val STORY_VIEWER = "story_viewer/{userId}"
    const val FOLLOWERS = "followers/{userId}"
    const val FOLLOWING = "following/{userId}"
    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"
    
    // === Navegación con Argumentos ===
    fun userProfile(userId: String) = "user_profile/$userId"
    fun postDetail(postId: String) = "post_detail/$postId"
    fun comments(postId: String) = "comments/$postId"
    fun storyViewer(userId: String) = "story_viewer/$userId"
    fun followers(userId: String) = "followers/$userId"
    fun following(userId: String) = "following/$userId"
}

/**
 * Argumentos de navegación para rutas con parámetros
 */
object IdatgramArgs {
    const val USER_ID = "userId"
    const val POST_ID = "postId"
}

/**
 * Navegación de las pestañas principales (Bottom Navigation)
 */
enum class BottomNavTab(
    val route: String,
    val titleResId: Int,
    val iconResId: Int? = null
) {
    HOME(
        route = IdatgramRoutes.HOME,
        titleResId = android.R.string.untitled, // Reemplazar con R.string.home
        iconResId = null // Usar Icons.Default.Home
    ),
    SEARCH(
        route = IdatgramRoutes.SEARCH,
        titleResId = android.R.string.untitled, // Reemplazar con R.string.search
        iconResId = null // Usar Icons.Default.Search
    ),
    ADD_POST(
        route = IdatgramRoutes.ADD_POST,
        titleResId = android.R.string.untitled, // Reemplazar con R.string.add_post
        iconResId = null // Usar Icons.Default.Add
    ),
    ACTIVITY(
        route = IdatgramRoutes.ACTIVITY,
        titleResId = android.R.string.untitled, // Reemplazar con R.string.activity
        iconResId = null // Usar Icons.Default.FavoriteBorder
    ),
    PROFILE(
        route = IdatgramRoutes.PROFILE,
        titleResId = android.R.string.untitled, // Reemplazar con R.string.profile
        iconResId = null // Usar Icons.Default.Person
    ),
    LOGIN(
        route = IdatgramRoutes.LOGIN,
        titleResId = android.R.string.untitled, // Reemplazar con R.string.login
        iconResId = null // Usar Icons.Default.FavoriteBorder
    ),
    REGISTER(
        route = IdatgramRoutes.REGISTER,
        titleResId = android.R.string.untitled, // Reemplazar con R.string.register
        iconResId = null // Usar Icons.Default.Person
    )
}

/**
 * Tipos de navegación según el estado del usuario
 */
sealed class NavigationState {
    object Unauthenticated : NavigationState()
    object Authenticated : NavigationState()
    object Loading : NavigationState()
}