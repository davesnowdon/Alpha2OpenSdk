package org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.ContextualSerializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerFactory;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.ser.impl.ReadOnlyClassToSerializerMap;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.util.RootNameLookup;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.type.JavaType;

public class StdSerializerProvider extends SerializerProvider {
   static final boolean CACHE_UNKNOWN_MAPPINGS = false;
   public static final JsonSerializer<Object> DEFAULT_NULL_KEY_SERIALIZER = new FailingSerializer("Null key for a Map not allowed in JSON (use a converting NullKeySerializer?)");
   public static final JsonSerializer<Object> DEFAULT_KEY_SERIALIZER = new StdKeySerializer();
   public static final JsonSerializer<Object> DEFAULT_UNKNOWN_SERIALIZER = new SerializerBase<Object>(Object.class) {
      public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonMappingException {
         if (provider.isEnabled(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS)) {
            this.failForEmpty(value);
         }

         jgen.writeStartObject();
         jgen.writeEndObject();
      }

      public final void serializeWithType(Object value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
         if (provider.isEnabled(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS)) {
            this.failForEmpty(value);
         }

         typeSer.writeTypePrefixForObject(value, jgen);
         typeSer.writeTypeSuffixForObject(value, jgen);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
         return null;
      }

      protected void failForEmpty(Object value) throws JsonMappingException {
         throw new JsonMappingException("No serializer found for class " + value.getClass().getName() + " and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS) )");
      }
   };
   protected final SerializerFactory _serializerFactory;
   protected final SerializerCache _serializerCache;
   protected final RootNameLookup _rootNames;
   protected JsonSerializer<Object> _unknownTypeSerializer;
   protected JsonSerializer<Object> _keySerializer;
   protected JsonSerializer<Object> _nullValueSerializer;
   protected JsonSerializer<Object> _nullKeySerializer;
   protected final ReadOnlyClassToSerializerMap _knownSerializers;
   protected DateFormat _dateFormat;

   public StdSerializerProvider() {
      super((SerializationConfig)null);
      this._unknownTypeSerializer = DEFAULT_UNKNOWN_SERIALIZER;
      this._keySerializer = DEFAULT_KEY_SERIALIZER;
      this._nullValueSerializer = NullSerializer.instance;
      this._nullKeySerializer = DEFAULT_NULL_KEY_SERIALIZER;
      this._serializerFactory = null;
      this._serializerCache = new SerializerCache();
      this._knownSerializers = null;
      this._rootNames = new RootNameLookup();
   }

   protected StdSerializerProvider(SerializationConfig config, StdSerializerProvider src, SerializerFactory f) {
      super(config);
      this._unknownTypeSerializer = DEFAULT_UNKNOWN_SERIALIZER;
      this._keySerializer = DEFAULT_KEY_SERIALIZER;
      this._nullValueSerializer = NullSerializer.instance;
      this._nullKeySerializer = DEFAULT_NULL_KEY_SERIALIZER;
      if (config == null) {
         throw new NullPointerException();
      } else {
         this._serializerFactory = f;
         this._serializerCache = src._serializerCache;
         this._unknownTypeSerializer = src._unknownTypeSerializer;
         this._keySerializer = src._keySerializer;
         this._nullValueSerializer = src._nullValueSerializer;
         this._nullKeySerializer = src._nullKeySerializer;
         this._rootNames = src._rootNames;
         this._knownSerializers = this._serializerCache.getReadOnlyLookupMap();
      }
   }

   protected StdSerializerProvider createInstance(SerializationConfig config, SerializerFactory jsf) {
      return new StdSerializerProvider(config, this, jsf);
   }

   public void setDefaultKeySerializer(JsonSerializer<Object> ks) {
      if (ks == null) {
         throw new IllegalArgumentException("Can not pass null JsonSerializer");
      } else {
         this._keySerializer = ks;
      }
   }

   public void setNullValueSerializer(JsonSerializer<Object> nvs) {
      if (nvs == null) {
         throw new IllegalArgumentException("Can not pass null JsonSerializer");
      } else {
         this._nullValueSerializer = nvs;
      }
   }

   public void setNullKeySerializer(JsonSerializer<Object> nks) {
      if (nks == null) {
         throw new IllegalArgumentException("Can not pass null JsonSerializer");
      } else {
         this._nullKeySerializer = nks;
      }
   }

