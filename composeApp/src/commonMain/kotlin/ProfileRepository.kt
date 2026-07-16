import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

class ProfileRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun getCurrentUser(): User? {
        return try {
            val uid = auth.currentUser?.uid ?: return null
            val doc = db.collection("users").document(uid).get()
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
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUsername(newUsername: String): Boolean {
        return try {
            val uid = auth.currentUser?.uid ?: return false
            val check = db.collection("users")
                .where { "username" equalTo newUsername }
                .get()
            if (check.documents.isNotEmpty()) return false
            db.collection("users").document(uid).update("username" to newUsername)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUsers(): List<User> {
        return try {
            val snapshot = db.collection("users").get()
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

    suspend fun getUserById(uid: String): User? {
        return try {
            val doc = db.collection("users").document(uid).get()
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
        } catch (e: Exception) {
            null
        }
    }
}
