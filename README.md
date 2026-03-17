# usageDirectPlus

A fork of [usageDirect](https://codeberg.org/fynngodau/usageDirect) that adds detailed app usage interval tracking, screen lock event logging, and periodic database export. The codebase has been fully modernized to current Android development standards.

## Modernization

The original usageDirect targeted SDK 28 and was written entirely in Java. This fork has been brought up to 2025-2026 standards:

- **100% Kotlin** — all 91 Java source files converted
- **Target SDK 34**, min SDK 26 (Android 8.0+)
- **Kotlin DSL** build files with a Gradle version catalog (`libs.versions.toml`)
- **Kotlin 2.0**, Room with KSP, Java 17
- **ViewBinding** replacing all `findViewById` calls
- **Coroutines** replacing raw Threads, Timers, and Handlers
- **AppCompatActivity** and **Activity Result API** replacing deprecated patterns
- **Material 3** theme and `MaterialAlertDialogBuilder` (database flavor)

## What's new

### App usage intervals
The original usageDirect stores only daily aggregate screen time (total milliseconds per app per day). usageDirectPlus also persists the raw start/stop timestamps for every foreground session in a new `usageIntervals` table. This enables analysis of *when* and *how long* each app session lasted, not just the daily total.

### Screen event tracking
Captures screen on/off events (API 25+) and lock screen (keyguard) show/hide events (API 28+) in a new `screenEvents` table. This lets you count unlocks, measure screen-on duration, and correlate usage patterns with device wake cycles.

### Periodic database export
A new settings screen lets you configure automatic export of the SQLite database to any directory via Android's Storage Access Framework (SAF). Options include:
- Toggle auto-export on/off
- Choose export directory
- Set interval (6h / 12h / 24h / 48h / 7 days)
- One-tap "Export now" button

Exports are timestamped (`usageDirectPlus-2026-03-16_143000.sqlite3`) so they never overwrite each other.

### Faster sync cycle
The background data collection interval has been reduced from 24 hours to 6 hours for more timely data.

## Database schema (v6)

### Existing tables (unchanged)
- **usageStats** — daily aggregate usage per app (day, timeUsed, applicationId, hidden)
- **lastUsed** — last-used timestamp per app
- **colors** — user-assigned app colors

### New tables

#### usageIntervals
| Column | Type | Notes |
|---|---|---|
| beginTime | INTEGER NOT NULL | Epoch ms, composite PK |
| endTime | INTEGER NOT NULL | Epoch ms |
| applicationId | TEXT NOT NULL | Package name, composite PK |

Indices: `applicationId`, `beginTime`

#### screenEvents
| Column | Type | Notes |
|---|---|---|
| timestamp | INTEGER NOT NULL | Epoch ms, PK |
| eventType | INTEGER NOT NULL | 15=SCREEN_ON, 16=SCREEN_OFF, 17=KEYGUARD_SHOWN, 18=KEYGUARD_HIDDEN |

Index: `timestamp`

## Building

```bash
git clone <this-repo>
cd usageDirectPlus

# Build the database flavor (the one with persistent storage)
./gradlew assembleDatabase
```

The APK will be at `Application/build/outputs/apk/database/release/`.

### Requirements
- JDK 17+
- Android SDK with compileSdk 34
- Gradle 8.4+ (wrapper included)

## Verification queries

After the first sync cycle, you can inspect the exported database:

```sql
-- Recent app usage intervals
SELECT * FROM usageIntervals ORDER BY beginTime DESC LIMIT 20;

-- Unlock count today (API 28+ devices)
SELECT COUNT(*) FROM screenEvents
WHERE eventType = 18
  AND timestamp >= strftime('%s','now','start of day') * 1000;

-- Daily screen-on time from intervals
SELECT date(beginTime/1000, 'unixepoch', 'localtime') AS day,
       SUM(endTime - beginTime) / 1000.0 / 60 AS minutes
FROM usageIntervals
GROUP BY day
ORDER BY day DESC;
```

## License

GNU General Public License v3.0 — same as the original usageDirect.

---

Forked from [usageDirect](https://codeberg.org/fynngodau/usageDirect) by Fynn Godau, which was itself forked from [the Android sample called "AppUsageStatistics"](https://github.com/googlesamples/android-AppUsageStatistics) (Copyright 2017 The Android Open Source Project, Inc).
