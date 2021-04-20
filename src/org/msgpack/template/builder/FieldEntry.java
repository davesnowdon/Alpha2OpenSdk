package org.msgpack.template.builder;

import java.lang.reflect.Type;
import org.msgpack.template.FieldOption;

public abstract class FieldEntry {
   protected FieldOption option;

   public FieldEntry() {
      this(FieldOption.IGNORE);
   }

   public FieldEntry(FieldOption option) {
      this.option = option;
   }

   public FieldOption getOption() {
      return this.option;
   }

   public void setOption(FieldOption option) {
      this.option = option;
   }

   public boolean isAvailable() {
      return this.option != FieldOption.IGNORE;
   }

   public boolean isOptional() {
      return this.option == FieldOption.OPTIONAL;
   }

   public boolean isNotNullable() {
      return this.option == FieldOption.NOTNULLABLE;
   }

   public abstract String getName();

   public abstract Class<?> getType();

   public abstract Type getGenericType();

   public abstract Object get(Object var1);

   public abstract void set(Object var1, Object var2);

   public String getJavaTypeName() {
      Class<?> type = this.getType();
      return type.isArray() ? this.arrayTypeToString(type) : type.getName();
   }

   public String arrayTypeToString(Class<?> type) {
      int dim = 1;

      Class baseType;
      for(baseType = type.getComponentType(); baseType.isArray(); ++dim) {
         baseType = baseType.getComponentType();
      }

      StringBuilder sb = new StringBuilder();
      sb.append(baseType.getName());

      for(int i = 0; i < dim; ++i) {
         sb.append("[]");
      }

      return sb.toString();
   }
}
