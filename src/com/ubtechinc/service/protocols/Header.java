package com.ubtechinc.service.protocols;

import com.ubtechinc.tools.PacketData;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Header implements Serializable {
   private static final long serialVersionUID = 1L;
   private short mFlag;
   private int mLength;
   private short mVersion;
   private short mExtInfo;
   private byte[] msg;
   private int index;
   private int len;
   private Header.HERADER_STATE mState;

   public void packetHeader(PacketData packet) {
      packet.putShort(this.mFlag);
      packet.putInt(this.mLength);
      packet.putShort(this.mVersion);
      packet.putShort(this.mExtInfo);
      packet.putBytes(this.msg);
   }

   public Header() {
      this.mState = Header.HERADER_STATE.FLAG1;
   }

   public boolean setData(byte data) {
      boolean bRet = false;
      switch(this.mState) {
      case FLAG1:
         this.mFlag = (short)(data & 255);
         this.mState = Header.HERADER_STATE.FLAG2;
         break;
      case FLAG2:
         this.mFlag = (short)(data << 8 | this.mFlag);
         this.setmFlag(this.mFlag);
         this.mState = Header.HERADER_STATE.LENGTH1;
         break;
      case LENGTH1:
         this.mLength = data & 255;
         this.mState = Header.HERADER_STATE.LENGTH2;
         break;
      case LENGTH2:
         this.mLength |= data << 8 & '\uff00';
         this.mState = Header.HERADER_STATE.LENGTH3;
         break;
      case LENGTH3:
         this.mLength |= data << 24 >>> 8;
         this.mState = Header.HERADER_STATE.LENGTH4;
         break;
      case LENGTH4:
         this.mLength |= data << 24;
         this.setmLength(this.mLength);
         this.len = this.getmLength() - 4;
         this.msg = new byte[this.len];
         this.mState = Header.HERADER_STATE.VERSION1;
         break;
      case VERSION1:
         this.mVersion = (short)(data & 255);
         this.mState = Header.HERADER_STATE.VERSION2;
         break;
      case VERSION2:
         this.mVersion = (short)(data << 8 | this.mVersion);
         this.setmVerSion(this.mVersion);
         this.mState = Header.HERADER_STATE.EXTINFO1;
         break;
      case EXTINFO1:
         this.mExtInfo = (short)(data & 255);
         this.mState = Header.HERADER_STATE.EXTINFO2;
         break;
      case EXTINFO2:
         this.mExtInfo = (short)(data << 8 | this.mExtInfo);
         this.setmExtInfo(this.mExtInfo);
         this.mState = Header.HERADER_STATE.MSG;
         if (this.len == 0) {
            this.mState = Header.HERADER_STATE.FLAG1;
            this.index = 0;
            this.len = 0;
            bRet = true;
         }
         break;
      case MSG:
         this.msg[this.index++] = data;
         if (this.index == this.len) {
            this.mState = Header.HERADER_STATE.FLAG1;
            this.index = 0;
            this.len = 0;
            bRet = true;
         }
      }

      return bRet;
   }

   public int readSocketInputStream(DataInputStream dis) {
      try {
         byte[] bys = new byte[2];
         if (dis.available() == 0) {
            return 0;
         } else {
            dis.read(bys);
            ByteBuffer buffer = ByteBuffer.wrap(bys, 0, 2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            this.setmFlag(buffer.getShort());
            if (this.getmFlag() != 4660) {
               return 0;
            } else {
               bys = new byte[4];
               dis.read(bys);
               buffer = ByteBuffer.wrap(bys, 0, 4);
               buffer.order(ByteOrder.LITTLE_ENDIAN);
               this.setmLength(buffer.getInt());
               if (this.getmLength() < 4) {
                  return 0;
               } else {
                  bys = new byte[2];
                  dis.read(bys);
                  buffer = ByteBuffer.wrap(bys, 0, 2);
                  buffer.order(ByteOrder.LITTLE_ENDIAN);
                  this.setmVerSion(buffer.getShort());
                  if (this.getmVerSion() != 1) {
                     return 0;
                  } else {
                     bys = new byte[2];
                     dis.read(bys);
                     buffer = ByteBuffer.wrap(bys, 0, 2);
                     buffer.order(ByteOrder.LITTLE_ENDIAN);
                     this.setmExtInfo(buffer.getShort());
                     int len = this.getmLength() - 4;
                     bys = new byte[len];

                     for(int i = 0; i < len; ++i) {
                        bys[i] = dis.readByte();
                     }

                     this.setMsg(bys);
                     return 1;
                  }
               }
            }
         }
      } catch (SocketTimeoutException var6) {
         var6.printStackTrace();
         return -2;
      } catch (SocketException var7) {
         var7.printStackTrace();
         return -3;
      } catch (IOException var8) {
         var8.printStackTrace();
         return 0;
      } catch (Exception var9) {
         var9.printStackTrace();
         return -1;
      }
   }

   public short getmFlag() {
      return this.mFlag;
   }

   public void setmFlag(short mFlag) {
      this.mFlag = mFlag;
   }

   public int getmLength() {
      return this.mLength;
   }

   public void setmLength(int mLength) {
      this.mLength = mLength;
   }

   public short getmExtInfo() {
      return this.mExtInfo;
   }

   public void setmExtInfo(short mExtInfo) {
      this.mExtInfo = mExtInfo;
   }

   public short getmVerSion() {
      return this.mVersion;
   }

   public void setmVerSion(short mVerSion) {
      this.mVersion = mVerSion;
   }

   public byte[] getMsg() {
      return this.msg;
   }

   public void setMsg(byte[] msg) {
      this.msg = msg;
   }

   private static enum HERADER_STATE {
      FLAG1,
      FLAG2,
      LENGTH1,
      LENGTH2,
      LENGTH3,
      LENGTH4,
      VERSION1,
      VERSION2,
      EXTINFO1,
      EXTINFO2,
      MSG;

      private HERADER_STATE() {
      }
   }
}
