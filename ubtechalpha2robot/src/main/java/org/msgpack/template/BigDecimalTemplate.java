package org.msgpack.template;

import java.io.IOException;
import java.math.BigDecimal;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class BigDecimalTemplate extends AbstractTemplate<BigDecimal> {
   static final BigDecimalTemplate instance = new BigDecimalTemplate();

   private BigDecimalTemplate() {
   }

   public void write(Packer pk, BigDecimal target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         pk.write(target.toString());
      }
   }

   public BigDecimal read(Unpacker u, BigDecimal to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         String temp = u.readString();
         return new BigDecimal(temp);
      }
   }

   public static BigDecimalTemplate getInstance() {
      return instance;
   }
}
