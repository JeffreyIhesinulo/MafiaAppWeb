import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<User?>(null) }
    val repository = remember { ProfileRepository() }
    val currentRoute = navController
        .currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(currentRoute) {
        if (currentRoute != "login" && currentRoute != "register") {
            currentUser = repository.getCurrentUser()
        }
    }
    Scaffold(
        containerColor = NavBackground,
        bottomBar = {
            val items = listOf(
	    "G" to "games",
	    "P" to "players",
	    "+" to "create",
	    "!" to "activity",
	    "S" to "profile"
		)
            val route = navController
                .currentBackStackEntryAsState().value?.destination?.route
            if (route != "login" && route != "register") {
                NavigationBar(containerColor = CardBg) {
                    items.forEach { (icon, r) ->
                        if (r == "create" && (currentUser == null || currentUser?.isAdmin != true)) return@forEach
                        NavigationBarItem(
                            selected = route == r,
                            onClick = { navController.navigate(r) },
                            icon = { Text(icon, fontSize = 20.sp) },
                            label = { Text(r.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginClick = {
                        navController.navigate("players") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            composable("players") { PlayersScreen(navController = navController) }
            composable("profile") { ProfileScreen(navController = navController) }
            composable("create") { CreateGamesScreen() }
            composable("game/{gameId}") { backStackEntry ->
                val gameId = backStackEntry.arguments?.read { getStringOrNull("gameId") } ?: ""
                GameDetailScreen(gameId = gameId, navController = navController)
            }
            composable("games") { GamesScreen(navController = navController) }
            composable("player/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.read { getStringOrNull("uid") } ?: ""
                PlayerProfileScreen(uid = uid, navController = navController)
            }
            composable("activity") { ActivityScreen(navController = navController) }
            composable("pending") { PendingApprovalsScreen(navController = navController) }
        }
    }
}
