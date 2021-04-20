package org.codehaus.jackson.map;

public interface ContextualKeyDeserializer {
   KeyDeserializer createContextual(DeserializationConfig var1, BeanProperty var2) throws JsonMappingException;
}