   public final void serializeValue(SerializationConfig config, JsonGenerator jgen, Object value, SerializerFactory jsf) throws IOException, JsonGenerationException {
      if (jsf == null) {
         throw new IllegalArgumentException("Can not pass null serializerFactory");
      } else {
         StdSerializerProvider inst = this.createInstance(config, jsf);
         if (inst.getClass() != this.getClass()) {
            throw new IllegalStateException("Broken serializer provider: createInstance returned instance of type " + inst.getClass() + "; blueprint of type " + this.getClass());
         } else {
            inst._serializeValue(jgen, value);
         }
      }
   }

   public final void serializeValue(SerializationConfig config, JsonGenerator jgen, Object value, JavaType rootType, SerializerFactory jsf) throws IOException, JsonGenerationException {
      if (jsf == null) {
         throw new IllegalArgumentException("Can not pass null serializerFactory");
      } else {
         StdSerializerProvider inst = this.createInstance(config, jsf);
         if (inst.getClass() != this.getClass()) {
            throw new IllegalStateException("Broken serializer provider: createInstance returned instance of type " + inst.getClass() + "; blueprint of type " + this.getClass());
         } else {
            inst._serializeValue(jgen, value, rootType);
         }
      }
   }

   public JsonSchema generateJsonSchema(Class<?> type, SerializationConfig config, SerializerFactory jsf) throws JsonMappingException {
      if (type == null) {
         throw new IllegalArgumentException("A class must be provided");
      } else {
         StdSerializerProvider inst = this.createInstance(config, jsf);
         if (inst.getClass() != this.getClass()) {
            throw new IllegalStateException("Broken serializer provider: createInstance returned instance of type " + inst.getClass() + "; blueprint of type " + this.getClass());
         } else {
            JsonSerializer<Object> ser = inst.findValueSerializer((Class)type, (BeanProperty)null);
            JsonNode schemaNode = ser instanceof SchemaAware ? ((SchemaAware)ser).getSchema(inst, (Type)null) : JsonSchema.getDefaultSchemaNode();
            if (!(schemaNode instanceof ObjectNode)) {
               throw new IllegalArgumentException("Class " + type.getName() + " would not be serialized as a JSON object and therefore has no schema");
            } else {
               return new JsonSchema((ObjectNode)schemaNode);
            }
         }
      }
   }

   public boolean hasSerializerFor(SerializationConfig config, Class<?> cls, SerializerFactory jsf) {
      return this.createInstance(config, jsf)._findExplicitUntypedSerializer(cls, (BeanProperty)null) != null;
   }

   public int cachedSerializersCount() {
      return this._serializerCache.size();
   }

   public void flushCachedSerializers() {
      this._serializerCache.flush();
   }

   public JsonSerializer<Object> findValueSerializer(Class<?> valueType, BeanProperty property) throws JsonMappingException {
      JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
      if (ser == null) {
         ser = this._serializerCache.untypedValueSerializer(valueType);
         if (ser == null) {
            ser = this._serializerCache.untypedValueSerializer(this._config.constructType(valueType));
            if (ser == null) {
               ser = this._createAndCacheUntypedSerializer(valueType, property);
               if (ser == null) {
                  ser = this.getUnknownTypeSerializer(valueType);
                  return ser;
               }
            }
         }
      }

      return ser instanceof ContextualSerializer ? ((ContextualSerializer)ser).createContextual(this._config, property) : ser;
   }

   public JsonSerializer<Object> findValueSerializer(JavaType valueType, BeanProperty property) throws JsonMappingException {
      JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
      if (ser == null) {
         ser = this._serializerCache.untypedValueSerializer(valueType);
         if (ser == null) {
            ser = this._createAndCacheUntypedSerializer(valueType, property);
            if (ser == null) {
               ser = this.getUnknownTypeSerializer(valueType.getRawClass());
               return ser;
            }
         }
      }

      return ser instanceof ContextualSerializer ? ((ContextualSerializer)ser).createContextual(this._config, property) : ser;
   }

