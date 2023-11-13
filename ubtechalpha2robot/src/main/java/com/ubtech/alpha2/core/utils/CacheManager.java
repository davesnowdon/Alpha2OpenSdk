package com.ubtech.alpha2.core.utils;

import android.os.Environment;
import com.ubtech.alpha2.BaseApplication;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;

public class CacheManager {
   private static final String TAG = CacheManager.class.getSimpleName();

   public CacheManager() {
   }

   public static void saveTestData(String xmlResult, String fileName) {
      try {
         String state = Environment.getExternalStorageState();
         if ("mounted".equals(state) && BaseApplication.isDebug) {
            String sdCardPath = Environment.getExternalStorageDirectory().getPath();
            StringBuilder path = (new StringBuilder(sdCardPath)).append(File.separator).append("testData");
            File file = new File(path.toString());
            if (!file.exists()) {
               file.mkdirs();
            }

            path = path.append(File.separator).append(fileName).append(".xml");
            file = new File(path.toString());
            FileOutputStream output = new FileOutputStream(file);
            BufferedOutputStream os = new BufferedOutputStream(output);
            os.write(xmlResult.getBytes());
            os.flush();
            os.close();
            NLog.e(TAG, "saveTestData success: " + path);
         }
      } catch (FileNotFoundException var8) {
         var8.printStackTrace();
      } catch (IOException var9) {
         var9.printStackTrace();
      }

   }

   public static boolean writeObject(Object object, String key) {
      try {
         String path = getCachePath(key);
         FileOutputStream fos = new FileOutputStream(path);
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject(object);
         oos.flush();
         oos.close();
         File file = new File(path);
         if (file.exists()) {
            file.setLastModified(System.currentTimeMillis());
            NLog.e(TAG, "writeObject object success : " + path);
            return true;
         }
      } catch (FileNotFoundException var6) {
         var6.printStackTrace();
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      return false;
   }

   public static Object readObject(String key) {
      Object obj = null;

      try {
         String cachePath = getCachePath(key);
         File file = new File(cachePath);
         if (file.exists()) {
            FileInputStream fis = new FileInputStream(cachePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            obj = ois.readObject();
            ois.close();
         }
      } catch (StreamCorruptedException var6) {
         var6.printStackTrace();
      } catch (OptionalDataException var7) {
         var7.printStackTrace();
      } catch (FileNotFoundException var8) {
         var8.printStackTrace();
      } catch (IOException var9) {
         var9.printStackTrace();
      } catch (ClassNotFoundException var10) {
         var10.printStackTrace();
      }

      return obj;
   }

   public static boolean isInvalidObject(String key, long timeout) {
      String path = getCachePath(key);
      File file = new File(path);
      if (file.exists()) {
         long last = file.lastModified();
         long current = System.currentTimeMillis();
         if (current - last < timeout * 1000L) {
            NLog.e(TAG, "the cahce is effect : " + path);
            return true;
         }
      }

      NLog.e(TAG, "the cahce is invalid : " + path);
      return false;
   }

   public static String getCachePath(String key) {
      StringBuilder path = new StringBuilder();
      path.append(BaseApplication.SYS_CACHE_PATH);
      path.append(File.separator);
      path.append(key);
      return path.toString();
   }

   public static boolean clearCache(String key) {
      File file = new File(getCachePath(key));
      return file.exists() ? file.delete() : false;
   }

   public static boolean clearCache() {
      File file = new File(BaseApplication.SYS_CACHE_PATH);
      return file.exists() ? file.delete() : false;
   }
}
