package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class LongTemplate extends AbstractTemplate<Long> {
   static final LongTemplate instance = new LongTemplate();

   private LongTemplate() {
   }

   public void write(Packer pk, Long target, boolean required) throws IOException {
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

   public Long read(Unpacker u, Long to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readLong();
   }

   public static LongTemplate getInstance() {
      return instance;
   }
}
