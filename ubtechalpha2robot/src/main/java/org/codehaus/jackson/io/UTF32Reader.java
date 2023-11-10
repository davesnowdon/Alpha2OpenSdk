package org.codehaus.jackson.io;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;

public final class UTF32Reader extends BaseReader {
   final boolean mBigEndian;
   char mSurrogate = 0;
   int mCharCount = 0;
   int mByteCount = 0;

   public UTF32Reader(IOContext ctxt, InputStream in, byte[] buf, int ptr, int len, boolean isBigEndian) {
      super(ctxt, in, buf, ptr, len);
      this.mBigEndian = isBigEndian;
   }

   public int read(char[] cbuf, int start, int len) throws IOException {
      if (this._buffer == null) {
         return -1;
      } else if (len < 1) {
         return len;
      } else {
         if (start < 0 || start + len > cbuf.length) {
            this.reportBounds(cbuf, start, len);
         }

         len += start;
         int outPtr = start;
         int ptr;
         if (this.mSurrogate != 0) {
            outPtr = start + 1;
            cbuf[start] = this.mSurrogate;
            this.mSurrogate = 0;
         } else {
            ptr = this._length - this._ptr;
            if (ptr < 4 && !this.loadMore(ptr)) {
               return -1;
            }
         }

         while(outPtr < len) {
            ptr = this._ptr;
            int ch;
            if (this.mBigEndian) {
               ch = this._buffer[ptr] << 24 | (this._buffer[ptr + 1] & 255) << 16 | (this._buffer[ptr + 2] & 255) << 8 | this._buffer[ptr + 3] & 255;
            } else {
               ch = this._buffer[ptr] & 255 | (this._buffer[ptr + 1] & 255) << 8 | (this._buffer[ptr + 2] & 255) << 16 | this._buffer[ptr + 3] << 24;
            }

            this._ptr += 4;
            if (ch > 65535) {
               if (ch > 1114111) {
                  this.reportInvalid(ch, outPtr - start, "(above " + Integer.toHexString(1114111) + ") ");
               }

               ch -= 65536;
               cbuf[outPtr++] = (char)('\ud800' + (ch >> 10));
               ch = '\udc00' | ch & 1023;
               if (outPtr >= len) {
                  this.mSurrogate = (char)ch;
                  break;
               }
            }

            cbuf[outPtr++] = (char)ch;
            if (this._ptr >= this._length) {
               break;
            }
         }

         len = outPtr - start;
         this.mCharCount += len;
         return len;
      }
   }

   private void reportUnexpectedEOF(int gotBytes, int needed) throws IOException {
      int bytePos = this.mByteCount + gotBytes;
      int charPos = this.mCharCount;
      throw new CharConversionException("Unexpected EOF in the middle of a 4-byte UTF-32 char: got " + gotBytes + ", needed " + needed + ", at char #" + charPos + ", byte #" + bytePos + ")");
   }

   private void reportInvalid(int value, int offset, String msg) throws IOException {
      int bytePos = this.mByteCount + this._ptr - 1;
      int charPos = this.mCharCount + offset;
      throw new CharConversionException("Invalid UTF-32 character 0x" + Integer.toHexString(value) + msg + " at char #" + charPos + ", byte #" + bytePos + ")");
   }

   private boolean loadMore(int available) throws IOException {
      this.mByteCount += this._length - available;
      int count;
      if (available > 0) {
         if (this._ptr > 0) {
            for(count = 0; count < available; ++count) {
               this._buffer[count] = this._buffer[this._ptr + count];
            }

            this._ptr = 0;
         }

         this._length = available;
      } else {
         this._ptr = 0;
         count = this._in.read(this._buffer);
         if (count < 1) {
            this._length = 0;
            if (count < 0) {
               this.freeBuffers();
               return false;
            }

            this.reportStrangeStream();
         }

         this._length = count;
      }

      for(; this._length < 4; this._length += count) {
         count = this._in.read(this._buffer, this._length, this._buffer.length - this._length);
         if (count < 1) {
            if (count < 0) {
               this.freeBuffers();
               this.reportUnexpectedEOF(this._length, 4);
            }

            this.reportStrangeStream();
         }
      }

      return true;
   }
}
