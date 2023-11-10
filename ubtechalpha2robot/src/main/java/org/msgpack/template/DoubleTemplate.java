package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class DoubleTemplate extends AbstractTemplate<Double> {
   static final DoubleTemplate instance = new DoubleTemplate();

   private DoubleTemplate() {
   }

   public void write(Packer pk, Double target, boolean required) throws IOException {
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

   public Double read(Unpacker u, Double to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readDouble();
   }

   public static DoubleTemplate getInstance() {
      return instance;
   }
}
