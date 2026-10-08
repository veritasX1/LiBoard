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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
                .clip(RoundedCornerShape(12.dp)) // LI-GESTALTUNG.md: 12 dp like LiMail
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

/**
 * The switch of all Li apps – same size, colours and motion as LiMail's SwitchRow (Olaf 08.10.: „Einheitlichkeit
 * wie Apple“, card ec15db7a): track 51 × 31 dp, green when on, system fill when off, a white 27 dp knob with a
 * shadow that only slides (never grows). Same call shape as Material's Switch so it can replace it everywhere;
 * [colors] is ignored on purpose.
 */
@Composable
fun IosSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @Suppress("UNUSED_PARAMETER") colors: SwitchColors? = null,
) {
    val dark = isDarkSettings()
    val knob by androidx.compose.animation.core.animateFloatAsState(if (checked) 1f else 0f, label = "switch")
    val track = if (checked) (if (dark) Ios.greenDark else Ios.green)
        else if (dark) Color(0x3D767680) else Color(0x1F767680) // = LiMail palette().fill
    var m = modifier.size(width = 51.dp, height = 31.dp).clip(androidx.compose.foundation.shape.CircleShape).background(track)
    if (onCheckedChange != null)
        m = m.then(Modifier.toggleable(value = checked, enabled = enabled, role = androidx.compose.ui.semantics.Role.Switch,
            onValueChange = onCheckedChange))
    Box(m.then(if (enabled) Modifier else Modifier.alpha(0.5f))) {
        Box(Modifier.padding(2.dp).offset(x = (20 * knob).dp).size(27.dp)
            .shadow(2.dp, androidx.compose.foundation.shape.CircleShape)
            .clip(androidx.compose.foundation.shape.CircleShape).background(Color.White))
    }
}

/**
 * A pushed settings page like on the iPhone (card 23328ec0): full screen, grouped background, „‹ Zurück“ on the
 * left, the title in the middle – used instead of Android pop-up dialogs for choices and sliders.
 */
@Composable
fun IosSubPage(title: String, onBack: () -> Unit, scroll: Boolean = true, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onBack,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight().background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.Companion.safeDrawing)
        ) {
            // the title stays centred: as much room on each side as the back button takes (iOS) – shortened only when needed
            var backWidth by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
            val density = androidx.compose.ui.platform.LocalDensity.current
            Box(Modifier.fillMaxWidth().height(44.dp)) {
                Box(Modifier.align(Alignment.CenterStart).onGloballyPositioned { backWidth = it.size.width }) {
                    helium314.keyboard.latin.utils.BackButton(
                        LocalPageTitle.current ?: androidx.compose.ui.res.stringResource(R.string.liboard_back), onBack)
                }
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = with(density) { backWidth.toDp() } + 8.dp))
            }
            Column(Modifier.fillMaxWidth().then(if (scroll) Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()) else Modifier.weight(1f)), content = content)
        }
    }
}

/** One choice of an [IosSubPage]: text on the left, the check mark on the right (Apple HIG). */
@Composable
fun IosChoiceRow(text: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable(onClick = onClick).padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 17.sp, modifier = Modifier.weight(1f).padding(vertical = 11.dp))
        IosCheck(selected)
    }
}

/** LiBoard: title of the previous page for the back button („‹ Erweitert“) and of the current page for pushed
 *  sub pages – as on the iPhone and in LiMail (card 23328ec0, LI-GESTALTUNG.md). */
val LocalBackTitle = androidx.compose.runtime.compositionLocalOf<String?> { null }
val LocalPageTitle = androidx.compose.runtime.compositionLocalOf<String?> { null }

@Composable
fun routeTitle(route: String?): String? {
    val res = when {
        route == null -> return null
        route == "settings" -> R.string.english_ime_name
        route == "about" -> R.string.settings_screen_about
        route == "typing_test" -> R.string.liboard_typing_test
        route == "text_correction" -> R.string.settings_screen_correction
        route == "text_correction_advanced" -> R.string.liboard_advanced
        route == "developer" -> R.string.liboard_developer
        route == "preferences" -> R.string.settings_screen_preferences
        route == "toolbar" -> R.string.settings_screen_toolbar
        route == "gesture_typing" -> R.string.settings_screen_gesture
        route == "advanced" -> R.string.settings_screen_advanced
        route == "appearance" || route.startsWith("colors") -> R.string.settings_screen_appearance
        route.startsWith("personal_dictionar") -> R.string.edit_personal_dictionary
        route == "languages" || route.startsWith("subtype") || route == "layouts" -> R.string.language_and_layouts_title
        route == "dictionaries" -> R.string.dictionary_settings_category
        else -> R.string.liboard_back
    }
    return androidx.compose.ui.res.stringResource(res)
}

/** The slider of all Li apps (iOS look, card 23328ec0): thin 4 dp track, blue left of the knob, system fill right,
 *  a white 28 dp knob with a shadow. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun IosSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val dark = isDarkSettings()
    val blue = MaterialTheme.colorScheme.primary
    val fill = if (dark) Color(0x3D767680) else Color(0x1F767680)
    androidx.compose.material3.Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        thumb = {
            Box(Modifier.size(28.dp).shadow(3.dp, androidx.compose.foundation.shape.CircleShape)
                .clip(androidx.compose.foundation.shape.CircleShape).background(Color.White))
        },
        track = { state ->
            val span = state.valueRange.endInclusive - state.valueRange.start
            val fraction = if (span == 0f) 0f else ((state.value - state.valueRange.start) / span).coerceIn(0f, 1f)
            androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))) {
                if (fraction > 0f) Box(Modifier.weight(fraction).fillMaxHeight().background(blue))
                if (fraction < 1f) Box(Modifier.weight(1f - fraction).fillMaxHeight().background(fill))
            }
        },
    )
}

/** A blue action row like iOS („Auf Standard zurücksetzen“). */
@Composable
fun IosActionRow(text: String, onClick: () -> Unit) {
    Text(text, color = MaterialTheme.colorScheme.primary, fontSize = 17.sp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 11.dp))
}

/**
 * Text entry as an iPhone-like page (card 23328ec0): one field in a group, an explanation below, „Auf Standard
 * zurücksetzen“ in blue. The text is kept when going back, if valid.
 */
@Composable
fun IosTextPage(
    title: String, initial: String, onBack: () -> Unit, onSave: (String) -> Unit,
    footer: String? = null, onDefault: (() -> Unit)? = null, isValid: (String) -> Boolean = { true },
    placeholder: String? = null, // shown while empty, e.g. what applies by default
) {
    var text by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(initial) }
    val valid = isValid(text)
    IosSubPage(title, onBack = { if (valid && text != initial) onSave(text); onBack() }) {
        IosGroup(footer = footer, items = listOf {
            androidx.compose.foundation.text.BasicTextField(
                value = text, onValueChange = { text = it }, singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 17.sp,
                    color = if (valid) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                decorationBox = { field ->
                    Box {
                        if (text.isEmpty() && placeholder != null)
                            Text(placeholder, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        field()
                    }
                },
            )
        })
        if (onDefault != null)
            IosGroup(items = listOf {
                IosActionRow(androidx.compose.ui.res.stringResource(R.string.liboard_reset_to_default)) { onDefault(); onBack() }
            })
    }
}
