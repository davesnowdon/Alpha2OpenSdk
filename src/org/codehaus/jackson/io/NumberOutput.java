package org.codehaus.jackson.io;

public final class NumberOutput {
   private static final char NULL_CHAR = '\u0000';
   private static int MILLION = 1000000;
   private static int BILLION = 1000000000;
   private static long TEN_BILLION_L = 10000000000L;
   private static long THOUSAND_L = 1000L;
   private static long MIN_INT_AS_LONG = -2147483648L;
   private static long MAX_INT_AS_LONG = 2147483647L;
   static final String SMALLEST_LONG = String.valueOf(-9223372036854775808L);
   static final char[] LEADING_TRIPLETS = new char[4000];
   static final char[] FULL_TRIPLETS = new char[4000];
   static final byte[] FULL_TRIPLETS_B;
   static final String[] sSmallIntStrs;
   static final String[] sSmallIntStrs2;

   public NumberOutput() {
   }

   public static int outputInt(int value, char[] buffer, int offset) {
      if (value < 0) {
         if (value == -2147483648) {
            return outputLong((long)value, buffer, offset);
         }

         buffer[offset++] = '-';
         value = -value;
      }

      if (value < MILLION) {
         if (value < 1000) {
            if (value < 10) {
               buffer[offset++] = (char)(48 + value);
            } else {
               offset = outputLeadingTriplet(value, buffer, offset);
            }
         } else {
            int thousands = value / 1000;
            value -= thousands * 1000;
            offset = outputLeadingTriplet(thousands, buffer, offset);
            offset = outputFullTriplet(value, buffer, offset);
         }

         return offset;
      } else {
         boolean hasBillions = value >= BILLION;
         if (hasBillions) {
            value -= BILLION;
            if (value >= BILLION) {
               value -= BILLION;
               buffer[offset++] = '2';
            } else {
               buffer[offset++] = '1';
            }
         }

         int newValue = value / 1000;
         int ones = value - newValue * 1000;
         value = newValue;
         newValue /= 1000;
         int thousands = value - newValue * 1000;
         if (hasBillions) {
            offset = outputFullTriplet(newValue, buffer, offset);
         } else {
            offset = outputLeadingTriplet(newValue, buffer, offset);
         }

         offset = outputFullTriplet(thousands, buffer, offset);
         offset = outputFullTriplet(ones, buffer, offset);
         return offset;
      }
   }

   public static int outputInt(int value, byte[] buffer, int offset) {
      if (value < 0) {
         if (value == -2147483648) {
            return outputLong((long)value, buffer, offset);
         }

         buffer[offset++] = 45;
         value = -value;
      }

      if (value < MILLION) {
         if (value < 1000) {
            if (value < 10) {
               buffer[offset++] = (byte)(48 + value);
            } else {
               offset = outputLeadingTriplet(value, buffer, offset);
            }
         } else {
            int thousands = value / 1000;
            value -= thousands * 1000;
            offset = outputLeadingTriplet(thousands, buffer, offset);
            offset = outputFullTriplet(value, buffer, offset);
         }

         return offset;
      } else {
         boolean hasBillions = value >= BILLION;
         if (hasBillions) {
            value -= BILLION;
            if (value >= BILLION) {
               value -= BILLION;
               buffer[offset++] = 50;
            } else {
               buffer[offset++] = 49;
            }
         }

         int newValue = value / 1000;
         int ones = value - newValue * 1000;
         value = newValue;
         newValue /= 1000;
         int thousands = value - newValue * 1000;
         if (hasBillions) {
            offset = outputFullTriplet(newValue, buffer, offset);
         } else {
            offset = outputLeadingTriplet(newValue, buffer, offset);
         }

         offset = outputFullTriplet(thousands, buffer, offset);
         offset = outputFullTriplet(ones, buffer, offset);
         return offset;
      }
   }

   public static int outputLong(long value, char[] buffer, int offset) {
      int origOffset;
      if (value < 0L) {
         if (value > MIN_INT_AS_LONG) {
            return outputInt((int)value, buffer, offset);
         }

         if (value == -9223372036854775808L) {
            origOffset = SMALLEST_LONG.length();
            SMALLEST_LONG.getChars(0, origOffset, buffer, offset);
            return offset + origOffset;
         }

         buffer[offset++] = '-';
         value = -value;
      } else if (value <= MAX_INT_AS_LONG) {
         return outputInt((int)value, buffer, offset);
      }

      origOffset = offset;
      offset += calcLongStrLength(value);

      int ptr;
      long newValue;
      int triplet;
      for(ptr = offset; value > MAX_INT_AS_LONG; value = newValue) {
         ptr -= 3;
         newValue = value / THOUSAND_L;
         triplet = (int)(value - newValue * THOUSAND_L);
         outputFullTriplet(triplet, buffer, ptr);
      }

      int newValue;
      int ivalue;
      for(ivalue = (int)value; ivalue >= 1000; ivalue = newValue) {
         ptr -= 3;
         newValue = ivalue / 1000;
         triplet = ivalue - newValue * 1000;
         outputFullTriplet(triplet, buffer, ptr);
      }

      outputLeadingTriplet(ivalue, buffer, origOffset);
      return offset;
   }

