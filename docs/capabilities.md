# Capabilities reference

Everything the Alpha2 can do, and how an app reaches it. This is the practical
map: the robot is an ordinary Android device (5.1.1, RK3288) wearing a robot
body, so its capabilities are split across **four** access surfaces, and choosing
the right one matters.

## The four access surfaces

1. **UBTECH SDK** (`Alpha2RobotApi`) — servos, speech/TTS, actions, ear/eye LEDs,
   noise/mic. Wraps AIDL to the on-robot serial services. These calls are the ones
   the store-authorization gate blocked; the fork removes that gate.
2. **Android broadcasts** to/from the system service (`com.ubtechinc.services.MainService`)
   — head keys, sound direction, wakeup, gesture, QR, UUID, RTC, charge/power,
   Wi-Fi/Bluetooth state. Register a `BroadcastReceiver` / `sendBroadcast`.
3. **Standard Android framework APIs** — camera, microphone, the IMU
   (accelerometer), battery/charging, Wi-Fi, Bluetooth, networking. Because it's a
   normal Android device, these work with no UBTECH involvement at all.
4. **A remote TCP socket** (port 6100, MessagePack) used by the phone/PC companion
   app. Some "query" results (battery %, firmware versions, servo angle read-back)
   are only returned here — **not** to on-robot apps. Noted per capability below.
   **This SDK ships no client for it:** there is no socket-opening code in the
   library (only `StaticValue.SERVER_PORT = 6100`). A consumer wanting these
   query results must implement the TCP client and MessagePack framing itself.

> Rule of thumb: if it's a *robot* thing (a joint, an LED ring, a head touch, sound
> direction) use the SDK or a service broadcast; if it's a *device* thing (camera,
> IMU, battery, network) use the ordinary Android API. Several capabilities the SDK
> hides (raw accelerometer, battery %, camera) are trivially available through
> Android.

---

## Motors / servos

- **20 servos**, addressed **id 1–20** in the single-servo API (internally 0–19),
  on a digital serial bus driven by the chest board. Two torque classes (20 kg·cm,
  8 kg·cm). Per-joint min/max clamps are enforced by the SDK before sending.
