
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

data class User(
    val uid: String,
    val username: String,
    val email: String,
    val mmr: Int,
    val wins: Int,
    val losses: Int,
    val games: Int,
    val rank: String,
    val isAdmin: Boolean,
    val approved: Boolean
)

data class Player(
    val uid: String = "",
    val username: String,
    val rank: String,
    val rankColor: androidx.compose.ui.graphics.Color,
    val mmr: Int,
    val mmrChange: Int,
    val games: Int,
    val isAdmin: Boolean = false,
    val lastGameAt: Long = 0L
)

data class Game(
    val id: String = "",
    val gameNumber: Int = 0,
    val result: String = "",
    val season: Int = 0,
    val date: Long = 0L,
    val createdBy: String = "",
    val players: List<GamePlayer> = emptyList()
)
@Serializable
data class GamePlayer(
    val uid: String = "",
    val username: String = "",
    val role: String = "",
    val mmrChange: Int = 0,
    val rank: String = ""
)

data class Activity(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val createdAt: Long = 0L,
    val createdBy: String = "",
    val gameId: String = ""
)
