/*
 * Copyright (C) 2013 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */
package helium314.keyboard.keyboard.emoji

import android.content.res.Resources
import android.view.View
import helium314.keyboard.keyboard.internal.KeyboardParams
import helium314.keyboard.latin.R
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.ResourceUtils

/** LiBoard: iOS-like emoji view — search field, category title, emoji band, category bar. */
internal class EmojiLayoutParams(res: Resources) {
    /** Whole emoji view: the keyboard plus the strip above it, which is hidden while emojis are shown. */
    val totalHeight: Int
    val searchFieldHeight: Int
    val titleHeight: Int
    val emojiKeyboardHeight: Int
    val bottomRowKeyboardHeight: Int

    init {
        val sv = Settings.getValues()
        val defaultKeyboardHeight = ResourceUtils.getSecondaryKeyboardHeight(res, sv)
        val stripHeight = if (sv.isSecondaryStripVisible) res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height) else 0
        totalHeight = defaultKeyboardHeight + stripHeight

        val keyVerticalGap = (res.getFraction(R.fraction.config_key_vertical_gap_holo,
            defaultKeyboardHeight, defaultKeyboardHeight) * sv.mKeyGapScale).toInt()
        val bottomPadding = (res.getFraction(R.fraction.config_keyboard_bottom_padding_holo,
            defaultKeyboardHeight, defaultKeyboardHeight) * sv.mBottomPaddingScale).toInt()
        val topPadding = res.getFraction(R.fraction.config_keyboard_top_padding_holo,
            defaultKeyboardHeight, defaultKeyboardHeight).toInt()

        val rowCount = KeyboardParams.DEFAULT_KEYBOARD_ROWS + if (sv.mShowsNumberRow) 1 else 0
        bottomRowKeyboardHeight = (defaultKeyboardHeight - bottomPadding - topPadding) / rowCount - keyVerticalGap / 2

        val density = res.displayMetrics.density
        searchFieldHeight = (36 * density).toInt()
        titleHeight = (22 * density).toInt()
        val searchMarginTop = (6 * density).toInt()
        emojiKeyboardHeight = totalHeight - searchMarginTop - searchFieldHeight - titleHeight - bottomRowKeyboardHeight - bottomPadding
    }

    fun setHeight(v: View, height: Int) {
        val lp = v.layoutParams
        lp.height = height
        v.layoutParams = lp
    }
}
