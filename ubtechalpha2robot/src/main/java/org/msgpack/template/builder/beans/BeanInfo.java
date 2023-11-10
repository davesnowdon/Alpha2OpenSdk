package org.msgpack.template.builder.beans;

public interface BeanInfo {
   PropertyDescriptor[] getPropertyDescriptors();

   MethodDescriptor[] getMethodDescriptors();

   EventSetDescriptor[] getEventSetDescriptors();

   BeanInfo[] getAdditionalBeanInfo();

   BeanDescriptor getBeanDescriptor();

   int getDefaultPropertyIndex();

   int getDefaultEventIndex();
}
