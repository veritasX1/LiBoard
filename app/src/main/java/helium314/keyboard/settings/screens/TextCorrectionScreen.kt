// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.latin.R
import helium314.keyboard.latin.permissions.PermissionsUtil
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.JniUtils
import helium314.keyboard.latin.utils.Log
import helium314.keyboard.latin.utils.ToolbarMode
import helium314.keyboard.latin.utils.getActivity
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.latin.utils.NextScreenIcon
import helium314.keyboard.settings.SearchSettingsScreen
import helium314.keyboard.settings.Setting
import helium314.keyboard.settings.SettingsActivity
import helium314.keyboard.settings.SettingsDestination
import helium314.keyboard.settings.SettingsWithoutKey
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.settings.dialogs.ConfirmationDialog
import helium314.keyboard.settings.initPreview
import helium314.keyboard.settings.preferences.Preference
import helium314.keyboard.settings.preferences.SwitchPreference
import helium314.keyboard.settings.preferences.SwitchPreferenceWithEmojiDictWarning
import helium314.keyboard.latin.utils.previewDark
import androidx.core.content.edit
import helium314.keyboard.keyboard.internal.PopupKeySpec
import helium314.keyboard.settings.preferences.SliderPreference
import helium314.keyboard.settings.preferences.TextInputPreference

