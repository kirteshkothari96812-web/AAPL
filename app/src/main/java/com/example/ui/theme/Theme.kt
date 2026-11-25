package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightOrganicColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = SageContainer,
    onPrimaryContainer = OnSageContainer,
    secondary = HarvestOchre,
    onSecondary = Color.White,
    secondaryContainer = HoneyContainer,
    onSecondaryContainer = OnHoneyContainer,
    tertiary = Terracotta,
    onTertiary = Color.White,
    tertiaryContainer = TerracottaContainer,
    onTertiaryContainer = OnTerracottaContainer,
    background = WarmCream,
    onBackground = DeepBarkText,
    surface = WarmCream,
    onSurface = DeepBarkText,
    surfaceVariant = WarmLinen,
    onSurfaceVariant = MutedMossText,
    surfaceContainerLowest = PureCardWhite,
    surfaceContainerLow = WarmLinen,
    surfaceContainer = SoftParchment,
    surfaceContainerHigh = Color(0xFFE3DBCB),
    outline = OutlineStone,
    outlineVariant = OutlineHairline,
    error = StockRedFg,
    onError = Color.White,
    errorContainer = StockRedBg,
    onErrorContainer = StockRedFg
)

private val DarkOrganicColorScheme = darkColorScheme(
    primary = MintPrimaryDark,
    onPrimary = OnMintDark,
    primaryContainer = SageContainerDark,
    onPrimaryContainer = SageContainer,
    secondary = Color(0xFFE9C46A),
    onSecondary = Color(0xFF3A2800),
    secondaryContainer = Color(0xFF4F3800),
    onSecondaryContainer = HoneyContainer,
    tertiary = Color(0xFFFFB59D),
    onTertiary = Color(0xFF561F0D),
    tertiaryContainer = Color(0xFF73331F),
    onTertiaryContainer = TerracottaContainer,
    background = DarkForestBg,
    onBackground = CreamTextDark,
    surface = DarkForestSurface,
    onSurface = CreamTextDark,
    surfaceVariant = DarkForestCard,
    onSurfaceVariant = SoftMossDark,
    surfaceContainerLowest = DarkForestSurface,
    surfaceContainerLow = DarkForestCard,
    surfaceContainer = Color(0xFF24342B),
    surfaceContainerHigh = Color(0xFF2B3D33),
    outline = Color(0xFF5B6E62),
    outlineVariant = Color(0xFF324339),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val AadiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AadiAarambhTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkOrganicColorScheme else LightOrganicColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AadiShapes,
        content = content
    )
}
