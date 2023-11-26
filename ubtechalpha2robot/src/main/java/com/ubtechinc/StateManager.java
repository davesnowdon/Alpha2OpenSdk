package com.ubtechinc;

import android.content.Context;
import com.ubtechinc.db.StateDao;
import com.ubtechinc.db.pojos.Alpha2State;

public class StateManager {
   public StateManager() {
   }

   public static boolean isPower(Context mContext) {
      boolean ret = false;
      Alpha2State state = StateDao.query(mContext);
      if (state == null) {
         ret = true;
      } else if (state.power == 0) {
         ret = false;
      } else if (state.power == 1) {
         ret = true;
      }

      return ret;
   }

   public static boolean isDebug(Context mContext) {
      boolean ret = false;
      Alpha2State state = StateDao.query(mContext);
      if (state == null) {
         ret = false;
      } else if (state.debug == 0) {
         ret = false;
      } else if (state.debug == 1) {
         ret = true;
      }

      return ret;
   }
}
