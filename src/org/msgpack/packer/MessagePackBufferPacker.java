package org.msgpack.packer;

import org.msgpack.MessagePack;
import org.msgpack.io.LinkedBufferOutput;
import org.msgpack.io.Output;

public class MessagePackBufferPacker extends MessagePackPacker implements BufferPacker {
   private static final int DEFAULT_BUFFER_SIZE = 512;

   public MessagePackBufferPacker(MessagePack msgpack) {
      this(msgpack, 512);
   }

   public MessagePackBufferPacker(MessagePack msgpack, int bufferSize) {
      super(msgpack, (Output)(new LinkedBufferOutput(bufferSize)));
   }

   public int getBufferSize() {
      return ((LinkedBufferOutput)this.out).getSize();
   }

   public byte[] toByteArray() {
      return ((LinkedBufferOutput)this.out).toByteArray();
   }

   public void clear() {
      this.reset();
      ((LinkedBufferOutput)this.out).clear();
   }
}
