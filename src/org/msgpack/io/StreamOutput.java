package org.msgpack.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class StreamOutput implements Output {
   private DataOutputStream out;

   public StreamOutput(OutputStream out) {
      this.out = new DataOutputStream(out);
   }

   public void write(byte[] b, int off, int len) throws IOException {
      this.out.write(b, off, len);
   }

   public void write(ByteBuffer bb) throws IOException {
      byte[] array;
      if (bb.hasArray()) {
         array = bb.array();
         int offset = bb.arrayOffset();
         this.out.write(array, offset, bb.remaining());
         bb.position(bb.limit());
      } else {
         array = new byte[bb.remaining()];
         bb.get(array);
         this.out.write(array);
      }

   }

   public void writeByte(byte v) throws IOException {
      this.out.write(v);
   }

   public void writeShort(short v) throws IOException {
      this.out.writeShort(v);
   }

   public void writeInt(int v) throws IOException {
      this.out.writeInt(v);
   }

   public void writeLong(long v) throws IOException {
      this.out.writeLong(v);
   }

   public void writeFloat(float v) throws IOException {
      this.out.writeFloat(v);
   }

   public void writeDouble(double v) throws IOException {
      this.out.writeDouble(v);
   }

   public void writeByteAndByte(byte b, byte v) throws IOException {
      this.out.write(b);
      this.out.write(v);
   }

   public void writeByteAndShort(byte b, short v) throws IOException {
      this.out.write(b);
      this.out.writeShort(v);
   }

   public void writeByteAndInt(byte b, int v) throws IOException {
      this.out.write(b);
      this.out.writeInt(v);
   }

   public void writeByteAndLong(byte b, long v) throws IOException {
      this.out.write(b);
      this.out.writeLong(v);
   }

   public void writeByteAndFloat(byte b, float v) throws IOException {
      this.out.write(b);
      this.out.writeFloat(v);
   }

   public void writeByteAndDouble(byte b, double v) throws IOException {
      this.out.write(b);
      this.out.writeDouble(v);
   }

   public void flush() throws IOException {
   }

   public void close() throws IOException {
      this.out.close();
   }
}
