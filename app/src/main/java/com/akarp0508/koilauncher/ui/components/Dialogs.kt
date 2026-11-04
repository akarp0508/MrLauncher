package com.akarp0508.koilauncher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akarp0508.koilauncher.R
import com.akarp0508.koilauncher.data.AppInfo
import com.akarp0508.koilauncher.ui.AppAction

@Composable
fun AppActionsDialog(
    app: AppInfo,
    actions: List<AppAction>,
    onAction: (AppAction) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(app.label) },
        text = {
            Column {
                actions.forEach { action ->
                    Text(
                        text = stringResource(action.label),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAction(action) }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        },
    )
}

@Composable
fun RenameDialog(
    app: AppInfo,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(app.label) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_title, app.systemLabel)) },
        text = {
            OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true)
        },
        confirmButton = {
            TextButton(onClick = { onRename(text) }) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = {
            if (app.label != app.systemLabel) {
                TextButton(onClick = { onRename("") }) { Text(stringResource(R.string.rename_reset)) }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
            }
        },
    )
}
