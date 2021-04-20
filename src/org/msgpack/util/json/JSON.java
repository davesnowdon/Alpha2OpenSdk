package org.msgpack.util.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.BufferUnpacker;
import org.msgpack.unpacker.Unpacker;

public class JSON extends MessagePack {
   public JSON() {
   }

   public JSON(MessagePack msgpack) {
      super(msgpack);
   }

   public Packer createPacker(OutputStream stream) {
      return new JSONPacker(this, stream);
   }

   public BufferPacker createBufferPacker() {
      return new JSONBufferPacker(this);
   }

   public BufferPacker createBufferPacker(int bufferSize) {
      return new JSONBufferPacker(this, bufferSize);
   }

   public Unpacker createUnpacker(InputStream stream) {
      return new JSONUnpacker(this, stream);
   }

   public BufferUnpacker createBufferUnpacker() {
      return new JSONBufferUnpacker();
   }

   public BufferUnpacker createBufferUnpacker(byte[] b) {
      return this.createBufferUnpacker().wrap(b);
   }

   public BufferUnpacker createBufferUnpacker(byte[] b, int off, int len) {
      return this.createBufferUnpacker().wrap(b, off, len);
   }

   public BufferUnpacker createBufferUnpacker(ByteBuffer bb) {
      return this.createBufferUnpacker().wrap(bb);
   }
}
