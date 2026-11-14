package org.fossify.phone.compose.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun HighlightedText(
    text: String,
    highlight: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    if (highlight.isEmpty() || !text.contains(highlight, ignoreCase = true)) {
        Text(
            text = text,
            modifier = modifier,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow
        )
        return
    }

    val annotatedString = buildAnnotatedString {
        var startIndex = 0
        val lowerText = text.lowercase()
        val lowerHighlight = highlight.lowercase()

        while (startIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerHighlight, startIndex)
            if (matchIndex == -1) {
                append(text.substring(startIndex))
                break
            } else {
                append(text.substring(startIndex, matchIndex))
                val endIndex = matchIndex + highlight.length
                val matchedPart = text.substring(matchIndex, endIndex)
                pushStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold))
                append(matchedPart)
                pop()
                startIndex = endIndex
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}
