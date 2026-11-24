package com.openpod.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpod.app.data.db.SettingsEntity
import com.openpod.app.playback.PlayerUiState
import com.openpod.app.playback.SkipIntervals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h800dp-xxhdpi")
class ThemeRender160Test {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val proofDir = File("/workspace/openpod-scope/themes/built-160")

    @Before
    fun createProofDir() {
        proofDir.mkdirs()
    }

    @Test
    fun renderDarkDefault() {
        setNowPlaying(SettingsEntity.THEME_DARK, SettingsEntity.BACKGROUND_SOFT, SettingsEntity.ACCENT_TEAL)
        capture("dark-default.png")
    }

    @Test
    fun renderTrueBlack() {
        setNowPlaying(SettingsEntity.THEME_DARK, SettingsEntity.BACKGROUND_TRUE_BLACK, SettingsEntity.ACCENT_TEAL)
        capture("true-black.png")
    }

    @Test
    fun renderLightTeal() {
        setNowPlaying(SettingsEntity.THEME_LIGHT, SettingsEntity.BACKGROUND_SOFT, SettingsEntity.ACCENT_TEAL)
        capture("light-teal.png")
    }

    @Test
    fun renderLightCoral() {
        setNowPlaying(SettingsEntity.THEME_LIGHT, SettingsEntity.BACKGROUND_SOFT, SettingsEntity.ACCENT_CORAL)
        capture("light-coral.png")
    }

    @Test
    fun renderAppearanceDark() {
        setAppearance(SettingsEntity.THEME_DARK, SettingsEntity.BACKGROUND_SOFT, SettingsEntity.ACCENT_TEAL, true)
        capture("appearance-dark.png")
    }

    @Test
    fun renderAppearanceLight() {
        setAppearance(SettingsEntity.THEME_LIGHT, SettingsEntity.BACKGROUND_SOFT, SettingsEntity.ACCENT_TEAL, false)
        capture("appearance-light.png")
    }

    private fun setNowPlaying(themeMode: String, background: String, accent: String) {
        rule.setContent {
            OpenPodTheme(themeMode = themeMode, darkBackground = background, accentId = accent) {
                ThemeNowPlayingPreview()
            }
        }
        rule.waitForIdle()
    }

    private fun setAppearance(themeMode: String, background: String, accent: String, systemIsDark: Boolean) {
        rule.setContent {
            OpenPodTheme(themeMode = themeMode, darkBackground = background, accentId = accent) {
                AppearanceSettingsScreen(
                    themeMode = themeMode,
                    darkBackground = background,
                    accent = accent,
                    layout = SettingsEntity.LAYOUT_LIST,
                    systemIsDark = systemIsDark,
                    onThemeMode = {},
                    onDarkBackground = {},
                    onAccent = {},
                    onLayout = {},
                    onBack = {}
                )
            }
        }
        rule.waitForIdle()
    }

    private fun capture(filename: String) {
        val content = rule.activity.findViewById<ViewGroup>(android.R.id.content)
        forceSoftwareLayer(content)
        val width = content.width.coerceAtLeast(1)
        val height = content.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        content.draw(AndroidCanvas(bitmap))
        FileOutputStream(File(proofDir, filename)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("wrote ${File(proofDir, filename)} ${bitmap.width}x${bitmap.height}")
    }

    private fun forceSoftwareLayer(view: View) {
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) forceSoftwareLayer(view.getChildAt(i))
        }
    }
}

