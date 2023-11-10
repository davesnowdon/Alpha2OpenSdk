package com.ubtechinc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.ubtechinc.db.pojos.Alpha2App;
import com.ubtechinc.db.provider.Provider;

public class AppDao {
   public AppDao() {
   }

   private static synchronized boolean isExist(Context mContext, String appid) {
      Cursor c = null;

      try {
         c = mContext.getContentResolver().query(Provider.Alpha2AppColumns.CONTENT_URI, new String[]{"name", "appid"}, "appid=?", new String[]{appid + ""}, (String)null);
         if (c != null && c.moveToFirst()) {
            boolean var3 = true;
            return var3;
         }
      } finally {
         if (c != null) {
            c.close();
         }

      }

      return false;
   }

   private static synchronized int insert(Context mContext, Alpha2App alpha2App) {
      ContentValues values = new ContentValues();
      values.put("name", alpha2App.name);
      values.put("appid", alpha2App.appid);
      Uri uri = mContext.getContentResolver().insert(Provider.Alpha2AppColumns.CONTENT_URI, values);
      Log.i("DbDao", "insert uri=" + uri);
      String lastPath = uri.getLastPathSegment();
      if (TextUtils.isEmpty(lastPath)) {
         Log.i("DbDao", "insert failure!");
      } else {
         Log.i("DbDao", "insert success! the id is " + lastPath);
      }

      return Integer.parseInt(lastPath);
   }

   public static synchronized int insert(Context mContext, String name, String appid) {
      boolean isExist = isExist(mContext, appid);
      if (isExist) {
         return -1;
      } else {
         Alpha2App p = new Alpha2App();
         p.name = name;
         p.appid = appid;
         int id = insert(mContext, p);
         return id;
      }
   }

   public static synchronized int delete(Context mContext, String appid) {
      int c = mContext.getContentResolver().delete(Provider.Alpha2AppColumns.CONTENT_URI, "appid =? ", new String[]{appid});
      return c;
   }

   public static synchronized void update(Context mContext, String name, String appid, String newName, String newAppid) {
      ContentValues values = new ContentValues();
      values.put("name", newName);
      values.put("appid", newAppid);
      mContext.getContentResolver().update(Provider.Alpha2AppColumns.CONTENT_URI, values, "appid =? ", new String[]{appid});
   }

   public static synchronized boolean query(Context mContext, String mAppid) {
      if (mContext == null && mAppid == null) {
         return false;
      } else {
         Cursor c = null;

         boolean var3;
         try {
            c = mContext.getContentResolver().query(Provider.Alpha2AppColumns.CONTENT_URI, new String[]{"name", "appid"}, "appid=?", new String[]{mAppid + ""}, (String)null);
            if (c != null && c.moveToFirst()) {
               Alpha2App p = new Alpha2App();
               p.name = c.getString(c.getColumnIndexOrThrow("name"));
               p.appid = c.getString(c.getColumnIndexOrThrow("appid"));
               boolean var4 = true;
               return var4;
            }

            var3 = false;
         } finally {
            if (c != null) {
               c.close();
            }

         }

         return var3;
      }
   }

   public static synchronized Alpha2App getTopApp(Context mContext) {
      if (mContext == null) {
         return null;
      } else {
         Cursor c = null;

         Alpha2App var3;
         try {
            c = mContext.getContentResolver().query(Provider.Alpha2AppColumns.CONTENT_URI, new String[]{"name", "appid"}, (String)null, (String[])null, (String)null);
            if (c == null || !c.moveToFirst()) {
               return null;
            }

            Alpha2App p = new Alpha2App();
            p.name = c.getString(c.getColumnIndexOrThrow("name"));
            p.appid = c.getString(c.getColumnIndexOrThrow("appid"));
            var3 = p;
         } finally {
            if (c != null) {
               c.close();
            }

         }

         return var3;
      }
   }

   public static synchronized int clearData(Context mContext) {
      int c = -1;

      try {
         c = mContext.getContentResolver().delete(Provider.Alpha2AppColumns.CONTENT_URI, (String)null, (String[])null);
      } catch (Exception var3) {
      }

      return c;
   }
}
