package org.codehaus.jackson;

public final class Base64Variants {
   static final String STD_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
   public static final Base64Variant MIME = new Base64Variant("MIME", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", true, '=', 76);
   public static final Base64Variant MIME_NO_LINEFEEDS;
   public static final Base64Variant PEM;
   public static final Base64Variant MODIFIED_FOR_URL;

   public Base64Variants() {
   }

   public static Base64Variant getDefaultVariant() {
      return MIME_NO_LINEFEEDS;
   }

   static {
      MIME_NO_LINEFEEDS = new Base64Variant(MIME, "MIME-NO-LINEFEEDS", 2147483647);
      PEM = new Base64Variant(MIME, "PEM", true, '=', 64);
      StringBuffer sb = new StringBuffer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
      sb.setCharAt(sb.indexOf("+"), '-');
      sb.setCharAt(sb.indexOf("/"), '_');
      MODIFIED_FOR_URL = new Base64Variant("MODIFIED-FOR-URL", sb.toString(), false, '\u0000', 2147483647);
   }
}
