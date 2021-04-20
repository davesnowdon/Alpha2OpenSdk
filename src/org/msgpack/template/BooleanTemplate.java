package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class BooleanTemplate extends AbstractTemplate<Boolean> {
   static final BooleanTemplate instance = new BooleanTemplate();

   private BooleanTemplate() {
   }

   public void write(Packer pk, Boolean target, boolean required) throws IOException {
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

   public Boolean read(Unpacker u, Boolean to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readBoolean();
   }

   public static BooleanTemplate getInstance() {
      return instance;
   }
}
