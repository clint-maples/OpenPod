package com.openpod.app.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import com.openpod.app.data.db.SettingsEntity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Locked OpenPod 1.60 theme tokens.
 *
 * Dark + Soft dark + Teal matches the 1.59 look except the inactive seek track,
 * which is raised to #8A939C for AA contrast. Light uses Accent wash surfaces
 * and Option D tonal control wells. Coral and Amber washes are automatically
 * half-tinted.
 */
object OpenPodPalette {
    val White = Color(0xFFFFFFFF)
    val SoftDark = Color(0xFF121418)
    val TrueBlack = Color(0xFF000000)
    val DarkSurfaceVariant = Color(0xFF1C2026)
    val DarkChromeBar = Color(0xFF262B33)
    val DarkTrueBlackContainer = Color(0xFF1C2026)
    val DarkOnSurface = Color(0xFFE8F1F5)
    val DarkSecondary = Color(0xFFB5C9D3)
    val DarkMuted = Color(0xFF8E8E93)
    val LightOnSurface = Color(0xFF121418)
    val LightSecondary = Color(0xFF3E5560)
    val LightMuted = Color(0xFF5C656C)
    val DarkInactiveSeek = Color(0xFF8A939C)
    val LightInactiveSeek = Color(0xFF7A858E)
    val FavoriteHeart = Color(0xFFE91E63)

    const val WASH_PAGE = 0.125f
    const val WASH_CARD = 0.085f
    const val WASH_BAND = 0.053f
    const val WASH_PAGE_HALF = 0.0625f
    const val WASH_CARD_HALF = 0.0425f
    const val WASH_BAND_HALF = 0.0265f
    const val WELL_MIX = 0.12f
    const val TRACK_ALPHA = 0.22f
    const val HAIRLINE_ALPHA = 0.08f
    const val DIVIDER_ALPHA = 0.12f
    const val PRIMARY_CONTAINER_DARK = 0.22f
    const val PRIMARY_CONTAINER_LIGHT = 0.16f

    fun mix(a: Color, b: Color, amount: Float): Color {
        val t = amount
        return Color(
            red = a.red + (b.red - a.red) * t,
            green = a.green + (b.green - a.green) * t,
            blue = a.blue + (b.blue - a.blue) * t,
            alpha = a.alpha + (b.alpha - a.alpha) * t
        )
    }

    fun rgb(color: Color): Int {
        fun ch(value: Float) = (value * 255f).roundToInt().coerceIn(0, 255)
        return (0xFF shl 24) or (ch(color.red) shl 16) or (ch(color.green) shl 8) or ch(color.blue)
    }

    fun contrastRatio(a: Color, b: Color): Double {
        val hi = max(a.luminance().toDouble(), b.luminance().toDouble())
        val lo = min(a.luminance().toDouble(), b.luminance().toDouble())
        return (hi + 0.05) / (lo + 0.05)
    }

    fun washAmounts(halfTint: Boolean): Triple<Float, Float, Float> {
        return if (halfTint) {
            Triple(WASH_PAGE_HALF, WASH_CARD_HALF, WASH_BAND_HALF)
        } else {
            Triple(WASH_PAGE, WASH_CARD, WASH_BAND)
        }
    }
}

enum class OpenPodAccent(
    val id: String,
    val label: String,
    val dark: Color,
    val onDark: Color,
    val light: Color,
    val onLight: Color,
    val halfTint: Boolean = false
) {
    TEAL("teal", "Teal", Color(0xFF7FB7C9), Color(0xFF00344A), Color(0xFF1A6A7C), Color.White),
    AMBER("amber", "Amber", Color(0xFFE2B04A), Color(0xFF2A2000), Color(0xFF8B5500), Color.White, halfTint = true),
    CORAL("coral", "Coral", Color(0xFFE8897A), Color(0xFF3A1010), Color(0xFFB1322C), Color.White, halfTint = true),
    GREEN("green", "Green", Color(0xFF7BC49A), Color(0xFF0A2E1C), Color(0xFF1F7048), Color.White),
    PURPLE("purple", "Purple", Color(0xFFC4A5E0), Color(0xFF2A1040), Color(0xFF6E3AA8), Color.White),
    GRAPHITE("graphite", "Graphite", Color(0xFFC2C8CE), Color(0xFF1A1D21), Color(0xFF4A525A), Color.White);

    fun primary(isDark: Boolean): Color = if (isDark) dark else light
    fun onPrimary(isDark: Boolean): Color = if (isDark) onDark else onLight

    companion object {
        val all: List<OpenPodAccent> = entries.toList()

        fun fromId(id: String): OpenPodAccent =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: TEAL

        fun labelOf(id: String): String = fromId(id).label
    }
}

