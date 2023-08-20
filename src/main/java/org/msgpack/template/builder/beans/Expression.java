package org.msgpack.template.builder.beans;

import org.apache.harmony.beans.BeansUtils;

public class Expression extends Statement {
   boolean valueIsDefined = false;
   Object value;

   public Expression(Object value, Object target, String methodName, Object[] arguments) {
      super(target, methodName, arguments);
      this.value = value;
      this.valueIsDefined = true;
   }

   public Expression(Object target, String methodName, Object[] arguments) {
      super(target, methodName, arguments);
      this.value = null;
      this.valueIsDefined = false;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      if (!this.valueIsDefined) {
         sb.append("<unbound>");
      } else if (this.value == null) {
         sb.append("null");
      } else {
         Class<?> clazz = this.value.getClass();
         sb.append(clazz == String.class ? "\"\"" : BeansUtils.idOfClass(clazz));
      }

      sb.append('=');
      sb.append(super.toString());
      return sb.toString();
   }

   public void setValue(Object value) {
      this.value = value;
      this.valueIsDefined = true;
   }

   public Object getValue() throws Exception {
      if (!this.valueIsDefined) {
         this.value = this.invokeMethod();
         this.valueIsDefined = true;
      }

      return this.value;
   }
}
