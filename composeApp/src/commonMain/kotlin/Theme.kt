import androidx.compose.ui.graphics.Color

val NavBackground = Color(0xFF12101A)
val CardBg = Color(0xFF1E1B2E)
val PurpleMain = Color(0xFF7B2FBE)
val GreenColor = Color(0xFF4CAF50)
val RedColor = Color(0xFFE53935)
val GoldColor = Color(0xFFFFD700)
val LightGreen = Color(0xFF53B957)
val DarkBackground = Color(0xFF12101A)
val CardBackground = Color(0xFF1E1B2E)

fun getRankColor(rank: String): Color {
    return when (rank) {
        "LEGEND" -> GoldColor
        "MASTER" -> Color(0xFF9C27B0)
        "ELITE" -> Color(0xFFE53935)
        "VETERAN" -> Color(0xFF2196F3)
        "IRON" -> Color(0xFFE79A30)
        else -> Color.Gray
    }
}

fun getRankFromMMR(mmr: Int): String {
    return when {
        mmr >= 200 -> "LEGEND"
        mmr >= 100 -> "MASTER"
        mmr >= 50  -> "ELITE"
        mmr >= 25  -> "VETERAN"
        else       -> "IRON"
    }
}

// Simple date formatter - will be replaced with proper implementation per platform if needed
fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return ""
    // Basic epoch-based formatting (UTC)
    val seconds = timestamp / 1000
    val minutes = (seconds / 60) % 60
    val hours = (seconds / 3600) % 24
    val days = seconds / 86400
    // Approximate month/day from epoch days (Jan 1 1970 = day 0)
    var y = 1970
    var remaining = days
    while (true) {
        val daysInYear = if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 366L else 365L
        if (remaining < daysInYear) break
        remaining -= daysInYear
        y++
    }
    val monthDays = intArrayOf(31, if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    val monthNames = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    var m = 0
    while (m < 12 && remaining >= monthDays[m]) {
        remaining -= monthDays[m]
        m++
    }
    val day = (remaining + 1).toString().padStart(2, '0')
    val h = hours.toString().padStart(2, '0')
    val min = minutes.toString().padStart(2, '0')
    return "${monthNames[m]} $day, $h:$min"
}
