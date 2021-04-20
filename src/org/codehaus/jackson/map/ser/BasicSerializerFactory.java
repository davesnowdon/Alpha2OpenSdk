package org.codehaus.jackson.map.ser;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.TimeZone;
import java.util.Map.Entry;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.ContextualSerializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializable;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerFactory;
import org.codehaus.jackson.map.Serializers;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ext.OptionalHandlerFactory;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.ser.impl.IndexedStringListSerializer;
import org.codehaus.jackson.map.ser.impl.InetAddressSerializer;
import org.codehaus.jackson.map.ser.impl.ObjectArraySerializer;
import org.codehaus.jackson.map.ser.impl.StringCollectionSerializer;
import org.codehaus.jackson.map.ser.impl.TimeZoneSerializer;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.util.EnumValues;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.util.TokenBuffer;

public abstract class BasicSerializerFactory extends SerializerFactory {
   protected static final HashMap<String, JsonSerializer<?>> _concrete = new HashMap();
   protected static final HashMap<String, Class<? extends JsonSerializer<?>>> _concreteLazy = new HashMap();
   protected static final HashMap<String, JsonSerializer<?>> _arraySerializers;
   protected OptionalHandlerFactory optionalHandlers;

   protected BasicSerializerFactory() {
      this.optionalHandlers = OptionalHandlerFactory.instance;
   }

   public abstract JsonSerializer<Object> createSerializer(SerializationConfig var1, JavaType var2, BeanProperty var3) throws JsonMappingException;

   public TypeSerializer createTypeSerializer(SerializationConfig config, JavaType baseType, BeanProperty property) {
      BasicBeanDescription bean = (BasicBeanDescription)config.introspectClassAnnotations(baseType.getRawClass());
      AnnotatedClass ac = bean.getClassInfo();
      AnnotationIntrospector ai = config.getAnnotationIntrospector();
      TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);
      Collection<NamedType> subtypes = null;
      if (b == null) {
         b = config.getDefaultTyper(baseType);
      } else {
         subtypes = config.getSubtypeResolver().collectAndResolveSubtypes((AnnotatedClass)ac, config, ai);
      }

