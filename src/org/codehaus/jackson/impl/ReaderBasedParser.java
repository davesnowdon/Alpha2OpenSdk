package org.codehaus.jackson.impl;

import java.io.IOException;
import java.io.Reader;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.sym.CharsToNameCanonicalizer;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.util.CharTypes;
import org.codehaus.jackson.util.TextBuffer;

public final class ReaderBasedParser extends ReaderBasedNumericParser {
   protected ObjectCodec _objectCodec;
   protected final CharsToNameCanonicalizer _symbols;
   protected boolean _tokenIncomplete = false;

   public ReaderBasedParser(IOContext ioCtxt, int features, Reader r, ObjectCodec codec, CharsToNameCanonicalizer st) {
      super(ioCtxt, features, r);
      this._objectCodec = codec;
      this._symbols = st;
   }

   public ObjectCodec getCodec() {
      return this._objectCodec;
   }

   public void setCodec(ObjectCodec c) {
      this._objectCodec = c;
   }

   public final String getText() throws IOException, JsonParseException {
      JsonToken t = this._currToken;
      if (t == JsonToken.VALUE_STRING) {
         if (this._tokenIncomplete) {
            this._tokenIncomplete = false;
            this._finishString();
         }

         return this._textBuffer.contentsAsString();
      } else {
         return this._getText2(t);
      }
   }

   protected final String _getText2(JsonToken t) {
      if (t == null) {
         return null;
      } else {
         switch(t) {
         case FIELD_NAME:
            return this._parsingContext.getCurrentName();
         case VALUE_STRING:
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return this._textBuffer.contentsAsString();
         default:
            return t.asString();
         }
      }
   }

   public char[] getTextCharacters() throws IOException, JsonParseException {
      if (this._currToken != null) {
         switch(this._currToken) {
         case FIELD_NAME:
            if (!this._nameCopied) {
               String name = this._parsingContext.getCurrentName();
               int nameLen = name.length();
               if (this._nameCopyBuffer == null) {
                  this._nameCopyBuffer = this._ioContext.allocNameCopyBuffer(nameLen);
               } else if (this._nameCopyBuffer.length < nameLen) {
                  this._nameCopyBuffer = new char[nameLen];
               }

               name.getChars(0, nameLen, this._nameCopyBuffer, 0);
               this._nameCopied = true;
            }

            return this._nameCopyBuffer;
         case VALUE_STRING:
            if (this._tokenIncomplete) {
               this._tokenIncomplete = false;
               this._finishString();
            }
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return this._textBuffer.getTextBuffer();
         default:
            return this._currToken.asCharArray();
         }
      } else {
         return null;
      }
   }

   public int getTextLength() throws IOException, JsonParseException {
      if (this._currToken != null) {
         switch(this._currToken) {
         case FIELD_NAME:
            return this._parsingContext.getCurrentName().length();
         case VALUE_STRING:
            if (this._tokenIncomplete) {
               this._tokenIncomplete = false;
               this._finishString();
            }
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return this._textBuffer.size();
         default:
            return this._currToken.asCharArray().length;
         }
      } else {
         return 0;
      }
   }

   public int getTextOffset() throws IOException, JsonParseException {
      if (this._currToken != null) {
         switch(this._currToken) {
         case FIELD_NAME:
            return 0;
         case VALUE_STRING:
            if (this._tokenIncomplete) {
               this._tokenIncomplete = false;
               this._finishString();
            }
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return this._textBuffer.getTextOffset();
         }
      }

      return 0;
   }

