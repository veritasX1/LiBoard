// SPDX-License-Identifier: GPL-3.0-only
// LiBoard: building blocks for settings that follow Apple's Human Interface Guidelines
// (inset grouped lists, section headers and footers, symbols on coloured tiles, iOS switches).
package helium314.keyboard.settings

import androidx.annotation.DrawableRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helium314.keyboard.latin.R

object Ios {
    val blue = Color(0xFF007AFF)
    val blueDark = Color(0xFF0A84FF)
    val green = Color(0xFF34C759)
    val greenDark = Color(0xFF30D158)

    // tile colours of the settings symbols, as in the iOS Settings app
    val gray = Color(0xFF8E8E93)
    val indigo = Color(0xFF5856D6)
    val orange = Color(0xFFFF9500)
    val pink = Color(0xFFFF2D55)
    val purple = Color(0xFFAF52DE)
    val red = Color(0xFFFF3B30)
    val teal = Color(0xFF30B0C7)
    val yellow = Color(0xFFFFCC00)
    val brown = Color(0xFFA2845E)
}

/** Whether the settings currently use the dark appearance. */
@Composable
fun isDarkSettings() = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** Background of a grouped cell (secondarySystemGroupedBackground). */
@Composable
fun iosCellColor() = MaterialTheme.colorScheme.surfaceContainerHigh

@Composable
fun iosSwitchColors(): SwitchColors {
    val dark = isDarkSettings()
    val off = if (dark) Color(0xFF39393D) else Color(0xFFE9E9EA)
    return SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = if (dark) Ios.greenDark else Ios.green,
        checkedBorderColor = Color.Transparent,
        checkedIconColor = Color.Transparent,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = off,
        uncheckedBorderColor = Color.Transparent,
        uncheckedIconColor = Color.Transparent,
    )
}

/** Small grey section title above a group, upper case as in iOS Settings. */
@Composable
fun IosHeader(text: String) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 22.dp, bottom = 7.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        letterSpacing = 0.sp,
    )
}

/** Explanation below a group. */
@Composable
fun IosFooter(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 7.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        lineHeight = 17.sp,
    )
}

/**
 * Inset grouped list: rows on a rounded card, separated by hairlines that start at the text.
 * [inset] is where separators begin (54 dp leaves room for a symbol tile).
 */
@Composable
fun IosGroup(
    header: String? = null,
    footer: String? = null,
    inset: Int = 16,
    items: List<@Composable () -> Unit>,
) {
    if (items.isEmpty()) return
    Column(Modifier.fillMaxWidth()) {
        if (header != null) IosHeader(header)
        else Box(Modifier.padding(top = 20.dp))
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iosCellColor())
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0)
                    HorizontalDivider(
                        Modifier.padding(start = inset.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                item()
            }
        }
        if (footer != null) IosFooter(footer)
    }
}

/** Settings symbol: white glyph on a coloured rounded square (29 dp, like iOS Settings). */
@Composable
fun IosIconTile(@DrawableRes icon: Int, color: Color = tileColorFor(icon)) {
    val ctx = LocalContext.current
    if (ctx.resources.getResourceTypeName(icon) == "mipmap") {
        IconOrImage(icon, null, 29)  // app icons keep their own look
        return
    }
    Box(
        Modifier.size(29.dp).clip(RoundedCornerShape(7.dp)).background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icon), null, Modifier.size(19.dp), tint = Color.White)
    }
}

fun tileColorFor(@DrawableRes icon: Int): Color = when (icon) {
    R.drawable.ic_settings_languages -> Ios.blue
    R.drawable.ic_settings_preferences -> Ios.gray
    R.drawable.ic_settings_appearance -> Ios.indigo
    R.drawable.ic_settings_toolbar -> Ios.teal
    R.drawable.ic_settings_gesture -> Ios.purple
    R.drawable.ic_settings_correction -> Ios.green
    R.drawable.ic_settings_layout -> Ios.orange
    R.drawable.ic_dictionary -> Ios.brown
    R.drawable.ic_settings_advanced -> Ios.gray
    R.drawable.ic_settings_about -> Ios.blue
    R.drawable.ic_settings_typing_test -> Ios.pink
    else -> Ios.gray
}

/** Section for screens that lay out their own content: grey title, content on a rounded cell. */
@Composable
fun IosSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 7.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(iosCellColor())
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) { content() }
    }
}

/** Replaces a radio button: a checkmark in the accent colour for the chosen option, as in iOS menus. */
@Composable
fun IosCheck(selected: Boolean, onClick: (() -> Unit)? = null) {
    Box(
        Modifier.size(28.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (selected)
            Text("✓", color = MaterialTheme.colorScheme.primary, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
    }
}
