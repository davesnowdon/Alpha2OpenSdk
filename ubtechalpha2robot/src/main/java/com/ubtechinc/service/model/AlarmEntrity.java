package com.ubtechinc.service.model;

import com.ubtechinc.service.model.base.BaseEntrity;
import java.io.Serializable;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class AlarmEntrity extends BaseEntrity implements Serializable {
   @Index(0)
   private int hh;
   @Index(1)
   private int mm;
   @Index(2)
   private int repeat;
   @Index(3)
   private boolean isUseAble;
   @Index(4)
   private String actionStartName;
   @Index(5)
   private String acitonEndName;
   @Index(6)
   private int actionType;
   @Index(7)
   private int yy;
   @Index(8)
   private int mo;
   @Index(9)
   private int day;
   @Index(10)
   private int date;
   @Index(11)
   private int ss;

   public AlarmEntrity() {
   }

   public int getSs() {
      return this.ss;
   }

   public void setSs(int ss) {
      this.ss = ss;
   }

   public int getYy() {
      return this.yy;
   }

   public void setYy(int yy) {
      this.yy = yy;
   }

   public int getMo() {
      return this.mo;
   }

   public void setMo(int mo) {
      this.mo = mo;
   }

   public int getDay() {
      return this.day;
   }

   public void setDay(int day) {
      this.day = day;
   }

   public int getDate() {
      return this.date;
   }

   public void setDate(int date) {
      this.date = date;
   }

   public int getActionType() {
      return this.actionType;
   }

   public void setActionType(int actionType) {
      this.actionType = actionType;
   }

   public int getHh() {
      return this.hh;
   }

   public void setHh(int hh) {
      this.hh = hh;
   }

   public int getMm() {
      return this.mm;
   }

   public void setMm(int mm) {
      this.mm = mm;
   }

   public int getRepeat() {
      return this.repeat;
   }

   public void setRepeat(int repeat) {
      this.repeat = repeat;
   }

   public boolean isUseAble() {
      return this.isUseAble;
   }

   public void setUseAble(boolean isUseAble) {
      this.isUseAble = isUseAble;
   }

   public String getActionStartName() {
      return this.actionStartName;
   }

   public void setActionStartName(String actionStartName) {
      this.actionStartName = actionStartName;
   }

   public String getAcitonEndName() {
      return this.acitonEndName;
   }

   public void setAcitonEndName(String acitonEndName) {
      this.acitonEndName = acitonEndName;
   }
}
