package org.codehaus.jackson.map.jsontype.impl;

import java.util.EnumMap;
import java.util.EnumSet;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

public class ClassNameIdResolver extends TypeIdResolverBase {
   public ClassNameIdResolver(JavaType baseType, TypeFactory typeFactory) {
      super(baseType, typeFactory);
   }

   public JsonTypeInfo.Id getMechanism() {
      return JsonTypeInfo.Id.CLASS;
   }

   public void registerSubtype(Class<?> type, String name) {
   }

   public String idFromValue(Object value) {
      return this._idFrom(value, value.getClass());
   }

   public String idFromValueAndType(Object value, Class<?> type) {
      return this._idFrom(value, type);
   }

   public JavaType typeFromId(String id) {
      if (id.indexOf(60) > 0) {
         JavaType t = TypeFactory.fromCanonical(id);
         return t;
      } else {
         try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> cls = Class.forName(id, true, loader);
            return this._typeFactory.constructSpecializedType(this._baseType, cls);
         } catch (ClassNotFoundException var4) {
            throw new IllegalArgumentException("Invalid type id '" + id + "' (for id type 'Id.class'): no such class found");
         } catch (Exception var5) {
            throw new IllegalArgumentException("Invalid type id '" + id + "' (for id type 'Id.class'): " + var5.getMessage(), var5);
         }
      }
   }

   protected final String _idFrom(Object value, Class<?> cls) {
      if (Enum.class.isAssignableFrom(cls) && !cls.isEnum()) {
         cls = cls.getSuperclass();
      }

      String str = cls.getName();
      if (str.startsWith("java.util")) {
         Class enumClass;
         if (value instanceof EnumSet) {
            enumClass = ClassUtil.findEnumType((EnumSet)value);
            str = TypeFactory.defaultInstance().constructCollectionType(EnumSet.class, enumClass).toCanonical();
         } else if (value instanceof EnumMap) {
            enumClass = ClassUtil.findEnumType((EnumMap)value);
            Class<?> valueClass = Object.class;
            str = TypeFactory.defaultInstance().constructMapType(EnumMap.class, enumClass, valueClass).toCanonical();
         } else {
            String end = str.substring(9);
            if ((end.startsWith(".Arrays$") || end.startsWith(".Collections$")) && str.indexOf("List") >= 0) {
               str = "java.util.ArrayList";
            }
         }
      }

      return str;
   }
}
