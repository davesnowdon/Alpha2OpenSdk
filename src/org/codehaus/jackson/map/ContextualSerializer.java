package org.codehaus.jackson.map;

public interface ContextualSerializer<T> {
   JsonSerializer<T> createContextual(SerializationConfig var1, BeanProperty var2) throws JsonMappingException;
}
