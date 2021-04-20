package org.codehaus.jackson.map;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.DateFormat;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.codehaus.jackson.FormatSchema;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.PrettyPrinter;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.io.SegmentedStringWriter;
import org.codehaus.jackson.map.deser.BeanDeserializerModifier;
import org.codehaus.jackson.map.deser.StdDeserializationContext;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.codehaus.jackson.map.introspect.BasicClassIntrospector;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.SubtypeResolver;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.jsontype.impl.StdSubtypeResolver;
import org.codehaus.jackson.map.jsontype.impl.StdTypeResolverBuilder;
import org.codehaus.jackson.map.ser.BeanSerializerFactory;
import org.codehaus.jackson.map.ser.BeanSerializerModifier;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.StdSerializerProvider;
import org.codehaus.jackson.map.type.SimpleType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.type.TypeModifier;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TreeTraversingParser;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.codehaus.jackson.util.TokenBuffer;
import org.codehaus.jackson.util.VersionUtil;

public class ObjectMapper extends ObjectCodec implements Versioned {
   private static final JavaType JSON_NODE_TYPE = SimpleType.constructUnsafe(JsonNode.class);
   protected static final ClassIntrospector<? extends BeanDescription> DEFAULT_INTROSPECTOR;
   protected static final AnnotationIntrospector DEFAULT_ANNOTATION_INTROSPECTOR;
   protected static final VisibilityChecker<?> STD_VISIBILITY_CHECKER;
   protected final JsonFactory _jsonFactory;
   protected SubtypeResolver _subtypeResolver;
   protected TypeFactory _typeFactory;
   protected SerializationConfig _serializationConfig;
   protected SerializerProvider _serializerProvider;
   protected SerializerFactory _serializerFactory;
   protected DeserializationConfig _deserializationConfig;
   protected DeserializerProvider _deserializerProvider;
   protected final ConcurrentHashMap<JavaType, JsonDeserializer<Object>> _rootDeserializers;

   public ObjectMapper() {
      this((JsonFactory)null, (SerializerProvider)null, (DeserializerProvider)null);
   }

   public ObjectMapper(JsonFactory jf) {
      this(jf, (SerializerProvider)null, (DeserializerProvider)null);
   }

   /** @deprecated */
   @Deprecated
   public ObjectMapper(SerializerFactory sf) {
      this((JsonFactory)null, (SerializerProvider)null, (DeserializerProvider)null);
      this.setSerializerFactory(sf);
   }

   public ObjectMapper(JsonFactory jf, SerializerProvider sp, DeserializerProvider dp) {
      this(jf, sp, dp, (SerializationConfig)null, (DeserializationConfig)null);
   }

   public ObjectMapper(JsonFactory jf, SerializerProvider sp, DeserializerProvider dp, SerializationConfig sconfig, DeserializationConfig dconfig) {
      this._rootDeserializers = new ConcurrentHashMap(64, 0.6F, 2);
      this._jsonFactory = (JsonFactory)(jf == null ? new MappingJsonFactory(this) : jf);
      this._typeFactory = TypeFactory.defaultInstance();
      this._serializationConfig = sconfig != null ? sconfig : new SerializationConfig(DEFAULT_INTROSPECTOR, DEFAULT_ANNOTATION_INTROSPECTOR, STD_VISIBILITY_CHECKER, (SubtypeResolver)null, (PropertyNamingStrategy)null, this._typeFactory, (HandlerInstantiator)null);
      this._deserializationConfig = dconfig != null ? dconfig : new DeserializationConfig(DEFAULT_INTROSPECTOR, DEFAULT_ANNOTATION_INTROSPECTOR, STD_VISIBILITY_CHECKER, (SubtypeResolver)null, (PropertyNamingStrategy)null, this._typeFactory, (HandlerInstantiator)null);
      this._serializerProvider = (SerializerProvider)(sp == null ? new StdSerializerProvider() : sp);
      this._deserializerProvider = (DeserializerProvider)(dp == null ? new StdDeserializerProvider() : dp);
      this._serializerFactory = BeanSerializerFactory.instance;
   }

   public Version version() {
      return VersionUtil.versionFor(this.getClass());
   }

