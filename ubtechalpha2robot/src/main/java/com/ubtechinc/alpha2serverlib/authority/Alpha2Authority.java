package com.ubtechinc.alpha2serverlib.authority;

import android.content.Context;

public class Alpha2Authority {
   public static Context mContext;
   public static String mAppid;

   public Alpha2Authority() {
   }

   public static void createAuthority(Context context, String appid) {
      mContext = context;
      mAppid = appid;
   }
}
