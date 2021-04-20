package org.apache.harmony.beans;

import java.lang.reflect.Method;
import java.util.Arrays;

public class BeansUtils {
   public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
   public static final String NEW = "new";
   public static final String NEWINSTANCE = "newInstance";
   public static final String NEWARRAY = "newArray";
   public static final String FORNAME = "forName";
   public static final String GET = "get";
   public static final String IS = "is";
   public static final String SET = "set";
   public static final String ADD = "add";
   public static final String PUT = "put";
   public static final String NULL = "null";
   public static final String QUOTE = "\"\"";
   private static final String EQUALS_METHOD = "equals";
   private static final Class<?>[] EQUALS_PARAMETERS = new Class[]{Object.class};

   public BeansUtils() {
   }

   public static final int getHashCode(Object obj) {
      return obj != null ? obj.hashCode() : 0;
   }

   public static final int getHashCode(boolean bool) {
      return bool ? 1 : 0;
   }

   public static String toASCIILowerCase(String string) {
      char[] charArray = string.toCharArray();
      StringBuilder sb = new StringBuilder(charArray.length);

      for(int index = 0; index < charArray.length; ++index) {
         if ('A' <= charArray[index] && charArray[index] <= 'Z') {
            sb.append((char)(charArray[index] + 32));
         } else {
            sb.append(charArray[index]);
         }
      }

      return sb.toString();
   }

   public static String toASCIIUpperCase(String string) {
      char[] charArray = string.toCharArray();
      StringBuilder sb = new StringBuilder(charArray.length);

      for(int index = 0; index < charArray.length; ++index) {
         if ('a' <= charArray[index] && charArray[index] <= 'z') {
            sb.append((char)(charArray[index] - 32));
         } else {
            sb.append(charArray[index]);
         }
      }

      return sb.toString();
   }

   public static boolean isPrimitiveWrapper(Class<?> wrapper, Class<?> base) {
      return base == Boolean.TYPE && wrapper == Boolean.class || base == Byte.TYPE && wrapper == Byte.class || base == Character.TYPE && wrapper == Character.class || base == Short.TYPE && wrapper == Short.class || base == Integer.TYPE && wrapper == Integer.class || base == Long.TYPE && wrapper == Long.class || base == Float.TYPE && wrapper == Float.class || base == Double.TYPE && wrapper == Double.class;
   }

   public static boolean declaredEquals(Class<?> clazz) {
      Method[] arr$ = clazz.getDeclaredMethods();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Method declaredMethod = arr$[i$];
         if ("equals".equals(declaredMethod.getName()) && Arrays.equals(declaredMethod.getParameterTypes(), EQUALS_PARAMETERS)) {
            return true;
         }
      }

      return false;
   }

   public static String idOfClass(Class<?> clazz) {
      Class<?> theClass = clazz;
      StringBuilder sb = new StringBuilder();
      if (clazz.isArray()) {
         do {
            sb.append("Array");
            theClass = theClass.getComponentType();
         } while(theClass.isArray());
      }

      String clazzName = theClass.getName();
      clazzName = clazzName.substring(clazzName.lastIndexOf(46) + 1);
      return clazzName + sb.toString();
   }
}
