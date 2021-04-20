package org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ResolvableSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.JavaType;

@JacksonStdImpl
public class MapSerializer extends ContainerSerializerBase<Map<?, ?>> implements ResolvableSerializer {
   protected static final JavaType UNSPECIFIED_TYPE = TypeFactory.unknownType();
   protected final BeanProperty _property;
   protected final HashSet<String> _ignoredEntries;
   protected final boolean _valueTypeIsStatic;
   protected final JavaType _keyType;
   protected final JavaType _valueType;
   protected JsonSerializer<Object> _keySerializer;
   protected JsonSerializer<Object> _valueSerializer;
   protected final TypeSerializer _valueTypeSerializer;
   protected PropertySerializerMap _dynamicValueSerializers;

   protected MapSerializer() {
      this((HashSet)null, (JavaType)null, (JavaType)null, false, (TypeSerializer)null, (JsonSerializer)null, (JsonSerializer)null, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   protected MapSerializer(HashSet<String> ignoredEntries, JavaType valueType, boolean valueTypeIsStatic, TypeSerializer vts) {
      this(ignoredEntries, UNSPECIFIED_TYPE, valueType, valueTypeIsStatic, vts, (JsonSerializer)null, (JsonSerializer)null, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   protected MapSerializer(HashSet<String> ignoredEntries, JavaType keyType, JavaType valueType, boolean valueTypeIsStatic, TypeSerializer vts, JsonSerializer<Object> keySerializer, BeanProperty property) {
      this(ignoredEntries, keyType, valueType, valueTypeIsStatic, vts, keySerializer, (JsonSerializer)null, property);
   }

   protected MapSerializer(HashSet<String> ignoredEntries, JavaType keyType, JavaType valueType, boolean valueTypeIsStatic, TypeSerializer vts, JsonSerializer<Object> keySerializer, JsonSerializer<Object> valueSerializer, BeanProperty property) {
      super(Map.class, false);
      this._property = property;
      this._ignoredEntries = ignoredEntries;
      this._keyType = keyType;
      this._valueType = valueType;
      this._valueTypeIsStatic = valueTypeIsStatic;
      this._valueTypeSerializer = vts;
      this._keySerializer = keySerializer;
      this._valueSerializer = valueSerializer;
      this._dynamicValueSerializers = PropertySerializerMap.emptyMap();
   }

   public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
      MapSerializer ms = new MapSerializer(this._ignoredEntries, this._keyType, this._valueType, this._valueTypeIsStatic, vts, this._keySerializer, this._valueSerializer, this._property);
      if (this._valueSerializer != null) {
         ms._valueSerializer = this._valueSerializer;
      }

      return ms;
   }

   /** @deprecated */
   @Deprecated
   public static MapSerializer construct(String[] ignoredList, JavaType mapType, boolean staticValueType, TypeSerializer vts, BeanProperty property) {
      return construct(ignoredList, mapType, staticValueType, vts, property, (JsonSerializer)null, (JsonSerializer)null);
   }

   public static MapSerializer construct(String[] ignoredList, JavaType mapType, boolean staticValueType, TypeSerializer vts, BeanProperty property, JsonSerializer<Object> keySerializer, JsonSerializer<Object> valueSerializer) {
      HashSet<String> ignoredEntries = toSet(ignoredList);
      JavaType keyType;
      JavaType valueType;
      if (mapType == null) {
         keyType = valueType = UNSPECIFIED_TYPE;
      } else {
         keyType = mapType.getKeyType();
         valueType = mapType.getContentType();
      }

      if (!staticValueType) {
         staticValueType = valueType != null && valueType.isFinal();
      }

      return new MapSerializer(ignoredEntries, keyType, valueType, staticValueType, vts, keySerializer, valueSerializer, property);
   }

   private static HashSet<String> toSet(String[] ignoredEntries) {
      if (ignoredEntries != null && ignoredEntries.length != 0) {
         HashSet<String> result = new HashSet(ignoredEntries.length);
         String[] arr$ = ignoredEntries;
         int len$ = ignoredEntries.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String prop = arr$[i$];
            result.add(prop);
         }

         return result;
      } else {
         return null;
      }
   }

   public void serialize(Map<?, ?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      jgen.writeStartObject();
      if (!value.isEmpty()) {
         if (this._valueSerializer != null) {
            this.serializeFieldsUsing(value, jgen, provider, this._valueSerializer);
         } else {
            this.serializeFields(value, jgen, provider);
         }
      }

      jgen.writeEndObject();
   }

   public void serializeWithType(Map<?, ?> value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
      typeSer.writeTypePrefixForObject(value, jgen);
      if (!value.isEmpty()) {
         if (this._valueSerializer != null) {
            this.serializeFieldsUsing(value, jgen, provider, this._valueSerializer);
         } else {
            this.serializeFields(value, jgen, provider);
         }
      }

      typeSer.writeTypeSuffixForObject(value, jgen);
   }

   protected void serializeFields(Map<?, ?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      if (this._valueTypeSerializer != null) {
         this.serializeTypedFields(value, jgen, provider);
      } else {
         JsonSerializer<Object> keySerializer = this._keySerializer;
         HashSet<String> ignored = this._ignoredEntries;
         boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);
         PropertySerializerMap serializers = this._dynamicValueSerializers;
         Iterator i$ = value.entrySet().iterator();

         while(true) {
            Object valueElem;
            Object keyElem;
            while(true) {
               if (!i$.hasNext()) {
                  return;
               }

               Entry<?, ?> entry = (Entry)i$.next();
               valueElem = entry.getValue();
               keyElem = entry.getKey();
               if (keyElem == null) {
                  provider.getNullKeySerializer().serialize((Object)null, jgen, provider);
                  break;
               }

               if ((!skipNulls || valueElem != null) && (ignored == null || !ignored.contains(keyElem))) {
                  keySerializer.serialize(keyElem, jgen, provider);
                  break;
               }
            }

            if (valueElem == null) {
               provider.defaultSerializeNull(jgen);
            } else {
               Class<?> cc = valueElem.getClass();
               JsonSerializer<Object> serializer = serializers.serializerFor(cc);
               if (serializer == null) {
                  if (this._valueType.hasGenericTypes()) {
                     serializer = this._findAndAddDynamic(serializers, this._valueType.forcedNarrowBy(cc), provider);
                  } else {
                     serializer = this._findAndAddDynamic(serializers, cc, provider);
                  }

                  serializers = this._dynamicValueSerializers;
               }

               try {
                  serializer.serialize(valueElem, jgen, provider);
               } catch (Exception var16) {
                  String keyDesc = "" + keyElem;
                  this.wrapAndThrow(provider, var16, value, keyDesc);
               }
            }
         }
      }
   }

   protected void serializeFieldsUsing(Map<?, ?> value, JsonGenerator jgen, SerializerProvider provider, JsonSerializer<Object> ser) throws IOException, JsonGenerationException {
      JsonSerializer<Object> keySerializer = this._keySerializer;
      HashSet<String> ignored = this._ignoredEntries;
      TypeSerializer typeSer = this._valueTypeSerializer;
      boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);
      Iterator i$ = value.entrySet().iterator();

      while(true) {
         Object valueElem;
         Object keyElem;
         while(true) {
            if (!i$.hasNext()) {
               return;
            }

            Entry<?, ?> entry = (Entry)i$.next();
            valueElem = entry.getValue();
            keyElem = entry.getKey();
            if (keyElem == null) {
               provider.getNullKeySerializer().serialize((Object)null, jgen, provider);
               break;
            }

            if ((!skipNulls || valueElem != null) && (ignored == null || !ignored.contains(keyElem))) {
               keySerializer.serialize(keyElem, jgen, provider);
               break;
            }
         }

         if (valueElem == null) {
            provider.defaultSerializeNull(jgen);
         } else {
            try {
               if (typeSer == null) {
                  ser.serialize(valueElem, jgen, provider);
               } else {
                  ser.serializeWithType(valueElem, jgen, provider, typeSer);
               }
            } catch (Exception var15) {
               String keyDesc = "" + keyElem;
               this.wrapAndThrow(provider, var15, value, keyDesc);
            }
         }
      }
   }

