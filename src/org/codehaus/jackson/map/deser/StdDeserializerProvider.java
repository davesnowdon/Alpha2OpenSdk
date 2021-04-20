package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.AbstractTypeResolver;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.ClassIntrospector;
import org.codehaus.jackson.map.ContextualDeserializer;
import org.codehaus.jackson.map.ContextualKeyDeserializer;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerFactory;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.Deserializers;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.KeyDeserializers;
import org.codehaus.jackson.map.ResolvableDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

public class StdDeserializerProvider extends DeserializerProvider {
   static final HashMap<JavaType, KeyDeserializer> _keyDeserializers = StdKeyDeserializers.constructAll();
   protected final ConcurrentHashMap<JavaType, JsonDeserializer<Object>> _cachedDeserializers;
   protected final HashMap<JavaType, JsonDeserializer<Object>> _incompleteDeserializers;
   protected DeserializerFactory _factory;

   public StdDeserializerProvider() {
      this(BeanDeserializerFactory.instance);
   }

   public StdDeserializerProvider(DeserializerFactory f) {
      this._cachedDeserializers = new ConcurrentHashMap(64, 0.75F, 2);
      this._incompleteDeserializers = new HashMap(8);
      this._factory = f;
   }

   public DeserializerProvider withAdditionalDeserializers(Deserializers d) {
      this._factory = this._factory.withAdditionalDeserializers(d);
      return this;
   }

   public DeserializerProvider withAdditionalKeyDeserializers(KeyDeserializers d) {
      this._factory = this._factory.withAdditionalKeyDeserializers(d);
      return this;
   }

   public DeserializerProvider withDeserializerModifier(BeanDeserializerModifier modifier) {
      this._factory = this._factory.withDeserializerModifier(modifier);
      return this;
   }

   public DeserializerProvider withAbstractTypeResolver(AbstractTypeResolver resolver) {
      this._factory = this._factory.withAbstractTypeResolver(resolver);
      return this;
   }

   public JsonDeserializer<Object> findValueDeserializer(DeserializationConfig config, JavaType propertyType, BeanProperty property) throws JsonMappingException {
      JsonDeserializer<Object> deser = this._findCachedDeserializer(propertyType);
      JsonDeserializer d;
      if (deser != null) {
         if (deser instanceof ContextualDeserializer) {
            d = ((ContextualDeserializer)deser).createContextual(config, property);
            deser = d;
         }

         return deser;
      } else {
         deser = this._createAndCacheValueDeserializer(config, propertyType, property);
         if (deser == null) {
            deser = this._handleUnknownValueDeserializer(propertyType);
         }

         if (deser instanceof ContextualDeserializer) {
            d = ((ContextualDeserializer)deser).createContextual(config, property);
            deser = d;
         }

         return deser;
      }
   }

   public JsonDeserializer<Object> findTypedValueDeserializer(DeserializationConfig config, JavaType type, BeanProperty property) throws JsonMappingException {
      JsonDeserializer<Object> deser = this.findValueDeserializer(config, type, property);
      TypeDeserializer typeDeser = this._factory.findTypeDeserializer(config, type, property);
      return (JsonDeserializer)(typeDeser != null ? new StdDeserializerProvider.WrappedDeserializer(typeDeser, deser) : deser);
   }

   public KeyDeserializer findKeyDeserializer(DeserializationConfig config, JavaType type, BeanProperty property) throws JsonMappingException {
      KeyDeserializer kd = this._factory.createKeyDeserializer(config, type, property);
      if (kd == null) {
         Class<?> raw = type.getRawClass();
         if (raw == String.class || raw == Object.class) {
            return null;
         }

         KeyDeserializer kdes = (KeyDeserializer)_keyDeserializers.get(type);
         if (kdes != null) {
            return kdes;
         }

         if (type.isEnumType()) {
            return StdKeyDeserializers.constructEnumKeyDeserializer(config, type);
         }

         kdes = StdKeyDeserializers.findStringBasedKeyDeserializer(config, type);
         if (kdes != null) {
            return kdes;
         }

         if (kd == null) {
            return this._handleUnknownKeyDeserializer(type);
         }
      }

      if (kd instanceof ContextualKeyDeserializer) {
         kd = ((ContextualKeyDeserializer)kd).createContextual(config, property);
      }

      return kd;
   }

   public boolean hasValueDeserializerFor(DeserializationConfig config, JavaType type) {
      JsonDeserializer<Object> deser = this._findCachedDeserializer(type);
      if (deser == null) {
         try {
            deser = this._createAndCacheValueDeserializer(config, type, (BeanProperty)null);
         } catch (Exception var5) {
            return false;
         }
      }

      return deser != null;
   }

   public int cachedDeserializersCount() {
      return this._cachedDeserializers.size();
   }

   public void flushCachedDeserializers() {
      this._cachedDeserializers.clear();
   }

   protected JsonDeserializer<Object> _findCachedDeserializer(JavaType type) {
      return (JsonDeserializer)this._cachedDeserializers.get(type);
   }

