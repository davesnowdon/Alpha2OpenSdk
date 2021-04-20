package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class NewActionInfo {
   @Index(0)
   private String id;
   @Index(1)
   private String actionName;
   @Index(2)
   private int actionType;

   public NewActionInfo() {
   }

   public int getActionType() {
      return this.actionType;
   }

   public void setActionType(int actionType) {
      this.actionType = actionType;
   }

   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getActionName() {
      return this.actionName;
   }

   public void setActionName(String actionName) {
      this.actionName = actionName;
   }
}
