package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class LongArrayTemplate extends AbstractTemplate<long[]> {
   static final LongArrayTemplate instance = new LongArrayTemplate();

   private LongArrayTemplate() {
   }

   public void write(Packer pk, long[] target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         pk.writeArrayBegin(target.length);
         long[] arr$ = target;
         int len$ = target.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            long a = arr$[i$];
            pk.write(a);
         }

         pk.writeArrayEnd();
      }
   }

   public long[] read(Unpacker u, long[] to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         int n = u.readArrayBegin();
         if (to == null || to.length != n) {
            to = new long[n];
         }

         for(int i = 0; i < n; ++i) {
            to[i] = u.readLong();
         }

         u.readArrayEnd();
         return to;
      }
   }

   public static LongArrayTemplate getInstance() {
      return instance;
   }
}
