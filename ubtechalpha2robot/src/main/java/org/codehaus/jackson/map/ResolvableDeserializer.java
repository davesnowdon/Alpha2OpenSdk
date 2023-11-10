package org.codehaus.jackson.map;

public interface ResolvableDeserializer {
   void resolve(DeserializationConfig var1, DeserializerProvider var2) throws JsonMappingException;
}
