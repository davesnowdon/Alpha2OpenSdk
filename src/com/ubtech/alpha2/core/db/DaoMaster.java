package com.ubtech.alpha2.core.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import com.ubtech.alpha2.core.db.dao.NoteDao;
import com.ubtech.alpha2.core.db.dao.OrderItemDao;
import de.greenrobot.dao.AbstractDaoMaster;
import de.greenrobot.dao.identityscope.IdentityScopeType;

public class DaoMaster extends AbstractDaoMaster {
   public static final int SCHEMA_VERSION = 3;

   public static void createAllTables(SQLiteDatabase db, boolean ifNotExists) {
      OrderItemDao.createTable(db, ifNotExists);
      NoteDao.createTable(db, ifNotExists);
   }

   public static void dropAllTables(SQLiteDatabase db, boolean ifExists) {
      OrderItemDao.dropTable(db, ifExists);
      NoteDao.dropTable(db, ifExists);
   }

   public DaoMaster(SQLiteDatabase db) {
      super(db, 3);
      this.registerDaoClass(OrderItemDao.class);
      this.registerDaoClass(NoteDao.class);
   }

   public DaoSession newSession() {
      return new DaoSession(this.db, IdentityScopeType.Session, this.daoConfigMap);
   }

   public DaoSession newSession(IdentityScopeType type) {
      return new DaoSession(this.db, type, this.daoConfigMap);
   }

   public static class DevOpenHelper extends DaoMaster.OpenHelper {
      public DevOpenHelper(Context context, String name, CursorFactory factory) {
         super(context, name, factory);
      }

      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
         DaoMaster.dropAllTables(db, true);
         this.onCreate(db);
      }
   }

   public abstract static class OpenHelper extends SQLiteOpenHelper {
      public OpenHelper(Context context, String name, CursorFactory factory) {
         super(context, name, factory, 3);
      }

      public void onCreate(SQLiteDatabase db) {
         Log.i("greenDAO", "Creating tables for schema version 3");
         DaoMaster.createAllTables(db, false);
      }
   }
}