@Composable
fun TextCorrectionScreen(
    onClickBack: () -> Unit,
    advanced: Boolean = false, // LiBoard: expert options live on their own page "Erweitert" (card 115c26bc)
) {
    val prefs = LocalContext.current.prefs()
    val ctx = LocalContext.current
    val b = (LocalContext.current.getActivity() as? SettingsActivity)?.prefChanged?.collectAsState()
    if ((b?.value ?: 0) < 0)
        Log.v("irrelevant", "stupid way to trigger recomposition on preference change")
    val autocorrectEnabled = prefs.getBoolean(Settings.PREF_AUTO_CORRECTION, Defaults.PREF_AUTO_CORRECTION)
    val suggestionsVisible = Settings.readToolbarMode(prefs).let { it == ToolbarMode.SUGGESTION_STRIP || it == ToolbarMode.EXPANDABLE }
    val suggestionsEnabled = suggestionsVisible && prefs.getBoolean(Settings.PREF_SHOW_SUGGESTIONS, Defaults.PREF_SHOW_SUGGESTIONS)
    val gestureEnabled = JniUtils.sHaveGestureLib && prefs.getBoolean(Settings.PREF_GESTURE_INPUT, Defaults.PREF_GESTURE_INPUT)
    // LiBoard: the main page follows iPhone Settings > General > Keyboard – a few plain switches,
    // what the keyboard may learn from, and reset; everything else is under "Erweitert" (card 115c26bc)
    val items = if (!advanced) listOf(
        SettingsWithoutKey.EDIT_PERSONAL_DICTIONARY,
        R.string.liboard_all_keyboards,
        Settings.PREF_AUTO_CAP,
        Settings.PREF_AUTO_CORRECTION,
        if (suggestionsVisible) Settings.PREF_SHOW_SUGGESTIONS else null,
        Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD,
        if (suggestionsEnabled || autocorrectEnabled) Settings.PREF_SUGGEST_EMOJIS else null,
        R.string.liboard_learn_from,
        Settings.PREF_KEY_USE_PERSONALIZED_DICTS,
        Settings.PREF_SUGGEST_CLIPBOARD_CONTENT,
        Settings.PREF_USE_CONTACTS,
        Settings.PREF_USE_APPS,
        // LiBoard: no spell checker service, so no setting for it (card 1feb0603)
        // LiBoard: like iOS – corrections only in the suggestion strip; the system's red underlines
        // bring Android's own menu, so we point to where it can be switched off (card 943fc7c2)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && systemSpellCheckerOn(ctx))
            SettingsWithoutKey.SYSTEM_SPELL_CHECKER else null,
        SettingsWithoutKey.CORRECTION_ADVANCED,
        R.string.liboard_reset_category,
        SettingsWithoutKey.RESET_KEYBOARD_DICTIONARY,
    ) else listOf(
        R.string.settings_category_correction,
        if (autocorrectEnabled) Settings.PREF_AUTO_CORRECT_CONFIDENCE else null,
        if (autocorrectEnabled) Settings.PREF_BACKSPACE_REVERTS_AUTOCORRECT else null,
        if (autocorrectEnabled) Settings.PREF_AUTOCORRECT_SHORTCUTS else null,
        if (autocorrectEnabled) Settings.PREF_MORE_AUTO_CORRECTION else null,
        if (autocorrectEnabled) Settings.PREF_AUTOCORRECT_CAPITALIZED_SUGGESTION else null,
        Settings.PREF_BLOCK_POTENTIALLY_OFFENSIVE,
        R.string.settings_category_space,
        Settings.PREF_AUTOSPACE_AFTER_PUNCTUATION,
        Settings.PREF_AUTOSPACE_AFTER_SUGGESTION,
        if (gestureEnabled) Settings.PREF_AUTOSPACE_BEFORE_GESTURE_TYPING else null,
        if (gestureEnabled) Settings.PREF_AUTOSPACE_AFTER_GESTURE_TYPING else null,
        Settings.PREF_SHIFT_REMOVES_AUTOSPACE,
        R.string.settings_category_suggestions,
        Settings.PREF_BIGRAM_PREDICTIONS,
        if (suggestionsEnabled) Settings.PREF_CENTER_SUGGESTION_TEXT_TO_ENTER else null,
        if (suggestionsEnabled || autocorrectEnabled) Settings.PREF_INLINE_EMOJI_SEARCH else null,
        Settings.PREF_SUGGEST_PUNCTUATION,
        if (prefs.getBoolean(Settings.PREF_SUGGEST_PUNCTUATION, Defaults.PREF_SUGGEST_PUNCTUATION))
            Settings.PREF_PUNCTUATION_SUGGESTIONS else null,
        if (suggestionsEnabled) Settings.PREF_ALWAYS_SHOW_SUGGESTIONS else null,
        if (suggestionsEnabled && prefs.getBoolean(Settings.PREF_ALWAYS_SHOW_SUGGESTIONS, Defaults.PREF_ALWAYS_SHOW_SUGGESTIONS))
            Settings.PREF_ALWAYS_SHOW_SUGGESTIONS_EXCEPT_WEB_TEXT else null,
        if (prefs.getBoolean(Settings.PREF_KEY_USE_PERSONALIZED_DICTS, Defaults.PREF_KEY_USE_PERSONALIZED_DICTS))
            Settings.PREF_ADD_TO_PERSONAL_DICTIONARY else null,
    )
    SearchSettingsScreen(
        onClickBack = onClickBack,
        title = stringResource(if (advanced) R.string.liboard_advanced else R.string.settings_screen_correction),
        settings = items
    )
}

