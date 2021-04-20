package org.codehaus.jackson.map.ser;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerFactory;
import org.codehaus.jackson.map.type.ClassKey;
import org.codehaus.jackson.type.JavaType;

public class CustomSerializerFactory extends BeanSerializerFactory {
   protected HashMap<ClassKey, JsonSerializer<?>> _directClassMappings;
   protected JsonSerializer<?> _enumSerializerOverride;
   protected HashMap<ClassKey, JsonSerializer<?>> _transitiveClassMappings;
   protected HashMap<ClassKey, JsonSerializer<?>> _interfaceMappings;

   public CustomSerializerFactory() {
      this((SerializerFactory.Config)null);
   }

   public CustomSerializerFactory(SerializerFactory.Config config) {
      super(config);
      this._directClassMappings = null;
      this._transitiveClassMappings = null;
      this._interfaceMappings = null;
   }

   public SerializerFactory withConfig(SerializerFactory.Config config) {
      if (this.getClass() != CustomSerializerFactory.class) {
         throw new IllegalStateException("Subtype of CustomSerializerFactory (" + this.getClass().getName() + ") has not properly overridden method 'withAdditionalSerializers': can not instantiate subtype with " + "additional serializer definitions");
      } else {
         return new CustomSerializerFactory(config);
      }
   }

   public <T> void addGenericMapping(Class<? extends T> type, JsonSerializer<T> ser) {
      ClassKey key = new ClassKey(type);
      if (type.isInterface()) {
         if (this._interfaceMappings == null) {
            this._interfaceMappings = new HashMap();
         }

         this._interfaceMappings.put(key, ser);
      } else {
         if (this._transitiveClassMappings == null) {
            this._transitiveClassMappings = new HashMap();
         }

         this._transitiveClassMappings.put(key, ser);
      }

   }

   public <T> void addSpecificMapping(Class<? extends T> forClass, JsonSerializer<T> ser) {
      ClassKey key = new ClassKey(forClass);
      if (forClass.isInterface()) {
         throw new IllegalArgumentException("Can not add specific mapping for an interface (" + forClass.getName() + ")");
      } else if (Modifier.isAbstract(forClass.getModifiers())) {
         throw new IllegalArgumentException("Can not add specific mapping for an abstract class (" + forClass.getName() + ")");
      } else {
         if (this._directClassMappings == null) {
            this._directClassMappings = new HashMap();
         }

         this._directClassMappings.put(key, ser);
      }
   }

   public void setEnumSerializer(JsonSerializer<?> enumSer) {
      this._enumSerializerOverride = enumSer;
   }

   public JsonSerializer<Object> createSerializer(SerializationConfig config, JavaType type, BeanProperty property) throws JsonMappingException {
      JsonSerializer<?> ser = this.findCustomSerializer(type.getRawClass(), config);
      return ser != null ? ser : super.createSerializer(config, type, property);
   }

   protected JsonSerializer<?> findCustomSerializer(Class<?> type, SerializationConfig config) {
      JsonSerializer<?> ser = null;
      ClassKey key = new ClassKey(type);
      if (this._directClassMappings != null) {
         ser = (JsonSerializer)this._directClassMappings.get(key);
         if (ser != null) {
            return ser;
         }
      }

      if (type.isEnum() && this._enumSerializerOverride != null) {
         return this._enumSerializerOverride;
      } else {
         Class curr;
         if (this._transitiveClassMappings != null) {
            for(curr = type; curr != null; curr = curr.getSuperclass()) {
               key.reset(curr);
               ser = (JsonSerializer)this._transitiveClassMappings.get(key);
               if (ser != null) {
                  return ser;
               }
            }
         }

         if (this._interfaceMappings != null) {
            key.reset(type);
            ser = (JsonSerializer)this._interfaceMappings.get(key);
            if (ser != null) {
               return ser;
            }

            for(curr = type; curr != null; curr = curr.getSuperclass()) {
               ser = this._findInterfaceMapping(curr, key);
               if (ser != null) {
                  return ser;
               }
            }
         }

         return null;
      }
   }

   protected JsonSerializer<?> _findInterfaceMapping(Class<?> cls, ClassKey key) {
      Class[] arr$ = cls.getInterfaces();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Class<?> iface = arr$[i$];
         key.reset(iface);
         JsonSerializer<?> ser = (JsonSerializer)this._interfaceMappings.get(key);
         if (ser != null) {
            return ser;
         }

         ser = this._findInterfaceMapping(iface, key);
         if (ser != null) {
            return ser;
         }
      }

      return null;
   }
}
