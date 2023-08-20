package org.msgpack.util.json;

import org.msgpack.MessagePack;
import org.msgpack.io.LinkedBufferOutput;
import org.msgpack.io.Output;
import org.msgpack.packer.BufferPacker;

public class JSONBufferPacker extends JSONPacker implements BufferPacker {
   private static final int DEFAULT_BUFFER_SIZE = 512;

   public JSONBufferPacker() {
      this(512);
   }

   public JSONBufferPacker(int bufferSize) {
      this(new MessagePack(), bufferSize);
   }

   public JSONBufferPacker(MessagePack msgpack) {
      this(msgpack, 512);
   }

   public JSONBufferPacker(MessagePack msgpack, int bufferSize) {
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
