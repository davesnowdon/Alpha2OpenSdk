package org.codehaus.jackson.xc;

import java.io.IOException;
import java.lang.reflect.Type;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

public class XmlAdapterJsonDeserializer extends StdDeserializer<Object> {
   protected static final JavaType ADAPTER_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(XmlAdapter.class);
   protected final BeanProperty _property;
   protected final XmlAdapter<Object, Object> _xmlAdapter;
   protected final JavaType _valueType;
   protected JsonDeserializer<?> _deserializer;

   public XmlAdapterJsonDeserializer(XmlAdapter<Object, Object> xmlAdapter, BeanProperty property) {
      super(Object.class);
      this._property = property;
      this._xmlAdapter = xmlAdapter;
      TypeFactory typeFactory = TypeFactory.defaultInstance();
      JavaType type = typeFactory.constructType((Type)xmlAdapter.getClass());
      JavaType[] rawTypes = typeFactory.findTypeParameters(type, XmlAdapter.class);
      this._valueType = rawTypes != null && rawTypes.length != 0 ? rawTypes[0] : TypeFactory.unknownType();
   }

   public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonDeserializer<?> deser = this._deserializer;
      if (deser == null) {
         DeserializationConfig config = ctxt.getConfig();
         this._deserializer = deser = ctxt.getDeserializerProvider().findValueDeserializer(config, this._valueType, this._property);
      }

      Object boundObject = deser.deserialize(jp, ctxt);

      try {
         return this._xmlAdapter.unmarshal(boundObject);
      } catch (Exception var6) {
         throw new JsonMappingException("Unable to unmarshal (to type " + this._valueType + "): " + var6.getMessage(), var6);
      }
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
   }
}
