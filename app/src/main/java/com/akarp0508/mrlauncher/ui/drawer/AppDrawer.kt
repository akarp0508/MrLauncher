package com.akarp0508.mrlauncher.ui.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.AppInfo
import com.akarp0508.mrlauncher.data.IconMode
import com.akarp0508.mrlauncher.ui.components.AppLabel
import com.akarp0508.mrlauncher.data.LabelAlignment
import com.akarp0508.mrlauncher.ui.components.textAlign
import com.akarp0508.mrlauncher.ui.theme.LauncherTheme

@Composable
fun AppDrawer(
    apps: List<AppInfo>,
    query: String,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onOpenSettings: () -> Unit,
    alignment: LabelAlignment,
    autoShowKeyboard: Boolean,
    icons: IconMode,
    sideMargin: Dp,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val colors = LauncherTheme.colors

    LaunchedEffect(Unit) {
        if (autoShowKeyboard) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
    // Scrolling means browsing, not typing: get the keyboard out of the way.
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) keyboard?.hide()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = sideMargin),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 24.dp)) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = LauncherTheme.textStyles.drawerApp.copy(color = colors.text),
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { apps.firstOrNull()?.let(onAppClick) }),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            stringResource(R.string.drawer_search_hint),
                            style = LauncherTheme.textStyles.drawerApp,
                            color = colors.textDim,
                        )
                    }
                    inner()
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
            )
            Text(
                text = stringResource(R.string.drawer_settings),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textDim,
                modifier = Modifier
                    .clickable(onClick = onOpenSettings)
                    .padding(8.dp),
            )
        }

        if (apps.isEmpty() && query.isNotEmpty()) {
            Text(
                text = stringResource(R.string.drawer_no_results),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textDim,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            items(apps, key = { it.key }) { app ->
                AppLabel(
                    app = app,
                    style = LauncherTheme.textStyles.drawerApp,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                    textAlign = alignment.textAlign,
                    icons = icons,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
