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
| `id` | Auto-generated primary key. | Auto-incrementing `long` (>= 1). |
| `timestamp` | Capture time (epoch millis). | `long`. |
| `timezoneId` | IANA time zone ID at capture. | Any non-empty string (e.g., `"UTC"`). |
| `dayOfWeek` | Day-of-week value from the calendar. | `int` (calendar index). |
| `minuteOfDay` | Minute within the day (0–1439). | `int`. |
| `lat` / `lng` | Location coordinates. | `Double` or `null`. |
| `accuracyM` | Location accuracy in meters. | `Float` or `null`. |
| `speedMps` | Speed in meters/second. | `Float` or `null`. |
| `bearingDeg` | Bearing in degrees. | `Float` or `null`. |
| `isGeofenceHit` | Whether a geofence triggered the snapshot. | `true`/`false`. |
| `geofenceId` | Identifier of the triggering geofence. | Any string or `null`. |
| `placeLabel` | Human-friendly place label. | Any string or `null`. |
| `locationSource` | Source of the location reading. | `"CACHED"`, `"FUSED"`, or `"UNKNOWN"`. |
| `activityType` | Detected user activity type. | Activity strings (e.g., `"IN_VEHICLE"`, `"STILL"`, or `"UNKNOWN"`). |
| `activityConfidence` | Confidence score for the detected activity. | `int` (0–100). |
| `isMoving` | Whether motion was detected. | `true`/`false`. |
| `stepsSinceLast` | Steps since the last snapshot. | `Integer` or `null`. |
| `isInMeeting` | Whether the calendar shows a current meeting. | `true`/`false`. |
| `currentEventId` | Current calendar event ID. | Any string or `null`. |
| `currentEventTitle` | Title of the current calendar event. | Any string or `null`. |
| `eventBusyStatus` | Busy/free status of the current event. | Any string or `null`. |
| `minutesToNextEvent` | Minutes until the next calendar event. | `Integer` or `null`. |
| `minutesLeftInEvent` | Minutes remaining in the current event. | `Integer` or `null`. |
| `batteryPct` | Battery level percentage. | `int` (0–100). |
| `isCharging` | Whether the device is charging. | `true`/`false`. |
| `powerSaveMode` | Whether power-save mode is active. | `true`/`false`. |
| `ringerMode` | Current device ringer mode. | `"SILENT"`, `"VIBRATE"`, or `"NORMAL"`. |
| `doNotDisturbOn` | Whether DND is enabled. | `true`/`false`. |
| `screenOn` | Whether the display is interactive. | `true`/`false`. |
| `deviceUnlocked` | Whether the device is unlocked. | `true`/`false`. |
| `appInForeground` | Whether SmartTask is in the foreground. | `true`/`false`. |
| `connectivityType` | Active connectivity type. | `"WIFI"`, `"CELLULAR"`, or `"NONE"`. |
| `isInternetAvailable` | Whether internet access is validated. | `true`/`false`. |
| `isRoaming` | Whether the device is roaming. | `Boolean` (`true`/`false`) or `null`. |
| `headphonesConnected` | Headphones presence. | `Boolean` (`true`/`false`) or `null`. |
| `bluetoothConnected` | Whether a Bluetooth device is connected. | `Boolean` (`true`/`false`) or `null`. |
| `wifiSsidHash` | Hashed SSID for privacy. | Any string or `null`. |
| `noiseLevelDb` | Ambient noise level in decibels. | `Float` or `null`. |
| `interruptionCostScore` | Derived interruption cost metric. | `float`. |
| `receptivityScore` | Derived receptivity metric. | `float`. |
| `contextLabel` | Derived label for the context. | Any string or `null`. |
| `permissionState` | Permission health for the snapshot. | `"OK"` or descriptive strings. |
| `dataQualityFlags` | Bitmask of data quality issues. | `int` (see `DataQualityFlags`). |
| `anonymized` | Whether the snapshot is anonymized. | `true`/`false`. |
| `sourceTrigger` | What triggered the capture. | Strings like `"APP_START"`, `"PERIODIC"`, `"GEOFENCE_ENTER"`, `"GEOFENCE_DWELL"`, `"GEOFENCE_EXIT"`, or `"UNKNOWN"`. |

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
