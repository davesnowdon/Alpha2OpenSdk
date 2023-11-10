package org.codehaus.jackson.map.util;

import java.io.IOException;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

public class JSONPObject implements JsonSerializableWithType {
   protected final String _function;
   protected final Object _value;
   protected final JavaType _serializationType;

   public JSONPObject(String function, Object value) {
      this(function, value, (JavaType)null);
   }

   public JSONPObject(String function, Object value, JavaType asType) {
      this._function = function;
      this._value = value;
      this._serializationType = asType;
   }

   /** @deprecated */
   @Deprecated
   public JSONPObject(String function, Object value, Class<?> rawType) {
      this._function = function;
      this._value = value;
      this._serializationType = rawType == null ? null : TypeFactory.defaultInstance().constructType((Type)rawType);
   }

   public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonProcessingException {
      this.serialize(jgen, provider);
   }

   public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeRaw(this._function);
      jgen.writeRaw('(');
      if (this._value == null) {
         provider.defaultSerializeNull(jgen);
      } else if (this._serializationType != null) {
         provider.findTypedValueSerializer((JavaType)this._serializationType, true, (BeanProperty)null).serialize(this._value, jgen, provider);
      } else {
         Class<?> cls = this._value.getClass();
         provider.findTypedValueSerializer((Class)cls, true, (BeanProperty)null).serialize(this._value, jgen, provider);
      }

      jgen.writeRaw(')');
   }

   public String getFunction() {
      return this._function;
   }

   public Object getValue() {
      return this._value;
   }

   public JavaType getSerializationType() {
      return this._serializationType;
   }
}
