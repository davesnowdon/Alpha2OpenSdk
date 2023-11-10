package org.codehaus.jackson.util;

public class BufferRecycler {
   public static final int DEFAULT_WRITE_CONCAT_BUFFER_LEN = 2000;
   protected final byte[][] _byteBuffers = new byte[BufferRecycler.ByteBufferType.values().length][];
   protected final char[][] _charBuffers = new char[BufferRecycler.CharBufferType.values().length][];

   public BufferRecycler() {
   }

   public final byte[] allocByteBuffer(BufferRecycler.ByteBufferType type) {
      int ix = type.ordinal();
      byte[] buffer = this._byteBuffers[ix];
      if (buffer == null) {
         buffer = this.balloc(type.size);
      } else {
         this._byteBuffers[ix] = null;
      }

      return buffer;
   }

   public final void releaseByteBuffer(BufferRecycler.ByteBufferType type, byte[] buffer) {
      this._byteBuffers[type.ordinal()] = buffer;
   }

   public final char[] allocCharBuffer(BufferRecycler.CharBufferType type) {
      return this.allocCharBuffer(type, 0);
   }

   public final char[] allocCharBuffer(BufferRecycler.CharBufferType type, int minSize) {
      if (type.size > minSize) {
         minSize = type.size;
      }

      int ix = type.ordinal();
      char[] buffer = this._charBuffers[ix];
      if (buffer != null && buffer.length >= minSize) {
         this._charBuffers[ix] = null;
      } else {
         buffer = this.calloc(minSize);
      }

      return buffer;
   }

   public final void releaseCharBuffer(BufferRecycler.CharBufferType type, char[] buffer) {
      this._charBuffers[type.ordinal()] = buffer;
   }

   private final byte[] balloc(int size) {
      return new byte[size];
   }

   private final char[] calloc(int size) {
      return new char[size];
   }

   public static enum CharBufferType {
      TOKEN_BUFFER(2000),
      CONCAT_BUFFER(2000),
      TEXT_BUFFER(200),
      NAME_COPY_BUFFER(200);

      private final int size;

      private CharBufferType(int size) {
         this.size = size;
      }
   }

   public static enum ByteBufferType {
      READ_IO_BUFFER(4000),
      WRITE_ENCODING_BUFFER(4000),
      WRITE_CONCAT_BUFFER(2000);

      private final int size;

      private ByteBufferType(int size) {
         this.size = size;
      }
   }
}
