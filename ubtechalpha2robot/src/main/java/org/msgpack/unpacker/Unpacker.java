package org.msgpack.unpacker;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.msgpack.template.Template;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;

public interface Unpacker extends Iterable<Value>, Closeable {
   Object read(Class var1) throws IOException;

   Object read(T var1) throws IOException;

   Object read(Template var1) throws IOException;

   Object read(T var1, Template var2) throws IOException;

   void skip() throws IOException;

   int readArrayBegin() throws IOException;

   void readArrayEnd(boolean var1) throws IOException;

   void readArrayEnd() throws IOException;

   int readMapBegin() throws IOException;

   void readMapEnd(boolean var1) throws IOException;

   void readMapEnd() throws IOException;

   void readNil() throws IOException;

   boolean trySkipNil() throws IOException;

   boolean readBoolean() throws IOException;

   byte readByte() throws IOException;

   short readShort() throws IOException;

   int readInt() throws IOException;

   long readLong() throws IOException;

   BigInteger readBigInteger() throws IOException;

   float readFloat() throws IOException;

   double readDouble() throws IOException;

   byte[] readByteArray() throws IOException;

   ByteBuffer readByteBuffer() throws IOException;

   String readString() throws IOException;

   Value readValue() throws IOException;

   ValueType getNextType() throws IOException;

   UnpackerIterator iterator();

   int getReadByteCount();

   void resetReadByteCount();

   void setRawSizeLimit(int var1);

   void setArraySizeLimit(int var1);

   void setMapSizeLimit(int var1);
}
