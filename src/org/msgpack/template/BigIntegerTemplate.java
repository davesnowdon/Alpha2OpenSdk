package org.msgpack.template;

import java.io.IOException;
import java.math.BigInteger;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class BigIntegerTemplate extends AbstractTemplate<BigInteger> {
   static final BigIntegerTemplate instance = new BigIntegerTemplate();

   private BigIntegerTemplate() {
   }

   public void write(Packer pk, BigInteger target, boolean required) throws IOException {
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

   public BigInteger read(Unpacker u, BigInteger to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : u.readBigInteger();
   }

   public static BigIntegerTemplate getInstance() {
      return instance;
   }
}
