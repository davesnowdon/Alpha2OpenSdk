package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.type.JavaType;

public abstract class ContainerDeserializer extends StdDeserializer {
   protected ContainerDeserializer(Class selfType) {
      super(selfType);
   }

   public abstract JavaType getContentType();

   public abstract JsonDeserializer getContentDeserializer();
}
