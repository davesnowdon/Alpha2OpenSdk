package org.codehaus.jackson.map.ser.impl;

import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.type.JavaType;

public abstract class PropertySerializerMap {
   public PropertySerializerMap() {
   }

   public abstract JsonSerializer<Object> serializerFor(Class<?> var1);

   public final PropertySerializerMap.SerializerAndMapResult findAndAddSerializer(Class<?> type, SerializerProvider provider, BeanProperty property) throws JsonMappingException {
      JsonSerializer<Object> serializer = provider.findValueSerializer(type, property);
      return new PropertySerializerMap.SerializerAndMapResult(serializer, this.newWith(type, serializer));
   }

   public final PropertySerializerMap.SerializerAndMapResult findAndAddSerializer(JavaType type, SerializerProvider provider, BeanProperty property) throws JsonMappingException {
      JsonSerializer<Object> serializer = provider.findValueSerializer(type, property);
      return new PropertySerializerMap.SerializerAndMapResult(serializer, this.newWith(type.getRawClass(), serializer));
   }

   protected abstract PropertySerializerMap newWith(Class<?> var1, JsonSerializer<Object> var2);

   public static PropertySerializerMap emptyMap() {
      return PropertySerializerMap.Empty.instance;
   }

   private static final class Multi extends PropertySerializerMap {
      private static final int MAX_ENTRIES = 8;
      private final PropertySerializerMap.TypeAndSerializer[] _entries;

      public Multi(PropertySerializerMap.TypeAndSerializer[] entries) {
         this._entries = entries;
      }

      public JsonSerializer<Object> serializerFor(Class<?> type) {
         int i = 0;

         for(int len = this._entries.length; i < len; ++i) {
            PropertySerializerMap.TypeAndSerializer entry = this._entries[i];
            if (entry.type == type) {
               return entry.serializer;
            }
         }

         return null;
      }

      protected PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
         int len = this._entries.length;
         if (len == 8) {
            return this;
         } else {
            PropertySerializerMap.TypeAndSerializer[] entries = new PropertySerializerMap.TypeAndSerializer[len + 1];
            System.arraycopy(this._entries, 0, entries, 0, len);
            entries[len] = new PropertySerializerMap.TypeAndSerializer(type, serializer);
            return new PropertySerializerMap.Multi(entries);
         }
      }
   }

   private static final class Double extends PropertySerializerMap {
      private final Class<?> _type1;
      private final Class<?> _type2;
      private final JsonSerializer<Object> _serializer1;
      private final JsonSerializer<Object> _serializer2;

      public Double(Class<?> type1, JsonSerializer<Object> serializer1, Class<?> type2, JsonSerializer<Object> serializer2) {
         this._type1 = type1;
         this._serializer1 = serializer1;
         this._type2 = type2;
         this._serializer2 = serializer2;
      }

      public JsonSerializer<Object> serializerFor(Class<?> type) {
         if (type == this._type1) {
            return this._serializer1;
         } else {
            return type == this._type2 ? this._serializer2 : null;
         }
      }

      protected PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
         PropertySerializerMap.TypeAndSerializer[] ts = new PropertySerializerMap.TypeAndSerializer[]{new PropertySerializerMap.TypeAndSerializer(this._type1, this._serializer1), new PropertySerializerMap.TypeAndSerializer(this._type2, this._serializer2)};
         return new PropertySerializerMap.Multi(ts);
      }
   }

   private static final class Single extends PropertySerializerMap {
      private final Class<?> _type;
      private final JsonSerializer<Object> _serializer;

      public Single(Class<?> type, JsonSerializer<Object> serializer) {
         this._type = type;
         this._serializer = serializer;
      }

      public JsonSerializer<Object> serializerFor(Class<?> type) {
         return type == this._type ? this._serializer : null;
      }

      protected PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
         return new PropertySerializerMap.Double(this._type, this._serializer, type, serializer);
      }
   }

   private static final class Empty extends PropertySerializerMap {
      protected static final PropertySerializerMap.Empty instance = new PropertySerializerMap.Empty();

      private Empty() {
      }

      public JsonSerializer<Object> serializerFor(Class<?> type) {
         return null;
      }

      protected PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
         return new PropertySerializerMap.Single(type, serializer);
      }
   }

   private static final class TypeAndSerializer {
      public final Class<?> type;
      public final JsonSerializer<Object> serializer;

      public TypeAndSerializer(Class<?> type, JsonSerializer<Object> serializer) {
         this.type = type;
         this.serializer = serializer;
      }
   }

   public static final class SerializerAndMapResult {
      public final JsonSerializer<Object> serializer;
      public final PropertySerializerMap map;

      public SerializerAndMapResult(JsonSerializer<Object> serializer, PropertySerializerMap map) {
         this.serializer = serializer;
         this.map = map;
      }
   }
}
