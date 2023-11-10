package org.codehaus.jackson.map.deser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.ContextualDeserializer;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializerFactory;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.deser.impl.StringCollectionDeserializer;
import org.codehaus.jackson.map.ext.OptionalHandlerFactory;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

public abstract class BasicDeserializerFactory extends DeserializerFactory {
   static final HashMap<JavaType, JsonDeserializer<Object>> _simpleDeserializers = StdDeserializers.constructAll();
   static final HashMap<String, Class<? extends Map>> _mapFallbacks = new HashMap();
   static final HashMap<String, Class<? extends Collection>> _collectionFallbacks;
   protected static final HashMap<JavaType, JsonDeserializer<Object>> _arrayDeserializers;
   protected OptionalHandlerFactory optionalHandlers;

   protected BasicDeserializerFactory() {
      this.optionalHandlers = OptionalHandlerFactory.instance;
   }

   public abstract DeserializerFactory withConfig(DeserializerFactory.Config var1);

   protected abstract JsonDeserializer<?> _findCustomArrayDeserializer(ArrayType var1, DeserializationConfig var2, DeserializerProvider var3, BeanProperty var4, TypeDeserializer var5, JsonDeserializer<?> var6) throws JsonMappingException;

   protected abstract JsonDeserializer<?> _findCustomCollectionDeserializer(CollectionType var1, DeserializationConfig var2, DeserializerProvider var3, BasicBeanDescription var4, BeanProperty var5, TypeDeserializer var6, JsonDeserializer<?> var7) throws JsonMappingException;

   protected abstract JsonDeserializer<?> _findCustomCollectionLikeDeserializer(CollectionLikeType var1, DeserializationConfig var2, DeserializerProvider var3, BasicBeanDescription var4, BeanProperty var5, TypeDeserializer var6, JsonDeserializer<?> var7) throws JsonMappingException;

   protected abstract JsonDeserializer<?> _findCustomEnumDeserializer(Class<?> var1, DeserializationConfig var2, BasicBeanDescription var3, BeanProperty var4) throws JsonMappingException;

   protected abstract JsonDeserializer<?> _findCustomMapDeserializer(MapType var1, DeserializationConfig var2, DeserializerProvider var3, BasicBeanDescription var4, BeanProperty var5, KeyDeserializer var6, TypeDeserializer var7, JsonDeserializer<?> var8) throws JsonMappingException;

   protected abstract JsonDeserializer<?> _findCustomMapLikeDeserializer(MapLikeType var1, DeserializationConfig var2, DeserializerProvider var3, BasicBeanDescription var4, BeanProperty var5, KeyDeserializer var6, TypeDeserializer var7, JsonDeserializer<?> var8) throws JsonMappingException;

   protected abstract JsonDeserializer<?> _findCustomTreeNodeDeserializer(Class<? extends JsonNode> var1, DeserializationConfig var2, BeanProperty var3) throws JsonMappingException;

   public JsonDeserializer<?> createArrayDeserializer(DeserializationConfig config, DeserializerProvider p, ArrayType type, BeanProperty property) throws JsonMappingException {
      JavaType elemType = type.getContentType();
      JsonDeserializer<Object> contentDeser = (JsonDeserializer)elemType.getValueHandler();
      JsonDeserializer custom;
      if (contentDeser == null) {
         JsonDeserializer<?> deser = (JsonDeserializer)_arrayDeserializers.get(elemType);
         if (deser != null) {
            custom = this._findCustomArrayDeserializer(type, config, p, property, (TypeDeserializer)null, (JsonDeserializer)null);
            if (custom != null) {
               return custom;
            }

            return deser;
         }

         if (elemType.isPrimitive()) {
            throw new IllegalArgumentException("Internal error: primitive type (" + type + ") passed, no array deserializer found");
         }
      }

      TypeDeserializer elemTypeDeser = (TypeDeserializer)elemType.getTypeHandler();
      if (elemTypeDeser == null) {
         elemTypeDeser = this.findTypeDeserializer(config, elemType, property);
      }

      custom = this._findCustomArrayDeserializer(type, config, p, property, elemTypeDeser, contentDeser);
      if (custom != null) {
         return custom;
      } else {
         if (contentDeser == null) {
            contentDeser = p.findValueDeserializer(config, elemType, property);
         }

         return new ArrayDeserializer(type, contentDeser, elemTypeDeser);
      }
   }

