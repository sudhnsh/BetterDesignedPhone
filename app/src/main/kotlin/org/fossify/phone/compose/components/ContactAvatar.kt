package org.fossify.phone.compose.components

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fossify.commons.helpers.isQPlus
import org.fossify.phone.compose.theme.AvatarColors
import org.fossify.phone.compose.theme.TextWhite

@Composable
fun ContactAvatar(
    name: String,
    photoUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var bitmap by remember(photoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUri) {
        if (!photoUri.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(photoUri)
                    val loaded = if (isQPlus()) {
                        context.contentResolver.loadThumbnail(uri, Size(128, 128), null)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    bitmap = loaded
                } catch (ignored: Exception) {
                    bitmap = null
                }
            }
        } else {
            bitmap = null
        }
    }

    val clickableModifier = if (onClick != null) {
        modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    } else {
        modifier
            .size(size)
            .clip(CircleShape)
    }

    val currentBitmap = bitmap
    if (currentBitmap != null) {
        Image(
            bitmap = currentBitmap.asImageBitmap(),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = clickableModifier
        )
    } else {
        val initial = name.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "#"
        val bgColor = remember(name) {
            val hash = name.hashCode()
            AvatarColors[kotlin.math.abs(hash) % AvatarColors.size]
        }

        Box(
            modifier = clickableModifier.background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = TextWhite,
                fontSize = (size.value * 0.42f).sp,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
