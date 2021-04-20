package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Message;

@Message
public class SavaActionProfileXMLRespone {
   private short result;

   public SavaActionProfileXMLRespone() {
   }

   public short getResult() {
      return this.result;
   }

   public void setResult(short result) {
      this.result = result;
   }
}
