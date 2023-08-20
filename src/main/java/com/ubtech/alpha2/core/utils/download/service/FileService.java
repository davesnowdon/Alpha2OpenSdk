package com.ubtech.alpha2.core.utils.download.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class FileService {
   private DBOpenHelper openHelper;

   public FileService(Context context) {
      this.openHelper = new DBOpenHelper(context);
   }

   public Map<Integer, Integer> getData(String path) {
      SQLiteDatabase db = this.openHelper.getReadableDatabase();
      Cursor cursor = db.rawQuery("select threadid, downlength from filedownlog where downpath=?", new String[]{path});
      HashMap data = new HashMap();

      while(cursor.moveToNext()) {
         data.put(cursor.getInt(0), cursor.getInt(1));
      }

      cursor.close();
      db.close();
      return data;
   }

   public void save(String path, Map<Integer, Integer> map) {
      SQLiteDatabase db = this.openHelper.getWritableDatabase();
      db.beginTransaction();

      try {
         Iterator var4 = map.entrySet().iterator();

         while(true) {
            if (!var4.hasNext()) {
               db.setTransactionSuccessful();
               break;
            }

            Entry<Integer, Integer> entry = (Entry)var4.next();
            db.execSQL("insert into filedownlog(downpath, threadid, downlength) values(?,?,?)", new Object[]{path, entry.getKey(), entry.getValue()});
         }
      } finally {
         db.endTransaction();
      }

      db.close();
   }

   public void update(String path, int threadId, int pos) {
      SQLiteDatabase db = this.openHelper.getWritableDatabase();
      db.execSQL("update filedownlog set downlength=? where downpath=? and threadid=?", new Object[]{pos, path, threadId});
      db.close();
   }

   public void delete(String path) {
      SQLiteDatabase db = this.openHelper.getWritableDatabase();
      db.execSQL("delete from filedownlog where downpath=?", new Object[]{path});
      db.close();
   }
}
