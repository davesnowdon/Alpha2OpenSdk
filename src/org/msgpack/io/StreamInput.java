package org.msgpack.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class StreamInput extends AbstractInput {
   private final InputStream in;
   private byte[] castBuffer;
   private ByteBuffer castByteBuffer;
   private int filled;

   public StreamInput(InputStream in) {
      this.in = in;
      this.castBuffer = new byte[8];
      this.castByteBuffer = ByteBuffer.wrap(this.castBuffer);
      this.filled = 0;
   }

   public int read(byte[] b, int off, int len) throws IOException {
      int n;
      for(int remain = len; remain > 0; off += n) {
         n = this.in.read(b, off, remain);
         if (n <= 0) {
            throw new EOFException();
         }

         this.incrReadByteCount(n);
         remain -= n;
      }

      return len;
   }

   public boolean tryRefer(BufferReferer ref, int size) throws IOException {
      return false;
   }

   public byte readByte() throws IOException {
      int n = this.in.read();
      if (n < 0) {
         throw new EOFException();
      } else {
         this.incrReadOneByteCount();
         return (byte)n;
      }
   }

   public void advance() {
      this.incrReadByteCount(this.filled);
      this.filled = 0;
   }

   private void require(int len) throws IOException {
      while(this.filled < len) {
         int n = this.in.read(this.castBuffer, this.filled, len - this.filled);
         if (n < 0) {
            throw new EOFException();
         }

         this.filled += n;
      }

   }

   public byte getByte() throws IOException {
      this.require(1);
      return this.castBuffer[0];
   }

   public short getShort() throws IOException {
      this.require(2);
      return this.castByteBuffer.getShort(0);
   }

   public int getInt() throws IOException {
      this.require(4);
      return this.castByteBuffer.getInt(0);
   }

   public long getLong() throws IOException {
      this.require(8);
      return this.castByteBuffer.getLong(0);
   }

   public float getFloat() throws IOException {
      this.require(4);
      return this.castByteBuffer.getFloat(0);
   }

   public double getDouble() throws IOException {
      this.require(8);
      return this.castByteBuffer.getDouble(0);
   }

   public void close() throws IOException {
      this.in.close();
   }
}
