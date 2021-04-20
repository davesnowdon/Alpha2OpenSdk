package com.ubtechinc.db.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class Provider {
   public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jacp.demo";
   public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jacp.demo";

   public Provider() {
   }

   public static final class ActionNameColumns implements BaseColumns {
      public static final String AUTHORITY = "com.ubtechinc.alpha2services.action_name";
      public static final Uri CONTENT_URI = Uri.parse("content://com.ubtechinc.alpha2services.action_name/actionNames");
      public static final String TABLE_NAME = "actionName";
      public static final String DEFAULT_SORT_ORDER = "action_id asc";
      public static final String ACTION_ID = "action_id";
      public static final String ACTION_TYPE = "action_type";
      public static final String ACTION_CN_NAME = "action_cn_name";
      public static final String ACTION_EN_NAME = "action_en_name";

      public ActionNameColumns() {
      }
   }

   public static final class PhotoUrlColumns implements BaseColumns {
      public static final String AUTHORITY = "com.ubtechinc.alpha2services.photo";
      public static final Uri CONTENT_URI = Uri.parse("content://com.ubtechinc.alpha2services.photo/photoUrls");
      public static final String TABLE_NAME = "photoUrl";
      public static final String DEFAULT_SORT_ORDER = "filePath asc";
      public static final String FILEPATH = "filePath";
      public static final String URL = "url";

      public PhotoUrlColumns() {
      }
   }

   public static final class SpeechPluginColumns implements BaseColumns {
      public static final String AUTHORITY = "com.ubtechinc.alpha2services.alpha2speech";
      public static final Uri CONTENT_URI = Uri.parse("content://com.ubtechinc.alpha2services.alpha2speech/speechs");
      public static final String TABLE_NAME = "speech";
      public static final String name = "name";
      public static final String action = "action";

      public SpeechPluginColumns() {
      }
   }

   public static final class StateColums implements BaseColumns {
      public static final String AUTHORITY = "com.ubtechinc.alpha2services.alpha2state";
      public static final Uri CONTENT_URI = Uri.parse("content://com.ubtechinc.alpha2services.alpha2state/states");
      public static final String TABLE_NAME = "state";
      public static final String POWER = "power";
      public static final String DEBUG = "debug";

      public StateColums() {
      }
   }

   public static final class Alpha2AppColumns implements BaseColumns {
      public static final String AUTHORITY = "com.ubtechinc.alpha2services.alpha2app";
      public static final Uri CONTENT_URI = Uri.parse("content://com.ubtechinc.alpha2services.alpha2app/apps");
      public static final String TABLE_NAME = "app";
      public static final String DEFAULT_SORT_ORDER = "appid desc";
      public static final String NAME = "name";
      public static final String APPID = "appid";

      public Alpha2AppColumns() {
      }
   }
}
