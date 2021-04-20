package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class UploadFileUrl {
   @Index(0)
   private String fileUrl;

   public UploadFileUrl() {
   }

   public String getFileUrl() {
      return this.fileUrl;
   }

   public void setFileUrl(String fileUrl) {
      this.fileUrl = fileUrl;
   }
}
