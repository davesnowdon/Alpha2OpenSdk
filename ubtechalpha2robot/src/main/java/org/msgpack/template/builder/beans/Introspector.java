package org.msgpack.template.builder.beans;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class Introspector {
   public static final int IGNORE_ALL_BEANINFO = 3;
   public static final int IGNORE_IMMEDIATE_BEANINFO = 2;
   public static final int USE_ALL_BEANINFO = 1;
   private static final String DEFAULT_BEANINFO_SEARCHPATH = "sun.beans.infos";
   private static String[] searchPath = new String[]{"sun.beans.infos"};
   private static final int DEFAULT_CAPACITY = 128;
   private static Map<Class<?>, StandardBeanInfo> theCache = Collections.synchronizedMap(new WeakHashMap(128));

   private Introspector() {
   }

   public static String decapitalize(String name) {
      if (name == null) {
         return null;
      } else if (name.length() != 0 && (name.length() <= 1 || !Character.isUpperCase(name.charAt(1)))) {
         char[] chars = name.toCharArray();
         chars[0] = Character.toLowerCase(chars[0]);
         return new String(chars);
      } else {
         return name;
      }
   }

   public static void flushCaches() {
      theCache.clear();
   }

   public static void flushFromCaches(Class<?> clazz) {
      if (clazz == null) {
         throw new NullPointerException();
      } else {
         theCache.remove(clazz);
      }
   }

   public static BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
      StandardBeanInfo beanInfo = (StandardBeanInfo)theCache.get(beanClass);
      if (beanInfo == null) {
         beanInfo = getBeanInfoImplAndInit(beanClass, (Class)null, 1);
         theCache.put(beanClass, beanInfo);
      }

      return beanInfo;
   }

   public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass) throws IntrospectionException {
      return (BeanInfo)(stopClass == null ? getBeanInfo(beanClass) : getBeanInfoImplAndInit(beanClass, stopClass, 1));
   }

   public static BeanInfo getBeanInfo(Class<?> beanClass, int flags) throws IntrospectionException {
      return (BeanInfo)(flags == 1 ? getBeanInfo(beanClass) : getBeanInfoImplAndInit(beanClass, (Class)null, flags));
   }

   public static String[] getBeanInfoSearchPath() {
      String[] path = new String[searchPath.length];
      System.arraycopy(searchPath, 0, path, 0, searchPath.length);
      return path;
   }

   public static void setBeanInfoSearchPath(String[] path) {
      if (System.getSecurityManager() != null) {
         System.getSecurityManager().checkPropertiesAccess();
      }

      searchPath = path;
   }

   private static StandardBeanInfo getBeanInfoImpl(Class<?> beanClass, Class<?> stopClass, int flags) throws IntrospectionException {
      BeanInfo explicitInfo = null;
      if (flags == 1) {
         explicitInfo = getExplicitBeanInfo(beanClass);
      }

      StandardBeanInfo beanInfo = new StandardBeanInfo(beanClass, explicitInfo, stopClass);
      if (beanInfo.additionalBeanInfo != null) {
         for(int i = beanInfo.additionalBeanInfo.length - 1; i >= 0; --i) {
            BeanInfo info = beanInfo.additionalBeanInfo[i];
            beanInfo.mergeBeanInfo(info, true);
         }
      }

      Class<?> beanSuperClass = beanClass.getSuperclass();
      if (beanSuperClass != stopClass) {
         if (beanSuperClass == null) {
            throw new IntrospectionException("Stop class is not super class of bean class");
         }

         int superflags = flags == 2 ? 1 : flags;
         BeanInfo superBeanInfo = getBeanInfoImpl(beanSuperClass, stopClass, superflags);
         if (superBeanInfo != null) {
            beanInfo.mergeBeanInfo(superBeanInfo, false);
         }
      }

      return beanInfo;
   }

   private static BeanInfo getExplicitBeanInfo(Class<?> beanClass) {
      String beanInfoClassName = beanClass.getName() + "BeanInfo";

      try {
         return loadBeanInfo(beanInfoClassName, beanClass);
      } catch (Exception var10) {
         int index = beanInfoClassName.lastIndexOf(46);
         String beanInfoName = index >= 0 ? beanInfoClassName.substring(index + 1) : beanInfoClassName;
         BeanInfo theBeanInfo = null;
         BeanDescriptor beanDescriptor = null;

         for(int i = 0; i < searchPath.length; ++i) {
            beanInfoClassName = searchPath[i] + "." + beanInfoName;

            try {
               theBeanInfo = loadBeanInfo(beanInfoClassName, beanClass);
            } catch (Exception var9) {
               continue;
            }

            beanDescriptor = theBeanInfo.getBeanDescriptor();
            if (beanDescriptor != null && beanClass == beanDescriptor.getBeanClass()) {
               return theBeanInfo;
            }
         }

         if (BeanInfo.class.isAssignableFrom(beanClass)) {
            try {
               return loadBeanInfo(beanClass.getName(), beanClass);
            } catch (Exception var8) {
            }
         }

         return null;
      }
   }

   private static BeanInfo loadBeanInfo(String beanInfoClassName, Class<?> beanClass) throws Exception {
      try {
         ClassLoader cl = beanClass.getClassLoader();
         if (cl != null) {
            return (BeanInfo)Class.forName(beanInfoClassName, true, beanClass.getClassLoader()).newInstance();
         }
      } catch (Exception var4) {
      }

      try {
         return (BeanInfo)Class.forName(beanInfoClassName, true, ClassLoader.getSystemClassLoader()).newInstance();
      } catch (Exception var3) {
         return (BeanInfo)Class.forName(beanInfoClassName, true, Thread.currentThread().getContextClassLoader()).newInstance();
      }
   }

   private static StandardBeanInfo getBeanInfoImplAndInit(Class<?> beanClass, Class<?> stopClass, int flag) throws IntrospectionException {
      StandardBeanInfo standardBeanInfo = getBeanInfoImpl(beanClass, stopClass, flag);
      standardBeanInfo.init();
      return standardBeanInfo;
   }
}
