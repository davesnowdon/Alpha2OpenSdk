package org.codehaus.jackson.smile;

public final class SmileConstants {
   public static final int MAX_SHORT_VALUE_STRING_BYTES = 64;
   public static final int MAX_SHORT_NAME_ASCII_BYTES = 64;
   public static final int MAX_SHORT_NAME_UNICODE_BYTES = 56;
   public static final int MAX_SHARED_NAMES = 1024;
   public static final int MAX_SHARED_STRING_VALUES = 1024;
   public static final int MAX_SHARED_STRING_LENGTH_BYTES = 65;
   public static final int MIN_BUFFER_FOR_POSSIBLE_SHORT_STRING = 196;
   public static final int INT_MARKER_END_OF_STRING = 252;
   public static final byte BYTE_MARKER_END_OF_STRING = -4;
   public static final byte BYTE_MARKER_END_OF_CONTENT = -1;
   public static final byte HEADER_BYTE_1 = 58;
   public static final byte HEADER_BYTE_2 = 41;
   public static final byte HEADER_BYTE_3 = 10;
   public static final int HEADER_VERSION_0 = 0;
   public static final byte HEADER_BYTE_4 = 0;
   public static final int HEADER_BIT_HAS_SHARED_NAMES = 1;
   public static final int HEADER_BIT_HAS_SHARED_STRING_VALUES = 2;
   public static final int HEADER_BIT_HAS_RAW_BINARY = 4;
   public static final int TOKEN_PREFIX_SHARED_STRING_SHORT = 0;
   public static final int TOKEN_PREFIX_TINY_ASCII = 64;
   public static final int TOKEN_PREFIX_SMALL_ASCII = 96;
   public static final int TOKEN_PREFIX_TINY_UNICODE = 128;
   public static final int TOKEN_PREFIX_SHORT_UNICODE = 160;
   public static final int TOKEN_PREFIX_SMALL_INT = 192;
   public static final int TOKEN_PREFIX_MISC_OTHER = 224;
   public static final byte TOKEN_LITERAL_EMPTY_STRING = 32;
   public static final byte TOKEN_LITERAL_NULL = 33;
   public static final byte TOKEN_LITERAL_FALSE = 34;
   public static final byte TOKEN_LITERAL_TRUE = 35;
   public static final byte TOKEN_LITERAL_START_ARRAY = -8;
   public static final byte TOKEN_LITERAL_END_ARRAY = -7;
   public static final byte TOKEN_LITERAL_START_OBJECT = -6;
   public static final byte TOKEN_LITERAL_END_OBJECT = -5;
   public static final int TOKEN_MISC_INTEGER = 36;
   public static final int TOKEN_MISC_FP = 40;
   public static final int TOKEN_MISC_LONG_TEXT_ASCII = 224;
   public static final int TOKEN_MISC_LONG_TEXT_UNICODE = 228;
   public static final int TOKEN_MISC_BINARY_7BIT = 232;
   public static final int TOKEN_MISC_SHARED_STRING_LONG = 236;
   public static final int TOKEN_MISC_BINARY_RAW = 253;
   public static final int TOKEN_MISC_INTEGER_32 = 0;
   public static final int TOKEN_MISC_INTEGER_64 = 1;
   public static final int TOKEN_MISC_INTEGER_BIG = 2;
   public static final int TOKEN_MISC_FLOAT_32 = 0;
   public static final int TOKEN_MISC_FLOAT_64 = 1;
   public static final int TOKEN_MISC_FLOAT_BIG = 2;
   public static final byte TOKEN_KEY_EMPTY_STRING = 32;
   public static final int TOKEN_PREFIX_KEY_SHARED_LONG = 48;
   public static final byte TOKEN_KEY_LONG_STRING = 52;
   public static final int TOKEN_PREFIX_KEY_SHARED_SHORT = 64;
   public static final int TOKEN_PREFIX_KEY_ASCII = 128;
   public static final int TOKEN_PREFIX_KEY_UNICODE = 192;
   public static final int[] sUtf8UnitLengths;

   public SmileConstants() {
   }

   static {
      int[] table = new int[256];

      for(int c = 128; c < 256; ++c) {
         byte code;
         if ((c & 224) == 192) {
            code = 1;
         } else if ((c & 240) == 224) {
            code = 2;
         } else if ((c & 248) == 240) {
            code = 3;
         } else {
            code = -1;
         }

         table[c] = code;
      }

      sUtf8UnitLengths = table;
   }
}
