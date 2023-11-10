package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class FloatArrayTemplate extends AbstractTemplate<float[]> {
   static final FloatArrayTemplate instance = new FloatArrayTemplate();

   private FloatArrayTemplate() {
   }

   public void write(Packer pk, float[] target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         pk.writeArrayBegin(target.length);
         float[] arr$ = target;
         int len$ = target.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            float a = arr$[i$];
            pk.write(a);
         }

         pk.writeArrayEnd();
      }
   }

   public float[] read(Unpacker u, float[] to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         int n = u.readArrayBegin();
         if (to == null || to.length != n) {
            to = new float[n];
         }

         for(int i = 0; i < n; ++i) {
            to[i] = u.readFloat();
         }

         u.readArrayEnd();
         return to;
      }
   }

   public static FloatArrayTemplate getInstance() {
      return instance;
   }
}
