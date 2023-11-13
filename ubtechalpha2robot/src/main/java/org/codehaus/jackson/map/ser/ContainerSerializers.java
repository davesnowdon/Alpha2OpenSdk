package org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
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
import org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.type.JavaType;

public final class ContainerSerializers {
   private ContainerSerializers() {
   }

   public static ContainerSerializerBase<?> indexedListSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property, JsonSerializer<Object> valueSerializer) {
      return new ContainerSerializers.IndexedListSerializer(elemType, staticTyping, vts, property, valueSerializer);
   }

   public static ContainerSerializerBase<?> collectionSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property, JsonSerializer<Object> valueSerializer) {
      return new ContainerSerializers.CollectionSerializer(elemType, staticTyping, vts, property, valueSerializer);
   }

   public static ContainerSerializerBase<?> iteratorSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property) {
      return new ContainerSerializers.IteratorSerializer(elemType, staticTyping, vts, property);
   }

   public static ContainerSerializerBase<?> iterableSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property) {
      return new ContainerSerializers.IterableSerializer(elemType, staticTyping, vts, property);
   }

   public static JsonSerializer<?> enumSetSerializer(JavaType enumType, BeanProperty property) {
      return new ContainerSerializers.EnumSetSerializer(enumType, property);
   }

   public static class EnumSetSerializer extends ContainerSerializers.AsArraySerializer<EnumSet<? extends Enum<?>>> {
      public EnumSetSerializer(JavaType elemType, BeanProperty property) {
         super(EnumSet.class, elemType, true, (TypeSerializer)null, property);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return this;
      }

      public void serializeContents(EnumSet<? extends Enum<?>> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         JsonSerializer<Object> enumSer = this._elementSerializer;

         Enum en;
         for(Iterator i$ = value.iterator(); i$.hasNext(); enumSer.serialize(en, jgen, provider)) {
            en = (Enum)i$.next();
            if (enumSer == null) {
               enumSer = provider.findValueSerializer(en.getDeclaringClass(), this._property);
            }
         }

      }
   }

   @JacksonStdImpl
   public static class IterableSerializer extends ContainerSerializers.AsArraySerializer<Iterable<?>> {
      public IterableSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property) {
         super(Iterable.class, elemType, staticTyping, vts, property);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return new ContainerSerializers.IterableSerializer(this._elementType, this._staticTyping, vts, this._property);
      }

      public void serializeContents(Iterable<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         Iterator<?> it = value.iterator();
         if (it.hasNext()) {
            TypeSerializer typeSer = this._valueTypeSerializer;
            JsonSerializer<Object> prevSerializer = null;
            Class prevClass = null;

            do {
               Object elem = it.next();
               if (elem == null) {
                  provider.defaultSerializeNull(jgen);
               } else {
                  Class<?> cc = elem.getClass();
                  JsonSerializer currSerializer;
                  if (cc == prevClass) {
                     currSerializer = prevSerializer;
                  } else {
                     currSerializer = provider.findValueSerializer(cc, this._property);
                     prevSerializer = currSerializer;
                     prevClass = cc;
                  }

                  if (typeSer == null) {
                     currSerializer.serialize(elem, jgen, provider);
                  } else {
                     currSerializer.serializeWithType(elem, jgen, provider, typeSer);
                  }
               }
            } while(it.hasNext());
         }

      }
   }

   @JacksonStdImpl
   public static class IteratorSerializer extends ContainerSerializers.AsArraySerializer<Iterator<?>> {
      public IteratorSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property) {
         super(Iterator.class, elemType, staticTyping, vts, property);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return new ContainerSerializers.IteratorSerializer(this._elementType, this._staticTyping, vts, this._property);
      }

      public void serializeContents(Iterator<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         if (value.hasNext()) {
            TypeSerializer typeSer = this._valueTypeSerializer;
            JsonSerializer<Object> prevSerializer = null;
            Class prevClass = null;

            do {
               Object elem = value.next();
               if (elem == null) {
                  provider.defaultSerializeNull(jgen);
               } else {
                  Class<?> cc = elem.getClass();
                  JsonSerializer currSerializer;
                  if (cc == prevClass) {
                     currSerializer = prevSerializer;
                  } else {
                     currSerializer = provider.findValueSerializer(cc, this._property);
                     prevSerializer = currSerializer;
                     prevClass = cc;
                  }

                  if (typeSer == null) {
                     currSerializer.serialize(elem, jgen, provider);
                  } else {
                     currSerializer.serializeWithType(elem, jgen, provider, typeSer);
                  }
               }
            } while(value.hasNext());
         }

      }
   }

   @JacksonStdImpl
   public static class CollectionSerializer extends ContainerSerializers.AsArraySerializer<Collection<?>> {
      /** @deprecated */
      @Deprecated
      public CollectionSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property) {
         this(elemType, staticTyping, vts, property, (JsonSerializer)null);
      }

      public CollectionSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property, JsonSerializer<Object> valueSerializer) {
         super(Collection.class, elemType, staticTyping, vts, property, valueSerializer);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return new ContainerSerializers.CollectionSerializer(this._elementType, this._staticTyping, vts, this._property);
      }

      public void serializeContents(Collection<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         if (this._elementSerializer != null) {
            this.serializeContentsUsing(value, jgen, provider, this._elementSerializer);
         } else {
            Iterator<?> it = value.iterator();
            if (it.hasNext()) {
               PropertySerializerMap serializers = this._dynamicSerializers;
               TypeSerializer typeSer = this._valueTypeSerializer;
               int i = 0;

               try {
                  do {
                     Object elem = it.next();
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

                           serializers = this._dynamicSerializers;
                        }

                        if (typeSer == null) {
                           serializer.serialize(elem, jgen, provider);
                        } else {
                           serializer.serializeWithType(elem, jgen, provider, typeSer);
                        }
                     }

                     ++i;
                  } while(it.hasNext());
               } catch (Exception var11) {
                  this.wrapAndThrow(provider, var11, value, i);
               }

            }
         }
      }

      public void serializeContentsUsing(Collection<?> value, JsonGenerator jgen, SerializerProvider provider, JsonSerializer<Object> ser) throws IOException, JsonGenerationException {
         Iterator<?> it = value.iterator();
         if (it.hasNext()) {
            TypeSerializer typeSer = this._valueTypeSerializer;
            int i = 0;

            do {
               Object elem = it.next();

               try {
                  if (elem == null) {
                     provider.defaultSerializeNull(jgen);
                  } else if (typeSer == null) {
                     ser.serialize(elem, jgen, provider);
                  } else {
                     ser.serializeWithType(elem, jgen, provider, typeSer);
                  }

                  ++i;
               } catch (Exception var10) {
                  this.wrapAndThrow(provider, var10, value, i);
               }
            } while(it.hasNext());
         }

      }
   }

   @JacksonStdImpl
   public static class IndexedListSerializer extends ContainerSerializers.AsArraySerializer<List<?>> {
      public IndexedListSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property, JsonSerializer<Object> valueSerializer) {
         super(List.class, elemType, staticTyping, vts, property, valueSerializer);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return new ContainerSerializers.IndexedListSerializer(this._elementType, this._staticTyping, vts, this._property, this._elementSerializer);
      }

      public void serializeContents(List<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         if (this._elementSerializer != null) {
            this.serializeContentsUsing(value, jgen, provider, this._elementSerializer);
         } else if (this._valueTypeSerializer != null) {
            this.serializeTypedContents(value, jgen, provider);
         } else {
            int len = value.size();
            if (len != 0) {
               int i = 0;

               try {
                  for(PropertySerializerMap serializers = this._dynamicSerializers; i < len; ++i) {
                     Object elem = value.get(i);
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

                           serializers = this._dynamicSerializers;
                        }

                        serializer.serialize(elem, jgen, provider);
                     }
                  }
               } catch (Exception var10) {
                  this.wrapAndThrow(provider, var10, value, i);
               }

            }
         }
      }

      public void serializeContentsUsing(List<?> value, JsonGenerator jgen, SerializerProvider provider, JsonSerializer<Object> ser) throws IOException, JsonGenerationException {
         int len = value.size();
         if (len != 0) {
            TypeSerializer typeSer = this._valueTypeSerializer;

            for(int i = 0; i < len; ++i) {
               Object elem = value.get(i);

               try {
                  if (elem == null) {
                     provider.defaultSerializeNull(jgen);
                  } else if (typeSer == null) {
                     ser.serialize(elem, jgen, provider);
                  } else {
                     ser.serializeWithType(elem, jgen, provider, typeSer);
                  }
               } catch (Exception var10) {
                  this.wrapAndThrow(provider, var10, value, i);
               }
            }

         }
      }

      public void serializeTypedContents(List<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         int len = value.size();
         if (len != 0) {
            int i = 0;

            try {
               TypeSerializer typeSer = this._valueTypeSerializer;

               for(PropertySerializerMap serializers = this._dynamicSerializers; i < len; ++i) {
                  Object elem = value.get(i);
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

                        serializers = this._dynamicSerializers;
                     }

                     serializer.serializeWithType(elem, jgen, provider, typeSer);
                  }
               }
            } catch (Exception var11) {
               this.wrapAndThrow(provider, var11, value, i);
            }

         }
      }
   }

   public abstract static class AsArraySerializer extends ContainerSerializerBase implements ResolvableSerializer {
      protected final boolean _staticTyping;
      protected final JavaType _elementType;
      protected final TypeSerializer _valueTypeSerializer;
      protected JsonSerializer<Object> _elementSerializer;
      protected final BeanProperty _property;
      protected PropertySerializerMap _dynamicSerializers;

      /** @deprecated */
      @Deprecated
      protected AsArraySerializer(Class<?> cls, JavaType et, boolean staticTyping, TypeSerializer vts, BeanProperty property) {
         this(cls, et, staticTyping, vts, property, (JsonSerializer)null);
      }

      protected AsArraySerializer(Class<?> cls, JavaType et, boolean staticTyping, TypeSerializer vts, BeanProperty property, JsonSerializer<Object> elementSerializer) {
         super(cls, false);
         this._elementType = et;
         this._staticTyping = staticTyping || et != null && et.isFinal();
         this._valueTypeSerializer = vts;
         this._property = property;
         this._elementSerializer = elementSerializer;
         this._dynamicSerializers = PropertySerializerMap.emptyMap();
      }

      public final void serialize(T value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeStartArray();
         this.serializeContents(value, jgen, provider);
         jgen.writeEndArray();
      }

      public final void serializeWithType(T value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
         typeSer.writeTypePrefixForArray(value, jgen);
         this.serializeContents(value, jgen, provider);
         typeSer.writeTypeSuffixForArray(value, jgen);
      }

      protected abstract void serializeContents(T var1, JsonGenerator var2, SerializerProvider var3) throws IOException, JsonGenerationException;

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
         ObjectNode o = this.createSchemaNode("array", true);
         JavaType contentType = null;
         if (typeHint != null) {
            JavaType javaType = provider.constructType(typeHint);
            contentType = javaType.getContentType();
            if (contentType == null && typeHint instanceof ParameterizedType) {
               Type[] typeArgs = ((ParameterizedType)typeHint).getActualTypeArguments();
               if (typeArgs.length == 1) {
                  contentType = provider.constructType(typeArgs[0]);
               }
            }
         }

         if (contentType == null && this._elementType != null) {
            contentType = this._elementType;
         }

         if (contentType != null) {
            JsonNode schemaNode = null;
            if (contentType.getRawClass() != Object.class) {
               JsonSerializer<Object> ser = provider.findValueSerializer(contentType, this._property);
               if (ser instanceof SchemaAware) {
                  schemaNode = ((SchemaAware)ser).getSchema(provider, (Type)null);
               }
            }

            if (schemaNode == null) {
               schemaNode = JsonSchema.getDefaultSchemaNode();
            }

            o.put("items", schemaNode);
         }

         return o;
      }

      public void resolve(SerializerProvider provider) throws JsonMappingException {
         if (this._staticTyping && this._elementType != null && this._elementSerializer == null) {
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
}
