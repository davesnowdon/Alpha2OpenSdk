package org.msgpack.template.builder.beans;

import java.lang.reflect.Method;
import org.apache.harmony.beans.BeansUtils;
import org.apache.harmony.beans.internal.nls.Messages;

public class IndexedPropertyDescriptor extends PropertyDescriptor {
   private Class<?> indexedPropertyType;
   private Method indexedGetter;
   private Method indexedSetter;

   public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass, String getterName, String setterName, String indexedGetterName, String indexedSetterName) throws IntrospectionException {
      super(propertyName, beanClass, getterName, setterName);
      this.setIndexedByName(beanClass, indexedGetterName, indexedSetterName);
   }

   private void setIndexedByName(Class<?> beanClass, String indexedGetterName, String indexedSetterName) throws IntrospectionException {
      String theIndexedGetterName = indexedGetterName;
      if (indexedGetterName == null) {
         if (indexedSetterName != null) {
            this.setIndexedWriteMethod(beanClass, indexedSetterName);
         }
      } else {
         if (indexedGetterName.length() == 0) {
            theIndexedGetterName = "get" + this.name;
         }

         this.setIndexedReadMethod(beanClass, theIndexedGetterName);
         if (indexedSetterName != null) {
            this.setIndexedWriteMethod(beanClass, indexedSetterName, this.indexedPropertyType);
         }
      }

      if (!this.isCompatible()) {
         throw new IntrospectionException(Messages.getString("custom.beans.57"));
      }
   }

   private boolean isCompatible() {
      Class<?> propertyType = this.getPropertyType();
      if (propertyType == null) {
         return true;
      } else {
         Class<?> componentTypeOfProperty = propertyType.getComponentType();
         if (componentTypeOfProperty == null) {
            return false;
         } else {
            return this.indexedPropertyType == null ? false : componentTypeOfProperty.getName().equals(this.indexedPropertyType.getName());
         }
      }
   }

   public IndexedPropertyDescriptor(String propertyName, Method getter, Method setter, Method indexedGetter, Method indexedSetter) throws IntrospectionException {
      super(propertyName, getter, setter);
      if (indexedGetter != null) {
         this.internalSetIndexedReadMethod(indexedGetter);
         this.internalSetIndexedWriteMethod(indexedSetter, true);
      } else {
         this.internalSetIndexedWriteMethod(indexedSetter, true);
         this.internalSetIndexedReadMethod(indexedGetter);
      }

      if (!this.isCompatible()) {
         throw new IntrospectionException(Messages.getString("custom.beans.57"));
      }
   }

   public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass) throws IntrospectionException {
      super(propertyName, beanClass);
      this.setIndexedByName(beanClass, "get".concat(initialUpperCase(propertyName)), "set".concat(initialUpperCase(propertyName)));
   }

   public void setIndexedReadMethod(Method indexedGetter) throws IntrospectionException {
      this.internalSetIndexedReadMethod(indexedGetter);
   }

   public void setIndexedWriteMethod(Method indexedSetter) throws IntrospectionException {
      this.internalSetIndexedWriteMethod(indexedSetter, false);
   }

   public Method getIndexedWriteMethod() {
      return this.indexedSetter;
   }

   public Method getIndexedReadMethod() {
      return this.indexedGetter;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof IndexedPropertyDescriptor)) {
         return false;
      } else {
         boolean var10000;
         label52: {
            IndexedPropertyDescriptor other = (IndexedPropertyDescriptor)obj;
            if (super.equals(other)) {
               label46: {
                  if (this.indexedPropertyType == null) {
                     if (other.indexedPropertyType != null) {
                        break label46;
                     }
                  } else if (!this.indexedPropertyType.equals(other.indexedPropertyType)) {
                     break label46;
                  }

                  if (this.indexedGetter == null) {
                     if (other.indexedGetter != null) {
                        break label46;
                     }
                  } else if (!this.indexedGetter.equals(other.indexedGetter)) {
                     break label46;
                  }

                  if (this.indexedSetter == null) {
                     if (other.indexedSetter == null) {
                        break label52;
                     }
                  } else if (this.indexedSetter.equals(other.indexedSetter)) {
                     break label52;
                  }
               }
            }

            var10000 = false;
            return var10000;
         }

         var10000 = true;
         return var10000;
      }
   }

   public int hashCode() {
      return super.hashCode() + BeansUtils.getHashCode(this.indexedPropertyType) + BeansUtils.getHashCode(this.indexedGetter) + BeansUtils.getHashCode(this.indexedSetter);
   }

   public Class<?> getIndexedPropertyType() {
      return this.indexedPropertyType;
   }

   private void setIndexedReadMethod(Class<?> beanClass, String indexedGetterName) throws IntrospectionException {
      Method getter;
      try {
         getter = beanClass.getMethod(indexedGetterName, Integer.TYPE);
      } catch (NoSuchMethodException var5) {
         throw new IntrospectionException(Messages.getString("custom.beans.58"));
      } catch (SecurityException var6) {
         throw new IntrospectionException(Messages.getString("custom.beans.59"));
      }

      this.internalSetIndexedReadMethod(getter);
   }

   private void internalSetIndexedReadMethod(Method indexGetter) throws IntrospectionException {
      if (indexGetter == null) {
         if (this.indexedSetter == null) {
            if (this.getPropertyType() != null) {
               throw new IntrospectionException(Messages.getString("custom.beans.5A"));
            }

            this.indexedPropertyType = null;
         }

         this.indexedGetter = null;
      } else if (indexGetter.getParameterTypes().length == 1 && indexGetter.getParameterTypes()[0] == Integer.TYPE) {
         Class<?> indexedReadType = indexGetter.getReturnType();
         if (indexedReadType == Void.TYPE) {
            throw new IntrospectionException(Messages.getString("custom.beans.5B"));
         } else if (this.indexedSetter != null && indexGetter.getReturnType() != this.indexedSetter.getParameterTypes()[1]) {
            throw new IntrospectionException(Messages.getString("custom.beans.5A"));
         } else {
            if (this.indexedGetter == null) {
               this.indexedPropertyType = indexedReadType;
            } else if (this.indexedPropertyType != indexedReadType) {
               throw new IntrospectionException(Messages.getString("custom.beans.5A"));
            }

            this.indexedGetter = indexGetter;
         }
      } else {
         throw new IntrospectionException(Messages.getString("custom.beans.5B"));
      }
   }

   private void setIndexedWriteMethod(Class<?> beanClass, String indexedSetterName) throws IntrospectionException {
      Method setter = null;

      try {
         setter = beanClass.getMethod(indexedSetterName, Integer.TYPE, this.getPropertyType().getComponentType());
      } catch (SecurityException var5) {
         throw new IntrospectionException(Messages.getString("custom.beans.5C"));
      } catch (NoSuchMethodException var6) {
         throw new IntrospectionException(Messages.getString("custom.beans.5D"));
      }

      this.internalSetIndexedWriteMethod(setter, true);
   }

   private void setIndexedWriteMethod(Class<?> beanClass, String indexedSetterName, Class<?> argType) throws IntrospectionException {
      try {
         Method setter = beanClass.getMethod(indexedSetterName, Integer.TYPE, argType);
         this.internalSetIndexedWriteMethod(setter, true);
      } catch (NoSuchMethodException var5) {
         throw new IntrospectionException(Messages.getString("custom.beans.5D"));
      } catch (SecurityException var6) {
         throw new IntrospectionException(Messages.getString("custom.beans.5C"));
      }
   }

   private void internalSetIndexedWriteMethod(Method indexSetter, boolean initialize) throws IntrospectionException {
      if (indexSetter == null) {
         if (this.indexedGetter == null) {
            if (this.getPropertyType() != null) {
               throw new IntrospectionException(Messages.getString("custom.beans.5E"));
            }

            this.indexedPropertyType = null;
         }

         this.indexedSetter = null;
      } else {
         Class<?>[] indexedSetterArgs = indexSetter.getParameterTypes();
         if (indexedSetterArgs.length != 2) {
            throw new IntrospectionException(Messages.getString("custom.beans.5F"));
         } else if (indexedSetterArgs[0] != Integer.TYPE) {
            throw new IntrospectionException(Messages.getString("custom.beans.60"));
         } else {
            Class<?> indexedWriteType = indexedSetterArgs[1];
            if (initialize && this.indexedGetter == null) {
               this.indexedPropertyType = indexedWriteType;
            } else if (this.indexedPropertyType != indexedWriteType) {
               throw new IntrospectionException(Messages.getString("custom.beans.61"));
            }

            this.indexedSetter = indexSetter;
         }
      }
   }

   private static String initialUpperCase(String string) {
      if (Character.isUpperCase(string.charAt(0))) {
         return string;
      } else {
         String initial = string.substring(0, 1).toUpperCase();
         return initial.concat(string.substring(1));
      }
   }
}
