package org.msgpack.unpacker;

final class ArrayAccept extends Accept {
   int size;

   ArrayAccept() {
      super("array");
   }

   void acceptArray(int size) {
      this.size = size;
   }
}
