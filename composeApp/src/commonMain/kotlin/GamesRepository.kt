import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.fromMilliseconds
import dev.gitlive.firebase.firestore.toMilliseconds

class GamesRepository {
    private val db = Firebase.firestore

    private fun readDate(doc: dev.gitlive.firebase.firestore.DocumentSnapshot, field: String): Long {
        return try {
            val ts = doc.get<Timestamp>(field)
            ts.toMilliseconds().toLong()
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun getGames(): List<Game> {
        return try {
            val snapshot = db.collection("games").get()
            snapshot.documents.map { doc ->
                Game(
                    id = doc.id,
                    gameNumber = doc.get<Long>("gameNumber").toInt(),
                    date = readDate(doc, "date"),
                    result = doc.get("result"),
                    season = doc.get<Long>("season").toInt(),
                    players = doc.get<List<GamePlayer>>("players")
                )
            }
        } catch (e: Exception) {
            println("GAMES ERROR: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createGame(
        result: String,
        season: Int,
        notes: String,
        players: Map<String, String>,
        allUsers: List<User>,
        hostId: String,
        willUid: String,
        willGuesses: Int
    ): Boolean {
        return try {
            val gamesCount = db.collection("games").get().documents.size
            val gameNumber = gamesCount + 1
            val currentUid = Firebase.auth.currentUser?.uid
            val playersList = players.map { (uid, role) ->
                val user = allUsers.find { it.uid == uid }
                GamePlayer(
                    uid = uid,
                    username = user?.username ?: "",
                    mmrChange = 0,
                    role = role,
                    rank = user?.rank ?: ""
                )
            }
            val gameData = mapOf(
                "gameNumber" to gameNumber,
                "result" to result,
                "season" to season,
                "notes" to notes,
                "date" to FieldValue.serverTimestamp,
                "createdBy" to currentUid,
                "players" to playersList
            )
            val gameRef = db.collection("games").add(gameData)
            val gameId = gameRef.id
            calculateAndUpdateMMR(players, result, hostId, willUid, willGuesses, gameId)
            db.collection("activity").add(
                mapOf(
                    "type" to "game",
                    "title" to "New Game Recorded",
                    "body" to "Game #GM-$gameNumber has been processed. ${if (result == "town") "Town" else "Mafia"} Victory!",
                    "createdAt" to FieldValue.serverTimestamp,
                    "createdBy" to currentUid,
                    "gameId" to gameId
                )
            )
            true
        } catch (e: Exception) {
            println("CREATE GAME ERROR: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun calculateAndUpdateMMR(
        players: Map<String, String>,
        result: String,
        hostId: String,
        willUid: String,
        willGuesses: Int,
        gameId: String
    ) {
        players.forEach { (uid, role) ->
            var mmrGain = 1
            mmrGain += when {
                result == "town" && role == "Citizen" -> 5
                result == "town" && role == "Sheriff" -> 10
                result == "mafia" && role == "Mafia" -> 6
                result == "mafia" && role == "Don" -> 10
                else -> 0
            }
            if (uid == willUid) mmrGain += willGuesses

            val gameDoc = db.collection("games").document(gameId).get()
            val playerList = gameDoc.get<List<GamePlayer>>("players")
            val updatedPlayers = playerList.map { player ->
                if (player.uid == uid) player.copy(mmrChange = mmrGain) else player
            }
            db.collection("games").document(gameId).update("players" to updatedPlayers)

            val isWinner = (result == "town" && (role == "Citizen" || role == "Sheriff")) ||
                    (result == "mafia" && (role == "Mafia" || role == "Don"))

            val userDoc = db.collection("users").document(uid).get()
            val currentMmr = userDoc.get<Long>("mmr").toInt()
            val newMmr = currentMmr + mmrGain
            val newRank = getRankFromMMR(newMmr)
            db.collection("users").document(uid).update(
                "mmr" to FieldValue.increment(mmrGain),
                "rank" to newRank,
                "games" to FieldValue.increment(1),
                "wins" to FieldValue.increment(if (isWinner) 1 else 0),
                "losses" to FieldValue.increment(if (!isWinner) 1 else 0),
                "lastGameAt" to FieldValue.serverTimestamp
            )
        }
        if (hostId.isNotEmpty()) {
            db.collection("users").document(hostId).update(
                "mmr" to FieldValue.increment(1)
            )
        }
    }

    suspend fun getPlayerMMRHistory(uid: String): List<Pair<Long, Int>> {
        return try {
            val oneMonthAgo = currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val snapshot = db.collection("games").get()
            snapshot.documents.mapNotNull { doc ->
                val date = readDate(doc, "date")
                if (date < oneMonthAgo) return@mapNotNull null
                val playerList = doc.get<List<GamePlayer>>("players")
                val player = playerList.find { it.uid == uid }
                if (player != null) Pair(date, player.mmrChange) else null
            }.sortedBy { it.first }
        } catch (e: Exception) {
            println("MMR HISTORY ERROR: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPlayerRecentGames(uid: String): List<Game> {
        return try {
            val snapshot = db.collection("games")
                .orderBy("date", Direction.DESCENDING)
                .limit(5)
                .get()
            snapshot.documents.mapNotNull { doc ->
                val playerList = doc.get<List<GamePlayer>>("players")
                val isParticipant = playerList.any { it.uid == uid }
                if (!isParticipant) return@mapNotNull null
                Game(
                    id = doc.id,
                    gameNumber = doc.get<Long>("gameNumber").toInt(),
                    date = readDate(doc, "date"),
                    result = doc.get("result"),
                    season = doc.get<Long>("season").toInt(),
                    players = playerList
                )
            }
        } catch (e: Exception) {
            println("RECENT GAMES ERROR: ${e.message}")
            emptyList()
        }
    }

    suspend fun getGameById(id: String): Game? {
        return try {
            val doc = db.collection("games").document(id).get()
            Game(
                id = doc.id,
                gameNumber = doc.get<Long>("gameNumber").toInt(),
                result = doc.get("result"),
                season = doc.get<Long>("season").toInt(),
                date = readDate(doc, "date"),
                createdBy = doc.get("createdBy"),
                players = doc.get<List<GamePlayer>>("players")
            )
        } catch (e: Exception) {
            println("GAME BY ID ERROR: ${e.message}")
            null
        }
    }
}

expect fun currentTimeMillis(): Long
