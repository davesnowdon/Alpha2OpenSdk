package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class IntegerArrayTemplate extends AbstractTemplate<int[]> {
   static final IntegerArrayTemplate instance = new IntegerArrayTemplate();

   private IntegerArrayTemplate() {
   }

   public void write(Packer pk, int[] target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         pk.writeArrayBegin(target.length);
         int[] arr$ = target;
         int len$ = target.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            int a = arr$[i$];
            pk.write(a);
         }

         pk.writeArrayEnd();
      }
   }

   public int[] read(Unpacker u, int[] to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         int n = u.readArrayBegin();
         int[] array;
         if (to != null && to.length == n) {
            array = to;
         } else {
            array = new int[n];
         }

         for(int i = 0; i < n; ++i) {
            array[i] = u.readInt();
         }

         u.readArrayEnd();
         return array;
      }
   }

   public static IntegerArrayTemplate getInstance() {
      return instance;
   }
}
