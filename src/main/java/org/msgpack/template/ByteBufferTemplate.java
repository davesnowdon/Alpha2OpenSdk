package org.msgpack.template;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class ByteBufferTemplate extends AbstractTemplate<ByteBuffer> {
   static final ByteBufferTemplate instance = new ByteBufferTemplate();

   private ByteBufferTemplate() {
   }

   public void write(Packer pk, ByteBuffer target, boolean required) throws IOException {
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

   public ByteBuffer read(Unpacker u, ByteBuffer to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readByteBuffer();
   }

   public static ByteBufferTemplate getInstance() {
      return instance;
   }
}