   public JsonSerializer<Object> findTypedValueSerializer(Class<?> valueType, boolean cache, BeanProperty property) throws JsonMappingException {
      JsonSerializer<Object> ser = this._knownSerializers.typedValueSerializer(valueType);
      if (ser != null) {
         return ser;
      } else {
         ser = this._serializerCache.typedValueSerializer(valueType);
         if (ser != null) {
            return ser;
         } else {
            JsonSerializer<Object> ser = this.findValueSerializer(valueType, property);
            TypeSerializer typeSer = this._serializerFactory.createTypeSerializer(this._config, this._config.constructType(valueType), property);
            if (typeSer != null) {
               ser = new StdSerializerProvider.WrappedSerializer(typeSer, (JsonSerializer)ser);
            }

            if (cache) {
               this._serializerCache.addTypedSerializer((Class)valueType, (JsonSerializer)ser);
            }

            return (JsonSerializer)ser;
         }
      }
   }

   public JsonSerializer<Object> findTypedValueSerializer(JavaType valueType, boolean cache, BeanProperty property) throws JsonMappingException {
      JsonSerializer<Object> ser = this._knownSerializers.typedValueSerializer(valueType);
      if (ser != null) {
         return ser;
      } else {
         ser = this._serializerCache.typedValueSerializer(valueType);
         if (ser != null) {
            return ser;
         } else {
            JsonSerializer<Object> ser = this.findValueSerializer(valueType, property);
            TypeSerializer typeSer = this._serializerFactory.createTypeSerializer(this._config, valueType, property);
            if (typeSer != null) {
               ser = new StdSerializerProvider.WrappedSerializer(typeSer, (JsonSerializer)ser);
            }

            if (cache) {
               this._serializerCache.addTypedSerializer((JavaType)valueType, (JsonSerializer)ser);
            }

            return (JsonSerializer)ser;
         }
      }
   }

   public JsonSerializer<Object> findKeySerializer(JavaType keyType, BeanProperty property) throws JsonMappingException {
      JsonSerializer<Object> ser = this._serializerFactory.createKeySerializer(this._config, keyType, property);
      if (ser == null) {
         ser = this._keySerializer;
      }

      if (ser instanceof ContextualSerializer) {
         ContextualSerializer<?> contextual = (ContextualSerializer)ser;
         ser = contextual.createContextual(this._config, property);
      }

      return ser;
   }

   public JsonSerializer<Object> getNullKeySerializer() {
      return this._nullKeySerializer;
   }

   public JsonSerializer<Object> getNullValueSerializer() {
      return this._nullValueSerializer;
   }

   public JsonSerializer<Object> getUnknownTypeSerializer(Class<?> unknownType) {
      return this._unknownTypeSerializer;
   }

   public final void defaultSerializeDateValue(long timestamp, JsonGenerator jgen) throws IOException, JsonProcessingException {
      if (this.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
         jgen.writeNumber(timestamp);
      } else {
         if (this._dateFormat == null) {
            this._dateFormat = (DateFormat)this._config.getDateFormat().clone();
         }

         jgen.writeString(this._dateFormat.format(new Date(timestamp)));
      }

   }

   public final void defaultSerializeDateValue(Date date, JsonGenerator jgen) throws IOException, JsonProcessingException {
      if (this.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
         jgen.writeNumber(date.getTime());
      } else {
         if (this._dateFormat == null) {
            DateFormat blueprint = this._config.getDateFormat();
            this._dateFormat = (DateFormat)blueprint.clone();
         }

         jgen.writeString(this._dateFormat.format(date));
      }

   }

   protected void _serializeValue(JsonGenerator jgen, Object value) throws IOException, JsonProcessingException {
      JsonSerializer ser;
      boolean wrap;
      if (value == null) {
         ser = this.getNullValueSerializer();
         wrap = false;
      } else {
         Class<?> cls = value.getClass();
         ser = this.findTypedValueSerializer((Class)cls, true, (BeanProperty)null);
         wrap = this._config.isEnabled(SerializationConfig.Feature.WRAP_ROOT_VALUE);
         if (wrap) {
            jgen.writeStartObject();
            jgen.writeFieldName(this._rootNames.findRootName((Class)value.getClass(), this._config));
         }
      }

      try {
         ser.serialize(value, jgen, this);
         if (wrap) {
            jgen.writeEndObject();
         }

      } catch (IOException var7) {
         throw var7;
      } catch (Exception var8) {
         String msg = var8.getMessage();
         if (msg == null) {
            msg = "[no message for " + var8.getClass().getName() + "]";
         }

         throw new JsonMappingException(msg, var8);
      }
   }

