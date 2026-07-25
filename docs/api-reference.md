# API reference — `Alpha2RobotApi`

The entire third-party API is one class, `com.ubtechinc.alpha2robot.Alpha2RobotApi`.
Method names below are this SDK's — the spelling errors in UBTECH's original SDK
are corrected here. See [gotchas-and-naming.md](gotchas-and-naming.md) for the
mapping from the original names and for the wire values that stay frozen.

## Construction and initialisation

```java
Alpha2RobotApi robot = new Alpha2RobotApi(context, appKey, clientAuthorizeListener);
```

- `appKey` — on the official SDK this had to be a key issued by UBTECH's developer
  portal. This fork does not validate it; pass any non-empty string.
- `ClientAuthorizeListener.onResult(int code, String info)` — the authorization
  callback. **`code == 1` means authorized.** On this fork it always reports
  `code = 1, info = "have offline authority"`. Initialise sub-systems from inside
  this callback (or after it) — the official demo only calls the `init*` methods
  once `code == 1`.

Sub-system init (call the ones you need):

| Method | Enables |
|--------|---------|
| `initSpeechApi(IAlpha2RobotClientListener, ISpeechInitInterface[, CustomLanguage])` | TTS + ASR. `ISpeechInitInterface.initOver()` fires when speech is ready. |
| `initActionApi(AlphaActionClientListener)` | Action playback; `onActionStop(name)` callback. |
| `initChestSerialApi()` | Chest serial link (servos + chest sensors). *In the official SDK this method is misspelled `initChestSeiralApi()`.* |
| `initHeaderSerialApi()` | Head serial link (head LEDs, keys, head sensors). |
| `initCustomMessageApi(IAlpha2CustomMessageListener)` | Binary message channel to a companion phone app. |

Always call `releaseApi()` in `onDestroy()`.

`static String getSdkVersion()` / `static String getServerVersion()` are available
before construction.

## Speech / TTS

```java
robot.speech_startTTS(LanguageType.LAU_ENGLISH, "Hello, I am Alpha 2", null);
```

- Languages: `LanguageType.LAU_ENGLISH` = `"en_us"`, `LanguageType.LAU_CHINESE` = `"zh_cn"`.
- Overloads: `speech_StartTTS(text)`, `speech_startTTS(text, voice)`, `speech_startTTS(language, text, voice)`, `speech_StopTTS()`.
- Chinese voice names: `catherine`, `john`, `xiaofeng`, `xiaoyan` (voice is only
  settable for Chinese TTS). `speech_setVoiceName(String)`.
- `speech_SetMIC(true)` makes the robot **release** the microphone to your app;
  `false` reclaims it for the robot. Call `false` on exit.
- Offline grammar ASR: `speech_initGrammar(grammar, listener)`,
  `speech_startGrammar(listener)`, `speech_stopGrammar()` (corrected from the
  original `speeh_startGrammar` / `speech_stopGrammaer`).
- `speech_setRecognizedLanguage("en_us"|"zh_cn")`, `speech_setSelfInterrupt(boolean)` (Chinese only).

TTS completion is reported through `IAlpha2RobotClientListener.onServerPlayEnd(boolean)`.

## Actions

```java
robot.action_getActionList(list -> { /* ArrayList<ArrayList<String>> */ });
robot.action_PlayActionName("ACT0");
robot.action_StopAction();
```

- `action_getActionList` returns, per row: `item.get(1)` = type code
  (`"1"` basic action, `"2"` dance, `"3"` story), `item.get(2)` = action name.
- Action names are installed on the robot; the demo also uses random `ACT0`..`ACT9`.

## Servos (see [effectors.md](effectors.md) for the servo map)

```java
robot.chest_SendOneFreeAngle((byte) 19, 90, (short) 1000);   // one servo
robot.chest_SendFreeAngle(int[20] angles, (short) 1000);      // all servos at once
```

- `chest_SendOneFreeAngle(byte id, int angle, short timeMs)` — single servo. The
  implementation accepts **`id` 1–20** (`id >= 1 && id <= 20`); ids outside that
  range are silently ignored (the call still returns success but sends nothing).
- `chest_SendFreeAngle(int[] data, short time)` — 20 entries; **array index 0–19
  maps to servo 1–20**, value = angle; **value `250` means "hold this joint, do
  not move".**
- `head_SendFreeAngle` / `head_SendOneFreeAngle` are the deprecated older names.
- `isChestAvailable()`, `isHeaderAvailable()` return readiness.
- `chest_configureSonar(int distance)` *(fork addition)* — arms the chest
  ultrasonic sensor by sending the firmware's own configure frame (chest command 4,
  `{2, distance}`; distance 0 disables). See the sonar caveat in
  [capabilities.md](capabilities.md): the call succeeds but a single app-side
  configure did not reliably start obstacle streaming in testing.

## LEDs

```java
robot.header_startEarLED(upMs, downMs, runMs);
robot.header_startEyeLED(colorType, upMs, downMs, runMs);
robot.header_stopEarLED();  robot.header_stopEyeLED();
```

- Only ear and eye LEDs have API methods, despite a `LED_MOUTH` constant existing.
- Times are milliseconds; `run` is the total (on+off)×N duration.
- `header_setNoise(boolean)` toggles head-mic noise reduction.

## Error codes — `UbxErrorCode.API_ERROR_CODE`

| Value | Meaning |
|-------|---------|
| `API_ERROR_SUCCEED` | Success. |
| `API_ERROR_NOT_INIT` | The relevant `init*Api` has not completed. |
| `API_ERROR_APPID_NOT_ACTIVE` | appid not the active/launcher app — the store gate. See [authorization.md](authorization.md). |
| `API_ERROR_AUTHORIZE_ERROR` | appid not authorized. |

On this fork the last two should not occur for gated calls; getting them back
means the de-restriction is not in effect for that build.

## Events in, from the robot

Sensor input does **not** arrive through this class's return values — it arrives
as Android broadcasts and (for raw serial) two override callbacks. See
[sensors-and-events.md](sensors-and-events.md).
