# Example: HelloAlpha (sensor-driven smoke test)

`examples/HelloAlpha` is a minimal app that exercises the three subsystems the
SDK drives on the robot — **speech (TTS)**, **action playback**, and **direct
servo control** — and confirms that the de-restriction holds on real hardware.

Because the Alpha2 has no user-visible touchscreen, the app is triggered by the
robot's own **head-touch sensors**, not on-screen buttons.

## What it does

- On start it constructs `Alpha2RobotApi`, initialises speech, action, and both
  serial links, and speaks a short greeting once speech is ready (so a user with
  no screen knows it is live).
- It registers a `BroadcastReceiver` for `AlphaConstant.UBTEK_KEY_BROADCAST`
  (`com.ubtechinc.key`) and reads the head key id from the `Byte` extra `key`.
- Touching the head runs a subsystem test: the front pad speaks; the back pad
  alternates between playing the first action and turning the head (servo 19).
  Each call logs its `API_ERROR_CODE` on screen and to logcat (tag `HelloAlpha`).
- **Voice reactions:** it parses the robot's speech-recogniser results (delivered
  on `onServerCallBack` as `Local_Result:rule:… action:… tag:…`) and plays a
  gesture for the `QA_*` intents it maps — after the wake word "hello alpha",
  "how tall are you" → wave, "how old are you" → raise both hands. It maps only
  `QA_*` (where the robot just answers verbally); `AP_*` motion intents are logged
  but left to the robot, which performs them itself. Every recognised phrase is
  logged. See [speech-recognition.md](speech-recognition.md) for the engine, the
  delivery mechanism, and the full grammar.
- It also logs every raw head/chest serial frame (hex) and every Android
  `KeyEvent` (`dispatchKeyEvent`), plus wakeup/speech broadcasts.
- **IMU:** it registers the accelerometer via the standard Android `SensorManager`
  and logs readings on motion, so tilting the robot is visible in the log.
- **Poses:** at startup it fetches and logs every pre-programmed action/pose name
  (via `action_getActionList`), so you can see what the robot ships with.

## Reading the result

- `API_ERROR_SUCCEED` + the robot physically responds → the SDK works and the
  client-side de-restriction holds.
- `API_ERROR_NOT_INIT` → the matching `init*Api` had not completed; wait for the
  "ready" log lines.
- `API_ERROR_APPID_NOT_ACTIVE` / `API_ERROR_AUTHORIZE_ERROR` → the de-restriction
  is not in effect for this build (see [authorization.md](authorization.md)).

## Build and run

See [getting-started.md](getting-started.md). In short: build the SDK, copy the
`.aar` into `app/libs/`, `./gradlew assembleDebug`, `adb install -r`, launch, and
`adb logcat -s HelloAlpha`, then touch the head.

## Status

The head-input map is now confirmed on hardware: the head has exactly two
software-readable touch pads — front (key id 5) and back (key id 4) — so the app
maps the front pad to speech and the back pad to the action/servo tests. The side
strips, back buttons, and chest button are not separately readable (see
[sensors-and-events.md](sensors-and-events.md)), so two triggers is the ceiling
for head-driven input. For more distinct triggers, combine head touch with other
sensors (e.g. sonar obstacle events or the accelerometer via Android
`SensorManager`).
