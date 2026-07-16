import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun App() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        AppNavigation()
    }
}
