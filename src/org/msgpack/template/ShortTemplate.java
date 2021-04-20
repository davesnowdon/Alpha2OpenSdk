package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class ShortTemplate extends AbstractTemplate<Short> {
   static final ShortTemplate instance = new ShortTemplate();

   private ShortTemplate() {
   }

   public void write(Packer pk, Short target, boolean required) throws IOException {
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

   public Short read(Unpacker u, Short to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readShort();
   }

   public static ShortTemplate getInstance() {
      return instance;
   }
}
