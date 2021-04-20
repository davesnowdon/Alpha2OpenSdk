package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class Alpha2Version {
   @Index(0)
   private byte state;
   @Index(1)
   private String version;

   public Alpha2Version() {
   }

   public byte getState() {
      return this.state;
   }

   public void setState(byte state) {
      this.state = state;
   }

   public String getVersion() {
      return this.version;
   }

   public void setVersion(String version) {
      this.version = version;
   }
}
