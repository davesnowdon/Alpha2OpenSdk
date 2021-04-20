package com.ubtechinc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.ubtechinc.db.pojos.Alpha2Photo;
import com.ubtechinc.db.provider.Provider;
import java.util.ArrayList;

public class PhotoDao {
   private static String TAG = "PhotoDao";

   public PhotoDao() {
   }

   private static synchronized boolean isExist(Context mContext, String filePath) {
      Cursor c = null;

      try {
         c = mContext.getContentResolver().query(Provider.PhotoUrlColumns.CONTENT_URI, new String[]{"filePath", "url"}, "filePath=?", new String[]{filePath + ""}, (String)null);
         if (c != null && c.moveToFirst()) {
            boolean var3 = true;
            return var3;
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      } finally {
         if (c != null) {
            c.close();
         }

      }

      return false;
   }

   private static synchronized int insert(Context mContext, Alpha2Photo alpha2Photo) {
      ContentValues values = new ContentValues();
      values.put("filePath", alpha2Photo.filePath);
      values.put("url", alpha2Photo.url);
      Uri uri = null;

      try {
         uri = mContext.getContentResolver().insert(Provider.PhotoUrlColumns.CONTENT_URI, values);
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      if (uri != null) {
         Log.i(TAG, "insert uri=" + uri);
         String lastPath = uri.getLastPathSegment();
         if (TextUtils.isEmpty(lastPath)) {
            Log.i(TAG, "insert failure!");
         } else {
            Log.i(TAG, "insert success! the id is " + lastPath);
         }

         return Integer.parseInt(lastPath);
      } else {
         return 0;
      }
   }

   public static synchronized int insert(Context mContext, String filePath, String url) {
      boolean isExist = isExist(mContext, filePath);
      if (isExist) {
         return -1;
      } else {
         Alpha2Photo alpha2Photo = new Alpha2Photo();
         alpha2Photo.filePath = filePath;
         alpha2Photo.url = url;
         int id = insert(mContext, alpha2Photo);
         return id;
      }
   }

   public static synchronized int delete(Context mContext, String filePath) {
      int c = mContext.getContentResolver().delete(Provider.PhotoUrlColumns.CONTENT_URI, "filePath =? ", new String[]{filePath});
      return c;
   }

   public static synchronized void update(Context mContext, String filePath, String url, String newFilePath, String newUrl) {
      ContentValues values = new ContentValues();
      values.put("filePath", newFilePath);
      values.put("url", newUrl);

      try {
         mContext.getContentResolver().update(Provider.PhotoUrlColumns.CONTENT_URI, values, "filePath =? ", new String[]{filePath});
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   public static synchronized Alpha2Photo query(Context mContext, String filePath) {
      if (mContext == null && filePath == null) {
         return null;
      } else {
         Cursor c = null;

         Alpha2Photo var6;
         try {
            c = mContext.getContentResolver().query(Provider.PhotoUrlColumns.CONTENT_URI, new String[]{"filePath", "url"}, "filePath=?", new String[]{filePath + ""}, (String)null);
            Alpha2Photo p;
            if (c == null || !c.moveToFirst()) {
               p = null;
               return p;
            }

            p = new Alpha2Photo();
            String filepath = c.getString(c.getColumnIndexOrThrow("filePath"));
            String url = c.getString(c.getColumnIndexOrThrow("url"));
            p.filePath = filepath;
            p.url = url;
            var6 = p;
         } catch (Exception var10) {
            var10.printStackTrace();
            return null;
         } finally {
            if (c != null) {
               c.close();
            }

         }

         return var6;
      }
   }

   public static synchronized ArrayList<String> FindPicNotUpload(Context mContext) {
      if (mContext == null) {
         return null;
      } else {
         Cursor c = null;

         try {
            c = mContext.getContentResolver().query(Provider.PhotoUrlColumns.CONTENT_URI, new String[]{"filePath", "url"}, "url=?", new String[]{""}, (String)null);
            ArrayList list;
            if (c != null && c.moveToFirst()) {
               list = new ArrayList();
               String filepath = c.getString(c.getColumnIndexOrThrow("filePath"));
               list.add(filepath);

               while(c.moveToNext()) {
                  filepath = c.getString(c.getColumnIndexOrThrow("filePath"));
                  list.add(filepath);
               }

               ArrayList var4 = list;
               return var4;
            }

            list = null;
            return list;
         } catch (Exception var8) {
            var8.printStackTrace();
         } finally {
            if (c != null) {
               c.close();
            }

         }

         return null;
      }
   }

   public static synchronized int clearData(Context mContext) {
      int c = mContext.getContentResolver().delete(Provider.PhotoUrlColumns.CONTENT_URI, (String)null, (String[])null);
      return c;
   }
}
