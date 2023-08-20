package org.codehaus.jackson.map;

import org.codehaus.jackson.type.JavaType;

public abstract class ClassIntrospector<T extends BeanDescription> {
   protected ClassIntrospector() {
   }

   public abstract T forSerialization(SerializationConfig var1, JavaType var2, ClassIntrospector.MixInResolver var3);

   public abstract T forDeserialization(DeserializationConfig var1, JavaType var2, ClassIntrospector.MixInResolver var3);

   public abstract T forCreation(DeserializationConfig var1, JavaType var2, ClassIntrospector.MixInResolver var3);

   public abstract T forClassAnnotations(MapperConfig<?> var1, Class<?> var2, ClassIntrospector.MixInResolver var3);

   public abstract T forDirectClassAnnotations(MapperConfig<?> var1, Class<?> var2, ClassIntrospector.MixInResolver var3);

   public interface MixInResolver {
      Class<?> findMixInClassFor(Class<?> var1);
   }
}
