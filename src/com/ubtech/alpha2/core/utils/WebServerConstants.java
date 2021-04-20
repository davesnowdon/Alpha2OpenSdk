package com.ubtech.alpha2.core.utils;

import android.os.Environment;

public class WebServerConstants {
   public static final String APKDOWNLOADPATH = "http://192.168.213.94:8080/ssh/";
   public static final String LOCAL_APK_PATH = Environment.getExternalStorageDirectory() + "/ubtech/alpha2";
   public static final String APKCONFIGACTION_CHECKMAXVERSION = "http://192.168.213.94:8080/ssh/apkConfigAction!checkMaxVersion.action";
   public static final String HARDWARD_VERSION_URL = "http://www.ubtrobot.com/tools/alpha1robot/alphabinversion.xml";
   public static final String ALPHA1S_APK_VERSION_URL = "http://www.ubtrobot.com/tools/alpha1robot/android/version.xml";
   public static final String BASE_ACTION_URL = "http://192.168.137.1:8081/file/baseaction.zip";
   public static final String BASE_ACTION_VERSION_URL = "http://192.168.137.1:8081/file/base_action_version.json";
   public static final String ENGINE_VERSION_URL = "http://192.168.137.1:8081/file/alpha_endine_version.json";
   public static final String ALPHA1S_APK_VERSION_NAME = "version.xml";
   public static final String ALPHA1S_APK_NAME = "Alpha1Blooth.apk";
   public static final String BASE_ACTION_FILE = "baseaction.zip";
   public static final String BASE_ACTION_VERSION_FILE = "base_action_version.json";
   public static final String ENGINE_VERSION_FILE = "alpha_endine_version.json";
   public static final String ALPHA1S_DEVICE_VERSION_URL = "";
   public static final String ALPHA1S_BIN_VERSION_NAME = "alphabinversion.xml";
   public static final String ALPHA1S_BIN_FILE_NAME = "alpha.bin";
   public static final String SD_PATH = Environment.getExternalStorageDirectory() + "";

   public WebServerConstants() {
   }

   public static String getSavePath(WebServerConstants.Product type) {
      String path = SD_PATH;
      switch(type) {
      case alpha_hard:
         path = path + "/ahpa1/hardversion";
         break;
      case alpha1s_ctrl:
         path = path + "/ahpa1/apk/version";
         break;
      case alpha1s_bin:
         path = path + "/ahpa1/bin/version";
         break;
      case alpha_engine:
         path = path + "/ahpa1/engine/version";
         break;
      case alpha_action:
         path = path + "/ahpa1/baseaction";
      }

      return path;
   }

   public static enum Product {
      alpha1s_ctrl,
      alpha2s_service,
      alpha2s_speech,
      alpha2s_ctrl,
      alpha1s_bin,
      alpha_hard,
      alpha_action,
      alpha_engine;

      private Product() {
      }
   }
}
