package com.littlebuddy.pet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.littlebuddy.pet.pet.PetState
import com.littlebuddy.pet.ui.theme.BlushPetal
import com.littlebuddy.pet.ui.theme.MidnightLagoon
import com.littlebuddy.pet.ui.theme.MistySky
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.ui.theme.SoftWhite

/**
 * Hàng 4 nút tương tác chính: Cho ăn, Chơi, Tủ đồ, Ngủ/Thức
 * Vuốt ve được thực hiện trực tiếp bằng cách chạm vào nhân vật.
 */
@Composable
fun PetActionButtons(
    petState: PetState,
    onFeed: () -> Unit,
    onPlay: () -> Unit,
    onWardrobe: () -> Unit,
    onToggleSleep: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Nút Cho ăn
        ActionButtonItem(
            title = "Cho ăn",
            subtitle = "Ngon miệng",
            icon = Icons.Rounded.Restaurant,
            backgroundColor = Rosewood,
            contentColor = SoftWhite,
            enabled = !petState.isSleeping,
            onClick = onFeed,
            modifier = Modifier.weight(1f)
        )

        // Nút Chơi
        ActionButtonItem(
            title = "Chơi",
            subtitle = "Vui vẻ",
            icon = Icons.Rounded.SportsEsports,
            backgroundColor = BlushPetal,
            contentColor = MidnightLagoon,
            enabled = !petState.isSleeping && petState.canPlay,
            onClick = onPlay,
            modifier = Modifier.weight(1f)
        )

        // Nút Tủ đồ
        ActionButtonItem(
            title = "Tủ đồ",
            subtitle = "Thay đồ",
            icon = Icons.Rounded.Checkroom,
            backgroundColor = MistySky,
            contentColor = MidnightLagoon,
            enabled = true,
            onClick = onWardrobe,
            modifier = Modifier.weight(1f)
        )

        // Nút Ngủ / Thức
        ActionButtonItem(
            title = if (petState.isSleeping) "Thức dậy" else "Đi ngủ",
            subtitle = if (petState.isSleeping) "Tỉnh giấc" else "Nghỉ ngơi",
            icon = if (petState.isSleeping) Icons.Rounded.WbSunny else Icons.Rounded.Bedtime,
            backgroundColor = if (petState.isSleeping) BlushPetal else Color(0xFF6B7280),
            contentColor = if (petState.isSleeping) MidnightLagoon else SoftWhite,
            enabled = true,
            onClick = onToggleSleep,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButtonItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentBg = if (enabled) backgroundColor else Color(0xFFD6CEC7)
    val currentContent = if (enabled) contentColor else Color(0xFF8C827A)

    Box(
        modifier = modifier
            .shadow(if (enabled) 3.dp else 0.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(currentBg)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = currentContent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = currentContent)
            Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = currentContent.copy(alpha = 0.75f))
        }
    }
}
