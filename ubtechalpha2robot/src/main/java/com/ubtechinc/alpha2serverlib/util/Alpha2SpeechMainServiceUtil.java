package com.ubtechinc.alpha2serverlib.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaEnglishOfflineUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaEnglishUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface;
import com.ubtechinc.alpha2serverlib.eventdispatch.AlphaEvent;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2SpeechGrammarListener;
import com.ubtechinc.contant.CustomLanguage;
import java.util.Locale;

public class Alpha2SpeechMainServiceUtil implements ServiceConnection {
   private static CustomLanguage defaultLanguage = null;
   private static boolean isFirst = true;
   private Context mContext;
   private String Alpha2SpeechMainServiceIntent = "";
   private IAlphaTextUnderstandListener mTextUnderstanderListener;
   private IAlphaEnglishUnderstandListener mEnglishUnderstanderListener;
   private IAlphaEnglishOfflineUnderstandListener mEnglishOfflineUnderstandListener;
   private AlphaEvent mEvent;
   private IAlpha2SpeechGrammarListener mGrammarListener;
   private boolean sForbidTTS = false;
   private static ISpeechInterface mService;
   private static ISpeechCallBackListener.Stub mListener;
   private byte mSessionID;
   private IAlpha2SpeechClientListener mCientListener;
   private Alpha2SpeechMainServiceUtil.ISpeechInitInterface mSpeechInitListener;

   /** @deprecated */
   public void setAlpha2SpeechMainServiceIntent() {
      this.setAlpha2SpeechMainServiceIntent(defaultLanguage);
   }

   public void setAlpha2SpeechMainServiceIntent(CustomLanguage specifyLanguage) {
      this.Alpha2SpeechMainServiceIntent = "com.ubtechinc.services.SpeechServices";
      Log.d("", "!!! SpeechServiceIntent=" + this.Alpha2SpeechMainServiceIntent);
   }

   private String getSystemLanguage() {
      Locale locale = this.mContext.getResources().getConfiguration().locale;
      String language = locale.getLanguage();
      return language;
   }

   /** @deprecated */
   public Alpha2SpeechMainServiceUtil(Context context) {
      this.mContext = context;
      this.setAlpha2SpeechMainServiceIntent();
   }

   /** @deprecated */
   public void startService() {
      Intent intent = new Intent(this.Alpha2SpeechMainServiceIntent);
      this.mContext.startService(intent);
   }

   /** @deprecated */
   public void stopService() {
      Intent intent = new Intent(this.Alpha2SpeechMainServiceIntent);
      this.mContext.stopService(intent);
   }

   public Alpha2SpeechMainServiceUtil.ISpeechInitInterface getmSpeechInitListener() {
      return this.mSpeechInitListener;
   }

   public void setmSpeechInitListener(Alpha2SpeechMainServiceUtil.ISpeechInitInterface mSpeechInitListener) {
      this.mSpeechInitListener = mSpeechInitListener;
   }

   public Alpha2SpeechMainServiceUtil(Context context, IAlpha2SpeechClientListener mCientListener) {
      this.mContext = context;
      this.setAlpha2SpeechMainServiceIntent();
      this.mCientListener = mCientListener;
      if (mListener != null) {
         try {
            mListener.onPlayEnd(true);
         } catch (RemoteException var4) {
            var4.printStackTrace();
         }

         this.onStopPlay();
      }

      mListener = new Alpha2SpeechMainServiceUtil.SpeechCallBackListenerImp();
      Intent service = new Intent(this.Alpha2SpeechMainServiceIntent);
      service.setPackage("com.ubtechinc.alpha2services");
      context.bindService(service, this, 1);
      Log.i("SpeechConnecton", "SpeechConnecton bindService ");
   }

   public Alpha2SpeechMainServiceUtil(Context context, IAlpha2SpeechClientListener mCientListener, Alpha2SpeechMainServiceUtil.ISpeechInitInterface mSpeechInitListener) {
      this.mContext = context;
      this.setAlpha2SpeechMainServiceIntent();
      this.mCientListener = mCientListener;
      this.mSpeechInitListener = mSpeechInitListener;
      if (mListener != null) {
         try {
            mListener.onPlayEnd(true);
         } catch (RemoteException var5) {
            var5.printStackTrace();
         }

         this.onStopPlay();
      }

      mListener = new Alpha2SpeechMainServiceUtil.SpeechCallBackListenerImp();
      Intent service = new Intent(this.Alpha2SpeechMainServiceIntent);
      service.setPackage("com.ubtechinc.alpha2services");
      context.bindService(service, this, 1);
   }

