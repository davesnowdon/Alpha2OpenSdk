package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Message;

@Message
public class UpdateSoftwareResult {
   private int type;
   private int result;

   public UpdateSoftwareResult() {
   }

   public int getResult() {
      return this.result;
   }

   public void setResult(int result) {
      this.result = result;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }
}
