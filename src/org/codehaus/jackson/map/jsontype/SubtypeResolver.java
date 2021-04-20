package org.codehaus.jackson.map.jsontype;

import java.util.Collection;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedMember;

public abstract class SubtypeResolver {
   public SubtypeResolver() {
   }

   public abstract void registerSubtypes(NamedType... var1);

   public abstract void registerSubtypes(Class<?>... var1);

   public abstract Collection<NamedType> collectAndResolveSubtypes(AnnotatedMember var1, MapperConfig<?> var2, AnnotationIntrospector var3);

   public abstract Collection<NamedType> collectAndResolveSubtypes(AnnotatedClass var1, MapperConfig<?> var2, AnnotationIntrospector var3);
}
