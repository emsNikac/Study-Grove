package com.studygrove.app.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studygrove.app.data.model.SettingsState
import com.studygrove.app.ui.components.PixelButton
import com.studygrove.app.ui.components.PixelPanel
import com.studygrove.app.ui.components.SectionTitle
import com.studygrove.app.ui.components.SettingsGearIcon
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveClay
import com.studygrove.app.ui.theme.GroveCream
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveMoss

@Composable
fun SettingsScreen(
    settings: SettingsState,
    onSoundChanged: (Boolean) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onGentleRemindersChanged: (Boolean) -> Unit,
    onTestModeChanged: (Boolean) -> Unit,
    onDefaultTimerChanged: (Int) -> Unit,
    onThemeSongChanged: (String) -> Unit,
    onResetProgress: () -> Unit
) {
    var confirmReset by remember { mutableStateOf(false) }
    var sliderMinutes by remember(settings.defaultTimerMinutes) { mutableIntStateOf(settings.defaultTimerMinutes) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> onNotificationsChanged(granted) }
    )
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsGearIcon(Modifier.size(30.dp))
            SectionTitle("Settings")
        }
        SettingSwitchRow(
            title = "Sound",
            description = "Gentle click and reward tones.",
            checked = settings.soundEnabled,
            onCheckedChange = onSoundChanged
        )
        SettingSwitchRow(
            title = "Timer notifications",
            description = "Local timer-complete notifications. No account or internet.",
            checked = settings.notificationsEnabled,
            onCheckedChange = { enabled ->
                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onNotificationsChanged(enabled)
                }
            }
        )
        SettingSwitchRow(
            title = "Evening reminder",
            description = "A quiet local reminder around 7 PM.",
            checked = settings.gentleRemindersEnabled,
            onCheckedChange = onGentleRemindersChanged
        )
        SettingSwitchRow(
            title = "Test mode",
            description = "Unlocks every item for unlimited free placement and cosmetic testing.",
            checked = settings.testModeEnabled,
            onCheckedChange = onTestModeChanged
        )
        PixelPanel(color = GroveCream) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Theme song", color = GroveInk, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                Text(
                    "Choose the background music that plays while you tend the grove.",
                    color = GroveInk.copy(alpha = 0.7f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ThemeSongButton("Theme 1", "theme_1", settings.themeSong, onThemeSongChanged, Modifier.weight(1f))
                    ThemeSongButton("Theme 2", "theme_2", settings.themeSong, onThemeSongChanged, Modifier.weight(1f))
                    ThemeSongButton("Theme 3", "theme_3", settings.themeSong, onThemeSongChanged, Modifier.weight(1f))
                }
            }
        }
        PixelPanel(color = GroveCream) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Timer preference", color = GroveInk, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                Text(
                    "Default custom timer length: $sliderMinutes minutes",
                    color = GroveInk.copy(alpha = 0.86f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "This is the starting value shown on the Custom Timer slider.",
                    color = GroveInk.copy(alpha = 0.7f)
                )
                Slider(
                    value = sliderMinutes.toFloat(),
                    onValueChange = { sliderMinutes = it.toInt().coerceIn(5, 180) },
                    onValueChangeFinished = { onDefaultTimerChanged(sliderMinutes) },
                    valueRange = 5f..180f,
                    steps = 174
                )
            }
        }
        PixelPanel(color = GroveCream) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Reset progress", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                Text("Clears coins, XP, inventory, garden layout, house layout, achievements, quests, and active timer.", color = GroveInk.copy(alpha = 0.72f))
                PixelButton("Reset with confirmation", color = GroveBerry, modifier = Modifier.fillMaxWidth(), onClick = { confirmReset = true })
            }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset Study Grove?") },
            text = { Text("This returns the game to a fresh garden. This cannot be undone.") },
            confirmButton = {
                PixelButton("Reset", color = GroveBerry) {
                    confirmReset = false
                    onResetProgress()
                }
            },
            dismissButton = {
                PixelButton("Keep Grove", color = GroveMoss) { confirmReset = false }
            }
        )
    }
}

@Composable
private fun ThemeSongButton(
    label: String,
    value: String,
    selectedValue: String,
    onThemeSongChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PixelButton(
        text = if (value == selectedValue) "$label On" else label,
        color = if (value == selectedValue) GroveMoss else GroveClay,
        modifier = modifier,
        onClick = { onThemeSongChanged(value) }
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    PixelPanel(color = GroveCream) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title,
                    color = GroveInk,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(description, color = GroveInk.copy(alpha = 0.7f))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                if (checked) "On" else "Off",
                color = GroveInk,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Spacer(Modifier.width(8.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
