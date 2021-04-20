package org.msgpack.template.builder;

import java.lang.reflect.Type;
import org.msgpack.template.FieldList;
import org.msgpack.template.Template;

public interface TemplateBuilder {
   boolean matchType(Type var1, boolean var2);

   <T> Template<T> buildTemplate(Type var1) throws TemplateBuildException;

   <T> Template<T> buildTemplate(Class<T> var1, FieldList var2) throws TemplateBuildException;

   void writeTemplate(Type var1, String var2);

   <T> Template<T> loadTemplate(Type var1);
}
