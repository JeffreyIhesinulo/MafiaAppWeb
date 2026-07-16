import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

class AuthRepository {
    private val auth = Firebase.auth

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password)
            if (result.user?.isEmailVerified == false) {
                auth.signOut()
                return Result.failure(Exception("Email not verified"))
            }
            val uid = result.user?.uid ?: ""
            val doc = Firebase.firestore.collection("users").document(uid).get()
            val approved = doc.get<Boolean>("approved")
            if (!approved) {
                auth.signOut()
                return Result.failure(Exception("Account not approved"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, username: String): Result<Unit> {
        return try {
            val usernameCheck = Firebase.firestore.collection("users")
                .where { "username" equalTo username }
                .get()
            if (usernameCheck.documents.isNotEmpty()) {
                return Result.failure(Exception("Username already taken"))
            }
            val result = auth.createUserWithEmailAndPassword(email, password)
            val uid = result.user?.uid ?: ""
            result.user?.sendEmailVerification()
            Firebase.firestore.collection("users").document(uid).set(
                mapOf(
                    "username" to username,
                    "email" to email,
                    "isAdmin" to false,
                    "mmr" to 0,
                    "wins" to 0,
                    "losses" to 0,
                    "games" to 0,
                    "rank" to "IRON",
                    "approved" to false
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun resetPassword(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email)
            true
        } catch (e: Exception) {
            false
        }
    }
}
