package com.ubtechinc.service.protocols;

import java.io.Serializable;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class HeaderCmd implements Serializable {
   private static final long serialVersionUID = 1L;
   @Index(0)
   private int msgID;
   @Index(1)
   private byte[] msgBuf;

   public HeaderCmd() {
   }

   public HeaderCmd(int id, byte[] buf) {
      this.msgID = id;
      this.msgBuf = buf;
   }

   public int getMsgID() {
      return this.msgID;
   }

   public void setMsgID(int msgID) {
      this.msgID = msgID;
   }

   public byte[] getMsgBuf() {
      return this.msgBuf;
   }

   public void setMsgBuf(byte[] msgBuf) {
      this.msgBuf = msgBuf;
   }
}
