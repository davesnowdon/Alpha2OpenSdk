package org.msgpack.template;

import java.io.IOException;
import java.util.Date;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class DateTemplate extends AbstractTemplate<Date> {
   static final DateTemplate instance = new DateTemplate();

   private DateTemplate() {
   }

   public void write(Packer pk, Date target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         pk.write(target.getTime());
      }
   }

   public Date read(Unpacker u, Date to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         long temp = u.readLong();
         return new Date(temp);
      }
   }

   public static DateTemplate getInstance() {
      return instance;
   }
}
