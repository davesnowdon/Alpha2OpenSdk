package org.msgpack.util.json;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import org.msgpack.MessagePack;
import org.msgpack.unpacker.BufferUnpacker;

public class JSONBufferUnpacker extends JSONUnpacker implements BufferUnpacker {
   private static final int DEFAULT_BUFFER_SIZE = 512;

   public JSONBufferUnpacker() {
      this(512);
   }

   public JSONBufferUnpacker(int bufferSize) {
      this(new MessagePack(), bufferSize);
   }

   public JSONBufferUnpacker(MessagePack msgpack) {
      this(msgpack, 512);
   }

   public JSONBufferUnpacker(MessagePack msgpack, int bufferSize) {
      super(msgpack, newEmptyReader());
   }

   public JSONBufferUnpacker wrap(byte[] b) {
      return this.wrap(b, 0, b.length);
   }

   public JSONBufferUnpacker wrap(byte[] b, int off, int len) {
      ByteArrayInputStream in = new ByteArrayInputStream(b, off, len);
      this.in = new InputStreamReader(in);
      return this;
   }

   public JSONBufferUnpacker wrap(ByteBuffer buf) {
      throw new UnsupportedOperationException("JSONBufferUnpacker doesn't support wrap(ByteBuffer buf)");
   }

   public JSONBufferUnpacker feed(byte[] b) {
      throw new UnsupportedOperationException("JSONBufferUnpacker doesn't support feed()");
   }

   public JSONBufferUnpacker feed(byte[] b, boolean reference) {
      throw new UnsupportedOperationException("JSONBufferUnpacker doesn't support feed()");
   }

   public JSONBufferUnpacker feed(byte[] b, int off, int len) {
      throw new UnsupportedOperationException("JSONBufferUnpacker doesn't support feed()");
   }

   public JSONBufferUnpacker feed(byte[] b, int off, int len, boolean reference) {
      throw new UnsupportedOperationException("JSONBufferUnpacker doesn't support feed()");
   }

   public JSONBufferUnpacker feed(ByteBuffer buf) {
      throw new UnsupportedOperationException("JSONBufferUnpacker doesn't support feed()");
   }

   public JSONBufferUnpacker feed(ByteBuffer buf, boolean reference) {
      throw new UnsupportedOperationException("JSONBufferUnpacker doesn't support feed()");
   }

   public int getBufferSize() {
      throw new UnsupportedOperationException("JSONBufferUnpacker doesn't support getBufferSize()");
   }

   public void copyReferencedBuffer() {
      throw new UnsupportedOperationException("JSONBufferUnpacker doesn't support copyReferencedBuffer()");
   }

   public void clear() {
      this.reset();
      this.in = newEmptyReader();
   }

   private static Reader newEmptyReader() {
      return new InputStreamReader(new ByteArrayInputStream(new byte[0]));
   }
}
