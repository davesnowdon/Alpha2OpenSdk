package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Message;

@Message
public class LoginResponse {
   private short result;

   public LoginResponse() {
   }

   public short getResult() {
      return this.result;
   }

   public void setResult(short result) {
      this.result = result;
   }
}
