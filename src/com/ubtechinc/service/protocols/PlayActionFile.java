package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Message;

@Message
public class PlayActionFile {
   private String filename;

   public PlayActionFile() {
   }

   public String getFilename() {
      return this.filename;
   }

   public void setFilename(String filename) {
      this.filename = filename;
   }
}