   public void registerModule(Module module) {
      String name = module.getModuleName();
      if (name == null) {
         throw new IllegalArgumentException("Module without defined name");
      } else {
         Version version = module.version();
         if (version == null) {
            throw new IllegalArgumentException("Module without defined version");
         } else {
            module.setupModule(new Module.SetupContext() {
               public Version getMapperVersion() {
                  return ObjectMapper.this.version();
               }

               public DeserializationConfig getDeserializationConfig() {
                  return ObjectMapper.this.getDeserializationConfig();
               }

               public SerializationConfig getSerializationConfig() {
                  return ObjectMapper.this.getSerializationConfig();
               }

               public void addDeserializers(Deserializers d) {
                  ObjectMapper.this._deserializerProvider = ObjectMapper.this._deserializerProvider.withAdditionalDeserializers(d);
               }

               public void addKeyDeserializers(KeyDeserializers d) {
                  ObjectMapper.this._deserializerProvider = ObjectMapper.this._deserializerProvider.withAdditionalKeyDeserializers(d);
               }

               public void addSerializers(Serializers s) {
                  ObjectMapper.this._serializerFactory = ObjectMapper.this._serializerFactory.withAdditionalSerializers(s);
               }

               public void addKeySerializers(Serializers s) {
                  ObjectMapper.this._serializerFactory = ObjectMapper.this._serializerFactory.withAdditionalKeySerializers(s);
               }

               public void addBeanSerializerModifier(BeanSerializerModifier modifier) {
                  ObjectMapper.this._serializerFactory = ObjectMapper.this._serializerFactory.withSerializerModifier(modifier);
               }

               public void addBeanDeserializerModifier(BeanDeserializerModifier modifier) {
                  ObjectMapper.this._deserializerProvider = ObjectMapper.this._deserializerProvider.withDeserializerModifier(modifier);
               }

               public void addAbstractTypeResolver(AbstractTypeResolver resolver) {
                  ObjectMapper.this._deserializerProvider = ObjectMapper.this._deserializerProvider.withAbstractTypeResolver(resolver);
               }

               public void addTypeModifier(TypeModifier modifier) {
                  TypeFactory f = ObjectMapper.this._typeFactory;
                  f = f.withModifier(modifier);
                  ObjectMapper.this.setTypeFactory(f);
               }

               public void insertAnnotationIntrospector(AnnotationIntrospector ai) {
                  ObjectMapper.this._deserializationConfig.insertAnnotationIntrospector(ai);
                  ObjectMapper.this._serializationConfig.insertAnnotationIntrospector(ai);
               }

               public void appendAnnotationIntrospector(AnnotationIntrospector ai) {
                  ObjectMapper.this._deserializationConfig.appendAnnotationIntrospector(ai);
                  ObjectMapper.this._serializationConfig.appendAnnotationIntrospector(ai);
               }

               public void setMixInAnnotations(Class<?> target, Class<?> mixinSource) {
                  ObjectMapper.this._deserializationConfig.addMixInAnnotations(target, mixinSource);
                  ObjectMapper.this._serializationConfig.addMixInAnnotations(target, mixinSource);
               }
            });
         }
      }
   }

   public ObjectMapper withModule(Module module) {
      this.registerModule(module);
      return this;
   }

   public SerializationConfig getSerializationConfig() {
      return this._serializationConfig;
   }

   public SerializationConfig copySerializationConfig() {
      return this._serializationConfig.createUnshared(this._subtypeResolver);
   }

   public ObjectMapper setSerializationConfig(SerializationConfig cfg) {
      this._serializationConfig = cfg;
      return this;
   }

   public DeserializationConfig getDeserializationConfig() {
      return this._deserializationConfig;
   }

   public DeserializationConfig copyDeserializationConfig() {
      return this._deserializationConfig.createUnshared(this._subtypeResolver);
   }

   public ObjectMapper setDeserializationConfig(DeserializationConfig cfg) {
      this._deserializationConfig = cfg;
      return this;
   }

   public ObjectMapper setSerializerFactory(SerializerFactory f) {
      this._serializerFactory = f;
      return this;
   }

   public ObjectMapper setSerializerProvider(SerializerProvider p) {
      this._serializerProvider = p;
      return this;
   }

   public SerializerProvider getSerializerProvider() {
      return this._serializerProvider;
   }

   public ObjectMapper setDeserializerProvider(DeserializerProvider p) {
      this._deserializerProvider = p;
      return this;
   }

