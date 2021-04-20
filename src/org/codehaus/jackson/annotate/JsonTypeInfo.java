package org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonTypeInfo {
   JsonTypeInfo.Id use();

   JsonTypeInfo.As include() default JsonTypeInfo.As.PROPERTY;

   String property() default "";

   public static enum As {
      PROPERTY,
      WRAPPER_OBJECT,
      WRAPPER_ARRAY;

      private As() {
      }
   }

   public static enum Id {
      NONE((String)null),
      CLASS("@class"),
      MINIMAL_CLASS("@c"),
      NAME("@type"),
      CUSTOM((String)null);

      private final String _defaultPropertyName;

      private Id(String defProp) {
         this._defaultPropertyName = defProp;
      }

      public String getDefaultPropertyName() {
         return this._defaultPropertyName;
      }
   }
}
