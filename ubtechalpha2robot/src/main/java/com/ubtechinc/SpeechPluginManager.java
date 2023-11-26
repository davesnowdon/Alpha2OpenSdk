package com.ubtechinc;

import android.content.Context;
import com.ubtechinc.contant.CustomLanguage;
import com.ubtechinc.db.SpeechPluginDao;
import com.ubtechinc.db.pojos.Alpha2SpeechPlugin;

public class SpeechPluginManager {
   public SpeechPluginManager() {
   }

   public static Alpha2SpeechPlugin getPlugin(Context mContext) {
      Alpha2SpeechPlugin plugin = SpeechPluginDao.query(mContext);
      if (plugin == null) {
         plugin = new Alpha2SpeechPlugin();
         plugin.name = "Iflytek";
         plugin.action = "com.ubtechinc.services.IflytekSpeeckServices";
      }

      return plugin;
   }

   public static Alpha2SpeechPlugin getSpecifyPlugin(Context mContext, CustomLanguage specifyLanguage) {
      Alpha2SpeechPlugin plugin = SpeechPluginDao.query(mContext);
      if (plugin == null) {
         plugin = new Alpha2SpeechPlugin();
         plugin.name = "Speech";
         plugin.action = "com.ubtechinc.services.SpeechServices";
      }

      return plugin;
   }
}