   public Alpha2SpeechMainServiceUtil(Context context, IAlpha2SpeechClientListener mCientListener, CustomLanguage specifyLanguage) {
      this.mContext = context;
      this.setAlpha2SpeechMainServiceIntent(specifyLanguage);
      this.mCientListener = mCientListener;
      if (mListener != null) {
         try {
            mListener.onPlayEnd(true);
         } catch (RemoteException var6) {
            var6.printStackTrace();
         }

         this.onStopPlay();
      }

      mListener = new Alpha2SpeechMainServiceUtil.SpeechCallBackListenerImp();
      Intent service = new Intent(this.Alpha2SpeechMainServiceIntent);
      service.setPackage("com.ubtechinc.alpha2services");
      context.bindService(service, this, 1);
      Log.i("SpeechConnecton", "SpeechConnecton bindService ");
   }

   public Alpha2SpeechMainServiceUtil(Context context, IAlpha2SpeechClientListener mCientListener, Alpha2SpeechMainServiceUtil.ISpeechInitInterface mSpeechInitListener, CustomLanguage specifyLanguage) {
      this.mContext = context;
      this.setAlpha2SpeechMainServiceIntent(specifyLanguage);
      this.mCientListener = mCientListener;
      this.mSpeechInitListener = mSpeechInitListener;
      if (mListener != null) {
         try {
            mListener.onPlayEnd(true);
         } catch (RemoteException var6) {
            var6.printStackTrace();
         }

         this.onStopPlay();
      }

      mListener = new Alpha2SpeechMainServiceUtil.SpeechCallBackListenerImp();
      Intent service = new Intent(this.Alpha2SpeechMainServiceIntent);
      service.setPackage("com.ubtechinc.alpha2services");
      context.bindService(service, this, 1);
   }

   public boolean isInitCompleted() {
      return mService != null;
   }

   /** @deprecated */
   public void waitForInitComplete() {
      for(int nTimes = 300; nTimes > 0; --nTimes) {
         SystemClock.sleep(10L);
         if (this.isInitCompleted()) {
            break;
         }
      }

   }

   /** @deprecated */
   public void onServiceConnected(ComponentName arg0, IBinder service) {
      mService = ISpeechInterface.Stub.asInterface(service);
      Log.i("SpeechConnecton", "!!! binder <-- " + mService + " -->");
      if (this.mSpeechInitListener != null) {
         this.mSpeechInitListener.initOver();
      }

      try {
         this.mSessionID = (byte)mService.registerSpeechCallBackListener(mListener);
      } catch (RemoteException var4) {
         var4.printStackTrace();
      }

   }

