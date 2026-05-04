# Study Grove

Study Grove is a cozy offline Android study-simulation game built in Kotlin with Jetpack Compose. The player grows a pixel-art garden by completing real study timers, earning coins and focus XP, buying seeds and decorations, placing items, furnishing a cottage, and building streaks.

## Build And Run

1. Open this directory in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration on an Android device or emulator.

Command line:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## What Is Implemented

- Compose-only UI with screens for Garden, Study Timer, Shop, Inventory, House, Profile, and Settings.
- Offline local save system using DataStore Preferences with JSON serialization and safe defaults.
- Study presets: 15, 25, 50 minutes, plus custom timers from 5 to 180 minutes.
- Timer state persists locally and rewards are calculated from real elapsed wall time when the app returns.
- Pause support with a 3-pause limit per session.
- Rewards: 1 coin and 1 XP per completed minute, preset bonuses, streak bonuses, daily quest bonuses.
- Level progression with gradually increasing XP thresholds.
- Shop categories for seeds, decorations, furniture, cosmetics, paths/fences, and upgrades.
- Inventory-driven garden placement and house furniture placement.
- Crops that grow after completed study sessions and can be harvested for small coin bonuses.
- Achievements, daily quests, study history calendar, profile stats, and character cosmetic equipping.
- Optional local notifications, sound toggles, haptic feedback, day/night garden visuals, and reset progress confirmation.
- Settings includes a local test mode that unlocks every item for unlimited free placement and cosmetic testing.
- No sign-in, ads, in-app purchases, subscriptions, online accounts, or network requirements for gameplay.

## Project Structure

```text
app/src/main/java/com/studygrove/app
  data
    local              DataStore save/load
    model              Serializable game state and catalogs
    repository         Game mutations and save orchestration
  domain
    achievements       Achievement unlock rules
    progression        XP levels and crop growth
    quests             Daily quest updates
    rewards            Coin, XP, streak reward math
    timer              Real-time focus timer calculations
  ui
    components         Pixel-style shared Compose widgets and sprites
    garden             Home garden screen and tile grid
    timer              Study timer screen
    shop               Shop screen
    inventory          Inventory and cosmetics
    house              Interior placement screen
    profile            Stats, achievements, calendar
    settings           Sound, notification, reset preferences
  util                 Notifications and simple generated tones
```

## Extending The Game

- Add shop items in `Catalogs.kt`. Pick a category, unlock level, price, placement target, and visual key.
- Add new crop behavior by setting `growthSessions` and `harvestCoins` on a seed item.
- Add achievements in `AchievementCatalog`, then add the unlock condition in `AchievementEngine`.
- Add daily quests in `DailyQuestCatalog`, then update progress from `DailyQuestEngine`.
- Add new pixel visuals by extending `PixelItemSprite` in `PixelComponents.kt`.

All gameplay state flows through `StudyGroveRepository`, so new systems should update `GameData` there or in small domain engines called by the repository.
