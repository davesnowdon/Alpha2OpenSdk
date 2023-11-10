package com.ubtechinc.tools;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class PacketData implements Serializable {
   private int nCapacity;
   private int nPosition;
   public ByteBuffer buffer;

   public PacketData(int Capacity) {
      this.nCapacity = Capacity;
      this.nPosition = 0;
      this.buffer = ByteBuffer.allocate(this.nCapacity);
      this.buffer.clear();
   }

   private boolean Allocate(int nlen) {
      if (this.nPosition + nlen > this.nCapacity) {
         byte[] temp = this.buffer.array();
         this.nCapacity += nlen;
         this.buffer = ByteBuffer.allocate(this.nCapacity);
         this.buffer.put(temp);
         this.buffer.position(this.nPosition);
      }

      return true;
   }

   public void putInt(int n) {
      this.Allocate(4);
      this.buffer.put(toLH(n));
      this.nPosition += 4;
   }

   public void putInt_(int n) {
      this.Allocate(4);
      this.buffer.putInt(n);
      this.nPosition += 4;
   }

   public void putLong(Long n) {
      this.Allocate(8);
      this.buffer.put(toLH(n));
      this.nPosition += 8;
   }

   public void putByte(byte n) {
      this.Allocate(1);
      this.buffer.put(n);
      ++this.nPosition;
   }

   public void putBytes(byte[] n) {
      this.Allocate(n.length);
      this.buffer.put(n);
      this.nPosition += n.length;
   }

   public void putFloat(float n) {
      this.Allocate(4);
      this.buffer.put(toLH(n));
      this.nPosition += 4;
   }

   public int putString(String str) {
      if (str != null && !"".equals(str)) {
         str.trim();
         byte[] buf = stringToBytes(str, str.length());
         if (null != buf) {
            int nLen = buf.length;
            this.putShort((short)nLen);
            this.Allocate(nLen);
            this.buffer.put(buf);
            this.nPosition += nLen;
            return nLen + 2;
         } else {
            this.putShort(Short.valueOf((short)0));
            return 0;
         }
      } else {
         this.putShort(Short.valueOf((short)0));
         return 0;
      }
   }

   private static String toStr(byte[] valArr, int maxLen) {
      int index;
      for(index = 0; index < valArr.length && index < maxLen && valArr[index] != 0; ++index) {
      }

      byte[] temp = new byte[index];
      System.arraycopy(valArr, 0, temp, 0, index);
      return new String(temp);
   }

   public void putShort(Short nData) {
      this.Allocate(2);
      this.buffer.put(toLH(nData));
      this.nPosition += 2;
   }

   public void putShort_(Short nData) {
      this.Allocate(2);
      this.buffer.put(toHH(nData));
      this.nPosition += 2;
   }

   public byte[] getBuffer() {
      byte[] temp = this.buffer.array();
      byte[] buf = new byte[this.nPosition];
      System.arraycopy(temp, 0, buf, 0, this.nPosition);
      return buf;
   }

   public static byte[] toLH(int n) {
      byte[] b = new byte[]{(byte)(n & 255), (byte)(n >> 8 & 255), (byte)(n >> 16 & 255), (byte)(n >> 24 & 255)};
      return b;
   }

   public static byte[] toHL(int n) {
      byte[] b = new byte[]{(byte)(n >> 24 & 255), (byte)(n >> 16 & 255), (byte)(n >> 8 & 255), (byte)(n & 255)};
      return b;
   }

   public static byte[] toLH(long n) {
      byte[] b = new byte[]{(byte)((int)(n & 255L)), (byte)((int)(n >> 8 & 255L)), (byte)((int)(n >> 16 & 255L)), (byte)((int)(n >> 24 & 255L)), (byte)((int)(n >> 32 & 255L)), (byte)((int)(n >> 40 & 255L)), (byte)((int)(n >> 48 & 255L)), (byte)((int)(n >> 56 & 255L))};
      return b;
   }

   private static byte[] toLH(float f) {
      return toLH(Float.floatToRawIntBits(f));
   }

   public static byte[] toLH(short n) {
      byte[] b = new byte[]{(byte)(n & 255), (byte)(n >> 8 & 255)};
      return b;
   }

   public static byte[] toHH(short n) {
      byte[] b = new byte[]{(byte)(n >> 8 & 255), (byte)(n & 255)};
      return b;
   }

   public static byte[] toHH(int n) {
      byte[] b = new byte[]{(byte)(n >> 24 & 255), (byte)(n >> 16 & 255), (byte)(n >> 8 & 255), (byte)(n & 255)};
      return b;
   }

   public static byte[] stringToBytes(String s, int length) {
      while(s.getBytes().length < length) {
         s = s + " ";
      }

      return s.getBytes();
   }

   public static String bytesToString(byte[] b) {
      StringBuffer result = new StringBuffer("");
      int length = b.length;

      for(int i = 0; i < length; ++i) {
         result.append((char)(b[i] & 255));
      }

      return result.toString();
   }

   public static byte[] stringToBytes(String s) {
      try {
         return s.getBytes("UTF-8");
      } catch (UnsupportedEncodingException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static int hBytesToInt(byte[] b) {
      int s = 0;

      for(int i = 0; i < 3; ++i) {
         if (b[i] >= 0) {
            s += b[i];
         } else {
            s = s + 256 + b[i];
         }

         s *= 256;
      }

      if (b[3] >= 0) {
         s += b[3];
      } else {
         s = s + 256 + b[3];
      }

      return s;
   }

   public static int lBytesToInt(byte[] b) {
      int s = 0;

      for(int i = 0; i < 3; ++i) {
         if (b[3 - i] >= 0) {
            s += b[3 - i];
         } else {
            s = s + 256 + b[3 - i];
         }

         s *= 256;
      }

      if (b[0] >= 0) {
         s += b[0];
      } else {
         s = s + 256 + b[0];
      }

      return s;
   }

   public static short hBytesToShort(byte[] b) {
      int s = 0;
      int s;
      if (b[0] >= 0) {
         s = s + b[0];
      } else {
         s = s + 256 + b[0];
      }

      s *= 256;
      if (b[1] >= 0) {
         s += b[1];
      } else {
         s = s + 256 + b[1];
      }

      short result = (short)s;
      return result;
   }

   public static short lBytesToShort(byte[] b) {
      int s = 0;
      int s;
      if (b[1] >= 0) {
         s = s + b[1];
      } else {
         s = s + 256 + b[1];
      }

      s *= 256;
      if (b[0] >= 0) {
         s += b[0];
      } else {
         s = s + 256 + b[0];
      }

      short result = (short)s;
      return result;
   }

   public static float hBytesToFloat(byte[] b) {
      int i = false;
      Float F = new Float(0.0D);
      int i = (((b[0] & 255) << 8 | b[1] & 255) << 8 | b[2] & 255) << 8 | b[3] & 255;
      return Float.intBitsToFloat(i);
   }

   public static float lBytesToFloat(byte[] b) {
      int i = false;
      Float F = new Float(0.0D);
      int i = (((b[3] & 255) << 8 | b[2] & 255) << 8 | b[1] & 255) << 8 | b[0] & 255;
      return Float.intBitsToFloat(i);
   }

   public static byte[] bytesReverseOrder(byte[] b) {
      int length = b.length;
      byte[] result = new byte[length];

      for(int i = 0; i < length; ++i) {
         result[length - i - 1] = b[i];
      }

      return result;
   }

   public static void printBytes(byte[] bb) {
      int length = bb.length;
   }

   public static void logBytes(byte[] bb) {
      int length = bb.length;
      String out = "";

      for(int i = 0; i < length; ++i) {
         out = out + bb + " ";
      }

   }

   public static int reverseInt(int i) {
      int result = hBytesToInt(toLH(i));
      return result;
   }

   public static short reverseShort(short s) {
      short result = hBytesToShort(toLH(s));
      return result;
   }

   public static float reverseFloat(float f) {
      float result = hBytesToFloat(toLH(f));
      return result;
   }
}
