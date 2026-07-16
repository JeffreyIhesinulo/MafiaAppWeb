import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun CreateGamesScreen() {
    var selectedPlayers by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var notes by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("town") }
    var isLoading by remember { mutableStateOf(false) }
    var players by remember { mutableStateOf<List<User>>(emptyList()) }
    val repository = remember { ProfileRepository() }
    val gamesRepository = remember { GamesRepository() }
    val scope = rememberCoroutineScope()
    var hostId by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var hasWill by remember { mutableStateOf(false) }
    var willUid by remember { mutableStateOf("") }
    var willGuesses by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { players = repository.getUsers(); isLoading = false }

    Column(modifier = Modifier.fillMaxSize().background(NavBackground).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).background(PurpleMain, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text("⚡", fontSize = 16.sp) }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Game", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        if (statusMessage.isNotEmpty()) { Text(statusMessage, color = if (statusMessage.contains("created")) GreenColor else RedColor, modifier = Modifier.padding(horizontal = 16.dp), fontSize = 13.sp) }
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), minLines = 3)
        Row(modifier = Modifier.padding(16.dp)) {
            Button(onClick = { result = "town" }, colors = ButtonDefaults.buttonColors(containerColor = if (result == "town") PurpleMain else CardBg)) { Text("♥ Town") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { result = "mafia" }, colors = ButtonDefaults.buttonColors(containerColor = if (result == "mafia") PurpleMain else CardBg)) { Text("♠ Mafia") }
        }
        Text("Select Players", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        players.forEach { player ->
            val isSelected = selectedPlayers.containsKey(player.uid)
            val currentRole = selectedPlayers[player.uid] ?: ""
            var expanded by remember { mutableStateOf(false) }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isSelected, onCheckedChange = { checked -> selectedPlayers = if (checked) selectedPlayers + (player.uid to "") else selectedPlayers - player.uid })
                Text(player.username, color = Color.White)
                if (isSelected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box {
                        OutlinedButton(onClick = { expanded = true }, border = BorderStroke(1.dp, PurpleMain), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text(currentRole.ifEmpty { "Role ▼" }, fontSize = 12.sp) }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("Citizen", "Sheriff", "Mafia", "Don").forEach { role -> DropdownMenuItem(text = { Text(role) }, onClick = { selectedPlayers = selectedPlayers + (player.uid to role); expanded = false }) }
                        }
                    }
                }
            }
        }
        var hostExpanded by remember { mutableStateOf(false) }
        val hostName = players.find { it.uid == hostId }?.username ?: "Select Host"
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedButton(onClick = { hostExpanded = true }, border = BorderStroke(1.dp, PurpleMain)) { Text(hostName) }
            DropdownMenu(expanded = hostExpanded, onDismissRequest = { hostExpanded = false }) { players.forEach { player -> DropdownMenuItem(text = { Text(player.username) }, onClick = { hostId = player.uid; hostExpanded = false }) } }
        }
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = hasWill, onCheckedChange = { hasWill = it }); Text("Last Will", color = Color.Red, fontWeight = FontWeight.Bold) }
        if (hasWill) {
            var willExpanded by remember { mutableStateOf(false) }
            val willName = players.find { it.uid == willUid }?.username ?: "Select Player"
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedButton(onClick = { willExpanded = true }, border = BorderStroke(1.dp, PurpleMain)) { Text(willName) }
                DropdownMenu(expanded = willExpanded, onDismissRequest = { willExpanded = false }) { players.forEach { player -> DropdownMenuItem(text = { Text(player.username) }, onClick = { willUid = player.uid; willExpanded = false }) } }
            }
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Guesses: $willGuesses", color = Color.White); Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { if (willGuesses > 0) willGuesses-- }) { Text("-") }; Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = { if (willGuesses < 4) willGuesses++ }) { Text("+") }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val roles = selectedPlayers.values.toList()
            val error = when {
                selectedPlayers.size < 8 -> "Minimum 8 players!"
                selectedPlayers.size > 12 -> "Maximum 12 players!"
                roles.any { it.isEmpty() } -> "Assign roles to all players!"
                roles.count { it == "Sheriff" } != 1 -> "Must have exactly 1 Sheriff!"
                roles.count { it == "Don" } != 1 -> "Must have exactly 1 Don!"
                hostId.isEmpty() -> "Select a host!"
                hasWill && willUid.isEmpty() -> "Select player for Last Will!"
                selectedPlayers.containsKey(hostId) -> "Host cannot be a player!"
                else -> null
            }
            if (error != null) { statusMessage = error; return@Button }
            scope.launch {
                val success = gamesRepository.createGame(result = result, season = 1, notes = notes, players = selectedPlayers, allUsers = players, hostId = hostId, willUid = if (hasWill) willUid else "", willGuesses = if (hasWill) willGuesses else 0)
                statusMessage = if (success) { selectedPlayers = emptyMap(); notes = ""; result = "town"; "Game created!" } else "Error creating game!"
            }
        }, modifier = Modifier.fillMaxWidth().padding(16.dp), colors = ButtonDefaults.buttonColors(containerColor = PurpleMain), shape = RoundedCornerShape(12.dp)) { Text("Save Game", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}
