package com.ubtechinc.alpha2robot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import com.ubtechinc.LuancherAppManager;
import com.ubtechinc.alpha2ctrlapp.network.action.ClientAuthorizeListener;
import com.ubtechinc.alpha2ctrlapp.network.action.UserAction;
import com.ubtechinc.alpha2ctrlapp.network.model.request.AuthenticationRequest;
import com.ubtechinc.alpha2robot.constant.UbxErrorCode;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionClient;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaEnglishOfflineUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaEnglishUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener;
import com.ubtechinc.alpha2serverlib.eventdispatch.AlphaEvent;
import com.ubtechinc.alpha2serverlib.interfaces.Alpha2SerialPortHeaderOnRcvListener;
import com.ubtechinc.alpha2serverlib.interfaces.Alpha2SerialPortOnRcvListener;
import com.ubtechinc.alpha2serverlib.interfaces.AlphaActionClientListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2ActionListListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2CustomMessageListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2EnglishOfflineUnderstandListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2EnglishUnderstandListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2RobotClientListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2RobotTextUnderstandListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2SpeechGrammarInitListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2SpeechGrammarListener;
import com.ubtechinc.alpha2serverlib.util.Alpha2SerialHeaderServiceUtil;
import com.ubtechinc.alpha2serverlib.util.Alpha2SerialServiceUtil;
import com.ubtechinc.alpha2serverlib.util.Alpha2SpeechMainServiceUtil;
import com.ubtechinc.alpha2serverlib.util.Alpha2XmppServiceUtil;
import com.ubtechinc.alpha2serverlib.util.AlphaActonServiceUtil;
import com.ubtechinc.alpha2serverlib.util.AlphaMainServiceUtil;
import com.ubtechinc.contant.CustomLanguage;
import com.ubtechinc.developer.DeveloperAngle;
import com.ubtechinc.developer.DeveloperAppButtenEventData;
import com.ubtechinc.developer.DeveloperAppConfigData;
import com.ubtechinc.developer.DeveloperAppData;
import com.ubtechinc.developer.DeveloperEarLedData;
import com.ubtechinc.developer.DeveloperEyesLedData;
import com.ubtechinc.developer.DeveloperPacketData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Alpha2RobotApi implements Alpha2SerialPortOnRcvListener, Alpha2SerialPortHeaderOnRcvListener {
   private static String sdkVersion = "2.0.0.2";
   private static String HAVE_NUANCE_OFFLINE_AUTHORITY = "nuance_offline_authority";
   private static int CUSTOM_CMD = 0;
   private static int CUSTOM_RESP = 1;
   private Context mContext;
   private String mAppID;
   private AlphaActonServiceUtil mActionServiceUtil;
   private Alpha2SerialServiceUtil mChestSerialServiceUtil;
   private Alpha2SerialHeaderServiceUtil mHeaderSerivalServiceUtil;
   private Alpha2SpeechMainServiceUtil mSpeechServiceUtil;
   private Alpha2XmppServiceUtil mXmppServiceUtil;
   private IAlpha2RobotClientListener mRobotClient;
   private IAlpha2RobotTextUnderstandListener mRobotTextListener;
   private IAlpha2EnglishUnderstandListener mEnglishUnderstandListener;
   private IAlpha2EnglishOfflineUnderstandListener mEnglishOfflineListener;
   private IAlpha2SpeechGrammarInitListener mSpeechGrammarInitListener;
   private IAlpha2SpeechGrammarListener mSpeechGrammarListener;
   private AlphaActionClientListener mActionListener;
   private boolean isAuthorize = true; // default to true now no longer possible to add apps to UBTECH store
   private boolean isNuanceOfflineAuthorize = false;
   private SharedPreferences sharedPreferences = null;
   private Editor editor = null;

   public static String getSdkVersion() {
      return sdkVersion;
   }

   public static String getServerVersion() {
      return AlphaMainServiceUtil.getVersion();
   }

   private boolean isAuthorize() {
      return this.isAuthorize;
   }

   private void setAuthorize(boolean isAuthorize) {
      Log.d("Alpha2RobotApi", "setAuthorize = "+ isAuthorize);
      this.isAuthorize = isAuthorize;
   }

   private boolean isNuanceOfflineAuthorize() {
      return this.isNuanceOfflineAuthorize;
   }

   private void setNuanceOfflineAuthorize(boolean isNuanceOfflineAuthorize) {
      this.isNuanceOfflineAuthorize = isNuanceOfflineAuthorize;
   }

   private Alpha2RobotApi(Context context, String appID) {
      this.mContext = context;
      this.mAppID = appID;
   }

   public Alpha2RobotApi(Context context, String appKey, ClientAuthorizeListener listenr) {
      this.mContext = context;
      this.mAppID = appKey;
      this.initSharedPreference();
      this.doProcess(listenr);
   }

   public void releaseApi() {
      if (this.mActionServiceUtil != null) {
         this.mActionServiceUtil.ReleaseConnection();
         this.mActionServiceUtil = null;
      }

      if (this.mSpeechServiceUtil != null) {
         this.mSpeechServiceUtil.ReleaseConnection();
         this.mSpeechServiceUtil = null;
      }

      if (this.mChestSerialServiceUtil != null) {
         this.mChestSerialServiceUtil.ReleaseConnection();
         this.mChestSerialServiceUtil = null;
      }

      if (this.mHeaderSerivalServiceUtil != null) {
         this.mHeaderSerivalServiceUtil.ReleaseConnection();
         this.mHeaderSerivalServiceUtil = null;
      }

      if (this.mXmppServiceUtil != null) {
         this.mXmppServiceUtil.ReleaseConnection();
         this.mXmppServiceUtil = null;
      }

   }

   public boolean initActionApi(AlphaActionClientListener listener) {
      boolean bRet = true;
      if (!this.isAuthorize) {
         return bRet;
      } else {
         this.mActionListener = listener;
         IAlphaActionClient client = new Alpha2RobotApi.ActionClientListener();
         if (this.mActionServiceUtil == null) {
            Log.d("", "!!!! mActionServiceUtil == null");
            this.mActionServiceUtil = new AlphaActonServiceUtil(this.mContext, client);
         }

         return bRet;
      }
   }

   public boolean initChestSeiralApi() {
      boolean bRet = true;
      if (this.mChestSerialServiceUtil == null) {
         this.mChestSerialServiceUtil = new Alpha2SerialServiceUtil(this.mContext, this);
      }

      return bRet;
   }

   public boolean initHeaderSerialApi() {
      boolean bRet = true;
      if (this.mHeaderSerivalServiceUtil == null) {
         this.mHeaderSerivalServiceUtil = new Alpha2SerialHeaderServiceUtil(this.mContext, this);
      }

      return bRet;
   }

   public UbxErrorCode.API_EEROR_CODE header_setNoise(boolean isOpen) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mHeaderSerivalServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         byte[] noiseData;
         if (!isOpen) {
            noiseData = new byte[]{1};
            this.mHeaderSerivalServiceUtil.sendCommand((byte)39, noiseData, noiseData.length);
         } else {
            noiseData = new byte[]{0};
            this.mHeaderSerivalServiceUtil.sendCommand((byte)39, noiseData, noiseData.length);
         }

         return nState;
      }
   }

   public boolean initSpeechApi(IAlpha2RobotClientListener mRobotClient, Alpha2SpeechMainServiceUtil.ISpeechInitInterface mSpeechInitListener) {
      return this.initSpeechApi(mRobotClient, mSpeechInitListener, CustomLanguage.DEFAULT_LANGUAGE);
   }

   public boolean initSpeechApi(IAlpha2RobotClientListener mRobotClient, Alpha2SpeechMainServiceUtil.ISpeechInitInterface mSpeechInitListener, CustomLanguage specifyLanguage) {
      boolean bRet = true;
      if (!this.isAuthorize) {
         return bRet;
      } else {
         if (this.mSpeechServiceUtil == null) {
            this.mRobotClient = mRobotClient;
            IAlpha2SpeechClientListener.Stub mClientListener = new Alpha2RobotApi.SpeechClientImp();
            this.mSpeechServiceUtil = new Alpha2SpeechMainServiceUtil(this.mContext, mClientListener, mSpeechInitListener, specifyLanguage);
         }

         return bRet;
      }
   }

   public boolean speech_SetMIC(boolean isWake) {
      boolean bRet = true;
      if (!this.isAuthorize) {
         return bRet;
      } else {
         String TAG = "Alpha2VoiceMainService";
         Log.d("Alpha2VoiceMainService", "SpeechDemoActivity | " + isWake + "mSpeechServiceUtil=" + this.mSpeechServiceUtil);
         if (this.mSpeechServiceUtil != null) {
            this.mSpeechServiceUtil.setWakeState(isWake);
         }

         return bRet;
      }
   }

   public UbxErrorCode.API_EEROR_CODE action_getActionList(IAlpha2ActionListListener listener) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mActionServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mActionServiceUtil.getActionList(listener);
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE action_PlayActionName(String actionName) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mActionServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mActionServiceUtil.playActionName(actionName, (AlphaEvent)null);
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE action_StopAction() {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mActionServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mActionServiceUtil.stopActionPlay();
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE speech_setVoiceName(String strVoiceName) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mSpeechServiceUtil.setVoiceName(strVoiceName);
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE speech_startTTS(String text, String strVoicName) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      nState = this.speech_startTTS((String)null, text, strVoicName);
      return nState;
   }

   public UbxErrorCode.API_EEROR_CODE speech_StartTTS(String text) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      nState = this.speech_startTTS((String)null, text, (String)null);
      return nState;
   }

   public UbxErrorCode.API_EEROR_CODE speech_startTTS(String language, String text, String strVoicName) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mSpeechServiceUtil.onPlay(text, strVoicName, language, true);
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE speech_StopTTS() {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mSpeechServiceUtil.onStopPlay();
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE speech_setRecognizedLanguage(String strLanguage) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mSpeechServiceUtil.setRecognizedLanguage(strLanguage);
         return nState;
      }
   }

   /** @deprecated */
   public UbxErrorCode.API_EEROR_CODE speech_startRecognized(String text) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mSpeechServiceUtil.onSpeech(text);
         return nState;
      }
   }

   /** @deprecated */
   public UbxErrorCode.API_EEROR_CODE speech_stopRecognized() {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mSpeechServiceUtil.onStopSpeech();
         return nState;
      }
   }

   /** @deprecated */
   public UbxErrorCode.API_EEROR_CODE speech_understandText(String strText, IAlpha2RobotTextUnderstandListener mRobotTextListener) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mRobotTextListener = mRobotTextListener;
         IAlphaTextUnderstandListener listener = new Alpha2RobotApi.SpeechTextUnderstand();
         this.mSpeechServiceUtil.textUnderstand(strText, listener);
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE speech_initGrammar(String strGramma, IAlpha2SpeechGrammarInitListener listener) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mSpeechGrammarInitListener = listener;
         this.mSpeechServiceUtil.initSpeechGrammar(strGramma, new ISpeechGrammarInitListener.Stub() {
            public void speechGrammarInitCallback(String arg0, int arg1) throws RemoteException {
               if (Alpha2RobotApi.this.mSpeechGrammarInitListener != null) {
                  Alpha2RobotApi.this.mSpeechGrammarInitListener.speechGrammarInitCallback(arg0, arg1);
               }

            }
         });
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE speeh_startGrammar(IAlpha2SpeechGrammarListener listener) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mSpeechGrammarListener = listener;
         this.mSpeechServiceUtil.startSpeechGrammar(listener);
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE speech_stopGrammaer() {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mSpeechServiceUtil.stopSpeechGrammar();
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE speech_setSelfInterrupt(boolean isInterrupt) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mSpeechServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.mSpeechServiceUtil.setSelfInterrupt(isInterrupt);
         return nState;
      }
   }

   public void onListenSerialPortRcvData(byte[] bytes, int len) {
   }

   public void onListenSerialPortHeaderRcvData(byte[] bytes, int len) {
   }

   public boolean isSystemApp(PackageInfo pInfo) {
      return (pInfo.applicationInfo.flags & 1) != 0;
   }

   private void doProcess(final ClientAuthorizeListener listener) {
      Alpha2RobotApi.this.isAuthorize = true;
      Alpha2RobotApi.this.isNuanceOfflineAuthorize = true;
      Alpha2RobotApi.this.editor.putString(Alpha2RobotApi.this.mContext.getPackageName(), info);
      Alpha2RobotApi.this.editor.putBoolean(Alpha2RobotApi.HAVE_NUANCE_OFFLINE_AUTHORITY, true);
      listener.onResult(2, "have offline authority");
   }

   private String readAppFile(String code) {
      String configName = "config.json";
      StringBuilder returnString = new StringBuilder();
      InputStream fIn = null;
      InputStreamReader isr = null;
      BufferedReader input = null;

      String line;
      try {
         String path = this.mContext.getFilesDir().getParent();
         File file_file = new File(path + "/files/" + configName);
         if (file_file.exists()) {
            fIn = new FileInputStream(file_file);
            isr = new InputStreamReader(fIn, code);
            input = new BufferedReader(isr);

            while((line = input.readLine()) != null) {
               returnString.append(line);
            }

            return returnString.toString();
         }

         line = null;
      } catch (Exception var20) {
         var20.getMessage();
         return returnString.toString();
      } finally {
         try {
            if (isr != null) {
               isr.close();
            }

            if (fIn != null) {
               fIn.close();
            }

            if (input != null) {
               input.close();
            }
         } catch (Exception var19) {
            var19.getMessage();
         }

      }

      return line;
   }

   private String readAssetsFile(String code) {
      String configName = "config.json";
      StringBuilder returnString = new StringBuilder();
      InputStream fIn = null;
      InputStreamReader isr = null;
      BufferedReader input = null;

      Object var8;
      try {
         fIn = this.mContext.getResources().getAssets().open(configName);
         isr = new InputStreamReader(fIn, code);
         input = new BufferedReader(isr);

         String line;
         while((line = input.readLine()) != null) {
            returnString.append(line);
         }

         return returnString.toString();
      } catch (Exception var18) {
         var18.getMessage();
         var8 = null;
      } finally {
         try {
            if (isr != null) {
               isr.close();
            }

            if (fIn != null) {
               fIn.close();
            }

            if (input != null) {
               input.close();
            }
         } catch (Exception var17) {
            var17.getMessage();
         }

      }

      return (String)var8;
   }

   public String[] readConfig(String code) {
      String[] config = new String[2];
      String configData = this.readAppFile(code);
      if (configData == null) {
         configData = "";
      }

      String configTag = this.readAssetsFile(code);
      if (configTag == null) {
         configTag = "";
      }

      config[0] = configTag;
      config[1] = configData;
      return config;
   }

   public void writeConfig(Intent intent) {
      Bundle bundle = intent.getExtras();
      DeveloperAppData appData = (DeveloperAppData)bundle.getSerializable("appdata");
      byte[] data = appData.getDatas();
      data = this.removeMessyCode(data);
      String json = new String(data);
      Log.i("appdata", "json= " + json);
      String path = this.mContext.getFilesDir().getParent();
      File file_file = new File(path + "/files/config.json");

      try {
         FileOutputStream fs = new FileOutputStream(file_file.getAbsolutePath());
         fs.write(data);
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   public boolean sendConfig2Server(Intent intent, String packageName, String code) {
      Bundle bundle = intent.getExtras();
      DeveloperAppData appData = (DeveloperAppData)bundle.getSerializable("appdata");
      byte[] data = appData.getDatas();
      String lauange = new String(data);
      Log.i("appdata", "lauange= " + lauange);
      String[] json = this.readConfig(code);
      DeveloperAppConfigData appConfig = new DeveloperAppConfigData();
      appConfig.setCmd(appData.getCmd());
      appConfig.setTags(json[0].getBytes());
      appConfig.setDatas(json[1].getBytes());
      appConfig.setPackageName(packageName);
      Intent intent2 = new Intent("com.ubtechinc.config.back");
      Bundle bundle2 = new Bundle();
      bundle2.putSerializable("appconfig", appConfig);
      intent2.putExtras(bundle2);
      this.mContext.sendBroadcast(intent2);
      return true;
   }

   private byte[] removeMessyCode(byte[] data) {
      byte[] byss = null;
      int length = data.length;
      String info = new String(data);
      int start = info.indexOf("{");

      if (start > 0) {
         byss = new byte[length - start];
         System.arraycopy(data, start, byss, 0, length - start);
      } else {
         byss = new byte[length];
         System.arraycopy(data, 0, byss, 0, length);
      }

      return byss;
   }

   public String readButtonEvent(String code) {
      String configName = "button.json";
      StringBuilder returnString = new StringBuilder();
      InputStream fIn = null;
      InputStreamReader isr = null;
      BufferedReader input = null;

      Object var8;
      try {
         fIn = this.mContext.getResources().getAssets().open(configName);
         isr = new InputStreamReader(fIn, code);
         input = new BufferedReader(isr);

         String line;
         while((line = input.readLine()) != null) {
            returnString.append(line);
         }

         return returnString.toString();
      } catch (Exception var18) {
         var18.getMessage();
         var8 = null;
      } finally {
         try {
            if (isr != null) {
               isr.close();
            }

            if (fIn != null) {
               fIn.close();
            }

            if (input != null) {
               input.close();
            }
         } catch (Exception var17) {
            var17.getMessage();
         }

      }

      return (String)var8;
   }

   public boolean sendButtonEvent2Server(Intent intent, String packageName, String code) {
      if (intent != null) {
         Bundle bundle = intent.getExtras();
         DeveloperAppData appData = (DeveloperAppData)bundle.getSerializable("appdata");
         byte[] data = appData.getDatas();
         String lauange = new String(data);
         Log.i("appdata", "lauange= " + lauange);
      }

      String json = this.readButtonEvent(code);
      DeveloperAppButtenEventData appEvent = new DeveloperAppButtenEventData();
      appEvent.setDatas(json.getBytes());
      appEvent.setPackageName(packageName);
      Intent intent2 = new Intent("com.ubtechinc.button.back");
      Bundle bundle2 = new Bundle();
      bundle2.putSerializable("appbutton", appEvent);
      intent2.putExtras(bundle2);
      this.mContext.sendBroadcast(intent2);
      return true;
   }

   public String parseClickEvent(Intent intent, String packageName) {
      Bundle bundle = intent.getExtras();
      DeveloperAppData appData = (DeveloperAppData)bundle.getSerializable("appclick");
      byte[] data = appData.getDatas();
      String index = new String(data);
      Log.i("appdata", "appclick= " + index);
      return index;
   }

   public UbxErrorCode.API_EEROR_CODE isChestAvailable() {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_AUTHORIZE_ERROR;
      } else if (this.mChestSerialServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else {
         return !LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID) ? UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE : nState;
      }
   }

   private void sendFreeAngle(int[] data, short time) {
      DeveloperPacketData packetData = new DeveloperPacketData(2);

      for(int i = 0; i < data.length; ++i) {
         packetData.putByte((byte)data[i]);
      }

      if (time < 20) {
         time = 20;
      }

      packetData.putShort_(time);
      this.mChestSerialServiceUtil.sendCommand((byte)52, packetData.getBuffer(), packetData.getBuffer().length);
   }

   /** @deprecated */
   @Deprecated
   public UbxErrorCode.API_EEROR_CODE head_SendFreeAngle(int[] data, short time) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      DeveloperAngle angle = new DeveloperAngle();
      angle.checkData(data);
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mChestSerialServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.sendFreeAngle(data, time);
         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE chest_SendFreeAngle(int[] data, short time) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      DeveloperAngle angle = new DeveloperAngle();
      angle.checkData(data);
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mChestSerialServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         this.sendFreeAngle(data, time);
         return nState;
      }
   }

   private void setOneFreeAngle(byte id, int angle, short time) {
      DeveloperPacketData packetData = new DeveloperPacketData(2);
      packetData.putByte(id);
      byte angleHight = (byte)(angle >> 8 & 255);
      packetData.putByte(angleHight);
      angleHight = (byte)(angle & 255);
      packetData.putByte(angleHight);
      packetData.putShort_(time);
      this.mChestSerialServiceUtil.sendCommand((byte)5, packetData.getBuffer(), packetData.getBuffer().length);
   }

   /** @deprecated */
   @Deprecated
   public UbxErrorCode.API_EEROR_CODE head_SendOneFreeAngle(byte id, int angle, short time) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      DeveloperAngle mAngle = new DeveloperAngle();
      angle = mAngle.checkAngle(id, angle);
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mChestSerialServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         if (id >= 1 && id <= 20) {
            if (time < 0) {
               time = 20;
            }

            this.setOneFreeAngle(id, angle, time);
         }

         return nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE chest_SendOneFreeAngle(byte id, int angle, short time) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      DeveloperAngle mAngle = new DeveloperAngle();
      angle = mAngle.checkAngle(id, angle);
      if (!this.isAuthorize) {
         return nState;
      } else if (this.mChestSerialServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else if (!LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID)) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE;
      } else {
         if (id >= 1 && id <= 20) {
            this.setOneFreeAngle(id, angle, time);
         }

         return nState;
      }
   }

   private void initSharedPreference() {
      Context var10003 = this.mContext;
      this.sharedPreferences = this.mContext.getSharedPreferences("ALPHA_APP_VALIDATE", 0);
      this.editor = this.sharedPreferences.edit();
   }

   public UbxErrorCode.API_EEROR_CODE isHeaderAvailable() {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_AUTHORIZE_ERROR;
      } else if (this.mHeaderSerivalServiceUtil == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else {
         return !LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, this.mAppID) ? UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE : nState;
      }
   }

   public UbxErrorCode.API_EEROR_CODE header_startEarLED(short upTime, short downTime, short runTime) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      UbxErrorCode.API_EEROR_CODE available = this.isHeaderAvailable();
      if (available != nState) {
         return available;
      } else {
         this.startEarLED(upTime, downTime, runTime);
         return nState;
      }
   }

   private void startEarLED(short upTime, short downTime, short runTime) {
      DeveloperEarLedData earLedData = new DeveloperEarLedData();
      earLedData.setmLeftLed(-1);
      earLedData.setmRightLed(-1);
      earLedData.setmBright(9);
      earLedData.setmLedUpTime(upTime);
      earLedData.setmLedDownTime(downTime);
      earLedData.setmRunTime(runTime);
      byte[] RawData = earLedData.getPlayData();
      this.mHeaderSerivalServiceUtil.sendCommand((byte)1, RawData, RawData.length);
   }

   public UbxErrorCode.API_EEROR_CODE header_stopEarLED() {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      UbxErrorCode.API_EEROR_CODE available = this.isHeaderAvailable();
      if (available != nState) {
         return available;
      } else {
         this.stopEarLED();
         return nState;
      }
   }

   private void stopEarLED() {
      byte[] param = new byte[]{1};
      this.mHeaderSerivalServiceUtil.sendCommand((byte)8, param, param.length);
   }

   public UbxErrorCode.API_EEROR_CODE header_startEyeLED(int colorType, short upTime, short downTime, short runTime) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      UbxErrorCode.API_EEROR_CODE available = this.isHeaderAvailable();
      if (available != nState) {
         return available;
      } else {
         this.startEyeLED(colorType, upTime, downTime, runTime);
         return nState;
      }
   }

   private void startEyeLED(int colorType, short upTime, short downTime, short runTime) {
      DeveloperEyesLedData eyesLedData = new DeveloperEyesLedData();
      eyesLedData.setmLeftLed((byte)-1);
      eyesLedData.setmRightLed((byte)-1);
      eyesLedData.setmBright((byte)9);
      eyesLedData.setmColor((byte)colorType);
      eyesLedData.setnLightUpTime(upTime);
      eyesLedData.setnLightDownTime(downTime);
      eyesLedData.setmRunTime(runTime);
      byte[] RawData = eyesLedData.getPlayData();
      this.mHeaderSerivalServiceUtil.sendCommand((byte)2, RawData, RawData.length);
   }

   public UbxErrorCode.API_EEROR_CODE header_stopEyeLED() {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      UbxErrorCode.API_EEROR_CODE available = this.isHeaderAvailable();
      if (available != nState) {
         return available;
      } else {
         this.stopEyeLED();
         return nState;
      }
   }

   private void stopEyeLED() {
      byte[] param = new byte[]{0};
      this.mHeaderSerivalServiceUtil.sendCommand((byte)8, param, param.length);
   }

   public void requestRobotUUID() {
      Intent intent = new Intent("com.ubtechinc.robot_uuid.request");
      this.mContext.sendBroadcast(intent);
   }

   public boolean initCustomMessageApi(IAlpha2CustomMessageListener listener) {
      if (!this.isAuthorize) {
         return false;
      } else {
         if (this.mXmppServiceUtil == null) {
            this.mXmppServiceUtil = Alpha2XmppServiceUtil.getInstance(this.mContext, this.mAppID, listener);
         }

         return true;
      }
   }

   public UbxErrorCode.API_EEROR_CODE sendCustomMessageRequest(String appID, byte[] message) {
      UbxErrorCode.API_EEROR_CODE nState = this.checkAuthorize(this.mXmppServiceUtil, this.mAppID);
      if (nState == UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED) {
         this.mXmppServiceUtil.sendCustomXmppMessage(CUSTOM_CMD, appID, new String(message));
      }

      return nState;
   }

   public UbxErrorCode.API_EEROR_CODE sendCustomMessageResp(String appID, byte[] message) {
      UbxErrorCode.API_EEROR_CODE nState = this.checkAuthorize(this.mXmppServiceUtil, this.mAppID);
      if (nState == UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED) {
         this.mXmppServiceUtil.sendCustomXmppMessage(CUSTOM_RESP, appID, new String(message));
      }

      return nState;
   }

   private UbxErrorCode.API_EEROR_CODE checkAuthorize(Object util, String appID) {
      UbxErrorCode.API_EEROR_CODE nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_SUCCEED;
      if (!this.isAuthorize) {
         return UbxErrorCode.API_EEROR_CODE.API_ERROR_AUTHORIZE_ERROR;
      } else if (util == null) {
         nState = UbxErrorCode.API_EEROR_CODE.API_ERROR_NOT_INIT;
         return nState;
      } else {
         return !LuancherAppManager.isDebug(this.mContext) && !LuancherAppManager.isLuancherAPP(this.mContext, appID) ? UbxErrorCode.API_EEROR_CODE.API_ERROR_APPID_NOT_ACTIVE : nState;
      }
   }

   private class ActionClientListener extends IAlphaActionClient.Stub {
      private ActionClientListener() {
      }

      public void onActionStop(String strActionFileName) throws RemoteException {
         if (Alpha2RobotApi.this.mActionListener != null) {
            Log.d("", "!!!!!!!!!! mClientListener 3");
            Alpha2RobotApi.this.mActionListener.onActionStop(strActionFileName);
         }

      }
   }

   /** @deprecated */
   @Deprecated
   private class EnglishOfflineUnderstand extends IAlphaEnglishOfflineUnderstandListener.Stub {
      private EnglishOfflineUnderstand() {
      }

      public void onAlpha2EnglishOfflineUnderstandResult(String arg0) throws RemoteException {
         if (Alpha2RobotApi.this.isNuanceOfflineAuthorize && Alpha2RobotApi.this.mEnglishOfflineListener != null) {
            Alpha2RobotApi.this.mEnglishOfflineListener.onAlpha2EnglishOfflineUnderStandResult(arg0);
         }

      }
   }

   /** @deprecated */
   @Deprecated
   private class EnglishUnderstand extends IAlphaEnglishUnderstandListener.Stub {
      private EnglishUnderstand() {
      }

      public void onAlpha2EnglishUnderstandResult(String arg0) throws RemoteException {
         if (Alpha2RobotApi.this.mEnglishUnderstandListener != null) {
            Alpha2RobotApi.this.mEnglishUnderstandListener.onAlpha2EnglishUnderStandResult(arg0);
         }

      }
   }

   /** @deprecated */
   @Deprecated
   private class SpeechTextUnderstand extends IAlphaTextUnderstandListener.Stub {
      private SpeechTextUnderstand() {
      }

      public void onAlpha2UnderStandError(int arg0) throws RemoteException {
         Alpha2RobotApi.this.mRobotTextListener.onAlpha2UnderStandError(arg0);
      }

      public void onAlpha2UnderStandTextResult(String arg0) throws RemoteException {
         Alpha2RobotApi.this.mRobotTextListener.onAlpha2UnderStandTextResult(arg0);
      }
   }

   private class SpeechClientImp extends IAlpha2SpeechClientListener.Stub {
      private SpeechClientImp() {
      }

      public void onServerCallBack(String text) {
         Log.i("zdy", "" + text);
         Alpha2RobotApi.this.mRobotClient.onServerCallBack(text);
      }

      public void onServerPlayEnd(boolean isEnd) {
         Alpha2RobotApi.this.mRobotClient.onServerPlayEnd(isEnd);
      }
   }
}
