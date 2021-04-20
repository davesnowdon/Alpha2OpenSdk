package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class DoubleArrayTemplate extends AbstractTemplate<double[]> {
   static final DoubleArrayTemplate instance = new DoubleArrayTemplate();

   private DoubleArrayTemplate() {
   }

   public void write(Packer pk, double[] target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         pk.writeArrayBegin(target.length);
         double[] arr$ = target;
         int len$ = target.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            double a = arr$[i$];
            pk.write(a);
         }

         pk.writeArrayEnd();
      }
   }

   public double[] read(Unpacker u, double[] to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         int n = u.readArrayBegin();
         if (to == null || to.length != n) {
            to = new double[n];
         }

         for(int i = 0; i < n; ++i) {
            to[i] = u.readDouble();
         }

         u.readArrayEnd();
         return to;
      }
   }

   public static DoubleArrayTemplate getInstance() {
      return instance;
   }
}
