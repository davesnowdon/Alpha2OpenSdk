package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class ShortArrayTemplate extends AbstractTemplate<short[]> {
   static final ShortArrayTemplate instance = new ShortArrayTemplate();

   private ShortArrayTemplate() {
   }

   public void write(Packer pk, short[] target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         pk.writeArrayBegin(target.length);
         short[] arr$ = target;
         int len$ = target.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            short a = arr$[i$];
            pk.write(a);
         }

         pk.writeArrayEnd();
      }
   }

   public short[] read(Unpacker u, short[] to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         int n = u.readArrayBegin();
         if (to == null || to.length != n) {
            to = new short[n];
         }

         for(int i = 0; i < n; ++i) {
            to[i] = u.readShort();
         }

         u.readArrayEnd();
         return to;
      }
   }

   public static ShortArrayTemplate getInstance() {
      return instance;
   }
}
