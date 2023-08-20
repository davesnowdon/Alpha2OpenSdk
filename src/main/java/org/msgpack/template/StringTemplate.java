package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class StringTemplate extends AbstractTemplate<String> {
   static final StringTemplate instance = new StringTemplate();

   private StringTemplate() {
   }

   public void write(Packer pk, String target, boolean required) throws IOException {
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

   public String read(Unpacker u, String to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readString();
   }

   public static StringTemplate getInstance() {
      return instance;
   }
}
