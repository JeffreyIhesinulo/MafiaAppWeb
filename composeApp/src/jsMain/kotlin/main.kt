import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.browser.document

// TODO: Get these values from Firebase Console -> Project Settings -> General -> Web apps
// If you haven't added a Web App yet: click "Add app" -> Web (</>)
private val firebaseOptions = FirebaseOptions(
    apiKey = "AIzaSyDkFIZ11GA4_DkzLESD6VXdBfzTZoiktqw",                              // TODO: replace
    authDomain = "mafiaapp-819fd.firebaseapp.com",
    projectId = "mafiaapp-819fd",
    storageBucket = "mafiaapp-819fd.appspot.com",
    applicationId = "1:458614595851:web:53ce47f0858ee01784c20e",                        // TODO: replace
    gcmSenderId = "458614595851"        // TODO: replace
)

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Firebase.initialize(options = firebaseOptions)
    ComposeViewport(document.body!!) {
        App()
    }
}

actual fun currentTimeMillis(): Long = js("Date.now()").unsafeCast<Double>().toLong()
