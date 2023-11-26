package com.ubtechinc.tools;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ByteHexHelper {
   private static boolean D = false;

   public ByteHexHelper() {
   }

   public static String bytesToHexString(byte[] src) {
      StringBuilder stringBuilder = new StringBuilder("");
      if (src != null && src.length > 0) {
         for(int i = 0; i < src.length; ++i) {
            int v = src[i] & 255;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
               stringBuilder.append(0);
            }

            stringBuilder.append(hv);
            stringBuilder.append(" ");
         }

         return stringBuilder.toString();
      } else {
         return "";
      }
   }

   public static String byteToHexString(byte src) {
      StringBuilder stringBuilder = new StringBuilder("");
      int v = src & 255;
      String hv = Integer.toHexString(v);
      if (hv.length() < 2) {
         stringBuilder.append(0);
      }

      stringBuilder.append(hv);
      return stringBuilder.toString();
   }

   public static int byteToInt(byte src) {
      return src & 255;
   }

   public static byte[] intToHexBytes(int id) {
      String hexString = Integer.toHexString(id);

      for(int len = hexString.length(); len < 2; len = hexString.length()) {
         hexString = "0" + hexString;
      }

      return hexStringToBytes(hexString);
   }

   public static byte[] intToTwoHexBytes(int id) {
      String hexString = Integer.toHexString(id);

      for(int len = hexString.length(); len < 4; len = hexString.length()) {
         hexString = "0" + hexString;
      }

      return hexStringToBytes(hexString);
   }

   public static byte[] intToFourHexBytes(int id) {
      String hexString = Integer.toHexString(id);

      for(int len = hexString.length(); len < 8; len = hexString.length()) {
         hexString = "0" + hexString;
      }

      return hexStringToBytes(hexString);
   }

   public static byte[] intToFourHexBytesTwo(int id) {
      String hexString = Integer.toHexString(id);
      int len = hexString.length();
      if (len < 2) {
         hexString = "0" + hexString;
         len = hexString.length();
      }

      while(len < 8) {
         hexString = hexString + "0";
         len = hexString.length();
      }

      return hexStringToBytes(hexString);
   }

   public static byte intToHexByte(int id) {
      String hexString = Integer.toHexString(id);

      for(int len = hexString.length(); len < 2; len = hexString.length()) {
         hexString = "0" + hexString;
      }

      return hexStringToByte(hexString);
   }

   public static byte[] hexStringToBytes2(String hexString) {
      if (hexString != null && !hexString.equals("")) {
         hexString = hexString.toUpperCase();
         int length = hexString.length() / 2;
         char[] hexChars = hexString.toCharArray();
         byte[] d = new byte[length];

         for(int i = 0; i < length; ++i) {
            int pos = i * 2;
            d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
         }

         return d;
      } else {
         byte[] bytes = new byte[0];
         return bytes;
      }
   }

   public static byte hexStringToByte(String hexString) {
      hexString = hexString.toUpperCase();
      int length = hexString.length() / 2;
      char[] hexChars = hexString.toCharArray();
      byte[] d = new byte[length];

      for(int i = 0; i < length; ++i) {
         int pos = i * 2;
         d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
      }

      return d[0];
   }

   public static byte[] hexStringToBytes(String str) {
      String str1 = str.replace(" ", "");
      System.out.println(str1);
      byte[] d = new byte[str1.length() / 2];

      for(int i = 0; i < str1.length(); ++i) {
         int tmp = str1.substring(i, i + 1).getBytes()[0];
         int c;
         if (tmp > 96) {
            c = (tmp - 97 + 10) * 16;
         } else if (tmp > 64) {
            c = (tmp - 65 + 10) * 16;
         } else {
            c = (tmp - 48) * 16;
         }

         ++i;
         tmp = str1.substring(i, i + 1).getBytes()[0];
         if (tmp > 96) {
            c += tmp - 97 + 10;
         } else if (tmp > 64) {
            c += tmp - 65 + 10;
         } else {
            c += tmp - 48;
         }

         d[i / 2] = (byte)c;
      }

      return d;
   }

   private static byte charToByte(char c) {
      return (byte)"0123456789ABCDEF".indexOf(c);
   }

   public static String XOR(String hex) {
      byte bytes = 0;
      if (hex.length() > 0) {
         for(int i = 0; i < hex.length() / 2; ++i) {
            bytes ^= hexStringToByte(hex.substring(2 * i, 2 * i + 2));
         }
      }

      byte[] bbb = new byte[]{bytes};
      return bytesToHexString(bbb);
   }

   public static String currentData() {
      StringBuffer stringBuffer = new StringBuffer();
      DecimalFormat decimalFormat = new DecimalFormat("00");
      Calendar calendar = Calendar.getInstance();
      String year = decimalFormat.format((long)calendar.get(1));
      String month = decimalFormat.format((long)(calendar.get(2) + 1));
      String day = decimalFormat.format((long)calendar.get(5));
      String hour = decimalFormat.format((long)calendar.get(11));
      String minute = decimalFormat.format((long)calendar.get(12));
      String second = decimalFormat.format((long)calendar.get(13));
      String week = decimalFormat.format((long)(calendar.get(7) - 1));
      stringBuffer.append(year.substring(2, year.length())).append(month).append(day).append(hour).append(minute).append(second).append(week);
      System.out.println(stringBuffer.toString());
      return stringBuffer.toString();
   }

   public static String RandomMethod() {
      int random = (int)(Math.random() * 100.0D);
      String hexString = Integer.toHexString(random);

      for(int len = hexString.length(); len < 2; len = hexString.length()) {
         hexString = "0" + hexString;
      }

      return hexString;
   }

   public static String packLength(String str) {
      String hexLength = Integer.toHexString(str.length() / 2);

      for(int len = hexLength.length(); len < 4; len = hexLength.length()) {
         hexLength = "0" + hexLength;
      }

      return hexLength;
   }

   public static String checkedSite(int site) {
      String hexLength = Integer.toHexString(site);

      for(int len = hexLength.length(); len < 2; len = hexLength.length()) {
         hexLength = "0" + hexLength;
      }

      return hexLength;
   }

   public static String packLength(int dataLen) {
      String hexLength = Integer.toHexString(dataLen);

      for(int len = hexLength.length(); len < 4; len = hexLength.length()) {
         hexLength = "0" + hexLength;
      }

      return hexLength;
   }

   public static int intPackLength(String str) {
      int intLength = Integer.valueOf(str, 16);
      return intLength;
   }

   public static int intPackLength(byte[] str) {
      String byteStr = bytesToHexString(str);
      int intLength = Integer.valueOf(byteStr, 16);
      return intLength;
   }

   public static String packVerify(String target, String source, String packLengths, String counter, String commandWord, String dataArea) {
      String verify = XOR(target + source + packLengths + counter + commandWord + dataArea);
      return verify;
   }

   public static String dpuString(String str) {
      String buffer = "";
      if (str != null && str.length() > 0) {
         byte[] src = (str + "\u0000").getBytes();
         String result = bytesToHexString(src);
         String resultLength = packLength(result);
         buffer = resultLength + result;
         System.out.println("resultLength==" + buffer);
      }

      return buffer;
   }

   public static String binaryString2hexString(String bString) {
      if (bString != null && !bString.equals("")) {
         int iTmp;
         if (bString.length() % 8 != 0) {
            int addLen = 8 - bString.length() % 8;

            for(iTmp = 0; iTmp < addLen; ++iTmp) {
               bString = bString + "0";
            }

            System.out.println("choiceItem = " + bString);
         }

         StringBuffer tmp = new StringBuffer();

         for(int i = 0; i < bString.length(); i += 4) {
            iTmp = 0;

            for(int j = 0; j < 4; ++j) {
               iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << 4 - j - 1;
            }

            tmp.append(Integer.toHexString(iTmp));
         }

         System.out.println("tmp.toString() = " + tmp.toString());
         return tmp.toString();
      } else {
         return "";
      }
   }

   public static String hexString2binaryString(String hexString) {
      if (hexString != null && hexString.length() % 2 == 0) {
         String bString = "";

         for(int i = 0; i < hexString.length(); ++i) {
            String tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
            bString = bString + tmp.substring(tmp.length() - 4);
         }

         return bString;
      } else {
         return null;
      }
   }

   public static String replaceBlank(String str) {
      String dest = "";
      if (str != null) {
         Pattern p = Pattern.compile("\t|\r|\n");
         Matcher m = p.matcher(str);
         dest = m.replaceAll("");
      }

      return dest.trim();
   }

   public static ArrayList<String> toStringArray(byte[] data) {
      if (data != null) {
         int total_bytes = data.length;
         if (total_bytes >= 3) {
            int walkthrough = 0;

            ArrayList result_strings;
            int temp_len;
            for(result_strings = new ArrayList(); walkthrough < total_bytes - 1; walkthrough += temp_len + 2) {
               temp_len = data[walkthrough] << 8 | data[walkthrough + 1];
               byte[] str_bytes = new byte[temp_len - 1];
               System.arraycopy(data, walkthrough + 2, str_bytes, 0, temp_len - 1);
               result_strings.add(new String(str_bytes));
            }

            return result_strings;
         }
      }

      return null;
   }

   public static byte[] appendByteArray(byte[] src, byte[] data) {
      if (src.length > 0 && data.length > 0) {
         byte[] ret = new byte[src.length + data.length];
         System.arraycopy(src, 0, ret, 0, src.length);
         System.arraycopy(data, 0, ret, src.length, data.length);
         return ret;
      } else {
         throw new IllegalArgumentException("字节数组参数错误");
      }
   }

   public static String calculateSingleFileMD5sum(File file) throws Exception {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      FileInputStream fis = new FileInputStream(file);
      byte[] buff = new byte[256];

      int readLen;
      while((readLen = fis.read(buff)) != -1) {
         md5.update(buff, 0, readLen);
      }

      fis.close();
      StringBuilder sb = new StringBuilder();
      byte[] data = md5.digest();
      byte[] var7 = data;
      int var8 = data.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         byte b = var7[var9];
         sb.append((new Formatter()).format("%02x", b));
      }

      return sb.toString();
   }

   public static String parseAscii(String str) {
      StringBuilder sb = new StringBuilder();
      byte[] bs = str.getBytes();

      for(int i = 0; i < bs.length; ++i) {
         sb.append(toHex(bs[i]));
      }

      return sb.toString();
   }

   public static String toHex(int n) {
      StringBuilder sb = new StringBuilder();
      if (n / 16 == 0) {
         return toHexUtil(n);
      } else {
         String t = toHex(n / 16);
         int nn = n % 16;
         sb.append(t).append(toHexUtil(nn));
         return sb.toString();
      }
   }

   private static String toHexUtil(int n) {
      String rt = "";
      switch(n) {
      case 10:
         rt = rt + "A";
         break;
      case 11:
         rt = rt + "B";
         break;
      case 12:
         rt = rt + "C";
         break;
      case 13:
         rt = rt + "D";
         break;
      case 14:
         rt = rt + "E";
         break;
      case 15:
         rt = rt + "F";
         break;
      default:
         rt = rt + n;
      }

      return rt;
   }
}
