package org.msgpack.io;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class ByteBufferOutput implements Output {
   private ByteBuffer buffer;
   private ByteBufferOutput.ExpandBufferCallback callback;

   public ByteBufferOutput(ByteBuffer buffer) {
      this(buffer, (ByteBufferOutput.ExpandBufferCallback)null);
   }

   public ByteBufferOutput(ByteBuffer buffer, ByteBufferOutput.ExpandBufferCallback callback) {
      this.buffer = buffer;
      this.callback = callback;
   }

   private void reserve(int len) throws IOException {
      if (len > this.buffer.remaining()) {
         if (this.callback == null) {
            throw new BufferOverflowException();
         } else {
            this.buffer = this.callback.call(this.buffer, len);
         }
      }
   }

   public void write(byte[] b, int off, int len) throws IOException {
      this.reserve(len);
      this.buffer.put(b, off, len);
   }

   public void write(ByteBuffer bb) throws IOException {
      this.reserve(bb.remaining());
      this.buffer.put(bb);
   }

   public void writeByte(byte v) throws IOException {
      this.reserve(1);
      this.buffer.put(v);
   }

   public void writeShort(short v) throws IOException {
      this.reserve(2);
      this.buffer.putShort(v);
   }

   public void writeInt(int v) throws IOException {
      this.reserve(4);
      this.buffer.putInt(v);
   }

   public void writeLong(long v) throws IOException {
      this.reserve(8);
      this.buffer.putLong(v);
   }

   public void writeFloat(float v) throws IOException {
      this.reserve(4);
      this.buffer.putFloat(v);
   }

   public void writeDouble(double v) throws IOException {
      this.reserve(8);
      this.buffer.putDouble(v);
   }

   public void writeByteAndByte(byte b, byte v) throws IOException {
      this.reserve(2);
      this.buffer.put(b);
      this.buffer.put(v);
   }

   public void writeByteAndShort(byte b, short v) throws IOException {
      this.reserve(3);
      this.buffer.put(b);
      this.buffer.putShort(v);
   }

   public void writeByteAndInt(byte b, int v) throws IOException {
      this.reserve(5);
      this.buffer.put(b);
      this.buffer.putInt(v);
   }

   public void writeByteAndLong(byte b, long v) throws IOException {
      this.reserve(9);
      this.buffer.put(b);
      this.buffer.putLong(v);
   }

   public void writeByteAndFloat(byte b, float v) throws IOException {
      this.reserve(5);
      this.buffer.put(b);
      this.buffer.putFloat(v);
   }

   public void writeByteAndDouble(byte b, double v) throws IOException {
      this.reserve(9);
      this.buffer.put(b);
      this.buffer.putDouble(v);
   }

   public void flush() throws IOException {
   }

   public void close() {
   }

   public interface ExpandBufferCallback {
      ByteBuffer call(ByteBuffer var1, int var2) throws IOException;
   }
}
