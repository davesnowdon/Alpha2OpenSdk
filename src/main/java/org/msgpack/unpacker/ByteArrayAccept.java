package org.msgpack.unpacker;

import java.io.IOException;
import java.nio.ByteBuffer;

final class ByteArrayAccept extends Accept {
   byte[] value;

   ByteArrayAccept() {
      super("raw value");
   }

   void acceptRaw(byte[] raw) {
      this.value = raw;
   }

   void acceptEmptyRaw() {
      this.value = new byte[0];
   }

   public void refer(ByteBuffer bb, boolean gift) throws IOException {
      this.value = new byte[bb.remaining()];
      bb.get(this.value);
   }
}
