package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class FloatTemplate extends AbstractTemplate<Float> {
   static final FloatTemplate instance = new FloatTemplate();

   private FloatTemplate() {
   }

   public void write(Packer pk, Float target, boolean required) throws IOException {
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

   public Float read(Unpacker u, Float to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readFloat();
   }

   public static FloatTemplate getInstance() {
      return instance;
   }
}
