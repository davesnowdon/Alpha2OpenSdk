package org.codehaus.jackson.map.util;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.io.SerializedString;
import org.codehaus.jackson.map.AnnotationIntrospector;

public final class EnumValues {
   private final EnumMap<?, SerializedString> _values;

   private EnumValues(Map<Enum<?>, SerializedString> v) {
      this._values = new EnumMap(v);
   }

   public static EnumValues construct(Class<Enum<?>> enumClass, AnnotationIntrospector intr) {
      return constructFromName(enumClass, intr);
   }

   public static EnumValues constructFromName(Class<Enum<?>> enumClass, AnnotationIntrospector intr) {
      Class<? extends Enum<?>> cls = ClassUtil.findEnumType(enumClass);
      Enum<?>[] values = (Enum[])cls.getEnumConstants();
      if (values == null) {
         throw new IllegalArgumentException("Can not determine enum constants for Class " + enumClass.getName());
      } else {
         Map<Enum<?>, SerializedString> map = new HashMap();
         Enum[] arr$ = values;
         int len$ = values.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enum<?> en = arr$[i$];
            String value = intr.findEnumValue(en);
            map.put(en, new SerializedString(value));
         }

         return new EnumValues(map);
      }
   }

   public static EnumValues constructFromToString(Class<Enum<?>> enumClass, AnnotationIntrospector intr) {
      Class<? extends Enum<?>> cls = ClassUtil.findEnumType(enumClass);
      Enum<?>[] values = (Enum[])cls.getEnumConstants();
      if (values == null) {
         throw new IllegalArgumentException("Can not determine enum constants for Class " + enumClass.getName());
      } else {
         Map<Enum<?>, SerializedString> map = new HashMap();
         Enum[] arr$ = values;
         int len$ = values.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enum<?> en = arr$[i$];
            map.put(en, new SerializedString(en.toString()));
         }

         return new EnumValues(map);
      }
   }

   /** @deprecated */
   @Deprecated
   public String valueFor(Enum<?> key) {
      SerializedString sstr = (SerializedString)this._values.get(key);
      return sstr == null ? null : sstr.getValue();
   }

   public SerializedString serializedValueFor(Enum<?> key) {
      return (SerializedString)this._values.get(key);
   }

   public Collection<SerializedString> values() {
      return this._values.values();
   }
}
