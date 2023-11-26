package com.ubtechinc.service.protocols;

import java.io.Serializable;
import org.msgpack.annotation.Message;

@Message
public class FindMobilePhoneData implements Serializable {
   private boolean isSendByClient;

   public FindMobilePhoneData() {
   }

   public boolean isSendByClient() {
      return this.isSendByClient;
   }

   public void setSendByClient(boolean isSendByClient) {
      this.isSendByClient = isSendByClient;
   }
}
