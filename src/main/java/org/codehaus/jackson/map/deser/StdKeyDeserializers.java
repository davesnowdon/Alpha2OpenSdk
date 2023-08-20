package org.codehaus.jackson.map.deser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

class StdKeyDeserializers {
   final HashMap<JavaType, KeyDeserializer> _keyDeserializers = new HashMap();

   private StdKeyDeserializers() {
      this.add(new StdKeyDeserializer.BoolKD());
      this.add(new StdKeyDeserializer.ByteKD());
      this.add(new StdKeyDeserializer.CharKD());
      this.add(new StdKeyDeserializer.ShortKD());
      this.add(new StdKeyDeserializer.IntKD());
      this.add(new StdKeyDeserializer.LongKD());
      this.add(new StdKeyDeserializer.FloatKD());
      this.add(new StdKeyDeserializer.DoubleKD());
   }

   private void add(StdKeyDeserializer kdeser) {
      Class<?> keyClass = kdeser.getKeyClass();
      this._keyDeserializers.put(TypeFactory.defaultInstance().constructType((Type)keyClass), kdeser);
   }

   public static HashMap<JavaType, KeyDeserializer> constructAll() {
      return (new StdKeyDeserializers())._keyDeserializers;
   }

   public static KeyDeserializer constructEnumKeyDeserializer(DeserializationConfig config, JavaType type) {
      EnumResolver<?> er = EnumResolver.constructUnsafe(type.getRawClass(), config.getAnnotationIntrospector());
      return new StdKeyDeserializer.EnumKD(er);
   }

   public static KeyDeserializer findStringBasedKeyDeserializer(DeserializationConfig config, JavaType type) {
      BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspect(type);
      Constructor<?> ctor = beanDesc.findSingleArgConstructor(String.class);
      if (ctor != null) {
         return new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
      } else {
         Method m = beanDesc.findFactoryMethod(String.class);
         return m != null ? new StdKeyDeserializer.StringFactoryKeyDeserializer(m) : null;
      }
   }
}
