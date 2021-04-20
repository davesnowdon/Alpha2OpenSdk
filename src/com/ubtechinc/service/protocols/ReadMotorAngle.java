package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Message;

@Message
public class ReadMotorAngle {
   private int id;

   public ReadMotorAngle() {
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }
}
