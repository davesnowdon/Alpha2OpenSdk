package org.codehaus.jackson.map;

import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;

public abstract class JsonSerializer {
   public JsonSerializer() {
   }

   public abstract void serialize(T var1, JsonGenerator var2, SerializerProvider var3) throws IOException, JsonProcessingException;

   public void serializeWithType(T value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonProcessingException {
      this.serialize(value, jgen, provider);
   }

   public Class handledType() {
      return null;
   }

   public abstract static class None extends JsonSerializer<Object> {
      public None() {
      }
   }
}