   protected JsonDeserializer<Object> _createAndCacheValueDeserializer(DeserializationConfig config, JavaType type, BeanProperty property) throws JsonMappingException {
      synchronized(this._incompleteDeserializers) {
         JsonDeserializer<Object> deser = this._findCachedDeserializer(type);
         if (deser != null) {
            return deser;
         } else {
            int count = this._incompleteDeserializers.size();
            if (count > 0) {
               deser = (JsonDeserializer)this._incompleteDeserializers.get(type);
               if (deser != null) {
                  return deser;
               }
            }

            JsonDeserializer var7;
            try {
               var7 = this._createAndCache2(config, type, property);
            } finally {
               if (count == 0 && this._incompleteDeserializers.size() > 0) {
                  this._incompleteDeserializers.clear();
               }

            }

            return var7;
         }
      }
   }

   protected JsonDeserializer<Object> _createAndCache2(DeserializationConfig config, JavaType type, BeanProperty property) throws JsonMappingException {
      JsonDeserializer deser;
      try {
         deser = this._createDeserializer(config, type, property);
      } catch (IllegalArgumentException var10) {
         throw new JsonMappingException(var10.getMessage(), (JsonLocation)null, var10);
      }

      if (deser == null) {
         return null;
      } else {
         boolean isResolvable = deser instanceof ResolvableDeserializer;
         boolean addToCache = deser.getClass() == BeanDeserializer.class;
         if (!addToCache && config.isEnabled(DeserializationConfig.Feature.USE_ANNOTATIONS)) {
            AnnotationIntrospector aintr = config.getAnnotationIntrospector();
            AnnotatedClass ac = AnnotatedClass.construct(deser.getClass(), aintr, (ClassIntrospector.MixInResolver)null);
            Boolean cacheAnn = aintr.findCachability(ac);
            if (cacheAnn != null) {
               addToCache = cacheAnn;
            }
         }

         if (isResolvable) {
            this._incompleteDeserializers.put(type, deser);
            this._resolveDeserializer(config, (ResolvableDeserializer)deser);
            this._incompleteDeserializers.remove(type);
         }

         if (addToCache) {
            this._cachedDeserializers.put(type, deser);
         }

         return deser;
      }
   }

   protected JsonDeserializer<Object> _createDeserializer(DeserializationConfig config, JavaType type, BeanProperty property) throws JsonMappingException {
      if (type.isEnumType()) {
         return this._factory.createEnumDeserializer(config, this, type, property);
      } else {
         if (type.isContainerType()) {
            if (type.isArrayType()) {
               return this._factory.createArrayDeserializer(config, this, (ArrayType)type, property);
            }

            if (type.isMapLikeType()) {
               MapLikeType mlt = (MapLikeType)type;
               if (mlt.isTrueMapType()) {
                  return this._factory.createMapDeserializer(config, this, (MapType)mlt, property);
               }

               return this._factory.createMapLikeDeserializer(config, this, mlt, property);
            }

            if (type.isCollectionLikeType()) {
               CollectionLikeType clt = (CollectionLikeType)type;
               if (clt.isTrueCollectionType()) {
                  return this._factory.createCollectionDeserializer(config, this, (CollectionType)clt, property);
               }

               return this._factory.createCollectionLikeDeserializer(config, this, clt, property);
            }
         }

         return JsonNode.class.isAssignableFrom(type.getRawClass()) ? this._factory.createTreeDeserializer(config, this, type, property) : this._factory.createBeanDeserializer(config, this, type, property);
      }
   }

   protected void _resolveDeserializer(DeserializationConfig config, ResolvableDeserializer ser) throws JsonMappingException {
      ser.resolve(config, this);
   }

   protected JsonDeserializer<Object> _handleUnknownValueDeserializer(JavaType type) throws JsonMappingException {
      Class<?> rawClass = type.getRawClass();
      if (!ClassUtil.isConcrete(rawClass)) {
         throw new JsonMappingException("Can not find a Value deserializer for abstract type " + type);
      } else {
         throw new JsonMappingException("Can not find a Value deserializer for type " + type);
      }
   }

   protected KeyDeserializer _handleUnknownKeyDeserializer(JavaType type) throws JsonMappingException {
      throw new JsonMappingException("Can not find a (Map) Key deserializer for type " + type);
   }

   protected static final class WrappedDeserializer extends JsonDeserializer<Object> {
      final TypeDeserializer _typeDeserializer;
      final JsonDeserializer<Object> _deserializer;

      public WrappedDeserializer(TypeDeserializer typeDeser, JsonDeserializer<Object> deser) {
         this._typeDeserializer = typeDeser;
         this._deserializer = deser;
      }

      public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         return this._deserializer.deserializeWithType(jp, ctxt, this._typeDeserializer);
      }

      public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
         throw new IllegalStateException("Type-wrapped deserializer's deserializeWithType should never get called");
      }
   }
}
