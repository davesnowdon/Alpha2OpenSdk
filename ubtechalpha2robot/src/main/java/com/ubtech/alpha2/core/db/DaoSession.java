package com.ubtech.alpha2.core.db;

import android.database.sqlite.SQLiteDatabase;
import com.ubtech.alpha2.core.db.dao.NoteDao;
import com.ubtech.alpha2.core.db.dao.OrderItemDao;
import com.ubtech.alpha2.core.db.model.Note;
import com.ubtech.alpha2.core.model.response.OrderItem;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;
import java.util.Map;

public class DaoSession extends AbstractDaoSession {
   private final DaoConfig orderItemDaoConfig;
   private final DaoConfig noteDaoConfig;
   private final OrderItemDao orderItemDao;
   private final NoteDao noteDao;

   public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig> daoConfigMap) {
      super(db);
      this.orderItemDaoConfig = ((DaoConfig)daoConfigMap.get(OrderItemDao.class)).clone();
      this.orderItemDaoConfig.initIdentityScope(type);
      this.noteDaoConfig = ((DaoConfig)daoConfigMap.get(NoteDao.class)).clone();
      this.noteDaoConfig.initIdentityScope(type);
      this.orderItemDao = new OrderItemDao(this.orderItemDaoConfig, this);
      this.noteDao = new NoteDao(this.noteDaoConfig, this);
      this.registerDao(OrderItem.class, this.orderItemDao);
      this.registerDao(Note.class, this.noteDao);
   }

   public void clear() {
      this.orderItemDaoConfig.getIdentityScope().clear();
      this.noteDaoConfig.getIdentityScope().clear();
   }

   public OrderItemDao getOrderItemDao() {
      return this.orderItemDao;
   }

   public NoteDao getNoteDao() {
      return this.noteDao;
   }
}
