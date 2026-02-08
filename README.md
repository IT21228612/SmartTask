# SmartTask 📱🧠  
*A Context-Aware Adaptive To-Do Application for Android*

SmartTask is a **privacy-first, context-aware Android to-do application** that dynamically prioritizes tasks and delivers notifications when users are most receptive. Instead of static reminders, SmartTask adapts to **location, activity, time, calendar state, and device conditions** to reduce interruption cost and improve task completion.

---

## 🚀 Key Features

- **Dynamic Task Prioritization**
  - Combines urgency, contextual relevance, learned behavior, and interruption cost
- **Context-Aware Notifications**
  - Delivers reminders only when the user is likely to act
- **Offline & Privacy-First**
  - All data stored locally on-device
- **Adaptive Learning**
  - Learns from user interactions (complete, snooze, dismiss)
- **Battery-Optimized**
  - Uses geofencing and throttled background work

---

## 🧩 System Architecture

SmartTask is built using a **modular architecture**:

### Core Modules
- **Task Input Module**
  - Task creation, editing, metadata management
- **Context Acquisition Engine**
  - Location, activity, calendar, and device state
- **Context Matching & Triggering**
  - Determines which tasks are relevant to current context
- **Task Prioritization Engine**
  - Scores tasks dynamically
- **Adaptive Notification System**
  - Schedules and suppresses notifications intelligently
- **Data Logging & Insights**
  - Logs interactions for on-device learning and analytics

---

## 🧠 Task Scoring Model

Each task is assigned a dynamic score:
Score = w1·Urgency + w2·ContextRelevance + w3·BehavioralAffinity - w4·InterruptionCost

- 
### Scoring Factors
- **Urgency** – Based on deadline and estimated duration
- **Context Relevance** – Location, time window, activity match
- **Behavioral Affinity** – Learned probability of task completion
- **Interruption Cost** – High during meetings or driving

---

## 🛠️ Tech Stack

| Layer | Technology |
|------|-----------|
| Platform | Android (Native) |
| Language | Java |
| IDE | Android Studio |
| Database | Room (SQLite) |
| Background Work | WorkManager |
| Location & Geofencing | Google Maps API |
| Activity Detection | Android Activity Recognition API |
| Notifications | Android Notification API |
| Optional ML | Rule-based + Statistical Models (Weka optional) |

---

## 🗂️ Database Schema (Room)

### Task (`tasks`)

| Property | Description | All possible values |
| --- | --- | --- |
| `id` | Auto-generated primary key. | Auto-incrementing `long` (>= 1). |
| `title` | Required task title. | Any non-empty string. |
| `description` | Optional task details. | Any string or `null`. |
| `category` | Label or grouping for the task. | Any string. |
| `createdAt` | Creation timestamp (epoch millis). | `long` (default `0`). |
| `updatedAt` | Last update timestamp (epoch millis). | `long` (default `0`). |
| `dueAt` | Optional due time (epoch millis). | `Long` or `null`. |
| `priority` | Priority score where higher = more important. | `int` (default `0`). |
| `locationLat` | Latitude of an optional geofence/target location. | `Double` or `null`. |
| `locationLng` | Longitude of an optional geofence/target location. | `Double` or `null`. |
| `locationRadiusM` | Radius around the location for triggers (meters). | `Float` or `null`. |
| `locationLabel` | Human-friendly place label. | Any string or `null`. |
| `estimatedDurationMin` | Estimated duration to finish the task (minutes). | `Integer` or `null`. |
| `preferredStartTime` | Optional preferred start time (epoch millis). | `Long` or `null`. |
| `preferredEndTime` | Optional preferred end time (epoch millis). | `Long` or `null`. |
| `notificationsEnabled` | Whether reminders are allowed. | `true`/`false` (default `true`). |
| `completed` | Whether the task is finished. | `true`/`false` (default `false`). |
| `completedAt` | When the task was completed (epoch millis). | `Long` or `null`. |
| `archived` | Whether the task is archived. | `true`/`false` (default `false`). |
| `snoozeUntil` | Time until which reminders are snoozed (epoch millis). | `Long` or `null`. |

