// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.preferences

import helium314.keyboard.settings.iosSwitchColors

import androidx.compose.foundation.layout.Row
import helium314.keyboard.settings.IosSwitch as Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.keyboard.internal.KeyboardIconsSet
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.Constants.Separators
import helium314.keyboard.latin.utils.getStringResourceOrName
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.settings.Setting
import helium314.keyboard.settings.dialogs.ReorderPage
import helium314.keyboard.settings.GetIconOrEmpty

@Composable
fun ReorderSwitchPreference(setting: Setting, default: String, footer: String? = null, pageTitle: String? = null) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    Preference(
        name = setting.title,
        onClick = { showDialog = true },
    ) { helium314.keyboard.latin.utils.NextScreenIcon() }
    if (showDialog) {
        val ctx = LocalContext.current
        val prefs = ctx.prefs()
        val items = prefs.getString(setting.key, default)!!.split(Separators.ENTRY).map {
            val both = it.split(Separators.KV)
            KeyAndState(both.first(), both.last().toBoolean())
        }
        ReorderPage(
            title = pageTitle ?: setting.title, // LiBoard: short page title like iOS
            onBack = { showDialog = false },
            onConfirmed = { reorderedItems ->
                val value = reorderedItems.joinToString(Separators.ENTRY) { it.name + Separators.KV + it.state }
                prefs.edit { putString(setting.key, value) }
                KeyboardSwitcher.getInstance().setThemeNeedsReload()
            },
            footer = footer ?: setting.description,
            onDefault = if (prefs.contains(setting.key)) { { prefs.edit { remove(setting.key) }; KeyboardSwitcher.getInstance().setThemeNeedsReload() } } else null,
            items = items,
            displayItem = { item ->
                var checked by rememberSaveable { mutableStateOf(item.state) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // LiBoard: an icon only where there is one – no empty 40 dp box in front of the text
                    if (KeyboardIconsSet.instance.iconIds[item.name.lowercase()] != null) KeyboardIconsSet.instance.GetIconOrEmpty(item.name)
                    val text = item.name.lowercase().getStringResourceOrName("", ctx)
                    val actualText = if (text != item.name.lowercase()) text
                        else item.name.lowercase().getStringResourceOrName("popup_keys_", ctx)
                    Text(actualText, Modifier.weight(1f))
                    Switch(
                        checked = checked,
                        onCheckedChange = { item.state = it; checked = it }
                    )
                }
            },
            getKey = { it.name }
        )
    }
}

private class KeyAndState(var name: String, var state: Boolean)
