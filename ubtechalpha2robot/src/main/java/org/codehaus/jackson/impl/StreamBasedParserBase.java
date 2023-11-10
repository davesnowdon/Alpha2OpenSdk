package org.codehaus.jackson.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.io.IOContext;

public abstract class StreamBasedParserBase extends JsonNumericParserBase {
   protected InputStream _inputStream;
   protected byte[] _inputBuffer;
   protected boolean _bufferRecyclable;

   protected StreamBasedParserBase(IOContext ctxt, int features, InputStream in, byte[] inputBuffer, int start, int end, boolean bufferRecyclable) {
      super(ctxt, features);
      this._inputStream = in;
      this._inputBuffer = inputBuffer;
      this._inputPtr = start;
      this._inputEnd = end;
      this._bufferRecyclable = bufferRecyclable;
   }

   public int releaseBuffered(OutputStream out) throws IOException {
      int count = this._inputEnd - this._inputPtr;
      if (count < 1) {
         return 0;
      } else {
         int origPtr = this._inputPtr;
         out.write(this._inputBuffer, origPtr, count);
         return count;
      }
   }

   public Object getInputSource() {
      return this._inputStream;
   }

   protected final boolean loadMore() throws IOException {
      this._currInputProcessed += (long)this._inputEnd;
      this._currInputRowStart -= this._inputEnd;
      if (this._inputStream != null) {
         int count = this._inputStream.read(this._inputBuffer, 0, this._inputBuffer.length);
         if (count > 0) {
            this._inputPtr = 0;
            this._inputEnd = count;
            return true;
         }

         this._closeInput();
         if (count == 0) {
            throw new IOException("InputStream.read() returned 0 characters when trying to read " + this._inputBuffer.length + " bytes");
         }
      }

      return false;
   }

   protected final boolean _loadToHaveAtLeast(int minAvailable) throws IOException {
      if (this._inputStream == null) {
         return false;
      } else {
         int amount = this._inputEnd - this._inputPtr;
         if (amount > 0 && this._inputPtr > 0) {
            this._currInputProcessed += (long)this._inputPtr;
            this._currInputRowStart -= this._inputPtr;
            System.arraycopy(this._inputBuffer, this._inputPtr, this._inputBuffer, 0, amount);
            this._inputEnd = amount;
         } else {
            this._inputEnd = 0;
         }

         int count;
         for(this._inputPtr = 0; this._inputEnd < minAvailable; this._inputEnd += count) {
            count = this._inputStream.read(this._inputBuffer, this._inputEnd, this._inputBuffer.length - this._inputEnd);
            if (count < 1) {
               this._closeInput();
               if (count == 0) {
                  throw new IOException("InputStream.read() returned 0 characters when trying to read " + amount + " bytes");
               }

               return false;
            }
         }

         return true;
      }
   }

   protected void _closeInput() throws IOException {
      if (this._inputStream != null) {
         if (this._ioContext.isResourceManaged() || this.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE)) {
            this._inputStream.close();
         }

         this._inputStream = null;
      }

   }

   protected void _releaseBuffers() throws IOException {
      super._releaseBuffers();
      if (this._bufferRecyclable) {
         byte[] buf = this._inputBuffer;
         if (buf != null) {
            this._inputBuffer = null;
            this._ioContext.releaseReadIOBuffer(buf);
         }
      }

   }
}
