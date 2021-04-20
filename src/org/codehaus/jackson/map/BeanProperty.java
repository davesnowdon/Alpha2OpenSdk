package org.codehaus.jackson.map;

import java.lang.annotation.Annotation;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.type.JavaType;

public interface BeanProperty {
   String getName();

   JavaType getType();

   <A extends Annotation> A getAnnotation(Class<A> var1);

   <A extends Annotation> A getContextAnnotation(Class<A> var1);

   AnnotatedMember getMember();

   public static class Std implements BeanProperty {
      protected final String _name;
      protected final JavaType _type;
      protected final AnnotatedMember _member;
      protected final Annotations _contextAnnotations;

      public Std(String name, JavaType type, Annotations contextAnnotations, AnnotatedMember member) {
         this._name = name;
         this._type = type;
         this._member = member;
         this._contextAnnotations = contextAnnotations;
      }

      public BeanProperty.Std withType(JavaType type) {
         return new BeanProperty.Std(this._name, type, this._contextAnnotations, this._member);
      }

      public <A extends Annotation> A getAnnotation(Class<A> acls) {
         return this._member.getAnnotation(acls);
      }

      public <A extends Annotation> A getContextAnnotation(Class<A> acls) {
         return this._contextAnnotations == null ? null : this._contextAnnotations.get(acls);
      }

      public String getName() {
         return this._name;
      }

      public JavaType getType() {
         return this._type;
      }

      public AnnotatedMember getMember() {
         return this._member;
      }
   }
}
