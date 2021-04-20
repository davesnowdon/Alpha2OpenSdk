package com.ubtech.alpha2.core.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CommonUtils {
   private static final String tag = CommonUtils.class.getSimpleName();
   public static final int NETTYPE_WIFI = 1;
   public static final int NETTYPE_CMWAP = 2;
   public static final int NETTYPE_CMNET = 3;

   public CommonUtils() {
   }

   public static boolean isNetworkConnected(Context context) {
      NLog.e("zdy", "ni  =" + context);
      ConnectivityManager cm = (ConnectivityManager)context.getSystemService("connectivity");
      NetworkInfo ni = cm.getActiveNetworkInfo();
      if (ni != null) {
         NLog.e("zdy", "ni  = " + ni + " ||  '" + ni.isConnectedOrConnecting());
         return true;
      } else {
         return false;
      }
   }

   public static int getNetworkType(Context context) {
      int netType = 0;
      ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService("connectivity");
      NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
      if (networkInfo == null) {
         return netType;
      } else {
         int nType = networkInfo.getType();
         if (nType == 0) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!TextUtils.isEmpty(extraInfo)) {
               if (extraInfo.toLowerCase().equals("cmnet")) {
                  netType = 3;
               } else {
                  netType = 2;
               }
            }
         } else if (nType == 1) {
            netType = 1;
         }

         return netType;
      }
   }

   public static boolean checkSDCard() {
      String flag = Environment.getExternalStorageState();
      return "mounted".equals(flag);
   }

   public static String getSaveLocalURL(String fileName) {
      StringBuilder path = new StringBuilder();
      if (!TextUtils.isEmpty(fileName)) {
         if (fileName.indexOf(".") < 0) {
            fileName = fileName + ".png";
         }

         path.append(Environment.getExternalStorageDirectory().getPath()).append(File.separator).append("youtu").append(File.separator);
         File file = new File(path.toString());
         if (!file.exists()) {
            file.mkdirs();
         }

         path.append(fileName);
      }

      return path.toString();
   }

   public static boolean checkFileExits(String filePath) {
      File file = new File(filePath);
      return file.exists();
   }

   public static boolean writeLocalFile(Bitmap bitmap, String fileName) {
      if (bitmap != null && checkSDCard()) {
         File file = null;
         FileOutputStream output = null;

         try {
            file = new File(getSaveLocalURL(fileName));
            if (!file.exists()) {
               if (file.createNewFile()) {
                  output = new FileOutputStream(file);
                  bitmap.compress(CompressFormat.PNG, 0, output);
                  output.flush();
                  boolean var4 = true;
                  return var4;
               }
            } else {
               Log.e(tag, "writeLocalFile: 该文件已经存在. " + fileName);
            }
         } catch (IOException var15) {
            var15.printStackTrace();
         } finally {
            try {
               if (output != null) {
                  output.close();
               }
            } catch (IOException var14) {
               var14.printStackTrace();
            }

         }

         return false;
      } else {
         return false;
      }
   }

   public static float dpToPixel(float dp, Context context) {
      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      return dp * ((float)metrics.densityDpi / 160.0F);
   }

   public static float pixelsToDp(float px, Context context) {
      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      return px / ((float)metrics.densityDpi / 160.0F);
   }
}
