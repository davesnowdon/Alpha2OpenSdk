package org.codehaus.jackson.impl;

import java.io.IOException;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.util.TextBuffer;
import org.codehaus.jackson.util.VersionUtil;

public abstract class JsonParserBase extends JsonParserMinimalBase {
   protected final IOContext _ioContext;
   protected boolean _closed;
   protected int _inputPtr = 0;
   protected int _inputEnd = 0;
   protected long _currInputProcessed = 0L;
   protected int _currInputRow = 1;
   protected int _currInputRowStart = 0;
   protected long _tokenInputTotal = 0L;
   protected int _tokenInputRow = 1;
   protected int _tokenInputCol = 0;
   protected JsonReadContext _parsingContext;
   protected JsonToken _nextToken;
   protected final TextBuffer _textBuffer;
   protected char[] _nameCopyBuffer = null;
   protected boolean _nameCopied = false;
   protected ByteArrayBuilder _byteArrayBuilder = null;
   protected byte[] _binaryValue;

   protected JsonParserBase(IOContext ctxt, int features) {
      this._features = features;
      this._ioContext = ctxt;
      this._textBuffer = ctxt.constructTextBuffer();
      this._parsingContext = JsonReadContext.createRootContext(this._tokenInputRow, this._tokenInputCol);
   }

   public Version version() {
      return VersionUtil.versionFor(this.getClass());
   }

   public String getCurrentName() throws IOException, JsonParseException {
      if (this._currToken != JsonToken.START_OBJECT && this._currToken != JsonToken.START_ARRAY) {
         return this._parsingContext.getCurrentName();
      } else {
         JsonReadContext parent = this._parsingContext.getParent();
         return parent.getCurrentName();
      }
   }

   public void close() throws IOException {
      if (!this._closed) {
         this._closed = true;

         try {
            this._closeInput();
         } finally {
            this._releaseBuffers();
         }
      }

   }

   public boolean isClosed() {
      return this._closed;
   }

   public JsonReadContext getParsingContext() {
      return this._parsingContext;
   }

   public JsonLocation getTokenLocation() {
      return new JsonLocation(this._ioContext.getSourceReference(), this.getTokenCharacterOffset(), this.getTokenLineNr(), this.getTokenColumnNr());
   }

   public JsonLocation getCurrentLocation() {
      int col = this._inputPtr - this._currInputRowStart + 1;
      return new JsonLocation(this._ioContext.getSourceReference(), this._currInputProcessed + (long)this._inputPtr - 1L, this._currInputRow, col);
   }

   public boolean hasTextCharacters() {
      if (this._currToken != null) {
         switch(this._currToken) {
         case FIELD_NAME:
            return this._nameCopied;
         case VALUE_STRING:
            return true;
         }
      }

      return false;
   }

   public final long getTokenCharacterOffset() {
      return this._tokenInputTotal;
   }

   public final int getTokenLineNr() {
      return this._tokenInputRow;
   }

   public final int getTokenColumnNr() {
      return this._tokenInputCol + 1;
   }

   protected final void loadMoreGuaranteed() throws IOException {
      if (!this.loadMore()) {
         this._reportInvalidEOF();
      }

   }

   protected abstract boolean loadMore() throws IOException;

   protected abstract void _finishString() throws IOException, JsonParseException;

   protected abstract void _closeInput() throws IOException;

   protected abstract byte[] _decodeBase64(Base64Variant var1) throws IOException, JsonParseException;

   protected void _releaseBuffers() throws IOException {
      this._textBuffer.releaseBuffers();
      char[] buf = this._nameCopyBuffer;
      if (buf != null) {
         this._nameCopyBuffer = null;
         this._ioContext.releaseNameCopyBuffer(buf);
      }

   }

   protected void _handleEOF() throws JsonParseException {
      if (!this._parsingContext.inRoot()) {
         this._reportInvalidEOF(": expected close marker for " + this._parsingContext.getTypeDesc() + " (from " + this._parsingContext.getStartLocation(this._ioContext.getSourceReference()) + ")");
      }

   }

   protected void _reportMismatchedEndMarker(int actCh, char expCh) throws JsonParseException {
      String startDesc = "" + this._parsingContext.getStartLocation(this._ioContext.getSourceReference());
      this._reportError("Unexpected close marker '" + (char)actCh + "': expected '" + expCh + "' (for " + this._parsingContext.getTypeDesc() + " starting at " + startDesc + ")");
   }

   public ByteArrayBuilder _getByteArrayBuilder() {
      if (this._byteArrayBuilder == null) {
         this._byteArrayBuilder = new ByteArrayBuilder();
      } else {
         this._byteArrayBuilder.reset();
      }

      return this._byteArrayBuilder;
   }
}
