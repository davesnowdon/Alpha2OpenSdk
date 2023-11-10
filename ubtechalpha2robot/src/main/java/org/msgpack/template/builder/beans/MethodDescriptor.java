package org.msgpack.template.builder.beans;

import java.lang.reflect.Method;

public class MethodDescriptor extends FeatureDescriptor {
   private Method method;
   private ParameterDescriptor[] parameterDescriptors;

   public MethodDescriptor(Method method, ParameterDescriptor[] parameterDescriptors) {
      if (method == null) {
         throw new NullPointerException();
      } else {
         this.method = method;
         this.parameterDescriptors = parameterDescriptors;
         this.setName(method.getName());
      }
   }

   public MethodDescriptor(Method method) {
      if (method == null) {
         throw new NullPointerException();
      } else {
         this.method = method;
         this.setName(method.getName());
      }
   }

   public Method getMethod() {
      return this.method;
   }

   public ParameterDescriptor[] getParameterDescriptors() {
      return this.parameterDescriptors;
   }

   void merge(MethodDescriptor anotherMethod) {
      super.merge(anotherMethod);
      if (this.method == null) {
         this.method = anotherMethod.method;
      }

      if (this.parameterDescriptors == null) {
         this.parameterDescriptors = anotherMethod.parameterDescriptors;
      }

   }
}
