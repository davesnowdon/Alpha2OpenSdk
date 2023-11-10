package com.ubtechinc.service.model;

import com.ubtechinc.service.model.base.BaseEntrity;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class GestureEntrity extends BaseEntrity {
   @Index(0)
   private int direction;
   @Index(1)
   private int type;
   @Index(2)
   private String actionParam;

   public GestureEntrity() {
   }

   public String getActionParam() {
      return this.actionParam;
   }

   public void setActionParam(String actionParam) {
      this.actionParam = actionParam;
   }

   public int getDirection() {
      return this.direction;
   }

   public void setDirection(int direction) {
      this.direction = direction;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }
}
