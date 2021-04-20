package com.ubtechinc.alpha2serverlib.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppCallBack;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2CustomMessageListener;

public class Alpha2XmppServiceUtil implements ServiceConnection {
   private Context mContext;
   private String mAppId;
   private IAlpha2XmppListener mService;
   private IAlpha2XmppCallBack.Stub mListener;
   private IAlpha2CustomMessageListener mCustomMessageListener;
   private static Alpha2XmppServiceUtil mAlpha2XmppServiceUtil;

   public void onServiceConnected(ComponentName name, IBinder service) {
      this.mService = IAlpha2XmppListener.Stub.asInterface(service);

      try {
         this.mService.registerXmppCallBackListener(this.mAppId, this.mListener);
      } catch (RemoteException var4) {
         var4.printStackTrace();
      }

   }

   public void onServiceDisconnected(ComponentName name) {
      if (this.mService != null) {
         try {
            this.mService.unRegisterXmppCallBackListener(this.mListener);
         } catch (RemoteException var3) {
            var3.printStackTrace();
         }
      }

   }

   public static Alpha2XmppServiceUtil getInstance(Context context, String appID, IAlpha2CustomMessageListener mCientListener) {
      if (mAlpha2XmppServiceUtil == null) {
         Class var3 = Alpha2XmppServiceUtil.class;
         synchronized(Alpha2XmppServiceUtil.class) {
            if (mAlpha2XmppServiceUtil == null) {
               mAlpha2XmppServiceUtil = new Alpha2XmppServiceUtil(context, appID, mCientListener);
            }
         }
      }

      return mAlpha2XmppServiceUtil;
   }

   private Alpha2XmppServiceUtil(Context context, String appID, IAlpha2CustomMessageListener mCientListener) {
      this.mContext = context;
      this.mAppId = appID;
      this.mCustomMessageListener = mCientListener;
      this.mListener = new Alpha2XmppServiceUtil.ISpeechCallBackListenerImpl();
      Intent service = new Intent("com.ubtechinc.services.Alpha2XmppServices");
      service.setPackage("com.ubtechinc.alpha2services");
      context.bindService(service, this, 1);
      Log.i("XmppServiceImpl", "SpeechConnecton bindService ALPHA_XMPP_SERVER");
   }

   public void sendCustomXmppMessage(int type, String appId, String message) {
      if (this.mService != null) {
         try {
            Log.i("XmppServiceImpl", "sendCustomXmppMessage :" + message);
            this.mService.sendCustomXmppMessage(type, appId, message);
         } catch (RemoteException var5) {
            var5.printStackTrace();
         }
      }

   }

   public boolean ReleaseConnection() {
      try {
         if (this.mService != null) {
            this.mService.unRegisterXmppCallBackListener(this.mListener);
         }
      } catch (RemoteException var2) {
         var2.printStackTrace();
      }

      this.mContext.unbindService(this);
      return true;
   }

   private class ISpeechCallBackListenerImpl extends IAlpha2XmppCallBack.Stub {
      private ISpeechCallBackListenerImpl() {
      }

      public void onReceiveMessage(String message) throws RemoteException {
         Log.i("XmppServiceImpl", message);
         if (Alpha2XmppServiceUtil.this.mCustomMessageListener != null) {
            Alpha2XmppServiceUtil.this.mCustomMessageListener.onReceiveMessage(message.getBytes());
         }

      }
   }
}
