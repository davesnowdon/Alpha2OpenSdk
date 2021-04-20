package org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

public final class AnnotatedParameter extends AnnotatedMember {
   protected final AnnotatedMember _owner;
   protected final Type _type;
   protected final AnnotationMap _annotations;

   public AnnotatedParameter(AnnotatedMember owner, Type type, AnnotationMap ann) {
      this._owner = owner;
      this._type = type;
      this._annotations = ann;
   }

   public void addOrOverride(Annotation a) {
      this._annotations.add(a);
   }

   public AnnotatedElement getAnnotated() {
      return null;
   }

   public int getModifiers() {
      return this._owner.getModifiers();
   }

   public String getName() {
      return "";
   }

   public <A extends Annotation> A getAnnotation(Class<A> acls) {
      return this._annotations.get(acls);
   }

   public Type getGenericType() {
      return this._type;
   }

   public Class<?> getRawType() {
      if (this._type instanceof Class) {
         return (Class)this._type;
      } else {
         JavaType t = TypeFactory.defaultInstance().constructType(this._type);
         return t.getRawClass();
      }
   }

   public Class<?> getDeclaringClass() {
      return this._owner.getDeclaringClass();
   }

   public Member getMember() {
      return this._owner.getMember();
   }

   public Type getParameterType() {
      return this._type;
   }
}
