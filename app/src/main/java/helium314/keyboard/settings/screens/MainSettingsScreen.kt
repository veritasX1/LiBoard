// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import helium314.keyboard.latin.R
import helium314.keyboard.latin.utils.JniUtils
import helium314.keyboard.latin.utils.SubtypeLocaleUtils.displayName
import helium314.keyboard.latin.utils.SubtypeSettings
import helium314.keyboard.latin.utils.NextScreenIcon
import helium314.keyboard.settings.SearchSettingsScreen
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.settings.initPreview
import helium314.keyboard.settings.preferences.Preference
import helium314.keyboard.latin.utils.previewDark
import helium314.keyboard.settings.IosGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp

@Composable
fun MainSettingsScreen(
    onClickAbout: () -> Unit,
    onClickTextCorrection: () -> Unit,
    onClickPreferences: () -> Unit,
    onClickToolbar: () -> Unit,
    onClickGestureTyping: () -> Unit,
    onClickDataGathering: () -> Unit,
    onClickAdvanced: () -> Unit,
    onClickAppearance: () -> Unit,
    onClickLanguage: () -> Unit,
    onClickLayouts: () -> Unit,
    onClickDictionaries: () -> Unit,
    onClickBack: () -> Unit,
    onClickTypingTest: () -> Unit = {},
) {
    SearchSettingsScreen(
        onClickBack = onClickBack,
        title = stringResource(R.string.english_ime_name), // LiBoard: short large title as in iOS Settings
        settings = emptyList(),
    ) {
        val enabledSubtypes = SubtypeSettings.getEnabledSubtypes(true)
        Scaffold(contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)) { innerPadding ->
            Column(
                Modifier.verticalScroll(rememberScrollState()).then(Modifier.padding(innerPadding))
            ) {
                // LiBoard: grouped like the iOS Settings app (Apple HIG); the typing test is not an
                // iPhone setting, so it lives at the end of "Erweitert" (Olaf 08.10.)
                IosGroup(inset = 59, items = listOf(
                    { Preference(
                        name = stringResource(R.string.language_and_layouts_title),
                        description = enabledSubtypes.joinToString(", ") { it.displayName() },
                        onClick = onClickLanguage,
                        icon = R.drawable.ic_settings_languages
                    ) { NextScreenIcon() } },
                    { Preference(
                        name = stringResource(R.string.settings_screen_secondary_layouts),
                        onClick = onClickLayouts,
                        icon = R.drawable.ic_settings_layout
                    ) { NextScreenIcon() } },
                    { Preference(
                        name = stringResource(R.string.dictionary_settings_category),
                        onClick = onClickDictionaries,
                        icon = R.drawable.ic_dictionary
                    ) { NextScreenIcon() } },
                ))
                IosGroup(inset = 59, items = buildList {
                    add @Composable { Preference(
                        name = stringResource(R.string.settings_screen_preferences),
                        onClick = onClickPreferences,
                        icon = R.drawable.ic_settings_preferences
                    ) { NextScreenIcon() } }
                    add @Composable { Preference(
                        name = stringResource(R.string.settings_screen_correction),
                        onClick = onClickTextCorrection,
                        icon = R.drawable.ic_settings_correction
                    ) { NextScreenIcon() } }
                    if (JniUtils.sHaveGestureLib)
                        add @Composable { Preference(
                            name = stringResource(R.string.settings_screen_gesture),
                            onClick = onClickGestureTyping,
                            icon = R.drawable.ic_settings_gesture
                        ) { NextScreenIcon() } }
                    // LiBoard: privacy first – no gesture data gathering, not even opt-in (onClickDataGathering unused)
                    add @Composable { Preference(
                        name = stringResource(R.string.settings_screen_toolbar),
                        onClick = onClickToolbar,
                        icon = R.drawable.ic_settings_toolbar
                    ) { NextScreenIcon() } }
                })
                IosGroup(inset = 59, items = listOf(
                    { Preference(
                        name = stringResource(R.string.settings_screen_appearance),
                        onClick = onClickAppearance,
                        icon = R.drawable.ic_settings_appearance
                    ) { NextScreenIcon() } },
                    { Preference(
                        name = stringResource(R.string.settings_screen_advanced),
                        onClick = onClickAdvanced,
                        icon = R.drawable.ic_settings_advanced
                    ) { NextScreenIcon() } },
                ))
                IosGroup(
                    inset = 59,
                    footer = stringResource(R.string.liboard_based_on_heliboard),
                    items = listOf(
                        { Preference(
                            name = stringResource(R.string.settings_screen_about),
                            onClick = onClickAbout,
                            icon = R.drawable.ic_settings_about
                        ) { NextScreenIcon() } },
                    )
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewScreen() {
    initPreview(LocalContext.current)
    Theme(previewDark) {
        Surface {
            MainSettingsScreen({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
        }
    }
}
