package org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ResolvableSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.type.JavaType;

public class BeanSerializer extends SerializerBase<Object> implements ResolvableSerializer, SchemaAware {
   protected static final BeanPropertyWriter[] NO_PROPS = new BeanPropertyWriter[0];
   protected final BeanPropertyWriter[] _props;
   protected final BeanPropertyWriter[] _filteredProps;
   protected final AnyGetterWriter _anyGetterWriter;
   protected final Object _propertyFilterId;

   public BeanSerializer(JavaType type, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties, AnyGetterWriter anyGetterWriter, Object filterId) {
      super(type);
      this._props = properties;
      this._filteredProps = filteredProperties;
      this._anyGetterWriter = anyGetterWriter;
      this._propertyFilterId = filterId;
   }

   public BeanSerializer(Class<?> rawType, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties, AnyGetterWriter anyGetterWriter, Object filterId) {
      super(rawType);
      this._props = properties;
      this._filteredProps = filteredProperties;
      this._anyGetterWriter = anyGetterWriter;
      this._propertyFilterId = filterId;
   }

   protected BeanSerializer(BeanSerializer src) {
      this(src._handledType, src._props, src._filteredProps, src._anyGetterWriter, src._propertyFilterId);
   }

   public static BeanSerializer createDummy(Class<?> forType) {
      return new BeanSerializer(forType, NO_PROPS, (BeanPropertyWriter[])null, (AnyGetterWriter)null, (Object)null);
   }

   public final void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      jgen.writeStartObject();
      if (this._propertyFilterId != null) {
         this.serializeFieldsFiltered(bean, jgen, provider);
      } else {
         this.serializeFields(bean, jgen, provider);
      }

      jgen.writeEndObject();
   }

   public void serializeWithType(Object bean, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
      typeSer.writeTypePrefixForObject(bean, jgen);
      if (this._propertyFilterId != null) {
         this.serializeFieldsFiltered(bean, jgen, provider);
      } else {
         this.serializeFields(bean, jgen, provider);
      }

      typeSer.writeTypeSuffixForObject(bean, jgen);
   }

   protected void serializeFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      BeanPropertyWriter[] props;
      if (this._filteredProps != null && provider.getSerializationView() != null) {
         props = this._filteredProps;
      } else {
         props = this._props;
      }

      int i = 0;

      try {
         for(int len = props.length; i < len; ++i) {
            BeanPropertyWriter prop = props[i];
            if (prop != null) {
               prop.serializeAsField(bean, jgen, provider);
            }
         }

         if (this._anyGetterWriter != null) {
            this._anyGetterWriter.getAndSerialize(bean, jgen, provider);
         }
      } catch (Exception var9) {
         String name = i == props.length ? "[anySetter]" : props[i].getName();
         this.wrapAndThrow(provider, var9, bean, name);
      } catch (StackOverflowError var10) {
         JsonMappingException mapE = new JsonMappingException("Infinite recursion (StackOverflowError)");
         String name = i == props.length ? "[anySetter]" : props[i].getName();
         mapE.prependPath(new JsonMappingException.Reference(bean, name));
         throw mapE;
      }

   }

   protected void serializeFieldsFiltered(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      BeanPropertyWriter[] props;
      if (this._filteredProps != null && provider.getSerializationView() != null) {
         props = this._filteredProps;
      } else {
         props = this._props;
      }

      BeanPropertyFilter filter = this.findFilter(provider);
      int i = 0;

      try {
         for(int len = props.length; i < len; ++i) {
            BeanPropertyWriter prop = props[i];
            if (prop != null) {
               filter.serializeAsField(bean, jgen, provider, prop);
            }
         }

         if (this._anyGetterWriter != null) {
            this._anyGetterWriter.getAndSerialize(bean, jgen, provider);
         }
      } catch (Exception var10) {
         String name = i == props.length ? "[anySetter]" : props[i].getName();
         this.wrapAndThrow(provider, var10, bean, name);
      } catch (StackOverflowError var11) {
         JsonMappingException mapE = new JsonMappingException("Infinite recursion (StackOverflowError)");
         String name = i == props.length ? "[anySetter]" : props[i].getName();
         mapE.prependPath(new JsonMappingException.Reference(bean, name));
         throw mapE;
      }

   }

   protected BeanPropertyFilter findFilter(SerializerProvider provider) throws JsonMappingException {
      Object filterId = this._propertyFilterId;
      FilterProvider filters = provider.getFilterProvider();
      if (filters == null) {
         throw new JsonMappingException("Can not resolve BeanPropertyFilter with id '" + filterId + "'; no FilterProvider configured");
      } else {
         BeanPropertyFilter filter = filters.findFilter(filterId);
         if (filter == null) {
            throw new JsonMappingException("No filter configured with id '" + filterId + "' (type " + filterId.getClass().getName() + ")");
         } else {
            return filter;
         }
      }
   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      ObjectNode o = this.createSchemaNode("object", true);
      ObjectNode propertiesNode = o.objectNode();

      for(int i = 0; i < this._props.length; ++i) {
         BeanPropertyWriter prop = this._props[i];
         JavaType propType = prop.getSerializationType();
         Type hint = propType == null ? prop.getGenericPropertyType() : propType.getRawClass();
         JsonSerializer<Object> ser = prop.getSerializer();
         if (ser == null) {
            Class<?> serType = prop.getRawSerializationType();
            if (serType == null) {
               serType = prop.getPropertyType();
            }

            ser = provider.findValueSerializer((Class)serType, prop);
         }

         JsonNode schemaNode = ser instanceof SchemaAware ? ((SchemaAware)ser).getSchema(provider, (Type)hint) : JsonSchema.getDefaultSchemaNode();
         propertiesNode.put(prop.getName(), schemaNode);
      }

      o.put("properties", (JsonNode)propertiesNode);
      return o;
   }

   public void resolve(SerializerProvider provider) throws JsonMappingException {
      int filteredCount = this._filteredProps == null ? 0 : this._filteredProps.length;
      int i = 0;

      for(int len = this._props.length; i < len; ++i) {
         BeanPropertyWriter prop = this._props[i];
         if (!prop.hasSerializer()) {
            JavaType type = prop.getSerializationType();
            if (type == null) {
               type = provider.constructType(prop.getGenericPropertyType());
               if (!type.isFinal()) {
                  if (type.isContainerType() || type.containedTypeCount() > 0) {
                     prop.setNonTrivialBaseType(type);
                  }
                  continue;
               }
            }

            JsonSerializer<Object> ser = provider.findValueSerializer((JavaType)type, prop);
            if (type.isContainerType()) {
               TypeSerializer typeSer = (TypeSerializer)type.getContentType().getTypeHandler();
               if (typeSer != null && ser instanceof ContainerSerializerBase) {
                  JsonSerializer<Object> ser2 = ((ContainerSerializerBase)ser).withValueTypeSerializer(typeSer);
                  ser = ser2;
               }
            }

            prop = prop.withSerializer((JsonSerializer)ser);
            this._props[i] = prop;
            if (i < filteredCount) {
               BeanPropertyWriter w2 = this._filteredProps[i];
               if (w2 != null) {
                  this._filteredProps[i] = w2.withSerializer((JsonSerializer)ser);
               }
            }
         }
      }

      if (this._anyGetterWriter != null) {
         this._anyGetterWriter.resolve(provider);
      }

   }

   public String toString() {
      return "BeanSerializer for " + this.handledType().getName();
   }
}
