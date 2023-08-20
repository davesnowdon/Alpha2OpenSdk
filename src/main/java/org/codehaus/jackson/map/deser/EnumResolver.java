package org.codehaus.jackson.map.deser;

import java.util.HashMap;
import org.codehaus.jackson.map.AnnotationIntrospector;

public final class EnumResolver<T extends Enum<T>> {
   protected final Class<T> _enumClass;
   protected final T[] _enums;
   protected final HashMap<String, T> _enumsById;

   private EnumResolver(Class<T> enumClass, T[] enums, HashMap<String, T> map) {
      this._enumClass = enumClass;
      this._enums = enums;
      this._enumsById = map;
   }

   public static <ET extends Enum<ET>> EnumResolver<ET> constructFor(Class<ET> enumCls, AnnotationIntrospector ai) {
      ET[] enumValues = (Enum[])enumCls.getEnumConstants();
      if (enumValues == null) {
         throw new IllegalArgumentException("No enum constants for class " + enumCls.getName());
      } else {
         HashMap<String, ET> map = new HashMap();
         Enum[] arr$ = enumValues;
         int len$ = enumValues.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ET e = arr$[i$];
            map.put(ai.findEnumValue(e), e);
         }

         return new EnumResolver(enumCls, enumValues, map);
      }
   }

   public static <ET extends Enum<ET>> EnumResolver<ET> constructUsingToString(Class<ET> enumCls) {
      ET[] enumValues = (Enum[])enumCls.getEnumConstants();
      HashMap<String, ET> map = new HashMap();
      int i = enumValues.length;

      while(true) {
         --i;
         if (i < 0) {
            return new EnumResolver(enumCls, enumValues, map);
         }

         ET e = enumValues[i];
         map.put(e.toString(), e);
      }
   }

   public static EnumResolver<?> constructUnsafe(Class<?> rawEnumCls, AnnotationIntrospector ai) {
      return constructFor(rawEnumCls, ai);
   }

   public static EnumResolver<?> constructUnsafeUsingToString(Class<?> rawEnumCls) {
      return constructUsingToString(rawEnumCls);
   }

   public T findEnum(String key) {
      return (Enum)this._enumsById.get(key);
   }

   public T getEnum(int index) {
      return index >= 0 && index < this._enums.length ? this._enums[index] : null;
   }

   public Class<T> getEnumClass() {
      return this._enumClass;
   }

   public int lastValidIndex() {
      return this._enums.length - 1;
   }
}
