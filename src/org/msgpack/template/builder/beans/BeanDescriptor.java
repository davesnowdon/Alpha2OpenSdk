package org.msgpack.template.builder.beans;

public class BeanDescriptor extends FeatureDescriptor {
   private Class<?> beanClass;
   private Class<?> customizerClass;

   public BeanDescriptor(Class<?> beanClass, Class<?> customizerClass) {
      if (beanClass == null) {
         throw new NullPointerException();
      } else {
         this.setName(this.getShortClassName(beanClass));
         this.beanClass = beanClass;
         this.customizerClass = customizerClass;
      }
   }

   public BeanDescriptor(Class<?> beanClass) {
      this(beanClass, (Class)null);
   }

   public Class<?> getCustomizerClass() {
      return this.customizerClass;
   }

   public Class<?> getBeanClass() {
      return this.beanClass;
   }

   private String getShortClassName(Class<?> leguminaClass) {
      if (leguminaClass == null) {
         return null;
      } else {
         String beanClassName = leguminaClass.getName();
         int lastIndex = beanClassName.lastIndexOf(".");
         return lastIndex == -1 ? beanClassName : beanClassName.substring(lastIndex + 1);
      }
   }
}
