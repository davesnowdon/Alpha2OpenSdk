package org.codehaus.jackson.map.deser;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.type.JavaType;

public class AbstractDeserializer extends JsonDeserializer<Object> {
   protected final JavaType _baseType;

   public AbstractDeserializer(JavaType bt) {
      this._baseType = bt;
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      switch(jp.getCurrentToken()) {
      case VALUE_STRING:
         return jp.getText();
      case VALUE_NUMBER_INT:
         if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
            return jp.getBigIntegerValue();
         }

         return jp.getIntValue();
      case VALUE_NUMBER_FLOAT:
         if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
            return jp.getDecimalValue();
         }

         return jp.getDoubleValue();
      case VALUE_TRUE:
         return Boolean.TRUE;
      case VALUE_FALSE:
         return Boolean.FALSE;
      case VALUE_EMBEDDED_OBJECT:
         return jp.getEmbeddedObject();
      case VALUE_NULL:
         return null;
      case START_ARRAY:
         return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
      default:
         return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
      }
   }

   public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      throw ctxt.instantiationException(this._baseType.getRawClass(), "abstract types can only be instantiated with additional type information");
   }
}
