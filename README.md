# Alpha2OpenSdk

An open SDK for building Android apps that run on the UBTECH Alpha2 robot. It
exposes the robot's on-device capabilities — speech/TTS, action playback, servos,
LEDs and sensors — through a single class, `Alpha2RobotApi`, and removes the
defunct store-authorization gate that made the original SDK unusable now that
UBTECH's cloud has shut down.

## What this is (and how it was built)

This SDK is an **original reimplementation** written against the robot's
documented service interface (AIDL, broadcast actions, the serial protocol) — not
a copy of UBTECH's SDK. The interface-level facts that must match for the robot to
accept a call — AIDL method names and order, service/broadcast action strings,
serial command bytes, constant values — are reproduced because interoperability
requires them; everything else is freshly authored. The reverse-engineering and
firmware-analysis notes that informed it are kept in a separate private archive.

## Third-party code

The SDK depends only on the Android framework and the Java standard library — it
vendors no third-party libraries. See the `NOTICE` file.

## Developing with Alpha 2

You'll probably want to use Vysor in order to interact with android on the robot.

Download vysor from <https://www.vysor.io/>

### On Linux

You may need to edit udev rules in order for adb in order to connect to the robot

run `lsusb`

You will see something like this (on my machine this shows 19 devices which I haven't listed for brevity)

```bash
$ lsusb
...
Bus 001 Device 057: ID 2207:0011  
...
```
On my machine it's not obvious which of the many USB devices is the Alpha2. I settled for running `lsusb` with Alpha2 not plugged in, plugging in Alpha 2, and running the command again and doing a diff. The interesting number is `2207:0011` The first part if the vendor ID and the second the product ID.

You will need to create a file in `/etc/udev/rules.d`. I called mine `45-alpha2.rules` - I don't think the filename is important as long as it's unique and obvious what it's for.

The contents should look like this

```text
SUBSYSTEM=="usb", ATTR{idVendor}=="2207", ATTR{idProduct}=="0011", MODE="0660", 
GROUP="plugdev", SYMLINK+="android%n"
```

You'll then want to unplug Alpha2's USB connection and then plug in again. if you run `adb devices` you should see something like

```text
$ ./adb devices
List of devices attached
40AI8N0HAU	device
```

If you launch Vysor, you should then see an entry for Alpha 2 and be able to launch a window giving you access to the android UI on Alpha 2.


## How to use the SDK

The official SDK gated TTS and actions behind a UBTECH "app id" that had to be activated against UBTECH's (now-defunct) servers, and it is no longer possible to obtain one. This fork **removes that store-authorization check**, so the app id is no longer validated — but the constructor and the `alpha2_appid` manifest entry still structurally expect a non-empty string, so pass any non-empty value such as "222B998EDFA5FAD7FCE78678FB9F2521". See `docs/authorization.md` for details.

### Add the SDK to your android app

- Create a folder called "libs" under the "app" folder in your android project
- download the built SDK (alpha2opensdk.aar.zip) from the releases area in the SDK repo
- unzip alpha2opensdk.aar.zip and copy ubtechalpha2robot-release.aar to the libs folder
- update the dependencies section of the app build.gradle file to include

```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])
    ...
}
```

### Android Manifest settings

Include the (no-longer-validated but structurally required) app ID in your manifest and require at least these permissions

```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.BROADCAST_STICKY" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<application ... >
    <!-- The value is no longer validated; any non-empty string works. -->
    <meta-data
        android:name="alpha2_appid"
        android:value="222B998EDFA5FAD7FCE78678FB9F2521" />
</application>
```

### Initialise the SDK

In your initialisation logic you will need to create an instance of a robot

```java
// The public constructor takes a ClientAuthorizeListener with a single
// onResult(int, String) callback. Because this fork removes the store-authorization
// check, authorization always succeeds (result code 1) regardless of the app id —
// but a non-empty appKey is still structurally required.
mRobot = new Alpha2RobotApi(this, "Your Appkey", new ClientAuthorizeListener() {
    @Override
    public void onResult(int code, String info) { /* code 1 = authorized */ }
});
mRobot.initSpeechApi(NewSDKActivity.this, NewSDKActivity.this, null); // Initialize speech service
mRobot.initActionApi(NewSDKActivity.this); // Initialize action service
mRobot.initCustomMessageApi(NewSDKActivity.this); // Initialize third-party channel
```

### Voice functions

The voice function includes controlling the robot's TTS broadcast, setting the broadcast language type, and setting the speaker.

#### Making the robot speak

```java
/**
 * @param  mRobotClient - Callback interface for TTS broadcast completion
 * @param  mSpeechInitListener - Callback interface for successful voice initialization
 * @param  specifyLanguage - Specified language type
 * @return  Initialization success returns true
 */
public boolean initSpeechApi(IAlpha2RobotClientListener mRobotClient,ISpeechInitInterface mSpeechInitListener,CustomLanguage specifyLanguage) 

/**
 * Broadcast TTS message 
 * @param  mRobotClient - Specifies the language of playback
 * @param  mSpeechInitListener - Specifies the content of playback
 * @param  specifyLanguage - Specifies the speaker, if not specified, the default speaker is used
 * @return  
 */
public API_ERROR_CODE speech_StartTTS (String language, String text, String strVoicName)

/**
 * Stop broadcasting TTS message 
 * @return  
 */
public API_ERROR_CODE speech_StopTTS() 

public API_ERROR_CODE speech_StartTTS (String text, String strVoicName)

public API_ERROR_CODE speech_StartTTS (String text)
```

How to use

```java
mRobot.initSpeechApi(new IAlpha2RobotClientListener() {
    @Override
    public void onServerCallBack(String s) {
        // Callback for TTS broadcast content
    }
    @Override
    public void onServerPlayEnd(boolean b) {
        // Callback for TTS broadcast completion
    }
}, new Alpha2SpeechMainServiceUtil.ISpeechInitInterface() {
    @Override
    public void initOver() {
            // Specify the language of voice recognition (Chinese or English)
            mRobot.speech_setRecognizedLanguage(LanguageType.LAU_CHINESE);
            ...
    }
});
// Broadcast TTS message (recommended using this API)
mRobot.speech_startTTS(LanguageType.LAU_CHINESE, "Hello", "xiaoyan");
// Broadcast TTS message (not recommended using this API)
mRobot.speech_StartTTS("Hello, my name is Alpha, nice to meet you..");
// This API is not recommended for use
mRobot.speech_startTTS("Hello, my name is Alpha", "catherine");
```

#### Speech understanding (voice commands)

> On this robot's firmware
> the **active** recogniser is **Nuance VoCon** — offline, inside the platform-signed
> system app, with a **fixed built-in grammar**. Consequences:
>
> - `speech_initGrammar` / `speech_startGrammar` drive the **inactive** iFlytek engine, so
>   they compile and return success but **do not affect recognition** — you cannot add your
>   own vocabulary without the proprietary Nuance grammar compiler.
> - `speech_understandText` used UBTECH's **cloud** NLU service, which is gone.
>
> What actually works is reacting to the robot's **built-in** commands: recognition is gated
> behind the wake word **"hello alpha"** and results arrive through
> `IAlpha2RobotClientListener.onServerCallBack(String)`. See
> **[docs/speech-recognition.md](docs/speech-recognition.md)** for the full built-in grammar
> (48 `AP_*` + 13 `QA_*` intents) and **[examples/HelloAlpha](examples/HelloAlpha)**.

Reacting to a built-in voice command (the listener you passed to `initSpeechApi`):

```java
@Override
public void onServerCallBack(String s) {
    // Recognition results are prefixed "Local_Result", e.g.
    //   "Local_Result:rule:QA action:QA_KNOWING tag:how tall are you"
    if (s == null || !s.startsWith("Local_Result")) return;
    String intent = fieldBetween(s, "action:", " tag:");   // -> "QA_KNOWING"
    if ("QA_KNOWING".equals(intent)) {
        mRobot.action_PlayActionName("Wave the left hand");
    }
}
```


#### Microphone access

If you want access to the microphone for recording audio you need to gain access using setMIC and then return control to the robot when done.

```java
/**
 * If your application needs to use the microphone, the robot should release the microphone
 * @param  isWake - false, the robot occupies the microphone; true, the robot releases the microphone
 * @return  Indicates whether the operation was successful
 */
public boolean speech_SetMIC(boolean isWake)
```

usage

```java
// reserve the microphone resource in the callback of voice initialization completion
@Override
public void initOver() {
   mRobot.speech_SetMIC(true);
}

// release microphone resource when the app exits
public class ExitBroadcast extends BroadcastReceiver {
   @Override
   public void onReceive(Context arg0, Intent intent) {
      // TODO Auto-generated method stub
      if (intent.getAction().equals(DeveloperAppStaticValue.APP_EXIT)) {
         MainActivity.this.finish();
         mRobot.speech_SetMIC(false);
         System.exit(0);
      }
}
```

### Movement

#### Action files

The robot can be made to move by exeecuting action files (more detail on these needed).

Note: When calling these APIs, you should have called `Alpha2RobotApi(Context ctx,String appkey,ClientAuthorizeListener listener)`

```java
/**
 * Initialize action service
 * @param  Action completion callback listener
 * @return 
 */
public boolean initActionApi(AlphaActionClientListener listener)

/**
 * Get action list.
 * @param listener - Listener for getting action list
 * @return  API_ERROR_CODE
 */
public API_ERROR_CODE action_getActionList(IAlpha2ActionListListener listener)

/**
 * Robot executes action file
 * @param actionName - The name of the action file (can be in Chinese or English)
 * @return API_ERROR_CODE
 */
public API_ERROR_CODE action_PlayActionName(String actionName)

/**
 * Stop performing action
 * @return API_ERROR_CODE
 */
public API_ERROR_CODE action_StopAction()
```

Usage

```java
//1. Initialize action service
mRobot.initActionApi(new AlphaActionClientListener() {
    @Override
    public void onActionStop(String actionName) {
        // Action callback interface
    }
});

//2. Get action list
mRobot.action_getActionList(new IAlpha2ActionListListener() {
    @Override
    public void onGetActionList(ArrayList<ArrayList<String>> list) {
        // After getting the action list, initialize the action list into the collection
        initActionList(list);
    }
});

//3. mAlphaActionList action list collection, mAlphaDanceList dance list collection, mAlphaStoryList story list collection
private void initActionList(ArrayList<ArrayList<String>> list) {
    if(list != null) {
        for (ArrayList<String> item : list) {
            if (item.get(1) != null && item.get(2) != null) {
                if("1".equals(item.get(1))) {
                    mAlphaActionList.add(item.get(2));
                } else if("2".equals(item.get(1))) {
                    mAlphaDanceList.add(item.get(2));
                } else if("3".equals(item.get(1))) {
                    mAlphaStoryList.add(item.get(2));
                }
            }
        }
    }
}

//4. Stop action：
mRobot.action_StopAction();

// 5. Play action file
mRobot.action_PlayActionName("Squat and Stand Up");
```

#### Direct servo control

Note: When calling the following APIs, you should have called `Alpha2RobotApi(Context ctx,String appkey,ClientAuthorizeListener listener)`

```java
/**
  * Initialize serial port service 
  * @param
  */
public boolean initChestSerialApi()

/**
 * Send data for 20 servos at once, setting angle data to control the robot's posture,
 * Note: This API will automatically detect and check the angle parameters
 * @param  data - The index represents the servo number, the value represents the angle
 * @param  time - Total execution time
 * @return  API_ERROR_CODE
 */
public API_ERROR_CODE chest_SendFreeAngle(int[] data, short time)

/**
 * Send single servo parameter to control the robot
 * @param   id - Servo number (0~19)
 * @param  angle - Angle parameter for the specified servo
 * @param  time - Total execution time
 * @return  API_ERROR_CODE
 */
public API_ERROR_CODE chest_SendOneFreeAngle(byte id, int angle, short time)
```

Note: An angle of 250 can make the corresponding servo number not move. There was apparently documentation for this in an Excel file but it's unclear where to find it.

#### LED control

The head has ear and eye LED rings. The ear ring takes up/down/run timings; the
eye ring additionally takes a colour. (UBTECH's original docs describe an 8-arg
`header_startEarLED` for a 5-mic variant; this SDK's actual signatures are below.)

```java
// Ear LEDs: fade-up, fade-down and run times (shorts, in the head board's units).
robot.header_startEarLED((short) upTime, (short) downTime, (short) runTime);
robot.header_stopEarLED();

// Eye LEDs: colour (1 R, 2 G, 3 B, 4 RG, 5 RB, 6 GB, 7 RGB) plus the same timings.
robot.header_startEyeLED(colorType, (short) upTime, (short) downTime, (short) runTime);
robot.header_stopEyeLED();
```

#### Speech localisation

Users can interact with the robot through voice, and the robot turns its head towards the user, enhancing user experience.

```java
//1. Add broadcast receiver for sound direction types
filter.addAction(StaticValue.ALPHA_SPEECH_DIRECTION);

//2. In the broadcast receiver, get the detected angle value
public void onReceive(Context arg0, Intent intent) {
    String action = intent.getAction();
    if (StaticValue.ALPHA_SPEECH_DIRECTION.equals(action)) {
        /**
         * Angle of the sound
         */
        byte angle = intent.getByteExtra("absoluteAngle", (byte) 0);
        processSpeechAngle(angle);
    }
}

//3. Turn head
private void processSpeechAngle(byte angle) {
    if (isWakeup) 
        return;
    int angleINT = angle;
    int angleHigh = (angleINT << 8);
    if (angleINT < 0) {
        angleINT = 256 + angleINT;
    }
    if (getmState() == RobotState.IDEL) {
        mRobot.chest_SendOneFreeAngle((byte) 19, angleINT, (short) 500);
    }
}
```


