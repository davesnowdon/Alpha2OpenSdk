package com.ubtechinc.tools;

public class ByteHelper {
   public ByteHelper() {
   }

   public static byte[] int2byte(int res) {
      byte[] targets = new byte[]{(byte)(res & 255), (byte)(res >> 8 & 255), (byte)(res >> 16 & 255), (byte)(res >>> 24)};

      for(int i = 0; i < targets.length; ++i) {
         System.out.println(targets[i]);
      }

      return targets;
   }

   public static int byte2int(byte[] res) {
      int targets = res[0] & 255;
      targets |= res[1] << 8 & '\uff00';
      targets |= res[2] << 24 >>> 8;
      targets |= res[3] << 24;
      System.out.println(targets);
      int x = res[0] & 255 | res[1] << 8 & '\uff00' | res[2] << 24 >>> 8 | res[3] << 24;
      return x;
   }

   public static byte[] int2byte2(int res) {
      byte[] targets = new byte[]{(byte)(res >> 8 & 255), (byte)(res & 255)};
      return targets;
   }

   public static int byte2int2(byte[] res) {
      int x = res[1] & 255 | res[0] << 8 & '\uff00';
      return x;
   }
}
