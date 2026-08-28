import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

class AuthRepository {
    private val auth = Firebase.auth

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password)
            val user = result.user ?: return Result.failure(Exception("Login failed"))

            if (!user.isEmailVerified) {
                auth.signOut()
                return Result.failure(Exception("Email not verified"))
            }

            val doc = Firebase.firestore.collection("users").document(user.uid).get()
            if (!doc.exists) {
                auth.signOut()
                return Result.failure(Exception("Account not approved"))
            }

            val approved = doc.get<Boolean?>("approved")
            if (approved != true) {
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

            val result = auth.createUserWithEmailAndPassword(email, password)
            val user = result.user ?: return Result.failure(Exception("Registration failed"))


            val taken = Firebase.firestore.collection("users")
                .where { "username" equalTo username }
                .get()
                .documents.isNotEmpty()

            if (taken) {
                user.delete()
                auth.signOut()
                return Result.failure(Exception("Username already taken"))
            }


            Firebase.firestore.collection("users").document(user.uid).set(
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

            user.sendEmailVerification()


            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            auth.signOut()
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