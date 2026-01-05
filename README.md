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

### Entities
- **Task**
  - `id`, `title`, `description`, `due_at`, `location`, `priority_tag`, etc.
- **ContextSnapshot**
  - `timestamp`, `location`, `activity`, `battery`, `connectivity`
- **InteractionLog**
  - `task_id`, `action`, `timestamp`, `context_snapshot_id`

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


