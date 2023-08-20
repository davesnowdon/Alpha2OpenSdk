package com.ubtechinc.service.model;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class BaseRequest {
   @Index(0)
   private short cmd;

   public BaseRequest() {
   }

   public short getCmd() {
      return this.cmd;
   }

   public void setCmd(short cmd) {
      this.cmd = cmd;
   }
}
