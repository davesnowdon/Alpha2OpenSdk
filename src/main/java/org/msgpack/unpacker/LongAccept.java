package org.msgpack.unpacker;

import org.msgpack.MessageTypeException;

final class LongAccept extends Accept {
   long value;

   LongAccept() {
      super("integer");
   }

   void acceptInteger(byte v) {
      this.value = (long)v;
   }

   void acceptInteger(short v) {
      this.value = (long)v;
   }

   void acceptInteger(int v) {
      this.value = (long)v;
   }

   void acceptInteger(long v) {
      this.value = v;
   }

   void acceptUnsignedInteger(byte v) {
      this.value = (long)(v & 255);
   }

   void acceptUnsignedInteger(short v) {
      this.value = (long)(v & '\uffff');
   }

   void acceptUnsignedInteger(int v) {
      if (v < 0) {
         this.value = (long)(v & 2147483647) + 2147483648L;
      } else {
         this.value = (long)v;
      }

   }

   void acceptUnsignedInteger(long v) {
      if (v < 0L) {
         throw new MessageTypeException();
      } else {
         this.value = v;
      }
   }
}
