package com.akarp0508.mrlauncher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.AppInfo
import com.akarp0508.mrlauncher.data.LabelAlignment
import com.akarp0508.mrlauncher.search.AppSearch
import com.akarp0508.mrlauncher.ui.components.AppLabel
import com.akarp0508.mrlauncher.ui.components.textAlign
import com.akarp0508.mrlauncher.ui.theme.LauncherTheme
import kotlin.math.abs

/** Present while the home screen is in edit mode; see [HomeScreen]. */
@Stable
class HomeEditor(
    /** Every non-hidden app, for the add list. */
    val availableApps: List<AppInfo>,
    val onReorder: (List<String>) -> Unit,
    val onToggleApp: (AppInfo) -> Unit,
    val onAppClick: (AppInfo) -> Unit,
    val onDone: () -> Unit,
)

/** Sits in the top margin above the clock, so the home layout below stays exactly as it is. */
@Composable
internal fun EditToolbar(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LauncherTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(HomeVerticalPadding)
            .padding(horizontal = HomeHorizontalPadding),
    ) {
        Text(
            text = stringResource(R.string.edit_home_hint),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textDim,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.edit_home_done),
            style = MaterialTheme.typography.labelLarge,
            color = colors.accent,
            modifier = Modifier
                .clickable(onClick = onDone)
                .padding(8.dp),
        )
    }
}

/**
 * The home app list with edit controls. Labels sit exactly where they do on the real home
 * screen; the drag handle and remove button live in the side margins.
 * Emits its rows straight into the caller's Column.
 */
@Composable
internal fun EditableHomeApps(apps: List<AppInfo>, alignment: LabelAlignment, editor: HomeEditor) {
    val colors = LauncherTheme.colors
    val currentOnReorder by rememberUpdatedState(editor.onReorder)
    var addingApps by rememberSaveable { mutableStateOf(false) }

    // Local copy so rows move live while dragging; saved once when the drag ends. One state object
    // for the screen's lifetime: the drag handlers below capture it and aren't restarted.
    var order by remember { mutableStateOf(apps) }
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val rowHeights = remember { mutableStateMapOf<String, Int>() }
    LaunchedEffect(apps) {
        if (draggingKey == null) order = apps
    }

    fun endDrag() {
        if (draggingKey != null) currentOnReorder(order.map { it.key })
        draggingKey = null
        dragOffset = 0f
    }

    val labelAlignment = when (alignment) {
        LabelAlignment.Left -> AbsoluteAlignment.CenterLeft
        LabelAlignment.Center -> Alignment.Center
        LabelAlignment.Right -> AbsoluteAlignment.CenterRight
    }
    val reorderDescription = stringResource(R.string.edit_home_reorder)
    val removeDescription = stringResource(R.string.edit_home_remove)

    order.forEach { app ->
        key(app.key) {
            val isDragging = app.key == draggingKey
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { rowHeights[app.key] = it.height }
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffset else 0f
                        alpha = if (isDragging) 0.7f else 1f
                    },
            ) {
                MarginControl(
                    text = "≡",
                    description = reorderDescription,
                    modifier = Modifier.pointerInput(app.key) {
                        detectDragGestures(
                            onDragStart = {
                                draggingKey = app.key
                                dragOffset = 0f
                            },
                            onDragEnd = ::endDrag,
                            onDragCancel = ::endDrag,
                        ) { change, amount ->
                            change.consume()
                            dragOffset += amount.y
                            // Swap with a neighbour once dragged past half of its height.
                            val index = order.indexOfFirst { it.key == app.key }
                            val neighbour = if (dragOffset > 0) index + 1 else index - 1
                            if (neighbour !in order.indices) return@detectDragGestures
                            val height = rowHeights[order[neighbour].key] ?: return@detectDragGestures
                            if (abs(dragOffset) > height / 2f) {
                                order = order.toMutableList().apply { add(neighbour, removeAt(index)) }
                                dragOffset += if (neighbour > index) -height else height
                            }
                        }
                    },
                )
                Box(contentAlignment = labelAlignment, modifier = Modifier.weight(1f)) {
                    AppLabel(
                        app = app,
                        style = LauncherTheme.textStyles.homeApp,
                        textAlign = alignment.textAlign,
                        onClick = { editor.onAppClick(app) },
                        onLongClick = { editor.onAppClick(app) },
                    )
                }
                MarginControl(
                    text = "✕",
                    description = removeDescription,
                    modifier = Modifier.clickable { editor.onToggleApp(app) },
                )
            }
        }
    }

    Text(
        text = stringResource(R.string.edit_home_add),
        style = LauncherTheme.textStyles.homeApp,
        color = colors.textDim,
        textAlign = alignment.textAlign,
        modifier = Modifier
            .padding(horizontal = HomeHorizontalPadding)
            .clickable { addingApps = true }
            .padding(vertical = LauncherTheme.itemSpacing),
    )

    if (addingApps) {
        AddHomeAppsDialog(
            apps = editor.availableApps,
            homeKeys = apps.map { it.key }.toSet(),
            onToggle = editor.onToggleApp,
            onDismiss = { addingApps = false },
        )
    }
}

/** A control occupying one side margin of the home screen, as tall as its row. */
@Composable
private fun MarginControl(text: String, description: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = LauncherTheme.colors.textDim,
        textAlign = TextAlign.Center,
        modifier = modifier
            .width(HomeHorizontalPadding)
            .semantics { contentDescription = description }
            .padding(vertical = 12.dp),
    )
}

@Composable
private fun AddHomeAppsDialog(
    apps: List<AppInfo>,
    homeKeys: Set<String>,
    onToggle: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(apps, query) { AppSearch.filter(apps, query) { it.label } }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.edit_home_done)) }
        },
        title = { Text(stringResource(R.string.edit_home_add_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.drawer_search_hint)) },
                )
                LazyColumn(Modifier.heightIn(max = 420.dp).padding(top = 8.dp)) {
                    items(results, key = { it.key }) { app ->
                        val onHome = app.key in homeKeys
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggle(app) }
                                .padding(vertical = 12.dp),
                        ) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge.copy(shadow = null),
                                color = if (onHome) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            if (onHome) {
                                Text("✓", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        },
    )
}
