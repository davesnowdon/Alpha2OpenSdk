package org.codehaus.jackson.xc;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.SerializerBase;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.schema.SchemaAware;

public class XmlAdapterJsonSerializer extends SerializerBase<Object> implements SchemaAware {
   private final XmlAdapter<Object, Object> xmlAdapter;
   private final BeanProperty _property;

   public XmlAdapterJsonSerializer(XmlAdapter<Object, Object> xmlAdapter, BeanProperty property) {
      super(Object.class);
      this.xmlAdapter = xmlAdapter;
      this._property = property;
   }

   public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      Object adapted;
      try {
         adapted = this.xmlAdapter.marshal(value);
      } catch (Exception var6) {
         throw new JsonMappingException("Unable to marshal: " + var6.getMessage(), var6);
      }

      if (adapted == null) {
         provider.getNullValueSerializer().serialize((Object)null, jgen, provider);
      } else {
         Class<?> c = adapted.getClass();
         provider.findTypedValueSerializer(c, true, this._property).serialize(adapted, jgen, provider);
      }

   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      JsonSerializer<Object> ser = provider.findValueSerializer(this.findValueClass(), this._property);
      JsonNode schemaNode = ser instanceof SchemaAware ? ((SchemaAware)ser).getSchema(provider, (Type)null) : JsonSchema.getDefaultSchemaNode();
      return schemaNode;
   }

   private Class<?> findValueClass() {
      Type superClass;
      for(superClass = this.xmlAdapter.getClass().getGenericSuperclass(); superClass instanceof ParameterizedType && XmlAdapter.class != ((ParameterizedType)superClass).getRawType(); superClass = ((Class)((ParameterizedType)superClass).getRawType()).getGenericSuperclass()) {
      }

      return (Class)((ParameterizedType)superClass).getActualTypeArguments()[0];
   }
}
