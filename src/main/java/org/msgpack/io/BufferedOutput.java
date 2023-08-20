package org.msgpack.io;

import java.io.IOException;
import java.nio.ByteBuffer;

abstract class BufferedOutput implements Output {
   protected byte[] buffer;
   protected int filled;
   protected final int bufferSize;
   protected ByteBuffer castByteBuffer;

   public BufferedOutput(int bufferSize) {
      if (bufferSize < 9) {
         bufferSize = 9;
      }

      this.bufferSize = bufferSize;
   }

   private void allocateNewBuffer() {
      this.buffer = new byte[this.bufferSize];
      this.castByteBuffer = ByteBuffer.wrap(this.buffer);
   }

   private void reserve(int len) throws IOException {
      if (this.buffer == null) {
         this.allocateNewBuffer();
      } else {
         if (this.bufferSize - this.filled < len) {
            if (!this.flushBuffer(this.buffer, 0, this.filled)) {
               this.buffer = new byte[this.bufferSize];
               this.castByteBuffer = ByteBuffer.wrap(this.buffer);
            }

            this.filled = 0;
         }

      }
   }

   public void write(byte[] b, int off, int len) throws IOException {
      if (this.buffer == null) {
         if (this.bufferSize < len) {
            this.flushBuffer(b, off, len);
            return;
         }

         this.allocateNewBuffer();
      }

      if (len <= this.bufferSize - this.filled) {
         System.arraycopy(b, off, this.buffer, this.filled, len);
         this.filled += len;
      } else if (len <= this.bufferSize) {
         if (!this.flushBuffer(this.buffer, 0, this.filled)) {
            this.allocateNewBuffer();
         }

         this.filled = 0;
         System.arraycopy(b, off, this.buffer, 0, len);
         this.filled = len;
      } else {
         this.flush();
         this.flushBuffer(b, off, len);
      }

   }

   public void write(ByteBuffer bb) throws IOException {
      int len = bb.remaining();
      if (this.buffer == null) {
         if (this.bufferSize < len) {
            this.flushByteBuffer(bb);
            return;
         }

         this.allocateNewBuffer();
      }

      if (len <= this.bufferSize - this.filled) {
         bb.get(this.buffer, this.filled, len);
         this.filled += len;
      } else if (len <= this.bufferSize) {
         if (!this.flushBuffer(this.buffer, 0, this.filled)) {
            this.allocateNewBuffer();
         }

         this.filled = 0;
         bb.get(this.buffer, 0, len);
         this.filled = len;
      } else {
         this.flush();
         this.flushByteBuffer(bb);
      }

   }

   public void writeByte(byte v) throws IOException {
      this.reserve(1);
      this.buffer[this.filled++] = v;
   }

   public void writeShort(short v) throws IOException {
      this.reserve(2);
      this.castByteBuffer.putShort(this.filled, v);
      this.filled += 2;
   }

   public void writeInt(int v) throws IOException {
      this.reserve(4);
      this.castByteBuffer.putInt(this.filled, v);
      this.filled += 4;
   }

   public void writeLong(long v) throws IOException {
      this.reserve(8);
      this.castByteBuffer.putLong(this.filled, v);
      this.filled += 8;
   }

   public void writeFloat(float v) throws IOException {
      this.reserve(4);
      this.castByteBuffer.putFloat(this.filled, v);
      this.filled += 4;
   }

   public void writeDouble(double v) throws IOException {
      this.reserve(8);
      this.castByteBuffer.putDouble(this.filled, v);
      this.filled += 8;
   }

   public void writeByteAndByte(byte b, byte v) throws IOException {
      this.reserve(2);
      this.buffer[this.filled++] = b;
      this.buffer[this.filled++] = v;
   }

   public void writeByteAndShort(byte b, short v) throws IOException {
      this.reserve(3);
      this.buffer[this.filled++] = b;
      this.castByteBuffer.putShort(this.filled, v);
      this.filled += 2;
   }

   public void writeByteAndInt(byte b, int v) throws IOException {
      this.reserve(5);
      this.buffer[this.filled++] = b;
      this.castByteBuffer.putInt(this.filled, v);
      this.filled += 4;
   }

   public void writeByteAndLong(byte b, long v) throws IOException {
      this.reserve(9);
      this.buffer[this.filled++] = b;
      this.castByteBuffer.putLong(this.filled, v);
      this.filled += 8;
   }

   public void writeByteAndFloat(byte b, float v) throws IOException {
      this.reserve(5);
      this.buffer[this.filled++] = b;
      this.castByteBuffer.putFloat(this.filled, v);
      this.filled += 4;
   }

   public void writeByteAndDouble(byte b, double v) throws IOException {
      this.reserve(9);
      this.buffer[this.filled++] = b;
      this.castByteBuffer.putDouble(this.filled, v);
      this.filled += 8;
   }

   public void flush() throws IOException {
      if (this.filled > 0) {
         if (!this.flushBuffer(this.buffer, 0, this.filled)) {
            this.buffer = null;
         }

         this.filled = 0;
      }

   }

   protected void flushByteBuffer(ByteBuffer bb) throws IOException {
      byte[] array;
      if (bb.hasArray()) {
         array = bb.array();
         int offset = bb.arrayOffset();
         this.flushBuffer(array, offset + bb.position(), bb.remaining());
         bb.position(bb.limit());
      } else {
         array = new byte[bb.remaining()];
         bb.get(array);
         this.flushBuffer(array, 0, array.length);
      }

   }

   protected abstract boolean flushBuffer(byte[] var1, int var2, int var3) throws IOException;
}
