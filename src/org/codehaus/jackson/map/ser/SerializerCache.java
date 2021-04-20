package org.codehaus.jackson.map.ser;

import java.util.HashMap;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ResolvableSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.impl.ReadOnlyClassToSerializerMap;
import org.codehaus.jackson.type.JavaType;

public final class SerializerCache {
   private HashMap<SerializerCache.TypeKey, JsonSerializer<Object>> _sharedMap = new HashMap(64);
   private ReadOnlyClassToSerializerMap _readOnlyMap = null;

   public SerializerCache() {
   }

   public ReadOnlyClassToSerializerMap getReadOnlyLookupMap() {
      ReadOnlyClassToSerializerMap m;
      synchronized(this) {
         m = this._readOnlyMap;
         if (m == null) {
            this._readOnlyMap = m = ReadOnlyClassToSerializerMap.from(this._sharedMap);
         }
      }

      return m.instance();
   }

   public synchronized int size() {
      return this._sharedMap.size();
   }

   public JsonSerializer<Object> untypedValueSerializer(Class<?> type) {
      synchronized(this) {
         return (JsonSerializer)this._sharedMap.get(new SerializerCache.TypeKey(type, false));
      }
   }

   public JsonSerializer<Object> untypedValueSerializer(JavaType type) {
      synchronized(this) {
         return (JsonSerializer)this._sharedMap.get(new SerializerCache.TypeKey(type, false));
      }
   }

   public JsonSerializer<Object> typedValueSerializer(JavaType type) {
      synchronized(this) {
         return (JsonSerializer)this._sharedMap.get(new SerializerCache.TypeKey(type, true));
      }
   }

   public JsonSerializer<Object> typedValueSerializer(Class<?> cls) {
      synchronized(this) {
         return (JsonSerializer)this._sharedMap.get(new SerializerCache.TypeKey(cls, true));
      }
   }

   public void addTypedSerializer(JavaType type, JsonSerializer<Object> ser) {
      synchronized(this) {
         if (this._sharedMap.put(new SerializerCache.TypeKey(type, true), ser) == null) {
            this._readOnlyMap = null;
         }

      }
   }

   public void addTypedSerializer(Class<?> cls, JsonSerializer<Object> ser) {
      synchronized(this) {
         if (this._sharedMap.put(new SerializerCache.TypeKey(cls, true), ser) == null) {
            this._readOnlyMap = null;
         }

      }
   }

   public void addAndResolveNonTypedSerializer(Class<?> type, JsonSerializer<Object> ser, SerializerProvider provider) throws JsonMappingException {
      synchronized(this) {
         if (this._sharedMap.put(new SerializerCache.TypeKey(type, false), ser) == null) {
            this._readOnlyMap = null;
         }

         if (ser instanceof ResolvableSerializer) {
            ((ResolvableSerializer)ser).resolve(provider);
         }

      }
   }

   public void addAndResolveNonTypedSerializer(JavaType type, JsonSerializer<Object> ser, SerializerProvider provider) throws JsonMappingException {
      synchronized(this) {
         if (this._sharedMap.put(new SerializerCache.TypeKey(type, false), ser) == null) {
            this._readOnlyMap = null;
         }

         if (ser instanceof ResolvableSerializer) {
            ((ResolvableSerializer)ser).resolve(provider);
         }

      }
   }

   public synchronized void flush() {
      this._sharedMap.clear();
   }

   public static final class TypeKey {
      protected int _hashCode;
      protected Class<?> _class;
      protected JavaType _type;
      protected boolean _isTyped;

      public TypeKey(Class<?> key, boolean typed) {
         this._class = key;
         this._type = null;
         this._isTyped = typed;
         this._hashCode = hash(key, typed);
      }

      public TypeKey(JavaType key, boolean typed) {
         this._type = key;
         this._class = null;
         this._isTyped = typed;
         this._hashCode = hash(key, typed);
      }

      private static final int hash(Class<?> cls, boolean typed) {
         int hash = cls.getName().hashCode();
         if (typed) {
            ++hash;
         }

         return hash;
      }

      private static final int hash(JavaType type, boolean typed) {
         int hash = type.hashCode() - 1;
         if (typed) {
            --hash;
         }

         return hash;
      }

      public void resetTyped(Class<?> cls) {
         this._type = null;
         this._class = cls;
         this._isTyped = true;
         this._hashCode = hash(cls, true);
      }

      public void resetUntyped(Class<?> cls) {
         this._type = null;
         this._class = cls;
         this._isTyped = false;
         this._hashCode = hash(cls, false);
      }

      public void resetTyped(JavaType type) {
         this._type = type;
         this._class = null;
         this._isTyped = true;
         this._hashCode = hash(type, true);
      }

      public void resetUntyped(JavaType type) {
         this._type = type;
         this._class = null;
         this._isTyped = false;
         this._hashCode = hash(type, false);
      }

      public final int hashCode() {
         return this._hashCode;
      }

      public final String toString() {
         return this._class != null ? "{class: " + this._class.getName() + ", typed? " + this._isTyped + "}" : "{type: " + this._type + ", typed? " + this._isTyped + "}";
      }

      public final boolean equals(Object o) {
         if (o == this) {
            return true;
         } else {
            SerializerCache.TypeKey other = (SerializerCache.TypeKey)o;
            if (other._isTyped == this._isTyped) {
               if (this._class != null) {
                  return other._class == this._class;
               } else {
                  return this._type.equals(other._type);
               }
            } else {
               return false;
            }
         }
      }
   }
}
