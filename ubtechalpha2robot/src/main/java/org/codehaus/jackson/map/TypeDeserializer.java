package org.codehaus.jackson.map;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;

public abstract class TypeDeserializer {
   public TypeDeserializer() {
   }

   public abstract JsonTypeInfo.As getTypeInclusion();

   public abstract String getPropertyName();

   public abstract TypeIdResolver getTypeIdResolver();

   public abstract Object deserializeTypedFromObject(JsonParser var1, DeserializationContext var2) throws IOException, JsonProcessingException;

   public abstract Object deserializeTypedFromArray(JsonParser var1, DeserializationContext var2) throws IOException, JsonProcessingException;

   public abstract Object deserializeTypedFromScalar(JsonParser var1, DeserializationContext var2) throws IOException, JsonProcessingException;

   public abstract Object deserializeTypedFromAny(JsonParser var1, DeserializationContext var2) throws IOException, JsonProcessingException;
}
