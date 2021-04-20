package org.msgpack.io;

import java.io.Closeable;
import java.io.IOException;

public interface Input extends Closeable {
   int read(byte[] var1, int var2, int var3) throws IOException;

   boolean tryRefer(BufferReferer var1, int var2) throws IOException;

   byte readByte() throws IOException;

   void advance();

   byte getByte() throws IOException;

   short getShort() throws IOException;

   int getInt() throws IOException;

   long getLong() throws IOException;

   float getFloat() throws IOException;

   double getDouble() throws IOException;

   int getReadByteCount();

   void resetReadByteCount();
}
