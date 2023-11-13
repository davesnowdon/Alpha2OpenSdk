package org.codehaus.jackson.map.deser;

import java.util.HashMap;
import org.codehaus.jackson.map.AnnotationIntrospector;

public final class EnumResolver {
   protected final Class _enumClass;
   protected final Object[] _enums;
   protected final HashMap<String, T> _enumsById;

   private EnumResolver(Class enumClass, T[] enums, HashMap<String, T> map) {
      this._enumClass = enumClass;
      this._enums = enums;
      this._enumsById = map;
   }

   public static  EnumResolver constructFor(Class enumCls, AnnotationIntrospector ai) {
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

   public static EnumResolver constructUsingToString(Class<ET> enumCls) {
      Object[] enumValues = (Enum[])enumCls.getEnumConstants();
      HashMap<String, ET> map = new HashMap();
      int i = enumValues.length;

      while(true) {
         --i;
         if (i < 0) {
            return new EnumResolver(enumCls, enumValues, map);
         }

         Object e = enumValues[i];
         map.put(e.toString(), e);
      }
   }

   public static EnumResolver constructUnsafe(Class rawEnumCls, AnnotationIntrospector ai) {
      return constructFor(rawEnumCls, ai);
   }

   public static EnumResolver constructUnsafeUsingToString(Class rawEnumCls) {
      return constructUsingToString(rawEnumCls);
   }

   public Object findEnum(String key) {
      return (Enum)this._enumsById.get(key);
   }

   public Object getEnum(int index) {
      return index >= 0 && index < this._enums.length ? this._enums[index] : null;
   }

   public Class getEnumClass() {
      return this._enumClass;
   }

   public int lastValidIndex() {
      return this._enums.length - 1;
   }
}
