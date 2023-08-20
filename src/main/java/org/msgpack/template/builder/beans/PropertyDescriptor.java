package org.msgpack.template.builder.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.apache.harmony.beans.BeansUtils;
import org.apache.harmony.beans.internal.nls.Messages;

public class PropertyDescriptor extends FeatureDescriptor {
   private Method getter;
   private Method setter;
   private Class<?> propertyEditorClass;
   boolean constrained;
   boolean bound;

   public PropertyDescriptor(String propertyName, Class<?> beanClass, String getterName, String setterName) throws IntrospectionException {
      if (beanClass == null) {
         throw new IntrospectionException(Messages.getString("custom.beans.03"));
      } else if (propertyName != null && propertyName.length() != 0) {
         this.setName(propertyName);
         if (getterName != null) {
            if (getterName.length() == 0) {
               throw new IntrospectionException("read or write method cannot be empty.");
            }

            try {
               this.setReadMethod(beanClass, getterName);
            } catch (IntrospectionException var6) {
               this.setReadMethod(beanClass, this.createDefaultMethodName(propertyName, "get"));
            }
         }

         if (setterName != null) {
            if (setterName.length() == 0) {
               throw new IntrospectionException("read or write method cannot be empty.");
            }

            this.setWriteMethod(beanClass, setterName);
         }

      } else {
         throw new IntrospectionException(Messages.getString("custom.beans.04"));
      }
   }

   public PropertyDescriptor(String propertyName, Method getter, Method setter) throws IntrospectionException {
      if (propertyName != null && propertyName.length() != 0) {
         this.setName(propertyName);
         this.setReadMethod(getter);
         this.setWriteMethod(setter);
      } else {
         throw new IntrospectionException(Messages.getString("custom.beans.04"));
      }
   }

   public PropertyDescriptor(String propertyName, Class<?> beanClass) throws IntrospectionException {
      if (beanClass == null) {
         throw new IntrospectionException(Messages.getString("custom.beans.03"));
      } else if (propertyName != null && propertyName.length() != 0) {
         this.setName(propertyName);

         try {
            this.setReadMethod(beanClass, this.createDefaultMethodName(propertyName, "is"));
         } catch (Exception var4) {
            this.setReadMethod(beanClass, this.createDefaultMethodName(propertyName, "get"));
         }

         this.setWriteMethod(beanClass, this.createDefaultMethodName(propertyName, "set"));
      } else {
         throw new IntrospectionException(Messages.getString("custom.beans.04"));
      }
   }

   public void setWriteMethod(Method setter) throws IntrospectionException {
      if (setter != null) {
         int modifiers = setter.getModifiers();
         if (!Modifier.isPublic(modifiers)) {
            throw new IntrospectionException(Messages.getString("custom.beans.05"));
         }

         Class<?>[] parameterTypes = setter.getParameterTypes();
         if (parameterTypes.length != 1) {
            throw new IntrospectionException(Messages.getString("custom.beans.06"));
         }

         Class<?> parameterType = parameterTypes[0];
         Class<?> propertyType = this.getPropertyType();
         if (propertyType != null && !propertyType.equals(parameterType)) {
            throw new IntrospectionException(Messages.getString("custom.beans.07"));
         }
      }

      this.setter = setter;
   }

   public void setReadMethod(Method getter) throws IntrospectionException {
      if (getter != null) {
         int modifiers = getter.getModifiers();
         if (!Modifier.isPublic(modifiers)) {
            throw new IntrospectionException(Messages.getString("custom.beans.0A"));
         }

         Class<?>[] parameterTypes = getter.getParameterTypes();
         if (parameterTypes.length != 0) {
            throw new IntrospectionException(Messages.getString("custom.beans.08"));
         }

         Class<?> returnType = getter.getReturnType();
         if (returnType.equals(Void.TYPE)) {
            throw new IntrospectionException(Messages.getString("custom.beans.33"));
         }

         Class<?> propertyType = this.getPropertyType();
         if (propertyType != null && !returnType.equals(propertyType)) {
            throw new IntrospectionException(Messages.getString("custom.beans.09"));
         }
      }

      this.getter = getter;
   }

   public Method getWriteMethod() {
      return this.setter;
   }

   public Method getReadMethod() {
      return this.getter;
   }

