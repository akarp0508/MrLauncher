package com.akarp0508.mrlauncher.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.AppInfo
import com.akarp0508.mrlauncher.data.LabelAlignment
import com.akarp0508.mrlauncher.ui.theme.LauncherTheme

@Composable
fun AppLabel(
    app: AppInfo,
    style: TextStyle,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
) {
    val workSuffix = stringResource(R.string.work_profile_suffix)
    val dim = LauncherTheme.colors.textDim
    val text = buildAnnotatedString {
        append(app.label)
        if (app.isWorkProfile) {
            withStyle(SpanStyle(color = dim, fontSize = style.fontSize * 0.6f)) {
                append("  $workSuffix")
            }
        }
    }
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = LauncherTheme.itemSpacing),
    )
}

val LabelAlignment.textAlign: TextAlign
    get() = when (this) {
        LabelAlignment.Start -> TextAlign.Start
        LabelAlignment.Center -> TextAlign.Center
        LabelAlignment.End -> TextAlign.End
    }