### ContextSnapshot (`context_snapshots`)

| Property | Description | All possible values |
| --- | --- | --- |
| `id` | Auto-generated primary key. | Auto-incrementing `long` (`>= 1`). |
| `timestamp` | Capture time (epoch millis). | Any `long` (default `0`). |
| `timezoneId` | Time zone ID at capture. | Any non-null `String` (default `""`; usually IANA IDs like `"Asia/Kolkata"`, `"UTC"`). |
| `dayOfWeek` | Day-of-week from `Calendar.DAY_OF_WEEK`. | `1..7` at runtime (`SUNDAY..SATURDAY`), or default `0` before collection. |
| `minuteOfDay` | Minute offset within the day. | `0..1439` (default `0`). |
| `lat` | Latitude from location collector. | `Double` or `null`. |
| `lng` | Longitude from location collector. | `Double` or `null`. |
| `accuracyM` | Location accuracy in meters. | `Float` or `null`. |
| `speedMps` | Speed in meters/second. | `Float` or `null`. |
| `bearingDeg` | Bearing in degrees when reliable. | `Float` or `null`. |
| `isGeofenceHit` | Whether a geofence event provided context. | `true` / `false` (default `false`). |
| `geofenceId` | Triggering geofence request ID. | Any `String` or `null`. |
| `placeLabel` | Place/task label resolved from geofence. | Any `String` or `null`. |
| `locationSource` | Origin of chosen location sample. | `"FUSED"`, `"CACHED"`, or `"UNKNOWN"` (default `"UNKNOWN"`). |
| `activityType` | Most probable detected activity. | `"IN_VEHICLE"`, `"ON_BICYCLE"`, `"WALKING"`, `"RUNNING"`, `"STILL"`, `"UNKNOWN"`, or `null`. |
| `activityConfidence` | Activity confidence score. | `int` (typically `0..100`, default `0`). |
| `isMoving` | Motion heuristic output. | `true` / `false` (default `false`). |
| `stepsSinceLast` | Steps since previous snapshot. | `Integer` or `null` (currently not populated by collectors). |
| `isInMeeting` | Whether user is currently in a busy calendar event. | `true` / `false` (default `false`). |
| `currentEventId` | Active calendar event ID. | Any `String` or `null`. |
| `currentEventTitle` | Active calendar event title (possibly hashed by privacy settings). | Any `String` or `null`. |
| `eventBusyStatus` | Busy-state marker for current event. | `"BUSY"` or `null` (deriver also handles arbitrary non-`"FREE"` strings defensively). |
| `minutesToNextEvent` | Minutes until next busy event. | `Integer` or `null`. |
| `minutesLeftInEvent` | Minutes remaining in current busy event. | `Integer` or `null`. |
| `batteryPct` | Battery percentage. | `int` (`0..100` when available; default `0`). |
| `isCharging` | Charging state. | `true` / `false` (default `false`). |
| `powerSaveMode` | Power saver status. | `true` / `false` (default `false`). |
| `ringerMode` | Device ringer mode. | `"SILENT"`, `"VIBRATE"`, `"NORMAL"`, or `null`. |
| `doNotDisturbOn` | DND state. | `true` / `false` (default `false`). |
| `screenOn` | Whether device is interactive. | `true` / `false` (default `false`). |
| `deviceUnlocked` | Device lock state inverse (`!isDeviceLocked`). | `true` / `false` (default `false`). |
| `appInForeground` | Whether SmartTask is in foreground. | `true` / `false` (default `false`). |
| `connectivityType` | Active transport bucket. | `"WIFI"`, `"CELLULAR"`, `"NONE"`, or `null`. |
| `isInternetAvailable` | Network validation result. | `true` / `false` (default `false`). |
| `isRoaming` | Telephony roaming state. | `Boolean` (`true` / `false`) or `null`. |
| `headphonesConnected` | Wired headphones/headset detected. | `Boolean` (`true` / `false`) or `null`. |
| `bluetoothConnected` | Bluetooth audio device detected. | `Boolean` (`true` / `false`) or `null`. |
| `wifiSsidHash` | SSID value (hashed or raw, based on privacy settings). | Any `String` or `null`. |
| `noiseLevelDb` | Ambient sound level. | `Float` or `null` (currently not populated by collectors). |
| `interruptionCostScore` | Derived interruption score (`clamp[0,100]`). | `float` in `0.0..100.0`. |
| `receptivityScore` | Derived receptivity score (`clamp[0,100]`). | `float` in `0.0..100.0`. |
| `contextLabel` | Derived context classification. | `"MEETING"`, `"COMMUTING"`, `"LOW_BATTERY"`, `"NO_CONNECTIVITY"`, `"AVAILABLE_ACTIVE"`, `"IDLE_INACTIVE"`, `"EVENING_ACTIVE"`, `"DEFAULT"`, `"AT_<PLACE_LABEL>"`, or `null`. |
| `permissionState` | Aggregate permission health string. | `"OK"` or comma-separated subset of `"LOCATION_DENIED"`, `"ACTIVITY_DENIED"`, `"CALENDAR_DENIED"`, `"BLUETOOTH_DENIED"`. |
| `dataQualityFlags` | Bitmask of data quality issues. | `int` bitmask using: `LOW_ACCURACY=1`, `STALE_LOCATION=2`, `NO_LOCATION=4`, `NO_ACTIVITY=8`, `NO_CALENDAR=16`, `NO_CONNECTIVITY_INFO=32`; any bitwise OR combination including `0`. |
| `anonymized` | Whether privacy anonymization was applied (for example SSID hashing). | `true` / `false` (default `false`). |
| `sourceTrigger` | Snapshot trigger source. | `"APP_START"`, `"PERIODIC"`, `"GEOFENCE_ENTER"`, `"GEOFENCE_DWELL"`, `"GEOFENCE_EXIT"`, or fallback/default `"UNKNOWN"` (non-null string). |

