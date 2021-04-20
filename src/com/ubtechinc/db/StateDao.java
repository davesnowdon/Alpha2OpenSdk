package com.ubtechinc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.ubtechinc.db.pojos.Alpha2State;
import com.ubtechinc.db.provider.Provider;

public class StateDao {
   private static String TAG = "StateDao";

   public StateDao() {
   }

   public static synchronized int insertPower(Context mContext, int i) {
      if (isExist(mContext)) {
         return updatePower(mContext, i);
      } else {
         Alpha2State state = new Alpha2State();
         state.power = i;
         ContentValues values = new ContentValues();
         values.put("power", state.power);
         Uri uri = mContext.getContentResolver().insert(Provider.StateColums.CONTENT_URI, values);
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

   public static synchronized int insertDebug(Context mContext, int i) {
      if (isExist(mContext)) {
         return updateDebug(mContext, i);
      } else {
         Alpha2State state = new Alpha2State();
         state.debug = i;
         ContentValues values = new ContentValues();
         values.put("debug", state.debug);
         Uri uri = mContext.getContentResolver().insert(Provider.StateColums.CONTENT_URI, values);
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

   public static synchronized Alpha2State query(Context mContext) {
      Cursor c = null;

      Alpha2State var3;
      try {
         c = mContext.getContentResolver().query(Provider.StateColums.CONTENT_URI, new String[]{"power", "debug"}, (String)null, (String[])null, (String)null);
         Alpha2State p;
         if (c == null || !c.moveToFirst()) {
            Log.i(TAG, "query failure!");
            p = null;
            return p;
         }

         p = new Alpha2State();
         p.power = c.getInt(c.getColumnIndexOrThrow("power"));
         p.debug = c.getInt(c.getColumnIndexOrThrow("debug"));
         Log.i(TAG, "Alpha2State=" + p.power + " | " + p.debug);
         var3 = p;
      } finally {
         if (c != null) {
            c.close();
         }

      }

      return var3;
   }

   public static synchronized int clearData(Context mContext) {
      int c = -1;

      try {
         c = mContext.getContentResolver().delete(Provider.StateColums.CONTENT_URI, (String)null, (String[])null);
      } catch (Exception var3) {
      }

      return c;
   }

   private static synchronized int updatePower(Context mContext, int power) {
      ContentValues values = new ContentValues();
      values.put("power", power);
      int c = mContext.getContentResolver().update(Provider.StateColums.CONTENT_URI, values, (String)null, (String[])null);
      return c;
   }

   private static synchronized int updateDebug(Context mContext, int debug) {
      ContentValues values = new ContentValues();
      values.put("debug", debug);
      int c = mContext.getContentResolver().update(Provider.StateColums.CONTENT_URI, values, (String)null, (String[])null);
      return c;
   }

   private static synchronized boolean isExist(Context mContext) {
      Cursor c = null;

      boolean var2;
      try {
         c = mContext.getContentResolver().query(Provider.StateColums.CONTENT_URI, (String[])null, (String)null, (String[])null, (String)null);
         if (c == null || !c.moveToFirst()) {
            var2 = false;
            return var2;
         }

         var2 = true;
      } finally {
         if (c != null) {
            c.close();
         }

      }

      return var2;
   }
}