fun createCorrectionSettings(context: Context) = listOf(
    Setting(context, SettingsWithoutKey.EDIT_PERSONAL_DICTIONARY, R.string.edit_personal_dictionary) {
        Preference(
            name = stringResource(R.string.edit_personal_dictionary),
            onClick = { SettingsDestination.navigateTo(SettingsDestination.PersonalDictionaries) },
        ) { NextScreenIcon() }
    },
    Setting(context, Settings.PREF_BLOCK_POTENTIALLY_OFFENSIVE,
        R.string.prefs_block_potentially_offensive_title, R.string.prefs_block_potentially_offensive_summary
    ) {
        SwitchPreference(it, Defaults.PREF_BLOCK_POTENTIALLY_OFFENSIVE)
    },
    Setting(context, Settings.PREF_AUTO_CORRECTION,
        R.string.autocorrect, R.string.auto_correction_summary
    ) {
        SwitchPreference(it, Defaults.PREF_AUTO_CORRECTION)
    },
    Setting(context, Settings.PREF_MORE_AUTO_CORRECTION,
        R.string.more_autocorrect, R.string.more_autocorrect_summary
    ) {
        SwitchPreference(it, Defaults.PREF_MORE_AUTO_CORRECTION)
    },
    Setting(context, Settings.PREF_AUTOCORRECT_SHORTCUTS,
        R.string.auto_correct_shortcuts, R.string.auto_correct_shortcuts_summary
    ) {
        SwitchPreference(it, Defaults.PREF_AUTOCORRECT_SHORTCUTS)
    },
    Setting(context, Settings.PREF_AUTOCORRECT_CAPITALIZED_SUGGESTION,
        R.string.auto_correct_capitalized_suggestions, R.string.auto_correct_capitalized_suggestions_description
    ) {
        SwitchPreference(it, Defaults.PREF_AUTOCORRECT_CAPITALIZED_SUGGESTION)
    },
    Setting(context, Settings.PREF_AUTO_CORRECT_CONFIDENCE, R.string.auto_correction_confidence) { setting ->
        SliderPreference(
            name = setting.title,
            key = setting.key,
            default = Defaults.PREF_AUTO_CORRECT_CONFIDENCE,
            range = 0f..1f,
            description = {
                val text = when (it) {
                    in 0f..0.40f -> stringResource(R.string.auto_correction_threshold_mode_modest)
                    in 0f..0.80f -> stringResource(R.string.auto_correction_threshold_mode_aggressive)
                    else -> stringResource(R.string.auto_correction_threshold_mode_very_aggressive)
                }
                text // LiBoard: no raw number, as in iOS (card 115c26bc)
            }
        )
    },
    Setting(context, Settings.PREF_BACKSPACE_REVERTS_AUTOCORRECT, R.string.backspace_reverts_autocorrect) {
        SwitchPreference(it, Defaults.PREF_BACKSPACE_REVERTS_AUTOCORRECT)
    },
    Setting(context, Settings.PREF_AUTO_CAP,
        R.string.auto_cap, R.string.auto_cap_summary
    ) {
        SwitchPreference(it, Defaults.PREF_AUTO_CAP)
    },
    Setting(context, Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD,
        R.string.use_double_space_period, R.string.use_double_space_period_summary
    ) {
        SwitchPreference(it, Defaults.PREF_KEY_USE_DOUBLE_SPACE_PERIOD)
    },
    Setting(context, Settings.PREF_AUTOSPACE_AFTER_PUNCTUATION,
        R.string.autospace_after_punctuation, R.string.autospace_after_punctuation_summary
    ) {
        SwitchPreference(it, Defaults.PREF_AUTOSPACE_AFTER_PUNCTUATION)
    },
    Setting(context, Settings.PREF_AUTOSPACE_AFTER_SUGGESTION, R.string.autospace_after_suggestion) {
        SwitchPreference(it, Defaults.PREF_AUTOSPACE_AFTER_SUGGESTION)
    },
    Setting(context, Settings.PREF_AUTOSPACE_AFTER_GESTURE_TYPING, R.string.autospace_after_gesture_typing) {
        SwitchPreference(it, Defaults.PREF_AUTOSPACE_AFTER_GESTURE_TYPING)
    },
    Setting(context, Settings.PREF_AUTOSPACE_BEFORE_GESTURE_TYPING, R.string.autospace_before_gesture_typing) {
        SwitchPreference(it, Defaults.PREF_AUTOSPACE_BEFORE_GESTURE_TYPING)
    },
    Setting(context, Settings.PREF_SHIFT_REMOVES_AUTOSPACE, R.string.shift_removes_autospace, R.string.shift_removes_autospace_summary) {
        SwitchPreference(it, Defaults.PREF_SHIFT_REMOVES_AUTOSPACE)
    },
    Setting(context, Settings.PREF_SHOW_SUGGESTIONS,
        R.string.prefs_show_suggestions, R.string.prefs_show_suggestions_summary
    ) {
        SwitchPreference(it, Defaults.PREF_SHOW_SUGGESTIONS)
    },
    Setting(context, Settings.PREF_ALWAYS_SHOW_SUGGESTIONS,
        R.string.prefs_always_show_suggestions, R.string.prefs_always_show_suggestions_summary
    ) {
        SwitchPreference(it, Defaults.PREF_ALWAYS_SHOW_SUGGESTIONS)
    },
    Setting(context, Settings.PREF_ALWAYS_SHOW_SUGGESTIONS_EXCEPT_WEB_TEXT,
        R.string.prefs_always_show_suggestions_except_web_text, R.string.prefs_always_show_suggestions_except_web_text_summary
    ) {
        SwitchPreference(it, Defaults.PREF_ALWAYS_SHOW_SUGGESTIONS_EXCEPT_WEB_TEXT)
    },
    Setting(context, Settings.PREF_KEY_USE_PERSONALIZED_DICTS,
        R.string.use_personalized_dicts, R.string.use_personalized_dicts_summary
    ) { setting ->
        var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
        SwitchPreference(setting, Defaults.PREF_KEY_USE_PERSONALIZED_DICTS,
            allowCheckedChange = {
                showConfirmDialog = !it
                it
            }
        )
        if (showConfirmDialog) {
            val prefs = LocalContext.current.prefs()
            ConfirmationDialog(
                onDismissRequest = { showConfirmDialog = false },
                onConfirmed = {
                    prefs.edit { putBoolean(setting.key, false) }
                },
                content = { Text(stringResource(R.string.disable_personalized_dicts_message)) }
            )
        }

    },
    Setting(context, Settings.PREF_BIGRAM_PREDICTIONS,
        R.string.bigram_prediction, R.string.bigram_prediction_summary
    ) {
        SwitchPreference(it, Defaults.PREF_BIGRAM_PREDICTIONS) { KeyboardSwitcher.getInstance().setThemeNeedsReload() }
    },
    Setting(context, Settings.PREF_SUGGEST_PUNCTUATION, R.string.suggest_punctuation, R.string.suggest_punctuation_summary
    ) {
        SwitchPreference(it, Defaults.PREF_SUGGEST_PUNCTUATION) { KeyboardSwitcher.getInstance().setThemeNeedsReload() }
    },
    Setting(context, Settings.PREF_PUNCTUATION_SUGGESTIONS, R.string.custom_punctuation_suggestions) { setting ->
        val defaultSpecs = PopupKeySpec.splitKeySpecs(stringResource(R.string.suggested_punctuations))
            ?.joinToString(" ") { if (it.length > 1 && it.startsWith('\\')) it.substring(1) else it }
        TextInputPreference(setting, defaultSpecs ?: "")
    },
    Setting(context, Settings.PREF_CENTER_SUGGESTION_TEXT_TO_ENTER,
        R.string.center_suggestion_text_to_enter, R.string.center_suggestion_text_to_enter_summary
    ) {
        SwitchPreference(it, Defaults.PREF_CENTER_SUGGESTION_TEXT_TO_ENTER)
    },
    Setting(context, Settings.PREF_SUGGEST_CLIPBOARD_CONTENT,
        R.string.suggest_clipboard_content, R.string.suggest_clipboard_content_summary
    ) {
        SwitchPreference(it, Defaults.PREF_SUGGEST_CLIPBOARD_CONTENT)
    },
    Setting(context, Settings.PREF_USE_CONTACTS,
        R.string.use_contacts_dict, R.string.use_contacts_dict_summary
    ) { setting ->
        val activity = LocalContext.current.getActivity() ?: return@Setting
        var granted by remember { mutableStateOf(PermissionsUtil.checkAllPermissionsGranted(activity, Manifest.permission.READ_CONTACTS)) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            granted = it
            if (granted)
                activity.prefs().edit { putBoolean(setting.key, true) }
        }
        SwitchPreference(setting, Defaults.PREF_USE_CONTACTS,
            allowCheckedChange = {
                if (it && !granted) {
                    launcher.launch(Manifest.permission.READ_CONTACTS)
                    false
                } else true
            }
        )
    },
    Setting(context, Settings.PREF_USE_APPS,
        R.string.use_apps_dict, R.string.use_apps_dict_summary
    ) { setting ->
        SwitchPreference(setting, Defaults.PREF_USE_APPS)
    },
    Setting(
        context, Settings.PREF_SUGGEST_EMOJIS, R.string.suggest_emojis, R.string.suggest_emojis_summary
    ) {
        SwitchPreferenceWithEmojiDictWarning(it, Defaults.PREF_SUGGEST_EMOJIS)
    },
    Setting(
        context, Settings.PREF_INLINE_EMOJI_SEARCH, R.string.inline_emoji_search, R.string.inline_emoji_search_summary) {
        SwitchPreferenceWithEmojiDictWarning(it, Defaults.PREF_INLINE_EMOJI_SEARCH)
    },
    Setting(context, Settings.PREF_ADD_TO_PERSONAL_DICTIONARY,
        R.string.add_to_personal_dictionary, R.string.add_to_personal_dictionary_summary
    ) {
        SwitchPreference(it, Defaults.PREF_ADD_TO_PERSONAL_DICTIONARY)
    },
    Setting(context, Settings.PREF_SPELLCHECK_SUGGEST,
        R.string.spell_check_suggestions, R.string.spell_check_suggestions_summary
    ) {
        SwitchPreference(it, Defaults.PREF_SPELLCHECK_SUGGEST)
    },
    // LiBoard: the system's spell checker underlines words in red and opens Android's menu on tap –
    // a keyboard cannot change that, so we only explain it and open the system setting (card 943fc7c2)
    Setting(context, SettingsWithoutKey.SYSTEM_SPELL_CHECKER, R.string.liboard_system_spell_checker) {
        val ctx = LocalContext.current
        Preference(
            name = it.title,
            description = stringResource(R.string.liboard_system_spell_checker_summary),
            onClick = { openSpellCheckerSettings(ctx) },
        ) { NextScreenIcon() }
    },
    Setting(context, SettingsWithoutKey.CORRECTION_ADVANCED, R.string.liboard_advanced) {
        Preference(
            name = it.title,
            onClick = { SettingsDestination.navigateTo(SettingsDestination.TextCorrectionAdvanced) },
        ) { NextScreenIcon() }
    },
    // LiBoard: like iOS "Reset Keyboard Dictionary" – removes only learned words, not own words
    // or text replacements (card 943fc7c2)
    Setting(context, SettingsWithoutKey.RESET_KEYBOARD_DICTIONARY, R.string.liboard_reset_dictionary) {
        val ctx = LocalContext.current
        var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
        Preference(name = it.title, destructive = true, onClick = { showConfirmDialog = true })
        if (showConfirmDialog) {
            ConfirmationDialog(
                onDismissRequest = { showConfirmDialog = false },
                onConfirmed = { resetKeyboardDictionary(ctx) },
                title = { Text(stringResource(R.string.liboard_reset_dictionary)) },
                content = { Text(stringResource(R.string.liboard_reset_dictionary_message)) },
                confirmButtonText = stringResource(R.string.liboard_reset_dictionary_confirm),
            )
        }
    },
)

private fun systemSpellCheckerOn(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        context.getSystemService(android.view.textservice.TextServicesManager::class.java)?.isSpellCheckerEnabled == true

private fun openSpellCheckerSettings(context: Context) {
    // no public action for this page; fall back to the language & input settings
    val direct = android.content.Intent().setClassName("com.android.settings", "com.android.settings.Settings\$SpellCheckersSettingsActivity")
    val fallback = android.content.Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS)
    for (intent in listOf(direct, fallback)) {
        try {
            context.startActivity(intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (_: android.content.ActivityNotFoundException) { }
    }
}

private fun resetKeyboardDictionary(context: Context) {
    helium314.keyboard.latin.personalization.PersonalizationHelper.removeAllUserHistoryDictionaries(context)
    // the keyboard reloads its dictionaries and starts with an empty history
    context.sendBroadcast(android.content.Intent(helium314.keyboard.dictionarypack.DictionaryPackConstants.NEW_DICTIONARY_INTENT_ACTION)
        .setPackage(context.packageName))
}

@Preview
@Composable
private fun PreferencePreview() {
    initPreview(LocalContext.current)
    Theme(previewDark) {
        Surface {
            TextCorrectionScreen(onClickBack = {  })
        }
    }
}
