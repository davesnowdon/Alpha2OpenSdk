package org.codehaus.jackson.map.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.codehaus.jackson.io.NumberInput;

public class StdDateFormat extends DateFormat {
   static final String DATE_FORMAT_STR_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
   static final String DATE_FORMAT_STR_ISO8601_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
   static final String DATE_FORMAT_STR_PLAIN = "yyyy-MM-dd";
   static final String DATE_FORMAT_STR_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
   static final String[] ALL_FORMATS = new String[]{"yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "EEE, dd MMM yyyy HH:mm:ss zzz", "yyyy-MM-dd"};
   static final SimpleDateFormat DATE_FORMAT_RFC1123;
   static final SimpleDateFormat DATE_FORMAT_ISO8601;
   static final SimpleDateFormat DATE_FORMAT_ISO8601_Z;
   static final SimpleDateFormat DATE_FORMAT_PLAIN;
   public static final StdDateFormat instance;
   transient SimpleDateFormat _formatRFC1123;
   transient SimpleDateFormat _formatISO8601;
   transient SimpleDateFormat _formatISO8601_z;
   transient SimpleDateFormat _formatPlain;

   public StdDateFormat() {
   }

   public StdDateFormat clone() {
      return new StdDateFormat();
   }

   public static DateFormat getBlueprintISO8601Format() {
      return DATE_FORMAT_ISO8601;
   }

   public static DateFormat getISO8601Format(TimeZone tz) {
      DateFormat df = (SimpleDateFormat)DATE_FORMAT_ISO8601.clone();
      df.setTimeZone(tz);
      return df;
   }

   public static DateFormat getBlueprintRFC1123Format() {
      return DATE_FORMAT_RFC1123;
   }

   public static DateFormat getRFC1123Format(TimeZone tz) {
      DateFormat df = (SimpleDateFormat)DATE_FORMAT_RFC1123.clone();
      df.setTimeZone(tz);
      return df;
   }

   public Date parse(String dateStr) throws ParseException {
      dateStr = dateStr.trim();
      ParsePosition pos = new ParsePosition(0);
      Date result = this.parse(dateStr, pos);
      if (result != null) {
         return result;
      } else {
         StringBuilder sb = new StringBuilder();
         String[] arr$ = ALL_FORMATS;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String f = arr$[i$];
            if (sb.length() > 0) {
               sb.append("\", \"");
            } else {
               sb.append('"');
            }

            sb.append(f);
         }

         sb.append('"');
         throw new ParseException(String.format("Can not parse date \"%s\": not compatible with any of standard forms (%s)", dateStr, sb.toString()), pos.getErrorIndex());
      }
   }

   public Date parse(String dateStr, ParsePosition pos) {
      if (this.looksLikeISO8601(dateStr)) {
         return this.parseAsISO8601(dateStr, pos);
      } else {
         int i = dateStr.length();

         char ch;
         do {
            --i;
            if (i < 0) {
               break;
            }

            ch = dateStr.charAt(i);
         } while(ch >= '0' && ch <= '9');

         return i < 0 && NumberInput.inLongRange(dateStr, false) ? new Date(Long.parseLong(dateStr)) : this.parseAsRFC1123(dateStr, pos);
      }
   }

   public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
      if (this._formatISO8601 == null) {
         this._formatISO8601 = (SimpleDateFormat)DATE_FORMAT_ISO8601.clone();
      }

      return this._formatISO8601.format(date, toAppendTo, fieldPosition);
   }

   protected boolean looksLikeISO8601(String dateStr) {
      return dateStr.length() >= 5 && Character.isDigit(dateStr.charAt(0)) && Character.isDigit(dateStr.charAt(3)) && dateStr.charAt(4) == '-';
   }

   protected Date parseAsISO8601(String dateStr, ParsePosition pos) {
      int len = dateStr.length();
      char c = dateStr.charAt(len - 1);
      SimpleDateFormat df;
      if (len <= 10 && Character.isDigit(c)) {
         df = this._formatPlain;
         if (df == null) {
            df = this._formatPlain = (SimpleDateFormat)DATE_FORMAT_PLAIN.clone();
         }
      } else {
         StringBuilder sb;
         if (c == 'Z') {
            df = this._formatISO8601_z;
            if (df == null) {
               df = this._formatISO8601_z = (SimpleDateFormat)DATE_FORMAT_ISO8601_Z.clone();
            }

            if (dateStr.charAt(len - 4) == ':') {
               sb = new StringBuilder(dateStr);
               sb.insert(len - 1, ".000");
               dateStr = sb.toString();
            }
         } else if (hasTimeZone(dateStr)) {
            c = dateStr.charAt(len - 3);
            if (c == ':') {
               sb = new StringBuilder(dateStr);
               sb.delete(len - 3, len - 2);
               dateStr = sb.toString();
            } else if (c == '+' || c == '-') {
               dateStr = dateStr + "00";
            }

            len = dateStr.length();
            c = dateStr.charAt(len - 9);
            if (Character.isDigit(c)) {
               sb = new StringBuilder(dateStr);
               sb.insert(len - 5, ".000");
               dateStr = sb.toString();
            }

            df = this._formatISO8601;
            if (this._formatISO8601 == null) {
               df = this._formatISO8601 = (SimpleDateFormat)DATE_FORMAT_ISO8601.clone();
            }
         } else {
            sb = new StringBuilder(dateStr);
            int timeLen = len - dateStr.lastIndexOf(84) - 1;
            if (timeLen <= 8) {
               sb.append(".000");
            }

            sb.append('Z');
            dateStr = sb.toString();
            df = this._formatISO8601_z;
            if (df == null) {
               df = this._formatISO8601_z = (SimpleDateFormat)DATE_FORMAT_ISO8601_Z.clone();
            }
         }
      }

      return df.parse(dateStr, pos);
   }

   protected Date parseAsRFC1123(String dateStr, ParsePosition pos) {
      if (this._formatRFC1123 == null) {
         this._formatRFC1123 = (SimpleDateFormat)DATE_FORMAT_RFC1123.clone();
      }

      return this._formatRFC1123.parse(dateStr, pos);
   }

   private static final boolean hasTimeZone(String str) {
      int len = str.length();
      if (len >= 6) {
         char c = str.charAt(len - 6);
         if (c == '+' || c == '-') {
            return true;
         }

         c = str.charAt(len - 5);
         if (c == '+' || c == '-') {
            return true;
         }

         c = str.charAt(len - 3);
         if (c == '+' || c == '-') {
            return true;
         }
      }

      return false;
   }

   static {
      TimeZone gmt = TimeZone.getTimeZone("GMT");
      DATE_FORMAT_RFC1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
      DATE_FORMAT_RFC1123.setTimeZone(gmt);
      DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      DATE_FORMAT_ISO8601.setTimeZone(gmt);
      DATE_FORMAT_ISO8601_Z = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      DATE_FORMAT_ISO8601_Z.setTimeZone(gmt);
      DATE_FORMAT_PLAIN = new SimpleDateFormat("yyyy-MM-dd");
      DATE_FORMAT_PLAIN.setTimeZone(gmt);
      instance = new StdDateFormat();
   }
}
