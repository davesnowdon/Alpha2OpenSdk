package com.ubtechinc.alpha2serverlib.util;

import android.content.Context;
import android.content.Intent;

public class AlphaMainServiceUtil {
   private static String version = "2.0.0.1";
   private Context mContext;

   public static String getVersion() {
      return version;
   }

   public AlphaMainServiceUtil(Context context) {
      this.mContext = context;
   }

   public void startService() {
      Intent intent = new Intent("com.ubtechinc.services.MainService");
      this.mContext.startService(intent);
   }

   public void stopService() {
      Intent intent = new Intent("com.ubtechinc.services.MainService");
      this.mContext.stopService(intent);
   }
}
