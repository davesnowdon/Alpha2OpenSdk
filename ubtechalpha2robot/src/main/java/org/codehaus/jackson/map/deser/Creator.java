package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

abstract class Creator {
   private Creator() {
   }

   static final class PropertyBased {
      protected final Constructor<?> _ctor;
      protected final Method _factoryMethod;
      protected final HashMap<String, SettableBeanProperty> _properties;
      protected final Object[] _defaultValues;

      public PropertyBased(AnnotatedConstructor ctor, SettableBeanProperty[] ctorProps, AnnotatedMethod factory, SettableBeanProperty[] factoryProps) {
         SettableBeanProperty[] props;
         if (ctor != null) {
            this._ctor = ctor.getAnnotated();
            this._factoryMethod = null;
            props = ctorProps;
         } else {
            if (factory == null) {
               throw new IllegalArgumentException("Internal error: neither delegating constructor nor factory method passed");
            }

            this._ctor = null;
            this._factoryMethod = factory.getAnnotated();
            props = factoryProps;
         }

         this._properties = new HashMap();
         Object[] defValues = null;
         int i = 0;

         for(int len = props.length; i < len; ++i) {
            SettableBeanProperty prop = props[i];
            this._properties.put(prop.getName(), prop);
            if (prop.getType().isPrimitive()) {
               if (defValues == null) {
                  defValues = new Object[len];
               }

               defValues[i] = ClassUtil.defaultValue(prop.getType().getRawClass());
            }
         }

         this._defaultValues = defValues;
      }

      public Collection<SettableBeanProperty> properties() {
         return this._properties.values();
      }

      public SettableBeanProperty findCreatorProperty(String name) {
         return (SettableBeanProperty)this._properties.get(name);
      }

      public PropertyValueBuffer startBuilding(JsonParser jp, DeserializationContext ctxt) {
         return new PropertyValueBuffer(jp, ctxt, this._properties.size());
      }

      public Object build(PropertyValueBuffer buffer) throws Exception {
         Object bean;
         try {
            if (this._ctor != null) {
               bean = this._ctor.newInstance(buffer.getParameters(this._defaultValues));
            } else {
               bean = this._factoryMethod.invoke((Object)null, buffer.getParameters(this._defaultValues));
            }
         } catch (Exception var4) {
            ClassUtil.throwRootCause(var4);
            return null;
         }

         for(PropertyValue pv = buffer.buffered(); pv != null; pv = pv.next) {
            pv.assign(bean);
         }

         return bean;
      }
   }

   static final class Delegating {
      protected final AnnotatedMember _creator;
      protected final JavaType _valueType;
      protected final Constructor<?> _ctor;
      protected final Method _factoryMethod;
      protected JsonDeserializer<Object> _deserializer;

      public Delegating(BasicBeanDescription beanDesc, AnnotatedConstructor ctor, AnnotatedMethod factory) {
         TypeBindings bindings = beanDesc.bindingsForBeanType();
         if (ctor != null) {
            this._creator = ctor;
            this._ctor = ctor.getAnnotated();
            this._factoryMethod = null;
            this._valueType = bindings.resolveType(ctor.getParameterType(0));
         } else {
            if (factory == null) {
               throw new IllegalArgumentException("Internal error: neither delegating constructor nor factory method passed");
            }

            this._creator = factory;
            this._ctor = null;
            this._factoryMethod = factory.getAnnotated();
            this._valueType = bindings.resolveType(factory.getParameterType(0));
         }

      }

      public JavaType getValueType() {
         return this._valueType;
      }

      public AnnotatedMember getCreator() {
         return this._creator;
      }

      public void setDeserializer(JsonDeserializer<Object> deser) {
         this._deserializer = deser;
      }

      public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         Object value = this._deserializer.deserialize(jp, ctxt);

         try {
            return this._ctor != null ? this._ctor.newInstance(value) : this._factoryMethod.invoke((Object)null, value);
         } catch (Exception var5) {
            ClassUtil.unwrapAndThrowAsIAE(var5);
            return null;
         }
      }
   }

   static final class NumberBased {
      protected final Class<?> _valueClass;
      protected final Constructor<?> _intCtor;
      protected final Constructor<?> _longCtor;
      protected final Method _intFactoryMethod;
      protected final Method _longFactoryMethod;

      public NumberBased(Class<?> valueClass, AnnotatedConstructor intCtor, AnnotatedMethod ifm, AnnotatedConstructor longCtor, AnnotatedMethod lfm) {
         this._valueClass = valueClass;
         this._intCtor = intCtor == null ? null : intCtor.getAnnotated();
         this._longCtor = longCtor == null ? null : longCtor.getAnnotated();
         this._intFactoryMethod = ifm == null ? null : ifm.getAnnotated();
         this._longFactoryMethod = lfm == null ? null : lfm.getAnnotated();
      }

      public Object construct(int value) {
         try {
            if (this._intCtor != null) {
               return this._intCtor.newInstance(value);
            }

            if (this._intFactoryMethod != null) {
               return this._intFactoryMethod.invoke(this._valueClass, value);
            }
         } catch (Exception var3) {
            ClassUtil.unwrapAndThrowAsIAE(var3);
         }

         return this.construct((long)value);
      }

      public Object construct(long value) {
         try {
            if (this._longCtor != null) {
               return this._longCtor.newInstance(value);
            }

            if (this._longFactoryMethod != null) {
               return this._longFactoryMethod.invoke(this._valueClass, value);
            }
         } catch (Exception var4) {
            ClassUtil.unwrapAndThrowAsIAE(var4);
         }

         return null;
      }
   }

   static final class StringBased {
      protected final Class<?> _valueClass;
      protected final Method _factoryMethod;
      protected final Constructor<?> _ctor;

      public StringBased(Class<?> valueClass, AnnotatedConstructor ctor, AnnotatedMethod factoryMethod) {
         this._valueClass = valueClass;
         this._ctor = ctor == null ? null : ctor.getAnnotated();
         this._factoryMethod = factoryMethod == null ? null : factoryMethod.getAnnotated();
      }

      public Object construct(String value) {
         try {
            if (this._ctor != null) {
               return this._ctor.newInstance(value);
            }

            if (this._factoryMethod != null) {
               return this._factoryMethod.invoke(this._valueClass, value);
            }
         } catch (Exception var3) {
            ClassUtil.unwrapAndThrowAsIAE(var3);
         }

         return null;
      }
   }
}
