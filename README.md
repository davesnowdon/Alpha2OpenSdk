# Alpha2OpenSdk

## Decompile official SDK

Decompile with Fernflower: <https://github.com/JetBrains/intellij-community/blob/0e2aa4030ee763c9b0c828f0b5119f4cdcc66f35/plugins/java-decompiler/engine/README.md> which ships with JetBrains IDE.

```bash
java -cp java-decompiler.jar org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler -hdc=0 -dgs=1 -rsy=1 -lit=1 ubtechalpha2robot.jar source/
```

## Included Open Source Code

The official SDK appears to include code for CodeHaus Jackson 1.8.3 and msgpack 0.6.11. It's not clear why these were embedded in the jar and  weren't referenced as dependencies. For now I've replaced the decompiled binaries for these libraries with the original source and made a few hacks so it builds. The decompiled source would not build "as is" either so I have my doubts whether this code was build for android or whether it is actually required by the Alpha 2 SDK. For now the aim to to get the SDK to a usable state and once this is done I will look to remove the other code embedded in it.

## Developing with ALpha 2

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

Currently the SDK makes use of a UBTECH "app id" - We think it is no longer possible to obtain these for Alpha2 from UBTECH. Eventually we hope to remove all mention of them from the SDK. For now, the recommendation is to try a string like "222B998EDFA5FAD7FCE78678FB9F2521"

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

You'll want to include (for now) the app ID in your manifest and require at least these permissions

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
<application
    ....…
    <meta-data
    android:name="alpha2_appid"
    android:value="222B998EDFA5FAD7FCE78678FB9F2521" /> xxxxx is the appkey applied for on the developer platform
</application>

```

### Initialise the SDK

In your initialisation logic you will need to create an instance of a robot

```java
mRobot = new Alpha2RobotApi(this, "Your Appkey");
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
            mRobot.speech_setRecognizedLanguage(LauguageType.LAU_CHINESE);
            ...
    }
});
// Broadcast TTS message (recommended using this API)
mRobot.speech_startTTS(LauguageType.LAU_CHINESE, "Hello", "xiaoyan");
// Broadcast TTS message (not recommended using this API)
mRobot.speech_StartTTS("Hello, my name is Alpha, nice to meet you..");
// This API is not recommended for use
mRobot.speech_startTTS("Hello, my name is Alpha", "catherine");
```

#### Speech understanding

```java
/**
 * Initialize semantic understanding
 * @param  strGramma - User-defined offline semantics
 * @param  listener - Callback for semantic understanding
 * @return  API_ERROR_CODE
 */
 public API_ERROR_CODE speech_initGrammar(String strGramma, IAlpha2SpeechGrammarInitListener listener)

/**
 * Register the callback interface for semantic understanding
 * @param  listener - the callback listener which indicates the result of initialization
 * @return  API_ERROR_CODE
 */
 public API_ERROR_CODE speeh_startGrammar(IAlpha2SpeechGrammarListener listener) 

/**
 * Specify the language of recognition
 * @param strLanguage - : en_us, zh_cn
 * @return  API_ERROR_CODE
 */
public API_ERROR_CODE speech_setRecognizedLanguage(String strLanguage) 

/**
 * Semantic understanding
 * @deprecated This API is outdated, not recommended for use, suggest using speech_initGrammar instead
 * @param  strText - the content which will be semantically analyzed
 * @return  API_ERROR_CODE
 */
public API_ERROR_CODE speech_understandText(String strText,
      IAlpha2RobotTextUnderstandListener mRobotTextListener)
```

Usage

```java
 mRobot.speech_initGrammar(mLocalGrammar, new IAlpha2SpeechGrammarInitListener() {
    @Override
    public void speechGrammarInitCallback(String s, int i) {
        // Semantic understanding initialization
        mRobot.speeh_startGrammar(new NewSDKActivity());
    }
});
mRobot.speeh_startGrammar(new IAlpha2SpeechGrammarListener() {
    /**
     * Callback for semantic understanding
     * @param  SpeechResultType - Return parameter type
     * @param  strResult - Return data
     */
    @Override
    public void onSpeechGrammarResult(int SpeechResultType, String strResult) {
        
    }
   /**
     * Error callback
     * @param  i - Error code
     */    
    @Override
    public void onSpeechGrammarError(int i) {
    
    }
});
mRobot.speech_setRecognizedLanguage(LauguageType.LAU_CHINESE);
mRobot.speech_understandText(strTts, new IAlpha2RobotTextUnderstandListener() {
         /**
           * Error callback
           * @param  i - Error code
           */
         @Override
         public void onAlpha2UnderStandError(int nErrorCode) {
         }
        /**
           * Get semantic result
           * @param  strResult - Recognized data
           */
         @Override
         public void onAlpha2UnderStandTextResult(String strResult) {
             
         }
      });
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

```java
/**
 * Used to control the ear LEDs on the 5mic robot
 *
 * @param color - LEDs color (1 R, 2 G, 3 B,4 RG,5 RB,6 GB,7 RGB)
 * @param  bright - LEDs brightness
 * @param  rightEar - the 8 LEDs on the right, 255 means controlling all 8 LEDs
 * @param  leftEar - the 8 LEDs on the left, 255 means controlling all 8 LEDs
 * @param  upTime - Time the lights are on
 * @param  downTime - Time the lights are off
 * @param  runTime - Time the lights are on and off
 * @param  mode - the work mode of LEDs
              0:normal mode, the units are all 10ms
              1:start breathe, the unit of upTime is 200ms, the unit of downTime is 1ms, runTime is useless
              2:stop breathe
 * @return  API_ERROR_CODE
 */
public API_ERROR_CODE header_startEarLED(int color, int bright, int rightEar, int leftEar,
                               int upTime, int downTime, int runTime, int mode) 
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


