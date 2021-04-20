package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class GetNewActionList {
   @Index(0)
   private String languageType;
   @Index(1)
   private int actionType;

   public GetNewActionList() {
   }

   public String getLanguageType() {
      return this.languageType;
   }

   public void setLanguageType(String languageType) {
      this.languageType = languageType;
   }

   public int getActionType() {
      return this.actionType;
   }

   public void setActionType(int actionType) {
      this.actionType = actionType;
   }
}
