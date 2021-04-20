package org.codehaus.jackson;

import java.io.IOException;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

public abstract class ObjectCodec {
   protected ObjectCodec() {
   }

   public abstract <T> T readValue(JsonParser var1, Class<T> var2) throws IOException, JsonProcessingException;

   public abstract <T> T readValue(JsonParser var1, TypeReference<?> var2) throws IOException, JsonProcessingException;

   public abstract <T> T readValue(JsonParser var1, JavaType var2) throws IOException, JsonProcessingException;

   public abstract JsonNode readTree(JsonParser var1) throws IOException, JsonProcessingException;

   public abstract void writeValue(JsonGenerator var1, Object var2) throws IOException, JsonProcessingException;

   public abstract void writeTree(JsonGenerator var1, JsonNode var2) throws IOException, JsonProcessingException;

   public abstract JsonNode createObjectNode();

   public abstract JsonNode createArrayNode();

   public abstract JsonParser treeAsTokens(JsonNode var1);

   public abstract <T> T treeToValue(JsonNode var1, Class<T> var2) throws IOException, JsonProcessingException;
}
