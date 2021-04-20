package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.io.NumberInput;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.KeyDeserializer;

public abstract class StdKeyDeserializer extends KeyDeserializer {
   protected final Class<?> _keyClass;

   protected StdKeyDeserializer(Class<?> cls) {
      this._keyClass = cls;
   }

   public final Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (key == null) {
         return null;
      } else {
         try {
            Object result = this._parse(key, ctxt);
            if (result != null) {
               return result;
            }
         } catch (Exception var4) {
            throw ctxt.weirdKeyException(this._keyClass, key, "not a valid representation: " + var4.getMessage());
         }

         throw ctxt.weirdKeyException(this._keyClass, key, "not a valid representation");
      }
   }

   public Class<?> getKeyClass() {
      return this._keyClass;
   }

   protected abstract Object _parse(String var1, DeserializationContext var2) throws Exception;

   protected int _parseInt(String key) throws IllegalArgumentException {
      return Integer.parseInt(key);
   }

   protected long _parseLong(String key) throws IllegalArgumentException {
      return Long.parseLong(key);
   }

   protected double _parseDouble(String key) throws IllegalArgumentException {
      return NumberInput.parseDouble(key);
   }

   static final class StringFactoryKeyDeserializer extends StdKeyDeserializer {
      final Method _factoryMethod;

      public StringFactoryKeyDeserializer(Method fm) {
         super(fm.getDeclaringClass());
         this._factoryMethod = fm;
      }

      public Object _parse(String key, DeserializationContext ctxt) throws Exception {
         return this._factoryMethod.invoke((Object)null, key);
      }
   }

   static final class StringCtorKeyDeserializer extends StdKeyDeserializer {
      final Constructor<?> _ctor;

      public StringCtorKeyDeserializer(Constructor<?> ctor) {
         super(ctor.getDeclaringClass());
         this._ctor = ctor;
      }

      public Object _parse(String key, DeserializationContext ctxt) throws Exception {
         return this._ctor.newInstance(key);
      }
   }

   static final class EnumKD extends StdKeyDeserializer {
      final EnumResolver<?> _resolver;

      EnumKD(EnumResolver<?> er) {
         super(er.getEnumClass());
         this._resolver = er;
      }

      public Enum<?> _parse(String key, DeserializationContext ctxt) throws JsonMappingException {
         Enum<?> e = this._resolver.findEnum(key);
         if (e == null) {
            throw ctxt.weirdKeyException(this._keyClass, key, "not one of values for Enum class");
         } else {
            return e;
         }
      }
   }

   static final class FloatKD extends StdKeyDeserializer {
      FloatKD() {
         super(Float.class);
      }

      public Float _parse(String key, DeserializationContext ctxt) throws JsonMappingException {
         return (float)this._parseDouble(key);
      }
   }

   static final class DoubleKD extends StdKeyDeserializer {
      DoubleKD() {
         super(Double.class);
      }

      public Double _parse(String key, DeserializationContext ctxt) throws JsonMappingException {
         return this._parseDouble(key);
      }
   }

   static final class LongKD extends StdKeyDeserializer {
      LongKD() {
         super(Long.class);
      }

      public Long _parse(String key, DeserializationContext ctxt) throws JsonMappingException {
         return this._parseLong(key);
      }
   }

   static final class IntKD extends StdKeyDeserializer {
      IntKD() {
         super(Integer.class);
      }

      public Integer _parse(String key, DeserializationContext ctxt) throws JsonMappingException {
         return this._parseInt(key);
      }
   }

   static final class CharKD extends StdKeyDeserializer {
      CharKD() {
         super(Character.class);
      }

      public Character _parse(String key, DeserializationContext ctxt) throws JsonMappingException {
         if (key.length() == 1) {
            return key.charAt(0);
         } else {
            throw ctxt.weirdKeyException(this._keyClass, key, "can only convert 1-character Strings");
         }
      }
   }

   static final class ShortKD extends StdKeyDeserializer {
      ShortKD() {
         super(Integer.class);
      }

      public Short _parse(String key, DeserializationContext ctxt) throws JsonMappingException {
         int value = this._parseInt(key);
         if (value >= -32768 && value <= 32767) {
            return (short)value;
         } else {
            throw ctxt.weirdKeyException(this._keyClass, key, "overflow, value can not be represented as 16-bit value");
         }
      }
   }

   static final class ByteKD extends StdKeyDeserializer {
      ByteKD() {
         super(Byte.class);
      }

      public Byte _parse(String key, DeserializationContext ctxt) throws JsonMappingException {
         int value = this._parseInt(key);
         if (value >= -128 && value <= 127) {
            return (byte)value;
         } else {
            throw ctxt.weirdKeyException(this._keyClass, key, "overflow, value can not be represented as 8-bit value");
         }
      }
   }

   static final class BoolKD extends StdKeyDeserializer {
      BoolKD() {
         super(Boolean.class);
      }

      public Boolean _parse(String key, DeserializationContext ctxt) throws JsonMappingException {
         if ("true".equals(key)) {
            return Boolean.TRUE;
         } else if ("false".equals(key)) {
            return Boolean.FALSE;
         } else {
            throw ctxt.weirdKeyException(this._keyClass, key, "value not 'true' or 'false'");
         }
      }
   }
}
