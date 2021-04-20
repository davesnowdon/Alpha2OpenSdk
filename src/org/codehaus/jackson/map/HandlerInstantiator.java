package org.codehaus.jackson.map;

import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;

public abstract class HandlerInstantiator {
   public HandlerInstantiator() {
   }

   public abstract JsonDeserializer<?> deserializerInstance(DeserializationConfig var1, Annotated var2, Class<? extends JsonDeserializer<?>> var3);

   public abstract KeyDeserializer keyDeserializerInstance(DeserializationConfig var1, Annotated var2, Class<? extends KeyDeserializer> var3);

   public abstract JsonSerializer<?> serializerInstance(SerializationConfig var1, Annotated var2, Class<? extends JsonSerializer<?>> var3);

   public abstract TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> var1, Annotated var2, Class<? extends TypeResolverBuilder<?>> var3);

   public abstract TypeIdResolver typeIdResolverInstance(MapperConfig<?> var1, Annotated var2, Class<? extends TypeIdResolver> var3);
}
