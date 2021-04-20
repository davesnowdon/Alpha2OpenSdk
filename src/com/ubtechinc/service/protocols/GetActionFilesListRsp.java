package com.ubtechinc.service.protocols;

import com.ubtechinc.contant.StaticValue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.msgpack.annotation.Message;

@Message
public class GetActionFilesListRsp {
   private List<String> fileList = new ArrayList();

   public GetActionFilesListRsp() {
   }

   public void setActionFileName(String actionFileName) {
      if (this.isActionFileExisst(actionFileName)) {
         this.fileList.add(actionFileName);
         String filePath = actionFileName.replace('\\', '/');
         int nIndex = filePath.lastIndexOf(".", filePath.length());
         String FolderName = filePath.substring(0, nIndex);
         File path = new File(StaticValue.ACTION_PATH + FolderName);
         if (path.isDirectory()) {
            File[] files = path.listFiles();

            for(int i = 0; i < files.length; ++i) {
               String fileNames = FolderName + "/" + files[i].getName();
               this.fileList.add(fileNames);
            }
         }

      }
   }

   private boolean isActionFileExisst(String actionFileName) {
      boolean bRet = false;
      File file = new File(StaticValue.ACTION_PATH + actionFileName);
      if (file.exists()) {
         bRet = true;
      }

      return bRet;
   }
}
