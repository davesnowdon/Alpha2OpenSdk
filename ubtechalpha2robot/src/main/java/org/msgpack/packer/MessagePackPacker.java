package org.msgpack.packer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.io.Output;
import org.msgpack.io.StreamOutput;

public class MessagePackPacker extends AbstractPacker {
   protected final Output out;
   private PackerStack stack;

   public MessagePackPacker(MessagePack msgpack, OutputStream stream) {
      this(msgpack, (Output)(new StreamOutput(stream)));
   }

   protected MessagePackPacker(MessagePack msgpack, Output out) {
      super(msgpack);
      this.stack = new PackerStack();
      this.out = out;
   }

   protected void writeByte(byte d) throws IOException {
      if (d < -32) {
         this.out.writeByteAndByte((byte)-48, d);
      } else {
         this.out.writeByte(d);
      }

      this.stack.reduceCount();
   }

   protected void writeShort(short d) throws IOException {
      if (d < -32) {
         if (d < -128) {
            this.out.writeByteAndShort((byte)-47, d);
         } else {
            this.out.writeByteAndByte((byte)-48, (byte)d);
         }
      } else if (d < 128) {
         this.out.writeByte((byte)d);
      } else if (d < 256) {
         this.out.writeByteAndByte((byte)-52, (byte)d);
      } else {
         this.out.writeByteAndShort((byte)-51, d);
      }

      this.stack.reduceCount();
   }

   protected void writeInt(int d) throws IOException {
      if (d < -32) {
         if (d < -32768) {
            this.out.writeByteAndInt((byte)-46, d);
         } else if (d < -128) {
            this.out.writeByteAndShort((byte)-47, (short)d);
         } else {
            this.out.writeByteAndByte((byte)-48, (byte)d);
         }
      } else if (d < 128) {
         this.out.writeByte((byte)d);
      } else if (d < 256) {
         this.out.writeByteAndByte((byte)-52, (byte)d);
      } else if (d < 65536) {
         this.out.writeByteAndShort((byte)-51, (short)d);
      } else {
         this.out.writeByteAndInt((byte)-50, d);
      }

      this.stack.reduceCount();
   }

   protected void writeLong(long d) throws IOException {
      if (d < -32L) {
         if (d < -32768L) {
            if (d < -2147483648L) {
               this.out.writeByteAndLong((byte)-45, d);
            } else {
               this.out.writeByteAndInt((byte)-46, (int)d);
            }
         } else if (d < -128L) {
            this.out.writeByteAndShort((byte)-47, (short)((int)d));
         } else {
            this.out.writeByteAndByte((byte)-48, (byte)((int)d));
         }
      } else if (d < 128L) {
         this.out.writeByte((byte)((int)d));
      } else if (d < 65536L) {
         if (d < 256L) {
            this.out.writeByteAndByte((byte)-52, (byte)((int)d));
         } else {
            this.out.writeByteAndShort((byte)-51, (short)((int)d));
         }
      } else if (d < 4294967296L) {
         this.out.writeByteAndInt((byte)-50, (int)d);
      } else {
         this.out.writeByteAndLong((byte)-49, d);
      }

      this.stack.reduceCount();
   }

   protected void writeBigInteger(BigInteger d) throws IOException {
      if (d.bitLength() <= 63) {
         this.writeLong(d.longValue());
         this.stack.reduceCount();
      } else {
         if (d.bitLength() != 64 || d.signum() != 1) {
            throw new MessageTypeException("MessagePack can't serialize BigInteger larger than (2^64)-1");
         }

         this.out.writeByteAndLong((byte)-49, d.longValue());
         this.stack.reduceCount();
      }

   }

   protected void writeFloat(float d) throws IOException {
      this.out.writeByteAndFloat((byte)-54, d);
      this.stack.reduceCount();
   }

   protected void writeDouble(double d) throws IOException {
      this.out.writeByteAndDouble((byte)-53, d);
      this.stack.reduceCount();
   }

   protected void writeBoolean(boolean d) throws IOException {
      if (d) {
         this.out.writeByte((byte)-61);
      } else {
         this.out.writeByte((byte)-62);
      }

      this.stack.reduceCount();
   }

   protected void writeByteArray(byte[] b, int off, int len) throws IOException {
      if (len < 32) {
         this.out.writeByte((byte)(160 | len));
      } else if (len < 65536) {
         this.out.writeByteAndShort((byte)-38, (short)len);
      } else {
         this.out.writeByteAndInt((byte)-37, len);
      }

      this.out.write(b, off, len);
      this.stack.reduceCount();
   }

   protected void writeByteBuffer(ByteBuffer bb) throws IOException {
      int len = bb.remaining();
      if (len < 32) {
         this.out.writeByte((byte)(160 | len));
      } else if (len < 65536) {
         this.out.writeByteAndShort((byte)-38, (short)len);
      } else {
         this.out.writeByteAndInt((byte)-37, len);
      }

      int pos = bb.position();

      try {
         this.out.write(bb);
      } finally {
         bb.position(pos);
      }

      this.stack.reduceCount();
   }

   protected void writeString(String s) throws IOException {
      byte[] b;
      try {
         b = s.getBytes("UTF-8");
      } catch (UnsupportedEncodingException var4) {
         throw new MessageTypeException(var4);
      }

      this.writeByteArray(b, 0, b.length);
      this.stack.reduceCount();
   }

   public Packer writeNil() throws IOException {
      this.out.writeByte((byte)-64);
      this.stack.reduceCount();
      return this;
   }

   public Packer writeArrayBegin(int size) throws IOException {
      if (size < 16) {
         this.out.writeByte((byte)(144 | size));
      } else if (size < 65536) {
         this.out.writeByteAndShort((byte)-36, (short)size);
      } else {
         this.out.writeByteAndInt((byte)-35, size);
      }

      this.stack.reduceCount();
      this.stack.pushArray(size);
      return this;
   }

   public Packer writeArrayEnd(boolean check) throws IOException {
      if (!this.stack.topIsArray()) {
         throw new MessageTypeException("writeArrayEnd() is called but writeArrayBegin() is not called");
      } else {
         int remain = this.stack.getTopCount();
         if (remain > 0) {
            if (check) {
               throw new MessageTypeException("writeArrayEnd(check=true) is called but the array is not end: " + remain);
            }

            for(int i = 0; i < remain; ++i) {
               this.writeNil();
            }
         }

         this.stack.pop();
         return this;
      }
   }

   public Packer writeMapBegin(int size) throws IOException {
      if (size < 16) {
         this.out.writeByte((byte)(128 | size));
      } else if (size < 65536) {
         this.out.writeByteAndShort((byte)-34, (short)size);
      } else {
         this.out.writeByteAndInt((byte)-33, size);
      }

      this.stack.reduceCount();
      this.stack.pushMap(size);
      return this;
   }

   public Packer writeMapEnd(boolean check) throws IOException {
      if (!this.stack.topIsMap()) {
         throw new MessageTypeException("writeMapEnd() is called but writeMapBegin() is not called");
      } else {
         int remain = this.stack.getTopCount();
         if (remain > 0) {
            if (check) {
               throw new MessageTypeException("writeMapEnd(check=true) is called but the map is not end: " + remain);
            }

            for(int i = 0; i < remain; ++i) {
               this.writeNil();
            }
         }

         this.stack.pop();
         return this;
      }
   }

   public void reset() {
      this.stack.clear();
   }

   public void flush() throws IOException {
      this.out.flush();
   }

   public void close() throws IOException {
      this.out.close();
   }
}
