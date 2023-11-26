package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class MotorSocket {
   @Index(0)
   private byte cmd;
   @Index(1)
   private byte[] param;

   public MotorSocket() {
   }

   public byte getCmd() {
      return this.cmd;
   }

   public void setCmd(byte cmd) {
      this.cmd = cmd;
   }

   public byte[] getParam() {
      return this.param;
   }

   public void setParam(byte[] param) {
      this.param = param;
   }
}
