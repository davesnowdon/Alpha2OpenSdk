package org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonAutoDetect {
   JsonMethod[] value() default {JsonMethod.ALL};

   JsonAutoDetect.Visibility getterVisibility() default JsonAutoDetect.Visibility.DEFAULT;

   JsonAutoDetect.Visibility isGetterVisibility() default JsonAutoDetect.Visibility.DEFAULT;

   JsonAutoDetect.Visibility setterVisibility() default JsonAutoDetect.Visibility.DEFAULT;

   JsonAutoDetect.Visibility creatorVisibility() default JsonAutoDetect.Visibility.DEFAULT;

   JsonAutoDetect.Visibility fieldVisibility() default JsonAutoDetect.Visibility.DEFAULT;

   public static enum Visibility {
      ANY,
      NON_PRIVATE,
      PROTECTED_AND_PUBLIC,
      PUBLIC_ONLY,
      NONE,
      DEFAULT;

      private Visibility() {
      }

      public boolean isVisible(Member m) {
         switch(this) {
         case ANY:
            return true;
         case NONE:
            return false;
         case NON_PRIVATE:
            return !Modifier.isPrivate(m.getModifiers());
         case PROTECTED_AND_PUBLIC:
            if (Modifier.isProtected(m.getModifiers())) {
               return true;
            }
         case PUBLIC_ONLY:
            return Modifier.isPublic(m.getModifiers());
         default:
            return false;
         }
      }
   }
}
