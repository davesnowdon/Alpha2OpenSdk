# HelloAlpha — Alpha2OpenSdk smoke test

A minimal Android app that exercises the subsystems the SDK drives on the robot
over AIDL: **speech (TTS)**, **action playback**, **direct servo control**, and —
newest — **reacting to the robot's speech recogniser**. Use it to confirm that a
build of the SDK works on real hardware and that the store-authorization
restriction really is removed.

The whole app is [`MainActivity`](app/src/main/java/com/davesnowdon/helloalpha/MainActivity.java).
The Alpha2 has no user-visible touchscreen, so it is driven by the robot's own
sensors and voice rather than on-screen buttons, logging each SDK call's
`API_ERROR_CODE` on screen and to logcat (tag `HelloAlpha`):

- **Head touch** — a receiver for the head-key broadcast (`com.ubtechinc.key`):
  front pad → TTS, back pad → action / head-servo (alternating).
- **Voice reactions** — after the wake word **"hello alpha"**, the robot's
  offline (Nuance) recogniser delivers results to `onServerCallBack`; the app
  parses them and plays a gesture for the intents it maps (see below).
- **IMU** sampling, the **pre-programmed pose list**, and raw serial frames / key
  events are also logged.

### Voice reactions — what to expect

The robot's recogniser has a **fixed built-in grammar** you cannot extend, and it
**acts on its own recognitions**. So the app only maps **`QA_*` intents** (where
the robot just answers verbally and stays still, so our gesture is the only
motion). Say "hello alpha", then:

- **"how tall are you"** → robot answers **and waves** (`QA_KNOWING`)
- **"how old are you"** → robot answers **and raises both hands** (`QA_Age`)

`Action_Performance` intents (e.g. "sit down" → `AP_SQUAT`, "stand up" →
`AP_STAND_UP`, "can you dance" → `AP_DANCE`) are **logged but not mapped** — the
robot performs those moves itself, which would preempt a mapped action. Every
recognised phrase is logged as `recognized: rule=… action=… text="…"` so you can
extend the mapping. The full grammar and all intents are documented in
[docs/speech-recognition.md](../../docs/speech-recognition.md).

See the developer docs in [`../../docs/`](../../docs/) — especially
[example-helloalpha.md](../../docs/example-helloalpha.md),
[speech-recognition.md](../../docs/speech-recognition.md), and
[sensors-and-events.md](../../docs/sensors-and-events.md) — for the full picture.

## Prerequisites

- A built SDK `.aar` (see the top-level README for building the SDK).
- The robot reachable over `adb` (see the top-level README's Vysor/udev section).

## Build

The SDK is consumed as a local `.aar`, so copy the freshly built artifact in
first. The `.aar` is **git-ignored on purpose** — never commit it, or it will
drift from the SDK source.

```bash
# from the repo root, after building the SDK
cp ubtechalpha2robot/build/outputs/aar/ubtechalpha2robot-release.aar \
   examples/HelloAlpha/app/libs/

cd examples/HelloAlpha
./gradlew assembleDebug
```

This project intentionally mirrors the SDK's toolchain (Android Gradle Plugin
4.2.2 / Gradle 7.0 / `compileSdk 25`), so it builds in the same environment as
the SDK — including the CI Docker image `davesnowdon/alpha2build`:

```bash
# from the repo root
docker run --rm -v "$PWD":/work -w /work/examples/HelloAlpha \
    davesnowdon/alpha2build:sha-94523ae bash -lc './gradlew assembleDebug'
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Install and run

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.davesnowdon.helloalpha/.MainActivity
adb logcat -s HelloAlpha
```

The app has no on-screen buttons — the robot has no user-visible screen. On start
it greets, samples the IMU, and logs the pre-programmed pose list. Then trigger
the tests by touching the head or speaking:

1. **Front pad** — the robot says "Hello, I am Alpha 2" (TTS).
2. **Back pad** — plays an action / turns the head (neck servo 19); alternates
   between the two on successive touches.
3. **Say "hello alpha"**, then **"how tall are you"** (robot answers + waves) or
   **"how old are you"** (robot answers + raises both hands). Any recognised
   phrase is logged as `recognized: rule=… action=… text="…"`.

You can also drive any pose directly, with no touch:

```bash
adb shell am broadcast -a com.ubtechinc.services.thirdparty.playaction \
  --es TP_alphacmdName "Shake head"
```

## Reading the result

- **Call returns `API_ERROR_SUCCEED` and the robot responds** — the SDK works
  and the client-side de-restriction holds.
- **Returns `API_ERROR_NOT_INIT`** — the corresponding `init...Api` call has not
  completed; wait for "Speech service ready." / "initChestSerialApi -> true".
- **Succeeds but nothing physically happens, or fails the same way no matter
  the app id** — points at robot-side behaviour, not this SDK.
- **`NoClassDefFoundError` in logcat** — a runtime dependency of the SDK is
  missing from [`app/build.gradle`](app/build.gradle); add the matching library
  there. (The declared set covers the speech/action/servo paths.)

## Notes

- `targetSdkVersion` is 22 to match the robot (Android 5.1.1), which uses the
  install-time permission model — no runtime permission prompts are needed.
- The `alpha2_appid` in the manifest and the app id passed to `Alpha2RobotApi`
  are no longer validated by this SDK; any non-empty string works.
