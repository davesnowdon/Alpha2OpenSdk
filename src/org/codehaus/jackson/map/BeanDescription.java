package org.codehaus.jackson.map;

import java.util.Collection;
import java.util.LinkedHashMap;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.type.JavaType;

public abstract class BeanDescription {
   protected final JavaType _type;

   protected BeanDescription(JavaType type) {
      this._type = type;
   }

   public JavaType getType() {
      return this._type;
   }

   public Class<?> getBeanClass() {
      return this._type.getRawClass();
   }

   public abstract boolean hasKnownClassAnnotations();

   public abstract TypeBindings bindingsForBeanType();

   public abstract Annotations getClassAnnotations();

   public abstract LinkedHashMap<String, AnnotatedMethod> findGetters(VisibilityChecker<?> var1, Collection<String> var2);

   public abstract LinkedHashMap<String, AnnotatedMethod> findSetters(VisibilityChecker<?> var1);
}
