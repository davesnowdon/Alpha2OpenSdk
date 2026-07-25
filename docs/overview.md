# Overview

## What the Alpha2 is

The UBTECH Alpha2 is a ~20-DOF humanoid robot built around a Rockchip RK3288
board running Android. The application processor runs standard Android apps; a
separate **chest daughter-board** drives the 20 servos and reads body sensors,
and a **head board** handles the head touch pad, ear/eye LEDs, microphones and
head-mounted sensors. The Android side talks to those boards over serial links,
brokered by an on-robot system service (`Alpha2Services`).

Third-party apps are ordinary Android APKs installed on the robot. They talk to
the robot's capabilities through one SDK class, `Alpha2RobotApi`, which
communicates with `Alpha2Services` via AIDL/Binder and Android broadcasts.

## What this is

`Alpha2OpenSdk` is an open, original reimplementation of UBTECH's
`ubtechalpha2robot` SDK — written against the robot's documented service interface
rather than shipping UBTECH's code. Its goals:

1. **Remove the store-authorization restriction.** The official SDK refused to
   let an app speak or perform actions unless the app was registered and
   "activated" against UBTECH's cloud/developer platform and launched through
   the official client. Those servers are gone, which would otherwise make every
   third-party app permanently inert. See [authorization.md](authorization.md).
2. **Ship only what's needed, vendoring nothing.** The reimplementation covers the
   documented `Alpha2RobotApi` surface (speech, actions, serial/servos, LEDs,
   custom messaging) and depends only on the Android framework and the Java
   standard library — none of the third-party libraries the original jar bundled.
3. **Correct the API names.** UBTECH's SDK is riddled with spelling errors
   (`initChestSeiralApi`, `speeh_startGrammar`, class `AlphaContant`,
   `LauguageType`, …). This SDK uses corrected Java-facing names — but keeps the
   strings and numeric values the on-robot service compares against exactly as the
   robot expects. See [gotchas-and-naming.md](gotchas-and-naming.md).

## Status

- The de-restriction is **proven on real hardware**: with the gate removed, TTS,
  action playback, and direct servo control all return `API_ERROR_SUCCEED` and
  physically execute on a real Alpha2, using an appid the defunct store can no
  longer validate.
- A sensor-driven smoke-test app lives in `examples/HelloAlpha`
  ([example-helloalpha.md](example-helloalpha.md)).

## Why not just use the official SDK?

Because it does not work any more. The gate is enforced both client-side (in the
SDK) and by the on-robot service, and it depends on cloud activation that no
longer exists. One alternative route is to patch the robot's firmware/services on
the device. This fork instead fixes the problem at the SDK source level, openly,
so anyone can build apps that run on an Alpha2 without any surviving UBTECH
infrastructure.
