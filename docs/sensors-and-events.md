# Sensors and events

This is the most important — and least documented — part of programming the
Alpha2. The official demo never shows physical-sensor handling; the following was
reconstructed from the SDK, the on-robot service, and hardware testing.

## Two delivery paths

Sensor and event data reaches your app in one of two ways:

1. **Android broadcasts.** The on-robot service (`Alpha2Services`) reads the
   serial links, decodes events, and re-broadcasts them as ordinary Android
   `Intent`s. Your app registers a `BroadcastReceiver`. This is the path that
   actually works for most physical input.
2. **Raw serial callbacks.** `Alpha2RobotApi` exposes two overridable methods,
   `onListenSerialPortRcvData(byte[], int)` (chest board) and
   `onListenSerialPortHeaderRcvData(byte[], int)` (head board). Subclass the API
   to receive raw frames. In practice these often stay silent because the system
   service consumes the port and only re-broadcasts decoded events — so prefer
   the broadcast path unless you specifically need raw bytes.

The internal chain, for reference: `SerialPortUtl.ReadThread` reads the port →
`AlphaSerialPortServices.onDataReceived` → the service decodes → `sendBroadcast`.

## Head touch — the `com.ubtechinc.key` broadcast

The head board reports a touch as a serial frame with command byte `-127`
(`HEADER_SEND_KEY`) whose first parameter byte is a **key id**. The service acts
on known ids (volume, stop, QR) **and** re-broadcasts the raw key id to all apps:

- **Action:** `AlphaConstant.UBTEK_KEY_BROADCAST` = `"com.ubtechinc.key"`
- **Extra:** `AlphaConstant.UBTEK_KEY_VALUE` = `"key"` — and it is a **`Byte`**, not
  an int. `intent.getIntExtra("key", …)` returns the default because of the type
  mismatch; read it with `getByteExtra("key", …)` or via `getExtras().get("key")`.

### Known head key ids and their built-in behaviour

The on-robot service recognises these key ids (from the head-key handler in
`Alpha2Services`). Every id is broadcast regardless, so your app can react to any
of them — but be aware the service also performs the built-in action:

