package org.codehaus.jackson.map;

import org.codehaus.jackson.map.deser.BeanDeserializerModifier;
import org.codehaus.jackson.type.JavaType;

public abstract class DeserializerProvider {
   protected DeserializerProvider() {
   }

   public abstract DeserializerProvider withAdditionalDeserializers(Deserializers var1);

   public abstract DeserializerProvider withAdditionalKeyDeserializers(KeyDeserializers var1);

   public abstract DeserializerProvider withDeserializerModifier(BeanDeserializerModifier var1);

   public abstract DeserializerProvider withAbstractTypeResolver(AbstractTypeResolver var1);

   public abstract JsonDeserializer<Object> findValueDeserializer(DeserializationConfig var1, JavaType var2, BeanProperty var3) throws JsonMappingException;

   public abstract JsonDeserializer<Object> findTypedValueDeserializer(DeserializationConfig var1, JavaType var2, BeanProperty var3) throws JsonMappingException;

   public abstract KeyDeserializer findKeyDeserializer(DeserializationConfig var1, JavaType var2, BeanProperty var3) throws JsonMappingException;

   public abstract boolean hasValueDeserializerFor(DeserializationConfig var1, JavaType var2);

   /** @deprecated */
   @Deprecated
   public final JsonDeserializer<Object> findValueDeserializer(DeserializationConfig config, JavaType type, JavaType referrer, String refPropName) throws JsonMappingException {
      return this.findValueDeserializer(config, type, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonDeserializer<Object> findTypedValueDeserializer(DeserializationConfig config, JavaType type) throws JsonMappingException {
      return this.findTypedValueDeserializer(config, type, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final KeyDeserializer findKeyDeserializer(DeserializationConfig config, JavaType keyType) throws JsonMappingException {
      return this.findKeyDeserializer(config, keyType, (BeanProperty)null);
   }

   public abstract int cachedDeserializersCount();

   public abstract void flushCachedDeserializers();
}
