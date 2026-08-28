import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class PlayerRepository {
    private val db = Firebase.firestore

    suspend fun getPlayers(): List<Player> {
        return try {
            val snapshot = db.collection("users").get()
            snapshot.documents.map { doc ->
                val rank = doc.get<String>("rank")
                Player(
                    uid = doc.id,
                    username = doc.get("username"),
                    rank = rank,
                    rankColor = getRankColor(rank),
                    mmr = doc.get<Long>("mmr").toInt(),
                    mmrChange = (doc.get<Long?>("mmrChange") ?: 0L).toInt(),
                    games = doc.get<Long>("games").toInt(),
                    isAdmin = doc.get("isAdmin"),
                    lastGameAt = (doc.get<Long?>("lastGameAt") ?: 0L)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPendingUsers(): List<User> {
        return try {
            val snapshot = db.collection("users")
                .where { "approved" equalTo false }
                .get()
            snapshot.documents.map { doc ->
                User(
                    uid = doc.id,
                    username = doc.get("username"),
                    email = doc.get("email"),
                    mmr = doc.get<Long>("mmr").toInt(),
                    wins = doc.get<Long>("wins").toInt(),
                    losses = doc.get<Long>("losses").toInt(),
                    games = doc.get<Long>("games").toInt(),
                    rank = doc.get("rank"),
                    isAdmin = doc.get("isAdmin"),
                    approved = doc.get("approved")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun rejectUser(uid: String): Boolean {
        return try {
            val doc = db.collection("users").document(uid).get()
            val username = doc.get<String?>("username")

            db.collection("users").document(uid).delete()

            if (username != null) {
                db.collection("usernames").document(username.lowercase()).delete()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun approveUser(uid: String): Boolean {
        return try {
            db.collection("users").document(uid).update("approved" to true)
            true
        } catch (e: Exception) {
            false
        }
    }
}
