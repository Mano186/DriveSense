# DriveSense

DriveSense is a mid-level Android sensor project for the Mobile Systems assignment.

* Kotlin, min SDK 34, target/compile SDK 36
* Three Activities: Dashboard, Drive Monitor, and History
* Intent flow: Dashboard → Monitor → History; Monitor returns a safety score to Dashboard
* Sensors: `TYPE_LINEAR_ACCELERATION` and `TYPE_GYROSCOPE`
* CustomView: `AccelerationGraphView`, which plots recent acceleration readings
* RecyclerView: saved drive history
* Local-only SharedPreferences storage (latest 20 sessions), with no GPS, camera, microphone, account, or cloud access

## Viva notes

Linear acceleration is used because it excludes gravity, which makes the braking/acceleration explanation simpler. The gyroscope measures rotational speed in rad/s, allowing the app to flag sharp turns. Thresholds are deliberately simple and can be tuned after device testing.

Secure the phone before a test drive. This is an awareness/demo app, not a safety system.
