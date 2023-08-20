package org.msgpack.io;

abstract class AbstractInput implements Input {
   private int readByteCount = 0;

   AbstractInput() {
   }

   public int getReadByteCount() {
      return this.readByteCount;
   }

   public void resetReadByteCount() {
      this.readByteCount = 0;
   }

   protected final void incrReadByteCount(int size) {
      this.readByteCount += size;
   }

   protected final void incrReadOneByteCount() {
      ++this.readByteCount;
   }
}
