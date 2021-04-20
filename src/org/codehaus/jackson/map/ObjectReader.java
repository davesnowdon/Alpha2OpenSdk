package org.codehaus.jackson.map;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import org.codehaus.jackson.FormatSchema;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.map.deser.StdDeserializationContext;
import org.codehaus.jackson.map.type.SimpleType;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.TreeTraversingParser;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.util.VersionUtil;

public class ObjectReader extends ObjectCodec implements Versioned {
   private static final JavaType JSON_NODE_TYPE = SimpleType.constructUnsafe(JsonNode.class);
   protected final DeserializationConfig _config;
   protected final ConcurrentHashMap<JavaType, JsonDeserializer<Object>> _rootDeserializers;
   protected final DeserializerProvider _provider;
   protected final JsonFactory _jsonFactory;
   protected final JavaType _valueType;
   protected final Object _valueToUpdate;
   protected final FormatSchema _schema;

   protected ObjectReader(ObjectMapper mapper, DeserializationConfig config) {
      this((ObjectMapper)mapper, config, (JavaType)null, (Object)null, (FormatSchema)null);
   }

   protected ObjectReader(ObjectMapper mapper, DeserializationConfig config, JavaType valueType, Object valueToUpdate, FormatSchema schema) {
      this._config = config;
      this._rootDeserializers = mapper._rootDeserializers;
      this._provider = mapper._deserializerProvider;
      this._jsonFactory = mapper._jsonFactory;
      this._valueType = valueType;
      this._valueToUpdate = valueToUpdate;
      if (valueToUpdate != null && valueType.isArrayType()) {
         throw new IllegalArgumentException("Can not update an array value");
      } else {
         this._schema = schema;
      }
   }

   protected ObjectReader(ObjectReader base, DeserializationConfig config, JavaType valueType, Object valueToUpdate, FormatSchema schema) {
      this._config = config;
      this._rootDeserializers = base._rootDeserializers;
      this._provider = base._provider;
      this._jsonFactory = base._jsonFactory;
      this._valueType = valueType;
      this._valueToUpdate = valueToUpdate;
      if (valueToUpdate != null && valueType.isArrayType()) {
         throw new IllegalArgumentException("Can not update an array value");
      } else {
         this._schema = schema;
      }
   }

   public Version version() {
      return VersionUtil.versionFor(this.getClass());
   }

   public ObjectReader withType(JavaType valueType) {
      return valueType == this._valueType ? this : new ObjectReader(this, this._config, valueType, this._valueToUpdate, this._schema);
   }

   public ObjectReader withType(Class<?> valueType) {
      return this.withType(this._config.constructType(valueType));
   }

   public ObjectReader withType(Type valueType) {
      return this.withType(this._config.getTypeFactory().constructType(valueType));
   }

   public ObjectReader withType(TypeReference<?> valueTypeRef) {
      return this.withType(this._config.getTypeFactory().constructType(valueTypeRef.getType()));
   }

   public ObjectReader withNodeFactory(JsonNodeFactory f) {
      return f == this._config.getNodeFactory() ? this : new ObjectReader(this, this._config.withNodeFactory(f), this._valueType, this._valueToUpdate, this._schema);
   }

   public ObjectReader withValueToUpdate(Object value) {
      if (value == this._valueToUpdate) {
         return this;
      } else if (value == null) {
         throw new IllegalArgumentException("cat not update null value");
      } else {
         JavaType t = this._config.constructType(value.getClass());
         return new ObjectReader(this, this._config, t, value, this._schema);
      }
   }

   public ObjectReader withSchema(FormatSchema schema) {
      return this._schema == schema ? this : new ObjectReader(this, this._config, this._valueType, this._valueToUpdate, schema);
   }

   public <T> T readValue(JsonParser jp) throws IOException, JsonProcessingException {
      return this._bind(jp);
   }

   public JsonNode readTree(JsonParser jp) throws IOException, JsonProcessingException {
      return this._bindAsTree(jp);
   }

   public <T> T readValue(InputStream src) throws IOException, JsonProcessingException {
      return this._bindAndClose(this._jsonFactory.createJsonParser(src));
   }

   public <T> T readValue(Reader src) throws IOException, JsonProcessingException {
      return this._bindAndClose(this._jsonFactory.createJsonParser(src));
   }

