package org.msgpack.template.builder;

import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.msgpack.template.OrdinalEnumTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;

public class OrdinalEnumTemplateBuilder extends AbstractTemplateBuilder {
   private static final Logger LOG = Logger.getLogger(OrdinalEnumTemplateBuilder.class.getName());

   public OrdinalEnumTemplateBuilder(TemplateRegistry registry) {
      super(registry);
   }

   public boolean matchType(Type targetType, boolean hasAnnotation) {
      Class<?> targetClass = (Class)targetType;
      boolean matched = matchAtOrdinalEnumTemplateBuilder(targetClass, hasAnnotation);
      if (matched && LOG.isLoggable(Level.FINE)) {
         LOG.fine("matched type: " + targetClass.getName());
      }

      return matched;
   }

   public <T> Template<T> buildTemplate(Class<T> targetClass, FieldEntry[] entries) {
      throw new UnsupportedOperationException("fatal error: " + targetClass.getName());
   }

   public <T> Template<T> buildTemplate(Type targetType) throws TemplateBuildException {
      Class<T> targetClass = (Class)targetType;
      this.checkOrdinalEnumValidation(targetClass);
      return new OrdinalEnumTemplate(targetClass);
   }

   protected void checkOrdinalEnumValidation(Class<?> targetClass) {
      if (!targetClass.isEnum()) {
         throw new TemplateBuildException("tried to build ordinal enum template of non-enum class: " + targetClass.getName());
      }
   }
}
