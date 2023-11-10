package org.codehaus.jackson.map;

import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;

public abstract class PropertyNamingStrategy {
   public PropertyNamingStrategy() {
   }

   public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
      return defaultName;
   }

   public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
      return defaultName;
   }

   public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
      return defaultName;
   }
}
