package org.msgpack.packer;

public interface BufferPacker extends Packer {
   int getBufferSize();

   byte[] toByteArray();

   void clear();
}
