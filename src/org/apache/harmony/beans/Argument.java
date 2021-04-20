package org.apache.harmony.beans;

public class Argument {
   private Class<?> type;
   private Object value;
   private Class<?>[] interfaces;

   public Argument(Object value) {
      this.value = value;
      if (this.value != null) {
         this.type = value.getClass();
         this.interfaces = this.type.getInterfaces();
      }

   }

   public Argument(Class<?> type, Object value) {
      this.type = type;
      this.value = value;
      this.interfaces = type.getInterfaces();
   }

   public Class<?> getType() {
      return this.type;
   }

   public Object getValue() {
      return this.value;
   }

   public Class<?>[] getInterfaces() {
      return this.interfaces;
   }

   public void setType(Class<?> type) {
      this.type = type;
      this.interfaces = type.getInterfaces();
   }

   public void setInterfaces(Class<?>[] interfaces) {
      this.interfaces = interfaces;
   }
}
