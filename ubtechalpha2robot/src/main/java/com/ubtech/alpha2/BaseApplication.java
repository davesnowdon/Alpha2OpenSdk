package com.ubtech.alpha2;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.text.TextUtils;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.ubtech.alpha2.core.db.DaoMaster;
import com.ubtech.alpha2.core.db.DaoSession;
import com.ubtech.alpha2.core.utils.AppCrashHandler;
import com.ubtech.alpha2.core.utils.NLog;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BaseApplication extends Application {
   private final String tag = BaseApplication.class.getSimpleName();
   public static boolean isDebug = false;
   public static String SYS_CACHE_PATH = null;
   private static DaoMaster daoMaster;
   private static DaoSession daoSession;

   public BaseApplication() {
   }

   public void onCreate() {
      SYS_CACHE_PATH = this.getFilesDir().getPath();
      this.initConfig();
      this.initCrashHandler();
      this.initImageLoader(this.getApplicationContext());
   }

   private void initCrashHandler() {
      if (!isDebug) {
         AppCrashHandler crashHandler = AppCrashHandler.getInstance();
         crashHandler.init(this.getApplicationContext());
      }

   }

   private void initConfig() {
      try {
         Properties props = new Properties();
         InputStream input = this.getAssets().open("config.properties");
         if (input != null) {
            props.load(input);
            String flag = props.getProperty("debug");
            if (!TextUtils.isEmpty(flag)) {
               isDebug = Boolean.parseBoolean(flag);
               NLog.e(this.tag, "isDebug: " + isDebug);
               NLog.setDebug(isDebug);
            }
         }
      } catch (IOException var4) {
         var4.printStackTrace();
      }

   }

   private void initImageLoader(Context mContext) {
      ImageLoaderConfiguration config = (new Builder(mContext)).threadPriority(3).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).build();
      ImageLoader.getInstance().init(config);
   }

   public static DaoSession getDaoSession(Context mContext) {
      if (daoSession == null) {
         if (daoMaster == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(mContext, "ubtech_alpha2_db", (CursorFactory)null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
         }

         daoSession = daoMaster.newSession();
      }

      return daoSession;
   }
}
