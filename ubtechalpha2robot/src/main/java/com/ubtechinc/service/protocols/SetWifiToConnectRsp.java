package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Message;

@Message
public class SetWifiToConnectRsp {
   private int result;

   public SetWifiToConnectRsp() {
   }

   public int getResult() {
      return this.result;
   }

   public void setResult(int result) {
      this.result = result;
   }
}