      return b == null ? null : b.buildTypeSerializer(config, baseType, subtypes, property);
   }

   public final JsonSerializer<?> getNullSerializer() {
      return NullSerializer.instance;
   }

   protected abstract Iterable<Serializers> customSerializers();

   public final JsonSerializer<?> findSerializerByLookup(JavaType type, SerializationConfig config, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping) {
      Class<?> raw = type.getRawClass();
      String clsName = raw.getName();
      JsonSerializer<?> ser = (JsonSerializer)_concrete.get(clsName);
      if (ser != null) {
         return ser;
      } else {
         Class<? extends JsonSerializer<?>> serClass = (Class)_concreteLazy.get(clsName);
         if (serClass != null) {
            try {
               return (JsonSerializer)serClass.newInstance();
            } catch (Exception var11) {
               throw new IllegalStateException("Failed to instantiate standard serializer (of type " + serClass.getName() + "): " + var11.getMessage(), var11);
            }
         } else {
            return null;
         }
      }
   }

   public final JsonSerializer<?> findSerializerByPrimaryType(JavaType type, SerializationConfig config, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping) throws JsonMappingException {
      Class<?> raw = type.getRawClass();
      if (JsonSerializable.class.isAssignableFrom(raw)) {
         return (JsonSerializer)(JsonSerializableWithType.class.isAssignableFrom(raw) ? StdSerializers.SerializableWithTypeSerializer.instance : StdSerializers.SerializableSerializer.instance);
      } else {
         AnnotatedMethod valueMethod = beanDesc.findJsonValueMethod();
         if (valueMethod != null) {
            Method m = valueMethod.getAnnotated();
            if (config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
               ClassUtil.checkAndFixAccess(m);
            }

            JsonSerializer<Object> ser = this.findSerializerFromAnnotation(config, valueMethod, property);
            return new JsonValueSerializer(m, ser, property);
         } else if (InetAddress.class.isAssignableFrom(raw)) {
            return InetAddressSerializer.instance;
         } else if (TimeZone.class.isAssignableFrom(raw)) {
            return TimeZoneSerializer.instance;
         } else {
            JsonSerializer<?> ser = this.optionalHandlers.findSerializer(config, type);
            if (ser != null) {
               return ser;
            } else if (Number.class.isAssignableFrom(raw)) {
               return StdSerializers.NumberSerializer.instance;
            } else if (Enum.class.isAssignableFrom(raw)) {
               return EnumSerializer.construct(raw, config, beanDesc);
            } else if (Calendar.class.isAssignableFrom(raw)) {
               return StdSerializers.CalendarSerializer.instance;
            } else {
               return Date.class.isAssignableFrom(raw) ? StdSerializers.UtilDateSerializer.instance : null;
            }
         }
      }
   }

   public final JsonSerializer<?> findSerializerByAddonType(SerializationConfig config, JavaType javaType, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping) throws JsonMappingException {
      Class<?> type = javaType.getRawClass();
      if (Iterator.class.isAssignableFrom(type)) {
         return this.buildIteratorSerializer(config, javaType, beanDesc, property, staticTyping);
      } else if (Iterable.class.isAssignableFrom(type)) {
         return this.buildIterableSerializer(config, javaType, beanDesc, property, staticTyping);
      } else {
         return CharSequence.class.isAssignableFrom(type) ? ToStringSerializer.instance : null;
      }
   }

   protected JsonSerializer<Object> findSerializerFromAnnotation(SerializationConfig config, Annotated a, BeanProperty property) throws JsonMappingException {
      Object serDef = config.getAnnotationIntrospector().findSerializer(a);
      if (serDef == null) {
         return null;
      } else if (serDef instanceof JsonSerializer) {
         JsonSerializer<Object> ser = (JsonSerializer)serDef;
         return ser instanceof ContextualSerializer ? ((ContextualSerializer)ser).createContextual(config, property) : ser;
      } else if (!(serDef instanceof Class)) {
         throw new IllegalStateException("AnnotationIntrospector returned value of type " + serDef.getClass().getName() + "; expected type JsonSerializer or Class<JsonSerializer> instead");
      } else {
         Class<?> cls = (Class)serDef;
         if (!JsonSerializer.class.isAssignableFrom(cls)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class " + cls.getName() + "; expected Class<JsonSerializer>");
         } else {
            JsonSerializer<Object> ser = config.serializerInstance(a, cls);
            return ser instanceof ContextualSerializer ? ((ContextualSerializer)ser).createContextual(config, property) : ser;
         }
      }
   }

   public JsonSerializer<?> buildContainerSerializer(SerializationConfig config, JavaType type, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping) {
      JavaType elementType = type.getContentType();
      TypeSerializer elementTypeSerializer = this.createTypeSerializer(config, elementType, property);
      if (elementTypeSerializer != null) {
         staticTyping = false;
      } else if (!staticTyping) {
         staticTyping = this.usesStaticTyping(config, beanDesc, elementTypeSerializer, property);
      }

      JsonSerializer<Object> elementValueSerializer = findContentSerializer(config, beanDesc.getClassInfo(), property);
      if (type.isMapLikeType()) {
         MapLikeType mlt = (MapLikeType)type;
         JsonSerializer<Object> keySerializer = findKeySerializer(config, beanDesc.getClassInfo(), property);
         return mlt.isTrueMapType() ? this.buildMapSerializer(config, (MapType)mlt, beanDesc, property, staticTyping, keySerializer, elementTypeSerializer, elementValueSerializer) : this.buildMapLikeSerializer(config, mlt, beanDesc, property, staticTyping, keySerializer, elementTypeSerializer, elementValueSerializer);
      } else if (type.isCollectionLikeType()) {
         CollectionLikeType clt = (CollectionLikeType)type;
         return clt.isTrueCollectionType() ? this.buildCollectionSerializer(config, (CollectionType)clt, beanDesc, property, staticTyping, elementTypeSerializer, elementValueSerializer) : this.buildCollectionLikeSerializer(config, clt, beanDesc, property, staticTyping, elementTypeSerializer, elementValueSerializer);
      } else {
         return type.isArrayType() ? this.buildArraySerializer(config, (ArrayType)type, beanDesc, property, staticTyping, elementTypeSerializer, elementValueSerializer) : null;
      }
   }

   protected JsonSerializer<?> buildCollectionLikeSerializer(SerializationConfig config, CollectionLikeType type, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
      Iterator i$ = this.customSerializers().iterator();

      JsonSerializer ser;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         Serializers serializers = (Serializers)i$.next();
         ser = serializers.findCollectionLikeSerializer(config, type, beanDesc, property, elementTypeSerializer, elementValueSerializer);
      } while(ser == null);

      return ser;
   }

   protected JsonSerializer<?> buildCollectionSerializer(SerializationConfig config, CollectionType type, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
      Iterator i$ = this.customSerializers().iterator();

      JsonSerializer ser;
      do {
         if (!i$.hasNext()) {
            Class<?> raw = type.getRawClass();
            if (EnumSet.class.isAssignableFrom(raw)) {
               return this.buildEnumSetSerializer(config, type, beanDesc, property, staticTyping, elementTypeSerializer, elementValueSerializer);
            }

            Class<?> elementRaw = type.getContentType().getRawClass();
            if (this.isIndexedList(raw)) {
               if (elementRaw == String.class) {
                  return new IndexedStringListSerializer(property);
               }

               return ContainerSerializers.indexedListSerializer(type.getContentType(), staticTyping, elementTypeSerializer, property, elementValueSerializer);
            }

            if (elementRaw == String.class) {
               return new StringCollectionSerializer(property);
            }

            return ContainerSerializers.collectionSerializer(type.getContentType(), staticTyping, elementTypeSerializer, property, elementValueSerializer);
         }

         Serializers serializers = (Serializers)i$.next();
         ser = serializers.findCollectionSerializer(config, type, beanDesc, property, elementTypeSerializer, elementValueSerializer);
      } while(ser == null);

      return ser;
   }

   protected JsonSerializer<?> buildEnumSetSerializer(SerializationConfig config, JavaType type, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
      JavaType enumType = type.getContentType();
      if (!enumType.isEnumType()) {
         enumType = null;
      }

      return ContainerSerializers.enumSetSerializer(enumType, property);
   }

   protected boolean isIndexedList(Class<?> cls) {
      return RandomAccess.class.isAssignableFrom(cls);
   }

   protected JsonSerializer<?> buildMapLikeSerializer(SerializationConfig config, MapLikeType type, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
      Iterator i$ = this.customSerializers().iterator();

      JsonSerializer ser;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         Serializers serializers = (Serializers)i$.next();
         ser = serializers.findMapLikeSerializer(config, type, beanDesc, property, keySerializer, elementTypeSerializer, elementValueSerializer);
      } while(ser == null);

      return ser;
   }

   protected JsonSerializer<?> buildMapSerializer(SerializationConfig config, MapType type, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
      Iterator i$ = this.customSerializers().iterator();

      JsonSerializer ser;
      do {
         if (!i$.hasNext()) {
            if (EnumMap.class.isAssignableFrom(type.getRawClass())) {
               return this.buildEnumMapSerializer(config, type, beanDesc, property, staticTyping, elementTypeSerializer, elementValueSerializer);
            }

            return MapSerializer.construct(config.getAnnotationIntrospector().findPropertiesToIgnore(beanDesc.getClassInfo()), type, staticTyping, elementTypeSerializer, property, keySerializer, elementValueSerializer);
         }

         Serializers serializers = (Serializers)i$.next();
         ser = serializers.findMapSerializer(config, type, beanDesc, property, keySerializer, elementTypeSerializer, elementValueSerializer);
      } while(ser == null);

      return ser;
   }

   protected JsonSerializer<?> buildEnumMapSerializer(SerializationConfig config, JavaType type, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
      JavaType keyType = type.getKeyType();
      EnumValues enums = null;
      if (keyType.isEnumType()) {
         Class<Enum<?>> enumClass = keyType.getRawClass();
         enums = EnumValues.construct(enumClass, config.getAnnotationIntrospector());
      }

      return new EnumMapSerializer(type.getContentType(), staticTyping, enums, elementTypeSerializer, property, elementValueSerializer);
   }

   protected JsonSerializer<?> buildArraySerializer(SerializationConfig config, ArrayType type, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
      Class<?> raw = type.getRawClass();
      if (String[].class == raw) {
         return new ArraySerializers.StringArraySerializer(property);
      } else {
         JsonSerializer<?> ser = (JsonSerializer)_arraySerializers.get(raw.getName());
         return (JsonSerializer)(ser != null ? ser : new ObjectArraySerializer(type.getContentType(), staticTyping, elementTypeSerializer, property, elementValueSerializer));
      }
   }

   protected JsonSerializer<?> buildIteratorSerializer(SerializationConfig config, JavaType type, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping) {
      JavaType valueType = type.containedType(0);
      if (valueType == null) {
         valueType = TypeFactory.unknownType();
      }

      TypeSerializer vts = this.createTypeSerializer(config, valueType, property);
      return ContainerSerializers.iteratorSerializer(valueType, this.usesStaticTyping(config, beanDesc, vts, property), vts, property);
   }

   protected JsonSerializer<?> buildIterableSerializer(SerializationConfig config, JavaType type, BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping) {
      JavaType valueType = type.containedType(0);
      if (valueType == null) {
         valueType = TypeFactory.unknownType();
      }

      TypeSerializer vts = this.createTypeSerializer(config, valueType, property);
      return ContainerSerializers.iterableSerializer(valueType, this.usesStaticTyping(config, beanDesc, vts, property), vts, property);
   }

   protected <T extends JavaType> T modifyTypeByAnnotation(SerializationConfig config, Annotated a, T type) {
      Class<?> superclass = config.getAnnotationIntrospector().findSerializationType(a);
      if (superclass != null) {
         try {
            type = type.widenBy(superclass);
         } catch (IllegalArgumentException var6) {
            throw new IllegalArgumentException("Failed to widen type " + type + " with concrete-type annotation (value " + superclass.getName() + "), method '" + a.getName() + "': " + var6.getMessage());
         }
      }

      return modifySecondaryTypesByAnnotation(config, a, type);
   }

   protected static <T extends JavaType> T modifySecondaryTypesByAnnotation(SerializationConfig config, Annotated a, T type) {
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      if (type.isContainerType()) {
         Class<?> keyClass = intr.findSerializationKeyType(a, type.getKeyType());
         if (keyClass != null) {
            if (!(type instanceof MapType)) {
               throw new IllegalArgumentException("Illegal key-type annotation: type " + type + " is not a Map type");
            }

            try {
               type = ((MapType)type).widenKey(keyClass);
            } catch (IllegalArgumentException var8) {
               throw new IllegalArgumentException("Failed to narrow key type " + type + " with key-type annotation (" + keyClass.getName() + "): " + var8.getMessage());
            }
         }

         Class<?> cc = intr.findSerializationContentType(a, type.getContentType());
         if (cc != null) {
            try {
               type = type.widenContentsBy(cc);
            } catch (IllegalArgumentException var7) {
               throw new IllegalArgumentException("Failed to narrow content type " + type + " with content-type annotation (" + cc.getName() + "): " + var7.getMessage());
            }
         }
      }

      return type;
   }

   protected static JsonSerializer<Object> findKeySerializer(SerializationConfig config, Annotated a, BeanProperty property) {
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      Class<? extends JsonSerializer<?>> serClass = intr.findKeySerializer(a);
      if ((serClass == null || serClass == JsonSerializer.None.class) && property != null) {
         serClass = intr.findKeySerializer(property.getMember());
      }

      return serClass != null && serClass != JsonSerializer.None.class ? config.serializerInstance(a, serClass) : null;
   }

   protected static JsonSerializer<Object> findContentSerializer(SerializationConfig config, Annotated a, BeanProperty property) {
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      Class<? extends JsonSerializer<?>> serClass = intr.findContentSerializer(a);
      if ((serClass == null || serClass == JsonSerializer.None.class) && property != null) {
         serClass = intr.findContentSerializer(property.getMember());
      }

      return serClass != null && serClass != JsonSerializer.None.class ? config.serializerInstance(a, serClass) : null;
   }

   protected boolean usesStaticTyping(SerializationConfig config, BasicBeanDescription beanDesc, TypeSerializer typeSer, BeanProperty property) {
      if (typeSer != null) {
         return false;
      } else {
         AnnotationIntrospector intr = config.getAnnotationIntrospector();
         JsonSerialize.Typing t = intr.findSerializationTyping(beanDesc.getClassInfo());
         if (t != null) {
            if (t == JsonSerialize.Typing.STATIC) {
               return true;
            }
         } else if (config.isEnabled(SerializationConfig.Feature.USE_STATIC_TYPING)) {
            return true;
         }

         if (property != null) {
            JavaType type = property.getType();
            if (type.isContainerType()) {
               if (intr.findSerializationContentType(property.getMember(), property.getType()) != null) {
                  return true;
               }

               if (type instanceof MapType && intr.findSerializationKeyType(property.getMember(), property.getType()) != null) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   static {
      _concrete.put(String.class.getName(), new StdSerializers.StringSerializer());
      ToStringSerializer sls = ToStringSerializer.instance;
      _concrete.put(StringBuffer.class.getName(), sls);
      _concrete.put(StringBuilder.class.getName(), sls);
      _concrete.put(Character.class.getName(), sls);
      _concrete.put(Character.TYPE.getName(), sls);
      _concrete.put(Boolean.TYPE.getName(), new StdSerializers.BooleanSerializer(true));
      _concrete.put(Boolean.class.getName(), new StdSerializers.BooleanSerializer(false));
      JsonSerializer<?> intS = new StdSerializers.IntegerSerializer();
      _concrete.put(Integer.class.getName(), intS);
      _concrete.put(Integer.TYPE.getName(), intS);
      _concrete.put(Long.class.getName(), StdSerializers.LongSerializer.instance);
      _concrete.put(Long.TYPE.getName(), StdSerializers.LongSerializer.instance);
      _concrete.put(Byte.class.getName(), StdSerializers.IntLikeSerializer.instance);
      _concrete.put(Byte.TYPE.getName(), StdSerializers.IntLikeSerializer.instance);
      _concrete.put(Short.class.getName(), StdSerializers.IntLikeSerializer.instance);
      _concrete.put(Short.TYPE.getName(), StdSerializers.IntLikeSerializer.instance);
      _concrete.put(Float.class.getName(), StdSerializers.FloatSerializer.instance);
      _concrete.put(Float.TYPE.getName(), StdSerializers.FloatSerializer.instance);
      _concrete.put(Double.class.getName(), StdSerializers.DoubleSerializer.instance);
      _concrete.put(Double.TYPE.getName(), StdSerializers.DoubleSerializer.instance);
      JsonSerializer<?> ns = new StdSerializers.NumberSerializer();
      _concrete.put(BigInteger.class.getName(), ns);
      _concrete.put(BigDecimal.class.getName(), ns);
      _concrete.put(Calendar.class.getName(), StdSerializers.CalendarSerializer.instance);
      _concrete.put(Date.class.getName(), StdSerializers.UtilDateSerializer.instance);
      _concrete.put(java.sql.Date.class.getName(), new StdSerializers.SqlDateSerializer());
      _concrete.put(Time.class.getName(), new StdSerializers.SqlTimeSerializer());
      _concrete.put(Timestamp.class.getName(), StdSerializers.UtilDateSerializer.instance);
      Iterator i$ = (new JdkSerializers()).provide().iterator();

      while(i$.hasNext()) {
         Entry<Class<?>, Object> en = (Entry)i$.next();
         Object value = en.getValue();
         if (value instanceof JsonSerializer) {
            _concrete.put(((Class)en.getKey()).getName(), (JsonSerializer)value);
         } else {
            if (!(value instanceof Class)) {
               throw new IllegalStateException("Internal error: unrecognized value of type " + en.getClass().getName());
            }

            Class<? extends JsonSerializer<?>> cls = (Class)value;
            _concreteLazy.put(((Class)en.getKey()).getName(), cls);
         }
      }

      _concreteLazy.put(TokenBuffer.class.getName(), StdSerializers.TokenBufferSerializer.class);
      _arraySerializers = new HashMap();
      _arraySerializers.put(boolean[].class.getName(), new ArraySerializers.BooleanArraySerializer());
      _arraySerializers.put(byte[].class.getName(), new ArraySerializers.ByteArraySerializer());
      _arraySerializers.put(char[].class.getName(), new ArraySerializers.CharArraySerializer());
      _arraySerializers.put(short[].class.getName(), new ArraySerializers.ShortArraySerializer());
      _arraySerializers.put(int[].class.getName(), new ArraySerializers.IntArraySerializer());
      _arraySerializers.put(long[].class.getName(), new ArraySerializers.LongArraySerializer());
      _arraySerializers.put(float[].class.getName(), new ArraySerializers.FloatArraySerializer());
      _arraySerializers.put(double[].class.getName(), new ArraySerializers.DoubleArraySerializer());
   }
}
