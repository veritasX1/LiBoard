// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.settings.SearchSettingsScreen
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * LiBoard: typing test – measures how cleanly one types, so every change to the typing engine can
 * be checked instead of guessed. Works with any keyboard (compare FUTO and LiBoard in the same way).
 * Everything stays on the phone (shared preferences), nothing is sent anywhere.
 */
private val SENTENCES = listOf(
    "Ich komme heute etwas später nach Hause.",
    "Kannst du bitte noch Milch und Brot mitbringen?",
    "Das Wetter in Kiel ist wieder richtig schön.",
    "Wir treffen uns morgen um halb neun am Bahnhof.",
    "Der Bericht ist fast fertig, ich schicke ihn gleich.",
    "Danke für die schnelle Antwort, das hilft mir sehr.",
    "Die Präsentation braucht noch ein paar Folien.",
    "Hast du schon den neuen Kalender ausprobiert?",
    "Das Meeting ist leider auf Donnerstag verschoben.",
    "Bitte prüfe die Zahlen noch einmal genau.",
    "Thanks, see you tomorrow at the meeting.",
    "Das Feature ist im neuen Release enthalten.",
)
private const val RUNS_KEY = "liboard_typing_runs"

/** Edit distance between what was asked and what was typed (characters). */
private fun distance(a: String, b: String): Int {
    val previous = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        var diagonal = previous[0]
        previous[0] = i
        for (j in 1..b.length) {
            val above = previous[j]
            previous[j] = minOf(previous[j] + 1, previous[j - 1] + 1, diagonal + if (a[i - 1] == b[j - 1]) 0 else 1)
            diagonal = above
        }
    }
    return previous[b.length]
}

private fun activeKeyboard(context: android.content.Context): String {
    val id = AndroidSettings.Secure.getString(context.contentResolver, AndroidSettings.Secure.DEFAULT_INPUT_METHOD).orEmpty()
    return when {
        "liboard" in id -> "LiBoard"
        "futo" in id -> "FUTO"
        "google" in id -> "Gboard"
        else -> id.substringBefore("/").substringAfterLast(".").ifEmpty { "?" }
    }
}

@Composable
fun TypingTestScreen(onClickBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.prefs()
    var index by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    var started by remember { mutableLongStateOf(0L) }
    var deletions by remember { mutableIntStateOf(0) }
    // totals of the running round
    var errors by remember { mutableIntStateOf(0) }
    var characters by remember { mutableIntStateOf(0) }
    var corrections by remember { mutableIntStateOf(0) }
    var millis by remember { mutableLongStateOf(0L) }
    var runs by remember { mutableStateOf(JSONArray(prefs.getString(RUNS_KEY, "[]"))) }
    val target = SENTENCES[index % SENTENCES.size]
    val done = index >= SENTENCES.size

    fun next() {
        val typed = text.trim()
        errors += distance(target, typed)
        characters += target.length
        corrections += deletions
        if (started > 0) millis += System.currentTimeMillis() - started
        text = ""; started = 0L; deletions = 0
        index++
        if (index >= SENTENCES.size) {
            val run = JSONObject()
                .put("date", SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date()))
                .put("keyboard", activeKeyboard(context))
                .put("errorRate", errors.toDouble() / characters)
                .put("corrections", corrections)
                .put("wpm", if (millis > 0) characters / 5.0 / (millis / 60000.0) else 0.0)
            val updated = JSONArray(runs.toString()).put(run)
            while (updated.length() > 30) updated.remove(0)
            prefs.edit { putString(RUNS_KEY, updated.toString()) }
            runs = updated
        }
    }

    fun restart() {
        index = 0; text = ""; started = 0L; deletions = 0; errors = 0; characters = 0; corrections = 0; millis = 0L
    }

    SearchSettingsScreen(onClickBack = onClickBack, title = "Tipp-Test", settings = emptyList()) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!done) {
                Text("Satz ${index + 1} von ${SENTENCES.size} · Tastatur: ${activeKeyboard(context)}",
                    style = MaterialTheme.typography.bodySmall)
                Text(target, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = text,
                    onValueChange = { new ->
                        if (started == 0L && new.isNotEmpty()) started = System.currentTimeMillis()
                        if (new.length < text.length) deletions++
                        text = new
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    placeholder = { Text("Hier tippen – einfach drauflos, wie im Alltag") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { next() }, enabled = text.isNotBlank()) { Text("Weiter") }
                    OutlinedButton(onClick = { restart() }) { Text("Neu beginnen") }
                }
                Text("Fehler, die du selbst korrigierst, zählen als Korrekturen; was am Ende falsch bleibt, als Fehler.",
                    style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Fertig!", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { restart() }) { Text("Noch eine Runde") }
            }
            Spacer(Modifier.height(8.dp))
            Text("Bisherige Runden (nur auf diesem Handy)", fontWeight = FontWeight.Bold)
            if (runs.length() == 0) Text("Noch keine – tippe eine Runde mit FUTO und eine mit LiBoard zum Vergleich.",
                style = MaterialTheme.typography.bodySmall)
            for (i in runs.length() - 1 downTo 0) {
                val run = runs.getJSONObject(i)
                Text(
                    "${run.getString("date")} · ${run.getString("keyboard")}: " +
                        "%.1f %% Fehler · %d Korrekturen · %.0f Wörter/Min".format(Locale.GERMANY,
                            run.getDouble("errorRate") * 100, run.getInt("corrections"), run.getDouble("wpm")),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
