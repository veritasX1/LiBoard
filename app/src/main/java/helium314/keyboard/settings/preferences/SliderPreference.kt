// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import helium314.keyboard.latin.utils.Log
import helium314.keyboard.latin.utils.getActivity
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.settings.SettingsActivity
import helium314.keyboard.settings.dialogs.SliderDialog
import androidx.core.content.edit

@Suppress("UNCHECKED_CAST") // it's sort of checked
@Composable
/** Slider preference for Int or Float (weird casting stuff, but should be fine) */
fun <T: Number> SliderPreference(
    name: String,
    modifier: Modifier = Modifier,
    key: String,
    description: @Composable (T) -> String,
    default: T,
    range: ClosedFloatingPointRange<Float>,
    stepSize: Int? = null,
    onValueChanged: (Float?) -> Unit = { },
    footer: String? = null, // LiBoard: what the value does, below the slider
    onConfirmed: (T) -> Unit = { },
) {
    val ctx = LocalContext.current
    val prefs = ctx.prefs()
    val b = (ctx.getActivity() as? SettingsActivity)?.prefChanged?.collectAsState()
    if ((b?.value ?: 0) < 0)
        Log.v("irrelevant", "stupid way to trigger recomposition on preference change")
    val initialValue = if (default is Int || default is Float)
        getPrefOfType(prefs, key, default)
    else throw IllegalArgumentException("only float and int are supported")

    var showDialog by rememberSaveable { mutableStateOf(false) }
    Preference(
        name = name,
        onClick = { showDialog = true },
        modifier = modifier,
    ) {
        // LiBoard: the value in grey on the right, like the lists (card 23328ec0)
        androidx.compose.material3.Text(description(initialValue), maxLines = 1)
        helium314.keyboard.latin.utils.NextScreenIcon()
    }
    // LiBoard: an iPhone-like page with the slider instead of an Android pop-up (card 23328ec0)
    if (showDialog) {
        var position by remember { mutableStateOf(initialValue.toFloat()) }
        fun save(value: Float) {
            if (default is Int) { prefs.edit { putInt(key, value.toInt()) }; onConfirmed(value.toInt() as T) }
            else { prefs.edit { putFloat(key, value) }; onConfirmed(value as T) }
        }
        helium314.keyboard.settings.IosSubPage(name, onBack = { onValueChanged(null); showDialog = false }) {
            @Suppress("UNCHECKED_CAST")
            val shown = description((if (default is Int) position.toInt() else position) as T)
            helium314.keyboard.settings.IosGroup(footer = footer, items = listOf {
                androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    androidx.compose.material3.Text(shown, fontSize = 17.sp)
                    helium314.keyboard.settings.IosSlider(
                        value = position,
                        onValueChange = { position = it; onValueChanged(it) },
                        onValueChangeFinished = { save(position) },
                        valueRange = range,
                        steps = stepSize?.let { ((range.endInclusive - range.start) / it - 1).toInt() } ?: 0,
                    )
                }
            })
            helium314.keyboard.settings.IosGroup(items = listOf {
                helium314.keyboard.settings.IosActionRow(androidx.compose.ui.res.stringResource(helium314.keyboard.latin.R.string.liboard_reset_to_default)) {
                    prefs.edit { remove(key) }; position = default.toFloat(); onValueChanged(null); onConfirmed(default)
                }
            })
        }
    }
}
