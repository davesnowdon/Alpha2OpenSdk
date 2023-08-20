package org.msgpack.unpacker;

final class DoubleAccept extends Accept {
   double value;

   DoubleAccept() {
      super("float");
   }

   void acceptFloat(float v) {
      this.value = (double)v;
   }

   void acceptDouble(double v) {
      this.value = v;
   }
}
