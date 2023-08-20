package org.codehaus.jackson.map;

import java.lang.reflect.Type;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.deser.BeanDeserializerModifier;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

public abstract class DeserializerFactory {
   protected static final Deserializers[] NO_DESERIALIZERS = new Deserializers[0];

   public DeserializerFactory() {
   }

   public abstract DeserializerFactory.Config getConfig();

   public abstract DeserializerFactory withConfig(DeserializerFactory.Config var1);

   public final DeserializerFactory withAdditionalDeserializers(Deserializers additional) {
      return this.withConfig(this.getConfig().withAdditionalDeserializers(additional));
   }

   public final DeserializerFactory withAdditionalKeyDeserializers(KeyDeserializers additional) {
      return this.withConfig(this.getConfig().withAdditionalKeyDeserializers(additional));
   }

   public final DeserializerFactory withDeserializerModifier(BeanDeserializerModifier modifier) {
      return this.withConfig(this.getConfig().withDeserializerModifier(modifier));
   }

   public final DeserializerFactory withAbstractTypeResolver(AbstractTypeResolver resolver) {
      return this.withConfig(this.getConfig().withAbstractTypeResolver(resolver));
   }

   public abstract JsonDeserializer<Object> createBeanDeserializer(DeserializationConfig var1, DeserializerProvider var2, JavaType var3, BeanProperty var4) throws JsonMappingException;

   public abstract JsonDeserializer<?> createArrayDeserializer(DeserializationConfig var1, DeserializerProvider var2, ArrayType var3, BeanProperty var4) throws JsonMappingException;

   public abstract JsonDeserializer<?> createCollectionDeserializer(DeserializationConfig var1, DeserializerProvider var2, CollectionType var3, BeanProperty var4) throws JsonMappingException;

   public abstract JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationConfig var1, DeserializerProvider var2, CollectionLikeType var3, BeanProperty var4) throws JsonMappingException;

   public abstract JsonDeserializer<?> createEnumDeserializer(DeserializationConfig var1, DeserializerProvider var2, JavaType var3, BeanProperty var4) throws JsonMappingException;

   public abstract JsonDeserializer<?> createMapDeserializer(DeserializationConfig var1, DeserializerProvider var2, MapType var3, BeanProperty var4) throws JsonMappingException;

   public abstract JsonDeserializer<?> createMapLikeDeserializer(DeserializationConfig var1, DeserializerProvider var2, MapLikeType var3, BeanProperty var4) throws JsonMappingException;

   public abstract JsonDeserializer<?> createTreeDeserializer(DeserializationConfig var1, DeserializerProvider var2, JavaType var3, BeanProperty var4) throws JsonMappingException;

   public KeyDeserializer createKeyDeserializer(DeserializationConfig config, JavaType type, BeanProperty property) throws JsonMappingException {
      return null;
   }

   public TypeDeserializer findTypeDeserializer(DeserializationConfig config, JavaType baseType, BeanProperty property) {
      return null;
   }

   /** @deprecated */
   @Deprecated
   public final TypeDeserializer findTypeDeserializer(DeserializationConfig config, JavaType baseType) {
      return this.findTypeDeserializer(config, baseType, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonDeserializer<Object> createBeanDeserializer(DeserializationConfig config, JavaType type, DeserializerProvider p) throws JsonMappingException {
      return this.createBeanDeserializer(config, p, type, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonDeserializer<?> createArrayDeserializer(DeserializationConfig config, ArrayType type, DeserializerProvider p) throws JsonMappingException {
      return this.createArrayDeserializer(config, p, type, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonDeserializer<?> createCollectionDeserializer(DeserializationConfig config, CollectionType type, DeserializerProvider p) throws JsonMappingException {
      return this.createCollectionDeserializer(config, p, type, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonDeserializer<?> createEnumDeserializer(DeserializationConfig config, Class<?> enumClass, DeserializerProvider p) throws JsonMappingException {
      return this.createEnumDeserializer(config, p, TypeFactory.type((Type)enumClass), (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonDeserializer<?> createMapDeserializer(DeserializationConfig config, MapType type, DeserializerProvider p) throws JsonMappingException {
      return this.createMapDeserializer(config, p, type, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public final JsonDeserializer<?> createTreeDeserializer(DeserializationConfig config, Class<? extends JsonNode> nodeClass, DeserializerProvider p) throws JsonMappingException {
      return this.createTreeDeserializer(config, p, TypeFactory.type((Type)nodeClass), (BeanProperty)null);
   }

   public abstract static class Config {
      public Config() {
      }

      public abstract DeserializerFactory.Config withAdditionalDeserializers(Deserializers var1);

      public abstract DeserializerFactory.Config withAdditionalKeyDeserializers(KeyDeserializers var1);

      public abstract DeserializerFactory.Config withDeserializerModifier(BeanDeserializerModifier var1);

      public abstract DeserializerFactory.Config withAbstractTypeResolver(AbstractTypeResolver var1);

      public abstract Iterable<Deserializers> deserializers();

      public abstract Iterable<KeyDeserializers> keyDeserializers();

      public abstract Iterable<BeanDeserializerModifier> deserializerModifiers();

      public abstract Iterable<AbstractTypeResolver> abstractTypeResolvers();

      public abstract boolean hasDeserializers();

      public abstract boolean hasKeyDeserializers();

      public abstract boolean hasDeserializerModifiers();

      public abstract boolean hasAbstractTypeResolvers();
   }
}
