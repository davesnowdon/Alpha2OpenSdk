package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Message;

@Message
public class DeleteActionFile {
   private String filename;

   public DeleteActionFile() {
   }

   public String getFilename() {
      return this.filename;
   }

   public void setFilename(String filename) {
      this.filename = filename;
   }
}
