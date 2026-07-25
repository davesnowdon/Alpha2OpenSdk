package com.ubtechinc.alpha2serverlib.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService;
import com.ubtechinc.alpha2serverlib.interfaces.Alpha2SerialPortHeaderOnRcvListener;

/**
 * Binds the robot's head serial-port service and provides {@code sendCommand} plus a
 * receive callback for frames coming back from the head microcontroller.
 */
public class Alpha2SerialHeaderServiceUtil implements ServiceConnection {
   private static final String TAG = "Alpha2SerialHeaderServiceUtil";
   private static final String ACTION = "com.ubtechinc.services.AlphaSerialPortHeaderServices";
   private static final String SERVICE_PACKAGE = "com.ubtechinc.alpha2services";
   private static final int WAIT_TICKS = 300;
   private static final long WAIT_TICK_MS = 10L;

   private final Context mContext;
   private final Alpha2SerialPortHeaderOnRcvListener mClientListener;
   private final IAlpha2SerialPortRcvClient.Stub mRcvListener;
   private IAlpha2SerialPortService mService;
   private byte mSessionID;
   private boolean mBound;

   public Alpha2SerialHeaderServiceUtil(Context context, Alpha2SerialPortHeaderOnRcvListener listener) {
      this.mContext = context;
      this.mClientListener = listener;
      this.mRcvListener = new SerialPortRcvClientImpl();
      Intent intent = new Intent(ACTION);
      intent.setPackage(SERVICE_PACKAGE);
      // Binding is asynchronous and intentionally NOT awaited here (see
      // Alpha2SerialServiceUtil): blocking the main-thread caller would deadlock the
      // ServiceConnection callback. Callers check readiness with isInitCompleted().
      this.mBound = this.mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
   }

   public boolean isInitCompleted() {
      return this.mService != null;
   }

   /**
    * Optionally block (up to ~3s) for the async bind to complete. Returns immediately on
    * the main thread (the {@link ServiceConnection} callback is delivered there, so
    * spinning would block the bind it waits for).
    */
   public void waitForInitComplete() {
      if (Looper.myLooper() == Looper.getMainLooper()) {
         return;
      }
      for (int ticks = WAIT_TICKS; ticks > 0 && !this.isInitCompleted(); --ticks) {
         SystemClock.sleep(WAIT_TICK_MS);
      }
   }

   public boolean sendCommand(byte nCmd, byte[] nParam, int nLen) {
      if (this.mService == null) {
         return false;
      }
      try {
         return this.mService.sendCommand(this.mSessionID, nCmd, nParam, nLen);
      } catch (RemoteException | RuntimeException e) {
         e.printStackTrace();
         return false;
      }
   }

   public boolean ReleaseConnection() {
      if (this.mService != null) {
         try {
            this.mService.unRegisterSerialPortRcvListener(this.mRcvListener);
         } catch (RemoteException e) {
            e.printStackTrace();
         }
      }
      if (this.mBound) {
         this.mBound = false;
         try {
            this.mContext.unbindService(this);
         } catch (IllegalArgumentException e) {
            e.printStackTrace();
         }
      }
      this.mService = null;
      return true;
   }

   @Override
   public void onServiceConnected(ComponentName name, IBinder service) {
      Log.v(TAG, "onServiceConnected");
      this.mService = IAlpha2SerialPortService.Stub.asInterface(service);
      try {
         this.mSessionID = (byte) this.mService.registerSerialPortRcvListener(this.mRcvListener);
      } catch (RemoteException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void onServiceDisconnected(ComponentName name) {
      Log.v(TAG, "onServiceDisconnected");
      try {
         if (this.mService != null) {
            this.mService.unRegisterSerialPortRcvListener(this.mRcvListener);
         }
      } catch (RemoteException e) {
         e.printStackTrace();
      } finally {
         this.mService = null;
      }
   }

   private final class SerialPortRcvClientImpl extends IAlpha2SerialPortRcvClient.Stub {
      @Override
      public void onListenSerialPortRcvData(byte[] bytes, int len) throws RemoteException {
         if (Alpha2SerialHeaderServiceUtil.this.mClientListener != null) {
            Alpha2SerialHeaderServiceUtil.this.mClientListener.onListenSerialPortHeaderRcvData(bytes, len);
         }
      }
   }
}
