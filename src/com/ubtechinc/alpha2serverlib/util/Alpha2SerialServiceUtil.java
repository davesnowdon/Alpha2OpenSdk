package com.ubtechinc.alpha2serverlib.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService;
import com.ubtechinc.alpha2serverlib.interfaces.Alpha2SerialPortOnRcvListener;

public class Alpha2SerialServiceUtil implements ServiceConnection {
   private static final String TAG = "Alpha2SerialServiceUtil";
   private Context mContext;
   private IAlpha2SerialPortRcvClient.Stub mRcvListener;
   private Intent mCallIntent;
   private Alpha2SerialPortOnRcvListener mCientListener;
   private byte mSessionID;
   private IAlpha2SerialPortService mService;
   static Alpha2SerialServiceUtil mCommonInstance;

   public static void initCommonInstance(Context context, Alpha2SerialPortOnRcvListener listener) {
      mCommonInstance = new Alpha2SerialServiceUtil(context, listener);
   }

   public static void releaseCommonInstance() {
      if (mCommonInstance != null) {
         mCommonInstance.ReleaseConnection();
         mCommonInstance = null;
      }

   }

   public static Alpha2SerialServiceUtil getCommonInstance() {
      return mCommonInstance;
   }

   public Alpha2SerialServiceUtil(Context context, Alpha2SerialPortOnRcvListener listener) {
      this.mContext = context;
      this.mCientListener = listener;
      this.mCallIntent = new Intent("com.ubtechinc.services.AlphaSerialPortServices");
      this.mCallIntent.setPackage("com.ubtechinc.alpha2services");
      this.mRcvListener = new Alpha2SerialServiceUtil.Alpha2SerialPortRcvClientImpl();
      this.mContext.bindService(this.mCallIntent, this, 1);
      this.waitForInitComplete();
   }

   public boolean isInitCompleted() {
      return this.mService != null;
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

   public boolean sendCommand(byte nCmd, byte[] nParam, int nLen) {
      try {
         return this.mService.sendCommand(this.mSessionID, nCmd, nParam, nLen);
      } catch (RemoteException var5) {
         var5.printStackTrace();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return true;
   }

   /** @deprecated */
   public boolean sendRawData(byte[] datas, int nLen) {
      try {
         return this.mService.sendRawData(datas, nLen);
      } catch (RemoteException var4) {
         var4.printStackTrace();
         return true;
      }
   }

   public boolean ReleaseConnection() {
      try {
         this.mService.unRegisterSerialPortRcvListener(this.mRcvListener);
         this.mContext.unbindService(this);
      } catch (RemoteException var2) {
         var2.printStackTrace();
      }

      return true;
   }

   /** @deprecated */
   public void onServiceConnected(ComponentName name, IBinder service) {
      Log.v("Alpha2SerialServiceUtil", "onServiceConnected");
      this.mService = IAlpha2SerialPortService.Stub.asInterface(service);

      try {
         this.mSessionID = (byte)this.mService.registerSerialPortRcvListener(this.mRcvListener);
      } catch (RemoteException var4) {
         var4.printStackTrace();
      }

   }

   /** @deprecated */
   public void onServiceDisconnected(ComponentName name) {
      Log.v("Alpha2SerialServiceUtil", "onServiceDisconnected");

      try {
         this.mService.unRegisterSerialPortRcvListener(this.mRcvListener);
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   /** @deprecated */
   private class Alpha2SerialPortRcvClientImpl extends IAlpha2SerialPortRcvClient.Stub {
      private Alpha2SerialPortRcvClientImpl() {
      }

      public void onListenSerialPortRcvData(byte[] bytes, int len) throws RemoteException {
         if (Alpha2SerialServiceUtil.this.mCientListener != null) {
            Alpha2SerialServiceUtil.this.mCientListener.onListenSerialPortRcvData(bytes, len);
         }

      }
   }
}
