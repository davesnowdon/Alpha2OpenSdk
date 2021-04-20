package com.ubtech.alpha2.core.utils.download.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DBOpenHelper extends SQLiteOpenHelper {
   private static final String DBNAME = "itcast.db";
   private static final int VERSION = 1;

   public DBOpenHelper(Context context) {
      super(context, "itcast.db", (CursorFactory)null, 1);
   }

   public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE IF NOT EXISTS filedownlog (id integer primary key autoincrement, downpath varchar(100), threadid INTEGER, downlength INTEGER)");
   }

   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS filedownlog");
      this.onCreate(db);
   }
}
