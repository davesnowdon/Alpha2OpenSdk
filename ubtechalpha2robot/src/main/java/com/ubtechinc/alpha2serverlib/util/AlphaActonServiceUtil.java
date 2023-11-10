package com.ubtechinc.alpha2serverlib.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.ubtechinc.StateManager;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionClient;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionListListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService;
import com.ubtechinc.alpha2serverlib.eventdispatch.AlphaEvent;
import com.ubtechinc.alpha2serverlib.interfaces.Alpha2SerialPortOnRcvListener;
import com.ubtechinc.alpha2serverlib.interfaces.AlphaActionClientListener;
import com.ubtechinc.alpha2serverlib.interfaces.IAlpha2ActionListListener;
import java.util.ArrayList;

public class AlphaActonServiceUtil implements ServiceConnection, Alpha2SerialPortOnRcvListener {
   private static final String TAG = "AlphaActonServiceUtil";
   private Context mContext;
   private IAlpha2ActionListListener mIAlpha2ActionListListener;
   private AlphaActionClientListener mActionListener;
   private IAlphaActionClient mClientListener;
   private IAlphaActionClient.Stub mActionClient;
   private IAlphaActionService mService;
   private AlphaActonServiceUtil.DetectCompletedThread mDetectThread;
   private boolean mIsClosePowerAfterCompleted;
   private static AlphaEvent mEvent;

   public boolean ismIsClosePowerAfterCompleted() {
      return this.mIsClosePowerAfterCompleted;
   }

   public void setmIsClosePowerAfterCompleted(boolean mIsClosePowerAfterCompleted) {
      this.mIsClosePowerAfterCompleted = mIsClosePowerAfterCompleted;
      if (this.mDetectThread != null) {
         this.mDetectThread.stopThread();
         this.mDetectThread = null;
      }

      if (mIsClosePowerAfterCompleted) {
         this.mDetectThread = new AlphaActonServiceUtil.DetectCompletedThread();
         this.mDetectThread.start();
      }

   }

   public AlphaActonServiceUtil(Context context, IAlphaActionClient client) {
      this.mContext = context;
      this.mClientListener = client;
      this.mActionClient = new AlphaActonServiceUtil.AlphaActionClienImpl();
      Intent ServeiceIntent = new Intent("com.ubtechinc.services.AlphaActionServices");
      ServeiceIntent.setPackage("com.ubtechinc.alpha2services");
      this.mContext.bindService(ServeiceIntent, this, 1);
   }

   public AlphaActonServiceUtil(Context context, AlphaActionClientListener listener) {
      this.mContext = context;
      this.mActionListener = listener;
      this.mActionClient = new AlphaActonServiceUtil.AlphaActionClienImpl();
      Intent ServeiceIntent = new Intent("com.ubtechinc.services.AlphaActionServices");
      ServeiceIntent.setPackage("com.ubtechinc.alpha2services");
      this.mContext.bindService(ServeiceIntent, this, 1);
   }

   public boolean playActionName(String strActionName, AlphaEvent event) {
      mEvent = event;
      if (this.mService == null) {
         if (mEvent != null) {
            mEvent.stop();
         }

         return false;
      } else if (!StateManager.isPower(this.mContext) && !strActionName.equals("坐下")) {
         if (mEvent != null) {
            mEvent.stop();
         }

         return false;
      } else {
         this.setmIsClosePowerAfterCompleted(false);

         try {
            return this.mService.playActionName(strActionName);
         } catch (RemoteException var4) {
            var4.printStackTrace();
            return false;
         }
      }
   }

   public boolean playActionFile(String strActionFile, AlphaEvent event) {
      mEvent = event;
      if (this.mService == null) {
         if (mEvent != null) {
            mEvent.stop();
         }

         return false;
      } else if (!StateManager.isPower(this.mContext) && !strActionFile.endsWith("坐下.ubx")) {
         Log.d("", "!!! isPower");
         if (mEvent != null) {
            mEvent.stop();
         }

         return false;
      } else {
         this.setmIsClosePowerAfterCompleted(false);
         Log.d("", "!!! mService.playActionFile");

         try {
            return this.mService.playActionFile(strActionFile);
         } catch (RemoteException var4) {
            var4.printStackTrace();
            return false;
         }
      }
   }

   public boolean isCompleted() {
      if (this.mService == null) {
         return false;
      } else {
         try {
            return this.mService.isCompleted();
         } catch (RemoteException var2) {
            return false;
         }
      }
   }