   public <T> T readValue(String src) throws IOException, JsonProcessingException {
      return this._bindAndClose(this._jsonFactory.createJsonParser(src));
   }

   public <T> T readValue(byte[] src) throws IOException, JsonProcessingException {
      return this._bindAndClose(this._jsonFactory.createJsonParser(src));
   }

   public <T> T readValue(byte[] src, int offset, int length) throws IOException, JsonProcessingException {
      return this._bindAndClose(this._jsonFactory.createJsonParser(src, offset, length));
   }

   public <T> T readValue(File src) throws IOException, JsonProcessingException {
      return this._bindAndClose(this._jsonFactory.createJsonParser(src));
   }

   public <T> T readValue(URL src) throws IOException, JsonProcessingException {
      return this._bindAndClose(this._jsonFactory.createJsonParser(src));
   }

   public <T> T readValue(JsonNode src) throws IOException, JsonProcessingException {
      return this._bindAndClose(this.treeAsTokens(src));
   }

   public JsonNode readTree(InputStream in) throws IOException, JsonProcessingException {
      return this._bindAndCloseAsTree(this._jsonFactory.createJsonParser(in));
   }

   public JsonNode readTree(Reader r) throws IOException, JsonProcessingException {
      return this._bindAndCloseAsTree(this._jsonFactory.createJsonParser(r));
   }

   public JsonNode readTree(String content) throws IOException, JsonProcessingException {
      return this._bindAndCloseAsTree(this._jsonFactory.createJsonParser(content));
   }

