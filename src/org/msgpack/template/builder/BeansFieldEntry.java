package org.msgpack.template.builder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import org.msgpack.MessageTypeException;
import org.msgpack.template.FieldOption;
import org.msgpack.template.builder.beans.PropertyDescriptor;

public class BeansFieldEntry extends FieldEntry {
   protected PropertyDescriptor desc;

   public BeansFieldEntry() {
   }

   public BeansFieldEntry(BeansFieldEntry e) {
      super(e.option);
      this.desc = e.getPropertyDescriptor();
   }

   public BeansFieldEntry(PropertyDescriptor desc) {
      this(desc, FieldOption.DEFAULT);
   }

   public BeansFieldEntry(PropertyDescriptor desc, FieldOption option) {
      super(option);
      this.desc = desc;
   }

   public String getGetterName() {
      return this.getPropertyDescriptor().getReadMethod().getName();
   }

   public String getSetterName() {
      return this.getPropertyDescriptor().getWriteMethod().getName();
   }

   public PropertyDescriptor getPropertyDescriptor() {
      return this.desc;
   }

   public String getName() {
      return this.getPropertyDescriptor().getDisplayName();
   }

   public Class<?> getType() {
      return this.getPropertyDescriptor().getPropertyType();
   }

   public Type getGenericType() {
      return this.getPropertyDescriptor().getReadMethod().getGenericReturnType();
   }

   public Object get(Object target) {
      try {
         return this.getPropertyDescriptor().getReadMethod().invoke(target);
      } catch (IllegalArgumentException var3) {
         throw new MessageTypeException(var3);
      } catch (IllegalAccessException var4) {
         throw new MessageTypeException(var4);
      } catch (InvocationTargetException var5) {
         throw new MessageTypeException(var5);
      }
   }

   public void set(Object target, Object value) {
      try {
         this.getPropertyDescriptor().getWriteMethod().invoke(target, value);
      } catch (IllegalArgumentException var4) {
         throw new MessageTypeException(var4);
      } catch (IllegalAccessException var5) {
         throw new MessageTypeException(var5);
      } catch (InvocationTargetException var6) {
         throw new MessageTypeException(var6);
      }
   }
}
