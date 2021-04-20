package org.codehaus.jackson.io;

public final class NumberInput {
   public static final String NASTY_SMALL_DOUBLE = "2.2250738585072012e-308";
   static final long L_BILLION = 1000000000L;
   static final String MIN_LONG_STR_NO_SIGN = String.valueOf(-9223372036854775808L).substring(1);
   static final String MAX_LONG_STR = String.valueOf(9223372036854775807L);

   public NumberInput() {
   }

   public static final int parseInt(char[] digitChars, int offset, int len) {
      int num = digitChars[offset] - 48;
      len += offset;
      ++offset;
      if (offset < len) {
         num = num * 10 + (digitChars[offset] - 48);
         ++offset;
         if (offset < len) {
            num = num * 10 + (digitChars[offset] - 48);
            ++offset;
            if (offset < len) {
               num = num * 10 + (digitChars[offset] - 48);
               ++offset;
               if (offset < len) {
                  num = num * 10 + (digitChars[offset] - 48);
                  ++offset;
                  if (offset < len) {
                     num = num * 10 + (digitChars[offset] - 48);
                     ++offset;
                     if (offset < len) {
                        num = num * 10 + (digitChars[offset] - 48);
                        ++offset;
                        if (offset < len) {
                           num = num * 10 + (digitChars[offset] - 48);
                           ++offset;
                           if (offset < len) {
                              num = num * 10 + (digitChars[offset] - 48);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return num;
   }

   public static final int parseInt(String str) {
      char c = str.charAt(0);
      int length = str.length();
      boolean negative = c == '-';
      int offset = 1;
      if (negative) {
         if (length == 1 || length > 10) {
            return Integer.parseInt(str);
         }

         c = str.charAt(offset++);
      } else if (length > 9) {
         return Integer.parseInt(str);
      }

      if (c <= '9' && c >= '0') {
         int num = c - 48;
         if (offset < length) {
            c = str.charAt(offset++);
            if (c > '9' || c < '0') {
               return Integer.parseInt(str);
            }

            num = num * 10 + (c - 48);
            if (offset < length) {
               c = str.charAt(offset++);
               if (c <= '9' && c >= '0') {
                  num = num * 10 + (c - 48);
                  if (offset >= length) {
                     return negative ? -num : num;
                  }

                  do {
                     c = str.charAt(offset++);
                     if (c > '9' || c < '0') {
                        return Integer.parseInt(str);
                     }

                     num = num * 10 + (c - 48);
                  } while(offset < length);

                  return negative ? -num : num;
               }

               return Integer.parseInt(str);
            }
         }

         return negative ? -num : num;
      } else {
         return Integer.parseInt(str);
      }
   }

   public static final long parseLong(char[] digitChars, int offset, int len) {
      int len1 = len - 9;
      long val = (long)parseInt(digitChars, offset, len1) * 1000000000L;
      return val + (long)parseInt(digitChars, offset + len1, 9);
   }

   public static final long parseLong(String str) {
      int length = str.length();
      return length <= 9 ? (long)parseInt(str) : Long.parseLong(str);
   }

   public static final boolean inLongRange(char[] digitChars, int offset, int len, boolean negative) {
      String cmpStr = negative ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
      int cmpLen = cmpStr.length();
      if (len < cmpLen) {
         return true;
      } else if (len > cmpLen) {
         return false;
      } else {
         for(int i = 0; i < cmpLen; ++i) {
            int diff = digitChars[offset + i] - cmpStr.charAt(i);
            if (diff != 0) {
               return diff < 0;
            }
         }

         return true;
      }
   }

   public static final boolean inLongRange(String numberStr, boolean negative) {
      String cmpStr = negative ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
      int cmpLen = cmpStr.length();
      int actualLen = numberStr.length();
      if (actualLen < cmpLen) {
         return true;
      } else if (actualLen > cmpLen) {
         return false;
      } else {
         for(int i = 0; i < cmpLen; ++i) {
            int diff = numberStr.charAt(i) - cmpStr.charAt(i);
            if (diff != 0) {
               return diff < 0;
            }
         }

         return true;
      }
   }

   public static int parseAsInt(String input, int defaultValue) {
      if (input == null) {
         return defaultValue;
      } else {
         input = input.trim();
         int len = input.length();
         if (len == 0) {
            return defaultValue;
         } else {
            int i = 0;
            char c;
            if (i < len) {
               c = input.charAt(0);
               if (c == '+') {
                  input = input.substring(1);
                  len = input.length();
               } else if (c == '-') {
                  ++i;
               }
            }

            while(i < len) {
               c = input.charAt(i);
               if (c > '9' || c < '0') {
                  try {
                     return (int)parseDouble(input);
                  } catch (NumberFormatException var6) {
                     return defaultValue;
                  }
               }

               ++i;
            }

            try {
               return Integer.parseInt(input);
            } catch (NumberFormatException var7) {
               return defaultValue;
            }
         }
      }
   }

   public static long parseAsLong(String input, long defaultValue) {
      if (input == null) {
         return defaultValue;
      } else {
         input = input.trim();
         int len = input.length();
         if (len == 0) {
            return defaultValue;
         } else {
            int i = 0;
            char c;
            if (i < len) {
               c = input.charAt(0);
               if (c == '+') {
                  input = input.substring(1);
                  len = input.length();
               } else if (c == '-') {
                  ++i;
               }
            }

            while(i < len) {
               c = input.charAt(i);
               if (c > '9' || c < '0') {
                  try {
                     return (long)parseDouble(input);
                  } catch (NumberFormatException var7) {
                     return defaultValue;
                  }
               }

               ++i;
            }

            try {
               return Long.parseLong(input);
            } catch (NumberFormatException var8) {
               return defaultValue;
            }
         }
      }
   }

   public static double parseAsDouble(String input, double defaultValue) {
      if (input == null) {
         return defaultValue;
      } else {
         input = input.trim();
         int len = input.length();
         if (len == 0) {
            return defaultValue;
         } else {
            try {
               return parseDouble(input);
            } catch (NumberFormatException var5) {
               return defaultValue;
            }
         }
      }
   }

   public static final double parseDouble(String numStr) throws NumberFormatException {
      return "2.2250738585072012e-308".equals(numStr) ? 2.2250738585072014E-308D : Double.parseDouble(numStr);
   }
}