---

## 🔔 Notification Logic

- Notifications are triggered only if:
  - Task score exceeds threshold
  - Interruption cost is low
- Suppression rules include:
  - Driving
  - Active meetings
  - Low battery
- Actions:
  - **Done**, **Snooze**, **Postpone** (all logged for learning)

---

## 🔐 Privacy & Security

- 100% **local data storage** by default
- No cloud sync unless explicitly enabled
- Runtime permission handling with user consent
- Optional encryption for sensitive tables

---

## ⚡ Battery Optimization

- Geofencing instead of continuous GPS
- Adaptive sampling based on battery level
- Background work scheduled via WorkManager

---

## 🧪 Testing Strategy

- **Unit Tests**: JUnit (scoring logic, database)
- **UI Tests**: Espresso
- **Sensor Simulation**: Emulator + Mockito
- **Evaluation Metrics**:
  - Task completion rate
  - Missed reminders
  - NASA-TLX workload score
  - User surveys

---

## 🔄 CI / Deployment

- **Version Control**: Git & GitHub
- **CI**: GitHub Actions (unit tests, static checks)
- **Deployment**: Google Play Store

---

## 📌 Project Status

🚧 **Active Development**  
Initial implementation focuses on rule-based prioritization with gradual integration of on-device learning.

---

## 📄 License

This project is licensed under the **MIT License** (or update as applicable).

---

## 🙌 Acknowledgements

- Android SDK & Jetpack Libraries
- Google Maps & Activity Recognition APIs

---

## 📬 Contact

For questions, feedback, or collaboration, feel free to open an issue or pull request.
