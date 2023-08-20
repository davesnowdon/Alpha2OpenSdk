package org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.type.JavaType;

public class AsArrayTypeDeserializer extends TypeDeserializerBase {
   public AsArrayTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property) {
      super(bt, idRes, property);
   }

   public JsonTypeInfo.As getTypeInclusion() {
      return JsonTypeInfo.As.WRAPPER_ARRAY;
   }

   public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return this._deserialize(jp, ctxt);
   }

   public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return this._deserialize(jp, ctxt);
   }

   public Object deserializeTypedFromScalar(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return this._deserialize(jp, ctxt);
   }

   public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return this._deserialize(jp, ctxt);
   }

   private final Object _deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonDeserializer<Object> deser = this._findDeserializer(ctxt, this._locateTypeId(jp, ctxt));
      Object value = deser.deserialize(jp, ctxt);
      if (jp.nextToken() != JsonToken.END_ARRAY) {
         throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "expected closing END_ARRAY after type information and deserialized value");
      } else {
         return value;
      }
   }

   protected final String _locateTypeId(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (!jp.isExpectedStartArrayToken()) {
         throw ctxt.wrongTokenException(jp, JsonToken.START_ARRAY, "need JSON Array to contain As.WRAPPER_ARRAY type information for class " + this.baseTypeName());
      } else if (jp.nextToken() != JsonToken.VALUE_STRING) {
         throw ctxt.wrongTokenException(jp, JsonToken.VALUE_STRING, "need JSON String that contains type id (for subtype of " + this.baseTypeName() + ")");
      } else {
         String result = jp.getText();
         jp.nextToken();
         return result;
      }
   }
}
