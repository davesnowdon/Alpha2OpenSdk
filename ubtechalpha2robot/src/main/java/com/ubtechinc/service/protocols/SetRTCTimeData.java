package com.ubtechinc.service.protocols;

import java.io.Serializable;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class SetRTCTimeData implements Serializable {
   private static final long serialVersionUID = 2169409831946422538L;
   @Index(0)
   private int year;
   @Index(1)
   private int month;
   @Index(2)
   private int day;
   @Index(3)
   private int week;
   @Index(4)
   private int hour;
   @Index(5)
   private int minute;
   @Index(6)
   private int second;

   public SetRTCTimeData() {
   }

   public int getYear() {
      return this.year;
   }

   public void setYear(int year) {
      this.year = year;
   }

   public int getMonth() {
      return this.month;
   }

   public void setMonth(int month) {
      this.month = month;
   }

   public int getDay() {
      return this.day;
   }

   public void setDay(int day) {
      this.day = day;
   }

   public int getWeek() {
      return this.week;
   }

   public void setWeek(int week) {
      this.week = week;
   }

   public int getHour() {
      return this.hour;
   }

   public void setHour(int hour) {
      this.hour = hour;
   }

   public int getMinute() {
      return this.minute;
   }

   public void setMinute(int minute) {
      this.minute = minute;
   }

   public int getSecond() {
      return this.second;
   }

   public void setSecond(int second) {
      this.second = second;
   }

   public static long getSerialversionuid() {
      return 2169409831946422538L;
   }
}
