import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import kotlinx.coroutines.launch

@Composable
fun PendingApprovalsScreen(navController: NavController) {
    var pendingUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val repository = remember { PlayerRepository() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { pendingUsers = repository.getPendingUsers(); isLoading = false }

    Column(modifier = Modifier.fillMaxSize().background(NavBackground)) {
        Box(modifier = Modifier.fillMaxWidth().background(CardBg).padding(horizontal = 16.dp, vertical = 12.dp)) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterStart)) { Text("←", fontSize = 20.sp, color = Color.White) }
            Text("Pending Approvals", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            if (pendingUsers.isNotEmpty()) { Box(modifier = Modifier.align(Alignment.CenterEnd).clip(CircleShape).background(PurpleMain).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("${pendingUsers.size}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
        }
        if (isLoading) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PurpleMain) } }
        else if (pendingUsers.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No pending approvals", color = Color.Gray) } }
        else {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pendingUsers) { user ->
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardBg).padding(16.dp)) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(PurpleMain), contentAlignment = Alignment.Center) { Text(user.username.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) { Text(user.username, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold); Text(user.email, color = Color.Gray, fontSize = 12.sp) }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { scope.launch { if (repository.rejectUser(user.uid)) pendingUsers = pendingUsers.filter { it.uid != user.uid } } }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red), border = BorderStroke(1.dp, Color.Red)) { Text("✗ Reject") }
                                Button(onClick = { scope.launch { if (repository.approveUser(user.uid)) pendingUsers = pendingUsers.filter { it.uid != user.uid } } }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = PurpleMain)) { Text("✓ Approve") }
                            }
                        }
                    }
                }
            }
        }
    }
}