| key id | Built-in behaviour in the service | Observed pad (hardware) |
|-------:|-----------------------------------|-------------------------|
| `4` | Volume up | one crown pad |
| `5` | Volume down | the other crown pad |
| `6` | Stop current action / TTS / alarm (the manual's "tap to stop") | — |
| `7` | Start the QR-code scanner | — |
| `10` | (reserved; no built-in action) | — |

On hardware we confirmed ids **4** and **5** from the two crown touch pads
(captured as scancodes `0x3d01` and `0x3c01`). ids 6/7/10 exist in the firmware
but were not individually mapped to a physical spot in testing.

### Head input also arrives as Android KeyEvents / kernel keys

The same two head touches also come through as ordinary Android `KeyEvent`s
(`onKeyUp` / `dispatchKeyEvent`, `getScanCode()`) and as raw kernel key events on
the `rk29-keypad` input device (`/dev/input/event0`). All three views agree.

### Confirmed head-input map (verified on hardware)

Testing every physical head input on a real robot, across the broadcast, KeyEvent,
and raw-`getevent` layers, shows the head exposes **exactly two software-readable
touch inputs**:

| Physical input | Kernel key (getevent) | KeyEvent scancode | `com.ubtechinc.key` id | Built-in action |
|----------------|-----------------------|-------------------|------------------------|-----------------|
| **Front pad**  | `KEY_F2` (event0)     | 60                | **5**                  | volume down |
| **Back pad**   | `KEY_F3` (event0)     | 61                | **4**                  | volume up |

(The service maps key id 4 to a volume increase and key id 5 to a volume
decrease; the pad IDs were confirmed on hardware, and the up/down direction is
per the on-robot service's own key handler.)

The additional head-key ids the firmware defines — **6** (tap-to-stop), **7**
(start QR), **10** (reserved) — are produced by *gestures* on these same two pads
(tap vs. swipe vs. long-press), not by separate sensors.

The side "strips" and back "buttons" some owners see are **not separately
readable**: pressing them produced no event on any layer (broadcast, KeyEvent, or
raw kernel input) — the strip is simply the physical extent of the front/back
capacitive zone. The **chest button** likewise produces no `/dev/input` event
(it's wired to power management, not the input subsystem). So plan for two head
inputs, front and back.

### Minimal receiver

```java
IntentFilter f = new IntentFilter(AlphaConstant.UBTEK_KEY_BROADCAST);
registerReceiver(new BroadcastReceiver() {
    public void onReceive(Context c, Intent i) {
        int key = i.getByteExtra(AlphaConstant.UBTEK_KEY_VALUE, (byte) -1);
        // dispatch on key ...
    }
}, f);
```

## Sound-source localisation (turn to the speaker)

- **Action:** `StaticValue.ALPHA_SPEECH_DIRECTION` = `"com.ubtechinc.services.SPEECH_DIRECTION"`
- **Extra:** `getByteExtra("absoluteAngle", (byte)0)` — the absolute head angle to
  turn to face the detected human voice (horizontal only). Convert a negative byte
  to 0–255 (`if (a < 0) a += 256`) and drive the head-yaw servo:
  `chest_SendOneFreeAngle((byte) 19, angle, (short) 500)`.

Only human voices trigger this; other sounds are ignored.

## Wakeup

- **Action:** `StaticValue.ALPHA_TTS_HINT` = `"com.ubtechinc.robot.tts_hint_wakeup"`
- **Extra:** `getString("hint_event")`; value `"wakeup"` means the wake word fired.

## Gesture

- `StaticValue.ALPHA_GESTURE_ACTION` = `"come.ubt.alpha2.gesture"` *(sic)*, with a
  direction in extra `StaticValue.ALPHA_GESTURE_DIRECTION` (`"getstureDirection"`, sic).

## Sonar / obstacle and fall

These are reported as head/chest serial command bytes (i.e. on the raw callback,
or as internal events the service consumes):

- Chest sonar/obstacle: `CHES_SEND_OBSTACLE` (`-127`); head obstacle
  `HEADER_SEND_OBSTACLE` (`-128`).
- Fall: `CHES_SEND_FALLDOWN` (`-105`), `HEADER_FALL_DERECTION` (`-125`, sic).
  `param[0]` encodes the fall side (1 = fell backward, 0 = fell forward); the
  service reacts by playing the matching get-up action.
- Chest touch board: `CHEST_TOUCH_BOARD` (`-119`).

Sonar reaches apps as a **boolean present/absent** event only (the trigger
*distance* is configurable but the raw distance is never reported); fall is a
**side flag** only. For **raw accelerometer** data and **battery %**, do not look
to the SDK — use the standard Android `SensorManager` (`TYPE_ACCELEROMETER`) and
`BatteryManager`, since the robot is a normal Android device. See
[capabilities.md](capabilities.md) for the full breakdown of which surface serves
each capability. The SDK exposes no servo angle/load/current feedback.

## The physical sensor taxonomy

From `HardwareTestValue`: `SOUND=1`, `FALL=2`, `TOUCH_BOARD=3`,
`PRESSURE_SENSOR=4`, `SONAR=5` — i.e. microphone/sound, fall (accelerometer),
capacitive touch board(s), hand-pressure sensor, and sonar (2 ultrasonic on the
chest, anti-collision).

## Companion-app "button" events are not physical buttons

The demo's `APP_BUTTON_EVENT` / `APP_BUTOON_EVENT_CLICK` broadcasts are **virtual
buttons** defined in the app's `assets/button.json` and driven by the UBTECH
companion phone app — a UI surface for a screenless robot, unrelated to the
physical head buttons. Don't confuse them with head-touch input.

## Broadcast quick reference

| Action constant | String | Key extra(s) |
|---|---|---|
| `AlphaConstant.UBTEK_KEY_BROADCAST` | `com.ubtechinc.key` | `key` (Byte) — head key id |
| `StaticValue.ALPHA_SPEECH_DIRECTION` | `com.ubtechinc.services.SPEECH_DIRECTION` | `absoluteAngle` (byte) |
| `StaticValue.ALPHA_TTS_HINT` | `com.ubtechinc.robot.tts_hint_wakeup` | `hint_event` (String) |
| `StaticValue.ALPHA_GESTURE_ACTION` | `come.ubt.alpha2.gesture` | `getstureDirection` |
| `DeveloperAppStaticValue.APP_ROBOT_UUID_INFO` | `com.ubtechinc.robot_uuid.info` | `robot_uuid` (String) |
