# Alpha2OpenSdk developer documentation

A clean, single source of developer information for the UBTECH Alpha2 robot and
this open SDK fork.

UBTECH's own developer documentation is no longer distributed, the developer
portal and cloud services are gone, and the main community forum
(`alpha2.3duk.co.uk`) is offline. These docs reconstruct the practical
information a developer needs, from the surviving official materials, the
official demo, the SDK jar, the forum archive, and hands-on testing on a real
robot. Where a fact came from reverse engineering or hardware observation rather
than official docs, that is called out.

> Scope note: this describes facts — API signatures, constant values, event
> mechanisms, hardware behaviour. It contains no copied UBTECH source, no forum
> post text, and nothing from any third-party (e.g. proprietary) tool.

## Contents

| Doc | What's in it |
|-----|--------------|
| [overview.md](overview.md) | What this fork is, what it removes, and why |
| [getting-started.md](getting-started.md) | Toolchain, building the SDK, building and deploying an app to the robot |
| [api-reference.md](api-reference.md) | The `Alpha2RobotApi` surface: init, speech, actions, servos, LEDs, error codes |
| [capabilities.md](capabilities.md) | **Everything the robot can do and how to reach it** — motors, cameras, ultrasound, IMU, battery, charging, LEDs, speech, and more, across all four access surfaces |
| [sensors-and-events.md](sensors-and-events.md) | How sensor input reaches an app; head-touch keys (both paths); sound, fall, sonar; the broadcast reference |
| [speech-recognition.md](speech-recognition.md) | Voice commands: the Nuance engine, wake word, how results reach your app, and the **full built-in grammar (all intents)** |
| [effectors.md](effectors.md) | Servos (map, IDs, angle model), LEDs, TTS, action playback |
| [hardware.md](hardware.md) | Boards, sensor suite, servo layout, serial-frame protocol |
| [authorization.md](authorization.md) | How the original store-authorization gate worked and what the fork removes |
| [example-helloalpha.md](example-helloalpha.md) | Walkthrough of the sensor-driven smoke-test app |
| [releasing.md](releasing.md) | How to cut a release and publish the SDK + example artifacts with `gh` |
| [gotchas-and-naming.md](gotchas-and-naming.md) | Corrected Java names vs frozen wire values, region lock |
| [sources.md](sources.md) | Provenance of every claim, and leads for further work |

## Quick facts

- **Robot OS:** Android (5.1.1 on later units; some material references 4.4.2), SoC Rockchip RK3288.
- **DOF:** 20 servos, digital serial bus, IDs assigned per servo.
- **SDK entry point:** a single class, `com.ubtechinc.alpha2robot.Alpha2RobotApi`.
- **What this SDK fixes:** removes the client-side store-authorization gate so an app can speak and act without UBTECH's (now-defunct) account/appid activation; reimplements only the documented surface, vendoring no third-party libraries; and corrects the API names. See [authorization.md](authorization.md).
