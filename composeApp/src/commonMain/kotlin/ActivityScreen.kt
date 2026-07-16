import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.launch

@Composable
fun ActivityScreen(navController: NavController) {
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    val repository = remember { ActivityRepository() }
    val profileRepository = remember { ProfileRepository() }
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { currentUser = profileRepository.getCurrentUser(); activities = repository.getActivities(); isLoading = false }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showCreateDialog = false }, title = { Text("New Announcement") }, text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        }, confirmButton = {
            Button(onClick = { scope.launch { val success = repository.createAnnouncement(title, body, currentUser?.uid ?: ""); if (success) { activities = repository.getActivities(); showCreateDialog = false } } }) { Text("Post") }
        }, dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } })
    }

    Column(modifier = Modifier.fillMaxSize().background(NavBackground)) {
        Box(modifier = Modifier.fillMaxWidth().background(CardBg).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(PurpleMain, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text("\uD83D\uDD14", fontSize = 16.sp) }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Activity", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            if (currentUser?.isAdmin == true) { IconButton(onClick = { showCreateDialog = true }, modifier = Modifier.align(Alignment.CenterEnd)) { Text("➕", fontSize = 18.sp) } }
        }
        if (isLoading) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PurpleMain) } }
        else if (activities.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No activity yet", color = Color.Gray) } }
        else {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(activities) { activity ->
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardBg).clickable { if (activity.type == "game" && activity.gameId.isNotEmpty()) navController.navigate("game/${activity.gameId}") }.padding(16.dp)) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) { Text("\uD83D\uDCE2", fontSize = 16.sp); Spacer(modifier = Modifier.width(8.dp)); Text(text = activity.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                                Text(text = formatDate(activity.createdAt), color = Color.Gray, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = activity.body, color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
