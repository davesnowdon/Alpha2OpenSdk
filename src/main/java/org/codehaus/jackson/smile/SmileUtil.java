package org.codehaus.jackson.smile;

public class SmileUtil {
   public SmileUtil() {
   }

   public static int zigzagEncode(int input) {
      return input < 0 ? ~(input << 1) : input << 1;
   }

   public static int zigzagDecode(int encoded) {
      return (encoded & 1) == 0 ? encoded >>> 1 : ~(encoded >>> 1);
   }

   public static long zigzagEncode(long input) {
      return input < 0L ? ~(input << 1) : input << 1;
   }

   public static long zigzagDecode(long encoded) {
      return (encoded & 1L) == 0L ? encoded >>> 1 : ~(encoded >>> 1);
   }
}
