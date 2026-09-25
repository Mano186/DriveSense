# DriveSense

DriveSense is a mid-level Android sensor project for the Mobile Systems assignment.

* Kotlin, min SDK 34, target/compile SDK 36
* Three Activities: Dashboard, Drive Monitor, and History
* Intent flow: Dashboard → Monitor → History; Monitor returns a safety score to Dashboard
* Sensors: `TYPE_LINEAR_ACCELERATION` and `TYPE_GYROSCOPE`
* CustomView: `AccelerationGraphView`, which plots recent acceleration readings
* RecyclerView: saved drive history
* Drive Detail Activity: event-by-event review and simple personalised coaching
* Material 3 UI: dynamic Android colour support, rounded cards, touch-friendly controls, and animated CustomView safety-score rings
* Local-only SharedPreferences storage (latest 20 sessions), with no GPS, camera, microphone, account, or cloud access

## Demo and phone testing

Use **Run Safe Demo Events** in the monitor to create three sample events without moving the phone. It is intended for emulator tests and viva demonstrations; no sensors are read in this mode.

For real-device testing, secure the phone, enable Driving mode, and make only safe stationary movements. The two sliders adjust event sensitivity: lower values detect smaller movements and higher values reduce false positives. See `TEST_CASES.md` for the submission test checklist.

## Viva notes

Linear acceleration is used because it excludes gravity, which makes the braking/acceleration explanation simpler. The gyroscope measures rotational speed in rad/s, allowing the app to flag sharp turns. Thresholds are deliberately simple and can be tuned after device testing.

Secure the phone before a test drive. This is an awareness/demo app, not a safety system.
