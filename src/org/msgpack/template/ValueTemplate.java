package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

public class ValueTemplate extends AbstractTemplate<Value> {
   static final ValueTemplate instance = new ValueTemplate();

   private ValueTemplate() {
   }

   public void write(Packer pk, Value target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         target.writeTo(pk);
      }
   }

   public Value read(Unpacker u, Value to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readValue();
   }

   public static ValueTemplate getInstance() {
      return instance;
   }
}
