package com.ubtechinc.service.protocols;

import com.ubtechinc.contant.StaticValue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.msgpack.annotation.Message;

@Message
public class GetActionFileList {
   private List<String> fileList = new ArrayList();

   public List<String> getFileList() {
      return this.fileList;
   }

   public void setFileList(List<String> fileList) {
      this.fileList = fileList;
   }

   public GetActionFileList() {
      this.getActionFileList();
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

   public void getActionFileList() {
      File path = new File(StaticValue.ACTION_PATH);
      File[] files = path.listFiles();
      if (null != files) {
         for(int i = 0; i < files.length; ++i) {
            if (!files[i].isDirectory()) {
               String fileExt = this.getExtensionName(files[i].getName());
               if (fileExt.equals("ubx")) {
                  String strFileName = this.getFileNameNoEx(files[i].getName());
                  this.fileList.add(strFileName);
               }
            }
         }

      }
   }
}
