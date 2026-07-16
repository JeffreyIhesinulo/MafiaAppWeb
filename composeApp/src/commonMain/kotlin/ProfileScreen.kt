import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import kotlinx.coroutines.launch

@Composable
fun MmrChart(mmrHistory: List<Pair<Long, Int>>, currentMmr: Int) {
    var runningMmr = currentMmr - mmrHistory.sumOf { it.second }
    val points = mmrHistory.map { (_, change) -> runningMmr += change; runningMmr }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${points.minOrNull() ?: 0}", color = Color.Gray, fontSize = 10.sp)
        Text("${points.maxOrNull() ?: 0}", color = Color.Gray, fontSize = 10.sp)
    }
    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        val w = size.width; val h = size.height; val pad = 16.dp.toPx()
        val minMmr = points.min().toFloat(); val maxMmr = points.max().toFloat()
        val range = (maxMmr - minMmr).coerceAtLeast(1f)
        val path = Path()
        points.forEachIndexed { i, mmr ->
            val x = pad + (i.toFloat() / (points.size - 1).coerceAtLeast(1)) * (w - pad * 2)
            val y = h - pad - ((mmr - minMmr) / range) * (h - pad * 2)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        for (i in 0..3) { val y = pad + (i.toFloat() / 3) * (h - pad * 2); drawLine(color = Color.Gray.copy(alpha = 0.2f), start = Offset(pad, y), end = Offset(w - pad, y), strokeWidth = 1.dp.toPx()) }
        drawPath(path = path, color = Color(0xFF4CAF50), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        points.forEachIndexed { i, mmr ->
            val x = pad + (i.toFloat() / (points.size - 1).coerceAtLeast(1)) * (w - pad * 2)
            val y = h - pad - ((mmr - minMmr) / range) * (h - pad * 2)
            drawCircle(color = Color(0xFF4CAF50), radius = 4.dp.toPx(), center = Offset(x, y))
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        mmrHistory.firstOrNull()?.let { Text(formatDate(it.first).take(6), color = Color.Gray, fontSize = 10.sp) }
        mmrHistory.lastOrNull()?.let { Text(formatDate(it.first).take(6), color = Color.Gray, fontSize = 10.sp) }
    }
}

@Composable
fun RecentGameRow(game: Game, myPlayer: GamePlayer?, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(NavBackground).padding(horizontal = 16.dp, vertical = 12.dp).clickable { onClick() }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text(text = "#GM-${game.gameNumber}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold); Text(text = myPlayer?.role ?: "", color = Color.Gray, fontSize = 13.sp) }
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "+ ${myPlayer?.mmrChange ?: 0}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (game.result == "town") LightGreen.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(text = if (game.result == "town") "♥ Town" else "♠ Mafia", color = if (game.result == "town") LightGreen else Color.Red, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val repository = remember { ProfileRepository() }
    var showEditDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var recentGames by remember { mutableStateOf<List<Game>>(emptyList()) }
    var mmrHistory by remember { mutableStateOf<List<Pair<Long, Int>>>(emptyList()) }
    val gamesRepository = remember { GamesRepository() }
    var statusMessage by remember { mutableStateOf("") }

    if (showEditDialog) {
        var newUsername by remember { mutableStateOf(user?.username ?: "") }
        AlertDialog(onDismissRequest = { showEditDialog = false }, title = { Text("Edit Username") }, text = {
            OutlinedTextField(value = newUsername, onValueChange = { newUsername = it }, label = { Text("Username") })
        }, confirmButton = {
            Button(shape = RoundedCornerShape(14.dp), onClick = { scope.launch { val success = repository.updateUsername(newUsername); if (success) { user = user?.copy(username = newUsername); showEditDialog = false; statusMessage = "Username updated!" } } }) { Text("Save") }
        }, dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel", color = Color.Gray) } })
    }

    LaunchedEffect(Unit) {
        user = repository.getCurrentUser()
        user?.let { mmrHistory = gamesRepository.getPlayerMMRHistory(it.uid); recentGames = gamesRepository.getPlayerRecentGames(it.uid) }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(NavBackground).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        if (isLoading) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PurpleMain) } }
        else {
            Box(modifier = Modifier.fillMaxWidth().background(CardBg).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(32.dp).background(PurpleMain, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text("\uD83D\uDC64", fontSize = 16.sp) }; Spacer(modifier = Modifier.width(8.dp)); Text("Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                IconButton(onClick = { showEditDialog = true }, modifier = Modifier.align(Alignment.CenterEnd)) { Text("✏\uFE0F", fontSize = 18.sp) }
            }
            if (statusMessage.isNotEmpty()) { Text(statusMessage, color = GreenColor, fontSize = 13.sp, modifier = Modifier.padding(8.dp)) }
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
            if (user?.isAdmin == true) { Spacer(modifier = Modifier.height(8.dp)); Button(onClick = { navController.navigate("pending") }, colors = ButtonDefaults.buttonColors(containerColor = PurpleMain)) { Text("Pending Approvals") } }
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(CardBg).padding(horizontal = 32.dp, vertical = 16.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("CURRENT RATING", color = Color.Gray, fontSize = 11.sp); Text("${user?.mmr ?: 0} MMR", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold) } }
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
                    if (mmrHistory.isEmpty()) Text("No games this month", color = Color.Gray, fontSize = 12.sp) else MmrChart(mmrHistory, user?.mmr ?: 0)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Recent Games", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (recentGames.isEmpty()) Text("No recent games", color = Color.Gray, fontSize = 12.sp)
                    else recentGames.forEach { game -> val myPlayer = game.players.find { it.uid == user?.uid }; RecentGameRow(game = game, myPlayer = myPlayer, onClick = { navController.navigate("game/${game.id}") }); Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { scope.launch { AuthRepository().logout(); navController.navigate("login") { popUpTo(0) } } }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000)), shape = RoundedCornerShape(12.dp)) { Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
