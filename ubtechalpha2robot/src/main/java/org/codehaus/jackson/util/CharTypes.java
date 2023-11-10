package org.codehaus.jackson.util;

import java.util.Arrays;

public final class CharTypes {
   private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
   private static final byte[] HEX_BYTES;
   static final int[] sInputCodes;
   static final int[] sInputCodesUtf8;
   static final int[] sInputCodesJsNames;
   static final int[] sInputCodesUtf8JsNames;
   static final int[] sInputCodesComment;
   static final int[] sOutputEscapes128;
   static final int[] sHexValues;

   public CharTypes() {
   }

   public static final int[] getInputCodeLatin1() {
      return sInputCodes;
   }

   public static final int[] getInputCodeUtf8() {
      return sInputCodesUtf8;
   }

   public static final int[] getInputCodeLatin1JsNames() {
      return sInputCodesJsNames;
   }

   public static final int[] getInputCodeUtf8JsNames() {
      return sInputCodesUtf8JsNames;
   }

   public static final int[] getInputCodeComment() {
      return sInputCodesComment;
   }

   public static final int[] get7BitOutputEscapes() {
      return sOutputEscapes128;
   }

   public static int charToHex(int ch) {
      return ch > 127 ? -1 : sHexValues[ch];
   }

   public static void appendQuoted(StringBuilder sb, String content) {
      int[] escCodes = sOutputEscapes128;
      int escLen = escCodes.length;
      int i = 0;

      for(int len = content.length(); i < len; ++i) {
         char c = content.charAt(i);
         if (c < escLen && escCodes[c] != 0) {
            sb.append('\\');
            int escCode = escCodes[c];
            if (escCode < 0) {
               sb.append('u');
               sb.append('0');
               sb.append('0');
               int value = -(escCode + 1);
               sb.append(HEX_CHARS[value >> 4]);
               sb.append(HEX_CHARS[value & 15]);
            } else {
               sb.append((char)escCode);
            }
         } else {
            sb.append(c);
         }
      }

   }

   public static char[] copyHexChars() {
      return (char[])((char[])HEX_CHARS.clone());
   }

   public static byte[] copyHexBytes() {
      return (byte[])((byte[])HEX_BYTES.clone());
   }

   static {
      int i = HEX_CHARS.length;
      HEX_BYTES = new byte[i];

      int i;
      for(i = 0; i < i; ++i) {
         HEX_BYTES[i] = (byte)HEX_CHARS[i];
      }

      int[] table = new int[256];

      for(i = 0; i < 32; ++i) {
         table[i] = -1;
      }

      table[34] = 1;
      table[92] = 1;
      sInputCodes = table;
      table = new int[sInputCodes.length];
      System.arraycopy(sInputCodes, 0, table, 0, sInputCodes.length);

      for(i = 128; i < 256; ++i) {
         byte code;
         if ((i & 224) == 192) {
            code = 2;
         } else if ((i & 240) == 224) {
            code = 3;
         } else if ((i & 248) == 240) {
            code = 4;
         } else {
            code = -1;
         }

         table[i] = code;
      }

      sInputCodesUtf8 = table;
      table = new int[256];
      Arrays.fill(table, -1);

      for(i = 33; i < 256; ++i) {
         if (Character.isJavaIdentifierPart((char)i)) {
            table[i] = 0;
         }
      }

      table[64] = 0;
      table[35] = 0;
      table[42] = 0;
      table[45] = 0;
      table[43] = 0;
      sInputCodesJsNames = table;
      table = new int[256];
      System.arraycopy(sInputCodesJsNames, 0, table, 0, sInputCodesJsNames.length);
      Arrays.fill(table, 128, 128, 0);
      sInputCodesUtf8JsNames = table;
      sInputCodesComment = new int[256];
      System.arraycopy(sInputCodesUtf8, 128, sInputCodesComment, 128, 128);
      Arrays.fill(sInputCodesComment, 0, 32, -1);
      sInputCodesComment[9] = 0;
      sInputCodesComment[10] = 10;
      sInputCodesComment[13] = 13;
      sInputCodesComment[42] = 42;
      table = new int[128];

      for(i = 0; i < 32; ++i) {
         table[i] = -1;
      }

      table[34] = 34;
      table[92] = 92;
      table[8] = 98;
      table[9] = 116;
      table[12] = 102;
      table[10] = 110;
      table[13] = 114;
      sOutputEscapes128 = table;
      sHexValues = new int[128];
      Arrays.fill(sHexValues, -1);

      for(i = 0; i < 10; sHexValues[48 + i] = i++) {
      }

      for(i = 0; i < 6; ++i) {
         sHexValues[97 + i] = 10 + i;
         sHexValues[65 + i] = 10 + i;
      }

   }
}
