package org.codehaus.jackson.map;

public interface ResolvableSerializer {
   void resolve(SerializerProvider var1) throws JsonMappingException;
}
