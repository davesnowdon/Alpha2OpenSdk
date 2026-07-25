# Gotchas and safe renaming

## Java names are corrected; wire values are frozen

UBTECH's original SDK was full of spelling errors. This SDK **corrects the
Java-facing names** (methods, types and packages visible to your app), but
preserves every string and numeric value the on-robot `Alpha2Services` (or a
companion app) compares against — renaming a value breaks interoperability even
though the code still compiles.

### Corrected Java names (when porting from UBTECH's SDK)

The corrected names are the only names — there are no deprecated aliases. If you
are porting an app written against UBTECH's original SDK, rename:

- Methods: `initChestSeiralApi` → `initChestSerialApi`, `speeh_startGrammar` →
  `speech_startGrammar`, `speech_stopGrammaer` → `speech_stopGrammar`.
- Types / packages: `LauguageType` → `LanguageType`, `AlphaContant` →
  `AlphaConstant`, package `com.ubtechinc.contant` → `com.ubtechinc.constant`.
  The result enum is `UbxErrorCode.API_ERROR_CODE`.

### NOT safe to rename (values the robot compares against)

Do **not** change the string constants and numeric values that cross the process
boundary to `Alpha2Services` or to companion apps. These must stay byte-for-byte:

- Broadcast action strings: `com.ubtechinc.key`, `com.ubtechinc.services.SPEECH_DIRECTION`,
  `com.ubtechinc.robot.tts_hint_wakeup`, the `come.ubt.alpha2.gesture` actions
  (note the `come.` typo is real and must stay), the `DeveloperAppStaticValue`
  `com.ubtechinc.button.*` / `com.ubtechinc.config.*` actions, etc.
- Intent extra names: `key`, `absoluteAngle`, `hint_event`, `robot_uuid`,
  `getstureDirection` (sic), `value`.
- Serial command bytes and key ids (e.g. `HEADER_SEND_KEY = -127`, head key ids
  4/5/6/7/10, the `250` no-move sentinel).
- Manifest meta-data names: `alpha2_appid`, `alpha2_buttonevent`, `alpha2_appconfig`.
- Language codes `en_us` / `zh_cn`, action type codes `"1"/"2"/"3"`, TTS voice
  names (`xiaoyan`, …).

Rule of thumb: **rename symbols, preserve values.** If a symbol's *value* is a
string or number the robot's services or another app will read, the value is
frozen. A good safeguard is a test that asserts these constant values are
unchanged.

## Other traps

- **Byte vs int extra.** The head-key broadcast's `key` extra is a `Byte`;
  `getIntExtra("key", …)` silently returns the default. Use `getByteExtra`.
- **Serial callbacks are usually silent.** The system service consumes the serial
  port and re-broadcasts; don't rely on `onListenSerialPort*RcvData` for events
  that have a broadcast form.
- **"Buttons" ambiguity.** The demo's button events are *virtual* buttons from the
  phone app, not the physical head buttons.
- **Servo safety.** No servo feedback exists; respect recommended limits
  (especially head pitch, servo 20) and let a move finish before commanding the
  next.
- **Region lock / binding.** Historic UBTECH binding was region-locked and
  QR-based; ROMs are serial-bound. None of this matters once the SDK gate is
  removed, but it explains why cloud-based "fixes" are dead ends.
- **appid.** No longer validated by the fork, but a non-empty constructor arg and
  manifest `alpha2_appid` are still expected structurally.
