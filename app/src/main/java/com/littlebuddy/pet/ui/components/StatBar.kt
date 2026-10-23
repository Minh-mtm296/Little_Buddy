package com.littlebuddy.pet.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.littlebuddy.pet.pet.PetState
import com.littlebuddy.pet.ui.theme.BlushPetal
import com.littlebuddy.pet.ui.theme.MidnightLagoon
import com.littlebuddy.pet.ui.theme.MistySky
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.ui.theme.SageLeaf
import com.littlebuddy.pet.ui.theme.SoftWhite

/**
 * Thanh tiến trình chỉ số chung (StatProgressBar) với hiệu ứng animation, bo tròn mềm mại
 * và hỗ trợ chỉ số trợ năng Accessibility Semantics (0f .. 100f).
 */
@Composable
fun StatProgressBar(
    progress: Float,
    color: Color,
    height: Dp,
    modifier: Modifier = Modifier,
    semanticsValue: Float? = null,
    trackColor: Color = Color(0xFFEDE8E3)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "statProgressBarAnim"
    )

    val accessibilityModifier = if (semanticsValue != null) {
        Modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(semanticsValue, 0f..100f)
        }
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .then(accessibilityModifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

/**
 * Thanh chỉ số thu gọn (1 hàng ngang) cho màn hình chính.
 * Hiển thị 3 chỉ số cốt lõi: Độ no (🍚), Niềm vui (💕), Năng lượng (⚡).
 * Màu sắc đồng bộ chuẩn theo Theme: Rosewood, SageLeaf, MistySky.
 */
@Composable
fun CompactStatsRow(
    petState: PetState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(SoftWhite.copy(alpha = 0.90f))
            .border(1.5.dp, BlushPetal, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactStatItem(
            emoji = "🍚",
            label = "Độ no",
            value = petState.hunger,
            barColor = Rosewood,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(36.dp)
                .background(BlushPetal)
        )

        CompactStatItem(
            emoji = "💕",
            label = "Niềm vui",
            value = petState.happiness,
            barColor = SageLeaf,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(36.dp)
                .background(BlushPetal)
        )

        CompactStatItem(
            emoji = "⚡",
            label = "Năng lượng",
            value = petState.energy,
            barColor = MistySky,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CompactStatItem(
    emoji: String,
    label: String,
    value: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, style = MaterialTheme.typography.labelSmall)
            Text(
                text = "${value.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MidnightLagoon
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        StatProgressBar(
            progress = value / 100f,
            color = barColor,
            height = 7.dp,
            semanticsValue = value
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MidnightLagoon.copy(alpha = 0.5f)
        )
    }
}

/**
 * Hiển thị khối 3 thanh chỉ số chi tiết kèm gợi ý trạng thái.
 */
@Composable
fun PetStatsSection(
    petState: PetState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SoftWhite.copy(alpha = 0.85f))
            .border(1.5.dp, BlushPetal, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SingleStatBar(
            title = "Độ no",
            value = petState.hunger,
            icon = Icons.Rounded.Restaurant,
            barColor = Rosewood,
            iconBgColor = BlushPetal,
            hint = when {
                petState.hunger <= 20f -> "🥺 ${petState.petName} đang đói meo, cho bé ăn nhé!"
                petState.hunger <= 40f -> "😿 ${petState.petName} hơi đói rồi đó…"
                petState.hunger <= 70f -> "😊 ${petState.petName} đang no, bụng bình thường~"
                else -> "😽 No căng bụng, ${petState.petName} hạnh phúc!"
            }
        )

        SingleStatBar(
            title = "Niềm vui",
            value = petState.happiness,
            icon = Icons.Rounded.Favorite,
            barColor = SageLeaf,
            iconBgColor = SageLeaf.copy(alpha = 0.25f),
            hint = when {
                petState.happiness <= 20f -> "🥹 ${petState.petName} đang cần một chút quan tâm…"
                petState.happiness <= 50f -> "😐 ${petState.petName} cần được vui vẻ hơn một chút!"
                petState.happiness <= 80f -> "😸 ${petState.petName} vui lắm khi được chơi cùng bạn!"
                else -> "💕 ${petState.petName} đang rất hạnh phúc và yêu đời!"
            }
        )

        SingleStatBar(
            title = "Năng lượng",
            value = petState.energy,
            icon = Icons.Rounded.BatteryChargingFull,
            barColor = MistySky,
            iconBgColor = MistySky.copy(alpha = 0.3f),
            hint = when {
                petState.isSleeping -> "💤 ${petState.petName} đang nghỉ ngơi, đang hồi sức…"
                petState.energy <= 20f -> "😴 ${petState.petName} buồn ngủ rồi, cho bé nghỉ ngơi nha!"
                petState.energy <= 50f -> "🥱 ${petState.petName} hơi mệt, không nên chơi nhiều."
                petState.energy <= 80f -> "😊 ${petState.petName} đang ổn, vẫn còn sức đó!"
                else -> "✨ Tràn đầy năng lượng, chơi thôi nào!"
            }
        )
    }
}

@Composable
private fun SingleStatBar(
    title: String,
    value: Float,
    icon: ImageVector,
    barColor: Color,
    iconBgColor: Color,
    hint: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MidnightLagoon, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MidnightLagoon)
                Text(
                    text = "${value.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MidnightLagoon
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            StatProgressBar(
                progress = value / 100f,
                color = barColor,
                height = 10.dp,
                semanticsValue = value
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = MidnightLagoon.copy(alpha = 0.65f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