   public JsonDeserializer<?> createCollectionDeserializer(DeserializationConfig config, DeserializerProvider p, CollectionType type, BeanProperty property) throws JsonMappingException {
      type = (CollectionType)this.mapAbstractType(config, type);
      Class<?> collectionClass = type.getRawClass();
      BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspectClassAnnotations(collectionClass);
      JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
      if (deser != null) {
         return deser;
      } else {
         type = (CollectionType)this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, (String)null);
         JavaType contentType = type.getContentType();
         JsonDeserializer<Object> contentDeser = (JsonDeserializer)contentType.getValueHandler();
         TypeDeserializer contentTypeDeser = (TypeDeserializer)contentType.getTypeHandler();
         if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType, property);
         }

         JsonDeserializer<?> custom = this._findCustomCollectionDeserializer(type, config, p, beanDesc, property, contentTypeDeser, contentDeser);
         if (custom != null) {
            return custom;
         } else {
            if (contentDeser == null) {
               if (EnumSet.class.isAssignableFrom(collectionClass)) {
                  return new EnumSetDeserializer(this.constructEnumResolver(contentType.getRawClass(), config));
               }

               contentDeser = p.findValueDeserializer(config, contentType, property);
            }

            if (type.isInterface() || type.isAbstract()) {
               Class<? extends Collection> fallback = (Class)_collectionFallbacks.get(collectionClass.getName());
               if (fallback == null) {
                  throw new IllegalArgumentException("Can not find a deserializer for non-concrete Collection type " + type);
               }

               collectionClass = fallback;
            }

            boolean fixAccess = config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
            Constructor<Collection<Object>> ctor = ClassUtil.findConstructor(collectionClass, fixAccess);
            return (JsonDeserializer)(contentType.getRawClass() == String.class ? new StringCollectionDeserializer(type, contentDeser, ctor) : new CollectionDeserializer(type, contentDeser, contentTypeDeser, ctor));
         }
      }
   }

   public JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationConfig config, DeserializerProvider p, CollectionLikeType type, BeanProperty property) throws JsonMappingException {
      type = (CollectionLikeType)this.mapAbstractType(config, type);
      Class<?> collectionClass = type.getRawClass();
      BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspectClassAnnotations(collectionClass);
      JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
      if (deser != null) {
         return deser;
      } else {
         type = (CollectionLikeType)this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, (String)null);
         JavaType contentType = type.getContentType();
         JsonDeserializer<Object> contentDeser = (JsonDeserializer)contentType.getValueHandler();
         TypeDeserializer contentTypeDeser = (TypeDeserializer)contentType.getTypeHandler();
         if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType, property);
         }

         return this._findCustomCollectionLikeDeserializer(type, config, p, beanDesc, property, contentTypeDeser, contentDeser);
      }
   }

   public JsonDeserializer<?> createMapDeserializer(DeserializationConfig config, DeserializerProvider p, MapType type, BeanProperty property) throws JsonMappingException {
      type = (MapType)this.mapAbstractType(config, type);
      BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspectForCreation(type);
      JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
      if (deser != null) {
         return deser;
      } else {
         type = (MapType)this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, (String)null);
         JavaType keyType = type.getKeyType();
         JavaType contentType = type.getContentType();
         JsonDeserializer<Object> contentDeser = (JsonDeserializer)contentType.getValueHandler();
         KeyDeserializer keyDes = (KeyDeserializer)keyType.getValueHandler();
         if (keyDes == null) {
            keyDes = p.findKeyDeserializer(config, keyType, property);
         }

         TypeDeserializer contentTypeDeser = (TypeDeserializer)contentType.getTypeHandler();
         if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType, property);
         }

         JsonDeserializer<?> custom = this._findCustomMapDeserializer(type, config, p, beanDesc, property, keyDes, contentTypeDeser, contentDeser);
         if (custom != null) {
            return custom;
         } else {
            if (contentDeser == null) {
               contentDeser = p.findValueDeserializer(config, contentType, property);
            }

            Class<?> mapClass = type.getRawClass();
            Class fallback;
            if (EnumMap.class.isAssignableFrom(mapClass)) {
               fallback = keyType.getRawClass();
               if (fallback != null && fallback.isEnum()) {
                  return new EnumMapDeserializer(this.constructEnumResolver(fallback, config), contentDeser);
               } else {
                  throw new IllegalArgumentException("Can not construct EnumMap; generic (key) type not available");
               }
            } else {
               if (type.isInterface() || type.isAbstract()) {
                  fallback = (Class)_mapFallbacks.get(mapClass.getName());
                  if (fallback == null) {
                     throw new IllegalArgumentException("Can not find a deserializer for non-concrete Map type " + type);
                  }

                  type = (MapType)type.forcedNarrowBy(fallback);
                  beanDesc = (BasicBeanDescription)config.introspectForCreation(type);
               }

               boolean fixAccess = config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
               Constructor<Map<Object, Object>> defaultCtor = beanDesc.findDefaultConstructor();
               if (defaultCtor != null && fixAccess) {
                  ClassUtil.checkAndFixAccess(defaultCtor);
               }

               MapDeserializer md = new MapDeserializer(type, defaultCtor, keyDes, contentDeser, contentTypeDeser);
               md.setIgnorableProperties(config.getAnnotationIntrospector().findPropertiesToIgnore(beanDesc.getClassInfo()));
               md.setCreators(this.findMapCreators(config, beanDesc));
               return md;
            }
         }
      }
   }

   public JsonDeserializer<?> createMapLikeDeserializer(DeserializationConfig config, DeserializerProvider p, MapLikeType type, BeanProperty property) throws JsonMappingException {
      type = (MapLikeType)this.mapAbstractType(config, type);
      BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspectForCreation(type);
      JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
      if (deser != null) {
         return deser;
      } else {
         type = (MapLikeType)this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, (String)null);
         JavaType keyType = type.getKeyType();
         JavaType contentType = type.getContentType();
         JsonDeserializer<Object> contentDeser = (JsonDeserializer)contentType.getValueHandler();
         KeyDeserializer keyDes = (KeyDeserializer)keyType.getValueHandler();
         if (keyDes == null) {
            keyDes = p.findKeyDeserializer(config, keyType, property);
         }

         TypeDeserializer contentTypeDeser = (TypeDeserializer)contentType.getTypeHandler();
         if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType, property);
         }

         return this._findCustomMapLikeDeserializer(type, config, p, beanDesc, property, keyDes, contentTypeDeser, contentDeser);
      }
   }

   public JsonDeserializer<?> createEnumDeserializer(DeserializationConfig config, DeserializerProvider p, JavaType type, BeanProperty property) throws JsonMappingException {
      BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspectForCreation(type);
      JsonDeserializer<?> des = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
      if (des != null) {
         return des;
      } else {
         Class<?> enumClass = type.getRawClass();
         JsonDeserializer<?> custom = this._findCustomEnumDeserializer(enumClass, config, beanDesc, property);
         if (custom != null) {
            return custom;
         } else {
            Iterator i$ = beanDesc.getFactoryMethods().iterator();

            AnnotatedMethod factory;
            do {
               if (!i$.hasNext()) {
                  return new EnumDeserializer(this.constructEnumResolver(enumClass, config));
               }

               factory = (AnnotatedMethod)i$.next();
            } while(!config.getAnnotationIntrospector().hasCreatorAnnotation(factory));

            int argCount = factory.getParameterCount();
            if (argCount == 1) {
               Class<?> returnType = factory.getRawType();
               if (returnType.isAssignableFrom(enumClass)) {
                  return EnumDeserializer.deserializerForCreator(config, enumClass, factory);
               }
            }

            throw new IllegalArgumentException("Unsuitable method (" + factory + ") decorated with @JsonCreator (for Enum type " + enumClass.getName() + ")");
         }
      }
   }

   public JsonDeserializer<?> createTreeDeserializer(DeserializationConfig config, DeserializerProvider p, JavaType nodeType, BeanProperty property) throws JsonMappingException {
      Class<? extends JsonNode> nodeClass = nodeType.getRawClass();
      JsonDeserializer<?> custom = this._findCustomTreeNodeDeserializer(nodeClass, config, property);
      return custom != null ? custom : JsonNodeDeserializer.getDeserializer(nodeClass);
   }

   protected JsonDeserializer<Object> findStdBeanDeserializer(DeserializationConfig config, DeserializerProvider p, JavaType type, BeanProperty property) throws JsonMappingException {
      JsonDeserializer<Object> deser = (JsonDeserializer)_simpleDeserializers.get(type);
      if (deser != null) {
         return deser;
      } else {
         Class<?> cls = type.getRawClass();
         if (!AtomicReference.class.isAssignableFrom(cls)) {
            JsonDeserializer<?> d = this.optionalHandlers.findDeserializer(type, config, p);
            return d != null ? d : null;
         } else {
            TypeFactory tf = config.getTypeFactory();
            JavaType[] params = tf.findTypeParameters(type, AtomicReference.class);
            JavaType referencedType;
            if (params != null && params.length >= 1) {
               referencedType = params[0];
            } else {
               referencedType = TypeFactory.unknownType();
            }

            JsonDeserializer<?> d2 = new StdDeserializer.AtomicReferenceDeserializer(referencedType, property);
            return d2;
         }
      }
   }

   public TypeDeserializer findTypeDeserializer(DeserializationConfig config, JavaType baseType, BeanProperty property) {
      Class<?> cls = baseType.getRawClass();
      BasicBeanDescription bean = (BasicBeanDescription)config.introspectClassAnnotations(cls);
      AnnotatedClass ac = bean.getClassInfo();
      AnnotationIntrospector ai = config.getAnnotationIntrospector();
      TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);
      Collection<NamedType> subtypes = null;
      if (b == null) {
         b = config.getDefaultTyper(baseType);
         if (b == null) {
            return null;
         }
      } else {
         subtypes = config.getSubtypeResolver().collectAndResolveSubtypes((AnnotatedClass)ac, config, ai);
      }

      return b.buildTypeDeserializer(config, baseType, subtypes, property);
   }

   public TypeDeserializer findPropertyTypeDeserializer(DeserializationConfig config, JavaType baseType, AnnotatedMember annotated, BeanProperty property) {
      AnnotationIntrospector ai = config.getAnnotationIntrospector();
      TypeResolverBuilder<?> b = ai.findPropertyTypeResolver(config, annotated, baseType);
      if (b == null) {
         return this.findTypeDeserializer(config, baseType, property);
      } else {
         Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes((AnnotatedMember)annotated, config, ai);
         return b.buildTypeDeserializer(config, baseType, subtypes, property);
      }
   }

   public TypeDeserializer findPropertyContentTypeDeserializer(DeserializationConfig config, JavaType containerType, AnnotatedMember propertyEntity, BeanProperty property) {
      AnnotationIntrospector ai = config.getAnnotationIntrospector();
      TypeResolverBuilder<?> b = ai.findPropertyContentTypeResolver(config, propertyEntity, containerType);
      JavaType contentType = containerType.getContentType();
      if (b == null) {
         return this.findTypeDeserializer(config, contentType, property);
      } else {
         Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes((AnnotatedMember)propertyEntity, config, ai);
         return b.buildTypeDeserializer(config, contentType, subtypes, property);
      }
   }

   protected abstract JavaType mapAbstractType(DeserializationConfig var1, JavaType var2) throws JsonMappingException;

   protected JsonDeserializer<Object> findDeserializerFromAnnotation(DeserializationConfig config, Annotated ann, BeanProperty property) throws JsonMappingException {
      Object deserDef = config.getAnnotationIntrospector().findDeserializer(ann);
      return deserDef != null ? this._constructDeserializer(config, ann, property, deserDef) : null;
   }

   JsonDeserializer<Object> _constructDeserializer(DeserializationConfig config, Annotated ann, BeanProperty property, Object deserDef) throws JsonMappingException {
      if (deserDef instanceof JsonDeserializer) {
         JsonDeserializer<Object> deser = (JsonDeserializer)deserDef;
         if (deser instanceof ContextualDeserializer) {
            deser = ((ContextualDeserializer)deser).createContextual(config, property);
         }

         return deser;
      } else if (!(deserDef instanceof Class)) {
         throw new IllegalStateException("AnnotationIntrospector returned deserializer definition of type " + deserDef.getClass().getName() + "; expected type JsonDeserializer or Class<JsonDeserializer> instead");
      } else {
         Class<? extends JsonDeserializer<?>> deserClass = (Class)deserDef;
         if (!JsonDeserializer.class.isAssignableFrom(deserClass)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class " + deserClass.getName() + "; expected Class<JsonDeserializer>");
         } else {
            JsonDeserializer<Object> deser = config.deserializerInstance(ann, deserClass);
            if (deser instanceof ContextualDeserializer) {
               deser = ((ContextualDeserializer)deser).createContextual(config, property);
            }

            return deser;
         }
      }
   }

   protected <T extends JavaType> T modifyTypeByAnnotation(DeserializationConfig config, Annotated a, T type, String propName) throws JsonMappingException {
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      Class<?> subclass = intr.findDeserializationType(a, type, propName);
      if (subclass != null) {
         try {
            type = type.narrowBy(subclass);
         } catch (IllegalArgumentException var15) {
            throw new JsonMappingException("Failed to narrow type " + type + " with concrete-type annotation (value " + subclass.getName() + "), method '" + a.getName() + "': " + var15.getMessage(), (JsonLocation)null, var15);
         }
      }

      if (type.isContainerType()) {
         Class<?> keyClass = intr.findDeserializationKeyType(a, type.getKeyType(), propName);
         if (keyClass != null) {
            if (!(type instanceof MapType)) {
               throw new JsonMappingException("Illegal key-type annotation: type " + type + " is not a Map type");
            }

            try {
               type = ((MapType)type).narrowKey(keyClass);
            } catch (IllegalArgumentException var14) {
               throw new JsonMappingException("Failed to narrow key type " + type + " with key-type annotation (" + keyClass.getName() + "): " + var14.getMessage(), (JsonLocation)null, var14);
            }
         }

         JavaType keyType = type.getKeyType();
         Class cc;
         if (keyType != null && keyType.getValueHandler() == null) {
            cc = intr.findKeyDeserializer(a);
            if (cc != null && cc != KeyDeserializer.None.class) {
               KeyDeserializer kd = config.keyDeserializerInstance(a, cc);
               keyType.setValueHandler(kd);
            }
         }

         cc = intr.findDeserializationContentType(a, type.getContentType(), propName);
         if (cc != null) {
            try {
               type = type.narrowContentsBy(cc);
            } catch (IllegalArgumentException var13) {
               throw new JsonMappingException("Failed to narrow content type " + type + " with content-type annotation (" + cc.getName() + "): " + var13.getMessage(), (JsonLocation)null, var13);
            }
         }

         JavaType contentType = type.getContentType();
         if (contentType.getValueHandler() == null) {
            Class<? extends JsonDeserializer<?>> cdClass = intr.findContentDeserializer(a);
            if (cdClass != null && cdClass != JsonDeserializer.None.class) {
               JsonDeserializer<Object> cd = config.deserializerInstance(a, cdClass);
               type.getContentType().setValueHandler(cd);
            }
         }
      }

      return type;
   }

   protected JavaType resolveType(DeserializationConfig config, BasicBeanDescription beanDesc, JavaType type, AnnotatedMember member, BeanProperty property) {
      if (type.isContainerType()) {
         AnnotationIntrospector intr = config.getAnnotationIntrospector();
         JavaType keyType = type.getKeyType();
         Class cdClass;
         if (keyType != null) {
            cdClass = intr.findKeyDeserializer(member);
            if (cdClass != null && cdClass != KeyDeserializer.None.class) {
               KeyDeserializer kd = config.keyDeserializerInstance(member, cdClass);
               keyType.setValueHandler(kd);
            }
         }

         cdClass = intr.findContentDeserializer(member);
         if (cdClass != null && cdClass != JsonDeserializer.None.class) {
            JsonDeserializer<Object> cd = config.deserializerInstance(member, cdClass);
            type.getContentType().setValueHandler(cd);
         }

         if (member instanceof AnnotatedMember) {
            TypeDeserializer contentTypeDeser = this.findPropertyContentTypeDeserializer(config, type, member, property);
            if (contentTypeDeser != null) {
               type = type.withContentTypeHandler(contentTypeDeser);
            }
         }
      }

      TypeDeserializer valueTypeDeser;
      if (member instanceof AnnotatedMember) {
         valueTypeDeser = this.findPropertyTypeDeserializer(config, type, member, property);
      } else {
         valueTypeDeser = this.findTypeDeserializer(config, type, (BeanProperty)null);
      }

      if (valueTypeDeser != null) {
         type = type.withTypeHandler(valueTypeDeser);
      }

      return type;
   }

   protected EnumResolver<?> constructEnumResolver(Class<?> enumClass, DeserializationConfig config) {
      return config.isEnabled(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING) ? EnumResolver.constructUnsafeUsingToString(enumClass) : EnumResolver.constructUnsafe(enumClass, config.getAnnotationIntrospector());
   }

   protected CreatorContainer findMapCreators(DeserializationConfig config, BasicBeanDescription beanDesc) throws JsonMappingException {
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      boolean fixAccess = config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
      CreatorContainer creators = new CreatorContainer(beanDesc, fixAccess);
      Iterator i$ = beanDesc.getConstructors().iterator();

      while(true) {
         AnnotatedConstructor ctor;
         int argCount;
         SettableBeanProperty[] properties;
         int nameCount;
         int i;
         AnnotatedParameter param;
         String name;
         do {
            do {
               if (!i$.hasNext()) {
                  i$ = beanDesc.getFactoryMethods().iterator();

                  while(true) {
                     AnnotatedMethod factory;
                     do {
                        do {
                           if (!i$.hasNext()) {
                              return creators;
                           }

                           factory = (AnnotatedMethod)i$.next();
                           argCount = factory.getParameterCount();
                        } while(argCount < 1);
                     } while(!intr.hasCreatorAnnotation(factory));

                     properties = new SettableBeanProperty[argCount];
                     nameCount = 0;

                     for(i = 0; i < argCount; ++i) {
                        param = factory.getParameter(i);
                        name = param == null ? null : intr.findPropertyNameForParam(param);
                        if (name == null || name.length() == 0) {
                           throw new IllegalArgumentException("Parameter #" + i + " of factory method " + factory + " has no property name annotation: must have for @JsonCreator for a Map type");
                        }

                        ++nameCount;
                        properties[i] = this.constructCreatorProperty(config, beanDesc, name, i, param);
                     }

                     creators.addPropertyFactory(factory, properties);
                  }
               }

               ctor = (AnnotatedConstructor)i$.next();
               argCount = ctor.getParameterCount();
            } while(argCount < 1);
         } while(!intr.hasCreatorAnnotation(ctor));

         properties = new SettableBeanProperty[argCount];
         nameCount = 0;

         for(i = 0; i < argCount; ++i) {
            param = ctor.getParameter(i);
            name = param == null ? null : intr.findPropertyNameForParam(param);
            if (name == null || name.length() == 0) {
               throw new IllegalArgumentException("Parameter #" + i + " of constructor " + ctor + " has no property name annotation: must have for @JsonCreator for a Map type");
            }

            ++nameCount;
            properties[i] = this.constructCreatorProperty(config, beanDesc, name, i, param);
         }

         creators.addPropertyConstructor(ctor, properties);
      }
   }

   protected SettableBeanProperty constructCreatorProperty(DeserializationConfig config, BasicBeanDescription beanDesc, String name, int index, AnnotatedParameter param) throws JsonMappingException {
      JavaType t0 = config.getTypeFactory().constructType(param.getParameterType(), beanDesc.bindingsForBeanType());
      BeanProperty.Std property = new BeanProperty.Std(name, t0, beanDesc.getClassAnnotations(), param);
      JavaType type = this.resolveType(config, beanDesc, t0, param, property);
      if (type != t0) {
         property = property.withType(type);
      }

      JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, param, property);
      type = this.modifyTypeByAnnotation(config, param, type, name);
      TypeDeserializer typeDeser = this.findTypeDeserializer(config, type, property);
      SettableBeanProperty prop = new SettableBeanProperty.CreatorProperty(name, type, typeDeser, beanDesc.getClassAnnotations(), param, index);
      if (deser != null) {
         prop.setValueDeserializer(deser);
      }

      return prop;
   }

   static {
      _mapFallbacks.put(Map.class.getName(), LinkedHashMap.class);
      _mapFallbacks.put(ConcurrentMap.class.getName(), ConcurrentHashMap.class);
      _mapFallbacks.put(SortedMap.class.getName(), TreeMap.class);
      _mapFallbacks.put("java.util.NavigableMap", TreeMap.class);

      try {
         Class<?> key = Class.forName("java.util.ConcurrentNavigableMap");
         Class<?> value = Class.forName("java.util.ConcurrentSkipListMap");
         _mapFallbacks.put(key.getName(), value);
      } catch (ClassNotFoundException var3) {
      }

      _collectionFallbacks = new HashMap();
      _collectionFallbacks.put(Collection.class.getName(), ArrayList.class);
      _collectionFallbacks.put(List.class.getName(), ArrayList.class);
      _collectionFallbacks.put(Set.class.getName(), HashSet.class);
      _collectionFallbacks.put(SortedSet.class.getName(), TreeSet.class);
      _collectionFallbacks.put(Queue.class.getName(), LinkedList.class);
      _collectionFallbacks.put("java.util.Deque", LinkedList.class);
      _collectionFallbacks.put("java.util.NavigableSet", TreeSet.class);
      _arrayDeserializers = ArrayDeserializers.getAll();
   }
}
