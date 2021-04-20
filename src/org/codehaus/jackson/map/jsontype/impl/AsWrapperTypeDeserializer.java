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

public class AsWrapperTypeDeserializer extends TypeDeserializerBase {
   public AsWrapperTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property) {
      super(bt, idRes, property);
   }

   public JsonTypeInfo.As getTypeInclusion() {
      return JsonTypeInfo.As.WRAPPER_OBJECT;
   }

   public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return this._deserialize(jp, ctxt);
   }

   public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return this._deserialize(jp, ctxt);
   }

   public Object deserializeTypedFromScalar(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return this._deserialize(jp, ctxt);
   }

   public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return this._deserialize(jp, ctxt);
   }

   private final Object _deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
         throw ctxt.wrongTokenException(jp, JsonToken.START_OBJECT, "need JSON Object to contain As.WRAPPER_OBJECT type information for class " + this.baseTypeName());
      } else if (jp.nextToken() != JsonToken.FIELD_NAME) {
         throw ctxt.wrongTokenException(jp, JsonToken.FIELD_NAME, "need JSON String that contains type id (for subtype of " + this.baseTypeName() + ")");
      } else {
         JsonDeserializer<Object> deser = this._findDeserializer(ctxt, jp.getText());
         jp.nextToken();
         Object value = deser.deserialize(jp, ctxt);
         if (jp.nextToken() != JsonToken.END_OBJECT) {
            throw ctxt.wrongTokenException(jp, JsonToken.END_OBJECT, "expected closing END_OBJECT after type information and deserialized value");
         } else {
            return value;
         }
      }
   }
}
