package org.codehaus.jackson.map;

import java.io.IOException;
import org.codehaus.jackson.JsonProcessingException;

public abstract class KeyDeserializer {
   public KeyDeserializer() {
   }

   public abstract Object deserializeKey(String var1, DeserializationContext var2) throws IOException, JsonProcessingException;

   public abstract static class None extends KeyDeserializer {
      public None() {
      }
   }
}
