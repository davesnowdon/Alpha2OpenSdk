package org.msgpack.packer;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.type.Value;

public abstract class AbstractPacker implements Packer {
   protected MessagePack msgpack;

   protected AbstractPacker(MessagePack msgpack) {
      this.msgpack = msgpack;
   }

   public Packer write(boolean o) throws IOException {
      this.writeBoolean(o);
      return this;
   }

   public Packer write(byte o) throws IOException {
      this.writeByte(o);
      return this;
   }

   public Packer write(short o) throws IOException {
      this.writeShort(o);
      return this;
   }

   public Packer write(int o) throws IOException {
      this.writeInt(o);
      return this;
   }

   public Packer write(long o) throws IOException {
      this.writeLong(o);
      return this;
   }

   public Packer write(float o) throws IOException {
      this.writeFloat(o);
      return this;
   }

   public Packer write(double o) throws IOException {
      this.writeDouble(o);
      return this;
   }

   public Packer write(Boolean o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeBoolean(o);
      }

      return this;
   }

   public Packer write(Byte o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeByte(o);
      }

      return this;
   }

   public Packer write(Short o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeShort(o);
      }

      return this;
   }

   public Packer write(Integer o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeInt(o);
      }

      return this;
   }

   public Packer write(Long o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeLong(o);
      }

      return this;
   }

   public Packer write(BigInteger o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeBigInteger(o);
      }

      return this;
   }

   public Packer write(Float o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeFloat(o);
      }

      return this;
   }

   public Packer write(Double o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeDouble(o);
      }

      return this;
   }

   public Packer write(byte[] o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeByteArray(o);
      }

      return this;
   }

   public Packer write(byte[] o, int off, int len) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeByteArray(o, off, len);
      }

      return this;
   }

   public Packer write(ByteBuffer o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeByteBuffer(o);
      }

      return this;
   }

   public Packer write(String o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         this.writeString(o);
      }

      return this;
   }

   public Packer write(Object o) throws IOException {
      if (o == null) {
         this.writeNil();
      } else {
         Template tmpl = this.msgpack.lookup(o.getClass());
         tmpl.write(this, o);
      }

      return this;
   }

   public Packer write(Value v) throws IOException {
      if (v == null) {
         this.writeNil();
      } else {
         v.writeTo(this);
      }

      return this;
   }

   public Packer writeArrayEnd() throws IOException {
      this.writeArrayEnd(true);
      return this;
   }

   public Packer writeMapEnd() throws IOException {
      this.writeMapEnd(true);
      return this;
   }

   public void close() throws IOException {
   }

   protected abstract void writeBoolean(boolean var1) throws IOException;

   protected abstract void writeByte(byte var1) throws IOException;

   protected abstract void writeShort(short var1) throws IOException;

   protected abstract void writeInt(int var1) throws IOException;

   protected abstract void writeLong(long var1) throws IOException;

   protected abstract void writeBigInteger(BigInteger var1) throws IOException;

   protected abstract void writeFloat(float var1) throws IOException;

   protected abstract void writeDouble(double var1) throws IOException;

   protected void writeByteArray(byte[] b) throws IOException {
      this.writeByteArray(b, 0, b.length);
   }

   protected abstract void writeByteArray(byte[] var1, int var2, int var3) throws IOException;

   protected abstract void writeByteBuffer(ByteBuffer var1) throws IOException;

   protected abstract void writeString(String var1) throws IOException;
}
