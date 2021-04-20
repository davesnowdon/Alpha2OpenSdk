package org.msgpack.unpacker;

final class MapAccept extends Accept {
   int size;

   MapAccept() {
      super("map");
   }

   void acceptMap(int size) {
      this.size = size;
   }
}
