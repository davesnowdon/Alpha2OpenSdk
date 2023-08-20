package org.codehaus.jackson.map.introspect;

import java.lang.reflect.Member;
import org.codehaus.jackson.map.util.ClassUtil;

public abstract class AnnotatedMember extends Annotated {
   protected AnnotatedMember() {
   }

   public abstract Class<?> getDeclaringClass();

   public abstract Member getMember();

   public final void fixAccess() {
      ClassUtil.checkAndFixAccess(this.getMember());
   }
}