   /** @deprecated */
   public void onServiceDisconnected(ComponentName arg0) {
      try {
         mService.unRegisterSpeechCallBackListener(mListener);
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   public boolean ReleaseConnection() {
      try {
         if (mService != null) {
            mService.unRegisterSpeechCallBackListener(mListener);
         }
      } catch (RemoteException var2) {
         var2.printStackTrace();
      }

      this.mContext.unbindService(this);
      return true;
   }

   public void setRecognizedLanguage(String strLanguage) {
      if (mService != null) {
         try {
            this.mGrammarListener = null;
            mService.setRecognizedLanguage(strLanguage);
         } catch (RemoteException var3) {
            var3.printStackTrace();
         }
      }

   }

   public void onSpeech(String text) {
      try {
         mService.onSpeech(mListener, text);
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   public void setVoiceName(String strVoiceName) {
      try {
         mService.setVoiceName(strVoiceName);
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   public void onPlay(String text, String strVoiceName) {
      this.onPlay(text, strVoiceName, (String)null, true);
   }

   public void onPlay(String text, boolean isTip) {
      this.onPlay(text, (String)null, (String)null, isTip);
   }

   public void onPlay(String text, boolean isTip, AlphaEvent event) {
      try {
         if (!this.sForbidTTS) {
            this.mEvent = event;
            if (isTip) {
               mService.onPlay(mListener, text, (String)null, (String)null);
            } else {
               mService.onPlayHigh(mListener, text, (String)null, (String)null);
            }
         } else {
            this.mEvent.stop();
         }
      } catch (RemoteException var5) {
         var5.printStackTrace();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public void onPlay(String text, String voiceName, String language, boolean isTip) {
      try {
         if (isTip) {
            mService.onPlay(mListener, text, voiceName, language);
         } else {
            mService.onPlayHigh(mListener, text, voiceName, language);
         }
      } catch (RemoteException var6) {
         var6.printStackTrace();
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   public void onStopPlay() {
      try {
         mService.onStopPlay(mListener);
      } catch (RemoteException var2) {
         var2.printStackTrace();
      }

   }

   public void onStopSpeech() {
      try {
         mService.onStopSpeech(mListener);
      } catch (RemoteException var2) {
         var2.printStackTrace();
      }

   }

   public void stopSpeechAndEnterIdleMode() {
      if (mService != null) {
         try {
            mService.stopSpeechAndEnterIdleMode();
         } catch (RemoteException var2) {
            var2.printStackTrace();
         }
      }

   }

   public boolean isInit() {
      return mService != null;
   }

   public void setWakeState(boolean onWake) {
      try {
         mService.setWakeState(onWake);
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   /** @deprecated */
   public void textUnderstand(String strTextToUnderstand, IAlphaTextUnderstandListener textUnderstandListener) {
      this.mTextUnderstanderListener = textUnderstandListener;

      try {
         mService.onTextUnderstand(strTextToUnderstand, new IAlphaTextUnderstandListener.Stub() {
            public void onAlpha2UnderStandError(int nErrorCode) throws RemoteException {
               if (Alpha2SpeechMainServiceUtil.this.mTextUnderstanderListener != null) {
                  Alpha2SpeechMainServiceUtil.this.mTextUnderstanderListener.onAlpha2UnderStandError(nErrorCode);
               }

            }

            public void onAlpha2UnderStandTextResult(String strResult) throws RemoteException {
               if (Alpha2SpeechMainServiceUtil.this.mTextUnderstanderListener != null) {
                  Alpha2SpeechMainServiceUtil.this.mTextUnderstanderListener.onAlpha2UnderStandTextResult(strResult);
               }

            }
         });
      } catch (RemoteException var4) {
         var4.printStackTrace();
      }

   }

   /** @deprecated */
   public void englishUnderstand(IAlphaEnglishUnderstandListener englishUnderstandListener, IAlphaEnglishOfflineUnderstandListener englishOfflineListener) {
      this.mEnglishUnderstanderListener = englishUnderstandListener;
      this.mEnglishOfflineUnderstandListener = englishOfflineListener;

      try {
         mService.setEnglishOfflineListener(new IAlphaEnglishOfflineUnderstandListener.Stub() {
            public void onAlpha2EnglishOfflineUnderstandResult(String strResult) throws RemoteException {
               if (Alpha2SpeechMainServiceUtil.this.mEnglishOfflineUnderstandListener != null) {
                  Alpha2SpeechMainServiceUtil.this.mEnglishOfflineUnderstandListener.onAlpha2EnglishOfflineUnderstandResult(strResult);
               }

            }
         });
         mService.onEnglishUnderstand(new IAlphaEnglishUnderstandListener.Stub() {
            public void onAlpha2EnglishUnderstandResult(String strResult) throws RemoteException {
               if (Alpha2SpeechMainServiceUtil.this.mEnglishUnderstanderListener != null) {
                  Alpha2SpeechMainServiceUtil.this.mEnglishUnderstanderListener.onAlpha2EnglishUnderstandResult(strResult);
               }

            }
         });
      } catch (RemoteException var4) {
         var4.printStackTrace();
      }

   }

   public void initSpeechGrammar(String strGrammar, ISpeechGrammarInitListener listener) {
      try {
         mService.initSpeechGrammar(strGrammar, listener);
      } catch (RemoteException var4) {
         var4.printStackTrace();
      }

   }

   public void startSpeechGrammar(IAlpha2SpeechGrammarListener listener) {
      try {
         this.mGrammarListener = listener;
         mService.startSpeechGrammar((ISpeechGrammarListener)null);
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   public void stopSpeechGrammar() {
      try {
         this.mGrammarListener = null;
         mService.stopSpeechGrammar();
      } catch (RemoteException var2) {
         var2.printStackTrace();
      }

   }

   public void setSelfInterrupt(boolean isInterrupt) {
      try {
         mService.setSelfInterrupt(isInterrupt);
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   public boolean issForbidTTS() {
      return this.sForbidTTS;
   }

   public void setsForbidTTS(boolean sForbidTTS) {
      this.sForbidTTS = sForbidTTS;
   }

   public interface ISpeechInitInterface {
      void initOver();
   }

   public class SpeechCallBackListenerImp extends ISpeechCallBackListener.Stub {
      public SpeechCallBackListenerImp() {
      }

      public void onCallBack(int type, String text) throws RemoteException {
         if (Alpha2SpeechMainServiceUtil.this.mGrammarListener != null) {
            Alpha2SpeechMainServiceUtil.this.mGrammarListener.onSpeechGrammarResult(type, text);
         } else {
            Alpha2SpeechMainServiceUtil.this.mCientListener.onServerCallBack(text);
         }

      }

      public void onPlayEnd(boolean isEnd) throws RemoteException {
         if (Alpha2SpeechMainServiceUtil.this.mEvent != null) {
            Alpha2SpeechMainServiceUtil.this.mEvent.stop();
         }

         Alpha2SpeechMainServiceUtil.this.mCientListener.onServerPlayEnd(isEnd);
      }
   }
}
