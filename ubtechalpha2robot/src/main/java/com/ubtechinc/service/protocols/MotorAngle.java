package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class MotorAngle {
   @Index(0)
   private int id;
   @Index(1)
   private int angel;
   @Index(2)
   private int time;

   public MotorAngle() {
   }

   public int getTime() {
      return this.time;
   }

   public void setTime(int time) {
      this.time = time;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int getAngel() {
      return this.angel;
   }

   public void setAngel(int angel) {
      this.angel = angel;
   }
}
