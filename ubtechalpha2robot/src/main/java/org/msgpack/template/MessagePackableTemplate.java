package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessagePackable;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class MessagePackableTemplate extends AbstractTemplate<MessagePackable> {
   private Class<?> targetClass;

   MessagePackableTemplate(Class<?> targetClass) {
      this.targetClass = targetClass;
   }

   public void write(Packer pk, MessagePackable target, boolean required) throws IOException {
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

   public MessagePackable read(Unpacker u, MessagePackable to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         if (to == null) {
            try {
               to = (MessagePackable)this.targetClass.newInstance();
            } catch (InstantiationException var5) {
               throw new MessageTypeException(var5);
            } catch (IllegalAccessException var6) {
               throw new MessageTypeException(var6);
            }
         }

         to.readFrom(u);
         return to;
      }
   }
}
