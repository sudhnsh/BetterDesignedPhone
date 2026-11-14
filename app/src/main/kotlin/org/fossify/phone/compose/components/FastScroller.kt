package org.fossify.phone.compose.components

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun FastScroller(
    letters: List<Char>,
    listState: LazyListState,
    letterIndexMap: Map<Char, Int>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedLetter by remember { mutableStateOf<Char?>(null) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(28.dp)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .pointerInput(letters, letterIndexMap) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            val itemHeight = size.height / letters.size.coerceAtLeast(1)
                            val index = (offset.y / itemHeight).toInt().coerceIn(0, letters.lastIndex)
                            val char = letters.getOrNull(index) ?: return@detectVerticalDragGestures
                            selectedLetter = char
                            letterIndexMap[char]?.let { targetIndex ->
                                coroutineScope.launch { listState.scrollToItem(targetIndex) }
                            }
                        },
                        onDragEnd = { selectedLetter = null },
                        onDragCancel = { selectedLetter = null },
                        onVerticalDrag = { change, _ ->
                            val itemHeight = size.height / letters.size.coerceAtLeast(1)
                            val index = (change.position.y / itemHeight).toInt().coerceIn(0, letters.lastIndex)
                            val char = letters.getOrNull(index) ?: return@detectVerticalDragGestures
                            if (char != selectedLetter) {
                                selectedLetter = char
                                letterIndexMap[char]?.let { targetIndex ->
                                    coroutineScope.launch { listState.scrollToItem(targetIndex) }
                                }
                            }
                        }
                    )
                },
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            letters.forEach { char ->
                val isSelected = selectedLetter == char
                Text(
                    text = char.toString(),
                    fontSize = 11.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
