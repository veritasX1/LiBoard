// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.dialogs

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import helium314.keyboard.latin.R
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.previewDark
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun <T: Any> ReorderDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: (List<T>) -> Unit,
    items: List<T>,
    getKey: (T) -> Any, // actually it's not "Any", but "anything that can be stored in a bundle"
    displayItem: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    onNeutral: () -> Unit = { },
    neutralButtonText: String? = null,
) {
    var reorderableItems by remember(items) { mutableStateOf(items) }
    val listState = rememberLazyListState()

    val dragDropState = rememberReorderableLazyListState(listState) { from, to ->
        reorderableItems = reorderableItems.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }
    ThreeButtonAlertDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = { onConfirmed(reorderableItems) },
        onNeutral = { onDismissRequest(); onNeutral() },
        neutralButtonText = neutralButtonText,
        modifier = modifier,
        title = title,
        content = {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(reorderableItems, key = getKey) { item ->
                    ReorderableItem(
                        state = dragDropState,
                        key = getKey(item)
                    ) { dragging ->
                        val elevation by animateDpAsState(if (dragging) 4.dp else 0.dp)
                        Surface(shadowElevation = elevation) {
                            Row(
                                modifier = Modifier
                                    .longPressDraggableHandle()
                                    .heightIn(min = 36.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_drag_indicator),
                                    "Reorder",
                                    Modifier.padding(end = 6.dp),
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                )
                               displayItem(item)
                            }
                        }
                    }
                }
            }
        },
    )
}

@Preview
@Composable
private fun Preview() {
    Theme(previewDark) {
        ReorderDialog(
            onConfirmed = {},
            onDismissRequest = {},
            items = listOf(1, 2, 3),
            displayItem = { Text(it.toString(), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            getKey = { it.toString() }
        )
    }
}

/**
 * LiBoard: the same reorderable list as an iPhone-like page (card 23328ec0) – rows in a rounded group, the drag
 * handle on the right as in iOS edit mode, changes kept when going back, „Auf Standard zurücksetzen“ below.
 */
@Composable
fun <T: Any> ReorderPage(
    title: String,
    onBack: () -> Unit,
    onConfirmed: (List<T>) -> Unit,
    items: List<T>,
    getKey: (T) -> Any,
    displayItem: @Composable (T) -> Unit,
    footer: String? = null,
    onDefault: (() -> Unit)? = null,
) {
    var reorderableItems by remember(items) { mutableStateOf(items) }
    val listState = rememberLazyListState()
    val dragDropState = rememberReorderableLazyListState(listState) { from, to ->
        reorderableItems = reorderableItems.toMutableList().apply { add(to.index, removeAt(from.index)) }
    }
    helium314.keyboard.settings.IosSubPage(title, onBack = { onConfirmed(reorderableItems); onBack() }, scroll = false) {
        // grouped list like LI-GESTALTUNG.md: white cells with hairlines, the handle „≡“ on the right as in iOS edit mode
        LazyColumn(state = listState, modifier = Modifier.padding(top = 20.dp)) {
            itemsIndexed(reorderableItems, key = { _, it -> getKey(it) }) { index, item ->
                ReorderableItem(state = dragDropState, key = getKey(item)) { dragging ->
                    val elevation by animateDpAsState(if (dragging) 4.dp else 0.dp)
                    val last = index == reorderableItems.lastIndex
                    val shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        topStart = if (index == 0) 12.dp else 0.dp, topEnd = if (index == 0) 12.dp else 0.dp,
                        bottomStart = if (last) 12.dp else 0.dp, bottomEnd = if (last) 12.dp else 0.dp)
                    Surface(shadowElevation = elevation, color = helium314.keyboard.settings.iosCellColor(), shape = shape,
                        modifier = Modifier.padding(horizontal = 16.dp)) {
                        androidx.compose.foundation.layout.Column {
                            if (index > 0) androidx.compose.material3.HorizontalDivider(Modifier.padding(start = 16.dp), thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant)
                            Row(Modifier.heightIn(min = 44.dp).padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.weight(1f)) { displayItem(item) }
                                Text("≡", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.longPressDraggableHandle().padding(horizontal = 14.dp, vertical = 8.dp))
                            }
                        }
                    }
                }
            }
            if (footer != null || onDefault != null) item(key = "_footer") {
                androidx.compose.foundation.layout.Column {
                    if (footer != null) helium314.keyboard.settings.IosFooter(footer)
                    if (onDefault != null) helium314.keyboard.settings.IosGroup(items = listOf {
                        helium314.keyboard.settings.IosActionRow(stringResource(R.string.liboard_reset_to_default)) { onDefault(); onBack() }
                    })
                }
            }
        }
    }
}
