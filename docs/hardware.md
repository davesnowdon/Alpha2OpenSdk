# Hardware reference

## Platform (from a robot's own firmware properties)

- SoC **Rockchip RK3288 v2**; `armeabi-v7a` (32-bit).
- **Android 5.1.1 (API 22)**; 320 dpi display; 192 MB Dalvik growth limit.
- Manufacturer/brand UBTECH; model `alpha2`; hardware rev `alpha2_10005`.
- Wi-Fi (`wlan0`) and Bluetooth present; **no GPS** (`ro.factory.hasGPS=false`).
- Standard RK3288 partition layout (uboot/kernel/boot/recovery/system/userdata…)
  plus a `radical_update` partition used by the OTA/update path.
- Shipped robot apps include the system service `com.ubtechinc.alpha2services`,
  plus translation, English-chat, `com.ubtech.smartcamera`, and
  `com.alpha2.videoSupervision` (remote video monitoring).

## Boards

- **Main board:** Rockchip **RK3288**, running Android. Runs the app processor,
  Wi-Fi/Bluetooth, camera, microphones, speaker, and the `Alpha2Services` system
  service.
- **Chest daughter-board:** drives the 20 servos and reads body sensors (sonar,
  fall/accelerometer, chest touch board, power/charge). Talks to Android over a
  serial link.
- **Head board:** handles the head touch pad/keys, ear/eye LEDs, head
  microphone(s) and head-mounted sensors, over its own serial link.

The Android service reads both serial links, decodes frames, and re-broadcasts
events to apps (see [sensors-and-events.md](sensors-and-events.md)).

## Sensor suite

| Sensor | Notes |
|--------|-------|
| Microphone array | Dual digital mics; drives ASR, wake word, sound localisation. |
| Head touch (capacitive) | Crown touch zone; swipe front↔back = volume, tap = stop. Reports key ids on the head serial link. |
| Sonar (ultrasonic) | 2 on the chest; anti-collision. No SDK distance read; usable as an event trigger. |
| Accelerometer | Fall detection / auto-get-up. |
| Hand pressure | Pressure sensor in the hands. |
| Camera | Autofocus 8 MP, forehead. |

`HardwareTestValue` enumerates the categories: `SOUND=1`, `FALL=2`,
`TOUCH_BOARD=3`, `PRESSURE_SENSOR=4`, `SONAR=5`.

## Servo layout

20 DOF, IDs assigned per servo, grouped in UBTECH's tooling as **Head, Hands
(arms), Legs**. Confirmed head servos: **19 = yaw** (left/right), **20 = pitch**
(up/down). Two torque classes (20 kg·cm and 8 kg·cm). See
[effectors.md](effectors.md) for the command model and the `250` "no-move"
sentinel.

## Lights

Addressable lights on ears, eyes, mouth, head, hands, and chest. Eyes are 8-LED
rings. The chest light is the system status indicator. The SDK exposes ear and
eye control only.

## Serial frame format (observed)

The boards speak a byte-framed serial protocol. An observed chest frame:

```
f8 8f 0a 00 00 80 01 38 00 c3 ed
```

- Leading `f8` is a frame header; the trailing bytes (`c3 ed` here) look like a
  checksum/terminator.
- The SDK decodes frames into a small packet abstraction: a **command byte**
  plus a **parameter buffer**. Sensor/status replies use command bytes with the
  high bit set (they appear as negative Java `byte`s). Selected command bytes:

| Command byte | Constant | Meaning |
|-------------:|----------|---------|
| `-127` | `HEADER_SEND_KEY` | head key press; param[0] = key id (see sensors doc) |
| `-126` | `HEADER_SOUND_DIRECTION` | sound-source direction |
| `-125` | `HEADER_FALL_DERECTION` *(sic)* | fall direction |
| `-128` | `HEADER_SEND_OBSTACLE` | head obstacle |
| `-119` | `CHEST_TOUCH_BOARD` | chest touch board |
| `-127` | `CHES_SEND_OBSTACLE` | chest sonar/obstacle (chest link) |
| `-105` | `CHES_SEND_FALLDOWN` | fall detected |
| `-128` | `CHEST_SEND_POWER` | battery/power report |
| `-126` | `CHES_SEND_ANGLEINFO` | servo angle info |

(Head and chest links reuse some byte values; they are disambiguated by which
link the frame arrived on.)

## Serial topology and audio

- The body/chest board and the head board are on **separate UARTs** (referenced as
  a body serial node and head serial node, e.g. `/dev/ttyS1` for the body and
  `/dev/ttySAC*` for the head). The SDK's chest/head serial APIs correspond to
  these two links.
- A dedicated **audio DSP** node (`/dev/zl380tw`, echo cancellation) sits on the
  microphone array; the eye LED ring is driven from that **5-mic array board**.
- Hardware variant note: the **Lynx** revision uses **infrared** proximity sensors
  where the standard Alpha2 uses **sonar**.

## Firmware / recovery notes

- Lowest-level recovery is **maskrom** mode (a key + power combination).
- Each unit's ROM is **signed and bound to its serial number** (the QR code), so
  ROM images are not portable between robots.
- The robot's cloud dependency historically split across **region-locked
  servers** (China vs. rest-of-world) that never synced — a robot bound on one
  could not be re-bound on the other. This is part of why an orphaned unit loses
  functionality, and why a source-level de-restriction (rather than cloud access)
  is the durable fix.