   public DeserializerProvider getDeserializerProvider() {
      return this._deserializerProvider;
   }

   public VisibilityChecker<?> getVisibilityChecker() {
      return this._serializationConfig.getDefaultVisibilityChecker();
   }

   public void setVisibilityChecker(VisibilityChecker<?> vc) {
      this._deserializationConfig = this._deserializationConfig.withVisibilityChecker(vc);
      this._serializationConfig = this._serializationConfig.withVisibilityChecker(vc);
   }

   public SubtypeResolver getSubtypeResolver() {
      if (this._subtypeResolver == null) {
         this._subtypeResolver = new StdSubtypeResolver();
      }

      return this._subtypeResolver;
   }

   public void setSubtypeResolver(SubtypeResolver r) {
      this._subtypeResolver = r;
   }

   public ObjectMapper setAnnotationIntrospector(AnnotationIntrospector ai) {
      this._serializationConfig = this._serializationConfig.withAnnotationIntrospector(ai);
      this._deserializationConfig = this._deserializationConfig.withAnnotationIntrospector(ai);
      return this;
   }

   public ObjectMapper setPropertyNamingStrategy(PropertyNamingStrategy s) {
      this._serializationConfig = this._serializationConfig.withPropertyNamingStrategy(s);
      this._deserializationConfig = this._deserializationConfig.withPropertyNamingStrategy(s);
      return this;
   }

   public ObjectMapper enableDefaultTyping() {
      return this.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
   }

   public ObjectMapper enableDefaultTyping(ObjectMapper.DefaultTyping dti) {
      return this.enableDefaultTyping(dti, JsonTypeInfo.As.WRAPPER_ARRAY);
   }

   public ObjectMapper enableDefaultTyping(ObjectMapper.DefaultTyping applicability, JsonTypeInfo.As includeAs) {
      TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(applicability);
      TypeResolverBuilder<?> typer = typer.init(JsonTypeInfo.Id.CLASS, (TypeIdResolver)null);
      typer = typer.inclusion(includeAs);
      return this.setDefaultTyping(typer);
   }

