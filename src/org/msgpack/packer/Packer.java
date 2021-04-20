package org.msgpack.packer;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.msgpack.type.Value;

public interface Packer extends Closeable, Flushable {
   Packer write(boolean var1) throws IOException;

   Packer write(byte var1) throws IOException;

   Packer write(short var1) throws IOException;

   Packer write(int var1) throws IOException;

   Packer write(long var1) throws IOException;

   Packer write(float var1) throws IOException;

   Packer write(double var1) throws IOException;

   Packer write(Boolean var1) throws IOException;

   Packer write(Byte var1) throws IOException;

   Packer write(Short var1) throws IOException;

   Packer write(Integer var1) throws IOException;

   Packer write(Long var1) throws IOException;

   Packer write(Float var1) throws IOException;

   Packer write(Double var1) throws IOException;

   Packer write(BigInteger var1) throws IOException;

   Packer write(byte[] var1) throws IOException;

   Packer write(byte[] var1, int var2, int var3) throws IOException;

   Packer write(ByteBuffer var1) throws IOException;

   Packer write(String var1) throws IOException;

   Packer write(Value var1) throws IOException;

   Packer write(Object var1) throws IOException;

   Packer writeNil() throws IOException;

   Packer writeArrayBegin(int var1) throws IOException;

   Packer writeArrayEnd(boolean var1) throws IOException;

   Packer writeArrayEnd() throws IOException;

   Packer writeMapBegin(int var1) throws IOException;

   Packer writeMapEnd(boolean var1) throws IOException;

   Packer writeMapEnd() throws IOException;
}