   public byte[] getBinaryValue(Base64Variant b64variant) throws IOException, JsonParseException {
      if (this._currToken != JsonToken.VALUE_STRING && (this._currToken != JsonToken.VALUE_EMBEDDED_OBJECT || this._binaryValue == null)) {
         this._reportError("Current token (" + this._currToken + ") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
      }

      if (this._tokenIncomplete) {
         try {
            this._binaryValue = this._decodeBase64(b64variant);
         } catch (IllegalArgumentException var3) {
            throw this._constructError("Failed to decode VALUE_STRING as base64 (" + b64variant + "): " + var3.getMessage());
         }

         this._tokenIncomplete = false;
      }

      return this._binaryValue;
   }

   public JsonToken nextToken() throws IOException, JsonParseException {
      if (this._currToken == JsonToken.FIELD_NAME) {
         return this._nextAfterName();
      } else {
         if (this._tokenIncomplete) {
            this._skipString();
         }

         int i = this._skipWSOrEnd();
         if (i < 0) {
            this.close();
            return this._currToken = null;
         } else {
            this._tokenInputTotal = this._currInputProcessed + (long)this._inputPtr - 1L;
            this._tokenInputRow = this._currInputRow;
            this._tokenInputCol = this._inputPtr - this._currInputRowStart - 1;
            this._binaryValue = null;
            if (i == 93) {
               if (!this._parsingContext.inArray()) {
                  this._reportMismatchedEndMarker(i, '}');
               }

               this._parsingContext = this._parsingContext.getParent();
               return this._currToken = JsonToken.END_ARRAY;
            } else if (i == 125) {
               if (!this._parsingContext.inObject()) {
                  this._reportMismatchedEndMarker(i, ']');
               }

               this._parsingContext = this._parsingContext.getParent();
               return this._currToken = JsonToken.END_OBJECT;
            } else {
               if (this._parsingContext.expectComma()) {
                  if (i != 44) {
                     this._reportUnexpectedChar(i, "was expecting comma to separate " + this._parsingContext.getTypeDesc() + " entries");
                  }

                  i = this._skipWS();
               }

               boolean inObject = this._parsingContext.inObject();
               if (inObject) {
                  String name = this._parseFieldName(i);
                  this._parsingContext.setCurrentName(name);
                  this._currToken = JsonToken.FIELD_NAME;
                  i = this._skipWS();
                  if (i != 58) {
                     this._reportUnexpectedChar(i, "was expecting a colon to separate field name and value");
                  }

                  i = this._skipWS();
               }

               JsonToken t;
               switch(i) {
               case 34:
                  this._tokenIncomplete = true;
                  t = JsonToken.VALUE_STRING;
                  break;
               case 45:
               case 48:
               case 49:
               case 50:
               case 51:
               case 52:
               case 53:
               case 54:
               case 55:
               case 56:
               case 57:
                  t = this.parseNumberText(i);
                  break;
               case 91:
                  if (!inObject) {
                     this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                  }

                  t = JsonToken.START_ARRAY;
                  break;
               case 93:
               case 125:
                  this._reportUnexpectedChar(i, "expected a value");
               case 116:
                  this._matchToken(JsonToken.VALUE_TRUE);
                  t = JsonToken.VALUE_TRUE;
                  break;
               case 102:
                  this._matchToken(JsonToken.VALUE_FALSE);
                  t = JsonToken.VALUE_FALSE;
                  break;
               case 110:
                  this._matchToken(JsonToken.VALUE_NULL);
                  t = JsonToken.VALUE_NULL;
                  break;
               case 123:
                  if (!inObject) {
                     this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                  }

                  t = JsonToken.START_OBJECT;
                  break;
               default:
                  t = this._handleUnexpectedValue(i);
               }

               if (inObject) {
                  this._nextToken = t;
                  return this._currToken;
               } else {
                  this._currToken = t;
                  return t;
               }
            }
         }
      }
   }

   private final JsonToken _nextAfterName() {
      this._nameCopied = false;
      JsonToken t = this._nextToken;
      this._nextToken = null;
      if (t == JsonToken.START_ARRAY) {
         this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
      } else if (t == JsonToken.START_OBJECT) {
         this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
      }

      return this._currToken = t;
   }

   public void close() throws IOException {
      super.close();
      this._symbols.release();
   }

   protected final String _parseFieldName(int i) throws IOException, JsonParseException {
      if (i != 34) {
         return this._handleUnusualFieldName(i);
      } else {
         int ptr = this._inputPtr;
         int hash = 0;
         int inputLen = this._inputEnd;
         if (ptr < inputLen) {
            int[] codes = CharTypes.getInputCodeLatin1();
            int maxCode = codes.length;

            do {
               int ch = this._inputBuffer[ptr];
               if (ch < maxCode && codes[ch] != 0) {
                  if (ch == '"') {
                     int start = this._inputPtr;
                     this._inputPtr = ptr + 1;
                     return this._symbols.findSymbol(this._inputBuffer, start, ptr - start, hash);
                  }
                  break;
               }

               hash = hash * 31 + ch;
               ++ptr;
            } while(ptr < inputLen);
         }

         int start = this._inputPtr;
         this._inputPtr = ptr;
         return this._parseFieldName2(start, hash, 34);
      }
   }

   private String _parseFieldName2(int startPtr, int hash, int endChar) throws IOException, JsonParseException {
      this._textBuffer.resetWithShared(this._inputBuffer, startPtr, this._inputPtr - startPtr);
      char[] outBuf = this._textBuffer.getCurrentSegment();
      int outPtr = this._textBuffer.getCurrentSegmentSize();

      while(true) {
         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOF(": was expecting closing '" + (char)endChar + "' for name");
         }

         char c = this._inputBuffer[this._inputPtr++];
         if (c <= '\\') {
            if (c == '\\') {
               c = this._decodeEscaped();
            } else if (c <= endChar) {
               if (c == endChar) {
                  this._textBuffer.setCurrentLength(outPtr);
                  TextBuffer tb = this._textBuffer;
                  char[] buf = tb.getTextBuffer();
                  int start = tb.getTextOffset();
                  int len = tb.size();
                  return this._symbols.findSymbol(buf, start, len, hash);
               }

               if (c < ' ') {
                  this._throwUnquotedSpace(c, "name");
               }
            }
         }

         hash = hash * 31 + c;
         outBuf[outPtr++] = c;
         if (outPtr >= outBuf.length) {
            outBuf = this._textBuffer.finishCurrentSegment();
            outPtr = 0;
         }
      }
   }

