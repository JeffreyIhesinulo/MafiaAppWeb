import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun GameDetailScreen(gameId: String, navController: NavController) {
    var game by remember { mutableStateOf<Game?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val repository = remember { GamesRepository() }
    LaunchedEffect(Unit) { game = repository.getGameById(gameId); isLoading = false }

    Column(modifier = Modifier.fillMaxSize().background(NavBackground).verticalScroll(rememberScrollState())) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PurpleMain) }
        } else {
            Box(modifier = Modifier.fillMaxWidth().background(CardBg).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(text = "#GM-${game?.gameNumber}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).clip(RoundedCornerShape(12.dp)).background(if (game?.result == "town") LightGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)).padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (game?.result == "town") "♥" else "♠", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = if (game?.result == "town") "Town Victory" else "Mafia Victory", color = if (game?.result == "town") LightGreen else Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = formatDate(game?.date ?: 0L), color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(CardBg).padding(16.dp)) {
                Column {
                    Text("Roster & Performance", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    game?.players?.forEach { player ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { navController.navigate("player/${player.uid}") }, verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(getRankColor(player.rank)), contentAlignment = Alignment.Center) { Text(text = player.username.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = player.username, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(PurpleMain.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 2.dp)) { Text(text = player.role, color = PurpleMain, fontSize = 11.sp) }
                            }
                            Text(text = "+${player.mmrChange}", color = Color.Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        if (player != game?.players?.last()) { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray.copy(alpha = 0.2f))) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
