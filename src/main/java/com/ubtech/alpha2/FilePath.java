package com.ubtech.alpha2;

import android.os.Environment;

public class FilePath {
   private static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
   public static String appPath;
   public static String actionsPath;
   public static String versionPath;

   public FilePath() {
   }

   static {
      appPath = sdPath + "/ubtech/alpha2s/app";
      actionsPath = sdPath + "/actions";
      versionPath = sdPath + "/ubtech/alpha2s/version.config";
   }
}
