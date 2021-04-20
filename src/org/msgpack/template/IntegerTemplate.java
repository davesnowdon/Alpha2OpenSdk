package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class IntegerTemplate extends AbstractTemplate<Integer> {
   static final IntegerTemplate instance = new IntegerTemplate();

   private IntegerTemplate() {
   }

   public void write(Packer pk, Integer target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         pk.write(target);
      }
   }

   public Integer read(Unpacker u, Integer to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readInt();
   }

   public static IntegerTemplate getInstance() {
      return instance;
   }
}
