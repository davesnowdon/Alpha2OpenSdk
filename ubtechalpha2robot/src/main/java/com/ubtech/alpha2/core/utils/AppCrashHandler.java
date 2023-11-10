package com.ubtech.alpha2.core.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class AppCrashHandler implements UncaughtExceptionHandler {
   private final String tag = AppCrashHandler.class.getSimpleName();
   private Context mContext;
   private static AppCrashHandler instance;
   private UncaughtExceptionHandler mDefaultHandler;
   private Properties crashReport = new Properties();
   private final String TRACE = "trace";
   private final String EXCEPTION = "exception";
   private final String VERSIONNAME = "versionName";
   private final String VERSIONCODE = "versionCode";
   private final String PREFIX = "crash_";
   private final String PATTERN = "yyyy-MM-dd hh:mm:ss";
   private final String SUFFIX = ".cr";

   public AppCrashHandler() {
   }

   public static AppCrashHandler getInstance() {
      if (instance == null) {
         instance = new AppCrashHandler();
      }

      return instance;
   }

   public void init(Context context) {
      this.mContext = context;
      this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
      Thread.setDefaultUncaughtExceptionHandler(this);
   }

   public void uncaughtException(Thread thread, Throwable ex) {
      if (!this.handlerException(ex) && this.mDefaultHandler != null) {
         this.mDefaultHandler.uncaughtException(thread, ex);
      } else {
         try {
            Thread.sleep(5000L);
         } catch (InterruptedException var4) {
            var4.printStackTrace();
         }
      }

   }

   private boolean handlerException(Throwable ex) {
      if (ex == null) {
         return true;
      } else {
         String msg = ex.getLocalizedMessage();
         if (TextUtils.isEmpty(msg)) {
            return false;
         } else {
            (new Thread() {
               public void run() {
                  Looper.prepare();
                  NLog.e(AppCrashHandler.this.tag, "test exit!");
                  Looper.loop();
               }
            }).start();
            this.conllectCrashDeviceInfo(this.mContext);
            this.saveCrashInfo(ex);
            this.sendCrashReport();
            return true;
         }
      }
   }

   private void conllectCrashDeviceInfo(Context context) {
      try {
         PackageManager pm = context.getPackageManager();
         PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 1);
         if (pi != null) {
            this.crashReport.put("versionName", String.valueOf(pi.versionName));
            this.crashReport.put("versionCode", String.valueOf(pi.versionCode));
         }

         Field[] fieldList = Build.class.getDeclaredFields();
         if (fieldList != null) {
            Field[] var5 = fieldList;
            int var6 = fieldList.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               Field device = var5[var7];
               device.setAccessible(true);
               this.crashReport.put(device.getName(), String.valueOf(device.get((Object)null)));
            }
         }
      } catch (NameNotFoundException var9) {
         var9.printStackTrace();
      } catch (IllegalArgumentException var10) {
         var10.printStackTrace();
      } catch (IllegalAccessException var11) {
         var11.printStackTrace();
      }

   }

   private void saveCrashInfo(Throwable ex) {
      try {
         Writer writer = new StringWriter();
         PrintWriter printWriter = new PrintWriter(writer);
         ex.printStackTrace(printWriter);

         for(Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
            cause.printStackTrace(printWriter);
         }

         String result = writer.toString();
         printWriter.close();
         this.crashReport.put("exception", ex.getLocalizedMessage());
         this.crashReport.put("trace", result);
         String fileName = this.getCrashFileName();
         FileOutputStream fos = this.mContext.openFileOutput(fileName, 0);
         this.crashReport.store(fos, this.mContext.getPackageName());
         fos.flush();
         fos.close();
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   public String getCrashFileName() {
      StringBuilder fileName = new StringBuilder("crash_");
      Date date = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      fileName.append(sdf.format(date));
      fileName.append(".cr");
      return fileName.toString();
   }

   public void sendCrashReport() {
      File filesDir = this.mContext.getFilesDir();
      FilenameFilter filter = new FilenameFilter() {
         public boolean accept(File dir, String filename) {
            return filename.endsWith(".cr");
         }
      };
      String[] list = filesDir.list(filter);
      if (list != null && list.length > 0) {
         String[] var4 = list;
         int var5 = list.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String fileName = var4[var6];
            File file = new File(this.mContext.getFilesDir(), fileName);
            if (file.exists()) {
               file.delete();
            }
         }
      }

   }
}