   protected void serializeTypedFields(Map<?, ?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      JsonSerializer<Object> keySerializer = this._keySerializer;
      JsonSerializer<Object> prevValueSerializer = null;
      Class<?> prevValueClass = null;
      HashSet<String> ignored = this._ignoredEntries;
      boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);
      Iterator i$ = value.entrySet().iterator();

      while(true) {
         Object valueElem;
         Object keyElem;
         while(true) {
            if (!i$.hasNext()) {
               return;
            }

            Entry<?, ?> entry = (Entry)i$.next();
            valueElem = entry.getValue();
            keyElem = entry.getKey();
            if (keyElem == null) {
               provider.getNullKeySerializer().serialize((Object)null, jgen, provider);
               break;
            }

            if ((!skipNulls || valueElem != null) && (ignored == null || !ignored.contains(keyElem))) {
               keySerializer.serialize(keyElem, jgen, provider);
               break;
            }
         }

         if (valueElem == null) {
            provider.defaultSerializeNull(jgen);
         } else {
            Class<?> cc = valueElem.getClass();
            JsonSerializer currSerializer;
            if (cc == prevValueClass) {
               currSerializer = prevValueSerializer;
            } else {
               currSerializer = provider.findValueSerializer(cc, this._property);
               prevValueSerializer = currSerializer;
               prevValueClass = cc;
            }

            try {
               currSerializer.serializeWithType(valueElem, jgen, provider, this._valueTypeSerializer);
            } catch (Exception var17) {
               String keyDesc = "" + keyElem;
               this.wrapAndThrow(provider, var17, value, keyDesc);
            }
         }
      }
   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      ObjectNode o = this.createSchemaNode("object", true);
      return o;
   }

   public void resolve(SerializerProvider provider) throws JsonMappingException {
      if (this._valueTypeIsStatic && this._valueSerializer == null) {
         this._valueSerializer = provider.findValueSerializer(this._valueType, this._property);
      }

      if (this._keySerializer == null) {
         this._keySerializer = provider.findKeySerializer(this._keyType, this._property);
      }

   }

   protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map, Class<?> type, SerializerProvider provider) throws JsonMappingException {
      PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, this._property);
      if (map != result.map) {
         this._dynamicValueSerializers = result.map;
      }

      return result.serializer;
   }

   protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map, JavaType type, SerializerProvider provider) throws JsonMappingException {
      PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, this._property);
      if (map != result.map) {
         this._dynamicValueSerializers = result.map;
      }

      return result.serializer;
   }
}
