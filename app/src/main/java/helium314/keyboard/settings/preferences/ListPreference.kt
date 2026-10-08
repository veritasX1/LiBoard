package helium314.keyboard.settings.preferences

import android.content.SharedPreferences
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import helium314.keyboard.latin.R
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.settings.Setting
import helium314.keyboard.settings.dialogs.ListPickerDialog

@Composable
/** [items] are displayString to value */
fun <T: Any> ListPreference(
    setting: Setting,
    items: List<Pair<String, T>>,
    default: T,
    onDefault: (() -> Unit)? = null,
    footer: String? = null, // LiBoard: explanation with an example below the choices
    onChanged: (T) -> Unit = { }
) {
    var showPage by rememberSaveable { mutableStateOf(false) }
    val prefs = LocalContext.current.prefs()
    var current by remember { mutableStateOf(getPrefOfType(prefs, setting.key, default)) }
    val selected = items.firstOrNull { it.second == current }
    // LiBoard: like the iPhone – the chosen value in grey on the right, a page with check marks instead of an
    // Android pop-up (card 23328ec0)
    Preference(
        name = setting.title,
        description = selected?.first?.takeIf { it.length > 22 },
        onClick = { showPage = true }
    ) {
        selected?.first?.takeIf { it.length <= 22 }?.let { Text(it) }
        helium314.keyboard.latin.utils.NextScreenIcon()
    }
    if (showPage) {
        helium314.keyboard.settings.IosSubPage(setting.title, onBack = { showPage = false }) {
            helium314.keyboard.settings.IosGroup(footer = footer, items = items.map { item ->
                @Composable {
                    helium314.keyboard.settings.IosChoiceRow(item.first, item.second == current) {
                        if (item.second != current) {
                            putPrefOfType(prefs, setting.key, item.second)
                            current = item.second
                            onChanged(item.second)
                        }
                    }
                }
            })
            if (onDefault != null)
                helium314.keyboard.settings.IosGroup(items = listOf {
                    helium314.keyboard.settings.IosActionRow(androidx.compose.ui.res.stringResource(R.string.liboard_reset_to_default)) {
                        onDefault(); current = getPrefOfType(prefs, setting.key, default)
                    }
                })
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> getPrefOfType(prefs: SharedPreferences, key: String, default: T): T =
    when (default) {
        is String -> prefs.getString(key, default)
        is Int -> prefs.getInt(key, default)
        is Long -> prefs.getLong(key, default)
        is Float -> prefs.getFloat(key, default)
        is Boolean -> prefs.getBoolean(key, default)
        else -> throw IllegalArgumentException("unknown type ${default.javaClass}")
    } as T

private fun <T: Any> putPrefOfType(prefs: SharedPreferences, key: String, value: T) =
    prefs.edit {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Boolean -> putBoolean(key, value)
            else -> throw IllegalArgumentException("unknown type ${value.javaClass}")
        }
    }
