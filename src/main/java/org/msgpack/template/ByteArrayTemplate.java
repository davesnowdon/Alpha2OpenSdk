package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class ByteArrayTemplate extends AbstractTemplate<byte[]> {
   static final ByteArrayTemplate instance = new ByteArrayTemplate();

   private ByteArrayTemplate() {
   }

   public void write(Packer pk, byte[] target, boolean required) throws IOException {
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

   public byte[] read(Unpacker u, byte[] to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readByteArray();
   }

   public static ByteArrayTemplate getInstance() {
      return instance;
   }
}
