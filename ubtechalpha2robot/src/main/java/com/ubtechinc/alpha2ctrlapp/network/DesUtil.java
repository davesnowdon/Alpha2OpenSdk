package com.ubtechinc.alpha2ctrlapp.network;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DesUtil {
   private static final String DEFAULT_PASSWORD_CRYPT_KEY = "__jDlog_";
   private static final String DES = "DES";
   private static Cipher cipher = null;

   public DesUtil() {
   }

   public static byte[] encrypt(byte[] src, byte[] key) throws Exception {
      SecureRandom sr = new SecureRandom();
      DESKeySpec dks = new DESKeySpec(key);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey securekey = keyFactory.generateSecret(dks);
      cipher.init(1, securekey, sr);
      return cipher.doFinal(src);
   }

   public static byte[] decrypt(byte[] src, byte[] key) throws Exception {
      SecureRandom sr = new SecureRandom();
      DESKeySpec dks = new DESKeySpec(key);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey securekey = keyFactory.generateSecret(dks);
      cipher.init(2, securekey, sr);
      return cipher.doFinal(src);
   }

   public static String decrypt(String data) {
      try {
         return new String(decrypt(hex2byte(data.getBytes()), "__jDlog_".getBytes()));
      } catch (Exception var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static String decrypt(String data, String key) {
      try {
         return new String(decrypt(hex2byte(data.getBytes()), key.getBytes()));
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static String encrypt(String data) {
      try {
         return byte2hex(encrypt(data.getBytes(), "__jDlog_".getBytes()));
      } catch (Exception var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static String encrypt(String data, String key) {
      try {
         return byte2hex(encrypt(data.getBytes(), key.getBytes()));
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static String byte2hex(byte[] b) {
      String hs = "";
      String stmp = "";

      for(int n = 0; n < b.length; ++n) {
         stmp = Integer.toHexString(b[n] & 255);
         if (stmp.length() == 1) {
            hs = hs + "0" + stmp;
         } else {
            hs = hs + stmp;
         }
      }

      return hs.toUpperCase();
   }

   public static byte[] hex2byte(byte[] b) {
      if (b.length % 2 != 0) {
         throw new IllegalArgumentException("长度不是偶数");
      } else {
         byte[] b2 = new byte[b.length / 2];

         for(int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte)Integer.parseInt(item, 16);
         }

         return b2;
      }
   }

   public static boolean checkVeryCode(String veryCode, String key, String correctCode) {
      return correctCode.equals(decrypt(veryCode, key));
   }

   public static String getRandomCode(int count) {
      StringBuffer sb = new StringBuffer();
      String str = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
      Random r = new Random();

      for(int i = 0; i < count; ++i) {
         int num = r.nextInt(str.length());
         sb.append(str.charAt(num));
         str = str.replace(str.charAt(num) + "", "");
      }

      return sb.toString();
   }

   public static String getRandomInt(int count) {
      StringBuffer sb = new StringBuffer();
      String str = "0123456789";
      Random r = new Random();

      for(int i = 0; i < count; ++i) {
         int num = r.nextInt(str.length());
         sb.append(str.charAt(num));
         str = str.replace(str.charAt(num) + "", "");
      }

      return sb.toString();
   }

   public static final String MD5(String s) {
      char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

      try {
         byte[] btInput = s.getBytes();
         MessageDigest mdInst = MessageDigest.getInstance("MD5");
         mdInst.update(btInput);
         byte[] md = mdInst.digest();
         int j = md.length;
         char[] str = new char[j * 2];
         int k = 0;

         for(int i = 0; i < j; ++i) {
            byte byte0 = md[i];
            str[k++] = hexDigits[byte0 >>> 4 & 15];
            str[k++] = hexDigits[byte0 & 15];
         }

         return new String(str);
      } catch (Exception var10) {
         var10.printStackTrace();
         return null;
      }
   }

   public static void main(String[] args) {
      String url = "123456";
      url = encrypt(url, "ubx7878192012");
      System.err.println(url);
      url = decrypt("C16A7C1FAFA05342", "UBX1234567");
   }

   public static String getMD5(String val, int bit) {
      try {
         MessageDigest md = MessageDigest.getInstance("MD5");
         md.update(val.getBytes());
         byte[] b = md.digest();
         StringBuffer buf = new StringBuffer("");

         for(int offset = 0; offset < b.length; ++offset) {
            int i = b[offset];
            if (i < 0) {
               i += 256;
            }

            if (i < 16) {
               buf.append("0");
            }

            buf.append(Integer.toHexString(i));
         }

         if (bit == 32) {
            return buf.toString().toUpperCase();
         } else {
            return buf.toString().substring(8, 24).toUpperCase();
         }
      } catch (NoSuchAlgorithmException var7) {
         var7.printStackTrace();
         return null;
      }
   }

   public static final String getMD5(String s) {
      char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

      try {
         byte[] btInput = s.getBytes();
         MessageDigest mdInst = MessageDigest.getInstance("MD5");
         mdInst.update(btInput);
         byte[] md = mdInst.digest();
         int j = md.length;
         char[] str = new char[j * 2];
         int k = 0;

         for(int i = 0; i < j; ++i) {
            byte byte0 = md[i];
            str[k++] = hexDigits[byte0 >>> 4 & 15];
            str[k++] = hexDigits[byte0 & 15];
         }

         return new String(str);
      } catch (Exception var10) {
         var10.printStackTrace();
         return null;
      }
   }

   static {
      try {
         cipher = Cipher.getInstance("DES");
      } catch (Exception var1) {
         var1.printStackTrace();
      }

   }
}
