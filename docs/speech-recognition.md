# Speech recognition (voice commands)

How the Alpha2 recognises spoken commands, how a third-party app receives the
results, and the full built-in grammar. Everything here was established on real
hardware and by extracting the grammar from the on-robot service; see
[Provenance](#provenance).

## Engines

The robot ships **two** speech-recognition engines inside the platform-signed
system app `com.ubtechinc.alpha2services` (uid `system`):

- **Nuance VoCon** — an **embedded, offline** recogniser. This is the **active**
  engine. Assets live in the app: `assets/vocon/*.dat` (acoustic/CLC models),
  `assets/vocon/ubt_grammar.fcf` (the compiled UBTECH command grammar),
  `assets/asr/common*.jet`, `assets/nuance_data.zip`. TTS is Nuance vexpressive
  (voices *Samantha*, *Ava*).
- **iFlytek (讯飞)** — present but **not** the active command engine on this
  firmware (`assets/iflytek/`, `assets/*.bnf` / `*.abnf` sample grammars). The
  SDK's `speech_initGrammar` targets this path, which is why a custom grammar
  **compiles successfully but never affects recognition** — the live engine is
  Nuance and uses its own grammar.

A Nuance **cloud** recogniser (`ubtech-mix-engusa-ssl.nuancemobility.net`) is also
wired up but is **dead** (servers gone), so only the offline path works. That is
fine — the offline grammar is rich (see below).

## How recognition reaches your app

1. **Wake word.** Recognition is gated behind the wake word **"hello alpha"**.
   Waking also makes the robot stand and orient toward the speaker (its own
   built-in behaviour).
2. **Wakeup event.** Wakeup is broadcast as `com.ubtechinc.robot.tts_hint_wakeup`
   (`StaticValue.ALPHA_TTS_HINT`), string extra `hint_event` =
   `enter_wakeup` then `wakeup`.
3. **Recognition result.** The recognised utterance is delivered to your app
   through the **ordinary speech callback** —
   `IAlpha2RobotClientListener.onServerCallBack(String)` — **not** a broadcast.
   The string looks like:

   ```
   Local_Result:rule:<RULE>  action:<INTENT>  tag:<recognised text>
   ```

   e.g. `Local_Result:rule:QA action:QA_KNOWING tag:How tall are you` or
   `Local_Result:rule:Action_Performance action:AP_SQUAT tag:sit down`.
   Parse `action:` for the intent and `tag:` for the recognised phrase.

There is **no** public SDK method to register for recognised commands, and the
third-party speech broadcasts (`ALPHA_THIRD_PARTY_SPEECH_*`, `speechcmd`) did
**not** fire for an app launched over `adb` — the robot only routes recognition to
the callback of the app whose speech client is bound (this app), and drives its
own behaviour internally. Receiving results does **not** require a root/
platform-signed app; it uses the normal speech callback above.

## The robot acts on its own recognitions (contention)

The robot runs its **own** behaviour for what it recognises, and that competes
with anything your app plays:

- **`QA_*` intents** — the robot answers **verbally** and does **not** move. A
  gesture you play in response is the only motion, so it lands reliably. This is
  the clean place to react.
- **`Action_Performance` (`AP_*`) intents** — the robot **performs the move
  itself**. A mapped `action_PlayActionName` returns `API_ERROR_SUCCEED` but is
  often **preempted** by the robot's own action (observed: saying "stand up"
  played the robot's own "Squat and stand up" over our mapped action).

The [HelloAlpha example](example-helloalpha.md) therefore maps only `QA_*` intents
to gestures and leaves `AP_*` moves to the robot.

## Full built-in grammar (intents)

Extracted from `assets/vocon/ubt_grammar.fcf`. Intent tags are `@grammarall#<name>`.
The exact spoken trigger phrases are compiled into the `.fcf`; recognised words
seen include *wave, sit, stand, squat, dance, bow, nod, shake, punch, kung(fu),
forward, back(wards)*. Bare single words may mis-parse (e.g. "wave" alone matched
a QA rule) — the fuller phrase is more reliable ("wave your left hand").

### `Action_Performance` — `AP_*` (48 motion / emotion commands)

| Group | Intents |
|-------|---------|
| Locomotion | `AP_MOVE_FORWARD`, `AP_MOVE_BACKWARD`, `AP_MOVE_LEFTWARD`, `AP_MOVE_RIGHTWARD`, `AP_GO_BACKWARD`, `AP_FACE_FORWARD`, `AP_TURN_LEFT`, `AP_TURN_RIGHT`, `AP_TURN_LEFT_AND_WALK`, `AP_TURN_RIGHT_AND_WALK` |
| Arms / hands | `AP_WAVE_THE_LEFT_HAND`, `AP_WAVE_THE_RIGHT_HAND`, `AP_RAISE_THE_LEFT_HAND`, `AP_RAISE_THE_RIGHT_HAND`, `AP_RAISE_BOTH_HANDS`, `AP_LIFT_THE_LEFT_HAND`, `AP_LIFT_THE_RIGHT_HAND`, `AP_LIFT_BOTH_HANDS`, `AP_LEFT_PUNCH`, `AP_RIGHT_PUNCH`, `AP_SHAKE_HANDS`, `AP_APPLAUD` |
| Legs / posture | `AP_LEFT_LEG_KICK`, `AP_RIGHT_LEG_KICK`, `AP_LEFT_LEG_LIFT`, `AP_RIGHT_LEG_LIFT`, `AP_SQUAT` (sit down / crouch), `AP_STAND_UP` |
| Head | `AP_NOD`, `AP_SHAKE_HEAD`, `AP_RAISE_HEAD`, `AP_RAISE_HEAD_LEFTWARD`, `AP_RAISE_HEAD_RIGHTWARD`, `AP_LOWER_HEAD`, `AP_TURN_HEAD_LEFTWARD`, `AP_TURN_HEAD_RIGHTWARD` |
| Expressive / emotion | `AP_DANCE`, `AP_BOW`, `AP_GREETING`, `AP_PLAY_KUNGFU`, `AP_BLINK`, `AP_ACT_CUTE`, `AP_HAPPY`, `AP_SAD`, `AP_LAUGH`, `AP_BORING`, `AP_EMOTION_YES`, `AP_EMOTION_NO` |

### `QA` — `QA_*` (13 question / chat intents)

`QA_Age`, `QA_Name`, `QA_Birthday`, `QA_Family`, `QA_From`, `QA_Where`,
`QA_Ability`, `QA_KNOWING`, `QA_ADVISE`, `QA_CHATTING`, `QA_COOK`, `QA_EATING`,
`QA_INTERVIEW`.

### System / device intents

| Rule | Intents |
|------|---------|
| `Take_Photo` | `TP_Start_Cam`, `TP_Take_Pic` |
| `Self_Check` | `SC_Self_Check_Application`, `_Bound`, `_Connect_Network`, `_Internet`, `_Lastest_Pictures`, `_Login`, `_Power`, `_Power_Off`, `_Sleeping_State`, `_Storage`, `_Volume_Down`, `_Volume_Up`, `_Wifi` |
| `Connect_Wifi` | `CW_Connect_Wifi` |
| `Find_My_Phone` | `FMP_Find_My_Phone` |
| `Story_Search` | `SS_Story_Search` |
| `App_Manager` | `AM_App_Manager` |
| `Get_Log` | `GL_Get_Log` |
| `Wakeup` | `wakeup_word` |

## Can the grammar be changed?

- **Reading it: yes** — the intent set above was extracted directly from
  `ubt_grammar.fcf`, and the vocabulary is already broad (wave, squat/sit,
  stand up, dance, bow, punch, kung fu, nod, shake, turn, walk, …). For most
  purposes you do not need to change it — pick a phrase it already knows and map
  the intent.
- **Adding words: not practically.** `ubt_grammar.fcf` is a **compiled** Nuance
  VoCon grammar. Regenerating it from a modified source grammar requires the
  proprietary **Nuance VoCon grammar compiler**, which is not publicly available.
  Editing the compiled binary is not viable.
- **Deploying a modified system app is otherwise feasible on this hardware.** The
  firmware is a **`userdebug`, `test-keys`** build (`ro.build.display.id =
  rk3288-userdebug 5.1.1 … test-keys`). That means the platform is signed with the
  **public AOSP test keys**, so an app can be re-signed to join
  `sharedUserId=android.uid.system`, and `adb root` is generally available. The
  blocker for custom voice commands is therefore the Nuance grammar compiler, not
  device signing/locking.

## Provenance

The engine identity, wake word, callback format, and contention behaviour were
observed on a real Alpha2 over `adb logcat`. The full intent list was extracted
with `strings` from `assets/vocon/ubt_grammar.fcf` inside the
`com.ubtechinc.alpha2services` APK, recovered read-only from a firmware backup's
`userdata.img` via `debugfs`. Only **factual** interface information (intent tag
names, formats, behaviours) is reproduced — no proprietary code, model data, or
grammar source. See [sources.md](sources.md).
