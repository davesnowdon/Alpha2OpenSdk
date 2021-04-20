package org.msgpack.unpacker;

import java.nio.ByteBuffer;

public interface BufferUnpacker extends Unpacker {
   BufferUnpacker wrap(byte[] var1);

   BufferUnpacker wrap(byte[] var1, int var2, int var3);

   BufferUnpacker wrap(ByteBuffer var1);

   BufferUnpacker feed(byte[] var1);

   BufferUnpacker feed(byte[] var1, boolean var2);

   BufferUnpacker feed(byte[] var1, int var2, int var3);

   BufferUnpacker feed(byte[] var1, int var2, int var3, boolean var4);

   BufferUnpacker feed(ByteBuffer var1);

   BufferUnpacker feed(ByteBuffer var1, boolean var2);

   int getBufferSize();

   void copyReferencedBuffer();

   void clear();
}
