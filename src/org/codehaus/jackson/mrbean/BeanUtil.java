package org.codehaus.jackson.mrbean;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BeanUtil {
   public BeanUtil() {
   }

   protected static boolean isConcrete(Member member) {
      int mod = member.getModifiers();
      return (mod & 1536) == 0;
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
}
