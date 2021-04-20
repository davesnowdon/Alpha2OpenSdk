package com.ubtechinc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.ubtechinc.db.pojos.Alpha2ActionName;
import com.ubtechinc.db.provider.Provider;

public class ActionNameDao {
   private static String TAG = "ActionNameDao";

   public ActionNameDao() {
   }

   public static synchronized boolean isExist(Context mContext, String actionID) {
      Cursor c = null;

      boolean var3;
      try {
         c = mContext.getContentResolver().query(Provider.ActionNameColumns.CONTENT_URI, new String[]{"action_id", "action_type"}, "action_id=?", new String[]{actionID + ""}, (String)null);
         if (c == null || !c.moveToFirst()) {
            return false;
         }

         var3 = true;
      } catch (Exception var7) {
         var7.printStackTrace();
         return false;
      } finally {
         if (c != null) {
            c.close();
         }

      }

      return var3;
   }

   public static synchronized int insert(Context mContext, Alpha2ActionName alpha2ActionName) {
      ContentValues values = new ContentValues();
      values.put("action_id", alpha2ActionName.action_id);
      values.put("action_type", alpha2ActionName.action_type);
      values.put("action_cn_name", alpha2ActionName.action_cn_name);
      values.put("action_en_name", alpha2ActionName.action_en_name);
      Uri uri = null;

      try {
         uri = mContext.getContentResolver().insert(Provider.ActionNameColumns.CONTENT_URI, values);
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

   public static synchronized int insert(Context mContext, String actionID, int actionType, String actionCNName, String actionENName) {
      boolean isExist = isExist(mContext, actionID);
      if (isExist) {
         return -1;
      } else {
         Alpha2ActionName alpha2ActionName = new Alpha2ActionName();
         alpha2ActionName.action_id = actionID;
         alpha2ActionName.action_type = actionType + "";
         alpha2ActionName.action_cn_name = actionCNName;
         alpha2ActionName.action_en_name = actionENName;
         int id = insert(mContext, alpha2ActionName);
         return id;
      }
   }

   public static synchronized int delete(Context mContext, String actionID) {
      int c = mContext.getContentResolver().delete(Provider.ActionNameColumns.CONTENT_URI, "action_id =? ", new String[]{actionID});
      return c;
   }

   public static synchronized void update(Context mContext, String oldActionID, String actionID, int actionType, String actionCNName, String actionENName) {
      ContentValues values = new ContentValues();
      values.put("action_id", actionID);
      values.put("action_type", actionType + "");
      if (actionCNName != null && actionENName != null) {
         values.put("action_cn_name", actionCNName);
         values.put("action_en_name", actionENName);
      } else {
         if (actionCNName == null && actionENName == null) {
            return;
         }

         if (actionCNName == null) {
            values.put("action_en_name", actionENName);
         } else if (actionENName == null) {
            values.put("action_cn_name", actionCNName);
         }
      }

      try {
         mContext.getContentResolver().update(Provider.ActionNameColumns.CONTENT_URI, values, "action_id =? ", new String[]{oldActionID});
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   public static synchronized Alpha2ActionName query(Context mContext, String actionID) {
      if (mContext == null && actionID == null) {
         return null;
      } else {
         Alpha2ActionName p = null;
         Cursor c = null;

         try {
            c = mContext.getContentResolver().query(Provider.ActionNameColumns.CONTENT_URI, new String[]{"action_id", "action_type", "action_cn_name", "action_en_name"}, "action_id=?", new String[]{actionID + ""}, (String)null);
            String action_id;
            if (c != null && c.moveToFirst()) {
               p = new Alpha2ActionName();
               action_id = c.getString(c.getColumnIndexOrThrow("action_id"));
               String action_type = c.getString(c.getColumnIndexOrThrow("action_type"));
               String action_cn_name = c.getString(c.getColumnIndexOrThrow("action_cn_name"));
               String action_en_name = c.getString(c.getColumnIndexOrThrow("action_en_name"));
               p.action_id = action_id;
               p.action_type = action_type;
               p.action_cn_name = action_cn_name;
               p.action_en_name = action_en_name;
            } else {
               action_id = null;
            }
         } catch (Exception var11) {
            var11.printStackTrace();
         } finally {
            if (c != null) {
               c.close();
            }

            return p;
         }
      }
   }

   public static synchronized int clearData(Context mContext) {
      int c = mContext.getContentResolver().delete(Provider.ActionNameColumns.CONTENT_URI, (String)null, (String[])null);
      return c;
   }

   public static synchronized String getAllAction(Context mContext) {
      StringBuilder sb = new StringBuilder();
      Cursor c = null;

      try {
         c = mContext.getContentResolver().query(Provider.ActionNameColumns.CONTENT_URI, new String[]{"action_id", "action_type", "action_cn_name", "action_en_name"}, (String)null, (String[])null, (String)null);
         if (c != null && c.moveToFirst()) {
            new Alpha2ActionName();
            String action_id = c.getString(c.getColumnIndexOrThrow("action_id"));
            String action_type = c.getString(c.getColumnIndexOrThrow("action_type"));
            String action_cn_name = c.getString(c.getColumnIndexOrThrow("action_cn_name"));
            String action_en_name = c.getString(c.getColumnIndexOrThrow("action_en_name"));
            sb.append(action_id).append("##");
            sb.append(action_type).append("##");
            sb.append(action_cn_name).append("##");
            sb.append(action_en_name).append("##");

            while(c.moveToNext()) {
               action_id = c.getString(c.getColumnIndexOrThrow("action_id"));
               action_type = c.getString(c.getColumnIndexOrThrow("action_type"));
               action_cn_name = c.getString(c.getColumnIndexOrThrow("action_cn_name"));
               action_en_name = c.getString(c.getColumnIndexOrThrow("action_en_name"));
               sb.append(action_id).append("##");
               sb.append(action_type).append("##");
               sb.append(action_cn_name).append("##");
               sb.append(action_en_name).append("##");
            }

            sb.delete(sb.length() - 2, sb.length());
         } else {
            Object var3 = null;
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      } finally {
         if (c != null) {
            c.close();
         }

         return sb.toString();
      }
   }

   public static synchronized Alpha2ActionName getActionID(Context mContext, String actionName, String language) {
      if (mContext == null && actionName == null) {
         return null;
      } else {
         Alpha2ActionName p = null;
         Cursor c = null;

         try {
            if ("CN".equals(language)) {
               c = mContext.getContentResolver().query(Provider.ActionNameColumns.CONTENT_URI, new String[]{"action_id", "action_type", "action_cn_name", "action_en_name"}, "action_cn_name=?", new String[]{actionName + ""}, (String)null);
            } else if ("EN".equals(language)) {
               c = mContext.getContentResolver().query(Provider.ActionNameColumns.CONTENT_URI, new String[]{"action_id", "action_type", "action_cn_name", "action_en_name"}, "action_en_name=?", new String[]{actionName + ""}, (String)null);
            }

            String action_id;
            if (c != null && c.moveToFirst()) {
               p = new Alpha2ActionName();
               action_id = c.getString(c.getColumnIndexOrThrow("action_id"));
               String action_type = c.getString(c.getColumnIndexOrThrow("action_type"));
               String action_cn_name = c.getString(c.getColumnIndexOrThrow("action_cn_name"));
               String action_en_name = c.getString(c.getColumnIndexOrThrow("action_en_name"));
               p.action_id = action_id;
               p.action_type = action_type;
               p.action_cn_name = action_cn_name;
               p.action_en_name = action_en_name;
            } else {
               action_id = null;
            }
         } catch (Exception var12) {
            var12.printStackTrace();
         } finally {
            if (c != null) {
               c.close();
            }

            return p;
         }
      }
   }
}
