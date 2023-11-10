package org.codehaus.jackson.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.io.IOContext;

public abstract class ReaderBasedParserBase extends JsonNumericParserBase {
   protected Reader _reader;
   protected char[] _inputBuffer;

   protected ReaderBasedParserBase(IOContext ctxt, int features, Reader r) {
      super(ctxt, features);
      this._reader = r;
      this._inputBuffer = ctxt.allocTokenBuffer();
   }

   public int releaseBuffered(Writer w) throws IOException {
      int count = this._inputEnd - this._inputPtr;
      if (count < 1) {
         return 0;
      } else {
         int origPtr = this._inputPtr;
         w.write(this._inputBuffer, origPtr, count);
         return count;
      }
   }

   public Object getInputSource() {
      return this._reader;
   }

   protected final boolean loadMore() throws IOException {
      this._currInputProcessed += (long)this._inputEnd;
      this._currInputRowStart -= this._inputEnd;
      if (this._reader != null) {
         int count = this._reader.read(this._inputBuffer, 0, this._inputBuffer.length);
         if (count > 0) {
            this._inputPtr = 0;
            this._inputEnd = count;
            return true;
         }

         this._closeInput();
         if (count == 0) {
            throw new IOException("Reader returned 0 characters when trying to read " + this._inputEnd);
         }
      }

      return false;
   }

   protected char getNextChar(String eofMsg) throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
         this._reportInvalidEOF(eofMsg);
      }

      return this._inputBuffer[this._inputPtr++];
   }

   protected void _closeInput() throws IOException {
      if (this._reader != null) {
         if (this._ioContext.isResourceManaged() || this.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE)) {
            this._reader.close();
         }

         this._reader = null;
      }

   }

   protected void _releaseBuffers() throws IOException {
      super._releaseBuffers();
      char[] buf = this._inputBuffer;
      if (buf != null) {
         this._inputBuffer = null;
         this._ioContext.releaseTokenBuffer(buf);
      }

   }

   protected final boolean _matchToken(String matchStr, int i) throws IOException, JsonParseException {
      int len = matchStr.length();

      do {
         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOFInValue();
         }

         if (this._inputBuffer[this._inputPtr] != matchStr.charAt(i)) {
            this._reportInvalidToken(matchStr.substring(0, i), "'null', 'true', 'false' or NaN");
         }

         ++this._inputPtr;
         ++i;
      } while(i < len);

      if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
         return true;
      } else {
         char c = this._inputBuffer[this._inputPtr];
         if (Character.isJavaIdentifierPart(c)) {
            ++this._inputPtr;
            this._reportInvalidToken(matchStr.substring(0, i), "'null', 'true', 'false' or NaN");
         }

         return true;
      }
   }

   protected void _reportInvalidToken(String matchedPart, String msg) throws IOException, JsonParseException {
      StringBuilder sb = new StringBuilder(matchedPart);

      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         char c = this._inputBuffer[this._inputPtr];
         if (!Character.isJavaIdentifierPart(c)) {
            break;
         }

         ++this._inputPtr;
         sb.append(c);
      }

      this._reportError("Unrecognized token '" + sb.toString() + "': was expecting ");
   }
}
