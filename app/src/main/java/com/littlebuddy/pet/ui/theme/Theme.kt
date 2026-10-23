package com.littlebuddy.pet.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Rosewood,
    onPrimary = SoftWhite,
    primaryContainer = BlushPetal,
    onPrimaryContainer = MidnightLagoon,
    secondary = SageLeaf,
    onSecondary = SoftWhite,
    secondaryContainer = BlushPetal,
    onSecondaryContainer = MidnightLagoon,
    tertiary = MistySky,
    onTertiary = MidnightLagoon,
    background = VanillaCream,
    onBackground = MidnightLagoon,
    surface = SurfaceCream,
    onSurface = MidnightLagoon
)

val LittleBuddyShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun LittleBuddyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = LittleBuddyTypography,
        shapes = LittleBuddyShapes,
        content = content
    )
}
