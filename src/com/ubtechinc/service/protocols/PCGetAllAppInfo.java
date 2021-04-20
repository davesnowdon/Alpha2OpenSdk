package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Message;

@Message
public class PCGetAllAppInfo {
   private boolean isPCRequest;

   public PCGetAllAppInfo() {
   }

   public boolean isPCRequest() {
      return this.isPCRequest;
   }

   public void setPCRequest(boolean isPCRequest) {
      this.isPCRequest = isPCRequest;
   }
}
