package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class BooleanArrayTemplate extends AbstractTemplate<boolean[]> {
   static final BooleanArrayTemplate instance = new BooleanArrayTemplate();

   private BooleanArrayTemplate() {
   }

   public void write(Packer pk, boolean[] target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         pk.writeArrayBegin(target.length);
         boolean[] arr$ = target;
         int len$ = target.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            boolean a = arr$[i$];
            pk.write(a);
         }

         pk.writeArrayEnd();
      }
   }

   public boolean[] read(Unpacker u, boolean[] to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         int n = u.readArrayBegin();
         if (to == null || to.length != n) {
            to = new boolean[n];
         }

         for(int i = 0; i < n; ++i) {
            to[i] = u.readBoolean();
         }

         u.readArrayEnd();
         return to;
      }
   }

   public static BooleanArrayTemplate getInstance() {
      return instance;
   }
}
