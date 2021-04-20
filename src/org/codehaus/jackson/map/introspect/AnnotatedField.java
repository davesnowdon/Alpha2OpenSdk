package org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

public final class AnnotatedField extends AnnotatedMember {
   protected final Field _field;
   protected final AnnotationMap _annotations;

   public AnnotatedField(Field field, AnnotationMap annMap) {
      this._field = field;
      this._annotations = annMap;
   }

   public void addOrOverride(Annotation a) {
      this._annotations.add(a);
   }

   public Field getAnnotated() {
      return this._field;
   }

   public int getModifiers() {
      return this._field.getModifiers();
   }

   public String getName() {
      return this._field.getName();
   }

   public <A extends Annotation> A getAnnotation(Class<A> acls) {
      return this._annotations.get(acls);
   }

   public Type getGenericType() {
      return this._field.getGenericType();
   }

   public Class<?> getRawType() {
      return this._field.getType();
   }

   public Class<?> getDeclaringClass() {
      return this._field.getDeclaringClass();
   }

   public Member getMember() {
      return this._field;
   }

   public String getFullName() {
      return this.getDeclaringClass().getName() + "#" + this.getName();
   }

   public int getAnnotationCount() {
      return this._annotations.size();
   }

   public String toString() {
      return "[field " + this.getName() + ", annotations: " + this._annotations + "]";
   }
}
