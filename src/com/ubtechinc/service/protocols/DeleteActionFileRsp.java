package com.ubtechinc.service.protocols;

import java.io.File;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class DeleteActionFileRsp {
   @Index(0)
   private String filename;
   @Index(1)
   private int result;

   public void deleteFile(File file) {
      if (file.exists()) {
         if (file.isFile()) {
            file.delete();
         } else if (file.isDirectory()) {
            File[] files = file.listFiles();

            for(int i = 0; i < files.length; ++i) {
               this.deleteFile(files[i]);
            }
         }

         file.delete();
      }

   }

   public DeleteActionFileRsp(String name) {
      this.filename = name;
      this.deleteFile(new File(name));
      String filePath = name.replace('\\', '/');
      int nIndex = filePath.lastIndexOf(".", filePath.length());
      String FolderName = filePath.substring(0, nIndex);
      this.deleteFile(new File(FolderName));
      this.result = 0;
   }

   public DeleteActionFileRsp() {
   }

   public String getFilename() {
      return this.filename;
   }

   public void setFilename(String filename) {
      this.filename = filename;
   }

   public int getResult() {
      return this.result;
   }

   public void setResult(int result) {
      this.result = result;
   }
}
