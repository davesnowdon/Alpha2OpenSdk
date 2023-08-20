package com.ubtech.alpha2.core.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.ubtech.alpha2.core.db.DaoSession;
import com.ubtech.alpha2.core.db.model.Note;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import java.util.Date;

public class NoteDao extends AbstractDao<Note, Long> {
   public static final String TABLENAME = "NOTE";

   public NoteDao(DaoConfig config) {
      super(config);
   }

   public NoteDao(DaoConfig config, DaoSession daoSession) {
      super(config, daoSession);
   }

   public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
      String constraint = ifNotExists ? "IF NOT EXISTS " : "";
      db.execSQL("CREATE TABLE " + constraint + "'NOTE' ('_id' INTEGER PRIMARY KEY ,'TEXT' TEXT NOT NULL ,'COMMENT' TEXT,'DATE' INTEGER);");
   }

   public static void dropTable(SQLiteDatabase db, boolean ifExists) {
      String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'NOTE'";
      db.execSQL(sql);
   }

   protected void bindValues(SQLiteStatement stmt, Note entity) {
      stmt.clearBindings();
      Long id = entity.getId();
      if (id != null) {
         stmt.bindLong(1, id);
      }

      stmt.bindString(2, entity.getText());
      String comment = entity.getComment();
      if (comment != null) {
         stmt.bindString(3, comment);
      }

      Date date = entity.getDate();
      if (date != null) {
         stmt.bindLong(4, date.getTime());
      }

   }

   public Long readKey(Cursor cursor, int offset) {
      return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
   }

   public Note readEntity(Cursor cursor, int offset) {
      Note entity = new Note(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), cursor.getString(offset + 1), cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), cursor.isNull(offset + 3) ? null : new Date(cursor.getLong(offset + 3)));
      return entity;
   }

   public void readEntity(Cursor cursor, Note entity, int offset) {
      entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
      entity.setText(cursor.getString(offset + 1));
      entity.setComment(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
      entity.setDate(cursor.isNull(offset + 3) ? null : new Date(cursor.getLong(offset + 3)));
   }

   protected Long updateKeyAfterInsert(Note entity, long rowId) {
      entity.setId(rowId);
      return rowId;
   }

   public Long getKey(Note entity) {
      return entity != null ? entity.getId() : null;
   }

   protected boolean isEntityUpdateable() {
      return true;
   }

   public static class Properties {
      public static final Property Id = new Property(0, Long.class, "id", true, "_id");
      public static final Property Text = new Property(1, String.class, "text", false, "TEXT");
      public static final Property Comment = new Property(2, String.class, "comment", false, "COMMENT");
      public static final Property Date = new Property(3, Date.class, "date", false, "DATE");

      public Properties() {
      }
   }
}