data class OpenPodColors(
    val scheme: ColorScheme,
    val isDark: Boolean,
    val isTrueBlack: Boolean,
    val accent: OpenPodAccent,
    val page: Color,
    val card: Color,
    val band: Color,
    val progressTransport: Color,
    val chromeBar: Color,
    val controlWell: Color,
    val useTonalButtons: Boolean,
    val muted: Color,
    val hairline: Color,
    val divider: Color,
    val inactiveSeekTrack: Color,
    val scrollbarThumb: Color,
    val scrollbarTrack: Color,
    val sheetContainer: Color,
    val sheetContent: Color,
    val sheetHandle: Color,
    val favoriteHeart: Color = OpenPodPalette.FavoriteHeart
)

fun resolveOpenPodDark(themeMode: String, systemDark: Boolean): Boolean {
    return when (SettingsEntity.sanitizeThemeMode(themeMode)) {
        SettingsEntity.THEME_LIGHT -> false
        SettingsEntity.THEME_SYSTEM -> systemDark
        else -> true
    }
}

fun openPodColors(
    isDark: Boolean,
    trueBlack: Boolean,
    accent: OpenPodAccent
): OpenPodColors {
    return if (isDark) darkColors(trueBlack, accent) else lightColors(accent)
}

private fun darkColors(trueBlack: Boolean, accent: OpenPodAccent): OpenPodColors {
    val page = if (trueBlack) OpenPodPalette.TrueBlack else OpenPodPalette.SoftDark
    val surfaceVariant = if (trueBlack) OpenPodPalette.SoftDark else OpenPodPalette.DarkSurfaceVariant
    val containerHigh = if (trueBlack) {
        OpenPodPalette.DarkTrueBlackContainer
    } else {
        OpenPodPalette.DarkChromeBar
    }
    val primary = accent.dark
    val onPrimary = accent.onDark
    val primaryContainer = OpenPodPalette.mix(page, primary, OpenPodPalette.PRIMARY_CONTAINER_DARK)
    val onSurface = OpenPodPalette.DarkOnSurface
    val secondary = OpenPodPalette.DarkSecondary
    val muted = OpenPodPalette.DarkMuted
    val scheme = darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = primary,
        secondary = secondary,
        onSecondary = page,
        secondaryContainer = OpenPodPalette.mix(page, secondary, 0.16f),
        onSecondaryContainer = secondary,
        background = page,
        onBackground = onSurface,
        surface = page,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = muted,
        surfaceTint = Color.Transparent,
        surfaceContainerLowest = page,
        surfaceContainerLow = surfaceVariant,
        surfaceContainer = surfaceVariant,
        surfaceContainerHigh = containerHigh,
        surfaceContainerHighest = OpenPodPalette.DarkChromeBar,
        outline = onSurface.copy(alpha = OpenPodPalette.DIVIDER_ALPHA),
        outlineVariant = onSurface.copy(alpha = OpenPodPalette.HAIRLINE_ALPHA),
        inverseSurface = onSurface,
        inverseOnSurface = OpenPodPalette.SoftDark,
        inversePrimary = accent.light,
        scrim = Color.Black.copy(alpha = 0.5f)
    )
    return OpenPodColors(
        scheme = scheme,
        isDark = true,
        isTrueBlack = trueBlack,
        accent = accent,
        page = page,
        card = containerHigh,
        band = OpenPodPalette.TrueBlack,
        progressTransport = OpenPodPalette.TrueBlack,
        chromeBar = OpenPodPalette.DarkChromeBar,
        controlWell = Color.Transparent,
        useTonalButtons = false,
        muted = muted,
        hairline = Color.White.copy(alpha = OpenPodPalette.HAIRLINE_ALPHA),
        divider = Color.White.copy(alpha = OpenPodPalette.DIVIDER_ALPHA),
        inactiveSeekTrack = OpenPodPalette.DarkInactiveSeek,
        scrollbarThumb = primary,
        scrollbarTrack = onSurface.copy(alpha = OpenPodPalette.TRACK_ALPHA),
        sheetContainer = containerHigh,
        sheetContent = onSurface,
        sheetHandle = muted
    )
}

