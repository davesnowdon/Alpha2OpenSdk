package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Message;

@Message
public class FileName {
   private String fileName;

   public FileName() {
   }

   public String getFileName() {
      return this.fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }
}
