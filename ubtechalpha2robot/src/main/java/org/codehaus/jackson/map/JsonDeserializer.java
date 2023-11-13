package org.codehaus.jackson.map;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;

public abstract class JsonDeserializer {
   public JsonDeserializer() {
   }

   public abstract T deserialize(JsonParser var1, DeserializationContext var2) throws IOException, JsonProcessingException;

   public T deserialize(JsonParser jp, DeserializationContext ctxt, T intoValue) throws IOException, JsonProcessingException {
      throw new UnsupportedOperationException();
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
   }

   public T getNullValue() {
      return null;
   }

   public abstract static class None extends JsonDeserializer<Object> {
      public None() {
      }
   }
}
