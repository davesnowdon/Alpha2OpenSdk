package org.msgpack.unpacker;

import org.msgpack.MessageTypeException;

final class IntAccept extends Accept {
   int value;

   IntAccept() {
      super("integer");
   }

   void acceptInteger(byte v) {
      this.value = v;
   }

   void acceptInteger(short v) {
      this.value = v;
   }

   void acceptInteger(int v) {
      this.value = v;
   }

   void acceptInteger(long v) {
      if (v >= -2147483648L && v <= 2147483647L) {
         this.value = (int)v;
      } else {
         throw new MessageTypeException();
      }
   }

   void acceptUnsignedInteger(byte v) {
      this.value = v & 255;
   }

   void acceptUnsignedInteger(short v) {
      this.value = v & '\uffff';
   }

   void acceptUnsignedInteger(int v) {
      if (v < 0) {
         throw new MessageTypeException();
      } else {
         this.value = v;
      }
   }

   void acceptUnsignedInteger(long v) {
      if (v >= 0L && v <= 2147483647L) {
         this.value = (int)v;
      } else {
         throw new MessageTypeException();
      }
   }
}
