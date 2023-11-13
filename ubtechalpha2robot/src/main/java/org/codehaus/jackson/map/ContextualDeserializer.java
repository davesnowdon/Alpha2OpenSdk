package org.codehaus.jackson.map;

public interface ContextualDeserializer {
   JsonDeserializer createContextual(DeserializationConfig var1, BeanProperty var2) throws JsonMappingException;
}