   public ObjectMapper enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping applicability, String propertyName) {
      TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(applicability);
      TypeResolverBuilder<?> typer = typer.init(JsonTypeInfo.Id.CLASS, (TypeIdResolver)null);
      typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
      typer = typer.typeProperty(propertyName);
      return this.setDefaultTyping(typer);
   }

   public ObjectMapper disableDefaultTyping() {
      return this.setDefaultTyping((TypeResolverBuilder)null);
   }

   public ObjectMapper setDefaultTyping(TypeResolverBuilder<?> typer) {
      this._deserializationConfig = this._deserializationConfig.withTypeResolverBuilder(typer);
      this._serializationConfig = this._serializationConfig.withTypeResolverBuilder(typer);
      return this;
   }

   public void registerSubtypes(Class<?>... classes) {
      this.getSubtypeResolver().registerSubtypes(classes);
   }

   public void registerSubtypes(NamedType... types) {
      this.getSubtypeResolver().registerSubtypes(types);
   }

   public TypeFactory getTypeFactory() {
      return this._typeFactory;
   }

   public ObjectMapper setTypeFactory(TypeFactory f) {
      this._typeFactory = f;
      this._deserializationConfig = this._deserializationConfig.withTypeFactory(f);
      this._serializationConfig = this._serializationConfig.withTypeFactory(f);
      return this;
   }

   public JavaType constructType(Type t) {
      return this._typeFactory.constructType(t);
   }

   public ObjectMapper setNodeFactory(JsonNodeFactory f) {
      this._deserializationConfig = this._deserializationConfig.withNodeFactory(f);
      return this;
   }

   public void setFilters(FilterProvider filterProvider) {
      this._serializationConfig = this._serializationConfig.withFilters(filterProvider);
   }

   public JsonFactory getJsonFactory() {
      return this._jsonFactory;
   }

   public void setDateFormat(DateFormat dateFormat) {
      this._deserializationConfig = this._deserializationConfig.withDateFormat(dateFormat);
      this._serializationConfig = this._serializationConfig.withDateFormat(dateFormat);
   }

   public void setHandlerInstantiator(HandlerInstantiator hi) {
      this._deserializationConfig = this._deserializationConfig.withHandlerInstantiator(hi);
      this._serializationConfig = this._serializationConfig.withHandlerInstantiator(hi);
   }

   public ObjectMapper configure(SerializationConfig.Feature f, boolean state) {
      this._serializationConfig.set(f, state);
      return this;
   }

   public ObjectMapper configure(DeserializationConfig.Feature f, boolean state) {
      this._deserializationConfig.set(f, state);
      return this;
   }

   public ObjectMapper configure(JsonParser.Feature f, boolean state) {
      this._jsonFactory.configure(f, state);
      return this;
   }

   public ObjectMapper configure(JsonGenerator.Feature f, boolean state) {
      this._jsonFactory.configure(f, state);
      return this;
   }

   public JsonNodeFactory getNodeFactory() {
      return this._deserializationConfig.getNodeFactory();
   }

   public <T> T readValue(JsonParser jp, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readValue(this.copyDeserializationConfig(), jp, this._typeFactory.constructType((Type)valueType));
   }

   public <T> T readValue(JsonParser jp, Class<T> valueType, DeserializationConfig cfg) throws IOException, JsonParseException, JsonMappingException {
      return this._readValue(cfg, jp, this._typeFactory.constructType((Type)valueType));
   }

   public <T> T readValue(JsonParser jp, TypeReference<?> valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
      return this._readValue(this.copyDeserializationConfig(), jp, this._typeFactory.constructType(valueTypeRef));
   }

   public <T> T readValue(JsonParser jp, TypeReference<?> valueTypeRef, DeserializationConfig cfg) throws IOException, JsonParseException, JsonMappingException {
      return this._readValue(cfg, jp, this._typeFactory.constructType(valueTypeRef));
   }

   public <T> T readValue(JsonParser jp, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readValue(this.copyDeserializationConfig(), jp, valueType);
   }

   public <T> T readValue(JsonParser jp, JavaType valueType, DeserializationConfig cfg) throws IOException, JsonParseException, JsonMappingException {
      return this._readValue(cfg, jp, valueType);
   }

   public JsonNode readTree(JsonParser jp) throws IOException, JsonProcessingException {
      return this.readTree(jp, this.copyDeserializationConfig());
   }

   public JsonNode readTree(JsonParser jp, DeserializationConfig cfg) throws IOException, JsonProcessingException {
      JsonNode n = (JsonNode)this._readValue(cfg, jp, JSON_NODE_TYPE);
      return (JsonNode)(n == null ? NullNode.instance : n);
   }

   public JsonNode readTree(InputStream in) throws IOException, JsonProcessingException {
      JsonNode n = (JsonNode)this.readValue(in, JSON_NODE_TYPE);
      return (JsonNode)(n == null ? NullNode.instance : n);
   }

   public JsonNode readTree(Reader r) throws IOException, JsonProcessingException {
      JsonNode n = (JsonNode)this.readValue(r, JSON_NODE_TYPE);
      return (JsonNode)(n == null ? NullNode.instance : n);
   }

   public JsonNode readTree(String content) throws IOException, JsonProcessingException {
      JsonNode n = (JsonNode)this.readValue(content, JSON_NODE_TYPE);
      return (JsonNode)(n == null ? NullNode.instance : n);
   }

   public <T> MappingIterator<T> readValues(JsonParser jp, JavaType valueType) throws IOException, JsonProcessingException {
      DeserializationConfig config = this.copyDeserializationConfig();
      DeserializationContext ctxt = this._createDeserializationContext(jp, config);
      JsonDeserializer<?> deser = this._findRootDeserializer(config, valueType);
      return new MappingIterator(valueType, jp, ctxt, deser);
   }

   public <T> MappingIterator<T> readValues(JsonParser jp, Class<?> valueType) throws IOException, JsonProcessingException {
      return this.readValues(jp, this._typeFactory.constructType((Type)valueType));
   }

   public <T> MappingIterator<T> readValues(JsonParser jp, TypeReference<?> valueTypeRef) throws IOException, JsonProcessingException {
      return this.readValues(jp, this._typeFactory.constructType(valueTypeRef));
   }

   public void writeValue(JsonGenerator jgen, Object value) throws IOException, JsonGenerationException, JsonMappingException {
      SerializationConfig config = this.copySerializationConfig();
      if (config.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
         this._writeCloseableValue(jgen, value, config);
      } else {
         this._serializerProvider.serializeValue(config, jgen, value, this._serializerFactory);
         if (config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
         }
      }

   }

   public void writeValue(JsonGenerator jgen, Object value, SerializationConfig config) throws IOException, JsonGenerationException, JsonMappingException {
      if (config.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
         this._writeCloseableValue(jgen, value, config);
      } else {
         this._serializerProvider.serializeValue(config, jgen, value, this._serializerFactory);
         if (config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
         }
      }

   }

   public void writeTree(JsonGenerator jgen, JsonNode rootNode) throws IOException, JsonProcessingException {
      SerializationConfig config = this.copySerializationConfig();
      this._serializerProvider.serializeValue(config, jgen, rootNode, this._serializerFactory);
      if (config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
         jgen.flush();
      }

   }

   public void writeTree(JsonGenerator jgen, JsonNode rootNode, SerializationConfig cfg) throws IOException, JsonProcessingException {
      this._serializerProvider.serializeValue(cfg, jgen, rootNode, this._serializerFactory);
      if (cfg.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
         jgen.flush();
      }

   }

   public ObjectNode createObjectNode() {
      return this._deserializationConfig.getNodeFactory().objectNode();
   }

   public ArrayNode createArrayNode() {
      return this._deserializationConfig.getNodeFactory().arrayNode();
   }

   public JsonParser treeAsTokens(JsonNode n) {
      return new TreeTraversingParser(n, this);
   }

   public <T> T treeToValue(JsonNode n, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
      return this.readValue(this.treeAsTokens(n), valueType);
   }

   public <T extends JsonNode> T valueToTree(Object fromValue) throws IllegalArgumentException {
      if (fromValue == null) {
         return null;
      } else {
         TokenBuffer buf = new TokenBuffer(this);

         try {
            this.writeValue((JsonGenerator)buf, fromValue);
            JsonParser jp = buf.asParser();
            JsonNode result = this.readTree(jp);
            jp.close();
            return result;
         } catch (IOException var5) {
            throw new IllegalArgumentException(var5.getMessage(), var5);
         }
      }
   }

   public boolean canSerialize(Class<?> type) {
      return this._serializerProvider.hasSerializerFor(this.copySerializationConfig(), type, this._serializerFactory);
   }

   public boolean canDeserialize(JavaType type) {
      return this._deserializerProvider.hasValueDeserializerFor(this.copyDeserializationConfig(), type);
   }

   public <T> T readValue(File src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType((Type)valueType));
   }

   public <T> T readValue(File src, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueTypeRef));
   }

   public <T> T readValue(File src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), valueType);
   }

   public <T> T readValue(URL src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType((Type)valueType));
   }

   public <T> T readValue(URL src, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueTypeRef));
   }

   public <T> T readValue(URL src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), valueType);
   }

   public <T> T readValue(String content, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(content), this._typeFactory.constructType((Type)valueType));
   }

   public <T> T readValue(String content, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(content), this._typeFactory.constructType(valueTypeRef));
   }

   public <T> T readValue(String content, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(content), valueType);
   }

   public <T> T readValue(Reader src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType((Type)valueType));
   }

   public <T> T readValue(Reader src, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueTypeRef));
   }

   public <T> T readValue(Reader src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), valueType);
   }

   public <T> T readValue(InputStream src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType((Type)valueType));
   }

   public <T> T readValue(InputStream src, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueTypeRef));
   }

   public <T> T readValue(InputStream src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), valueType);
   }

   public <T> T readValue(byte[] src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType((Type)valueType));
   }

   public <T> T readValue(byte[] src, int offset, int len, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src, offset, len), this._typeFactory.constructType((Type)valueType));
   }

   public <T> T readValue(byte[] src, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), this._typeFactory.constructType(valueTypeRef));
   }

   public <T> T readValue(byte[] src, int offset, int len, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src, offset, len), this._typeFactory.constructType(valueTypeRef));
   }

   public <T> T readValue(byte[] src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src), valueType);
   }

   public <T> T readValue(byte[] src, int offset, int len, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readMapAndClose(this._jsonFactory.createJsonParser(src, offset, len), valueType);
   }

   public <T> T readValue(JsonNode root, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readValue(this.copyDeserializationConfig(), this.treeAsTokens(root), this._typeFactory.constructType((Type)valueType));
   }

   public <T> T readValue(JsonNode root, TypeReference valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
      return this._readValue(this.copyDeserializationConfig(), this.treeAsTokens(root), this._typeFactory.constructType(valueTypeRef));
   }

   public <T> T readValue(JsonNode root, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      return this._readValue(this.copyDeserializationConfig(), this.treeAsTokens(root), valueType);
   }

   public void writeValue(File resultFile, Object value) throws IOException, JsonGenerationException, JsonMappingException {
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator(resultFile, JsonEncoding.UTF8), value);
   }

   public void writeValue(OutputStream out, Object value) throws IOException, JsonGenerationException, JsonMappingException {
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8), value);
   }

   public void writeValue(Writer w, Object value) throws IOException, JsonGenerationException, JsonMappingException {
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator(w), value);
   }

   public String writeValueAsString(Object value) throws IOException, JsonGenerationException, JsonMappingException {
      SegmentedStringWriter sw = new SegmentedStringWriter(this._jsonFactory._getBufferRecycler());
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator((Writer)sw), value);
      return sw.getAndClear();
   }

   public byte[] writeValueAsBytes(Object value) throws IOException, JsonGenerationException, JsonMappingException {
      ByteArrayBuilder bb = new ByteArrayBuilder(this._jsonFactory._getBufferRecycler());
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator((OutputStream)bb, JsonEncoding.UTF8), value);
      byte[] result = bb.toByteArray();
      bb.release();
      return result;
   }

   /** @deprecated */
   @Deprecated
   public void writeValueUsingView(JsonGenerator jgen, Object value, Class<?> viewClass) throws IOException, JsonGenerationException, JsonMappingException {
      this._configAndWriteValue(jgen, value, viewClass);
   }

   /** @deprecated */
   @Deprecated
   public void writeValueUsingView(Writer w, Object value, Class<?> viewClass) throws IOException, JsonGenerationException, JsonMappingException {
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator(w), value, viewClass);
   }

   /** @deprecated */
   @Deprecated
   public void writeValueUsingView(OutputStream out, Object value, Class<?> viewClass) throws IOException, JsonGenerationException, JsonMappingException {
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8), value, viewClass);
   }

   public ObjectWriter writer() {
      return new ObjectWriter(this, this.copySerializationConfig());
   }

   public ObjectWriter viewWriter(Class<?> serializationView) {
      return new ObjectWriter(this, this.copySerializationConfig().withView(serializationView));
   }

   public ObjectWriter typedWriter(Class<?> rootType) {
      JavaType t = rootType == null ? null : this._typeFactory.constructType((Type)rootType);
      return new ObjectWriter(this, this.copySerializationConfig(), t, (PrettyPrinter)null);
   }

   public ObjectWriter typedWriter(JavaType rootType) {
      return new ObjectWriter(this, this.copySerializationConfig(), rootType, (PrettyPrinter)null);
   }

   public ObjectWriter typedWriter(TypeReference<?> rootType) {
      JavaType t = rootType == null ? null : this._typeFactory.constructType(rootType);
      return new ObjectWriter(this, this.copySerializationConfig(), t, (PrettyPrinter)null);
   }

   public ObjectWriter prettyPrintingWriter(PrettyPrinter pp) {
      if (pp == null) {
         pp = ObjectWriter.NULL_PRETTY_PRINTER;
      }

      return new ObjectWriter(this, this.copySerializationConfig(), (JavaType)null, pp);
   }

   public ObjectWriter defaultPrettyPrintingWriter() {
      return new ObjectWriter(this, this.copySerializationConfig(), (JavaType)null, this._defaultPrettyPrinter());
   }

   public ObjectWriter filteredWriter(FilterProvider filterProvider) {
      return new ObjectWriter(this, this.copySerializationConfig().withFilters(filterProvider));
   }

   public ObjectWriter schemaBasedWriter(FormatSchema schema) {
      return new ObjectWriter(this, this.copySerializationConfig(), schema);
   }

   public ObjectReader reader() {
      return new ObjectReader(this, this.copyDeserializationConfig());
   }

   public ObjectReader updatingReader(Object valueToUpdate) {
      JavaType t = this._typeFactory.constructType((Type)valueToUpdate.getClass());
      return new ObjectReader(this, this.copyDeserializationConfig(), t, valueToUpdate, (FormatSchema)null);
   }

   public ObjectReader reader(JavaType type) {
      return new ObjectReader(this, this.copyDeserializationConfig(), type, (Object)null, (FormatSchema)null);
   }

   public ObjectReader reader(Class<?> type) {
      return this.reader(this._typeFactory.constructType((Type)type));
   }

   public ObjectReader reader(TypeReference<?> type) {
      return this.reader(this._typeFactory.constructType(type));
   }

   public ObjectReader reader(JsonNodeFactory f) {
      return (new ObjectReader(this, this.copyDeserializationConfig())).withNodeFactory(f);
   }

   public ObjectReader schemaBasedReader(FormatSchema schema) {
      return new ObjectReader(this, this.copyDeserializationConfig(), (JavaType)null, (Object)null, schema);
   }

   public <T> T convertValue(Object fromValue, Class<T> toValueType) throws IllegalArgumentException {
      return this._convert(fromValue, this._typeFactory.constructType((Type)toValueType));
   }

   public <T> T convertValue(Object fromValue, TypeReference toValueTypeRef) throws IllegalArgumentException {
      return this._convert(fromValue, this._typeFactory.constructType(toValueTypeRef));
   }

   public <T> T convertValue(Object fromValue, JavaType toValueType) throws IllegalArgumentException {
      return this._convert(fromValue, toValueType);
   }

   protected Object _convert(Object fromValue, JavaType toValueType) throws IllegalArgumentException {
      if (fromValue == null) {
         return null;
      } else {
         TokenBuffer buf = new TokenBuffer(this);

         try {
            this.writeValue((JsonGenerator)buf, fromValue);
            JsonParser jp = buf.asParser();
            Object result = this.readValue(jp, toValueType);
            jp.close();
            return result;
         } catch (IOException var6) {
            throw new IllegalArgumentException(var6.getMessage(), var6);
         }
      }
   }

   public JsonSchema generateJsonSchema(Class<?> t) throws JsonMappingException {
      return this.generateJsonSchema(t, this.copySerializationConfig());
   }

   public JsonSchema generateJsonSchema(Class<?> t, SerializationConfig cfg) throws JsonMappingException {
      return this._serializerProvider.generateJsonSchema(t, cfg, this._serializerFactory);
   }

   protected PrettyPrinter _defaultPrettyPrinter() {
      return new DefaultPrettyPrinter();
   }

   protected final void _configAndWriteValue(JsonGenerator jgen, Object value) throws IOException, JsonGenerationException, JsonMappingException {
      SerializationConfig cfg = this.copySerializationConfig();
      if (cfg.isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
         jgen.useDefaultPrettyPrinter();
      }

      if (cfg.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
         this._configAndWriteCloseable(jgen, value, cfg);
      } else {
         boolean closed = false;

         try {
            this._serializerProvider.serializeValue(cfg, jgen, value, this._serializerFactory);
            closed = true;
            jgen.close();
         } finally {
            if (!closed) {
               try {
                  jgen.close();
               } catch (IOException var11) {
               }
            }

         }

      }
   }

   protected final void _configAndWriteValue(JsonGenerator jgen, Object value, Class<?> viewClass) throws IOException, JsonGenerationException, JsonMappingException {
      SerializationConfig cfg = this.copySerializationConfig().withView(viewClass);
      if (cfg.isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
         jgen.useDefaultPrettyPrinter();
      }

      if (cfg.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
         this._configAndWriteCloseable(jgen, value, cfg);
      } else {
         boolean closed = false;

         try {
            this._serializerProvider.serializeValue(cfg, jgen, value, this._serializerFactory);
            closed = true;
            jgen.close();
         } finally {
            if (!closed) {
               try {
                  jgen.close();
               } catch (IOException var12) {
               }
            }

         }

      }
   }

   private final void _configAndWriteCloseable(JsonGenerator jgen, Object value, SerializationConfig cfg) throws IOException, JsonGenerationException, JsonMappingException {
      Closeable toClose = (Closeable)value;

      try {
         this._serializerProvider.serializeValue(cfg, jgen, value, this._serializerFactory);
         JsonGenerator tmpJgen = jgen;
         jgen = null;
         tmpJgen.close();
         Closeable tmpToClose = toClose;
         toClose = null;
         tmpToClose.close();
      } finally {
         if (jgen != null) {
            try {
               jgen.close();
            } catch (IOException var15) {
            }
         }

         if (toClose != null) {
            try {
               toClose.close();
            } catch (IOException var14) {
            }
         }

      }

   }

   private final void _writeCloseableValue(JsonGenerator jgen, Object value, SerializationConfig cfg) throws IOException, JsonGenerationException, JsonMappingException {
      Closeable toClose = (Closeable)value;

      try {
         this._serializerProvider.serializeValue(cfg, jgen, value, this._serializerFactory);
         if (cfg.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
         }

         Closeable tmpToClose = toClose;
         toClose = null;
         tmpToClose.close();
      } finally {
         if (toClose != null) {
            try {
               toClose.close();
            } catch (IOException var11) {
            }
         }

      }

   }

   protected Object _readValue(DeserializationConfig cfg, JsonParser jp, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      JsonToken t = this._initForReading(jp);
      Object result;
      if (t != JsonToken.VALUE_NULL && t != JsonToken.END_ARRAY && t != JsonToken.END_OBJECT) {
         DeserializationContext ctxt = this._createDeserializationContext(jp, cfg);
         result = this._findRootDeserializer(cfg, valueType).deserialize(jp, ctxt);
      } else {
         result = null;
      }

      jp.clearCurrentToken();
      return result;
   }

   protected Object _readMapAndClose(JsonParser jp, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
      Object var14;
      try {
         JsonToken t = this._initForReading(jp);
         Object result;
         if (t != JsonToken.VALUE_NULL && t != JsonToken.END_ARRAY && t != JsonToken.END_OBJECT) {
            DeserializationConfig cfg = this.copyDeserializationConfig();
            DeserializationContext ctxt = this._createDeserializationContext(jp, cfg);
            result = this._findRootDeserializer(cfg, valueType).deserialize(jp, ctxt);
         } else {
            result = null;
         }

         jp.clearCurrentToken();
         var14 = result;
      } finally {
         try {
            jp.close();
         } catch (IOException var12) {
         }

      }

      return var14;
   }

   protected JsonToken _initForReading(JsonParser jp) throws IOException, JsonParseException, JsonMappingException {
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
         deser = this._deserializerProvider.findTypedValueDeserializer(cfg, valueType, (BeanProperty)null);
         if (deser == null) {
            throw new JsonMappingException("Can not find a deserializer for type " + valueType);
         } else {
            this._rootDeserializers.put(valueType, deser);
            return deser;
         }
      }
   }

   protected DeserializationContext _createDeserializationContext(JsonParser jp, DeserializationConfig cfg) {
      return new StdDeserializationContext(cfg, jp, this._deserializerProvider);
   }

   static {
      DEFAULT_INTROSPECTOR = BasicClassIntrospector.instance;
      DEFAULT_ANNOTATION_INTROSPECTOR = new JacksonAnnotationIntrospector();
      STD_VISIBILITY_CHECKER = VisibilityChecker.Std.defaultInstance();
   }

   public static class DefaultTypeResolverBuilder extends StdTypeResolverBuilder {
      protected final ObjectMapper.DefaultTyping _appliesFor;

      public DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping t) {
         this._appliesFor = t;
      }

      public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes, BeanProperty property) {
         return this.useForType(baseType) ? super.buildTypeDeserializer(config, baseType, subtypes, property) : null;
      }

      public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes, BeanProperty property) {
         return this.useForType(baseType) ? super.buildTypeSerializer(config, baseType, subtypes, property) : null;
      }

      public boolean useForType(JavaType t) {
         switch(this._appliesFor) {
         case NON_CONCRETE_AND_ARRAYS:
            if (t.isArrayType()) {
               t = t.getContentType();
            }
         case OBJECT_AND_NON_CONCRETE:
            break;
         case NON_FINAL:
            if (t.isArrayType()) {
               t = t.getContentType();
            }

            return !t.isFinal();
         default:
            return t.getRawClass() == Object.class;
         }

         return t.getRawClass() == Object.class || !t.isConcrete();
      }
   }

   public static enum DefaultTyping {
      JAVA_LANG_OBJECT,
      OBJECT_AND_NON_CONCRETE,
      NON_CONCRETE_AND_ARRAYS,
      NON_FINAL;

      private DefaultTyping() {
      }
   }
}