@Composable
private fun ThemeNowPlayingPreview() {
    val chrome = LocalOpenPodColors.current
    val tint = openPodControlTint()
    val player = PlayerUiState(
        episodeId = 1L,
        title = "Episode 128: Soft light",
        podcastTitle = "OpenPod Daily",
        playing = true,
        positionMs = (46 * 60 + 44) * 1000L,
        durationMs = (46 * 60 + 44 + 42 * 60 + 10) * 1000L,
        speed = 1f,
        skipForwardSec = 30,
        skipBackSec = 15
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(chrome.page)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 28.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
        )
        Text(
            player.podcastTitle,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Text(
            player.title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Spacer(Modifier.weight(1f))
        Surface(color = chrome.progressTransport, contentColor = MaterialTheme.colorScheme.onSurface, tonalElevation = 0.dp) {
            NowPlayingProgressRow(
                positionMs = player.positionMs,
                durationMs = player.durationMs,
                onSeekMs = {}
            )
        }
        Spacer(Modifier.height(NowPlayingChrome.FOOTER_BAND_GAP_DP.dp))
        Surface(color = chrome.progressTransport, contentColor = MaterialTheme.colorScheme.onSurface, tonalElevation = 0.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(NowPlayingChrome.TRANSPORT_BAND_HEIGHT_DP.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PreviewSkip(seconds = 15, forward = false, tint = tint)
                OpenPodControlWell(Modifier.size(64.dp)) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(NowPlayingChrome.TRANSPORT_PLAY_ICON_DP.dp),
                        tint = tint
                    )
                }
                PreviewSkip(seconds = 30, forward = true, tint = tint)
            }
        }
        Spacer(Modifier.height(NowPlayingChrome.FOOTER_BAND_GAP_DP.dp))
        Surface(color = chrome.chromeBar, tonalElevation = 0.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(NowPlayingChrome.ACTIONS_BAND_HEIGHT_DP.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OpenPodControlWell(
                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("1.0x", color = tint, style = MaterialTheme.typography.titleMedium)
                }
                OpenPodControlWell(Modifier.size(32.dp)) {
                    Icon(Icons.Default.Equalizer, "EQ", tint = tint, modifier = Modifier.size(20.dp))
                }
                OpenPodControlWell(Modifier.size(32.dp)) {
                    Icon(Icons.Default.Bedtime, "Sleep", tint = tint, modifier = Modifier.size(20.dp))
                }
                OpenPodControlWell(Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, "Share", tint = tint, modifier = Modifier.size(20.dp))
                }
                OpenPodControlWell(Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Favorite,
                        "Favorite",
                        tint = chrome.favoriteHeart,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(NowPlayingChrome.FOOTER_BAND_GAP_DP.dp))
        MiniPlayer(
            player = player.copy(playing = false),
            onOpen = {},
            onUpNext = {},
            onTogglePlay = {},
            onSkipBack = {},
            onSkipForward = {}
        )
        HorizontalDivider(
            thickness = NowPlayingChrome.NAV_ACCENT_LINE_DP.dp,
            color = MaterialTheme.colorScheme.primary
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            val itemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = chrome.muted,
                unselectedTextColor = chrome.muted
            )
            NavigationBarItem(
                selected = false,
                onClick = {},
                icon = { Icon(Icons.Default.LibraryMusic, null) },
                label = { Text("Library") },
                colors = itemColors
            )
            NavigationBarItem(
                selected = false,
                onClick = {},
                icon = { Icon(Icons.Default.Explore, null) },
                label = { Text("Discover") },
                colors = itemColors
            )
            NavigationBarItem(
                selected = false,
                onClick = {},
                icon = { Icon(Icons.Default.QueueMusic, null) },
                label = { Text("Playlists") },
                colors = itemColors
            )
            NavigationBarItem(
                selected = true,
                onClick = {},
                icon = { Icon(Icons.Default.PlayArrow, null) },
                label = { Text("Now playing") },
                colors = itemColors
            )
        }
    }
}

@Composable
private fun PreviewSkip(seconds: Int, forward: Boolean, tint: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OpenPodControlWell(Modifier.size(48.dp), shape = CircleShape) {
            Icon(
                Icons.Default.Replay,
                contentDescription = null,
                modifier = Modifier
                    .size(NowPlayingChrome.TRANSPORT_SKIP_ICON_DP.dp)
                    .then(if (forward) Modifier.graphicsLayer { scaleX = -1f } else Modifier),
                tint = tint
            )
        }
        Text(
            SkipIntervals.compactLabel(seconds, forward),
            color = tint,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