- **Servo map** (established from a working community app's real usage):
  - **19 = head pan** (left/right), **20 = head tilt** (up/down)
  - **7, 11, 12, 16 = leg/body** joints (used together to lean/stand)
  - **3, 6 = arm** joints
  - (Remaining ids fill out the 20 DOF across both arms and legs.)
- **Set one joint:** `chest_SendOneFreeAngle(byte id, int angle, short timeMs)`.
  On the wire: serial cmd 5, payload `[id][angle:hi,lo][time:hi,lo]` (16-bit
  big-endian; `time` ms, floored to 20).
- **Set all joints:** `chest_SendFreeAngle(int[20], short timeMs)` — serial cmd 52,
  20 angle bytes + time. **Value `250` in any slot = "hold this joint, don't move".**
- **Read a joint's angle:** exposed only over the port-6100 socket
  (`GETMOTORANGLE`), **not** in `Alpha2RobotApi` and not as a broadcast. On-robot
  apps get no servo feedback (no angle, load, or current).
- **Cut servo torque:** the service issues motor-power-off (serial cmd 25) after an
  action completes; there is no public API to hold a joint limp on demand.
- **Higher-level motion:** named action files (`.ubx`) via
  `action_PlayActionName(name)` / `action_StopAction()`; enumerate with
  `action_getActionList` (types: 1 action, 2 dance, 3 story). A real robot returned
  **123 named actions** (87 actions/poses, 25 dances, 10 stories), including
  pre-programmed poses such as crouch-and-stand, `squat pose`, `one foot standing`,
  nod/shake-head, raise/wave hands, and leg kicks. ~171 raw `.ubx` files live under
  `/sdcard/actions/` (opaque numeric filenames; the SDK maps friendly names to them).
- **Playing a pose without any app code:** the service accepts a broadcast, so any
  named pose can be triggered from a shell (handy for testing). Verified driving a
  head-shake and a hand-wave — servos and ear/eye LEDs move together:

  ```
  adb shell am broadcast -a com.ubtechinc.services.thirdparty.playaction \
    --es TP_alphacmdName "Shake head"
  ```
- **Safety:** no feedback means no bump detection from servos; respect recommended
  limits (head tilt / servo 20 is the classic burn-out) and let a move finish
  before commanding the next.

## Head touch — two independent paths

The head touch board reports a key id. It reaches apps **two ways**, and an app can
use either:

1. **Service broadcast** `com.ubtechinc.key` (`AlphaConstant.UBTEK_KEY_BROADCAST`),
   `Byte` extra `key` (`UBTEK_KEY_VALUE`). Known ids and the service's built-in
   behaviour: **4** = volume up, **5** = volume down, **6** = stop action/TTS/alarm
   (also injected on sleep), **7** = start QR scanner, **10** = reserved. Every id
   is broadcast regardless of built-in handling.
2. **Android `KeyEvent`** to the focused activity (`onKeyUp`/`dispatchKeyEvent`,
   `getScanCode()`). Observed scan codes in a working app: **60 = front button,
   61 = back button, 66 = (used to toggle head tracking)**.

**Verified on hardware:** the head has exactly **two** software-readable touch
inputs — **front pad** (`KEY_F2` / scancode 60 / key id 5 / volume down) and **back
pad** (`KEY_F3` / scancode 61 / key id 4 / volume up). Ids 6/7/10 are gestures
(tap/long-press) on those same two pads. The side "strips" and back "buttons" are
**not** separately readable (they're the physical extent of the front/back zone),
and the **chest button** is wired to power management, not `/dev/input`, so no app
can read it. Plan for two head inputs.

> Practical note: read the `key` byte from the broadcast, or handle the KeyEvent
> scancodes (60/61) — both channels report the same two pads.

## Cameras / vision

- **Access via standard Android camera APIs** — not the SDK. The platform runs
  `com.android.camera2`, but camera2 is forced to legacy mode
  (`camera2.portability.force_api=1`), so apps use the classic `android.hardware.Camera`.
- Capture supports up to ~5 MP; the code is multi-camera aware (front/rear), and a
  working vision app selects the camera via OpenCV's `setCameraIndex` (documented
  front/back indices 98/99).
- **Computer vision** (face detect/track, recognition) is done with **OpenCV**
  (e.g. an LBP frontal-face cascade), feeding face-centre coordinates into servo
  19/20 for head tracking — a closed loop built entirely app-side.
- Shipped camera apps on the robot: `com.ubtech.smartcamera`,
  `com.alpha2.videoSupervision` (remote video monitoring).

## Ultrasound / sonar

- Two ultrasonic sensors on the chest, for **anti-collision**.
- **Off by default.** Verified on hardware: waving a hand in front of a
  freshly-booted robot produces **no** obstacle frames — only battery telemetry on
  the chest serial. The sonar must be armed first.
- **Arming frame:** the robot's own service arms the sonar with chest serial
  command **4** (`CHEST_CMD_SETTING`), payload **`{2, distance}`** — a non-zero
  distance arms it at that trigger range; `{2, 0}` disables it. This is the only
  sonar command the service uses (there is no separate "on" opcode). This fork
  exposes it as `Alpha2RobotApi.chest_configureSonar(int distance)`.
- **Runtime signal is boolean only:** obstacle present / removed (chest serial cmd
  `-127`, head `-128`), routed through the action/flowchart **event system**; the
  **raw distance is never reported** to apps.
- **Status / caveat:** `chest_configureSonar(100)` sends successfully
  (`API_ERROR_SUCCEED`) but in testing did **not** by itself start obstacle
  streaming. The service arms the sensor through its internal common serial
  instance and a disable-then-configure sequence, and the exact distance unit the
  PC editor uses is unknown, so a single app-side configure may be insufficient.
  Getting streaming to start reliably needs either replicating that exact sequence
  or a low-level route that writes frames straight to the body serial node
  `/dev/ttyS1` (which needs root). Treated as a documented, partly-open item —
  don't blind-guess serial frames to force it.
- The **Lynx** hardware variant uses **infrared** sensors instead of sonar.

## IMU / accelerometer

- **The SDK exposes only derived fall events**, not raw motion: chest `-105` /
  head `-125`, where `param[0]` encodes the fall side (1 = fell backward → play
  get-up-from-lying; 0 = fell forward → play get-up-from-prone). The service reacts
  by playing a stand-up action.
- **For raw acceleration, use the standard Android `SensorManager`**
  (`TYPE_ACCELEROMETER`). **Verified on hardware:** the accelerometer reads gravity
  correctly (~9.8 m/s² on Z upright) and tracks tilts cleanly (axes redistribute as
  the robot leans). It is the input device named `gsensor` (`/dev/input/event1`),
  but read it through the sensor framework, not raw `/dev/input`.
- **It is the only real IMU sensor.** The sensor HAL also lists gyroscope,
  magnetometer, proximity, light, pressure and temperature, but those are AOSP
  placeholders that read `0.0` — no hardware behind them. Treat the Alpha2 as
  having an accelerometer only (no gyro).

## Battery and charging

- **Read battery level via the standard Android battery API** —
  `BatteryManager.BATTERY_PROPERTY_CAPACITY` for a one-shot percentage, or an
  `ACTION_BATTERY_CHANGED` receiver for continuous level/plugged/status. This is
  the practical path for on-robot apps and it returns a real percentage.
- The chest board streams battery percentage on the chest serial (`CHEST_SEND_POWER`
  = `-128`, `param[1]`) — confirmed on hardware (observed `0x64` = 100% dropping to
  `0x63` = 99% while running on battery), and the service does fire a
  `sendPowerBroadcast`. But `Alpha2RobotApi` has no battery getter, so the
  simplest reliable read for an on-robot app is still the Android battery API.
- **Charging / DC state:** the chest reports charger on/off (serial `-118`); the
  practical read for an app is again the Android battery intent's plugged/status
  extras.
- **Charge-and-play** (allow motion while charging): broadcast
  `com.ubtechinc.services.SET_CHARGE_PLAY`, boolean extra `open_charge_play`.
- **Power save / sleep:** broadcast `com.ubtechinc.services.POWER_SAVE`, boolean
  extra `should_save_power` (also emits head key id 6 on sleep).
- Chest status LED reflects power state (charging red, charged green, low blinking
  red, standby slow blue) and is system-driven.

## Temperature

- Motors report an **over-temperature boolean** (chest `-110` / head `-124`,
  `param[0]==1`); the service responds by squatting and cutting servo power. **No
  numeric temperature** is exposed.

## Sound-source localisation

- Broadcast `com.ubtechinc.services.SPEECH_DIRECTION`
  (`StaticValue.ALPHA_SPEECH_DIRECTION`), byte extras `angle` (current head-yaw
  servo angle) and `absoluteAngle` (computed absolute direction of the human
  voice). Drive `chest_SendOneFreeAngle((byte)19, absoluteAngle, (short)500)` to
  face the speaker. Human voices only.

## LEDs

- **Ear and eye LEDs via the SDK:** `header_startEarLED(up,down,run)` (head serial
  cmd 1), `header_startEyeLED(color,up,down,run)` (cmd 2), `header_stopEarLED/…EyeLED`
  (cmd 8). Eye colour codes: 1=R, 2=G, 3=B, 4=RG, 5=RB, 6=GB, 7=RGB; each eye is an
  8-LED ring. Times in ms.
- Also a broadcast surface: `com.ubtechinc.services.LED_ACTION`
  (`ALPHA_LED_ACTION`) with a control-type + ear/eye + open/close.
- `LED_MOUTH` exists as a constant but has **no command path** in the service.
- Physically, the eye LED ring is driven from the **5-mic array board**
  (`com.ubtechinc.mic5.LedControl`). Eye state is also settable via named action
  assets (some actions carry LED frames).

## Speech, TTS, ASR

- SDK speech stack (see [api-reference.md](api-reference.md)). Languages `en_us` /
  `zh_cn`; Chinese voice names; `speech_SetMIC(true)` releases the mic to your app.
- **Voice commands / recognition — see [speech-recognition.md](speech-recognition.md)
  for the full picture.** In short: the active recogniser is **Nuance VoCon**
  (offline; iFlytek is present but inactive, so `speech_initGrammar` compiles but
  has no effect). Recognition is gated behind the wake word **"hello alpha"**, and
  results are delivered to `IAlpha2RobotClientListener.onServerCallBack(String)` as
  `Local_Result:rule:<RULE> action:<INTENT> tag:<text>`. The built-in grammar is
  rich — **48 `Action_Performance` motion intents** (`AP_WAVE_THE_LEFT_HAND`,
  `AP_SQUAT`, `AP_STAND_UP`, `AP_DANCE`, …) and **13 `QA` intents**, all listed in
  that doc. Note: the robot performs `AP_*` moves itself (contends with a mapped
  action); on `QA_*` it only speaks, so a gesture you add there lands cleanly.
- Wakeup arrives as broadcast `com.ubtechinc.robot.tts_hint_wakeup`
  (`ALPHA_TTS_HINT`), string extra `hint_event` = `enter_wakeup` / `wakeup`.
- The robot **can run fully offline** — offline ASR and on-device face detection
  work with no UBTECH cloud (relevant now that the cloud is gone; the Nuance cloud
  recogniser at `*.nuancemobility.net` is dead).
- Microphone (raw) is available through Android `AudioRecord` (48 kHz, 16-bit PCM);
  there is a dedicated audio-DSP node (`/dev/zl380tw`, echo-cancel) on the mic array.

## Gesture

- Camera-based gesture recognition broadcasts `come.ubt.alpha2.gesture`
  (`ALPHA_GESTURE_ACTION`, note the `come.` typo), int extra `getstureDirection`.

## QR code

- Head key id 7 (or the scan flow) starts the QR service; results arrive on
  `com.ubt.alpha2.qr_code` (`ALPHA_QR_CODE`), string extra `uncode_result`, boolean
  `flag`. When `flag` is true the payload is Wi-Fi-config JSON.

## Device identity / firmware / time

- **Robot serial/UUID:** `requestRobotUUID()` → reply broadcast
  `com.ubtechinc.robot_uuid.info` (`APP_ROBOT_UUID_INFO`), string extra
  `robot_uuid` (read from the chest EEPROM). This is a real app-facing round-trip.
- **Firmware versions** (chest/head) are queryable only over the port-6100 socket,
  not returned to on-robot apps. `Alpha2RobotApi.getSdkVersion()` /
  `getServerVersion()` are available locally.
- **RTC:** broadcast `com.ubtechinc.services.SET_RTC_TIME` (`ALPHA_SET_RTC_TIME`),
  serializable extra `rtc_time` (year/month/day/week/h/m/s); also sets the Android
  clock.

## Wi-Fi / Bluetooth / networking

- Wi-Fi and Bluetooth **state** are mirrored through service broadcasts
  (`ALPHA_WIFI_RESULT`, `ALPHA_BT_CONNECTION` with byte extra `BT_FLAG`, plus
  BT client connected/disconnected/data actions), but ordinary Android
  `WifiManager` / `BluetoothAdapter` also work.
- General networking is plain Android (`HttpURLConnection`, sockets, `MediaPlayer`
  for streaming audio). No GPS hardware (`ro.factory.hasGPS=false`); geolocation, if
  needed, is by IP.

---

## Access-surface summary

| Capability | Best access for an on-robot app |
|-----------|--------------------------------|
| Servos (set) | SDK `chest_SendOneFreeAngle` / `chest_SendFreeAngle` |
| Servo angle read-back | **Not available** to on-robot apps (socket-only) |
| Named actions/dances | SDK `action_PlayActionName` |
| Head touch | Broadcast `com.ubtechinc.key` **and/or** Android `KeyEvent` scan codes |
| Camera / vision | Android `Camera` API (+ OpenCV) |
| IMU / accelerometer | Android `SensorManager` (SDK gives fall events only) |
| Battery % / charging | Android `BatteryManager` / `ACTION_BATTERY_CHANGED` |
| Sonar | Boolean obstacle event (SDK/flowchart); no distance; raw via serial |
| Temperature | Over-temp boolean only |
| Sound direction | Broadcast `ALPHA_SPEECH_DIRECTION` |
| Ear/eye LEDs | SDK `header_start*LED` |
| Mouth LED | Not exposed |
| Speech / TTS / ASR | SDK speech stack (offline-capable) |
| Microphone (raw) | Android `AudioRecord` |
| Gesture / QR / UUID / RTC | Service broadcasts |
| Wi-Fi / BT / network | Android framework (state also on broadcasts) |

## What no surface gives you

Raw IMU through the SDK, numeric temperature, numeric sonar distance, and servo
angle/load/current feedback to on-robot apps. For raw motion and battery %, go to
the Android framework; for sonar distance and low-level sensor toggles, the only
route is writing UBTECH protocol frames to the serial node directly, outside the
SDK.
