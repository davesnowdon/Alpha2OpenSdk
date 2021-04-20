package com.ubtechinc.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class CrashHandler implements UncaughtExceptionHandler {
   private static final String TAG = "CrashHandler";
   private UncaughtExceptionHandler mDefaultHandler;
   private static CrashHandler INSTANCE = new CrashHandler();
   private Context mContext;
   private Map<String, String> info = new HashMap();
   private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

   private CrashHandler() {
   }

   public static CrashHandler getInstance() {
      return INSTANCE;
   }

   public void init(Context context) {
      this.mContext = context;
      this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
      Thread.setDefaultUncaughtExceptionHandler(this);
   }

   public void uncaughtException(Thread thread, Throwable ex) {
      if (!this.handleException(ex) && this.mDefaultHandler != null) {
         this.mDefaultHandler.uncaughtException(thread, ex);
      } else {
         ex.printStackTrace();

         try {
            Thread.sleep(3000L);
         } catch (InterruptedException var4) {
            var4.printStackTrace();
         }

         Process.killProcess(Process.myPid());
         System.exit(1);
      }

   }

   public boolean handleException(Throwable ex) {
      if (ex == null) {
         return false;
      } else {
         (new Thread() {
            public void run() {
               Looper.prepare();
               Toast.makeText(CrashHandler.this.mContext, "很抱歉,程序出现异常,即将退出", 0).show();
               Looper.loop();
            }
         }).start();
         this.collectDeviceInfo(this.mContext);
         this.saveCrashInfo2File(ex);
         return true;
      }
   }

   public void collectDeviceInfo(Context context) {
      try {
         PackageManager pm = context.getPackageManager();
         PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 1);
         if (pi != null) {
            String versionName = pi.versionName == null ? "null" : pi.versionName;
            String versionCode = pi.versionCode + "";
            this.info.put("versionName", versionName);
            this.info.put("versionCode", versionCode);
         }
      } catch (NameNotFoundException var10) {
         var10.printStackTrace();
      }

      Field[] fields = Build.class.getDeclaredFields();
      Field[] var12 = fields;
      int var13 = fields.length;

      for(int var14 = 0; var14 < var13; ++var14) {
         Field field = var12[var14];

         try {
            field.setAccessible(true);
            this.info.put(field.getName(), field.get("").toString());
            Log.d("CrashHandler", field.getName() + ":" + field.get(""));
         } catch (IllegalArgumentException var8) {
            var8.printStackTrace();
         } catch (IllegalAccessException var9) {
            var9.printStackTrace();
         }
      }

   }

   private String saveCrashInfo2File(Throwable ex) {
      StringBuffer sb = new StringBuffer();
      Iterator var3 = this.info.entrySet().iterator();

      String result;
      while(var3.hasNext()) {
         Entry<String, String> entry = (Entry)var3.next();
         String key = (String)entry.getKey();
         result = (String)entry.getValue();
         sb.append(key + "=" + result + "\r\n");
      }

      Writer writer = new StringWriter();
      PrintWriter pw = new PrintWriter(writer);
      ex.printStackTrace(pw);

      for(Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
         cause.printStackTrace(pw);
      }

      pw.close();
      result = writer.toString();
      sb.append(result);
      long timetamp = System.currentTimeMillis();
      String time = this.format.format(new Date());
      String fileName = "crash-" + time + "-" + timetamp + ".log";
      if (Environment.getExternalStorageState().equals("mounted")) {
         try {
            String path = Environment.getExternalStorageDirectory() + "/crash/";
            File dir = new File(path);
            if (!dir.exists()) {
               dir.mkdir();
            }

            FileOutputStream fos = new FileOutputStream(path + fileName);
            fos.write(sb.toString().getBytes());
            fos.close();
            return fileName;
         } catch (FileNotFoundException var14) {
            var14.printStackTrace();
         } catch (IOException var15) {
            var15.printStackTrace();
         }
      }

      return null;
   }
}
