// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextField
import helium314.keyboard.settings.IosFooter
import helium314.keyboard.settings.IosHeader
import helium314.keyboard.settings.IosGroup
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
import java.text.DateFormat
import androidx.compose.ui.res.stringResource
import helium314.keyboard.latin.R
import java.util.Date
import java.util.Locale

/**
 * LiBoard: typing test – measures how cleanly one types, so every change to the typing engine can
 * be checked instead of guessed. Works with any keyboard (compare FUTO and LiBoard in the same way).
 * Everything stays on the phone (shared preferences), nothing is sent anywhere.
 */
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
    val sentences = remember { context.resources.getStringArray(R.array.liboard_typing_sentences).toList() }
    var runs by remember { mutableStateOf(JSONArray(prefs.getString(RUNS_KEY, "[]"))) }
    val target = sentences[index % sentences.size]
    val done = index >= sentences.size

    fun next() {
        val typed = text.trim()
        errors += distance(target, typed)
        characters += target.length
        corrections += deletions
        if (started > 0) millis += System.currentTimeMillis() - started
        text = ""; started = 0L; deletions = 0
        index++
        if (index >= sentences.size) {
            val run = JSONObject()
                .put("date", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date()))
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

    SearchSettingsScreen(onClickBack = onClickBack, title = stringResource(R.string.liboard_typing_test), settings = emptyList()) {
        // LiBoard: laid out as iOS grouped lists (Apple HIG)
        Column(Modifier.verticalScroll(rememberScrollState()).imePadding()) {
            if (!done) {
                IosGroup(
                    header = stringResource(R.string.liboard_typing_sentence_of, index + 1, sentences.size, activeKeyboard(context)),
                    footer = stringResource(R.string.liboard_typing_footer),
                    items = listOf(
                        {
                            Text(target, style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                        },
                        {
                            TextField(
                                value = text,
                                onValueChange = { new ->
                                    if (started == 0L && new.isNotEmpty()) started = System.currentTimeMillis()
                                    if (new.length < text.length) deletions++
                                    text = new
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                placeholder = { Text(stringResource(R.string.liboard_typing_placeholder)) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                            )
                        },
                    )
                )
                Button(
                    onClick = { next() },
                    enabled = text.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp).fillMaxWidth().height(50.dp),
                ) { Text(stringResource(R.string.liboard_typing_next), fontWeight = FontWeight.SemiBold) }
                TextButton(
                    onClick = { restart() },
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                ) { Text(stringResource(R.string.liboard_typing_restart)) }
            } else {
                IosGroup(items = listOf {
                    Text(stringResource(R.string.liboard_typing_finished), style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                })
                Button(
                    onClick = { restart() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp).fillMaxWidth().height(50.dp),
                ) { Text(stringResource(R.string.liboard_typing_again), fontWeight = FontWeight.SemiBold) }
            }
            val header = stringResource(R.string.liboard_typing_runs)
            if (runs.length() == 0) {
                IosHeader(header)
                IosFooter(stringResource(R.string.liboard_typing_no_runs))
            } else IosGroup(
                header = header,
                items = (runs.length() - 1 downTo 0).map { i ->
                    val run = runs.getJSONObject(i)
                    val row: @Composable () -> Unit = {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Text("${run.getString("keyboard")} · ${run.getString("date")}", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                stringResource(R.string.liboard_typing_result,
                                    run.getDouble("errorRate") * 100, run.getInt("corrections"), run.getDouble("wpm")),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    row
                }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
