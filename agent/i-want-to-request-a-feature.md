# Suggested Features for Future Backlogs

Here are some suggested high-impact, elite features to enrich the EduPrep Offline app, focusing on retaining students and providing rich interactive tools offline:

## 1. Local Leaderboard & Peer Challenge (Wi-Fi Direct/Bluetooth)
- **Concept**: Since internet can be scarce or expensive for West African students, peer learning is highly effective. Peer-to-peer multiplayer quiz battles over Wi-Fi Direct or Bluetooth allow offline gamification.
- **Implementation**: Integrate Android's Wi-Fi Direct (P2P) API or Nearby Connections API. One student hosts a session, and peers join to answer the same set of CBT questions. A local high score board is updated dynamically.

## 2. Interactive Virtual Labs (2D Canvas Sandbox)
- **Concept**: A physical science simulator (e.g., Simple Harmonic Motion/Pendulum, Circuit builder, Acid-Base Titration) to assist WAEC/JAMB practical science preparation.
- **Implementation**: Standard Jetpack Compose `Canvas` with stateful sliders for variables (string length, gravity, resistance, drop speed) updated in a `Flow` or `StateStateFlow` running physics calculations.

## 3. Local Study Alarms & Boot Receiver
- **Concept**: To preserve streak counts and retain students, local study alarms help students remember to study at specific hours even with 0 cellular data.
- **Implementation**: Use `AlarmManager` with `setExactAndAllowWhileIdle()` triggering a `StudyReminderReceiver` class. A `BootReceiver` restores scheduled alarms upon device restarts by reading from Jetpack Preferences DataStore.

## 4. University Admission Cut-off Predictor
- **Concept**: A simple offline database/calculator that accepts mock JAMB scores and suggests potential matching Nigerian/West African universities based on their historical cut-off marks.
- **Implementation**: Local SQLite table containing university course requirements, mapping scores to cut-off data points instantly.
