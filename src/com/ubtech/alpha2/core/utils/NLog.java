package com.ubtech.alpha2.core.utils;

import android.util.Log;

public class NLog {
   private static final String LOG_FORMAT = "%1$s\n%2$s";
   public static boolean isDebug = true;

   public NLog() {
   }

   public static void d(String tag, Object... args) {
      log(3, (Throwable)null, tag, args);
   }

   public static void i(String tag, Object... args) {
      log(4, (Throwable)null, tag, args);
   }

   public static void w(String tag, Object... args) {
      log(5, (Throwable)null, tag, args);
   }

   public static void e(Throwable ex) {
      log(6, ex, (String)null);
   }

   public static void e(String tag, Object... args) {
      log(6, (Throwable)null, tag, args);
   }

   public static void e(Throwable ex, String tag, Object... args) {
      log(6, ex, tag, args);
   }

   private static void log(int priority, Throwable ex, String tag, Object... args) {
      if (isDebug) {
         String log = "";
         if (ex == null) {
            if (args != null && args.length > 0) {
               Object[] var5 = args;
               int var6 = args.length;

               for(int var7 = 0; var7 < var6; ++var7) {
                  Object obj = var5[var7];
                  log = log + String.valueOf(obj);
               }
            }
         } else {
            String logMessage = ex.getMessage();
            String logBody = Log.getStackTraceString(ex);
            log = String.format("%1$s\n%2$s", logMessage, logBody);
         }

         Log.println(priority, tag, log);
      }
   }

   public static boolean isDebug() {
      return isDebug;
   }

   public static void setDebug(boolean isDebug) {
      NLog.isDebug = isDebug;
   }
}
