package org.codehaus.jackson.map;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.type.JavaType;

public abstract class SerializerProvider {
   protected static final JavaType TYPE_OBJECT = TypeFactory.defaultInstance().uncheckedSimpleType(Object.class);
   protected final SerializationConfig _config;
   protected final Class<?> _serializationView;

   protected SerializerProvider(SerializationConfig config) {
      this._config = config;
      this._serializationView = config == null ? null : this._config.getSerializationView();
   }

   public abstract void setNullKeySerializer(JsonSerializer<Object> var1);

   public abstract void setNullValueSerializer(JsonSerializer<Object> var1);

   public abstract void setDefaultKeySerializer(JsonSerializer<Object> var1);

   public abstract void serializeValue(SerializationConfig var1, JsonGenerator var2, Object var3, SerializerFactory var4) throws IOException, JsonGenerationException;

   public abstract void serializeValue(SerializationConfig var1, JsonGenerator var2, Object var3, JavaType var4, SerializerFactory var5) throws IOException, JsonGenerationException;

   public abstract JsonSchema generateJsonSchema(Class<?> var1, SerializationConfig var2, SerializerFactory var3) throws JsonMappingException;

   public abstract boolean hasSerializerFor(SerializationConfig var1, Class<?> var2, SerializerFactory var3);

   public final SerializationConfig getConfig() {
      return this._config;
   }

   public final boolean isEnabled(SerializationConfig.Feature feature) {
      return this._config.isEnabled(feature);
   }

   public final Class<?> getSerializationView() {
      return this._serializationView;
   }

   public final FilterProvider getFilterProvider() {
      return this._config.getFilterProvider();
   }

   public JavaType constructType(Type type) {
      return this._config.getTypeFactory().constructType(type);
   }

   public abstract JsonSerializer<Object> findValueSerializer(Class<?> var1, BeanProperty var2) throws JsonMappingException;

   public abstract JsonSerializer<Object> findValueSerializer(JavaType var1, BeanProperty var2) throws JsonMappingException;

   public abstract JsonSerializer<Object> findTypedValueSerializer(Class<?> var1, boolean var2, BeanProperty var3) throws JsonMappingException;

   public abstract JsonSerializer<Object> findTypedValueSerializer(JavaType var1, boolean var2, BeanProperty var3) throws JsonMappingException;

   public abstract JsonSerializer<Object> findKeySerializer(JavaType var1, BeanProperty var2) throws JsonMappingException;

   /** @deprecated */
   @Deprecated
   public final JsonSerializer<Object> findValueSerializer(Class<?> runtimeType) throws JsonMappingException {
      return this.findValueSerializer((Class)runtimeType, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonSerializer<Object> findValueSerializer(JavaType serializationType) throws JsonMappingException {
      return this.findValueSerializer((JavaType)serializationType, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonSerializer<Object> findTypedValueSerializer(Class<?> valueType, boolean cache) throws JsonMappingException {
      return this.findTypedValueSerializer((Class)valueType, cache, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonSerializer<Object> findTypedValueSerializer(JavaType valueType, boolean cache) throws JsonMappingException {
      return this.findTypedValueSerializer((JavaType)valueType, cache, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonSerializer<Object> getKeySerializer() throws JsonMappingException {
      return this.findKeySerializer(TYPE_OBJECT, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonSerializer<Object> getKeySerializer(JavaType valueType, BeanProperty property) throws JsonMappingException {
      return this.findKeySerializer(valueType, property);
   }

   public abstract JsonSerializer<Object> getNullKeySerializer();

   public abstract JsonSerializer<Object> getNullValueSerializer();

   public abstract JsonSerializer<Object> getUnknownTypeSerializer(Class<?> var1);

   public final void defaultSerializeValue(Object value, JsonGenerator jgen) throws IOException, JsonProcessingException {
      if (value == null) {
         this.getNullValueSerializer().serialize((Object)null, jgen, this);
      } else {
         Class<?> cls = value.getClass();
         this.findTypedValueSerializer(cls, true).serialize(value, jgen, this);
      }

   }

   public final void defaultSerializeField(String fieldName, Object value, JsonGenerator jgen) throws IOException, JsonProcessingException {
      jgen.writeFieldName(fieldName);
      if (value == null) {
         this.getNullValueSerializer().serialize((Object)null, jgen, this);
      } else {
         Class<?> cls = value.getClass();
         this.findTypedValueSerializer(cls, true).serialize(value, jgen, this);
      }

   }

   public abstract void defaultSerializeDateValue(long var1, JsonGenerator var3) throws IOException, JsonProcessingException;

   public abstract void defaultSerializeDateValue(Date var1, JsonGenerator var2) throws IOException, JsonProcessingException;

   public final void defaultSerializeNull(JsonGenerator jgen) throws IOException, JsonProcessingException {
      this.getNullValueSerializer().serialize((Object)null, jgen, this);
   }

   public abstract int cachedSerializersCount();

   public abstract void flushCachedSerializers();
}
