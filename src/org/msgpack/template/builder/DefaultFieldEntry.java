package org.msgpack.template.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import org.msgpack.MessageTypeException;
import org.msgpack.template.FieldOption;

public class DefaultFieldEntry extends FieldEntry {
   protected Field field;

   public DefaultFieldEntry() {
      this((Field)null, FieldOption.IGNORE);
   }

   public DefaultFieldEntry(DefaultFieldEntry e) {
      this(e.field, e.option);
   }

   public DefaultFieldEntry(Field field, FieldOption option) {
      super(option);
      this.field = field;
   }

   public Field getField() {
      return this.field;
   }

   public void setField(Field field) {
      this.field = field;
   }

   public String getName() {
      return this.field.getName();
   }

   public Class<?> getType() {
      return this.field.getType();
   }

   public Type getGenericType() {
      return this.field.getGenericType();
   }

   public Object get(Object target) {
      try {
         return this.getField().get(target);
      } catch (IllegalArgumentException var3) {
         throw new MessageTypeException(var3);
      } catch (IllegalAccessException var4) {
         throw new MessageTypeException(var4);
      }
   }

   public void set(Object target, Object value) {
      try {
         this.field.set(target, value);
      } catch (IllegalArgumentException var4) {
         throw new MessageTypeException(var4);
      } catch (IllegalAccessException var5) {
         throw new MessageTypeException(var5);
      }
   }
}
