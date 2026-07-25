package com.davesnowdon.helloalpha;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ubtechinc.alpha2ctrlapp.network.action.ClientAuthorizeListener;
import com.ubtechinc.alpha2robot.Alpha2RobotApi;
import com.ubtechinc.alpha2robot.constant.AlphaConstant;
import com.ubtechinc.alpha2robot.constant.UbxErrorCode;
import com.ubtechinc.alpha2serverlib.interfaces.AlphaActionClientListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2ActionListListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2RobotClientListener;
import com.ubtechinc.alpha2serverlib.util.Alpha2SpeechMainServiceUtil;
import com.ubtechinc.constant.LanguageType;
import com.ubtechinc.constant.StaticValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sensor-driven smoke test / demo for the Alpha2OpenSdk.
 *
 * <p>The Alpha2 has no user-visible touchscreen, so this build is triggered by
 * the robot's own physical sensors and its speech recogniser rather than
 * on-screen buttons.
 *
 * <p><b>Head touch.</b> The robot's system service decodes a head-touch into a
 * key event and delivers it to apps as a broadcast
 * ({@link AlphaConstant#UBTEK_KEY_BROADCAST} = {@code "com.ubtechinc.key"}) with
 * a {@code Byte} extra {@link AlphaConstant#UBTEK_KEY_VALUE}. Hardware testing
 * confirmed the head exposes only two readable pads: front (id 5) and back
 * (id 4). This app maps the <b>front pad to speech</b> and the <b>back pad to a
 * motor test</b> (alternating a played action and a direct head-servo command).
 *
 * <p><b>Voice reactions.</b> The robot's live recogniser is Nuance, running
 * inside the platform-signed {@code com.ubtechinc.alpha2services} (uid=system).
 * There is no way to add words to its grammar (a custom {@code speech_initGrammar}
 * compiles but never reaches the active engine), but its <i>recognition results
 * are delivered to us</i> through the ordinary speech callback
 * ({@link IAlpha2RobotClientListener#onServerCallBack}) in the form
 * {@code "Local_Result:rule:<RULE> action:<ACTION> tag:<recognised text>"}.
 * Recognition is gated behind the wake word (<b>"hello alpha"</b>); wakeup itself
 * arrives on the {@link StaticValue#ALPHA_TTS_HINT} broadcast. This app watches
 * those results and plays a gesture for the intents it recognises (see
 * {@link #INTENT_GESTURES}) — so after "hello alpha" you can ask e.g.
 * <b>"how old are you"</b> and the robot answers <i>and</i> gestures. Unmapped
 * results are logged so more can be added. See docs/capabilities.md.
 *
 * <p><b>IMU.</b> The accelerometer (the robot's only real motion sensor) is read
 * through the standard Android {@link SensorManager}; readings are logged on
 * motion so tilting the robot is visible over {@code adb logcat}.
 *
 * <p><b>Poses.</b> At startup the pre-programmed action/pose names are fetched
 * via {@code action_getActionList} and logged.
 *
 * <p><b>Raw frames / keys.</b> Raw head/chest serial frames and every Android
 * {@link android.view.KeyEvent} are also logged (tag {@code HelloAlpha}).
 *
 * <p>If a call returns {@code API_ERROR_SUCCEED} and the robot physically
 * responds, the client-side de-restriction in this SDK build is working.
 */
public class MainActivity extends Activity implements SensorEventListener {

    private static final String TAG = "HelloAlpha";

    // Legacy UBTECH app id. Alpha2OpenSdk removes the store-authorization check,
    // so this is no longer validated, but the constructor still expects a
    // non-empty string. See the top-level project README.
    private static final String APP_ID = "222B998EDFA5FAD7FCE78678FB9F2521";

    // Minimum gap between two head-touch triggers, so a single pat that reports
    // several key events in quick succession only fires once.
    private static final long TOUCH_COOLDOWN_MS = 2500L;

    // Head touch-pad ids. These arrive as a Byte extra named
    // AlphaConstant.UBTEK_KEY_VALUE ("key") in the AlphaConstant.UBTEK_KEY_BROADCAST
    // broadcast. The SDK does not name the individual pads, so these values were
    // determined empirically on the robot (front pad -> 5, back pad -> 4).
    private static final int HEAD_SENSOR_FRONT = 5;
    private static final int HEAD_SENSOR_BACK = 4;

    // Neck yaw servo (left/right).
    private static final byte SERVO_HEAD_YAW = 19;

    // --- Voice reactions ---------------------------------------------------
    //
    // The robot's Nuance recogniser (wake word "hello alpha") classifies each
    // recognised utterance into a rule + "action" intent, e.g.
    //   "how tall are you" -> rule=QA            action=QA_KNOWING
    //   "sit down"          -> rule=Action_Performance action=AP_SQUAT
    // We cannot change its grammar, but its results are delivered to us on the
    // speech callback, so we play a gesture when a known intent is recognised.
    // Map intent -> pre-programmed action name (intents + names verified on a
    // real robot). Extend this as more intents are discovered from the logs.
    //
    // We map ONLY QA intents. On a QA intent the robot answers verbally but does
    // not move, so our gesture is the only motion and plays reliably. The robot
    // also has Action_Performance intents (AP_SQUAT for "sit down", AP_STAND_UP
    // for "stand up", AP_DANCE, AP_SHAKE_HEAD, AP_MOVE_FORWARD, ...) but it
    // performs those moves itself, which contends with / preempts a mapped action
    // — so we leave them to the robot and just log them. Extend with more QA
    // intents as they are discovered from the logs.
    private static final Map<String, String> INTENT_GESTURES = new LinkedHashMap<>();
    static {
        INTENT_GESTURES.put("QA_KNOWING", "Wave the left hand");   // "how tall are you"
        INTENT_GESTURES.put("QA_Age", "Raise both hands");         // "how old are you"
    }

    // Don't replay a gesture within this window.
    private static final long GESTURE_COOLDOWN_MS = 3500L;

    // Recognition results arrive on the speech callback prefixed like this.
    private static final String RECOGNITION_PREFIX = "Local_Result";

    // Candidate speech/command broadcasts, registered together so one hardware
    // session shows which actually reach a third-party app. In testing only the
    // wakeup hint (ALPHA_TTS_HINT) fired; the rest are kept for discovery.
    private static final String[] SPEECH_BROADCAST_ACTIONS = {
            StaticValue.ALPHA_TTS_HINT,
            StaticValue.ALPHA_SPEECH_DIRECTION,
            StaticValue.SPEECHCMD_ACTION,
            StaticValue.ALPHA_THIRD_PARTY_SPEECH_RECOGNIZERRESULT,
            StaticValue.ALPHA_THIRD_PARTY_SPEECH_TEXT,
    };

    private Alpha2RobotApi mRobot;
    private TextView mLog;
    private final ArrayList<String> mActions = new ArrayList<>();

    // IMU: the Alpha2's only real motion sensor is the accelerometer (the HAL
    // also lists gyro/mag/etc. but they are AOSP placeholders reading 0). It is
    // read through the standard Android SensorManager, not the UBTECH SDK.
    private SensorManager mSensors;
    private final float[] mLastAccel = new float[3];
    private boolean mHaveAccel = false;
    private long mLastAccelLogMs = 0L;

    // Debounce timestamps and back-pad toggle — only touched on the UI thread.
    private long mLastTouchMs = 0L;
    private long mLastGestureMs = 0L;
    private boolean mBackTurnHeadNext = false;
    private String mLastHeaderHex = "";
    private String mLastChestHex = "";

    /** Receives head-touch key events broadcast by the robot's system service. */
    private final BroadcastReceiver mKeyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onHeadKeyBroadcast(intent);
        }
    };

    /**
     * Logs speech-related broadcasts (wakeup, and the candidate command channels)
     * so we can see what the robot delivers to a third-party app.
     */
    private final BroadcastReceiver mSpeechReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onSpeechBroadcast(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLog = (TextView) findViewById(R.id.log);
        registerReceiver(mKeyReceiver, new IntentFilter(AlphaConstant.UBTEK_KEY_BROADCAST));
        registerReceiver(mSpeechReceiver, buildSpeechFilter());
        initRobot();
    }

    private static IntentFilter buildSpeechFilter() {
        IntentFilter filter = new IntentFilter();
        for (String action : SPEECH_BROADCAST_ACTIONS) {
            filter.addAction(action);
        }
        return filter;
    }

    private void initRobot() {
        log("SDK version " + Alpha2RobotApi.getSdkVersion());
        log("Creating Alpha2RobotApi (sensor-driven)...");

        // Subclass the API so the head/chest serial callbacks land here. Head
        // touch does NOT come this way (it is broadcast, see mKeyReceiver), but
        // obstacle/fall/etc. frames may, so log whatever arrives.
        mRobot = new Alpha2RobotApi(this, APP_ID, new ClientAuthorizeListener() {
            @Override
            public void onResult(int code, String info) {
                log("authorize onResult: code=" + code + " info=" + info);
            }
        }) {
            @Override
            public void onListenSerialPortHeaderRcvData(byte[] bytes, int len) {
                final String hex = toHex(bytes, len);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (!hex.equals(mLastHeaderHex)) { log("HEAD  <- " + hex); mLastHeaderHex = hex; }
                    }
                });
            }

            @Override
            public void onListenSerialPortRcvData(byte[] bytes, int len) {
                final String hex = toHex(bytes, len);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (!hex.equals(mLastChestHex)) { log("CHEST <- " + hex); mLastChestHex = hex; }
                    }
                });
            }
        };

        // Speech service. onServerCallBack carries both TTS server content AND the
        // recogniser's results (see onSpeechCallback). Greet once ready.
        mRobot.initSpeechApi(new IAlpha2RobotClientListener() {
            @Override
            public void onServerCallBack(String s) { onSpeechCallback(s); }
            @Override
            public void onServerPlayEnd(boolean done) { log("TTS play end: " + done); }
        }, new Alpha2SpeechMainServiceUtil.ISpeechInitInterface() {
            @Override
            public void initOver() {
                log("Speech service ready.");
                speakGreeting();
            }
        });

        // Action service (play named action files).
        mRobot.initActionApi(new AlphaActionClientListener() {
            @Override
            public void onActionStop(String name) { log("Action stopped: " + name); }
        });

        // Both serial links, so any raw obstacle/fall/sound frames are logged.
        boolean chest = mRobot.initChestSerialApi();
        boolean head = mRobot.initHeaderSerialApi();
        log("initChestSerialApi  -> " + chest);
        log("initHeaderSerialApi -> " + head);

        // Arm the chest sonar once the chest serial link has connected. Binding is
        // asynchronous, so this retries briefly instead of calling synchronously (which
        // would run before onServiceConnected and return NOT_INIT).
        armSonarWhenReady(0);

        // IMU: register the accelerometer (standard Android sensor framework).
        mSensors = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accel = (mSensors == null) ? null : mSensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accel != null) {
            mSensors.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
            log("IMU accelerometer registered — tilt/move the robot to see values.");
        } else {
            log("IMU: no accelerometer available.");
        }

        // Log the robot's pre-programmed action/pose names once the action
        // service has had a moment to bind.
        mLog.postDelayed(new Runnable() {
            @Override public void run() { logPoses(); }
        }, 1500);

        log("Ready. Touch my head, or say \"hello alpha\" then ask me something.");
    }

    /** Fetch and log every pre-programmed action/pose name (no playback). */
    private void logPoses() {
        UbxErrorCode.API_ERROR_CODE code = mRobot.action_getActionList(new IAlpha2ActionListListener() {
            @Override
            public void onGetActionList(ArrayList<ArrayList<String>> list) {
                int n = (list == null) ? 0 : list.size();
                log("Pre-programmed actions/poses: " + n + "  (type 1=action, 2=dance, 3=story)");
                if (list != null) {
                    for (ArrayList<String> item : list) {
                        if (item != null && item.size() > 2) {
                            log("  [" + item.get(1) + "] " + item.get(2));
                        }
                    }
                }
            }
        });
        log("action_getActionList (poses) -> " + code);
    }

    // Chest serial binds asynchronously; retry arming the sonar until it is ready.
    private static final int SONAR_ARM_MAX_ATTEMPTS = 15;
    private static final long SONAR_ARM_RETRY_MS = 300L;

    /**
     * Arm the chest sonar once the chest serial service is connected.
     * {@code chest_configureSonar} returns {@code NOT_INIT} until the bind completes, so
     * retry briefly and stop on the first success.
     */
    private void armSonarWhenReady(final int attempt) {
        UbxErrorCode.API_ERROR_CODE sonar = mRobot.chest_configureSonar(100);
        if (sonar == UbxErrorCode.API_ERROR_CODE.API_ERROR_SUCCEED) {
            log("chest_configureSonar(100) -> " + sonar);
        } else if (attempt < SONAR_ARM_MAX_ATTEMPTS) {
            mLog.postDelayed(new Runnable() {
                @Override public void run() { armSonarWhenReady(attempt + 1); }
            }, SONAR_ARM_RETRY_MS);
        } else {
            log("chest_configureSonar(100) -> " + sonar + " (chest serial not ready after retries)");
        }
    }

    private void speakGreeting() {
        UbxErrorCode.API_ERROR_CODE code = mRobot.speech_startTTS(
                LanguageType.LAU_ENGLISH,
                "Hello, I am Alpha 2. Touch my head, or say hello alpha and ask me something.",
                null);
        log("greeting speech_startTTS -> " + code);
    }

    // --- Voice reactions ---------------------------------------------------

    /**
     * The speech callback delivers both ordinary TTS server content and the
     * recogniser's results. Recognition results are prefixed "Local_Result" and
     * look like {@code Local_Result:rule:QA action:QA_Age tag:how old are you}.
     */
    private void onSpeechCallback(String s) {
        if (s == null) {
            return;
        }
        if (!s.startsWith(RECOGNITION_PREFIX)) {
            log("TTS callback: " + s);
            return;
        }
        String rule = fieldBetween(s, "rule:", " action:");
        String intent = fieldBetween(s, "action:", " tag:");
        String text = fieldBetween(s, "tag:", null);
        log("recognized: rule=" + rule + " action=" + intent + " text=\"" + text + "\"");

        String actionName = (intent == null) ? null : INTENT_GESTURES.get(intent);
        if (actionName != null) {
            playGesture(actionName);
        } else {
            log("(no gesture mapped for action=" + intent + " — add it to INTENT_GESTURES)");
        }
    }

    /** Play a gesture for a recognised phrase, with a cooldown. Robot still speaks. */
    private void playGesture(final String actionName) {
        long now = SystemClock.uptimeMillis();
        if (now - mLastGestureMs < GESTURE_COOLDOWN_MS) {
            log("** gesture " + actionName + " (ignored, cooling down)");
            return;
        }
        mLastGestureMs = now;
        runOnUiThread(new Runnable() {
            @Override public void run() {
                UbxErrorCode.API_ERROR_CODE code = mRobot.action_PlayActionName(actionName);
                log("** gesture -> action_PlayActionName(\"" + actionName + "\") -> " + code);
            }
        });
    }

    private void onSpeechBroadcast(Intent intent) {
        String action = intent.getAction();
        StringBuilder sb = new StringBuilder("speech bcast [").append(action).append("]:");
        Bundle extras = intent.getExtras();
        if (extras == null || extras.isEmpty()) {
            sb.append(" (no extras)");
        } else {
            for (String name : extras.keySet()) {
                sb.append(" [").append(name).append('=').append(extras.get(name)).append(']');
            }
        }
        log(sb.toString());
    }

    // --- Head-touch trigger (UI thread via broadcast) ----------------------

    /**
     * Dump every extra in the key broadcast so we can see how the value arrives
     * and which head sensor produced it, then fall through to the cycle handler
     * with a best-effort numeric value.
     */
    private void onHeadKeyBroadcast(Intent intent) {
        int key = -1;
        StringBuilder sb = new StringBuilder("key bcast:");
        Bundle extras = intent.getExtras();
        if (extras == null || extras.isEmpty()) {
            sb.append(" (no extras)");
        } else {
            for (String name : extras.keySet()) {
                Object value = extras.get(name);
                String type = (value == null) ? "null" : value.getClass().getSimpleName();
                sb.append(" [").append(name).append('=').append(value)
                        .append(" (").append(type).append(")]");
                if (value instanceof Number) {
                    key = ((Number) value).intValue();
                }
            }
        }
        log(sb.toString());
        onHeadTouch(key);
    }

    private void onHeadTouch(int key) {
        long now = SystemClock.uptimeMillis();
        if (now - mLastTouchMs < TOUCH_COOLDOWN_MS) {
            return;
        }
        mLastTouchMs = now;

        if (key == HEAD_SENSOR_FRONT) {
            log("** Front head pad -> TTS");
            testTts();
        } else if (key == HEAD_SENSOR_BACK) {
            // Only two head pads report on this broadcast, so the back pad
            // alternates between the action and servo subsystems to exercise
            // both from the same sensor.
            if (mBackTurnHeadNext) {
                log("** Back head pad -> servo (turn head)");
                testServo();
            } else {
                log("** Back head pad -> action");
                testAction();
            }
            mBackTurnHeadNext = !mBackTurnHeadNext;
        } else {
            log("** Head key=" + key + " (unmapped) -> TTS");
            testTts();
        }
    }

    /**
     * The side strips and back buttons are delivered as ordinary Android key
     * events to the focused window rather than the head-pad broadcast. Log every
     * key event (code + scancode) so they can be discovered, then let the system
     * handle it normally.
     */
    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
            log("keyEvent: code=" + event.getKeyCode()
                    + " scan=" + event.getScanCode()
                    + " (" + android.view.KeyEvent.keyCodeToString(event.getKeyCode()) + ")");
        }
        return super.dispatchKeyEvent(event);
    }

    // --- IMU (accelerometer) -----------------------------------------------

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }
        float x = event.values[0], y = event.values[1], z = event.values[2];
        long now = SystemClock.uptimeMillis();
        float delta = mHaveAccel
                ? Math.abs(x - mLastAccel[0]) + Math.abs(y - mLastAccel[1]) + Math.abs(z - mLastAccel[2])
                : Float.MAX_VALUE;
        // Log the first reading, then only on real motion, throttled, to avoid spam.
        if (!mHaveAccel || (delta > 1.2f && now - mLastAccelLogMs > 800L)) {
            log(String.format("IMU accel x=%.2f y=%.2f z=%.2f (m/s^2)", x, y, z));
            mLastAccelLogMs = now;
        }
        mLastAccel[0] = x; mLastAccel[1] = y; mLastAccel[2] = z;
        mHaveAccel = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    // --- SDK subsystem tests -----------------------------------------------

    /** Text-to-speech. */
    private void testTts() {
        UbxErrorCode.API_ERROR_CODE code =
                mRobot.speech_startTTS(LanguageType.LAU_ENGLISH, "Hello, I am Alpha 2", null);
        log("speech_startTTS -> " + code);
    }

    /** Fetch the action list, then play the first action it returns. */
    private void testAction() {
        UbxErrorCode.API_ERROR_CODE code = mRobot.action_getActionList(new IAlpha2ActionListListener() {
            @Override
            public void onGetActionList(ArrayList<ArrayList<String>> list) {
                mActions.clear();
                if (list != null) {
                    for (ArrayList<String> item : list) {
                        // Each row is [id, type, name]; name is index 2.
                        if (item != null && item.size() > 2 && item.get(2) != null) {
                            mActions.add(item.get(2));
                        }
                    }
                }
                log("Got " + mActions.size() + " actions.");
                if (!mActions.isEmpty()) {
                    String first = mActions.get(0);
                    UbxErrorCode.API_ERROR_CODE p = mRobot.action_PlayActionName(first);
                    log("action_PlayActionName(\"" + first + "\") -> " + p);
                } else {
                    log("No actions returned; cannot test playback.");
                }
            }
        });
        log("action_getActionList -> " + code);
    }

    /**
     * Turn the head. Servo 19 is the neck yaw (the same servo the SDK's
     * sound-localisation example drives).
     */
    private void testServo() {
        UbxErrorCode.API_ERROR_CODE code =
                mRobot.chest_SendOneFreeAngle(SERVO_HEAD_YAW, 90, (short) 1000);
        log("chest_SendOneFreeAngle(19, 90, 1000) -> " + code);
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mKeyReceiver);
        } catch (IllegalArgumentException ignored) {
            // Receiver was never registered; nothing to do.
        }
        try {
            unregisterReceiver(mSpeechReceiver);
        } catch (IllegalArgumentException ignored) {
            // Receiver was never registered; nothing to do.
        }
        if (mSensors != null) {
            mSensors.unregisterListener(this);
        }
        if (mRobot != null) {
            mRobot.releaseApi();
        }
        super.onDestroy();
    }

    // --- Helpers ------------------------------------------------------------

    /**
     * Return the text between {@code start} and {@code end} in {@code s}
     * (trimmed). If {@code end} is null, returns to the end of the string. Null
     * if {@code start} is absent.
     */
    private static String fieldBetween(String s, String start, String end) {
        int i = s.indexOf(start);
        if (i < 0) {
            return null;
        }
        i += start.length();
        int j = (end == null) ? s.length() : s.indexOf(end, i);
        if (j < 0) {
            j = s.length();
        }
        return s.substring(i, j).trim();
    }

    private static String toHex(byte[] bytes, int len) {
        if (bytes == null || len <= 0) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder(len * 3);
        int n = Math.min(len, bytes.length);
        for (int i = 0; i < n; i++) {
            sb.append(String.format("%02X", bytes[i] & 0xFF));
            if (i < n - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private void log(final String msg) {
        Log.i(TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLog.append(msg + "\n");
                final ScrollView sv = (ScrollView) mLog.getParent();
                sv.post(new Runnable() {
                    @Override public void run() { sv.fullScroll(View.FOCUS_DOWN); }
                });
            }
        });
    }
}
