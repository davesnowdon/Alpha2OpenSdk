# Sources and provenance

These docs are an original write-up of *facts* — API signatures, constant values,
event mechanisms, and hardware behaviour — needed to develop for the Alpha2 now
that the official docs, developer portal, cloud services, and community forum are
gone. No third-party source code, forum post text, or proprietary-tool content is
reproduced here.

## Where each kind of fact came from

- **Official UBTECH materials** (user manual, PC "programming software
  instructions", the SDK release notes, and the SDK's own spec document): the
  hardware overview, sensor names, default head-touch behaviours, servo counts and
  classes, LED locations, the account/binding/region-lock model, and the intended
  programming model.
- **Official SDK javadoc + `ubtechalpha2robot.jar`**: the full `Alpha2RobotApi`
  method surface, the `UbxErrorCode` enum, and the complete constant set
  (`StaticValue`, `AlphaConstant`, `HardwareTestValue`, `DeveloperAppStaticValue`,
  `EventCmdId`, `LanguageType`, `ActionType`, `CustomLanguage`, …) with values.
- **Official `alpha2demo`**: the canonical event/broadcast handling pattern
  (`ALPHA_SPEECH_DIRECTION` + `absoluteAngle`, `ALPHA_TTS_HINT` + `hint_event`),
  the init sequence, and servo/free-angle usage (servo 19, the `250` sentinel).
- **On-robot `Alpha2Services` (decompiled for analysis)**: the authoritative
  head-key handler (key ids 4/5/6/7/10 and their built-in behaviours, all
  re-broadcast on `com.ubtechinc.key`), and the two-part authorization gate
  (`isAuthorize` + active-app/launcher check → `API_ERROR_APPID_NOT_ACTIVE`).
  Analysed to understand behaviour; no code from it is copied into this repo.
- **Community forum archive** (`alpha2.3duk.co.uk`, offline; mined from a mirror):
  corroboration of the `API_ERROR_APPID_NOT_ACTIVE` restriction as experienced by
  developers, the servo bus being digital/ID-assigned with head = servo 20, the
  serial-frame example, and the sonar/accelerometer behaviour.
- **Hardware testing** on a real Alpha2: the head-key broadcast being a `Byte`
  extra; front/back crown pads = key ids 5 and 4; both serial links binding; and
  end-to-end confirmation that TTS/action/servo run with the gate removed.
- **A robot firmware backup** (`getprop`, package list, partitions): the platform
  facts (RK3288, Android 5.1.1/API 22, no GPS, installed apps, partition layout).
- **Existing community Alpha2 software** (examined for facts only, no code copied):
  confirmation that head buttons also arrive as Android `KeyEvent` scan codes
  (60/61/66), the practical servo-id map (head 19/20, legs 7/11/12/16, arms 3/6),
  that IMU/battery/camera are accessed through standard Android APIs
  (`SensorManager`, `BatteryManager`, `Camera`/OpenCV), that battery % is
  obtainable, that the sonar and IMU can be reached over the raw serial node
  beneath the SDK, the serial topology and audio-DSP/mic-array details, the
  head-touch gesture mappings, and the Lynx-vs-Alpha2 IR/sonar difference.
- **Firmware backup** (`system.img` / `userdata.img`, read-only via `debugfs`):
  the speech stack — the active recogniser is **Nuance VoCon** (iFlytek present but
  inactive), and the full command grammar (all `AP_*`/`QA_*`/system intents) was
  extracted with `strings` from `assets/vocon/ubt_grammar.fcf` in the
  `com.ubtechinc.alpha2services` APK. Also that the build is `userdebug`/`test-keys`
  (public AOSP platform key). Only factual intent names/formats reproduced — no
  proprietary grammar source, model data, or code. See
  [speech-recognition.md](speech-recognition.md).

## Known open items

- **Sonar enable sequence.** The sonar is off by default; the arm frame
  (`chest_configureSonar`, chest command 4 `{2, distance}`) sends but a single
  app-side configure did not start obstacle streaming in testing. See
  [capabilities.md](capabilities.md).
- **Per-joint servo angle ranges.** UBTECH's joint-range spreadsheet is not in the
  surviving materials; only servo 19's ~75–165° is confirmed.

Resolved earlier: the head-input map — hardware testing showed the head exposes
**exactly two** software-readable touch pads (front/back); the side "strips", back
buttons, and chest button are not readable inputs
([sensors-and-events.md](sensors-and-events.md)).

Resolved this cycle: **speech recognition** — engine (Nuance VoCon), wake word
("hello alpha"), how results reach an app (`onServerCallBack`), the contention
model, and the full built-in grammar are documented in
[speech-recognition.md](speech-recognition.md). Custom vocabulary can't be added
without the proprietary Nuance VoCon grammar compiler.

## Leads for further work

- **Further `Alpha2Services` analysis** can map the rest of the serial protocol
  (the sonar arm sequence, sonar distance framing, servo angle-info replies) if
  needed.
- **A hardware session** can settle the sonar-enable sequence (the head-input map
  is already resolved — see above).

## Licensing / IP note

This repository's SDK is an **original reimplementation** of the Alpha2 SDK
interface, written from the documented facts above. Only interface-level facts
that must match for interoperability (AIDL method names/order, action strings,
serial command bytes, constant values) are reproduced; everything else is freshly
authored. The decompiled-SDK and firmware analysis that informed it is kept in a
separate private archive, not in this repository. These docs deliberately contain
only factual/interface information. Any third-party software examined during
research was used for factual understanding only — none of its code or content is
reproduced or adapted here.
