package org.msgpack.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface Output extends Closeable, Flushable {
   void write(byte[] var1, int var2, int var3) throws IOException;

   void write(ByteBuffer var1) throws IOException;

   void writeByte(byte var1) throws IOException;

   void writeShort(short var1) throws IOException;

   void writeInt(int var1) throws IOException;

   void writeLong(long var1) throws IOException;

   void writeFloat(float var1) throws IOException;

   void writeDouble(double var1) throws IOException;

   void writeByteAndByte(byte var1, byte var2) throws IOException;

   void writeByteAndShort(byte var1, short var2) throws IOException;

   void writeByteAndInt(byte var1, int var2) throws IOException;

   void writeByteAndLong(byte var1, long var2) throws IOException;

   void writeByteAndFloat(byte var1, float var2) throws IOException;

   void writeByteAndDouble(byte var1, double var2) throws IOException;
}
