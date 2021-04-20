package org.codehaus.jackson.map;

import org.codehaus.jackson.type.JavaType;

public interface KeyDeserializers {
   KeyDeserializer findKeyDeserializer(JavaType var1, DeserializationConfig var2, BeanDescription var3, BeanProperty var4) throws JsonMappingException;
}
