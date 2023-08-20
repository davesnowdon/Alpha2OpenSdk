package org.codehaus.jackson.map;

import org.codehaus.jackson.map.ser.BeanSerializerModifier;
import org.codehaus.jackson.type.JavaType;

public abstract class SerializerFactory {
   public SerializerFactory() {
   }

   public abstract SerializerFactory.Config getConfig();

   public abstract SerializerFactory withConfig(SerializerFactory.Config var1);

   public final SerializerFactory withAdditionalSerializers(Serializers additional) {
      return this.withConfig(this.getConfig().withAdditionalSerializers(additional));
   }

   public final SerializerFactory withAdditionalKeySerializers(Serializers additional) {
      return this.withConfig(this.getConfig().withAdditionalKeySerializers(additional));
   }

   public final SerializerFactory withSerializerModifier(BeanSerializerModifier modifier) {
      return this.withConfig(this.getConfig().withSerializerModifier(modifier));
   }

   public abstract JsonSerializer<Object> createSerializer(SerializationConfig var1, JavaType var2, BeanProperty var3) throws JsonMappingException;

   public abstract TypeSerializer createTypeSerializer(SerializationConfig var1, JavaType var2, BeanProperty var3) throws JsonMappingException;

   public abstract JsonSerializer<Object> createKeySerializer(SerializationConfig var1, JavaType var2, BeanProperty var3) throws JsonMappingException;

   /** @deprecated */
   @Deprecated
   public final JsonSerializer<Object> createSerializer(JavaType type, SerializationConfig config) {
      try {
         return this.createSerializer(config, type, (BeanProperty)null);
      } catch (JsonMappingException var4) {
         throw new RuntimeJsonMappingException(var4);
      }
   }

   /** @deprecated */
   @Deprecated
   public final TypeSerializer createTypeSerializer(JavaType baseType, SerializationConfig config) {
      try {
         return this.createTypeSerializer(config, baseType, (BeanProperty)null);
      } catch (JsonMappingException var4) {
         throw new RuntimeException(var4);
      }
   }

   public abstract static class Config {
      public Config() {
      }

      public abstract SerializerFactory.Config withAdditionalSerializers(Serializers var1);

      public abstract SerializerFactory.Config withAdditionalKeySerializers(Serializers var1);

      public abstract SerializerFactory.Config withSerializerModifier(BeanSerializerModifier var1);

      public abstract boolean hasSerializers();

      public abstract boolean hasKeySerializers();

      public abstract boolean hasSerializerModifiers();

      public abstract Iterable<Serializers> serializers();

      public abstract Iterable<Serializers> keySerializers();

      public abstract Iterable<BeanSerializerModifier> serializerModifiers();
   }
}
