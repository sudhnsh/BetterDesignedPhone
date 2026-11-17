package org.fossify.phone.compose.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.fossify.phone.R
import org.fossify.phone.compose.theme.getNeumorphicColors
import org.fossify.phone.compose.theme.neumorphic

data class PhoneNavBarItem(
    val index: Int,
    val labelResId: Int,
    val selectedIconResId: Int,
    val unselectedIconResId: Int = selectedIconResId,
    val contentDescription: String,
    val badgeCount: Int? = null,
    val showBadgeDot: Boolean = false
)

@Composable
fun PhoneBottomNavBar(
    items: List<PhoneNavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bgLuminance = MaterialTheme.colorScheme.background.luminance()
    val isOled = MaterialTheme.colorScheme.background == Color.Black || bgLuminance < 0.01f
    val isDark = bgLuminance < 0.35f
    val neuColors = getNeumorphicColors()
    val containerShape = RoundedCornerShape(32.dp)
    val itemPillShape = RoundedCornerShape(26.dp)
    val safeSelectedIndex = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))

    val navBarPill = @Composable {
        Box(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth()
                .neumorphic(
                    cornerRadius = 32.dp,
                    lightShadowColor = neuColors.lightShadow,
                    darkShadowColor = neuColors.darkShadow,
                    shadowRadius = 8.dp,
                    offset = 5.dp
                )
                .background(neuColors.elementBackground, shape = containerShape)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                val totalWidth = maxWidth
                val itemCount = items.size.coerceAtLeast(1)
                val itemWidth = totalWidth / itemCount

                // ── Animated Sliding Capsule Indicator ─────────────────────
                val animatedPillOffset by animateDpAsState(
                    targetValue = itemWidth * safeSelectedIndex,
                    animationSpec = tween(
                        durationMillis = 280,
                        easing = FastOutSlowInEasing
                    ),
                    label = "nav_pill_offset"
                )

                Box(
                    modifier = Modifier
                        .offset(x = animatedPillOffset)
                        .width(itemWidth)
                        .height(52.dp)
                        .neumorphic(
                            cornerRadius = 26.dp,
                            lightShadowColor = neuColors.lightShadow,
                            darkShadowColor = neuColors.darkShadow,
                            shadowRadius = 5.dp,
                            offset = 3.dp
                        )
                        .background(
                            color = if (isDark) Color(0xFF34343E) else Color(0xFFFFFFFF),
                            shape = itemPillShape
                        )
                )

                // ── Interactive Navigation Items Row ───────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val selected = index == safeSelectedIndex

                        val contentColor by animateColorAsState(
                            targetValue = if (selected) {
                                if (isDark) Color(0xFFF5F5F0) else Color(0xFF181818)
                            } else {
                                if (isDark) Color(0xFF8E8E98) else Color(0xFF75757D)
                            },
                            animationSpec = tween(
                                durationMillis = 220,
                                easing = FastOutSlowInEasing
                            ),
                            label = "nav_content_color_$index"
                        )

                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1.08f else 1.0f,
                            animationSpec = tween(
                                durationMillis = 220,
                                easing = FastOutSlowInEasing
                            ),
                            label = "nav_icon_scale_$index"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(itemPillShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onItemSelected(item.index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (item.badgeCount != null && item.badgeCount > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError
                                            ) {
                                                Text(
                                                    text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        } else if (item.showBadgeDot) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(6.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            if (selected) item.selectedIconResId else item.unselectedIconResId
                                        ),
                                        contentDescription = item.contentDescription,
                                        tint = contentColor,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .scale(iconScale)
                                    )
                                }

                                Text(
                                    text = stringResource(item.labelResId),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout))
                .padding(
                    horizontal = 24.dp,
                    vertical = 8.dp
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier.widthIn(max = 340.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                navBarPill()
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(
                    horizontal = 24.dp,
                    vertical = 12.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            navBarPill()
        }
    }
}

fun defaultPhoneNavBarItems(): List<PhoneNavBarItem> = listOf(
    PhoneNavBarItem(
        index = 0,
        labelResId = R.string.tab_call,
        selectedIconResId = R.drawable.ic_clock_filled_vector,
        unselectedIconResId = R.drawable.ic_clock_vector,
        contentDescription = "Recents"
    ),
    PhoneNavBarItem(
        index = 1,
        labelResId = R.string.contacts_tab,
        selectedIconResId = R.drawable.ic_person_vector,
        unselectedIconResId = R.drawable.ic_person_outline_vector,
        contentDescription = "Contacts"
    ),
    PhoneNavBarItem(
        index = 2,
        labelResId = R.string.tab_keypad,
        selectedIconResId = R.drawable.ic_dialpad_vector,
        unselectedIconResId = R.drawable.ic_dialpad_vector,
        contentDescription = "Keypad"
    )
)
