package org.codehaus.jackson.map.jsontype;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.type.JavaType;

public interface TypeIdResolver {
   void init(JavaType var1);

   String idFromValue(Object var1);

   String idFromValueAndType(Object var1, Class<?> var2);

   JavaType typeFromId(String var1);

   JsonTypeInfo.Id getMechanism();
}