private fun lightColors(accent: OpenPodAccent): OpenPodColors {
    val (pageAmt, cardAmt, bandAmt) = OpenPodPalette.washAmounts(accent.halfTint)
    val page = OpenPodPalette.mix(OpenPodPalette.White, accent.light, pageAmt)
    val card = OpenPodPalette.mix(OpenPodPalette.White, accent.light, cardAmt)
    val band = OpenPodPalette.mix(OpenPodPalette.White, accent.light, bandAmt)
    val well = OpenPodPalette.mix(band, accent.light, OpenPodPalette.WELL_MIX)
    val primary = accent.light
    val onPrimary = accent.onLight
    val onSurface = OpenPodPalette.LightOnSurface
    val secondary = OpenPodPalette.LightSecondary
    val muted = OpenPodPalette.LightMuted
    val primaryContainer = OpenPodPalette.mix(page, primary, OpenPodPalette.PRIMARY_CONTAINER_LIGHT)
    val scheme = lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = primary,
        secondary = secondary,
        onSecondary = OpenPodPalette.White,
        secondaryContainer = OpenPodPalette.mix(page, secondary, 0.12f),
        onSecondaryContainer = secondary,
        background = page,
        onBackground = onSurface,
        surface = page,
        onSurface = onSurface,
        surfaceVariant = card,
        onSurfaceVariant = muted,
        surfaceTint = Color.Transparent,
        surfaceContainerLowest = page,
        surfaceContainerLow = page,
        surfaceContainer = card,
        surfaceContainerHigh = card,
        surfaceContainerHighest = OpenPodPalette.White,
        outline = onSurface.copy(alpha = OpenPodPalette.DIVIDER_ALPHA),
        outlineVariant = onSurface.copy(alpha = OpenPodPalette.HAIRLINE_ALPHA),
        inverseSurface = OpenPodPalette.SoftDark,
        inverseOnSurface = OpenPodPalette.DarkOnSurface,
        inversePrimary = accent.dark,
        scrim = Color.Black.copy(alpha = 0.5f)
    )
    return OpenPodColors(
        scheme = scheme,
        isDark = false,
        isTrueBlack = false,
        accent = accent,
        page = page,
        card = card,
        band = band,
        progressTransport = band,
        chromeBar = band,
        controlWell = well,
        useTonalButtons = true,
        muted = muted,
        hairline = onSurface.copy(alpha = OpenPodPalette.HAIRLINE_ALPHA),
        divider = onSurface.copy(alpha = OpenPodPalette.DIVIDER_ALPHA),
        inactiveSeekTrack = OpenPodPalette.LightInactiveSeek,
        scrollbarThumb = primary,
        scrollbarTrack = onSurface.copy(alpha = OpenPodPalette.TRACK_ALPHA),
        sheetContainer = card,
        sheetContent = onSurface,
        sheetHandle = muted
    )
}

val LocalOpenPodColors = staticCompositionLocalOf {
    openPodColors(isDark = true, trueBlack = false, accent = OpenPodAccent.TEAL)
}

@Composable
fun OpenPodTheme(
    themeMode: String = SettingsEntity.THEME_DARK,
    darkBackground: String = SettingsEntity.BACKGROUND_SOFT,
    accentId: String = SettingsEntity.ACCENT_TEAL,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = resolveOpenPodDark(themeMode, systemDark)
    val trueBlack = isDark &&
        SettingsEntity.sanitizeDarkBackground(darkBackground) == SettingsEntity.BACKGROUND_TRUE_BLACK
    val accent = OpenPodAccent.fromId(SettingsEntity.sanitizeAccent(accentId))
    val colors = remember(isDark, trueBlack, accent) {
        openPodColors(isDark, trueBlack, accent)
    }
    val view = LocalView.current
    SideEffect {
        applyOpenPodSystemBars(view.context, colors)
    }
    CompositionLocalProvider(LocalOpenPodColors provides colors) {
        MaterialTheme(colorScheme = colors.scheme, content = content)
    }
}

@Composable
fun OpenPodControlWell(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalOpenPodColors.current
    Box(
        modifier = modifier.then(
            if (colors.useTonalButtons) {
                Modifier.background(colors.controlWell, shape)
            } else {
                Modifier
            }
        ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun openPodControlTint(): Color {
    val colors = LocalOpenPodColors.current
    return if (colors.useTonalButtons) colors.scheme.primary else colors.scheme.onSurface
}

private fun applyOpenPodSystemBars(context: Context, colors: OpenPodColors) {
    val activity = context.findActivity() ?: return
    val scrim = android.graphics.Color.TRANSPARENT
    val style = if (colors.isDark) {
        SystemBarStyle.dark(scrim)
    } else {
        SystemBarStyle.light(scrim, scrim)
    }
    if (activity is ComponentActivity) {
        activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
    activity.window?.setBackgroundDrawable(ColorDrawable(colors.page.toArgb()))
}

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
