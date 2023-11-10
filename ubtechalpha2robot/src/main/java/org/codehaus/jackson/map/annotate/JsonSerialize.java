package org.codehaus.jackson.map.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.codehaus.jackson.annotate.JacksonAnnotation;
import org.codehaus.jackson.map.JsonSerializer;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonSerialize {
   Class<? extends JsonSerializer<?>> using() default JsonSerializer.None.class;

   Class<? extends JsonSerializer<?>> contentUsing() default JsonSerializer.None.class;

   Class<? extends JsonSerializer<?>> keyUsing() default JsonSerializer.None.class;

   Class<?> as() default NoClass.class;

   Class<?> keyAs() default NoClass.class;

   Class<?> contentAs() default NoClass.class;

   JsonSerialize.Typing typing() default JsonSerialize.Typing.DYNAMIC;

   JsonSerialize.Inclusion include() default JsonSerialize.Inclusion.ALWAYS;

   public static enum Typing {
      DYNAMIC,
      STATIC;

      private Typing() {
      }
   }

   public static enum Inclusion {
      ALWAYS,
      NON_NULL,
      NON_DEFAULT;

      private Inclusion() {
      }
   }
}
