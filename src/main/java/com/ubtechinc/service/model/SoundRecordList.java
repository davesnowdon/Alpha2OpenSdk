package com.ubtechinc.service.model;

import com.ubtechinc.contant.StaticValue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.msgpack.annotation.Message;

@Message
public class SoundRecordList {
   private List<String> soundRecordList = new ArrayList();

   public List<String> getSoundRecordList() {
      return this.soundRecordList;
   }

   public void setSoundRecordList(List<String> soundRecordList) {
      this.soundRecordList = soundRecordList;
   }

   public SoundRecordList() {
      this.getActionFileList();
   }

   public void getActionFileList() {
      File path = new File(StaticValue.ACTION_PATH);
      File[] files = path.listFiles();
      if (null != files) {
         for(int i = 0; i < files.length; ++i) {
            if (!files[i].isDirectory()) {
               String fileExt = this.getExtensionName(files[i].getName());
               if (fileExt.equals("zdy")) {
                  String strFileName = this.getFileNameNoEx(files[i].getName());
                  this.soundRecordList.add(strFileName);
               }
            }
         }

      }
   }

   private String getExtensionName(String filename) {
      if (filename != null && filename.length() > 0) {
         int dot = filename.lastIndexOf(46);
         if (dot > -1 && dot < filename.length() - 1) {
            return filename.substring(dot + 1);
         }
      }

      return filename;
   }

   private String getFileNameNoEx(String filename) {
      if (filename != null && filename.length() > 0) {
         int dot = filename.lastIndexOf(46);
         if (dot > -1 && dot < filename.length()) {
            return filename.substring(0, dot);
         }
      }

      return filename;
   }
}
