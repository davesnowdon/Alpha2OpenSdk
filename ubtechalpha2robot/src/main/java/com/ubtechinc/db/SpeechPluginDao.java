package com.ubtechinc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.ubtechinc.db.pojos.Alpha2SpeechPlugin;
import com.ubtechinc.db.provider.Provider;

public class SpeechPluginDao {
   private static String TAG = "StateDao";

   public SpeechPluginDao() {
   }

   public static synchronized int insert(Context mContext, Alpha2SpeechPlugin bean) {
      if (isExist(mContext)) {
         return update(mContext, bean);
      } else {
         ContentValues values = new ContentValues();
         values.put("name", bean.name);
         values.put("action", bean.action);
         Uri uri = mContext.getContentResolver().insert(Provider.SpeechPluginColumns.CONTENT_URI, values);
         Log.i(TAG, "insert uri=" + uri);
         String lastPath = uri.getLastPathSegment();
         if (TextUtils.isEmpty(lastPath)) {
            Log.i(TAG, "insert failure!");
         } else {
            Log.i(TAG, "insert success! the id is " + lastPath);
         }

         return Integer.parseInt(lastPath);
      }
   }

   public static synchronized Alpha2SpeechPlugin query(Context mContext) {
      Cursor c = null;

      Alpha2SpeechPlugin var3;
      try {
         c = mContext.getContentResolver().query(Provider.SpeechPluginColumns.CONTENT_URI, new String[]{"name", "action"}, (String)null, (String[])null, (String)null);
         Alpha2SpeechPlugin p;
         if (c == null || !c.moveToFirst()) {
            Log.i(TAG, "query failure!");
            p = null;
            return p;
         }

         p = new Alpha2SpeechPlugin();
         p.name = c.getString(c.getColumnIndexOrThrow("name"));
         p.action = c.getString(c.getColumnIndexOrThrow("action"));
         Log.i(TAG, "Alpha2State=" + p.name + " | " + p.action);
         var3 = p;
      } finally {
         if (c != null) {
            c.close();
         }

      }

      return var3;
   }

   public static synchronized int clearData(Context mContext) {
      int c = mContext.getContentResolver().delete(Provider.SpeechPluginColumns.CONTENT_URI, (String)null, (String[])null);
      return c;
   }

   private static synchronized int update(Context mContext, Alpha2SpeechPlugin bean) {
      ContentValues values = new ContentValues();
      values.put("name", bean.name);
      values.put("action", bean.action);
      int c = mContext.getContentResolver().update(Provider.SpeechPluginColumns.CONTENT_URI, values, (String)null, (String[])null);
      return c;
   }

   private static synchronized boolean isExist(Context mContext) {
      Cursor c = null;

      boolean var2;
      try {
         c = mContext.getContentResolver().query(Provider.SpeechPluginColumns.CONTENT_URI, (String[])null, (String)null, (String[])null, (String)null);
         if (c != null && c.moveToFirst()) {
            var2 = true;
            return var2;
         }

         var2 = false;
      } finally {
         if (c != null) {
            c.close();
         }

      }

      return var2;
   }
}
