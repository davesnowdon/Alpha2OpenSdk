# Getting started

## Toolchain

The Alpha2 runs an old Android, so the build stack is deliberately pinned:

- Android Gradle Plugin **4.2.2**, Gradle **7.0**
- `compileSdkVersion 25`, `minSdkVersion 22`, `targetSdkVersion 22`
- Java 1.8 source/target
- No AndroidX / no Jetifier (`android.useAndroidX=false`, `android.enableJetifier=false`)

Targeting API 22 keeps the legacy install-time permission model, so no runtime
permission prompts are needed on the robot.

The SDK and the example build reproducibly in the project's CI Docker image
(`davesnowdon/alpha2build`). Building in that image avoids local-toolchain drift:

```bash
# from the repo root
docker run --rm -v "$PWD":/work -w /work \
    davesnowdon/alpha2build:sha-<tag> bash -lc './gradlew assembleRelease'
```

Docker writes build output as root; fix ownership afterwards if needed:

```bash
docker run --rm -v "$PWD":/work alpine chown -R "$(id -u):$(id -g)" /work
```

## Building the SDK

The SDK module `ubtechalpha2robot` builds to an `.aar`:

```
ubtechalpha2robot/build/outputs/aar/ubtechalpha2robot-release.aar
```

## Building an app against the SDK

Apps consume the SDK as a local `.aar` via `flatDir`. The reimplemented SDK depends
only on the Android framework, so — unlike the original decompiled jar — the app does
**not** need to re-declare any transitive third-party libraries. The example's
`app/build.gradle` is the reference; the SDK dependency is simply:

```groovy
dependencies {
    implementation(name: 'ubtechalpha2robot-release', ext: 'aar')
}
```

Copy the freshly built SDK `.aar` into the app before building (the app git-ignores
it so it can't drift from source):

```bash
cp ubtechalpha2robot/build/outputs/aar/ubtechalpha2robot-release.aar \
   examples/HelloAlpha/app/libs/
cd examples/HelloAlpha && ./gradlew assembleDebug
```

## Deploying to the robot

The robot is an Android device reachable over `adb` (via USB, or over the network
with screen mirroring tools such as Vysor as the community historically used).

```bash
adb devices -l                                   # confirm the robot is attached
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n <package>/.MainActivity
adb logcat -s <YourLogTag>
```

Notes from real sessions:

- `adb` may not be on `PATH`; it typically lives under
  `~/Android/Sdk/platform-tools/adb`.
- A reinstall can fail with `INSTALL_FAILED_UPDATE_INCOMPATIBLE` if a previous
  copy was signed with a different debug key. Uninstall first:
  `adb uninstall <package>` then install again.
- The robot's app id / appkey no longer needs to be valid (the fork removes the
  check), but the `Alpha2RobotApi` constructor still expects a non-empty string,
  and the manifest still expects an `alpha2_appid` `<meta-data>` entry. Any
  non-empty value works.

## Minimal manifest

Inside `<application>`:

```xml
<meta-data android:name="alpha2_appid" android:value="<any-non-empty-string>" />
```

Permissions the SDK expects (declare the ones your app uses): `INTERNET`,
`ACCESS_WIFI_STATE`, `ACCESS_NETWORK_STATE`, `RECORD_AUDIO`,
`MODIFY_AUDIO_SETTINGS`, `READ_PHONE_STATE`, `BROADCAST_STICKY`, `BLUETOOTH`,
`WRITE_SETTINGS`, `WRITE_EXTERNAL_STORAGE`.