   protected void _serializeValue(JsonGenerator jgen, Object value, JavaType rootType) throws IOException, JsonProcessingException {
      boolean wrap;
      JsonSerializer ser;
      if (value == null) {
         ser = this.getNullValueSerializer();
         wrap = false;
      } else {
         if (!rootType.getRawClass().isAssignableFrom(value.getClass())) {
            this._reportIncompatibleRootType(value, rootType);
         }

         ser = this.findTypedValueSerializer((JavaType)rootType, true, (BeanProperty)null);
         wrap = this._config.isEnabled(SerializationConfig.Feature.WRAP_ROOT_VALUE);
         if (wrap) {
            jgen.writeStartObject();
            jgen.writeFieldName(this._rootNames.findRootName((JavaType)rootType, this._config));
         }
      }

      try {
         ser.serialize(value, jgen, this);
         if (wrap) {
            jgen.writeEndObject();
         }

      } catch (IOException var8) {
         throw var8;
      } catch (Exception var9) {
         String msg = var9.getMessage();
         if (msg == null) {
            msg = "[no message for " + var9.getClass().getName() + "]";
         }

         throw new JsonMappingException(msg, var9);
      }
   }

   protected void _reportIncompatibleRootType(Object value, JavaType rootType) throws IOException, JsonProcessingException {
      if (rootType.isPrimitive()) {
         Class<?> wrapperType = ClassUtil.wrapperType(rootType.getRawClass());
         if (wrapperType.isAssignableFrom(value.getClass())) {
            return;
         }
      }

      throw new JsonMappingException("Incompatible types: declared root type (" + rootType + ") vs " + value.getClass().getName());
   }

   protected JsonSerializer<Object> _findExplicitUntypedSerializer(Class<?> runtimeType, BeanProperty property) {
      JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(runtimeType);
      if (ser != null) {
         return ser;
      } else {
         ser = this._serializerCache.untypedValueSerializer(runtimeType);
         if (ser != null) {
            return ser;
         } else {
            try {
               return this._createAndCacheUntypedSerializer(runtimeType, property);
            } catch (Exception var5) {
               return null;
            }
         }
      }
   }

   protected JsonSerializer<Object> _createAndCacheUntypedSerializer(Class<?> type, BeanProperty property) throws JsonMappingException {
      JsonSerializer ser;
      try {
         ser = this._createUntypedSerializer(this._config.constructType(type), property);
      } catch (IllegalArgumentException var5) {
         throw new JsonMappingException(var5.getMessage(), (JsonLocation)null, var5);
      }

      if (ser != null) {
         this._serializerCache.addAndResolveNonTypedSerializer((Class)type, ser, this);
      }

      return ser;
   }

   protected JsonSerializer<Object> _createAndCacheUntypedSerializer(JavaType type, BeanProperty property) throws JsonMappingException {
      JsonSerializer ser;
      try {
         ser = this._createUntypedSerializer(type, property);
      } catch (IllegalArgumentException var5) {
         throw new JsonMappingException(var5.getMessage(), (JsonLocation)null, var5);
      }

      if (ser != null) {
         this._serializerCache.addAndResolveNonTypedSerializer((JavaType)type, ser, this);
      }

      return ser;
   }

   protected JsonSerializer<Object> _createUntypedSerializer(JavaType type, BeanProperty property) throws JsonMappingException {
      return this._serializerFactory.createSerializer(this._config, type, property);
   }

   private static final class WrappedSerializer extends JsonSerializer<Object> {
      protected final TypeSerializer _typeSerializer;
      protected final JsonSerializer<Object> _serializer;

      public WrappedSerializer(TypeSerializer typeSer, JsonSerializer<Object> ser) {
         this._typeSerializer = typeSer;
         this._serializer = ser;
      }

      public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
         this._serializer.serializeWithType(value, jgen, provider, this._typeSerializer);
      }

      public void serializeWithType(Object value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonProcessingException {
         this._serializer.serializeWithType(value, jgen, provider, typeSer);
      }

      public Class<Object> handledType() {
         return Object.class;
      }
   }
}
