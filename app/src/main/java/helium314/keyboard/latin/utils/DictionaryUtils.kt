// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.latin.utils

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.core.content.edit
import helium314.keyboard.compat.locale
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.Links
import helium314.keyboard.latin.common.LocaleUtils
import helium314.keyboard.latin.common.LocaleUtils.constructLocale
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.settings.dialogs.ConfirmationDialog
import java.io.File
import java.util.Locale

fun getDictionaryLocales(context: Context): MutableSet<Locale> {
    val locales = HashSet<Locale>()

    // get cached dictionaries: extracted or user-added dictionaries
    DictionaryInfoUtils.getCacheDirectories(context).forEach { directory ->
        if (!hasAnythingOtherThanExtractedMainDictionary(directory)) return@forEach
        val locale = DictionaryInfoUtils.getWordListIdFromFileName(directory.name).constructLocale()
        locales.add(locale)
    }
    // get assets dictionaries
    val assetsDictionaryList = DictionaryInfoUtils.getAssetsDictionaryList(context)
    if (assetsDictionaryList != null) {
        for (dictionary in assetsDictionaryList) {
            locales.add(DictionaryInfoUtils.extractLocaleFromAssetsDictionaryFile(dictionary))
        }
    }
    return locales
}

@Composable
fun MissingDictionaryDialog(onDismissRequest: () -> Unit, locale: Locale) {
    val prefs = LocalContext.current.prefs()
    if (prefs.getBoolean(Settings.PREF_DONT_SHOW_MISSING_DICTIONARY_DIALOG, Defaults.PREF_DONT_SHOW_MISSING_DICTIONARY_DIALOG)) {
        onDismissRequest()
        return
    }
    // LiBoard: no download links – German, English and French are built in (card 212c0fa2)
    val annotatedString = AnnotatedString(stringResource(R.string.liboard_no_dictionary_message,
        locale.getDisplayName(LocalContext.current.resources.configuration.locale())))

    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        cancelButtonText = stringResource(R.string.dialog_close),
        onConfirmed = { prefs.edit { putBoolean(Settings.PREF_DONT_SHOW_MISSING_DICTIONARY_DIALOG, true) } },
        confirmButtonText = stringResource(R.string.no_dictionary_dont_show_again_button),
        content = { Text(annotatedString) }
    )
}

/** LiBoard: no links to downloadable dictionaries (card 212c0fa2) */
@Composable
fun createDictionaryTextAnnotated(locale: Locale): AnnotatedString = AnnotatedString("")

fun cleanUnusedMainDicts(context: Context) {
    val dictionaryDir = File(DictionaryInfoUtils.getWordListCacheDirectory(context))
    val dirs = dictionaryDir.listFiles() ?: return
    val usedLocaleLanguageTags = hashSetOf<String>()
    SubtypeSettings.getEnabledSubtypes().forEach { subtype ->
        val locale = subtype.locale()
        usedLocaleLanguageTags.add(locale.toLanguageTag())
    }
    SubtypeSettings.getAdditionalSubtypes().forEach { subtype ->
        getSecondaryLocales(subtype.extraValue).forEach { usedLocaleLanguageTags.add(it.toLanguageTag()) }
    }
    for (dir in dirs) {
        if (!dir.isDirectory) continue
        if (dir.name in usedLocaleLanguageTags) continue
        if (hasAnythingOtherThanExtractedMainDictionary(dir))
            continue
        dir.deleteRecursively()
    }
}

private fun hasAnythingOtherThanExtractedMainDictionary(dir: File) =
    dir.listFiles()?.any { it.name != DictionaryInfoUtils.MAIN_DICT_FILE_NAME } != false
