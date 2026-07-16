import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
fun GamesScreen(navController: NavController) {
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val repository = remember { GamesRepository() }
    LaunchedEffect(Unit) { games = repository.getGames(); isLoading = false }

    Column(modifier = Modifier.fillMaxSize().background(NavBackground)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PurpleMain) }
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).background(PurpleMain, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text("⚡", fontSize = 16.sp) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Games", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(games) { game -> GameCard(game = game, onClick = { navController.navigate("game/${game.id}") }) }
            }
        }
    }
}

@Composable
fun GameCard(game: Game, onClick: () -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "#GM - ${game.gameNumber}", color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = formatDate(game.date), color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                game.players.take(5).forEach { player ->
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(CardBg), contentAlignment = Alignment.Center) { Text(text = player.username.firstOrNull()?.uppercase() ?: "?", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(getRankColor(player.rank)))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (game.players.size > 5) Text("+${game.players.size - 5}", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Season - ${game.season}", color = Color.White, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(LightGreen).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(text = if (game.result == "town") "♥ Town Victory" else "♠ Mafia Victory", color = if (game.result == "town") Color.Red else Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
