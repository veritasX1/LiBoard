package helium314.keyboard.latin

import android.text.InputType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Card f248ea29 (privacy first): what may never land in the clipboard history. */
class ClipboardPrivacyTest {
    private val text = InputType.TYPE_CLASS_TEXT
    private val password = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    private val webPassword = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
    private val pin = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

    @Test fun markedSensitiveIsNeverStored() = assertTrue(isSensitiveClip(true, text))
    // from API 24 the flag reads false (never null) without the extra – the password field must still count
    @Test fun passwordFieldCountsEvenWhenFlagIsFalse() {
        assertTrue(isSensitiveClip(false, password))
        assertTrue(isSensitiveClip(false, webPassword))
        assertTrue(isSensitiveClip(false, pin))
        assertTrue(isSensitiveClip(null, password))
    }
    @Test fun ordinaryTextIsNotSensitive() {
        assertFalse(isSensitiveClip(false, text))
        assertFalse(isSensitiveClip(null, text))
    }
}