   public static int outputLong(long value, byte[] buffer, int offset) {
      int origOffset;
      int i;
      if (value < 0L) {
         if (value > MIN_INT_AS_LONG) {
            return outputInt((int)value, buffer, offset);
         }

         if (value == -9223372036854775808L) {
            origOffset = SMALLEST_LONG.length();

            for(i = 0; i < origOffset; ++i) {
               buffer[offset++] = (byte)SMALLEST_LONG.charAt(i);
            }

            return offset;
         }

         buffer[offset++] = 45;
         value = -value;
      } else if (value <= MAX_INT_AS_LONG) {
         return outputInt((int)value, buffer, offset);
      }

      origOffset = offset;
      offset += calcLongStrLength(value);

      long newValue;
      int triplet;
      for(i = offset; value > MAX_INT_AS_LONG; value = newValue) {
         i -= 3;
         newValue = value / THOUSAND_L;
         triplet = (int)(value - newValue * THOUSAND_L);
         outputFullTriplet(triplet, buffer, i);
      }

      int newValue;
      int ivalue;
      for(ivalue = (int)value; ivalue >= 1000; ivalue = newValue) {
         i -= 3;
         newValue = ivalue / 1000;
         triplet = ivalue - newValue * 1000;
         outputFullTriplet(triplet, buffer, i);
      }

      outputLeadingTriplet(ivalue, buffer, origOffset);
      return offset;
   }

   public static String toString(int value) {
      if (value < sSmallIntStrs.length) {
         if (value >= 0) {
            return sSmallIntStrs[value];
         }

         int v2 = -value - 1;
         if (v2 < sSmallIntStrs2.length) {
            return sSmallIntStrs2[v2];
         }
      }

      return Integer.toString(value);
   }

   public static String toString(long value) {
      return value <= 2147483647L && value >= -2147483648L ? toString((int)value) : Long.toString(value);
   }

   public static String toString(double value) {
      return Double.toString(value);
   }

   private static int outputLeadingTriplet(int triplet, char[] buffer, int offset) {
      int digitOffset = triplet << 2;
      char c = LEADING_TRIPLETS[digitOffset++];
      if (c != 0) {
         buffer[offset++] = c;
      }

      c = LEADING_TRIPLETS[digitOffset++];
      if (c != 0) {
         buffer[offset++] = c;
      }

      buffer[offset++] = LEADING_TRIPLETS[digitOffset];
      return offset;
   }

   private static int outputLeadingTriplet(int triplet, byte[] buffer, int offset) {
      int digitOffset = triplet << 2;
      char c = LEADING_TRIPLETS[digitOffset++];
      if (c != 0) {
         buffer[offset++] = (byte)c;
      }

      c = LEADING_TRIPLETS[digitOffset++];
      if (c != 0) {
         buffer[offset++] = (byte)c;
      }

      buffer[offset++] = (byte)LEADING_TRIPLETS[digitOffset];
      return offset;
   }

   private static int outputFullTriplet(int triplet, char[] buffer, int offset) {
      int digitOffset = triplet << 2;
      buffer[offset++] = FULL_TRIPLETS[digitOffset++];
      buffer[offset++] = FULL_TRIPLETS[digitOffset++];
      buffer[offset++] = FULL_TRIPLETS[digitOffset];
      return offset;
   }

   private static int outputFullTriplet(int triplet, byte[] buffer, int offset) {
      int digitOffset = triplet << 2;
      buffer[offset++] = FULL_TRIPLETS_B[digitOffset++];
      buffer[offset++] = FULL_TRIPLETS_B[digitOffset++];
      buffer[offset++] = FULL_TRIPLETS_B[digitOffset];
      return offset;
   }

   private static int calcLongStrLength(long posValue) {
      int len = 10;

      for(long comp = TEN_BILLION_L; posValue >= comp && len != 19; comp = (comp << 3) + (comp << 1)) {
         ++len;
      }

      return len;
   }

   static {
      int ix = 0;

      for(int i1 = 0; i1 < 10; ++i1) {
         char f1 = (char)(48 + i1);
         char l1 = i1 == 0 ? 0 : f1;

         for(int i2 = 0; i2 < 10; ++i2) {
            char f2 = (char)(48 + i2);
            char l2 = i1 == 0 && i2 == 0 ? 0 : f2;

            for(int i3 = 0; i3 < 10; ++i3) {
               char f3 = (char)(48 + i3);
               LEADING_TRIPLETS[ix] = l1;
               LEADING_TRIPLETS[ix + 1] = l2;
               LEADING_TRIPLETS[ix + 2] = f3;
               FULL_TRIPLETS[ix] = f1;
               FULL_TRIPLETS[ix + 1] = f2;
               FULL_TRIPLETS[ix + 2] = f3;
               ix += 4;
            }
         }
      }

      FULL_TRIPLETS_B = new byte[4000];

      for(ix = 0; ix < 4000; ++ix) {
         FULL_TRIPLETS_B[ix] = (byte)FULL_TRIPLETS[ix];
      }

      sSmallIntStrs = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
      sSmallIntStrs2 = new String[]{"-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10"};
   }
}
