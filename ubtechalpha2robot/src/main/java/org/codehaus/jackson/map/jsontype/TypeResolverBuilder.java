package org.codehaus.jackson.map.jsontype;

import java.util.Collection;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.type.JavaType;

public interface TypeResolverBuilder {
   TypeSerializer buildTypeSerializer(SerializationConfig var1, JavaType var2, Collection<NamedType> var3, BeanProperty var4);

   TypeDeserializer buildTypeDeserializer(DeserializationConfig var1, JavaType var2, Collection<NamedType> var3, BeanProperty var4);

   Object init(JsonTypeInfo.Id var1, TypeIdResolver var2);

   Object inclusion(JsonTypeInfo.As var1);

   Object typeProperty(String var1);
}
