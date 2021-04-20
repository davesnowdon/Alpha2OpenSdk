package com.ubtechinc.alpha2serverlib.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2BlueToothSerialPortService;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient;
import com.ubtechinc.alpha2serverlib.interfaces.Alpha2SerialPortOnRcvListener;

public class Alpha2BlueToothSerialServiceUtil implements ServiceConnection {
   private static final String TAG = "Alpha2BlueToothSerialServiceUtil";
   private Context mContext;
   private IAlpha2SerialPortRcvClient.Stub mRcvListener;
   private Intent mCallIntent;
   private Alpha2SerialPortOnRcvListener mCientListener;
   private byte mSessionID;
   private static Alpha2BlueToothSerialServiceUtil mCommonInstance;
   private IAlpha2BlueToothSerialPortService mService;

   public static Alpha2BlueToothSerialServiceUtil getCommonInstance() {
      return mCommonInstance;
   }

   public static void initCommonInstance(Context context, Alpha2SerialPortOnRcvListener listener) {
      mCommonInstance = new Alpha2BlueToothSerialServiceUtil(context, listener);
   }

   public static void releaseCommonInstance() {
      if (mCommonInstance != null) {
         mCommonInstance.ReleaseConnection();
         mCommonInstance = null;
      }

   }

   public Alpha2BlueToothSerialServiceUtil(Context context, Alpha2SerialPortOnRcvListener listener) {
      this.mContext = context;
      this.mCientListener = listener;
      this.mCallIntent = new Intent("com.ubtechinc.services.AlphaBlueToothSerialPortServices");
      this.mRcvListener = new Alpha2BlueToothSerialServiceUtil.Alpha2SerialPortRcvClientImpl();
      this.mCallIntent.setPackage("com.ubtechinc.alpha2services");
      this.mContext.bindService(this.mCallIntent, this, 1);
   }

   public boolean sendCommand(byte nCmd, byte[] nParam, int nLen) {
      try {
         return this.mService.sendCommand(this.mSessionID, nCmd, nParam, nLen);
      } catch (RemoteException var5) {
         var5.printStackTrace();
         return true;
      }
   }

   public void sendATCMD(String cmd) {
      try {
         this.mService.sendATCMD(cmd);
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   public boolean ReleaseConnection() {
      try {
         if (this.mService != null) {
            this.mService.unRegisterSerialPortRcvListener(this.mRcvListener);
            this.mContext.unbindService(this);
         }
      } catch (RemoteException var2) {
         var2.printStackTrace();
      }

      return true;
   }

   public void onServiceConnected(ComponentName name, IBinder service) {
      Log.v("Alpha2BlueToothSerialServiceUtil", "onServiceConnected");
      this.mService = IAlpha2BlueToothSerialPortService.Stub.asInterface(service);

      try {
         this.mSessionID = (byte)this.mService.registerSerialPortRcvListener(this.mRcvListener);
      } catch (RemoteException var4) {
         var4.printStackTrace();
      }

   }

   public void onServiceDisconnected(ComponentName name) {
      Log.v("Alpha2BlueToothSerialServiceUtil", "onServiceDisconnected");

      try {
         this.mService.unRegisterSerialPortRcvListener(this.mRcvListener);
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   private class Alpha2SerialPortRcvClientImpl extends IAlpha2SerialPortRcvClient.Stub {
      private Alpha2SerialPortRcvClientImpl() {
      }

      public void onListenSerialPortRcvData(byte[] bytes, int len) throws RemoteException {
         if (Alpha2BlueToothSerialServiceUtil.this.mCientListener != null) {
            Alpha2BlueToothSerialServiceUtil.this.mCientListener.onListenSerialPortRcvData(bytes, len);
         }

      }
   }
}