   protected final String _handleUnusualFieldName(int i) throws IOException, JsonParseException {
      if (i == 39 && this.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES)) {
         return this._parseApostropheFieldName();
      } else {
         if (!this.isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)) {
            this._reportUnexpectedChar(i, "was expecting double-quote to start field name");
         }

         int[] codes = CharTypes.getInputCodeLatin1JsNames();
         int maxCode = codes.length;
         boolean firstOk;
         if (i < maxCode) {
            firstOk = codes[i] == 0 && (i < 48 || i > 57);
         } else {
            firstOk = Character.isJavaIdentifierPart((char)i);
         }

         if (!firstOk) {
            this._reportUnexpectedChar(i, "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
         }

         int ptr = this._inputPtr;
         int hash = 0;
         int inputLen = this._inputEnd;
         if (ptr < inputLen) {
            do {
               int ch = this._inputBuffer[ptr];
               int start;
               if (ch < maxCode) {
                  if (codes[ch] != 0) {
                     start = this._inputPtr - 1;
                     this._inputPtr = ptr;
                     return this._symbols.findSymbol(this._inputBuffer, start, ptr - start, hash);
                  }
               } else if (!Character.isJavaIdentifierPart((char)ch)) {
                  start = this._inputPtr - 1;
                  this._inputPtr = ptr;
                  return this._symbols.findSymbol(this._inputBuffer, start, ptr - start, hash);
               }

               hash = hash * 31 + ch;
               ++ptr;
            } while(ptr < inputLen);
         }

         int start = this._inputPtr - 1;
         this._inputPtr = ptr;
         return this._parseUnusualFieldName2(start, hash, codes);
      }
   }

   protected final String _parseApostropheFieldName() throws IOException, JsonParseException {
      int ptr = this._inputPtr;
      int hash = 0;
      int inputLen = this._inputEnd;
      if (ptr < inputLen) {
         int[] codes = CharTypes.getInputCodeLatin1();
         int maxCode = codes.length;

         do {
            int ch = this._inputBuffer[ptr];
            if (ch == '\'') {
               int start = this._inputPtr;
               this._inputPtr = ptr + 1;
               return this._symbols.findSymbol(this._inputBuffer, start, ptr - start, hash);
            }

            if (ch < maxCode && codes[ch] != 0) {
               break;
            }

            hash = hash * 31 + ch;
            ++ptr;
         } while(ptr < inputLen);
      }

      int start = this._inputPtr;
      this._inputPtr = ptr;
      return this._parseFieldName2(start, hash, 39);
   }

   protected final JsonToken _handleUnexpectedValue(int i) throws IOException, JsonParseException {
      switch(i) {
      case 39:
         if (this.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES)) {
            return this._handleApostropheValue();
         }
         break;
      case 43:
         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOFInValue();
         }

         return this._handleInvalidNumberStart(this._inputBuffer[this._inputPtr++], false);
      case 78:
         if (this._matchToken("NaN", 1)) {
            if (this.isEnabled(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
               return this.resetAsNaN("NaN", 0.0D / 0.0);
            }

            this._reportError("Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
         }

         this._reportUnexpectedChar(this._inputBuffer[this._inputPtr++], "expected 'NaN' or a valid value");
      }

      this._reportUnexpectedChar(i, "expected a valid value (number, String, array, object, 'true', 'false' or 'null')");
      return null;
   }

   protected final JsonToken _handleApostropheValue() throws IOException, JsonParseException {
      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
      int outPtr = this._textBuffer.getCurrentSegmentSize();

      while(true) {
         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOF(": was expecting closing quote for a string value");
         }

         char c = this._inputBuffer[this._inputPtr++];
         if (c <= '\\') {
            if (c == '\\') {
               c = this._decodeEscaped();
            } else if (c <= '\'') {
               if (c == '\'') {
                  this._textBuffer.setCurrentLength(outPtr);
                  return JsonToken.VALUE_STRING;
               }

               if (c < ' ') {
                  this._throwUnquotedSpace(c, "string value");
               }
            }
         }

         if (outPtr >= outBuf.length) {
            outBuf = this._textBuffer.finishCurrentSegment();
            outPtr = 0;
         }

         outBuf[outPtr++] = c;
      }
   }

   private String _parseUnusualFieldName2(int startPtr, int hash, int[] codes) throws IOException, JsonParseException {
      this._textBuffer.resetWithShared(this._inputBuffer, startPtr, this._inputPtr - startPtr);
      char[] outBuf = this._textBuffer.getCurrentSegment();
      int outPtr = this._textBuffer.getCurrentSegmentSize();
      int maxCode = codes.length;

      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         char c = this._inputBuffer[this._inputPtr];
         if (c <= maxCode) {
            if (codes[c] != 0) {
               break;
            }
         } else if (!Character.isJavaIdentifierPart(c)) {
            break;
         }

         ++this._inputPtr;
         hash = hash * 31 + c;
         outBuf[outPtr++] = c;
         if (outPtr >= outBuf.length) {
            outBuf = this._textBuffer.finishCurrentSegment();
            outPtr = 0;
         }
      }

      this._textBuffer.setCurrentLength(outPtr);
      TextBuffer tb = this._textBuffer;
      char[] buf = tb.getTextBuffer();
      int start = tb.getTextOffset();
      int len = tb.size();
      return this._symbols.findSymbol(buf, start, len, hash);
   }

   protected void _finishString() throws IOException, JsonParseException {
      int ptr = this._inputPtr;
      int inputLen = this._inputEnd;
      if (ptr < inputLen) {
         int[] codes = CharTypes.getInputCodeLatin1();
         int maxCode = codes.length;

         do {
            int ch = this._inputBuffer[ptr];
            if (ch < maxCode && codes[ch] != 0) {
               if (ch == '"') {
                  this._textBuffer.resetWithShared(this._inputBuffer, this._inputPtr, ptr - this._inputPtr);
                  this._inputPtr = ptr + 1;
                  return;
               }
               break;
            }

            ++ptr;
         } while(ptr < inputLen);
      }

      this._textBuffer.resetWithCopy(this._inputBuffer, this._inputPtr, ptr - this._inputPtr);
      this._inputPtr = ptr;
      this._finishString2();
   }

   protected void _finishString2() throws IOException, JsonParseException {
      char[] outBuf = this._textBuffer.getCurrentSegment();
      int outPtr = this._textBuffer.getCurrentSegmentSize();

      while(true) {
         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOF(": was expecting closing quote for a string value");
         }

         char c = this._inputBuffer[this._inputPtr++];
         if (c <= '\\') {
            if (c == '\\') {
               c = this._decodeEscaped();
            } else if (c <= '"') {
               if (c == '"') {
                  this._textBuffer.setCurrentLength(outPtr);
                  return;
               }

               if (c < ' ') {
                  this._throwUnquotedSpace(c, "string value");
               }
            }
         }

         if (outPtr >= outBuf.length) {
            outBuf = this._textBuffer.finishCurrentSegment();
            outPtr = 0;
         }

         outBuf[outPtr++] = c;
      }
   }

   protected void _skipString() throws IOException, JsonParseException {
      this._tokenIncomplete = false;
      int inputPtr = this._inputPtr;
      int inputLen = this._inputEnd;
      char[] inputBuffer = this._inputBuffer;

      while(true) {
         if (inputPtr >= inputLen) {
            this._inputPtr = inputPtr;
            if (!this.loadMore()) {
               this._reportInvalidEOF(": was expecting closing quote for a string value");
            }

            inputPtr = this._inputPtr;
            inputLen = this._inputEnd;
         }

         char c = inputBuffer[inputPtr++];
         if (c <= '\\') {
            if (c == '\\') {
               this._inputPtr = inputPtr;
               c = this._decodeEscaped();
               inputPtr = this._inputPtr;
               inputLen = this._inputEnd;
            } else if (c <= '"') {
               if (c == '"') {
                  this._inputPtr = inputPtr;
                  return;
               }

               if (c < ' ') {
                  this._inputPtr = inputPtr;
                  this._throwUnquotedSpace(c, "string value");
               }
            }
         }
      }
   }

   protected void _matchToken(JsonToken token) throws IOException, JsonParseException {
      String matchStr = token.asString();
      int i = 1;

      for(int len = matchStr.length(); i < len; ++i) {
         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOF(" in a value");
         }

         char c = this._inputBuffer[this._inputPtr];
         if (c != matchStr.charAt(i)) {
            this._reportInvalidToken(matchStr.substring(0, i), "'null', 'true' or 'false'");
         }

         ++this._inputPtr;
      }

   }

   protected final void _skipCR() throws IOException {
      if ((this._inputPtr < this._inputEnd || this.loadMore()) && this._inputBuffer[this._inputPtr] == '\n') {
         ++this._inputPtr;
      }

      ++this._currInputRow;
      this._currInputRowStart = this._inputPtr;
   }

   protected final void _skipLF() throws IOException {
      ++this._currInputRow;
      this._currInputRowStart = this._inputPtr;
   }

   private final int _skipWS() throws IOException, JsonParseException {
      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         int i = this._inputBuffer[this._inputPtr++];
         if (i > ' ') {
            if (i != '/') {
               return i;
            }

            this._skipComment();
         } else if (i != ' ') {
            if (i == '\n') {
               this._skipLF();
            } else if (i == '\r') {
               this._skipCR();
            } else if (i != '\t') {
               this._throwInvalidSpace(i);
            }
         }
      }

      throw this._constructError("Unexpected end-of-input within/between " + this._parsingContext.getTypeDesc() + " entries");
   }

   private final int _skipWSOrEnd() throws IOException, JsonParseException {
      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         int i = this._inputBuffer[this._inputPtr++];
         if (i > ' ') {
            if (i != '/') {
               return i;
            }

            this._skipComment();
         } else if (i != ' ') {
            if (i == '\n') {
               this._skipLF();
            } else if (i == '\r') {
               this._skipCR();
            } else if (i != '\t') {
               this._throwInvalidSpace(i);
            }
         }
      }

      this._handleEOF();
      return -1;
   }

   private final void _skipComment() throws IOException, JsonParseException {
      if (!this.isEnabled(JsonParser.Feature.ALLOW_COMMENTS)) {
         this._reportUnexpectedChar(47, "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
      }

      if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
         this._reportInvalidEOF(" in a comment");
      }

      char c = this._inputBuffer[this._inputPtr++];
      if (c == '/') {
         this._skipCppComment();
      } else if (c == '*') {
         this._skipCComment();
      } else {
         this._reportUnexpectedChar(c, "was expecting either '*' or '/' for a comment");
      }

   }

   private final void _skipCComment() throws IOException, JsonParseException {
      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         int i = this._inputBuffer[this._inputPtr++];
         if (i <= '*') {
            if (i != '*') {
               if (i < ' ') {
                  if (i == '\n') {
                     this._skipLF();
                  } else if (i == '\r') {
                     this._skipCR();
                  } else if (i != '\t') {
                     this._throwInvalidSpace(i);
                  }
               }
            } else {
               if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
                  break;
               }

               if (this._inputBuffer[this._inputPtr] == '/') {
                  ++this._inputPtr;
                  return;
               }
            }
         }
      }

      this._reportInvalidEOF(" in a comment");
   }

   private final void _skipCppComment() throws IOException, JsonParseException {
      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         int i = this._inputBuffer[this._inputPtr++];
         if (i < ' ') {
            if (i == '\n') {
               this._skipLF();
               break;
            } else if (i != '\r') {
               if (i != '\t') {
                  this._throwInvalidSpace(i);
               }
            } else {
               this._skipCR();
               break;
            }
         }
      }

   }

   protected final char _decodeEscaped() throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
         this._reportInvalidEOF(" in character escape sequence");
      }

      char c = this._inputBuffer[this._inputPtr++];
      switch(c) {
      case '"':
      case '/':
      case '\\':
         return c;
      case 'b':
         return '\b';
      case 'f':
         return '\f';
      case 'n':
         return '\n';
      case 'r':
         return '\r';
      case 't':
         return '\t';
      case 'u':
         int value = 0;

         for(int i = 0; i < 4; ++i) {
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
               this._reportInvalidEOF(" in character escape sequence");
            }

            int ch = this._inputBuffer[this._inputPtr++];
            int digit = CharTypes.charToHex(ch);
            if (digit < 0) {
               this._reportUnexpectedChar(ch, "expected a hex-digit for character escape sequence");
            }

            value = value << 4 | digit;
         }

         return (char)value;
      default:
         return this._handleUnrecognizedCharacterEscape(c);
      }
   }

   protected byte[] _decodeBase64(Base64Variant b64variant) throws IOException, JsonParseException {
      ByteArrayBuilder builder = this._getByteArrayBuilder();

      while(true) {
         while(true) {
            char ch;
            int bits;
            do {
               do {
                  if (this._inputPtr >= this._inputEnd) {
                     this.loadMoreGuaranteed();
                  }

                  ch = this._inputBuffer[this._inputPtr++];
               } while(ch <= ' ');

               bits = b64variant.decodeBase64Char(ch);
               if (bits >= 0) {
                  break;
               }

               if (ch == '"') {
                  return builder.toByteArray();
               }

               bits = this._decodeBase64Escape(b64variant, ch, 0);
            } while(bits < 0);

            int decodedData = bits;
            if (this._inputPtr >= this._inputEnd) {
               this.loadMoreGuaranteed();
            }

            ch = this._inputBuffer[this._inputPtr++];
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
               bits = this._decodeBase64Escape(b64variant, ch, 1);
            }

            decodedData = decodedData << 6 | bits;
            if (this._inputPtr >= this._inputEnd) {
               this.loadMoreGuaranteed();
            }

            ch = this._inputBuffer[this._inputPtr++];
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
               if (bits != -2) {
                  bits = this._decodeBase64Escape(b64variant, ch, 2);
               }

               if (bits == -2) {
                  if (this._inputPtr >= this._inputEnd) {
                     this.loadMoreGuaranteed();
                  }

                  ch = this._inputBuffer[this._inputPtr++];
                  if (!b64variant.usesPaddingChar(ch)) {
                     throw this.reportInvalidChar(b64variant, ch, 3, "expected padding character '" + b64variant.getPaddingChar() + "'");
                  }

                  decodedData >>= 4;
                  builder.append(decodedData);
                  continue;
               }
            }

            decodedData = decodedData << 6 | bits;
            if (this._inputPtr >= this._inputEnd) {
               this.loadMoreGuaranteed();
            }

            ch = this._inputBuffer[this._inputPtr++];
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
               if (bits != -2) {
                  bits = this._decodeBase64Escape(b64variant, ch, 3);
               }

               if (bits == -2) {
                  decodedData >>= 2;
                  builder.appendTwoBytes(decodedData);
                  continue;
               }
            }

            decodedData = decodedData << 6 | bits;
            builder.appendThreeBytes(decodedData);
         }
      }
   }

   private final int _decodeBase64Escape(Base64Variant b64variant, char ch, int index) throws IOException, JsonParseException {
      if (ch != '\\') {
         throw this.reportInvalidChar(b64variant, ch, index);
      } else {
         char unescaped = this._decodeEscaped();
         if (unescaped <= ' ' && index == 0) {
            return -1;
         } else {
            int bits = b64variant.decodeBase64Char(unescaped);
            if (bits < 0) {
               throw this.reportInvalidChar(b64variant, unescaped, index);
            } else {
               return bits;
            }
         }
      }
   }

   protected IllegalArgumentException reportInvalidChar(Base64Variant b64variant, char ch, int bindex) throws IllegalArgumentException {
      return this.reportInvalidChar(b64variant, ch, bindex, (String)null);
   }

   protected IllegalArgumentException reportInvalidChar(Base64Variant b64variant, char ch, int bindex, String msg) throws IllegalArgumentException {
      String base;
      if (ch <= ' ') {
         base = "Illegal white space character (code 0x" + Integer.toHexString(ch) + ") as character #" + (bindex + 1) + " of 4-char base64 unit: can only used between units";
      } else if (b64variant.usesPaddingChar(ch)) {
         base = "Unexpected padding character ('" + b64variant.getPaddingChar() + "') as character #" + (bindex + 1) + " of 4-char base64 unit: padding only legal as 3rd or 4th character";
      } else if (Character.isDefined(ch) && !Character.isISOControl(ch)) {
         base = "Illegal character '" + ch + "' (code 0x" + Integer.toHexString(ch) + ") in base64 content";
      } else {
         base = "Illegal character (code 0x" + Integer.toHexString(ch) + ") in base64 content";
      }

      if (msg != null) {
         base = base + ": " + msg;
      }

      return new IllegalArgumentException(base);
   }
}
