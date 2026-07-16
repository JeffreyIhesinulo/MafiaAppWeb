import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore

class ActivityRepository {
    private val db = Firebase.firestore

    suspend fun getActivities(): List<Activity> {
        return try {
            val snapshot = db.collection("activity")
                .orderBy("createdAt", Direction.DESCENDING)
                .get()
            snapshot.documents.map { doc ->
                Activity(
                    id = doc.id,
                    type = doc.get("type"),
                    title = doc.get("title"),
                    body = doc.get("body"),
                    createdAt = doc.get<Long?>("createdAt") ?: 0L,
                    createdBy = doc.get("createdBy"),
                    gameId = doc.get<String?>("gameId") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createAnnouncement(title: String, body: String, createdBy: String): Boolean {
        return try {
            db.collection("activity").add(
                mapOf(
                    "type" to "announcement",
                    "title" to title,
                    "body" to body,
                    "createdAt" to FieldValue.serverTimestamp,
                    "createdBy" to createdBy
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }
}
