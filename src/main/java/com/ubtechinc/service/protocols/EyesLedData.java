package com.ubtechinc.service.protocols;

import com.ubtechinc.tools.PacketData;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class EyesLedData {
   @Index(0)
   private byte mLeftLed;
   @Index(1)
   private byte mRightLed;
   @Index(2)
   private byte mBright;
   @Index(3)
   private byte mColor;
   @Index(4)
   private int nLightUpTime;
   @Index(5)
   private int nLightDownTime;
   @Index(6)
   private int mRunTime;

   public EyesLedData() {
   }

   public byte getmLeftLed() {
      return this.mLeftLed;
   }

   public void setmLeftLed(byte mLeftLed) {
      this.mLeftLed = mLeftLed;
   }

   public byte getmRightLed() {
      return this.mRightLed;
   }

   public void setmRightLed(byte mRightLed) {
      this.mRightLed = mRightLed;
   }

   public byte getmBright() {
      return this.mBright;
   }

   public void setmBright(byte mBright) {
      this.mBright = mBright;
   }

   public byte getmColor() {
      return this.mColor;
   }

   public void setmColor(byte mColor) {
      this.mColor = mColor;
   }

   public int getnLightUpTime() {
      return this.nLightUpTime;
   }

   public void setnLightUpTime(int nLightUpTime) {
      this.nLightUpTime = nLightUpTime;
   }

   public int getnLightDownTime() {
      return this.nLightDownTime;
   }

   public void setnLightDownTime(int nLightDownTime) {
      this.nLightDownTime = nLightDownTime;
   }

   public int getmRunTime() {
      return this.mRunTime;
   }

   public void setmRunTime(int mRunTime) {
      this.mRunTime = mRunTime;
   }

   public byte[] getPlayData() {
      PacketData packetData = new PacketData(4);
      packetData.putByte(this.mLeftLed);
      packetData.putByte(this.mRightLed);
      packetData.putByte(this.mBright);
      packetData.putByte(this.mColor);
      packetData.putShort_((short)(this.nLightUpTime & '\uffff'));
      packetData.putShort_((short)(this.nLightDownTime & '\uffff'));
      short nTotalTime = (short)this.mRunTime;
      packetData.putShort_(nTotalTime);
      return packetData.getBuffer();
   }
}
