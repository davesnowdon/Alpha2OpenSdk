package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class AnyTemplate extends AbstractTemplate {
   private TemplateRegistry registry;

   public AnyTemplate(TemplateRegistry registry) {
      this.registry = registry;
   }

   public void write(Packer pk, T target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         }

         pk.writeNil();
      } else {
         this.registry.lookup(target.getClass()).write(pk, target);
      }

   }

   public T read(Unpacker u, T to, boolean required) throws IOException, MessageTypeException {
      if (!required && u.trySkipNil()) {
         return null;
      } else if (to == null) {
         throw new MessageTypeException("convert into unknown type is invalid");
      } else {
         T o = u.read(to);
         if (required && o == null) {
            throw new MessageTypeException("Unexpected nil value");
         } else {
            return o;
         }
      }
   }
}
