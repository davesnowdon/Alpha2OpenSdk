package com.ubtechinc.alpha2serverlib.util;

import android.content.Context;
import android.content.Intent;

public class Alpha2SocketServiceUtil {
   Context mContext;
   Intent mServiceIntent;

   public Alpha2SocketServiceUtil(Context context) {
      this.mContext = context;
   }

   public void startService() {
      this.mServiceIntent = new Intent("com.ubtechinc.services.Alpha2SocketServices");
      this.mContext.startService(this.mServiceIntent);
   }

   public void stopService() {
      this.mContext.stopService(this.mServiceIntent);
   }
}
