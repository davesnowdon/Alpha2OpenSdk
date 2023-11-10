package org.codehaus.jackson.map.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public final class ClassUtil {
   public ClassUtil() {
   }

   public static List<Class<?>> findSuperTypes(Class<?> cls, Class<?> endBefore) {
      return findSuperTypes(cls, endBefore, new ArrayList());
   }

   public static List<Class<?>> findSuperTypes(Class<?> cls, Class<?> endBefore, List<Class<?>> result) {
      _addSuperTypes(cls, endBefore, result, false);
      return result;
   }

   private static void _addSuperTypes(Class<?> cls, Class<?> endBefore, Collection<Class<?>> result, boolean addClassItself) {
      if (cls != endBefore && cls != null && cls != Object.class) {
         if (addClassItself) {
            if (result.contains(cls)) {
               return;
            }

            result.add(cls);
         }

         Class[] arr$ = cls.getInterfaces();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Class<?> intCls = arr$[i$];
            _addSuperTypes(intCls, endBefore, result, true);
         }

         _addSuperTypes(cls.getSuperclass(), endBefore, result, true);
      }
   }

   public static String canBeABeanType(Class<?> type) {
      if (type.isAnnotation()) {
         return "annotation";
      } else if (type.isArray()) {
         return "array";
      } else if (type.isEnum()) {
         return "enum";
      } else {
         return type.isPrimitive() ? "primitive" : null;
      }
   }

   public static String isLocalType(Class<?> type) {
      try {
         if (type.getEnclosingMethod() != null) {
            return "local/anonymous";
         }

         if (type.getEnclosingClass() != null && !Modifier.isStatic(type.getModifiers())) {
            return "non-static member class";
         }
      } catch (SecurityException var2) {
      } catch (NullPointerException var3) {
      }

      return null;
   }

   public static boolean isProxyType(Class<?> type) {
      if (Proxy.isProxyClass(type)) {
         return true;
      } else {
         String name = type.getName();
         return name.startsWith("net.sf.cglib.proxy.") || name.startsWith("org.hibernate.proxy.");
      }
   }

   public static boolean isConcrete(Class<?> type) {
      int mod = type.getModifiers();
      return (mod & 1536) == 0;
   }

   public static boolean isConcrete(Member member) {
      int mod = member.getModifiers();
      return (mod & 1536) == 0;
   }

   public static boolean isCollectionMapOrArray(Class<?> type) {
      if (type.isArray()) {
         return true;
      } else if (Collection.class.isAssignableFrom(type)) {
         return true;
      } else {
         return Map.class.isAssignableFrom(type);
      }
   }

   public static String getClassDescription(Object classOrInstance) {
      if (classOrInstance == null) {
         return "unknown";
      } else {
         Class<?> cls = classOrInstance instanceof Class ? (Class)classOrInstance : classOrInstance.getClass();
         return cls.getName();
      }
   }

   public static boolean hasGetterSignature(Method m) {
      if (Modifier.isStatic(m.getModifiers())) {
         return false;
      } else {
         Class<?>[] pts = m.getParameterTypes();
         if (pts != null && pts.length != 0) {
            return false;
         } else {
            return Void.TYPE != m.getReturnType();
         }
      }
   }

   public static Throwable getRootCause(Throwable t) {
      while(t.getCause() != null) {
         t = t.getCause();
      }

      return t;
   }

   public static void throwRootCause(Throwable t) throws Exception {
      t = getRootCause(t);
      if (t instanceof Exception) {
         throw (Exception)t;
      } else {
         throw (Error)t;
      }
   }

   public static void throwAsIAE(Throwable t) {
      throwAsIAE(t, t.getMessage());
   }

   public static void throwAsIAE(Throwable t, String msg) {
      if (t instanceof RuntimeException) {
         throw (RuntimeException)t;
      } else if (t instanceof Error) {
         throw (Error)t;
      } else {
         throw new IllegalArgumentException(msg, t);
      }
   }

   public static void unwrapAndThrowAsIAE(Throwable t) {
      throwAsIAE(getRootCause(t));
   }

   public static void unwrapAndThrowAsIAE(Throwable t, String msg) {
      throwAsIAE(getRootCause(t), msg);
   }

   public static <T> T createInstance(Class<T> cls, boolean canFixAccess) throws IllegalArgumentException {
      Constructor<T> ctor = findConstructor(cls, canFixAccess);
      if (ctor == null) {
         throw new IllegalArgumentException("Class " + cls.getName() + " has no default (no arg) constructor");
      } else {
         try {
            return ctor.newInstance();
         } catch (Exception var4) {
            unwrapAndThrowAsIAE(var4, "Failed to instantiate class " + cls.getName() + ", problem: " + var4.getMessage());
            return null;
         }
      }
   }

   public static <T> Constructor<T> findConstructor(Class<T> cls, boolean canFixAccess) throws IllegalArgumentException {
      try {
         Constructor<T> ctor = cls.getDeclaredConstructor();
         if (canFixAccess) {
            checkAndFixAccess(ctor);
         } else if (!Modifier.isPublic(ctor.getModifiers())) {
            throw new IllegalArgumentException("Default constructor for " + cls.getName() + " is not accessible (non-public?): not allowed to try modify access via Reflection: can not instantiate type");
         }

         return ctor;
      } catch (NoSuchMethodException var3) {
      } catch (Exception var4) {
         unwrapAndThrowAsIAE(var4, "Failed to find default constructor of class " + cls.getName() + ", problem: " + var4.getMessage());
      }

      return null;
   }

   public static Object defaultValue(Class<?> cls) {
      if (cls == Integer.TYPE) {
         return 0;
      } else if (cls == Long.TYPE) {
         return 0L;
      } else if (cls == Boolean.TYPE) {
         return Boolean.FALSE;
      } else if (cls == Double.TYPE) {
         return 0.0D;
      } else if (cls == Float.TYPE) {
         return 0.0F;
      } else if (cls == Byte.TYPE) {
         return 0;
      } else if (cls == Short.TYPE) {
         return Short.valueOf((short)0);
      } else if (cls == Character.TYPE) {
         return '\u0000';
      } else {
         throw new IllegalArgumentException("Class " + cls.getName() + " is not a primitive type");
      }
   }

   public static Class<?> wrapperType(Class<?> primitiveType) {
      if (primitiveType == Integer.TYPE) {
         return Integer.class;
      } else if (primitiveType == Long.TYPE) {
         return Long.class;
      } else if (primitiveType == Boolean.TYPE) {
         return Boolean.class;
      } else if (primitiveType == Double.TYPE) {
         return Double.class;
      } else if (primitiveType == Float.TYPE) {
         return Float.class;
      } else if (primitiveType == Byte.TYPE) {
         return Byte.class;
      } else if (primitiveType == Short.TYPE) {
         return Short.class;
      } else if (primitiveType == Character.TYPE) {
         return Character.class;
      } else {
         throw new IllegalArgumentException("Class " + primitiveType.getName() + " is not a primitive type");
      }
   }

   public static void checkAndFixAccess(Member member) {
      AccessibleObject ao = (AccessibleObject)member;

      try {
         ao.setAccessible(true);
      } catch (SecurityException var4) {
         if (!ao.isAccessible()) {
            Class<?> declClass = member.getDeclaringClass();
            throw new IllegalArgumentException("Can not access " + member + " (from class " + declClass.getName() + "; failed to set access: " + var4.getMessage());
         }
      }

   }

   public static Class<? extends Enum<?>> findEnumType(EnumSet<?> s) {
      return !s.isEmpty() ? findEnumType((Enum)s.iterator().next()) : ClassUtil.EnumTypeLocator.instance.enumTypeFor(s);
   }

   public static Class<? extends Enum<?>> findEnumType(EnumMap<?, ?> m) {
      return !m.isEmpty() ? findEnumType((Enum)m.keySet().iterator().next()) : ClassUtil.EnumTypeLocator.instance.enumTypeFor(m);
   }

   public static Class<? extends Enum<?>> findEnumType(Enum<?> en) {
      Class<?> ec = en.getClass();
      if (ec.getSuperclass() != Enum.class) {
         ec = ec.getSuperclass();
      }

      return ec;
   }

   public static Class<? extends Enum<?>> findEnumType(Class<?> cls) {
      if (cls.getSuperclass() != Enum.class) {
         cls = cls.getSuperclass();
      }

      return cls;
   }

   private static class EnumTypeLocator {
      static final ClassUtil.EnumTypeLocator instance = new ClassUtil.EnumTypeLocator();
      private final Field enumSetTypeField = locateField(EnumSet.class, "elementType", Class.class);
      private final Field enumMapTypeField = locateField(EnumMap.class, "elementType", Class.class);

      private EnumTypeLocator() {
      }

      public Class<? extends Enum<?>> enumTypeFor(EnumSet<?> set) {
         if (this.enumSetTypeField != null) {
            return (Class)this.get(set, this.enumSetTypeField);
         } else {
            throw new IllegalStateException("Can not figure out type for EnumSet (odd JDK platform?)");
         }
      }

      public Class<? extends Enum<?>> enumTypeFor(EnumMap<?, ?> set) {
         if (this.enumMapTypeField != null) {
            return (Class)this.get(set, this.enumMapTypeField);
         } else {
            throw new IllegalStateException("Can not figure out type for EnumMap (odd JDK platform?)");
         }
      }

      private Object get(Object bean, Field field) {
         try {
            return field.get(bean);
         } catch (Exception var4) {
            throw new IllegalArgumentException(var4);
         }
      }

      private static Field locateField(Class<?> fromClass, String expectedName, Class<?> type) {
         Field found = null;
         Field[] fields = fromClass.getDeclaredFields();
         Field[] arr$ = fields;
         int len$ = fields.length;

         int i$;
         Field f;
         for(i$ = 0; i$ < len$; ++i$) {
            f = arr$[i$];
            if (expectedName.equals(f.getName()) && f.getType() == type) {
               found = f;
               break;
            }
         }

         if (found == null) {
            arr$ = fields;
            len$ = fields.length;

            for(i$ = 0; i$ < len$; ++i$) {
               f = arr$[i$];
               if (f.getType() == type) {
                  if (found != null) {
                     return null;
                  }

                  found = f;
               }
            }
         }

         if (found != null) {
            try {
               found.setAccessible(true);
            } catch (Throwable var9) {
            }
         }

         return found;
      }
   }
}
