import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PlayerProfileScreen(uid: String, navController: NavController) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val repository = remember { ProfileRepository() }
    var mmrHistory by remember { mutableStateOf<List<Pair<Long, Int>>>(emptyList()) }
    var recentGames by remember { mutableStateOf<List<Game>>(emptyList()) }
    val gamesRepository = remember { GamesRepository() }
    LaunchedEffect(Unit) {
        user = repository.getUserById(uid)
        user?.let { mmrHistory = gamesRepository.getPlayerMMRHistory(it.uid); recentGames = gamesRepository.getPlayerRecentGames(it.uid) }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(NavBackground).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        if (isLoading) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PurpleMain) } }
        else {
            Box(modifier = Modifier.fillMaxWidth().background(CardBg).padding(horizontal = 16.dp, vertical = 12.dp)) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterStart)) { Text("←", fontSize = 20.sp, color = Color.White) }
                Text(text = user?.username ?: "Player Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(CardBg), contentAlignment = Alignment.Center) { Text(text = user?.username?.firstOrNull()?.uppercase() ?: "?", fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(getRankColor(user?.rank ?: "")))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(user?.username ?: "", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(getRankColor(user?.rank ?: "").copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 4.dp)) { Text(user?.rank ?: "", color = getRankColor(user?.rank ?: ""), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                if (user?.isAdmin == true) { Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(PurpleMain.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 4.dp)) { Text("Admin", color = Color(0xFF9C27B0), fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(CardBg).padding(horizontal = 32.dp, vertical = 16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("CURRENT RATING", color = Color.Gray, fontSize = 11.sp); Text("${user?.mmr ?: 0} MMR", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(CardBg).padding(vertical = 16.dp)) {
                val winRate = if ((user?.games ?: 0) != 0) (user?.wins?.toFloat()!! / user?.games!! * 100).toInt() else 0
                listOf("${user?.games ?: 0}" to "GAMES", "${user?.wins ?: 0}" to "WINS", "${user?.losses ?: 0}" to "LOSSES", "$winRate%" to "WINRATE").forEachIndexed { index, (value, label) ->
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(label, color = Color.Gray, fontSize = 11.sp) }
                    if (index < 3) Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.Gray.copy(alpha = 0.3f)))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(CardBg).padding(16.dp)) {
                Column {
                    Text("Rating Progress", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("LAST MONTH", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (mmrHistory.isEmpty()) { Text("No games this month", color = Color.Gray, fontSize = 12.sp) }
                    else { MmrChart(mmrHistory = mmrHistory, currentMmr = user?.mmr ?: 0) }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Recent Games", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (recentGames.isEmpty()) { Text("No recent games", color = Color.Gray, fontSize = 12.sp) }
                    else { recentGames.forEach { game -> val myPlayer = game.players.find { it.uid == uid }; RecentGameRow(game = game, myPlayer = myPlayer, onClick = { navController.navigate("game/${game.id}") }); Spacer(modifier = Modifier.height(8.dp)) } }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
