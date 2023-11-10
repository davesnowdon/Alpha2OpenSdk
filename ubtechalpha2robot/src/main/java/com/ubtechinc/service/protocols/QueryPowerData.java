package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class QueryPowerData {
   @Index(0)
   private boolean mIsFromClient;
   @Index(1)
   private int powerValue;

   public QueryPowerData() {
   }

   public boolean ismIsFromClient() {
      return this.mIsFromClient;
   }

   public void setmIsFromClient(boolean mIsFromClient) {
      this.mIsFromClient = mIsFromClient;
   }

   public int getPowerValue() {
      return this.powerValue;
   }

   public void setPowerValue(int powerValue) {
      this.powerValue = powerValue;
   }
}
