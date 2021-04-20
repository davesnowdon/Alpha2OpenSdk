package org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ResolvableSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.util.EnumValues;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.type.JavaType;

@JacksonStdImpl
public class EnumMapSerializer extends ContainerSerializerBase<EnumMap<? extends Enum<?>, ?>> implements ResolvableSerializer {
   protected final boolean _staticTyping;
   protected final EnumValues _keyEnums;
   protected final JavaType _valueType;
   protected final BeanProperty _property;
   protected JsonSerializer<Object> _valueSerializer;
   protected final TypeSerializer _valueTypeSerializer;

   /** @deprecated */
   @Deprecated
   public EnumMapSerializer(JavaType valueType, boolean staticTyping, EnumValues keyEnums, TypeSerializer vts, BeanProperty property) {
      this(valueType, staticTyping, keyEnums, vts, property, (JsonSerializer)null);
   }

   public EnumMapSerializer(JavaType valueType, boolean staticTyping, EnumValues keyEnums, TypeSerializer vts, BeanProperty property, JsonSerializer<Object> valueSerializer) {
      super(EnumMap.class, false);
      this._staticTyping = staticTyping || valueType != null && valueType.isFinal();
      this._valueType = valueType;
      this._keyEnums = keyEnums;
      this._valueTypeSerializer = vts;
      this._property = property;
      this._valueSerializer = valueSerializer;
   }

   public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
      return new EnumMapSerializer(this._valueType, this._staticTyping, this._keyEnums, vts, this._property);
   }

   public void serialize(EnumMap<? extends Enum<?>, ?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      jgen.writeStartObject();
      if (!value.isEmpty()) {
         this.serializeContents(value, jgen, provider);
      }

      jgen.writeEndObject();
   }

   public void serializeWithType(EnumMap<? extends Enum<?>, ?> value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
      typeSer.writeTypePrefixForObject(value, jgen);
      if (!value.isEmpty()) {
         this.serializeContents(value, jgen, provider);
      }

      typeSer.writeTypeSuffixForObject(value, jgen);
   }

   protected void serializeContents(EnumMap<? extends Enum<?>, ?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      if (this._valueSerializer != null) {
         this.serializeContentsUsing(value, jgen, provider, this._valueSerializer);
      } else {
         JsonSerializer<Object> prevSerializer = null;
         Class<?> prevClass = null;
         EnumValues keyEnums = this._keyEnums;
         Iterator i$ = value.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<? extends Enum<?>, ?> entry = (Entry)i$.next();
            Enum<?> key = (Enum)entry.getKey();
            if (keyEnums == null) {
               SerializerBase<?> ser = (SerializerBase)provider.findValueSerializer(key.getDeclaringClass(), this._property);
               keyEnums = ((EnumSerializer)ser).getEnumValues();
            }

            jgen.writeFieldName(keyEnums.serializedValueFor(key));
            Object valueElem = entry.getValue();
            if (valueElem == null) {
               provider.defaultSerializeNull(jgen);
            } else {
               Class<?> cc = valueElem.getClass();
               JsonSerializer currSerializer;
               if (cc == prevClass) {
                  currSerializer = prevSerializer;
               } else {
                  currSerializer = provider.findValueSerializer(cc, this._property);
                  prevSerializer = currSerializer;
                  prevClass = cc;
               }

               try {
                  currSerializer.serialize(valueElem, jgen, provider);
               } catch (Exception var14) {
                  this.wrapAndThrow(provider, var14, value, ((Enum)entry.getKey()).name());
               }
            }
         }

      }
   }

   protected void serializeContentsUsing(EnumMap<? extends Enum<?>, ?> value, JsonGenerator jgen, SerializerProvider provider, JsonSerializer<Object> valueSer) throws IOException, JsonGenerationException {
      EnumValues keyEnums = this._keyEnums;
      Iterator i$ = value.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<? extends Enum<?>, ?> entry = (Entry)i$.next();
         Enum<?> key = (Enum)entry.getKey();
         if (keyEnums == null) {
            SerializerBase<?> ser = (SerializerBase)provider.findValueSerializer(key.getDeclaringClass(), this._property);
            keyEnums = ((EnumSerializer)ser).getEnumValues();
         }

         jgen.writeFieldName(keyEnums.serializedValueFor(key));
         Object valueElem = entry.getValue();
         if (valueElem == null) {
            provider.defaultSerializeNull(jgen);
         } else {
            try {
               valueSer.serialize(valueElem, jgen, provider);
            } catch (Exception var11) {
               this.wrapAndThrow(provider, var11, value, ((Enum)entry.getKey()).name());
            }
         }
      }

   }

   public void resolve(SerializerProvider provider) throws JsonMappingException {
      if (this._staticTyping && this._valueSerializer == null) {
         this._valueSerializer = provider.findValueSerializer(this._valueType, this._property);
      }

   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      ObjectNode o = this.createSchemaNode("object", true);
      if (typeHint instanceof ParameterizedType) {
         Type[] typeArgs = ((ParameterizedType)typeHint).getActualTypeArguments();
         if (typeArgs.length == 2) {
            JavaType enumType = provider.constructType(typeArgs[0]);
            JavaType valueType = provider.constructType(typeArgs[1]);
            ObjectNode propsNode = JsonNodeFactory.instance.objectNode();
            Class<Enum<?>> enumClass = enumType.getRawClass();
            Enum[] arr$ = (Enum[])enumClass.getEnumConstants();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Enum<?> enumValue = arr$[i$];
               JsonSerializer<Object> ser = provider.findValueSerializer(valueType.getRawClass(), this._property);
               JsonNode schemaNode = ser instanceof SchemaAware ? ((SchemaAware)ser).getSchema(provider, (Type)null) : JsonSchema.getDefaultSchemaNode();
               propsNode.put(provider.getConfig().getAnnotationIntrospector().findEnumValue(enumValue), schemaNode);
            }

            o.put("properties", (JsonNode)propsNode);
         }
      }

      return o;
   }
}
