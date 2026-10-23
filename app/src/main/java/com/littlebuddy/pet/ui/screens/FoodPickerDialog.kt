package com.littlebuddy.pet.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.littlebuddy.pet.R
import com.littlebuddy.pet.pet.BowlFood
import com.littlebuddy.pet.pet.PetState
import com.littlebuddy.pet.ui.theme.BlushPetal
import com.littlebuddy.pet.ui.theme.MidnightLagoon
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.ui.theme.SageLeaf
import com.littlebuddy.pet.ui.theme.SoftWhite
import com.littlebuddy.pet.ui.theme.VanillaCream

/**
 * Bảng chọn thức ăn cho thú cưng.
 * Hiện 3 lựa chọn: Hạt khô, Pate, Cá tươi.
 * Hiển thị boost stats để người dùng biết lựa chọn của mình ảnh hưởng gì.
 */
@Composable
fun FoodPickerDialog(
    petState: PetState,
    onSelectFood: (BowlFood) -> Unit,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(280),
        label = "dialogScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .scale(scale)
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(VanillaCream)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Tiêu đề
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bat_do_an),
                        contentDescription = "Bát đồ ăn",
                        modifier = Modifier.size(34.dp)
                    )
                    Text(
                        text = "Chọn thức ăn cho ${petState.petName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MidnightLagoon,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Độ no hiện tại: ${petState.hunger.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MidnightLagoon.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3 lựa chọn thức ăn
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FoodOptionCard(
                        food = BowlFood.DRY_FOOD,
                        color = Color(0xFFE8D4B8),
                        accentColor = Color(0xFF8B6914),
                        description = "Ngon, tiện và bổ dưỡng",
                        onClick = { onSelectFood(BowlFood.DRY_FOOD) }
                    )
                    FoodOptionCard(
                        food = BowlFood.PATE,
                        color = Color(0xFFF5C5C5),
                        accentColor = Rosewood,
                        description = "Mềm mịn, thơm ngon hơn",
                        onClick = { onSelectFood(BowlFood.PATE) }
                    )
                    FoodOptionCard(
                        food = BowlFood.FISH,
                        color = Color(0xFFBFDDD8),
                        accentColor = Color(0xFF2D7F77),
                        description = "Món ưa thích của mèo! Tăng nhiều Niềm vui",
                        onClick = { onSelectFood(BowlFood.FISH) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Nút hủy
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BlushPetal.copy(alpha = 0.4f))
                        .clickable { onDismiss() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Để sau",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MidnightLagoon.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodOptionCard(
    food: BowlFood,
    color: Color,
    accentColor: Color,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .border(1.5.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hình ảnh thực tế của bát đồ ăn
            Image(
                painter = painterResource(id = food.drawableRes),
                contentDescription = food.displayName,
                modifier = Modifier.size(width = 54.dp, height = 36.dp),
                contentScale = ContentScale.Fit
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MidnightLagoon
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MidnightLagoon.copy(alpha = 0.65f)
                )
            }

            // Boost stats hiển thị nhỏ bên phải
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${food.hungerBoost.toInt()}% no",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Rosewood
                )
                if (food.happinessBoost > 0f) {
                    Text(
                        text = "+${food.happinessBoost.toInt()}% vui",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = SageLeaf
                    )
                }
            }
        }
    }
}
