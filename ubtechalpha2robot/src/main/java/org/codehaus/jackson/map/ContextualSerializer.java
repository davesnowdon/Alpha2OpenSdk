package org.codehaus.jackson.map;

public interface ContextualSerializer {
   JsonSerializer createContextual(SerializationConfig var1, BeanProperty var2) throws JsonMappingException;
}
