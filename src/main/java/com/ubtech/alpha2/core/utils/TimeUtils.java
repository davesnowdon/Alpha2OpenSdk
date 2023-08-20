package com.ubtech.alpha2.core.utils;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeUtils {
   public static final String YYYY_MM_DD = "yyyy-MM-dd";
   public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
   public static final String DD_MM_YYYY = "dd-MM-yyyy";
   public static final String MM_DD_YYYY = "MM-dd-yyyy";
   public static final String HH_MM_SS = "HH:mm:ss";
   public static final String YYYYMMDD_HHMMSS = "yyyyMMdd HHmmss";
   static int[] daysInMonth = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
   static final int MONTH_FEBRUARY = 2;
   public static final int PRECISE_YEAR = 1;
   public static final int PRECISE_MONTH = 2;
   public static final int PRECISE_DAY = 3;
   public static final int PRECISE_HOUR = 4;
   public static final int PRECISE_MINUTE = 5;
   public static final int PRECISE_SECOND = 6;
   public static final int PRECISE_MilliSECOND = 7;

   public TimeUtils() {
   }

   public static int getYear() {
      return Calendar.getInstance().get(1);
   }

   public static int getMonth() {
      return Calendar.getInstance().get(2) + 1;
   }

   public static int getDayOfYear() {
      return Calendar.getInstance().get(6);
   }

   public static boolean isLeapYear(int year) {
      if (year % 100 == 0) {
         return year % 400 == 0;
      } else {
         return year % 4 == 0;
      }
   }

   public static int getDayOfMonth() {
      return Calendar.getInstance().get(5);
   }

   public static int getDayOfWeek() {
      return Calendar.getInstance().get(7);
   }

   public static int getWeekOfMonth() {
      return Calendar.getInstance().get(8);
   }

   public static Date getTimeYearNext(String format) {
      Calendar calendar = Calendar.getInstance();
      calendar.add(6, 183);
      String date = (new SimpleDateFormat(format)).format(calendar.getTime());
      return getDateParse(date, format);
   }

   public static String convertDateToString(Date dateTime, String format) {
      SimpleDateFormat df = new SimpleDateFormat(format);
      return df.format(dateTime);
   }

   public static String getTwoDay(String sj1, String sj2, String format) {
      SimpleDateFormat myFormatter = new SimpleDateFormat(format);
      long day = 0L;

      try {
         Date date = myFormatter.parse(sj1);
         Date mydate = myFormatter.parse(sj2);
         day = (date.getTime() - mydate.getTime()) / 86400000L;
      } catch (Exception var8) {
         return "";
      }

      return day + "";
   }

   public static String getWeek(String args) {
      Date date = strToDate(args, "yyyy-MM-dd");
      Calendar c = Calendar.getInstance();
      c.setTime(date);
      return (new SimpleDateFormat("EEEE")).format(c.getTime());
   }

   public static Date strToDate(String args, String format) {
      return (new SimpleDateFormat(format)).parse(args, new ParsePosition(0));
   }

   public static long getDays(String args1, String args2, String format) {
      if (args1 != null && !args1.equals("")) {
         if (args2 != null && !args2.equals("")) {
            SimpleDateFormat myFormatter = new SimpleDateFormat(format);
            Date date = null;
            Date mydate = null;

            try {
               date = myFormatter.parse(args1);
               mydate = myFormatter.parse(args2);
            } catch (Exception var8) {
            }

            long day = (date.getTime() - mydate.getTime()) / 86400000L;
            return day;
         } else {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   public static String getDefaultDay(String format) {
      String str = "";
      SimpleDateFormat sdf = new SimpleDateFormat(format);
      Calendar lastDate = Calendar.getInstance();
      lastDate.set(5, 1);
      lastDate.add(2, 1);
      lastDate.add(5, -1);
      str = sdf.format(lastDate.getTime());
      return str;
   }

   public static String getPreviousMonthFirst(String format) {
      Calendar lastDate = Calendar.getInstance();
      lastDate.set(5, 1);
      lastDate.add(2, -1);
      String date = (new SimpleDateFormat(format)).format(lastDate.getTime());
      return date;
   }

   public static String getPreviousMonthEnd(String format) {
      Calendar lastDate = Calendar.getInstance();
      lastDate.set(5, 1);
      lastDate.add(5, -1);
      String date = (new SimpleDateFormat(format)).format(lastDate.getTime());
      return date;
   }

   public static String getMonthFirst(int num, String format) {
      Calendar lastDate = Calendar.getInstance();
      lastDate.set(5, 1);
      lastDate.add(2, num);
      String date = (new SimpleDateFormat(format)).format(lastDate.getTime());
      return date;
   }

   public static String getMonthEnd(int num, String format) {
      Calendar lastDate = Calendar.getInstance();
      lastDate.set(5, 1);
      lastDate.add(2, num + 1);
      lastDate.add(5, -1);
      String date = (new SimpleDateFormat(format)).format(lastDate.getTime());
      return date;
   }

   public static String getFirstDayOfMonth(String format) {
      String str = "";
      SimpleDateFormat sdf = new SimpleDateFormat(format);
      Calendar lastDate = Calendar.getInstance();
      lastDate.set(5, 1);
      str = sdf.format(lastDate.getTime());
      return str;
   }

   public static String getCurrentWeekday() {
      int mondayPlus = getMondayPlus();
      GregorianCalendar currentDate = new GregorianCalendar();
      currentDate.add(5, mondayPlus + 6);
      Date monday = currentDate.getTime();
      DateFormat df = DateFormat.getDateInstance();
      String preMonday = df.format(monday);
      return preMonday;
   }

   public static String getNowTime(String format) {
      Date now = new Date();
      SimpleDateFormat dateFormat = new SimpleDateFormat(format);
      String hehe = dateFormat.format(now);
      return hehe;
   }

   public static int getMondayPlus() {
      Calendar cd = Calendar.getInstance();
      int dayOfWeek = cd.get(7) - 1;
      return dayOfWeek == 1 ? 0 : 1 - dayOfWeek;
   }

   public static String getMondayOFWeek() {
      int mondayPlus = getMondayPlus();
      GregorianCalendar currentDate = new GregorianCalendar();
      currentDate.add(5, mondayPlus);
      Date monday = currentDate.getTime();
      DateFormat df = DateFormat.getDateInstance();
      String preMonday = df.format(monday);
      return preMonday;
   }

   public static int getMonthDay(String args, String separator) {
      String[] str = args.split(separator);
      Calendar calendar = Calendar.getInstance();
      int months = Integer.parseInt(str[1]);
      int years = Integer.parseInt(str[0]);
      int days = 1;
      calendar.set(years, months - 1, days);
      int day = calendar.getActualMaximum(5);
      return day;
   }

   public static int dateDiff(String argsBegin, String argsEnd, String format) {
      Date dateBegin = getDateParse(argsBegin, format);
      Date dateEnd = getDateParse(argsEnd, format);
      long timeBegin = dateBegin.getTime();
      long timeEnd = dateEnd.getTime();
      long diff = Math.abs(timeBegin - timeEnd);
      diff /= 86400000L;
      return (int)diff;
   }

   public static Date getDateParse(String args, String format) {
      return (new SimpleDateFormat(format)).parse(args, new ParsePosition(0));
   }

   public static String getYesterday(int num, String format) {
      SimpleDateFormat sdf = new SimpleDateFormat(format);
      Calendar calendar = Calendar.getInstance();
      GregorianCalendar gc = new GregorianCalendar(calendar.get(1), calendar.get(2), calendar.get(5));
      gc.add(5, num);
      Date date = gc.getTime();
      String str = sdf.format(date);
      return str;
   }

   public static String getWeekday(int num, String format) {
      Calendar calendar = Calendar.getInstance();
      calendar.add(5, num * 7);
      calendar.set(7, 2);
      return (new SimpleDateFormat(format)).format(calendar.getTime());
   }

   public static String getWeekSunday(int num, String format) {
      Calendar calendar = Calendar.getInstance();
      calendar.setFirstDayOfWeek(2);
      calendar.set(7, 1);
      calendar.add(4, num);
      return (new SimpleDateFormat(format)).format(calendar.getTime());
   }

   public static String getPreviousWeekday(String format) {
      Calendar calendar = Calendar.getInstance();
      calendar.add(5, -7);
      calendar.set(7, 2);
      return (new SimpleDateFormat(format)).format(calendar.getTime());
   }

   public static String getPreviousWeekSunday(String format) {
      Calendar calendar = Calendar.getInstance();
      calendar.setFirstDayOfWeek(2);
      calendar.set(7, 1);
      calendar.add(4, -1);
      return (new SimpleDateFormat(format)).format(calendar.getTime());
   }

   public static String getNextMonday(String format) {
      Calendar calendar = Calendar.getInstance();
      calendar.add(5, 7);
      calendar.set(7, 2);
      return (new SimpleDateFormat(format)).format(calendar.getTime());
   }

   public static String getNextSunday(String format) {
      Calendar calendar = Calendar.getInstance();
      calendar.setFirstDayOfWeek(2);
      calendar.set(7, 1);
      calendar.add(4, 1);
      return (new SimpleDateFormat(format)).format(calendar.getTime());
   }

   public static String getNextMonthFirst(String format) {
      Calendar lastDate = Calendar.getInstance();
      lastDate.set(5, 1);
      lastDate.add(2, 1);
      return (new SimpleDateFormat(format)).format(lastDate.getTime());
   }

   public static String getNextMonthEnd(String format) {
      Calendar lastDate = Calendar.getInstance();
      lastDate.set(5, 1);
      lastDate.add(2, 2);
      lastDate.add(5, -1);
      return (new SimpleDateFormat(format)).format(lastDate.getTime());
   }

   public static String getWeek(String args, String num, String format) {
      Date date = getDateParse(args, "yyyy-MM-dd");
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      if ("1".equals(num)) {
         calendar.set(7, 2);
      } else if ("2".equals(num)) {
         calendar.set(7, 3);
      } else if ("3".equals(num)) {
         calendar.set(7, 4);
      } else if ("4".equals(num)) {
         calendar.set(7, 5);
      } else if ("5".equals(num)) {
         calendar.set(7, 6);
      } else if ("6".equals(num)) {
         calendar.set(7, 7);
      } else if ("0".equals(num)) {
         calendar.set(7, 1);
      }

      return (new SimpleDateFormat(format)).format(calendar.getTime());
   }

   public static boolean getTimeStepSize(String args1, String args2, String format) {
      Date date1 = getDateParse(args1, format);
      Date date2 = getDateParse(args2, format);
      long time1 = date1.getTime();
      long time2 = date2.getTime();
      return time1 > time2;
   }

   public static void main(String[] args) {
      TimeUtils tt = new TimeUtils();
      System.out.println("获取当天日期:" + getNowTime("yyyy-MM-dd"));
   }
}
