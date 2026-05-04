package com.studygrove.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studygrove.app.ui.components.NavPixelIcon
import com.studygrove.app.ui.components.PixelButton
import com.studygrove.app.ui.garden.GardenScreen
import com.studygrove.app.ui.house.HouseScreen
import com.studygrove.app.ui.inventory.InventoryScreen
import com.studygrove.app.ui.profile.ProfileScreen
import com.studygrove.app.ui.settings.SettingsScreen
import com.studygrove.app.ui.shop.ShopScreen
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveMoss
import com.studygrove.app.ui.theme.StudyGroveTheme
import com.studygrove.app.ui.timer.StudyTimerScreen
import java.time.Instant
import java.time.ZoneId

@Composable
fun StudyGroveApp(viewModel: StudyGroveViewModel) {
    val gameData by viewModel.gameData.collectAsStateWithLifecycle()
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    val nowMillis by viewModel.nowMillis.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val reward by viewModel.reward.collectAsStateWithLifecycle()
    val selectedGardenItem by viewModel.selectedGardenItemId.collectAsStateWithLifecycle()
    val selectedHouseItem by viewModel.selectedHouseItemId.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isNight = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).hour !in 6..18

    LaunchedEffect(message) {
        val text = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        viewModel.dismissMessage()
    }

    StudyGroveTheme(isNight = isNight) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    StudyGroveScreen.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = screen == destination,
                            onClick = { viewModel.navigate(destination) },
                            icon = { NavPixelIcon(destination, selected = screen == destination) },
                            label = { Text(destination.label, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                when (screen) {
                    StudyGroveScreen.GARDEN -> GardenScreen(
                        gameData = gameData,
                        selectedItemId = selectedGardenItem,
                        nowMillis = nowMillis,
                        onTileTapped = viewModel::onGardenTileTapped,
                        onClearSelection = viewModel::clearGardenSelection,
                        onNavigate = viewModel::navigate
                    )
                    StudyGroveScreen.TIMER -> StudyTimerScreen(
                        gameData = gameData,
                        nowMillis = nowMillis,
                        onStart = viewModel::startTimer,
                        onPause = viewModel::togglePause,
                        onCancel = viewModel::cancelTimer
                    )
                    StudyGroveScreen.SHOP -> ShopScreen(
                        gameData = gameData,
                        onBuyItem = viewModel::buyItem
                    )
                    StudyGroveScreen.INVENTORY -> InventoryScreen(
                        gameData = gameData,
                        onSelectGardenItem = viewModel::selectGardenItem,
                        onSelectHouseItem = viewModel::selectHouseItem,
                        onEquipCosmetic = viewModel::equipCosmetic
                    )
                    StudyGroveScreen.HOUSE -> HouseScreen(
                        gameData = gameData,
                        selectedItemId = selectedHouseItem,
                        onTileTapped = viewModel::onHouseTileTapped,
                        onSelectFurniture = viewModel::selectHouseItem,
                        onClearSelection = viewModel::clearHouseSelection
                    )
                    StudyGroveScreen.PROFILE -> ProfileScreen(gameData = gameData)
                    StudyGroveScreen.SETTINGS -> SettingsScreen(
                        settings = gameData.settings,
                        onSoundChanged = viewModel::setSoundEnabled,
                        onNotificationsChanged = viewModel::setNotificationsEnabled,
                        onGentleRemindersChanged = viewModel::setGentleRemindersEnabled,
                        onTestModeChanged = viewModel::setTestModeEnabled,
                        onDefaultTimerChanged = viewModel::setDefaultTimerMinutes,
                        onResetProgress = viewModel::resetProgress
                    )
                }
            }
        }
        reward?.let { sessionReward ->
            AlertDialog(
                onDismissRequest = viewModel::dismissReward,
                title = { Text("Session complete", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace) },
                text = {
                    Column {
                        Text(sessionReward.message)
                        Text("Study coins: ${sessionReward.baseCoins}")
                        Text("Full-session bonus: ${sessionReward.fullSessionBonus}")
                        Text("Streak bonus: ${sessionReward.streakBonus}")
                        Text("Daily quest bonus: ${sessionReward.questBonus}")
                        Text("Total: ${sessionReward.totalCoins} coins, ${sessionReward.xpEarned} XP")
                    }
                },
                confirmButton = {
                    PixelButton("Tend garden", color = GroveMoss, onClick = viewModel::dismissReward)
                },
                dismissButton = {
                    PixelButton("Profile", color = GroveBerry) {
                        viewModel.dismissReward()
                        viewModel.navigate(StudyGroveScreen.PROFILE)
                    }
                }
            )
        }
    }
}
