package com.ubtech.alpha2.core.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.ubtech.alpha2.core.db.DaoSession;
import com.ubtech.alpha2.core.model.response.OrderItem;
import com.ubtech.alpha2.core.utils.NLog;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import java.util.Iterator;
import java.util.List;

public class OrderItemDao extends AbstractDao<OrderItem, Long> {
   private final String TAG = OrderItemDao.class.getSimpleName();
   public static final String TABLENAME = "ORDER_ITEM";

   public OrderItemDao(DaoConfig config) {
      super(config);
   }

   public OrderItemDao(DaoConfig config, DaoSession daoSession) {
      super(config, daoSession);
   }

   public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
      String constraint = ifNotExists ? "IF NOT EXISTS " : "";
      db.execSQL("CREATE TABLE " + constraint + "'ORDER_ITEM' ('_id' INTEGER PRIMARY KEY ,'NAME' TEXT NOT NULL ,'MESSAGETYPE' TEXT NOT NULL ,'VOICECODE' TEXT NOT NULL ,'FOCUS' TEXT NOT NULL );");
   }

   public static void dropTable(SQLiteDatabase db, boolean ifExists) {
      String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'ORDER_ITEM'";
      db.execSQL(sql);
   }

   protected void bindValues(SQLiteStatement stmt, OrderItem entity) {
      stmt.clearBindings();
      Long id = entity.getId();
      if (id != null) {
         stmt.bindLong(1, id);
      }

      stmt.bindString(2, entity.getName());
      stmt.bindString(3, entity.getMessagetype());
      stmt.bindString(4, entity.getVoicecode());
      stmt.bindString(5, entity.getFocus());
   }

   public Long readKey(Cursor cursor, int offset) {
      return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
   }

   public OrderItem readEntity(Cursor cursor, int offset) {
      OrderItem entity = new OrderItem(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), cursor.getString(offset + 1), cursor.getString(offset + 2), cursor.getString(offset + 3), cursor.getString(offset + 4));
      return entity;
   }

   public void readEntity(Cursor cursor, OrderItem entity, int offset) {
      entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
      entity.setName(cursor.getString(offset + 1));
      entity.setMessagetype(cursor.getString(offset + 2));
      entity.setVoicecode(cursor.getString(offset + 3));
      entity.setFocus(cursor.getString(offset + 4));
   }

   protected Long updateKeyAfterInsert(OrderItem entity, long rowId) {
      entity.setId(rowId);
      return rowId;
   }

   public Long getKey(OrderItem entity) {
      return entity != null ? entity.getId() : null;
   }

   protected boolean isEntityUpdateable() {
      return true;
   }

   public void saveOrderItemList(List<OrderItem> list) {
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         OrderItem item = (OrderItem)var2.next();
         this.insert(item);
      }

   }

   public OrderItem queryByName(String name) {
      QueryBuilder qb = this.queryBuilder();
      qb.where(OrderItemDao.Properties.Name.eq(name), new WhereCondition[0]);
      List<OrderItem> focusList = qb.list();
      if (focusList.isEmpty()) {
         return null;
      } else {
         Iterator var4 = focusList.iterator();

         while(var4.hasNext()) {
            OrderItem note = (OrderItem)var4.next();
            NLog.e(this.TAG, note.toString());
         }

         return (OrderItem)focusList.get(0);
      }
   }

   public static class Properties {
      public static final Property Id = new Property(0, Long.class, "id", true, "_id");
      public static final Property Name = new Property(1, String.class, "name", false, "NAME");
      public static final Property Messagetype = new Property(2, String.class, "messagetype", false, "MESSAGETYPE");
      public static final Property Voicecode = new Property(3, String.class, "voicecode", false, "VOICECODE");
      public static final Property Focus = new Property(4, String.class, "focus", false, "FOCUS");

      public Properties() {
      }
   }
}
