package org.msgpack.unpacker;

import java.nio.ByteBuffer;
import org.msgpack.MessagePack;
import org.msgpack.io.Input;
import org.msgpack.io.LinkedBufferInput;

public class MessagePackBufferUnpacker extends MessagePackUnpacker implements BufferUnpacker {
   private static final int DEFAULT_BUFFER_SIZE = 512;

   public MessagePackBufferUnpacker(MessagePack msgpack) {
      this(msgpack, 512);
   }

   public MessagePackBufferUnpacker(MessagePack msgpack, int bufferSize) {
      super(msgpack, (Input)(new LinkedBufferInput(bufferSize)));
   }

   public MessagePackBufferUnpacker wrap(byte[] b) {
      return this.wrap(b, 0, b.length);
   }

   public MessagePackBufferUnpacker wrap(byte[] b, int off, int len) {
      ((LinkedBufferInput)this.in).clear();
      ((LinkedBufferInput)this.in).feed(b, off, len, true);
      return this;
   }

   public MessagePackBufferUnpacker wrap(ByteBuffer buf) {
      ((LinkedBufferInput)this.in).clear();
      ((LinkedBufferInput)this.in).feed(buf, true);
      return this;
   }

   public MessagePackBufferUnpacker feed(byte[] b) {
      ((LinkedBufferInput)this.in).feed(b);
      return this;
   }

   public MessagePackBufferUnpacker feed(byte[] b, boolean reference) {
      ((LinkedBufferInput)this.in).feed(b, reference);
      return this;
   }

   public MessagePackBufferUnpacker feed(byte[] b, int off, int len) {
      ((LinkedBufferInput)this.in).feed(b, off, len);
      return this;
   }

   public MessagePackBufferUnpacker feed(byte[] b, int off, int len, boolean reference) {
      ((LinkedBufferInput)this.in).feed(b, off, len, reference);
      return this;
   }

   public MessagePackBufferUnpacker feed(ByteBuffer b) {
      ((LinkedBufferInput)this.in).feed(b);
      return this;
   }

   public MessagePackBufferUnpacker feed(ByteBuffer buf, boolean reference) {
      ((LinkedBufferInput)this.in).feed(buf, reference);
      return this;
   }

   public int getBufferSize() {
      return ((LinkedBufferInput)this.in).getSize();
   }

   public void copyReferencedBuffer() {
      ((LinkedBufferInput)this.in).copyReferencedBuffer();
   }

   public void clear() {
      ((LinkedBufferInput)this.in).clear();
      this.reset();
   }
}
