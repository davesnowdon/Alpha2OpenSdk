package com.ubtechinc.developer;

import com.ubtechinc.tools.PacketData;

public class DeveloperEarLedData {
   private int mRunTime;
   private int mLeftLed;
   private int mRightLed;
   private int mBright;
   private int mLedUpTime;
   private int mLedDownTime;

   public DeveloperEarLedData() {
   }

   public int getmRunTime() {
      return this.mRunTime;
   }

   public void setmRunTime(int mRunTime) {
      this.mRunTime = mRunTime;
   }

   public int getmLeftLed() {
      return this.mLeftLed;
   }

   public void setmLeftLed(int mLeftLed) {
      this.mLeftLed = mLeftLed;
   }

   public int getmRightLed() {
      return this.mRightLed;
   }

   public void setmRightLed(int mRightLed) {
      this.mRightLed = mRightLed;
   }

   public int getmBright() {
      return this.mBright;
   }

   public void setmBright(int mBright) {
      this.mBright = mBright;
   }

   public int getmLedUpTime() {
      return this.mLedUpTime;
   }

   public void setmLedUpTime(int mLedUpTime) {
      this.mLedUpTime = mLedUpTime;
   }

   public int getmLedDownTime() {
      return this.mLedDownTime;
   }

   public void setmLedDownTime(int mLedDownTime) {
      this.mLedDownTime = mLedDownTime;
   }

   public byte[] getPlayData() {
      PacketData packetData = new PacketData(4);
      packetData.putByte((byte)(this.mLeftLed & 255));
      packetData.putByte((byte)(this.mRightLed & 255));
      packetData.putByte((byte)(this.mBright & 255));
      packetData.putShort_((short)(this.mLedUpTime & '\uffff'));
      packetData.putShort_((short)(this.mLedDownTime & '\uffff'));
      short nTotalTime = (short)this.mRunTime;
      packetData.putShort_(nTotalTime);
      return packetData.getBuffer();
   }
}
