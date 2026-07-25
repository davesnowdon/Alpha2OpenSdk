# The store-authorization restriction (and what the fork removes)

This is the reason the fork exists. The official SDK will not let a third-party
app speak or perform actions unless the app is both *authorized* and *active*
against UBTECH's infrastructure — infrastructure that no longer exists.

## How the original gate worked

Every gated call in the official `Alpha2RobotApi` (TTS, actions, servo commands,
LEDs, …) applied a **two-part check** before doing anything, and returned an error
enum otherwise:

1. **Authorization flag.** An internal `isAuthorize` flag had to be set. It was
   set by an authorization handshake: the SDK sent the app's key to UBTECH's
   server and, on success, flipped the flag. If the flag was false, the call
   returned `API_ERROR_AUTHORIZE_ERROR`.
2. **Active-app / launcher check.** Even when authorized, the call additionally
   required that the app was either a **debug build** *or* the **currently active
   launcher app** — i.e. an app launched through the official UBTECH client, not
   an arbitrarily started APK. If neither held, the call returned
   `API_ERROR_APPID_NOT_ACTIVE`.

In effect: *"return success only if authorized AND (debug OR launched via the
official client), otherwise refuse."* This is why community developers with
correctly registered app IDs still saw `speech_StartTTS()` fail with
`API_ERROR_APPID_NOT_ACTIVE`, and why UBTECH's advice was "it won't happen if the
app is launched from the mobile client." The behaviour is enforced in **both** the
SDK and the on-robot service.

## Why that is fatal now

The authorization step depended on UBTECH's developer platform and a live cloud
handshake (against region-locked servers). The developer portal, the app store,
and those servers are gone. With no way to authorize or to be "the active app"
in the official sense, an unmodified app is permanently stuck returning
`API_ERROR_AUTHORIZE_ERROR` / `API_ERROR_APPID_NOT_ACTIVE` — it can never speak or
move. The robot is otherwise fully functional; only this gate blocks it.

## What the fork changes

The fork neutralises the client side of the gate at the SDK source level:

- The internal authorization flag defaults to **already authorized**; the SDK's
  authorization routine no longer contacts any server and reports success
  (`code = 1, info = "have offline authority"`) to the `ClientAuthorizeListener`.
- The active-app / launcher check is removed from the gated calls, so they no
  longer return `API_ERROR_APPID_NOT_ACTIVE`.
- The dead server-client and authentication-request plumbing behind the old
  handshake is deleted.

The appid you pass to the constructor and put in the manifest is therefore no
longer validated — but a non-empty value is still expected structurally.

## Proven on hardware

With the fork in place, on a real Alpha2, using an appid the defunct store cannot
validate: TTS, action playback, and a direct servo command all returned
`API_ERROR_SUCCEED` and physically executed. Critically, there was **no
server-side re-check** blocking the calls — removing the client-side gate is
sufficient. The robot's own `ClientAuthorizeListener` callback still fires, but it
no longer gates anything.

## Relationship to other approaches

Another route to unlocking the robot is **patching its firmware/services** on the
device itself (a binary/installer approach). The fork takes the complementary,
open, source-level route: fix the SDK so a normal app built against it works, with
no device patching and no surviving UBTECH infrastructure. The two are
independent; this documentation does not reproduce or depend on any such tool.