   /** @deprecated */
   public void onEventHandlerTrigger(int nEventType, byte[] param) {
      if (this.mService != null) {
         try {
            this.mService.onEventHandlerTrigger(nEventType, param);
         } catch (RemoteException var4) {
            var4.printStackTrace();
         }

      }
   }

   public void stopActionPlay() {
      if (mEvent != null) {
         mEvent.stop();
      }

      if (this.mService != null) {
         try {
            this.mService.stopActionPlay();
         } catch (RemoteException var2) {
            var2.printStackTrace();
         }

      }
   }

   public void getActionList(IAlpha2ActionListListener listener) {
      this.mIAlpha2ActionListListener = listener;

      try {
         this.mService.getActionList(new AlphaActonServiceUtil.AlphaActionListListener());
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   public boolean ReleaseConnection() {
      try {
         this.mService.unRegisterActionClient(this.mActionClient);
         this.mContext.unbindService(this);
      } catch (RemoteException var2) {
         var2.printStackTrace();
      }

      return true;
   }

   /** @deprecated */
   public void onServiceConnected(ComponentName name, IBinder service) {
      Log.v("AlphaActonServiceUtil", "onServiceConnected");
      this.mService = IAlphaActionService.Stub.asInterface(service);

      try {
         this.mService.registerActionClient(this.mActionClient);
      } catch (RemoteException var4) {
         var4.printStackTrace();
      }

   }

   /** @deprecated */
   public void onServiceDisconnected(ComponentName name) {
      Log.v("AlphaActonServiceUtil", "onServiceDisconnected");

      try {
         this.mService.unRegisterActionClient(this.mActionClient);
         this.mService = null;
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   /** @deprecated */
   public void onListenSerialPortRcvData(byte[] bytes, int len) {
   }

   private class AlphaActionListListener extends IAlphaActionListListener.Stub {
      private AlphaActionListListener() {
      }

      public void onGetActionList(String list) throws RemoteException {
         String[] str = list.split("##");
         ArrayList<ArrayList<String>> actionList = null;
         if (str != null) {
            actionList = new ArrayList();
            if (str.length % 4 == 0) {
               actionList = new ArrayList();

               for(int i = 0; i < str.length / 4; ++i) {
                  ArrayList<String> action = new ArrayList();
                  int count = 4 * i;
                  action.add(str[count]);
                  action.add(str[count + 1]);
                  action.add(str[count + 2]);
                  action.add(str[count + 3]);
                  actionList.add(action);
               }
            }
         }

         AlphaActonServiceUtil.this.mIAlpha2ActionListListener.onGetActionList(actionList);
      }
   }

   /** @deprecated */
   private class AlphaActionClienImpl extends IAlphaActionClient.Stub {
      private AlphaActionClienImpl() {
      }

      public void onActionStop(String strActionFileName) throws RemoteException {
         if (AlphaActonServiceUtil.mEvent != null) {
            AlphaActonServiceUtil.mEvent.stop();
         }

         Log.d("", "!!!!!!!!!! mClientListener 1");
         if (AlphaActonServiceUtil.this.mActionListener != null) {
            AlphaActonServiceUtil.this.mActionListener.onActionStop(strActionFileName);
         }

         if (AlphaActonServiceUtil.this.mClientListener != null) {
            Log.d("", "!!!!!!!!!! mClientListener 2");
            AlphaActonServiceUtil.this.mClientListener.onActionStop(strActionFileName);
         }

      }
   }

   /** @deprecated */
   private class DetectCompletedThread extends Thread {
      private boolean mStop;

      private DetectCompletedThread() {
      }

      public void stopThread() {
         this.mStop = true;
      }

      public void run() {
         Log.v("chenlin", "DetectCompletedThread mStop = " + this.mStop + "  mIsClosePowerAfterCompleted = " + AlphaActonServiceUtil.this.mIsClosePowerAfterCompleted);

         while(!this.mStop || AlphaActonServiceUtil.this.mIsClosePowerAfterCompleted) {
            try {
               Thread.sleep(1000L);
               if (AlphaActonServiceUtil.this.isCompleted()) {
                  byte[] param = new byte[]{0};
                  Alpha2SerialServiceUtil.getCommonInstance().sendCommand((byte)25, param, param.length);
                  return;
               }
            } catch (InterruptedException var2) {
               break;
            }
         }

      }
   }
}
