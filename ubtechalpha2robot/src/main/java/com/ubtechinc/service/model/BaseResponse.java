package com.ubtechinc.service.model;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class BaseResponse {
   @Index(0)
   private short result;

   public BaseResponse() {
   }

   public short getResult() {
      return this.result;
   }

   public void setResult(short result) {
      this.result = result;
   }
}
