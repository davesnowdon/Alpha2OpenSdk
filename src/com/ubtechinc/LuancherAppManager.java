package com.ubtechinc;

import android.content.Context;
import com.ubtechinc.db.AppDao;
import com.ubtechinc.db.pojos.Alpha2App;

public class LuancherAppManager {
   public LuancherAppManager() {
   }

   public static boolean isLuancherAPP(Context mContext, String mAppID) {
      return AppDao.query(mContext, mAppID);
   }

   public static boolean isDebug(Context mContext) {
      Alpha2App app = AppDao.getTopApp(mContext);
      return app == null;
   }
}
