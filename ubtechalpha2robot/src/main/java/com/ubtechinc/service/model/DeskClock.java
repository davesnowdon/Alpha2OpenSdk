package com.ubtechinc.service.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class DeskClock implements Serializable, Parcelable {
   @Index(0)
   private int type;
   @Index(1)
   private int _id;
   @Index(2)
   private int hour;
   @Index(3)
   private int minutes;
   @Index(4)
   private int daysofweek;
   @Index(5)
   private long alarmtime;
   @Index(6)
   private boolean enabled;
   @Index(7)
   private int vibrate;
   @Index(8)
   private String message;
   @Index(9)
   private String alert;
   @Index(10)
   private String dtstart;
   @Index(11)
   private boolean iscomplete;
   public static final Creator<DeskClock> CREATOR = new Creator<DeskClock>() {
      public DeskClock createFromParcel(Parcel p) {
         return new DeskClock(p);
      }

      public DeskClock[] newArray(int size) {
         return new DeskClock[size];
      }
   };

   public boolean isIscomplete() {
      return this.iscomplete;
   }

   public void setIscomplete(boolean iscomplete) {
      this.iscomplete = iscomplete;
   }

   public String getDtstart() {
      return this.dtstart;
   }

   public void setDtstart(String dtstart) {
      this.dtstart = dtstart;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public int get_id() {
      return this._id;
   }

   public void set_id(int _id) {
      this._id = _id;
   }

   public int getHour() {
      return this.hour;
   }

   public void setHour(int hour) {
      this.hour = hour;
   }

   public int getMinutes() {
      return this.minutes;
   }

   public void setMinutes(int minutes) {
      this.minutes = minutes;
   }

   public int getDaysofweek() {
      return this.daysofweek;
   }

   public void setDaysofweek(int daysofweek) {
      this.daysofweek = daysofweek;
   }

   public long getAlarmtime() {
      return this.alarmtime;
   }

   public void setAlarmtime(long alarmtime) {
      this.alarmtime = alarmtime;
   }

   public int getVibrate() {
      return this.vibrate;
   }

   public void setVibrate(int vibrate) {
      this.vibrate = vibrate;
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getAlert() {
      return this.alert;
   }

   public void setAlert(String alert) {
      this.alert = alert;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public String toString() {
      return " " + this._id + " " + this.hour + " " + this.minutes + " " + this.daysofweek + " " + this.alarmtime + " " + this.enabled + " " + this.vibrate + " " + this.message + " " + this.alert + " DT " + this.dtstart;
   }

   public void writeToParcel(Parcel p, int flags) {
      p.writeInt(this._id);
      p.writeInt(this.enabled ? 1 : 0);
      p.writeInt(this.hour);
      p.writeInt(this.minutes);
      p.writeInt(this.daysofweek);
      p.writeLong(this.alarmtime);
      p.writeInt(this.vibrate);
      p.writeString(this.message);
      p.writeString(this.alert);
      p.writeString(this.dtstart);
      p.writeInt(this.iscomplete ? 1 : 0);
   }

   public int describeContents() {
      return 0;
   }

   public DeskClock() {
   }

   public DeskClock(Parcel p) {
      this._id = p.readInt();
      this.enabled = p.readInt() == 1;
      this.hour = p.readInt();
      this.minutes = p.readInt();
      this.daysofweek = p.readInt();
      this.alarmtime = p.readLong();
      this.vibrate = p.readInt();
      this.message = p.readString();
      this.alert = p.readString();
      this.dtstart = p.readString();
      this.iscomplete = p.readInt() == 1;
   }
}
