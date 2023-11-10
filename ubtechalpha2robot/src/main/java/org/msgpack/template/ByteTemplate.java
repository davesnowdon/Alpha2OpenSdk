package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class ByteTemplate extends AbstractTemplate<Byte> {
   static final ByteTemplate instance = new ByteTemplate();

   private ByteTemplate() {
   }

   public void write(Packer pk, Byte target, boolean required) throws IOException {
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

   public Byte read(Unpacker u, Byte to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readByte();
   }

   public static ByteTemplate getInstance() {
      return instance;
   }
}
