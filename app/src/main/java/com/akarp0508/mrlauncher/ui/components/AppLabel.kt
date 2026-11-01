package com.akarp0508.mrlauncher.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
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
    showIcon: Boolean = false,
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
    val clickable = modifier
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        .padding(vertical = LauncherTheme.itemSpacing)

    if (!showIcon) {
        Text(
            text = text,
            style = style,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = clickable,
        )
        return
    }

    // Icon a bit taller than the text, on the side the text is aligned to.
    val iconSize = with(LocalDensity.current) { (style.fontSize * ICON_TO_TEXT).toDp() }
    val iconAfterText = textAlign == TextAlign.Right || textAlign == TextAlign.End
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(iconSize * 0.4f, textAlign.arrangement),
        modifier = clickable,
    ) {
        if (!iconAfterText) AppIcon(app, iconSize)
        Text(
            text = text,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (iconAfterText) AppIcon(app, iconSize)
    }
}

private const val ICON_TO_TEXT = 1.3f

private val TextAlign?.arrangement: Alignment.Horizontal
    get() = when (this) {
        TextAlign.Center -> Alignment.CenterHorizontally
        TextAlign.Right, TextAlign.End -> AbsoluteAlignment.Right
        else -> AbsoluteAlignment.Left
    }

val LabelAlignment.horizontal: Alignment.Horizontal
    get() = when (this) {
        LabelAlignment.Left -> AbsoluteAlignment.Left
        LabelAlignment.Center -> Alignment.CenterHorizontally
        LabelAlignment.Right -> AbsoluteAlignment.Right
    }

val LabelAlignment.textAlign: TextAlign
    get() = when (this) {
        LabelAlignment.Left -> TextAlign.Left
        LabelAlignment.Center -> TextAlign.Center
        LabelAlignment.Right -> TextAlign.Right
    }
