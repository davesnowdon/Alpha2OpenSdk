# AGENTS.md

Guidance for AI coding agents working in this repository. This captures the things that
are **not** obvious from the code or a quick skim of the docs. Read
[`docs/README.md`](docs/README.md) for the full developer documentation; this file is the
short list of traps and conventions specific to working *on* the project.

## What this project is

- An **open, original reimplementation** of the UBTECH Alpha2 robot Android SDK. It talks
  to the robot's on-device system services and removes the client-side store-authorization
  gate that the (now-defunct) official one required.
- **It is not decompiled code.** Author changes from the documented interface, not by
  pasting UBTECH source. The reverse-engineering / decompiled reference and firmware
  analysis are kept in a **separate private archive**, not in this repo. Do not add
  decompiled source, proprietary grammar/model data, or copied prose here.

## #1 rule: do not "clean up" wire values — you will silently break the robot

The SDK is a client for the robot's services over **AIDL/Binder, Android broadcasts, and a
serial protocol**. Many identifiers look like typos but are the exact strings/bytes the
robot compares against. Changing a **value** produces **no compile error and no test
failure** — it only breaks once the app is running on hardware. The full catalogue is in
[`docs/gotchas-and-naming.md`](docs/gotchas-and-naming.md). Frozen (do not change the value):

- **AIDL**: the package `com.ubtechinc.alpha2serverlib.aidlinterface`, every interface and
  method name, **and the method declaration order** (order = Binder transaction id).
- **Service action strings** (e.g. `com.ubtechinc.services.SpeechServices`) bound with an
  explicit `setPackage("com.ubtechinc.alpha2services")`.
- **Broadcast actions and intent-extra names** — including the real typos
  `come.ubt.alpha2.gesture`, `getstureDirection`, `com.ubtechinc.udate.header`,
  `HEADER_FALL_DERECTION`.
- **Serial command bytes** and the `250` "hold this joint" sentinel.
- **`Serializable` DTO class names + field names + `serialVersionUID`**
  (`DeveloperAppButtenEventData`, `DeveloperAppData`, `DeveloperAppConfigData`) — they
  travel inside broadcast `Bundle`s, so the FQCN is part of the wire format.
- Language codes `en_us` / `zh_cn`, action-type codes `"1"/"2"/"3"`, and the built-in
  speech grammar intent names.

You **may** correct Java-facing **symbol names** (method/type/package identifiers); you may
**not** change the string/byte **values** above.

## AIDL

`.aidl` files live in `ubtechalpha2robot/src/main/aidl/...` and AGP generates the Binder
stubs (`buildFeatures { aidl true }`). **Never hand-write the generated `.java` stubs, and
never reorder AIDL methods** — the transaction ids must line up with the robot's copy.

## Building (assume no local Android SDK)

Build in the project's CI Docker image, not with a local toolchain:

```bash
# SDK (from repo root); image tag is pinned in .github/workflows/sdk.yml (currently sha-94523ae)
docker run --rm -v "$PWD":/work -w /work davesnowdon/alpha2build:sha-94523ae \
  bash -lc './gradlew :ubtechalpha2robot:assembleRelease'

# Example: stage the freshly built .aar, then build it
cp ubtechalpha2robot/build/outputs/aar/ubtechalpha2robot-release.aar examples/HelloAlpha/app/libs/
docker run --rm -v "$PWD":/work -w /work/examples/HelloAlpha davesnowdon/alpha2build:sha-94523ae \
  bash -lc './gradlew assembleDebug'
```

Pins that must not drift: AGP **4.2.2**, Gradle **7.0**, `compileSdk 25`, SDK `minSdk 21` /
example `minSdk 22` / `target 22`, **Java 8**, **no AndroidX**. The SDK depends only on the
Android framework — do not add third-party dependencies. The example commits a standard
(non-secret) debug keystore on purpose (AGP's auto-keystore path crashes on CI runners).

## Testing on the real robot (needs a human)

- The robot is an ordinary Android device over `adb` (often **not** on `PATH` — typically
  `~/Android/Sdk/platform-tools/`). It has **no launcher** for side-loaded apps, so start
  yours with `adb shell am start -n <pkg>/.MainActivity`. **Uninstall before installing**
  (`adb uninstall <pkg>`) or a debug-key mismatch fails with
  `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.
- `examples/HelloAlpha` **is the on-hardware smoke test**: it exercises TTS, action
  playback, a servo, head-touch, the accelerometer, and voice→gesture, logging under the
  tag `HelloAlpha`. To validate an SDK change on hardware, build + install it and read
  `adb logcat -s HelloAlpha`.
- **You cannot fully self-test.** Head-touch and voice need a person to physically touch
  the head or say "hello alpha" then a phrase. Set up the install, ask the human to
  interact, then read the log.
- **Service binds are asynchronous.** Do not add a busy-wait on the main thread for a bind
  to complete — the `ServiceConnection` callback is delivered on the main thread, so that
  deadlocks the very bind it waits for. Check `isInitCompleted()` and retry instead.

## Speech recognition (common feature-request trap)

The active recogniser is **Nuance VoCon with a fixed built-in grammar**.
`speech_initGrammar` / `speech_startGrammar` target the *inactive* iFlytek engine and have
no effect; there is **no way to add custom vocabulary**. React to the robot's built-in
commands via `IAlpha2RobotClientListener.onServerCallBack` (`Local_Result:rule:… action:…
tag:…`). Do not build features that assume custom voice grammar. See
[`docs/speech-recognition.md`](docs/speech-recognition.md).

## Repo & CI conventions

- Default branch is **`main`**. Work on a branch and open a **PR** (do not push to `main`).
  Use conventional-commit messages; keep PRs focused.
- **Keep `README.md` and `docs/` consistent** — they are treated as the spec, and drift is
  a bug (e.g. an API rename must be reflected in the docs).
- Two workflows:
  - `sdk.yml` — builds the SDK + example on push/PR to `main`; on a **published release**
    attaches `ubtechalpha2robot-release.aar` and `hello-alpha.apk` to the release.
  - `docker-image.yml` — rebuilds the CI toolchain image **only** when `docker/**` changes
    on `main` (PRs build-only; pushing needs Docker Hub secrets). See
    [`docker/README.md`](docker/README.md).
- Cutting a release: `gh release create v<X.Y.Z> --target main …`; CI builds and attaches
  the artifacts. See [`docs/releasing.md`](docs/releasing.md).

## Where to look

`docs/README.md` indexes everything. Highest-signal for agents:
[`gotchas-and-naming.md`](docs/gotchas-and-naming.md) (the frozen-value list),
[`speech-recognition.md`](docs/speech-recognition.md),
[`sensors-and-events.md`](docs/sensors-and-events.md),
[`api-reference.md`](docs/api-reference.md),
[`getting-started.md`](docs/getting-started.md) (toolchain + deploy),
[`releasing.md`](docs/releasing.md), and [`sources.md`](docs/sources.md) (provenance).
