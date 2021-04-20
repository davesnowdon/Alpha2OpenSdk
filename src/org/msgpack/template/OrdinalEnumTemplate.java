package org.msgpack.template;

import java.io.IOException;
import java.util.HashMap;
import org.msgpack.MessageTypeException;
import org.msgpack.annotation.OrdinalEnum;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class OrdinalEnumTemplate<T> extends AbstractTemplate<T> {
   protected T[] entries;
   protected HashMap<T, Integer> reverse;
   protected boolean strict;

   public OrdinalEnumTemplate(Class<T> targetClass) {
      this.entries = targetClass.getEnumConstants();
      this.reverse = new HashMap();

      for(int i = 0; i < this.entries.length; ++i) {
         this.reverse.put(this.entries[i], i);
      }

      this.strict = !targetClass.isAnnotationPresent(OrdinalEnum.class) || ((OrdinalEnum)targetClass.getAnnotation(OrdinalEnum.class)).strict();
   }

   public void write(Packer pk, T target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         Integer ordinal = (Integer)this.reverse.get(target);
         if (ordinal == null) {
            throw new MessageTypeException(new IllegalArgumentException("ordinal: " + ordinal));
         } else {
            pk.write(ordinal);
         }
      }
   }

   public T read(Unpacker pac, T to, boolean required) throws IOException, MessageTypeException {
      if (!required && pac.trySkipNil()) {
         return null;
      } else {
         int ordinal = pac.readInt();
         if (ordinal < this.entries.length) {
            return this.entries[ordinal];
         } else if (!this.strict) {
            return null;
         } else {
            throw new MessageTypeException(new IllegalArgumentException("ordinal: " + ordinal));
         }
      }
   }
}
