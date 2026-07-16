import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PlayersScreen(navController: NavController) {
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Players") }
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val repository = remember { PlayerRepository() }
    val filters = listOf("All Players", "Top Rated", "Recent Active", "Admins")

    LaunchedEffect(Unit) {
        players = repository.getPlayers()
        isLoading = false
    }

    val filteredPlayers = when (selectedFilter) {
        "Admins" -> players.filter { it.isAdmin }
        "Top Rated" -> players.sortedByDescending { it.mmr }
        "Recent Active" -> players.sortedByDescending { it.lastGameAt }
        else -> players
    }.filter { it.username.contains(searchText, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().background(NavBackground)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(PurpleMain, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text("⚡", fontSize = 16.sp) }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Players", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        OutlinedTextField(value = searchText, onValueChange = { searchText = it }, placeholder = { Text("Search players...", color = Color.Gray) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleMain, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White, unfocusedContainerColor = CardBg, focusedContainerColor = CardBg), shape = RoundedCornerShape(12.dp), leadingIcon = { Text("\uD83D\uDD0D", fontSize = 16.sp) })
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                val isSelected = filter == selectedFilter
                Box(modifier = Modifier.clickable { selectedFilter = filter }.background(if (isSelected) PurpleMain else CardBg, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(filter, color = if (isSelected) Color.White else Color.Gray, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("CLUB ROSTER", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Total ${filteredPlayers.size} Registered Members", color = Color.Gray, fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PurpleMain) }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(filteredPlayers) { player -> PlayerCard(player = player, onClick = { navController.navigate("player/${player.uid}") }) }
            }
        }
    }
}

@Composable
fun PlayerCard(player: Player, onClick: () -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(PurpleMain, CircleShape), contentAlignment = Alignment.Center) { Text(player.username.first().toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(player.username, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.background(player.rankColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(player.rank, color = player.rankColor, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                }
                Text("⚡ ${player.games} games", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${player.mmr} MMR", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(if (player.mmrChange > 0) "+${player.mmrChange}" else "${player.mmrChange}", color = if (player.mmrChange > 0) GreenColor else RedColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("›", color = Color.Gray, fontSize = 20.sp)
        }
    }
}
