package org.codehaus.jackson.map.ser.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
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
import org.codehaus.jackson.map.ser.ArraySerializers;
import org.codehaus.jackson.map.ser.ContainerSerializerBase;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.type.JavaType;

@JacksonStdImpl
public class ObjectArraySerializer extends ArraySerializers.AsArraySerializer<Object[]> implements ResolvableSerializer {
   protected final boolean _staticTyping;
   protected final JavaType _elementType;
   protected JsonSerializer<Object> _elementSerializer;
   protected PropertySerializerMap _dynamicSerializers;

   /** @deprecated */
   @Deprecated
   public ObjectArraySerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property) {
      this(elemType, staticTyping, vts, property, (JsonSerializer)null);
   }

   public ObjectArraySerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property, JsonSerializer<Object> elementSerializer) {
      super(Object[].class, vts, property);
      this._elementType = elemType;
      this._staticTyping = staticTyping;
      this._dynamicSerializers = PropertySerializerMap.emptyMap();
      this._elementSerializer = elementSerializer;
   }

   public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
      return new ObjectArraySerializer(this._elementType, this._staticTyping, vts, this._property, this._elementSerializer);
   }

   public void serializeContents(Object[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      int len = value.length;
      if (len != 0) {
         if (this._elementSerializer != null) {
            this.serializeContentsUsing(value, jgen, provider, this._elementSerializer);
         } else if (this._valueTypeSerializer != null) {
            this.serializeTypedContents(value, jgen, provider);
         } else {
            int i = 0;
            Object elem = null;

            try {
               for(PropertySerializerMap serializers = this._dynamicSerializers; i < len; ++i) {
                  elem = value[i];
                  if (elem == null) {
                     provider.defaultSerializeNull(jgen);
                  } else {
                     Class<?> cc = elem.getClass();
                     JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                     if (serializer == null) {
                        if (this._elementType.hasGenericTypes()) {
                           serializer = this._findAndAddDynamic(serializers, this._elementType.forcedNarrowBy(cc), provider);
                        } else {
                           serializer = this._findAndAddDynamic(serializers, cc, provider);
                        }
                     }

                     serializer.serialize(elem, jgen, provider);
                  }
               }

            } catch (IOException var10) {
               throw var10;
            } catch (Exception var11) {
               Object t;
               for(t = var11; t instanceof InvocationTargetException && ((Throwable)t).getCause() != null; t = ((Throwable)t).getCause()) {
               }

               if (t instanceof Error) {
                  throw (Error)t;
               } else {
                  throw JsonMappingException.wrapWithPath((Throwable)t, elem, i);
               }
            }
         }
      }
   }

   public void serializeContentsUsing(Object[] value, JsonGenerator jgen, SerializerProvider provider, JsonSerializer<Object> ser) throws IOException, JsonGenerationException {
      int len = value.length;
      TypeSerializer typeSer = this._valueTypeSerializer;
      int i = 0;
      Object elem = null;

      try {
         for(; i < len; ++i) {
            elem = value[i];
            if (elem == null) {
               provider.defaultSerializeNull(jgen);
            } else if (typeSer == null) {
               ser.serialize(elem, jgen, provider);
            } else {
               ser.serializeWithType(elem, jgen, provider, typeSer);
            }
         }

      } catch (IOException var11) {
         throw var11;
      } catch (Exception var12) {
         Object t;
         for(t = var12; t instanceof InvocationTargetException && ((Throwable)t).getCause() != null; t = ((Throwable)t).getCause()) {
         }

         if (t instanceof Error) {
            throw (Error)t;
         } else {
            throw JsonMappingException.wrapWithPath((Throwable)t, elem, i);
         }
      }
   }

   public void serializeTypedContents(Object[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      int len = value.length;
      TypeSerializer typeSer = this._valueTypeSerializer;
      int i = 0;
      Object elem = null;

      try {
         for(PropertySerializerMap serializers = this._dynamicSerializers; i < len; ++i) {
            elem = value[i];
            if (elem == null) {
               provider.defaultSerializeNull(jgen);
            } else {
               Class<?> cc = elem.getClass();
               JsonSerializer<Object> serializer = serializers.serializerFor(cc);
               if (serializer == null) {
                  serializer = this._findAndAddDynamic(serializers, cc, provider);
               }

               serializer.serializeWithType(elem, jgen, provider, typeSer);
            }
         }

      } catch (IOException var11) {
         throw var11;
      } catch (Exception var12) {
         Object t;
         for(t = var12; t instanceof InvocationTargetException && ((Throwable)t).getCause() != null; t = ((Throwable)t).getCause()) {
         }

         if (t instanceof Error) {
            throw (Error)t;
         } else {
            throw JsonMappingException.wrapWithPath((Throwable)t, elem, i);
         }
      }
   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      ObjectNode o = this.createSchemaNode("array", true);
      if (typeHint != null) {
         JavaType javaType = provider.constructType(typeHint);
         if (javaType.isArrayType()) {
            Class<?> componentType = ((ArrayType)javaType).getContentType().getRawClass();
            if (componentType == Object.class) {
               o.put("items", JsonSchema.getDefaultSchemaNode());
            } else {
               JsonSerializer<Object> ser = provider.findValueSerializer(componentType, this._property);
               JsonNode schemaNode = ser instanceof SchemaAware ? ((SchemaAware)ser).getSchema(provider, (Type)null) : JsonSchema.getDefaultSchemaNode();
               o.put("items", schemaNode);
            }
         }
      }

      return o;
   }

   public void resolve(SerializerProvider provider) throws JsonMappingException {
      if (this._staticTyping && this._elementSerializer == null) {
         this._elementSerializer = provider.findValueSerializer(this._elementType, this._property);
      }

   }

   protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map, Class<?> type, SerializerProvider provider) throws JsonMappingException {
      PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, this._property);
      if (map != result.map) {
         this._dynamicSerializers = result.map;
      }

      return result.serializer;
   }

   protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map, JavaType type, SerializerProvider provider) throws JsonMappingException {
      PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, this._property);
      if (map != result.map) {
         this._dynamicSerializers = result.map;
      }

      return result.serializer;
   }
}
