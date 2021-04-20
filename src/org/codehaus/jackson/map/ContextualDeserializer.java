package org.codehaus.jackson.map;

public interface ContextualDeserializer<T> {
   JsonDeserializer<T> createContextual(DeserializationConfig var1, BeanProperty var2) throws JsonMappingException;
}
