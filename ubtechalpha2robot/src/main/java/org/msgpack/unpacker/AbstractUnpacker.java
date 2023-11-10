package org.msgpack.unpacker;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.msgpack.MessagePack;
import org.msgpack.packer.Unconverter;
import org.msgpack.template.Template;
import org.msgpack.type.Value;

public abstract class AbstractUnpacker implements Unpacker {
   protected MessagePack msgpack;
   protected int rawSizeLimit = 134217728;
   protected int arraySizeLimit = 4194304;
   protected int mapSizeLimit = 2097152;

   protected AbstractUnpacker(MessagePack msgpack) {
      this.msgpack = msgpack;
   }

   public ByteBuffer readByteBuffer() throws IOException {
      return ByteBuffer.wrap(this.readByteArray());
   }

   public void readArrayEnd() throws IOException {
      this.readArrayEnd(false);
   }

   public void readMapEnd() throws IOException {
      this.readMapEnd(false);
   }

   public UnpackerIterator iterator() {
      return new UnpackerIterator(this);
   }

   protected abstract void readValue(Unconverter var1) throws IOException;

   public Value readValue() throws IOException {
      Unconverter uc = new Unconverter(this.msgpack);
      this.readValue(uc);
      return uc.getResult();
   }

   protected abstract boolean tryReadNil() throws IOException;

   public <T> T read(Class<T> klass) throws IOException {
      if (this.tryReadNil()) {
         return null;
      } else {
         Template<T> tmpl = this.msgpack.lookup(klass);
         return tmpl.read(this, (Object)null);
      }
   }

   public <T> T read(T to) throws IOException {
      if (this.tryReadNil()) {
         return null;
      } else {
         Template<T> tmpl = this.msgpack.lookup(to.getClass());
         return tmpl.read(this, to);
      }
   }

   public <T> T read(Template<T> tmpl) throws IOException {
      return this.tryReadNil() ? null : tmpl.read(this, (Object)null);
   }

   public <T> T read(T to, Template<T> tmpl) throws IOException {
      return this.tryReadNil() ? null : tmpl.read(this, to);
   }

   public int getReadByteCount() {
      throw new UnsupportedOperationException("Not implemented");
   }

   public void resetReadByteCount() {
      throw new UnsupportedOperationException("Not implemented");
   }

   public void setRawSizeLimit(int size) {
      if (size < 32) {
         this.rawSizeLimit = 32;
      } else {
         this.rawSizeLimit = size;
      }

   }

   public void setArraySizeLimit(int size) {
      if (size < 16) {
         this.arraySizeLimit = 16;
      } else {
         this.arraySizeLimit = size;
      }

   }

   public void setMapSizeLimit(int size) {
      if (size < 16) {
         this.mapSizeLimit = 16;
      } else {
         this.mapSizeLimit = size;
      }

   }
}
