package org.codehaus.jackson.io;

import java.io.IOException;
import java.io.InputStream;

public final class MergedStream extends InputStream {
   protected final IOContext _context;
   final InputStream _in;
   byte[] _buffer;
   int _ptr;
   final int _end;

   public MergedStream(IOContext context, InputStream in, byte[] buf, int start, int end) {
      this._context = context;
      this._in = in;
      this._buffer = buf;
      this._ptr = start;
      this._end = end;
   }

   public int available() throws IOException {
      return this._buffer != null ? this._end - this._ptr : this._in.available();
   }

   public void close() throws IOException {
      this.freeMergedBuffer();
      this._in.close();
   }

   public void mark(int readlimit) {
      if (this._buffer == null) {
         this._in.mark(readlimit);
      }

   }

   public boolean markSupported() {
      return this._buffer == null && this._in.markSupported();
   }

   public int read() throws IOException {
      if (this._buffer != null) {
         int c = this._buffer[this._ptr++] & 255;
         if (this._ptr >= this._end) {
            this.freeMergedBuffer();
         }

         return c;
      } else {
         return this._in.read();
      }
   }

   public int read(byte[] b) throws IOException {
      return this.read(b, 0, b.length);
   }

   public int read(byte[] b, int off, int len) throws IOException {
      if (this._buffer != null) {
         int avail = this._end - this._ptr;
         if (len > avail) {
            len = avail;
         }

         System.arraycopy(this._buffer, this._ptr, b, off, len);
         this._ptr += len;
         if (this._ptr >= this._end) {
            this.freeMergedBuffer();
         }

         return len;
      } else {
         return this._in.read(b, off, len);
      }
   }

   public void reset() throws IOException {
      if (this._buffer == null) {
         this._in.reset();
      }

   }

   public long skip(long n) throws IOException {
      long count = 0L;
      if (this._buffer != null) {
         int amount = this._end - this._ptr;
         if ((long)amount > n) {
            this._ptr += (int)n;
            return n;
         }

         this.freeMergedBuffer();
         count += (long)amount;
         n -= (long)amount;
      }

      if (n > 0L) {
         count += this._in.skip(n);
      }

      return count;
   }

   private void freeMergedBuffer() {
      byte[] buf = this._buffer;
      if (buf != null) {
         this._buffer = null;
         if (this._context != null) {
            this._context.releaseReadIOBuffer(buf);
         }
      }

   }
}