   public <T> MappingIterator<T> readValues(JsonParser jp) throws IOException, JsonProcessingException {
      DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
      return new MappingIterator(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType));
   }

   public <T> MappingIterator<T> readValues(InputStream src) throws IOException, JsonProcessingException {
      JsonParser jp = this._jsonFactory.createJsonParser(src);
      if (this._schema != null) {
         jp.setSchema(this._schema);
      }

      DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
      return new MappingIterator(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType));
   }

   public <T> MappingIterator<T> readValues(Reader src) throws IOException, JsonProcessingException {
      JsonParser jp = this._jsonFactory.createJsonParser(src);
      if (this._schema != null) {
         jp.setSchema(this._schema);
      }

      DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
      return new MappingIterator(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType));
   }

   public <T> MappingIterator<T> readValues(String json) throws IOException, JsonProcessingException {
      JsonParser jp = this._jsonFactory.createJsonParser(json);
      if (this._schema != null) {
         jp.setSchema(this._schema);
      }

      DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
      return new MappingIterator(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType));
   }

   public <T> MappingIterator<T> readValues(byte[] src, int offset, int length) throws IOException, JsonProcessingException {
      JsonParser jp = this._jsonFactory.createJsonParser(src, offset, length);
      if (this._schema != null) {
         jp.setSchema(this._schema);
      }

      DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
      return new MappingIterator(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType));
   }

   public <T> MappingIterator<T> readValues(File src) throws IOException, JsonProcessingException {
      JsonParser jp = this._jsonFactory.createJsonParser(src);
      if (this._schema != null) {
         jp.setSchema(this._schema);
      }

      DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
      return new MappingIterator(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType));
   }

   public <T> MappingIterator<T> readValues(URL src) throws IOException, JsonProcessingException {
      JsonParser jp = this._jsonFactory.createJsonParser(src);
      if (this._schema != null) {
         jp.setSchema(this._schema);
      }

      DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
      return new MappingIterator(this._valueType, jp, ctxt, this._findRootDeserializer(this._config, this._valueType));
   }

   protected Object _bind(JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
      JsonToken t = _initForReading(jp);
      Object result;
      if (t != JsonToken.VALUE_NULL && t != JsonToken.END_ARRAY && t != JsonToken.END_OBJECT) {
         DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
         if (this._valueToUpdate == null) {
            result = this._findRootDeserializer(this._config, this._valueType).deserialize(jp, ctxt);
         } else {
            this._findRootDeserializer(this._config, this._valueType).deserialize(jp, ctxt, this._valueToUpdate);
            result = this._valueToUpdate;
         }
      } else {
         result = this._valueToUpdate;
      }

      jp.clearCurrentToken();
      return result;
   }

   protected Object _bindAndClose(JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
      if (this._schema != null) {
         jp.setSchema(this._schema);
      }

      Object var13;
      try {
         JsonToken t = _initForReading(jp);
         Object result;
         if (t != JsonToken.VALUE_NULL && t != JsonToken.END_ARRAY && t != JsonToken.END_OBJECT) {
            DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
            if (this._valueToUpdate == null) {
               result = this._findRootDeserializer(this._config, this._valueType).deserialize(jp, ctxt);
            } else {
               this._findRootDeserializer(this._config, this._valueType).deserialize(jp, ctxt, this._valueToUpdate);
               result = this._valueToUpdate;
            }
         } else {
            result = this._valueToUpdate;
         }

         var13 = result;
      } finally {
         try {
            jp.close();
         } catch (IOException var11) {
         }

      }

      return var13;
   }

   protected JsonNode _bindAsTree(JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
      JsonToken t = _initForReading(jp);
      Object result;
      if (t != JsonToken.VALUE_NULL && t != JsonToken.END_ARRAY && t != JsonToken.END_OBJECT) {
         DeserializationContext ctxt = this._createDeserializationContext(jp, this._config);
         result = (JsonNode)this._findRootDeserializer(this._config, JSON_NODE_TYPE).deserialize(jp, ctxt);
      } else {
         result = NullNode.instance;
      }

      jp.clearCurrentToken();
      return (JsonNode)result;
   }

   protected JsonNode _bindAndCloseAsTree(JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
      if (this._schema != null) {
         jp.setSchema(this._schema);
      }

      JsonNode var2;
      try {
         var2 = this._bindAsTree(jp);
      } finally {
         try {
            jp.close();
         } catch (IOException var9) {
         }

      }

      return var2;
   }

   protected static JsonToken _initForReading(JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
      JsonToken t = jp.getCurrentToken();
      if (t == null) {
         t = jp.nextToken();
         if (t == null) {
            throw new EOFException("No content to map to Object due to end of input");
         }
      }

      return t;
   }

   protected JsonDeserializer<Object> _findRootDeserializer(DeserializationConfig cfg, JavaType valueType) throws JsonMappingException {
      JsonDeserializer<Object> deser = (JsonDeserializer)this._rootDeserializers.get(valueType);
      if (deser != null) {
         return deser;
      } else {
         deser = this._provider.findTypedValueDeserializer(cfg, valueType, (BeanProperty)null);
         if (deser == null) {
            throw new JsonMappingException("Can not find a deserializer for type " + valueType);
         } else {
            this._rootDeserializers.put(valueType, deser);
            return deser;
         }
      }
   }

   protected DeserializationContext _createDeserializationContext(JsonParser jp, DeserializationConfig cfg) {
      return new StdDeserializationContext(cfg, jp, this._provider);
   }

   public JsonNode createArrayNode() {
      return this._config.getNodeFactory().arrayNode();
   }

   public JsonNode createObjectNode() {
      return this._config.getNodeFactory().objectNode();
   }

   public <T> T readValue(JsonParser jp, Class<T> valueType) throws IOException, JsonProcessingException {
      return this.withType(valueType).readValue(jp);
   }

   public <T> T readValue(JsonParser jp, TypeReference<?> valueTypeRef) throws IOException, JsonProcessingException {
      return this.withType(valueTypeRef).readValue(jp);
   }

   public <T> T readValue(JsonParser jp, JavaType valueType) throws IOException, JsonProcessingException {
      return this.withType(valueType).readValue(jp);
   }

   public JsonParser treeAsTokens(JsonNode n) {
      return new TreeTraversingParser(n, this);
   }

   public <T> T treeToValue(JsonNode n, Class<T> valueType) throws IOException, JsonProcessingException {
      return this.readValue(this.treeAsTokens(n), valueType);
   }

   public void writeTree(JsonGenerator jgen, JsonNode rootNode) throws IOException, JsonProcessingException {
      throw new UnsupportedOperationException("Not implemented for ObjectReader");
   }

   public void writeValue(JsonGenerator jgen, Object value) throws IOException, JsonProcessingException {
      throw new UnsupportedOperationException("Not implemented for ObjectReader");
   }
}