   public boolean equals(Object object) {
      boolean result = object instanceof PropertyDescriptor;
      if (result) {
         PropertyDescriptor pd = (PropertyDescriptor)object;
         boolean gettersAreEqual = this.getter == null && pd.getReadMethod() == null || this.getter != null && this.getter.equals(pd.getReadMethod());
         boolean settersAreEqual = this.setter == null && pd.getWriteMethod() == null || this.setter != null && this.setter.equals(pd.getWriteMethod());
         boolean propertyTypesAreEqual = this.getPropertyType() == pd.getPropertyType();
         boolean propertyEditorClassesAreEqual = this.getPropertyEditorClass() == pd.getPropertyEditorClass();
         boolean boundPropertyAreEqual = this.isBound() == pd.isBound();
         boolean constrainedPropertyAreEqual = this.isConstrained() == pd.isConstrained();
         result = gettersAreEqual && settersAreEqual && propertyTypesAreEqual && propertyEditorClassesAreEqual && boundPropertyAreEqual && constrainedPropertyAreEqual;
      }

      return result;
   }

   public int hashCode() {
      return BeansUtils.getHashCode(this.getter) + BeansUtils.getHashCode(this.setter) + BeansUtils.getHashCode(this.getPropertyType()) + BeansUtils.getHashCode(this.getPropertyEditorClass()) + BeansUtils.getHashCode(this.isBound()) + BeansUtils.getHashCode(this.isConstrained());
   }

   public void setPropertyEditorClass(Class<?> propertyEditorClass) {
      this.propertyEditorClass = propertyEditorClass;
   }

   public Class<?> getPropertyType() {
      Class<?> result = null;
      if (this.getter != null) {
         result = this.getter.getReturnType();
      } else if (this.setter != null) {
         Class<?>[] parameterTypes = this.setter.getParameterTypes();
         result = parameterTypes[0];
      }

      return result;
   }

   public Class<?> getPropertyEditorClass() {
      return this.propertyEditorClass;
   }

   public void setConstrained(boolean constrained) {
      this.constrained = constrained;
   }

   public void setBound(boolean bound) {
      this.bound = bound;
   }

   public boolean isConstrained() {
      return this.constrained;
   }

   public boolean isBound() {
      return this.bound;
   }

   String createDefaultMethodName(String propertyName, String prefix) {
      String result = null;
      if (propertyName != null) {
         String bos = BeansUtils.toASCIIUpperCase(propertyName.substring(0, 1));
         String eos = propertyName.substring(1, propertyName.length());
         result = prefix + bos + eos;
      }

      return result;
   }

   void setReadMethod(Class<?> beanClass, String getterName) throws IntrospectionException {
      try {
         Method readMethod = beanClass.getMethod(getterName);
         this.setReadMethod(readMethod);
      } catch (Exception var4) {
         throw new IntrospectionException(var4.getLocalizedMessage());
      }
   }

   void setWriteMethod(Class<?> beanClass, String setterName) throws IntrospectionException {
      Method writeMethod = null;

      try {
         if (this.getter != null) {
            writeMethod = beanClass.getMethod(setterName, this.getter.getReturnType());
         } else {
            Class<?> clazz = beanClass;

            for(Method[] methods = null; clazz != null && writeMethod == null; clazz = clazz.getSuperclass()) {
               methods = clazz.getDeclaredMethods();
               Method[] arr$ = methods;
               int len$ = methods.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  Method method = arr$[i$];
                  if (setterName.equals(method.getName()) && method.getParameterTypes().length == 1) {
                     writeMethod = method;
                     break;
                  }
               }
            }
         }
      } catch (Exception var10) {
         throw new IntrospectionException(var10.getLocalizedMessage());
      }

      if (writeMethod == null) {
         throw new IntrospectionException(Messages.getString("custom.beans.64", (Object)setterName));
      } else {
         this.setWriteMethod(writeMethod);
      }
   }

   public PropertyEditor createPropertyEditor(Object bean) {
      if (this.propertyEditorClass == null) {
         return null;
      } else if (!PropertyEditor.class.isAssignableFrom(this.propertyEditorClass)) {
         throw new ClassCastException(Messages.getString("custom.beans.48"));
      } else {
         try {
            PropertyEditor editor;
            Constructor constr;
            try {
               constr = this.propertyEditorClass.getConstructor(Object.class);
               editor = (PropertyEditor)constr.newInstance(bean);
            } catch (NoSuchMethodException var5) {
               constr = this.propertyEditorClass.getConstructor();
               editor = (PropertyEditor)constr.newInstance();
            }

            return editor;
         } catch (Exception var6) {
            RuntimeException re = new RuntimeException(Messages.getString("custom.beans.47"), var6);
            throw re;
         }
      }
   }
}
