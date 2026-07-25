# Effectors — servos, LEDs, speech, actions

## Servos

The Alpha2 has **20 servos** on a digital serial bus, each with an assigned ID.
They come in two torque classes (a 20 kg·cm type and an 8 kg·cm type). The chest
daughter-board drives them; the Android side sends angle commands through the SDK.

### Two ways to command servos

```java
// one servo
robot.chest_SendOneFreeAngle((byte) id, angle, (short) timeMs);   // id 0–19

// all servos at once
int[] data = new int[20];      // index i -> servo, value -> angle
data[i] = 250;                 // 250 = "hold this joint, do not move"
robot.chest_SendFreeAngle(data, (short) timeMs);
```

- `time` is the move duration in ms (larger = slower/smoother).
- In the whole-array form, **`250` is the no-move sentinel** for a joint; a value
  in roughly `1–249` is an active angle.
- `head_SendFreeAngle` / `head_SendOneFreeAngle` are deprecated aliases.

### Servo map

Servo ids are **1–20** (the single-servo API takes the id directly). The mapping
below is established from a working community app's real per-joint usage:

| id(s) | Joint |
|------:|-------|
| 19 | head yaw (left/right) |
| 20 | head pitch (up/down) |
| 7, 11, 12, 16 | leg / body (leaned together to shift weight and stand) |
| 3, 6 | arms |
| (others) | fill out the remaining arm/leg DOF across the 20 total |

- **Servo 19 (head yaw)** is what the sound-localisation flow drives; the official
  free-angle demo sweeps it roughly **75°–165°**, centred near **120°**.
- **Servo 20 (head pitch)** can be damaged by driving past the *recommended* limit
  toward its *mechanical* limit (e.g. holding a full "look down"), so respect
  recommended limits rather than absolute travel.

### On the wire

- One-servo (`chest_SendOneFreeAngle`) → serial cmd 5, payload
  `[id][angle hi,lo][time hi,lo]` (16-bit big-endian; time ms, floored to 20).
- All-servo (`chest_SendFreeAngle`) → serial cmd 52, 20 angle bytes + time.
- The service cuts servo torque (serial cmd 25, power off) after an action
  finishes; there is no public API to hold a joint limp on demand.

### What the SDK does not give you

There is **no servo feedback** in the SDK — no angle read-back, no current/load,
so no direct bump detection from the servos. Commanding a new target before the
previous move completes can confuse a servo; let moves finish. Fresh replacement
servos ship with a default ID and must be re-assigned to the correct joint ID
(the bus is Dynamixel-like; UBTECH assigns IDs at the factory).

> Precise per-joint angle ranges were published by UBTECH in a spreadsheet
> ("Alpha2 joint angle ranges") that is not part of the surviving SDK materials.
> Until it resurfaces, treat servo 19's ~75–165° as the one confirmed range and
> probe others conservatively.

## LEDs

Addressable lights exist on the ears, eyes, mouth, head, hands and chest, but the
**SDK only exposes ear and eye control**:

```java
robot.header_startEarLED(upMs, downMs, runMs);
robot.header_startEyeLED(colorType, upMs, downMs, runMs);
robot.header_stopEarLED();
robot.header_stopEyeLED();
```

- Eye colour uses a colour code (1=R, 2=G, 3=B, 4=RG, 5=RB, 6=GB, 7=RGB). Each
  eye is a ring of 8 individually-addressable LEDs; a deprecated richer ear-LED
  overload also exposes per-ear 8-LED masks (255 = all) and a "breathe" mode.
- `LED_EAR=1`, `LED_EYE=2`, `LED_MOUTH=3` are the target constants, but there is
  **no mouth-LED method** in the API (no command path for the mouth exists).
- On the wire, ear = head serial cmd 1, eye = cmd 2, stop = cmd 8. Physically the
  eye ring is driven from the 5-mic array board (`com.ubtechinc.mic5.LedControl`).
- There is also a broadcast surface, `ALPHA_LED_ACTION`
  (`com.ubtechinc.services.LED_ACTION`), and some named actions carry LED frames.
- The chest LED is the status indicator (charging red, charged green, low-power
  blinking red, standby slow-blue) and is driven by the system, not this API.

## Speech / TTS

`speech_startTTS(language, text, voice)` — see [api-reference.md](api-reference.md).
English uses `LanguageType.LAU_ENGLISH` (`"en_us"`) and ignores the voice-name
argument (voice is Chinese-only). Recognised speech, when you use ASR, is decided
by the on-robot NLU first and delivered to your app as text.

## Actions

Pre-recorded motions ("actions"), dances, and stories are installed on the robot
as `.ubx` files and played by name:

```java
robot.action_PlayActionName("ACT0");
robot.action_StopAction();
```

Enumerate them with `action_getActionList` (type `"1"`=action, `"2"`=dance,
`"3"`=story; name at index 2). Actions synchronise servo motion with eye/ear LEDs
and audio when they were authored that way in UBTECH's PC action editor.
