package org.codehaus.jackson.map;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.map.deser.BeanDeserializerModifier;
import org.codehaus.jackson.map.ser.BeanSerializerModifier;
import org.codehaus.jackson.map.type.TypeModifier;

public abstract class Module implements Versioned {
   public Module() {
   }

   public abstract String getModuleName();

   public abstract Version version();

   public abstract void setupModule(Module.SetupContext var1);

   public interface SetupContext {
      Version getMapperVersion();

      DeserializationConfig getDeserializationConfig();

      SerializationConfig getSerializationConfig();

      void addDeserializers(Deserializers var1);

      void addKeyDeserializers(KeyDeserializers var1);

      void addSerializers(Serializers var1);

      void addKeySerializers(Serializers var1);

      void addBeanDeserializerModifier(BeanDeserializerModifier var1);

      void addBeanSerializerModifier(BeanSerializerModifier var1);

      void addAbstractTypeResolver(AbstractTypeResolver var1);

      void addTypeModifier(TypeModifier var1);

      void insertAnnotationIntrospector(AnnotationIntrospector var1);

      void appendAnnotationIntrospector(AnnotationIntrospector var1);

      void setMixInAnnotations(Class<?> var1, Class<?> var2);
   }
}
