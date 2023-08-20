package org.codehaus.jackson.smile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.impl.StreamBasedParserBase;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.sym.BytesToNameCanonicalizer;
import org.codehaus.jackson.sym.Name;

public class SmileParser extends StreamBasedParserBase {
   private static final int[] NO_INTS = new int[0];
   private static final String[] NO_STRINGS = new String[0];
   protected ObjectCodec _objectCodec;
   protected boolean _mayContainRawBinary;
   protected final SmileBufferRecycler<String> _smileBufferRecycler;
   protected boolean _tokenIncomplete = false;
   protected int _typeByte;
   protected boolean _got32BitFloat;
   protected final BytesToNameCanonicalizer _symbols;
   protected int[] _quadBuffer;
   protected int _quad1;
   protected int _quad2;
   protected String[] _seenNames;
   protected int _seenNameCount;
   protected String[] _seenStringValues;
   protected int _seenStringValueCount;
   protected static final ThreadLocal<SoftReference<SmileBufferRecycler<String>>> _smileRecyclerRef = new ThreadLocal();

   public SmileParser(IOContext ctxt, int parserFeatures, int smileFeatures, ObjectCodec codec, BytesToNameCanonicalizer sym, InputStream in, byte[] inputBuffer, int start, int end, boolean bufferRecyclable) {
      super(ctxt, parserFeatures, in, inputBuffer, start, end, bufferRecyclable);
      this._quadBuffer = NO_INTS;
      this._seenNames = NO_STRINGS;
      this._seenNameCount = 0;
      this._seenStringValues = null;
      this._seenStringValueCount = -1;
      this._objectCodec = codec;
      this._symbols = sym;
      this._tokenInputRow = -1;
      this._tokenInputCol = -1;
      this._smileBufferRecycler = _smileBufferRecycler();
   }

   public ObjectCodec getCodec() {
      return this._objectCodec;
   }

   public void setCodec(ObjectCodec c) {
      this._objectCodec = c;
   }

   protected boolean handleSignature(boolean consumeFirstByte, boolean throwException) throws IOException, JsonParseException {
      if (consumeFirstByte) {
         ++this._inputPtr;
      }

      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      if (this._inputBuffer[this._inputPtr] != 41) {
         if (throwException) {
            this._reportError("Malformed content: signature not valid, starts with 0x3a but followed by 0x" + Integer.toHexString(this._inputBuffer[this._inputPtr]) + ", not 0x29");
         }

         return false;
      } else {
         if (++this._inputPtr >= this._inputEnd) {
            this.loadMoreGuaranteed();
         }

         if (this._inputBuffer[this._inputPtr] != 10) {
            if (throwException) {
               this._reportError("Malformed content: signature not valid, starts with 0x3a, 0x29, but followed by 0x" + Integer.toHexString(this._inputBuffer[this._inputPtr]) + ", not 0xA");
            }

            return false;
         } else {
            if (++this._inputPtr >= this._inputEnd) {
               this.loadMoreGuaranteed();
            }

            int ch = this._inputBuffer[this._inputPtr++];
            int versionBits = ch >> 4 & 15;
            if (versionBits != 0) {
               this._reportError("Header version number bits (0x" + Integer.toHexString(versionBits) + ") indicate unrecognized version; only 0x0 handled by parser");
            }

            if ((ch & 1) == 0) {
               this._seenNames = null;
               this._seenNameCount = -1;
            }

            if ((ch & 2) != 0) {
               this._seenStringValues = NO_STRINGS;
               this._seenStringValueCount = 0;
            }

            this._mayContainRawBinary = (ch & 4) != 0;
            return true;
         }
      }
   }

   protected static final SmileBufferRecycler<String> _smileBufferRecycler() {
      SoftReference<SmileBufferRecycler<String>> ref = (SoftReference)_smileRecyclerRef.get();
      SmileBufferRecycler<String> br = ref == null ? null : (SmileBufferRecycler)ref.get();
      if (br == null) {
         br = new SmileBufferRecycler();
         _smileRecyclerRef.set(new SoftReference(br));
      }

      return br;
   }

   protected void _finishString() throws IOException, JsonParseException {
      this._throwInternal();
   }

   public void close() throws IOException {
      super.close();
      this._symbols.release();
   }

   protected void _releaseBuffers() throws IOException {
      super._releaseBuffers();
      String[] valueBuf = this._seenNames;
      if (valueBuf != null && valueBuf.length > 0) {
         this._seenNames = null;
         Arrays.fill(valueBuf, 0, this._seenNameCount, (Object)null);
         this._smileBufferRecycler.releaseSeenNamesBuffer(valueBuf);
      }

      valueBuf = this._seenStringValues;
      if (valueBuf != null && valueBuf.length > 0) {
         this._seenStringValues = null;
         Arrays.fill(valueBuf, 0, this._seenStringValueCount, (Object)null);
         this._smileBufferRecycler.releaseSeenStringValuesBuffer(valueBuf);
      }

   }

   public boolean mayContainRawBinary() {
      return this._mayContainRawBinary;
   }

   public JsonToken nextToken() throws IOException, JsonParseException {
      if (this._tokenIncomplete) {
         this._skipIncomplete();
      }

      this._tokenInputTotal = this._currInputProcessed + (long)this._inputPtr - 1L;
      this._binaryValue = null;
      if (this._parsingContext.inObject() && this._currToken != JsonToken.FIELD_NAME) {
         return this._currToken = this._handleFieldName();
      } else if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
         this._handleEOF();
         this.close();
         return this._currToken = null;
      } else {
         int ch = this._inputBuffer[this._inputPtr++];
         this._typeByte = ch;
         switch(ch >> 5 & 7) {
         case 0:
            if (ch == 0) {
               this._reportError("Invalid token byte 0x00");
            }

            return this._handleSharedString(ch - 1);
         case 1:
            int typeBits = ch & 31;
            if (typeBits < 4) {
               switch(typeBits) {
               case 0:
                  this._textBuffer.resetWithEmpty();
                  return this._currToken = JsonToken.VALUE_STRING;
               case 1:
                  return this._currToken = JsonToken.VALUE_NULL;
               case 2:
                  return this._currToken = JsonToken.VALUE_FALSE;
               default:
                  return this._currToken = JsonToken.VALUE_TRUE;
               }
            }

            if (typeBits < 8) {
               if ((typeBits & 3) <= 2) {
                  this._tokenIncomplete = true;
                  this._numTypesValid = 0;
                  return this._currToken = JsonToken.VALUE_NUMBER_INT;
               }
            } else if (typeBits < 12) {
               int subtype = typeBits & 3;
               if (subtype <= 2) {
                  this._tokenIncomplete = true;
                  this._numTypesValid = 0;
                  this._got32BitFloat = subtype == 0;
                  return this._currToken = JsonToken.VALUE_NUMBER_FLOAT;
               }
            } else {
               if (typeBits == 26 && this.handleSignature(false, false)) {
                  if (this._currToken == null) {
                     return this.nextToken();
                  }

                  return this._currToken = null;
               }

               this._reportError("Unrecognized token byte 0x3A (malformed segment header?");
            }
            break;
         case 2:
         case 3:
         case 4:
         case 5:
            this._currToken = JsonToken.VALUE_STRING;
            if (this._seenStringValueCount >= 0) {
               this._addSeenStringValue();
            } else {
               this._tokenIncomplete = true;
            }

            return this._currToken;
         case 6:
            this._numberInt = SmileUtil.zigzagDecode(ch & 31);
            this._numTypesValid = 1;
            return this._currToken = JsonToken.VALUE_NUMBER_INT;
         case 7:
            switch(ch & 31) {
            case 0:
            case 4:
               this._tokenIncomplete = true;
               return this._currToken = JsonToken.VALUE_STRING;
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
            case 7:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 28:
            case 30:
            default:
               break;
            case 8:
               this._tokenIncomplete = true;
               return this._currToken = JsonToken.VALUE_EMBEDDED_OBJECT;
            case 12:
            case 13:
            case 14:
            case 15:
               if (this._inputPtr >= this._inputEnd) {
                  this.loadMoreGuaranteed();
               }

               return this._handleSharedString(((ch & 3) << 8) + (this._inputBuffer[this._inputPtr++] & 255));
            case 24:
               this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
               return this._currToken = JsonToken.START_ARRAY;
            case 25:
               if (!this._parsingContext.inArray()) {
                  this._reportMismatchedEndMarker(93, '}');
               }

               this._parsingContext = this._parsingContext.getParent();
               return this._currToken = JsonToken.END_ARRAY;
            case 26:
               this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
               return this._currToken = JsonToken.START_OBJECT;
            case 27:
               this._reportError("Invalid type marker byte 0xFB in value mode (would be END_OBJECT in key mode)");
            case 29:
               this._tokenIncomplete = true;
               return this._currToken = JsonToken.VALUE_EMBEDDED_OBJECT;
            case 31:
               return this._currToken = null;
            }
         }

         this._reportError("Invalid type marker byte 0x" + Integer.toHexString(ch & 255) + " for expected value token");
         return null;
      }
   }

   private final JsonToken _handleSharedString(int index) throws IOException, JsonParseException {
      if (index >= this._seenStringValueCount) {
         this._reportInvalidSharedStringValue(index);
      }

      this._textBuffer.resetWithString(this._seenStringValues[index]);
      return this._currToken = JsonToken.VALUE_STRING;
   }

   private final void _addSeenStringValue() throws IOException, JsonParseException {
      this._finishToken();
      if (this._seenStringValueCount < this._seenStringValues.length) {
         this._seenStringValues[this._seenStringValueCount++] = this._textBuffer.contentsAsString();
      } else {
         this._expandSeenStringValues();
      }
   }

   private final void _expandSeenStringValues() {
      String[] oldShared = this._seenStringValues;
      int len = oldShared.length;
      String[] newShared;
      if (len == 0) {
         newShared = (String[])this._smileBufferRecycler.allocSeenStringValuesBuffer();
         if (newShared == null) {
            newShared = new String[64];
         }
      } else if (len == 1024) {
         newShared = oldShared;
         this._seenStringValueCount = 0;
      } else {
         int newSize = len == 64 ? 256 : 1024;
         newShared = new String[newSize];
         System.arraycopy(oldShared, 0, newShared, 0, oldShared.length);
      }

      this._seenStringValues = newShared;
      this._seenStringValues[this._seenStringValueCount++] = this._textBuffer.contentsAsString();
   }

   public String getCurrentName() throws IOException, JsonParseException {
      return this._parsingContext.getCurrentName();
   }

   public JsonParser.NumberType getNumberType() throws IOException, JsonParseException {
      return this._got32BitFloat ? JsonParser.NumberType.FLOAT : super.getNumberType();
   }

   public String getText() throws IOException, JsonParseException {
      if (this._tokenIncomplete) {
         this._tokenIncomplete = false;
         int tb = this._typeByte;
         int type = tb >> 5 & 7;
         if (type == 2 || type == 3) {
            this._decodeShortAsciiValue(1 + (tb & 63));
            return this._textBuffer.contentsAsString();
         }

         if (type == 4 || type == 5) {
            this._decodeShortUnicodeValue(2 + (tb & 63));
            return this._textBuffer.contentsAsString();
         }

         this._finishToken();
      }

      if (this._currToken == JsonToken.VALUE_STRING) {
         return this._textBuffer.contentsAsString();
      } else {
         JsonToken t = this._currToken;
         if (t == null) {
            return null;
         } else if (t == JsonToken.FIELD_NAME) {
            return this._parsingContext.getCurrentName();
         } else if (t.isNumeric()) {
            return this.getNumberValue().toString();
         } else {
            return this._currToken.asString();
         }
      }
   }

   public char[] getTextCharacters() throws IOException, JsonParseException {
      if (this._currToken != null) {
         if (this._tokenIncomplete) {
            this._finishToken();
         }

         switch(this._currToken) {
         case VALUE_STRING:
            return this._textBuffer.getTextBuffer();
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
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return this.getNumberValue().toString().toCharArray();
         default:
            return this._currToken.asCharArray();
         }
      } else {
         return null;
      }
   }

   public int getTextLength() throws IOException, JsonParseException {
      if (this._currToken != null) {
         if (this._tokenIncomplete) {
            this._finishToken();
         }

         switch(this._currToken) {
         case VALUE_STRING:
            return this._textBuffer.size();
         case FIELD_NAME:
            return this._parsingContext.getCurrentName().length();
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return this.getNumberValue().toString().length();
         default:
            return this._currToken.asCharArray().length;
         }
      } else {
         return 0;
      }
   }

   public int getTextOffset() throws IOException, JsonParseException {
      return 0;
   }

   public byte[] getBinaryValue(Base64Variant b64variant) throws IOException, JsonParseException {
      if (this._tokenIncomplete) {
         this._finishToken();
      }

      if (this._currToken != JsonToken.VALUE_EMBEDDED_OBJECT) {
         this._reportError("Current token (" + this._currToken + ") not VALUE_EMBEDDED_OBJECT, can not access as binary");
      }

      return this._binaryValue;
   }

   protected byte[] _decodeBase64(Base64Variant b64variant) throws IOException, JsonParseException {
      this._throwInternal();
      return null;
   }

   protected final JsonToken _handleFieldName() throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      byte ch;
      ch = this._inputBuffer[this._inputPtr++];
      this._typeByte = ch;
      int index;
      String name;
      Name n;
      label67:
      switch(ch >> 6 & 3) {
      case 0:
         switch(ch) {
         case 32:
            this._parsingContext.setCurrentName("");
            return JsonToken.FIELD_NAME;
         case 48:
         case 49:
         case 50:
         case 51:
            if (this._inputPtr >= this._inputEnd) {
               this.loadMoreGuaranteed();
            }

            index = ((ch & 3) << 8) + (this._inputBuffer[this._inputPtr++] & 255);
            if (index >= this._seenNameCount) {
               this._reportInvalidSharedName(index);
            }

            this._parsingContext.setCurrentName(this._seenNames[index]);
            return JsonToken.FIELD_NAME;
         case 52:
            this._handleLongFieldName();
            return JsonToken.FIELD_NAME;
         default:
            break label67;
         }
      case 1:
         index = ch & 63;
         if (index >= this._seenNameCount) {
            this._reportInvalidSharedName(index);
         }

         this._parsingContext.setCurrentName(this._seenNames[index]);
         return JsonToken.FIELD_NAME;
      case 2:
         index = 1 + (ch & 63);
         n = this._findDecodedFromSymbols(index);
         if (n != null) {
            name = n.getName();
            this._inputPtr += index;
         } else {
            name = this._decodeShortAsciiName(index);
            name = this._addDecodedToSymbols(index, name);
         }

         if (this._seenNames != null) {
            if (this._seenNameCount >= this._seenNames.length) {
               this._seenNames = this._expandSeenNames(this._seenNames);
            }

            this._seenNames[this._seenNameCount++] = name;
         }

         this._parsingContext.setCurrentName(name);
         return JsonToken.FIELD_NAME;
      case 3:
         index = ch & 63;
         if (index <= 55) {
            index += 2;
            n = this._findDecodedFromSymbols(index);
            if (n != null) {
               name = n.getName();
               this._inputPtr += index;
            } else {
               name = this._decodeShortUnicodeName(index);
               name = this._addDecodedToSymbols(index, name);
            }

            if (this._seenNames != null) {
               if (this._seenNameCount >= this._seenNames.length) {
                  this._seenNames = this._expandSeenNames(this._seenNames);
               }

               this._seenNames[this._seenNameCount++] = name;
            }

            this._parsingContext.setCurrentName(name);
            return JsonToken.FIELD_NAME;
         }

         if (index == 59) {
            if (!this._parsingContext.inObject()) {
               this._reportMismatchedEndMarker(125, ']');
            }

            this._parsingContext = this._parsingContext.getParent();
            return JsonToken.END_OBJECT;
         }
      }

      this._reportError("Invalid type marker byte 0x" + Integer.toHexString(ch) + " for expected field name (or END_OBJECT marker)");
      return null;
   }

   private final String[] _expandSeenNames(String[] oldShared) {
      int len = oldShared.length;
      String[] newShared;
      if (len == 0) {
         newShared = (String[])this._smileBufferRecycler.allocSeenNamesBuffer();
         if (newShared == null) {
            newShared = new String[64];
         }
      } else if (len == 1024) {
         newShared = oldShared;
         this._seenNameCount = 0;
      } else {
         int newSize = len == 64 ? 256 : 1024;
         newShared = new String[newSize];
         System.arraycopy(oldShared, 0, newShared, 0, oldShared.length);
      }

      return newShared;
   }

   private final String _addDecodedToSymbols(int len, String name) {
      if (len < 5) {
         return this._symbols.addName(name, this._quad1, 0).getName();
      } else if (len < 9) {
         return this._symbols.addName(name, this._quad1, this._quad2).getName();
      } else {
         int qlen = len + 3 >> 2;
         return this._symbols.addName(name, this._quadBuffer, qlen).getName();
      }
   }

   private final String _decodeShortAsciiName(int len) throws IOException, JsonParseException {
      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
      int outPtr = 0;
      byte[] inBuf = this._inputBuffer;
      int inPtr = this._inputPtr;

      int left;
      for(left = inPtr + len - 3; inPtr < left; outBuf[outPtr++] = (char)inBuf[inPtr++]) {
         outBuf[outPtr++] = (char)inBuf[inPtr++];
         outBuf[outPtr++] = (char)inBuf[inPtr++];
         outBuf[outPtr++] = (char)inBuf[inPtr++];
      }

      left = len & 3;
      if (left > 0) {
         outBuf[outPtr++] = (char)inBuf[inPtr++];
         if (left > 1) {
            outBuf[outPtr++] = (char)inBuf[inPtr++];
            if (left > 2) {
               outBuf[outPtr++] = (char)inBuf[inPtr++];
            }
         }
      }

      this._inputPtr = inPtr;
      this._textBuffer.setCurrentLength(len);
      return this._textBuffer.contentsAsString();
   }

   private final String _decodeShortUnicodeName(int len) throws IOException, JsonParseException {
      int outPtr = 0;
      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
      int inPtr = this._inputPtr;
      this._inputPtr += len;
      int[] codes = SmileConstants.sUtf8UnitLengths;
      byte[] inBuf = this._inputBuffer;

      int i;
      for(int end = inPtr + len; inPtr < end; outBuf[outPtr++] = (char)i) {
         i = inBuf[inPtr++] & 255;
         int code = codes[i];
         if (code != 0) {
            switch(code) {
            case 1:
               i = (i & 31) << 6 | inBuf[inPtr++] & 63;
               break;
            case 2:
               i = (i & 15) << 12 | (inBuf[inPtr++] & 63) << 6 | inBuf[inPtr++] & 63;
               break;
            case 3:
               i = (i & 7) << 18 | (inBuf[inPtr++] & 63) << 12 | (inBuf[inPtr++] & 63) << 6 | inBuf[inPtr++] & 63;
               i -= 65536;
               outBuf[outPtr++] = (char)('\ud800' | i >> 10);
               i = '\udc00' | i & 1023;
               break;
            default:
               this._reportError("Invalid byte " + Integer.toHexString(i) + " in short Unicode text block");
            }
         }
      }

      this._textBuffer.setCurrentLength(outPtr);
      return this._textBuffer.contentsAsString();
   }

   private final Name _decodeLongUnicodeName(int[] quads, int byteLen, int quadLen) throws IOException, JsonParseException {
      int lastQuadBytes = byteLen & 3;
      int lastQuad;
      if (lastQuadBytes < 4) {
         lastQuad = quads[quadLen - 1];
         quads[quadLen - 1] = lastQuad << (4 - lastQuadBytes << 3);
      } else {
         lastQuad = 0;
      }

      char[] cbuf = this._textBuffer.emptyAndGetCurrentSegment();
      int cix = 0;

      int ch;
      for(int ix = 0; ix < byteLen; cbuf[cix++] = (char)ch) {
         ch = quads[ix >> 2];
         int byteIx = ix & 3;
         ch = ch >> (3 - byteIx << 3) & 255;
         ++ix;
         if (ch > 127) {
            byte needed;
            if ((ch & 224) == 192) {
               ch &= 31;
               needed = 1;
            } else if ((ch & 240) == 224) {
               ch &= 15;
               needed = 2;
            } else if ((ch & 248) == 240) {
               ch &= 7;
               needed = 3;
            } else {
               this._reportInvalidInitial(ch);
               ch = 1;
               needed = 1;
            }

            if (ix + needed > byteLen) {
               this._reportInvalidEOF(" in long field name");
            }

            int ch2 = quads[ix >> 2];
            byteIx = ix & 3;
            ch2 >>= 3 - byteIx << 3;
            ++ix;
            if ((ch2 & 192) != 128) {
               this._reportInvalidOther(ch2);
            }

            ch = ch << 6 | ch2 & 63;
            if (needed > 1) {
               ch2 = quads[ix >> 2];
               byteIx = ix & 3;
               ch2 >>= 3 - byteIx << 3;
               ++ix;
               if ((ch2 & 192) != 128) {
                  this._reportInvalidOther(ch2);
               }

               ch = ch << 6 | ch2 & 63;
               if (needed > 2) {
                  ch2 = quads[ix >> 2];
                  byteIx = ix & 3;
                  ch2 >>= 3 - byteIx << 3;
                  ++ix;
                  if ((ch2 & 192) != 128) {
                     this._reportInvalidOther(ch2 & 255);
                  }

                  ch = ch << 6 | ch2 & 63;
               }
            }

            if (needed > 2) {
               ch -= 65536;
               if (cix >= cbuf.length) {
                  cbuf = this._textBuffer.expandCurrentSegment();
               }

               cbuf[cix++] = (char)('\ud800' + (ch >> 10));
               ch = '\udc00' | ch & 1023;
            }
         }

         if (cix >= cbuf.length) {
            cbuf = this._textBuffer.expandCurrentSegment();
         }
      }

      String baseName = new String(cbuf, 0, cix);
      if (lastQuadBytes < 4) {
         quads[quadLen - 1] = lastQuad;
      }

      return this._symbols.addName(baseName, quads, quadLen);
   }

   private final void _handleLongFieldName() throws IOException, JsonParseException {
      byte[] inBuf = this._inputBuffer;
      int quads = 0;
      int bytes = false;
      int q = 0;

      byte bytes;
      while(true) {
         byte b = inBuf[this._inputPtr++];
         if (-4 == b) {
            bytes = 0;
            break;
         }

         q = b & 255;
         b = inBuf[this._inputPtr++];
         if (-4 == b) {
            bytes = 1;
            break;
         }

         q = q << 8 | b & 255;
         b = inBuf[this._inputPtr++];
         if (-4 == b) {
            bytes = 2;
            break;
         }

         q = q << 8 | b & 255;
         b = inBuf[this._inputPtr++];
         if (-4 == b) {
            bytes = 3;
            break;
         }

         q = q << 8 | b & 255;
         if (quads >= this._quadBuffer.length) {
            this._quadBuffer = _growArrayTo(this._quadBuffer, this._quadBuffer.length + 256);
         }

         this._quadBuffer[quads++] = q;
      }

      int byteLen = quads << 2;
      if (bytes > 0) {
         if (quads >= this._quadBuffer.length) {
            this._quadBuffer = _growArrayTo(this._quadBuffer, this._quadBuffer.length + 256);
         }

         this._quadBuffer[quads++] = q;
         byteLen += bytes;
      }

      Name n = this._symbols.findName(this._quadBuffer, quads);
      String name;
      if (n != null) {
         name = n.getName();
      } else {
         name = this._decodeLongUnicodeName(this._quadBuffer, byteLen, quads).getName();
      }

      if (this._seenNames != null) {
         if (this._seenNameCount >= this._seenNames.length) {
            this._seenNames = this._expandSeenNames(this._seenNames);
         }

         this._seenNames[this._seenNameCount++] = name;
      }

      this._parsingContext.setCurrentName(name);
   }

   private final Name _findDecodedFromSymbols(int len) throws IOException, JsonParseException {
      if (this._inputEnd - this._inputPtr < len) {
         this._loadToHaveAtLeast(len);
      }

      int inPtr;
      byte[] inBuf;
      int q;
      if (len < 5) {
         inPtr = this._inputPtr;
         inBuf = this._inputBuffer;
         q = inBuf[inPtr] & 255;
         --len;
         if (len > 0) {
            int var10000 = q << 8;
            ++inPtr;
            q = var10000 + (inBuf[inPtr] & 255);
            --len;
            if (len > 0) {
               var10000 = q << 8;
               ++inPtr;
               q = var10000 + (inBuf[inPtr] & 255);
               --len;
               if (len > 0) {
                  var10000 = q << 8;
                  ++inPtr;
                  q = var10000 + (inBuf[inPtr] & 255);
               }
            }
         }

         this._quad1 = q;
         return this._symbols.findName(q);
      } else if (len < 9) {
         inPtr = this._inputPtr;
         inBuf = this._inputBuffer;
         q = inBuf[inPtr++] << 8;
         q += inBuf[inPtr++] & 255;
         q <<= 8;
         q += inBuf[inPtr++] & 255;
         q <<= 8;
         q += inBuf[inPtr++] & 255;
         int q2 = inBuf[inPtr++] & 255;
         len -= 5;
         if (len > 0) {
            q2 = (q2 << 8) + (inBuf[inPtr++] & 255);
            --len;
            if (len >= 0) {
               q2 = (q2 << 8) + (inBuf[inPtr++] & 255);
               --len;
               if (len >= 0) {
                  q2 = (q2 << 8) + (inBuf[inPtr++] & 255);
               }
            }
         }

         this._quad1 = q;
         this._quad2 = q2;
         return this._symbols.findName(q, q2);
      } else {
         return this._findDecodedMedium(len);
      }
   }

   private final Name _findDecodedMedium(int len) throws IOException, JsonParseException {
      int offset = len + 3 >> 2;
      if (offset > this._quadBuffer.length) {
         this._quadBuffer = _growArrayTo(this._quadBuffer, offset);
      }

      offset = 0;
      int inPtr = this._inputPtr;
      byte[] inBuf = this._inputBuffer;

      int q;
      do {
         q = (inBuf[inPtr++] & 255) << 8;
         q |= inBuf[inPtr++] & 255;
         q <<= 8;
         q |= inBuf[inPtr++] & 255;
         q <<= 8;
         q |= inBuf[inPtr++] & 255;
         this._quadBuffer[offset++] = q;
         len -= 4;
      } while(len > 3);

      if (len > 0) {
         q = inBuf[inPtr++] & 255;
         --len;
         if (len >= 0) {
            q = (q << 8) + (inBuf[inPtr++] & 255);
            --len;
            if (len >= 0) {
               q = (q << 8) + (inBuf[inPtr++] & 255);
            }
         }

         this._quadBuffer[offset++] = q;
      }

      return this._symbols.findName(this._quadBuffer, offset);
   }

   private static int[] _growArrayTo(int[] arr, int minSize) {
      int[] newArray = new int[minSize + 4];
      if (arr != null) {
         System.arraycopy(arr, 0, newArray, 0, arr.length);
      }

      return newArray;
   }

   protected void _parseNumericValue(int expType) throws IOException, JsonParseException {
      if (this._tokenIncomplete) {
         int tb = this._typeByte;
         if ((tb >> 5 & 7) != 1) {
            this._reportError("Current token (" + this._currToken + ") not numeric, can not use numeric value accessors");
         }

         this._tokenIncomplete = false;
         this._finishNumberToken(tb);
      }

   }

   protected void _finishToken() throws IOException, JsonParseException {
      this._tokenIncomplete = false;
      int tb = this._typeByte;
      int type = tb >> 5 & 7;
      if (type == 1) {
         this._finishNumberToken(tb);
      } else if (type <= 3) {
         this._decodeShortAsciiValue(1 + (tb & 63));
      } else if (type <= 5) {
         this._decodeShortUnicodeValue(2 + (tb & 63));
      } else {
         if (type == 7) {
            tb &= 31;
            switch(tb >> 2) {
            case 0:
               this._decodeLongAscii();
               return;
            case 1:
               this._decodeLongUnicode();
               return;
            case 2:
               this._binaryValue = this._read7BitBinaryWithLength();
               return;
            case 3:
            case 4:
            case 5:
            case 6:
            default:
               break;
            case 7:
               this._finishRawBinary();
               return;
            }
         }

         this._throwInternal();
      }
   }

   protected final void _finishNumberToken(int tb) throws IOException, JsonParseException {
      tb &= 31;
      int type = tb >> 2;
      if (type == 1) {
         int subtype = tb & 3;
         if (subtype == 0) {
            this._finishInt();
         } else if (subtype == 1) {
            this._finishLong();
         } else if (subtype == 2) {
            this._finishBigInteger();
         } else {
            this._throwInternal();
         }

      } else {
         if (type == 2) {
            switch(tb & 3) {
            case 0:
               this._finishFloat();
               return;
            case 1:
               this._finishDouble();
               return;
            case 2:
               this._finishBigDecimal();
               return;
            }
         }

         this._throwInternal();
      }
   }

   private final void _finishInt() throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      int value = this._inputBuffer[this._inputPtr++];
      if (value < 0) {
         value &= 63;
      } else {
         if (this._inputPtr >= this._inputEnd) {
            this.loadMoreGuaranteed();
         }

         int i = this._inputBuffer[this._inputPtr++];
         if (i >= 0) {
            value = (value << 7) + i;
            if (this._inputPtr >= this._inputEnd) {
               this.loadMoreGuaranteed();
            }

            i = this._inputBuffer[this._inputPtr++];
            if (i >= 0) {
               value = (value << 7) + i;
               if (this._inputPtr >= this._inputEnd) {
                  this.loadMoreGuaranteed();
               }

               i = this._inputBuffer[this._inputPtr++];
               if (i >= 0) {
                  value = (value << 7) + i;
                  if (this._inputPtr >= this._inputEnd) {
                     this.loadMoreGuaranteed();
                  }

                  i = this._inputBuffer[this._inputPtr++];
                  if (i >= 0) {
                     this._reportError("Corrupt input; 32-bit VInt extends beyond 5 data bytes");
                  }
               }
            }
         }

         value = (value << 6) + (i & 63);
      }

      this._numberInt = SmileUtil.zigzagDecode(value);
      this._numTypesValid = 1;
   }

   private final void _finishLong() throws IOException, JsonParseException {
      long l = (long)this._fourBytesToInt();

      while(true) {
         if (this._inputPtr >= this._inputEnd) {
            this.loadMoreGuaranteed();
         }

         int value = this._inputBuffer[this._inputPtr++];
         if (value < 0) {
            l = (l << 6) + (long)(value & 63);
            this._numberLong = SmileUtil.zigzagDecode(l);
            this._numTypesValid = 2;
            return;
         }

         l = (l << 7) + (long)value;
      }
   }

   private final void _finishBigInteger() throws IOException, JsonParseException {
      byte[] raw = this._read7BitBinaryWithLength();
      this._numberBigInt = new BigInteger(raw);
      this._numTypesValid = 4;
   }

   private final void _finishFloat() throws IOException, JsonParseException {
      int i = this._fourBytesToInt();
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      i = (i << 7) + this._inputBuffer[this._inputPtr++];
      float f = Float.intBitsToFloat(i);
      this._numberDouble = (double)f;
      this._numTypesValid = 8;
   }

   private final void _finishDouble() throws IOException, JsonParseException {
      long hi = (long)this._fourBytesToInt();
      long value = (hi << 28) + (long)this._fourBytesToInt();
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      value = (value << 7) + (long)this._inputBuffer[this._inputPtr++];
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      value = (value << 7) + (long)this._inputBuffer[this._inputPtr++];
      this._numberDouble = Double.longBitsToDouble(value);
      this._numTypesValid = 8;
   }

   private final int _fourBytesToInt() throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      int i = this._inputBuffer[this._inputPtr++];
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      int i = (i << 7) + this._inputBuffer[this._inputPtr++];
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      i = (i << 7) + this._inputBuffer[this._inputPtr++];
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      return (i << 7) + this._inputBuffer[this._inputPtr++];
   }

   private final void _finishBigDecimal() throws IOException, JsonParseException {
      int scale = SmileUtil.zigzagDecode(this._readUnsignedVInt());
      byte[] raw = this._read7BitBinaryWithLength();
      this._numberBigDecimal = new BigDecimal(new BigInteger(raw), scale);
      this._numTypesValid = 16;
   }

   private final int _readUnsignedVInt() throws IOException, JsonParseException {
      int value = 0;

      while(true) {
         if (this._inputPtr >= this._inputEnd) {
            this.loadMoreGuaranteed();
         }

         int i = this._inputBuffer[this._inputPtr++];
         if (i < 0) {
            value = (value << 6) + (i & 63);
            return value;
         }

         value = (value << 7) + i;
      }
   }

   private final byte[] _read7BitBinaryWithLength() throws IOException, JsonParseException {
      int byteLen = this._readUnsignedVInt();
      byte[] result = new byte[byteLen];
      int ptr = 0;

      int toDecode;
      int i;
      for(int lastOkPtr = byteLen - 7; ptr <= lastOkPtr; result[ptr++] = (byte)i) {
         if (this._inputEnd - this._inputPtr < 8) {
            this._loadToHaveAtLeast(8);
         }

         toDecode = (this._inputBuffer[this._inputPtr++] << 25) + (this._inputBuffer[this._inputPtr++] << 18) + (this._inputBuffer[this._inputPtr++] << 11) + (this._inputBuffer[this._inputPtr++] << 4);
         int x = this._inputBuffer[this._inputPtr++];
         toDecode += x >> 3;
         i = ((x & 7) << 21) + (this._inputBuffer[this._inputPtr++] << 14) + (this._inputBuffer[this._inputPtr++] << 7) + this._inputBuffer[this._inputPtr++];
         result[ptr++] = (byte)(toDecode >> 24);
         result[ptr++] = (byte)(toDecode >> 16);
         result[ptr++] = (byte)(toDecode >> 8);
         result[ptr++] = (byte)toDecode;
         result[ptr++] = (byte)(i >> 16);
         result[ptr++] = (byte)(i >> 8);
      }

      toDecode = result.length - ptr;
      if (toDecode > 0) {
         if (this._inputEnd - this._inputPtr < toDecode + 1) {
            this._loadToHaveAtLeast(toDecode + 1);
         }

         int value = this._inputBuffer[this._inputPtr++];

         for(i = 1; i < toDecode; ++i) {
            value = (value << 7) + this._inputBuffer[this._inputPtr++];
            result[ptr++] = (byte)(value >> 7 - i);
         }

         value <<= toDecode;
         result[ptr] = (byte)(value + this._inputBuffer[this._inputPtr++]);
      }

      return result;
   }

   protected final void _decodeShortAsciiValue(int len) throws IOException, JsonParseException {
      if (this._inputEnd - this._inputPtr < len) {
         this._loadToHaveAtLeast(len);
      }

      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
      int outPtr = 0;
      byte[] inBuf = this._inputBuffer;
      int inPtr = this._inputPtr;

      for(int end = inPtr + len; inPtr < end; ++inPtr) {
         outBuf[outPtr++] = (char)inBuf[inPtr];
      }

      this._inputPtr = inPtr;
      this._textBuffer.setCurrentLength(len);
   }

   protected final void _decodeShortUnicodeValue(int len) throws IOException, JsonParseException {
      if (this._inputEnd - this._inputPtr < len) {
         this._loadToHaveAtLeast(len);
      }

      int outPtr = 0;
      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
      int inPtr = this._inputPtr;
      this._inputPtr += len;
      int[] codes = SmileConstants.sUtf8UnitLengths;
      byte[] inputBuf = this._inputBuffer;

      int i;
      for(int end = inPtr + len; inPtr < end; outBuf[outPtr++] = (char)i) {
         i = inputBuf[inPtr++] & 255;
         int code = codes[i];
         if (code != 0) {
            switch(code) {
            case 1:
               i = (i & 31) << 6 | inputBuf[inPtr++] & 63;
               break;
            case 2:
               i = (i & 15) << 12 | (inputBuf[inPtr++] & 63) << 6 | inputBuf[inPtr++] & 63;
               break;
            case 3:
               i = (i & 7) << 18 | (inputBuf[inPtr++] & 63) << 12 | (inputBuf[inPtr++] & 63) << 6 | inputBuf[inPtr++] & 63;
               i -= 65536;
               outBuf[outPtr++] = (char)('\ud800' | i >> 10);
               i = '\udc00' | i & 1023;
               break;
            default:
               this._reportError("Invalid byte " + Integer.toHexString(i) + " in short Unicode text block");
            }
         }
      }

      this._textBuffer.setCurrentLength(outPtr);
   }

   private final void _decodeLongAscii() throws IOException, JsonParseException {
      int outPtr = 0;
      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();

      while(true) {
         if (this._inputPtr >= this._inputEnd) {
            this.loadMoreGuaranteed();
         }

         int inPtr = this._inputPtr;
         int left = this._inputEnd - inPtr;
         if (outPtr >= outBuf.length) {
            outBuf = this._textBuffer.finishCurrentSegment();
            outPtr = 0;
         }

         left = Math.min(left, outBuf.length - outPtr);

         do {
            byte b = this._inputBuffer[inPtr++];
            if (b == -4) {
               this._inputPtr = inPtr;
               this._textBuffer.setCurrentLength(outPtr);
               return;
            }

            outBuf[outPtr++] = (char)b;
            --left;
         } while(left > 0);

         this._inputPtr = inPtr;
      }
   }

   private final void _decodeLongUnicode() throws IOException, JsonParseException {
      int outPtr = 0;
      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
      int[] codes = SmileConstants.sUtf8UnitLengths;
      byte[] inputBuffer = this._inputBuffer;

      while(true) {
         label52:
         while(true) {
            int ptr = this._inputPtr;
            if (ptr >= this._inputEnd) {
               this.loadMoreGuaranteed();
               ptr = this._inputPtr;
            }

            if (outPtr >= outBuf.length) {
               outBuf = this._textBuffer.finishCurrentSegment();
               outPtr = 0;
            }

            int max = this._inputEnd;
            int max2 = ptr + (outBuf.length - outPtr);
            if (max2 < max) {
               max = max2;
            }

            while(ptr < max) {
               int c = inputBuffer[ptr++] & 255;
               if (codes[c] != 0) {
                  this._inputPtr = ptr;
                  if (c == 252) {
                     this._textBuffer.setCurrentLength(outPtr);
                     return;
                  }

                  switch(codes[c]) {
                  case 1:
                     c = this._decodeUtf8_2(c);
                     break;
                  case 2:
                     if (this._inputEnd - this._inputPtr >= 2) {
                        c = this._decodeUtf8_3fast(c);
                     } else {
                        c = this._decodeUtf8_3(c);
                     }
                     break;
                  case 3:
                  default:
                     this._reportInvalidChar(c);
                     break;
                  case 4:
                     c = this._decodeUtf8_4(c);
                     outBuf[outPtr++] = (char)('\ud800' | c >> 10);
                     if (outPtr >= outBuf.length) {
                        outBuf = this._textBuffer.finishCurrentSegment();
                        outPtr = 0;
                     }

                     c = '\udc00' | c & 1023;
                  }

                  if (outPtr >= outBuf.length) {
                     outBuf = this._textBuffer.finishCurrentSegment();
                     outPtr = 0;
                  }

                  outBuf[outPtr++] = (char)c;
                  continue label52;
               }

               outBuf[outPtr++] = (char)c;
            }

            this._inputPtr = ptr;
         }
      }
   }

   private final void _finishRawBinary() throws IOException, JsonParseException {
      int byteLen = this._readUnsignedVInt();
      this._binaryValue = new byte[byteLen];
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      int ptr = 0;

      while(true) {
         int toAdd = Math.min(byteLen, this._inputEnd - this._inputPtr);
         System.arraycopy(this._inputBuffer, this._inputPtr, this._binaryValue, ptr, toAdd);
         this._inputPtr += toAdd;
         ptr += toAdd;
         byteLen -= toAdd;
         if (byteLen <= 0) {
            return;
         }

         this.loadMoreGuaranteed();
      }
   }

   protected void _skipIncomplete() throws IOException, JsonParseException {
      this._tokenIncomplete = false;
      int tb = this._typeByte;
      int end;
      byte[] buf;
      label59:
      switch(tb >> 5 & 7) {
      case 1:
         tb &= 31;
         switch(tb >> 2) {
         case 1:
            switch(tb & 3) {
            case 1:
               this._skipBytes(4);
            case 0:
               while(true) {
                  end = this._inputEnd;
                  buf = this._inputBuffer;

                  while(this._inputPtr < end) {
                     if (buf[this._inputPtr++] < 0) {
                        return;
                     }
                  }

                  this.loadMoreGuaranteed();
               }
            case 2:
               this._skip7BitBinary();
               return;
            default:
               break label59;
            }
         case 2:
            switch(tb & 3) {
            case 0:
               this._skipBytes(5);
               return;
            case 1:
               this._skipBytes(10);
               return;
            case 2:
               this._readUnsignedVInt();
               this._skip7BitBinary();
               return;
            }
         default:
            break label59;
         }
      case 2:
      case 3:
         this._skipBytes(1 + (tb & 63));
         return;
      case 4:
      case 5:
         this._skipBytes(2 + (tb & 63));
         return;
      case 6:
      default:
         break;
      case 7:
         tb &= 31;
         switch(tb >> 2) {
         case 0:
         case 1:
            while(true) {
               end = this._inputEnd;
               buf = this._inputBuffer;

               while(this._inputPtr < end) {
                  if (buf[this._inputPtr++] == -4) {
                     return;
                  }
               }

               this.loadMoreGuaranteed();
            }
         case 2:
            this._skip7BitBinary();
            return;
         case 3:
         case 4:
         case 5:
         case 6:
         default:
            break;
         case 7:
            this._skipBytes(this._readUnsignedVInt());
            return;
         }
      }

      this._throwInternal();
   }

   protected void _skipBytes(int len) throws IOException, JsonParseException {
      while(true) {
         int toAdd = Math.min(len, this._inputEnd - this._inputPtr);
         this._inputPtr += toAdd;
         len -= toAdd;
         if (len <= 0) {
            return;
         }

         this.loadMoreGuaranteed();
      }
   }

   protected void _skip7BitBinary() throws IOException, JsonParseException {
      int origBytes = this._readUnsignedVInt();
      int chunks = origBytes / 7;
      int encBytes = chunks * 8;
      origBytes -= 7 * chunks;
      if (origBytes > 0) {
         encBytes += 1 + origBytes;
      }

      this._skipBytes(encBytes);
   }

   private final int _decodeUtf8_2(int c) throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      int d = this._inputBuffer[this._inputPtr++];
      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

      return (c & 31) << 6 | d & 63;
   }

   private final int _decodeUtf8_3(int c1) throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      c1 &= 15;
      int d = this._inputBuffer[this._inputPtr++];
      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

      int c = c1 << 6 | d & 63;
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      d = this._inputBuffer[this._inputPtr++];
      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

      c = c << 6 | d & 63;
      return c;
   }

   private final int _decodeUtf8_3fast(int c1) throws IOException, JsonParseException {
      c1 &= 15;
      int d = this._inputBuffer[this._inputPtr++];
      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

      int c = c1 << 6 | d & 63;
      d = this._inputBuffer[this._inputPtr++];
      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

      c = c << 6 | d & 63;
      return c;
   }

   private final int _decodeUtf8_4(int c) throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      int d = this._inputBuffer[this._inputPtr++];
      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

      c = (c & 7) << 6 | d & 63;
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      d = this._inputBuffer[this._inputPtr++];
      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

      c = c << 6 | d & 63;
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      d = this._inputBuffer[this._inputPtr++];
      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

      return (c << 6 | d & 63) - 65536;
   }

   protected void _reportInvalidSharedName(int index) throws IOException {
      if (this._seenNames == null) {
         this._reportError("Encountered shared name reference, even though document header explicitly declared no shared name references are included");
      }

      this._reportError("Invalid shared name reference " + index + "; only got " + this._seenNameCount + " names in buffer (invalid content)");
   }

   protected void _reportInvalidSharedStringValue(int index) throws IOException {
      if (this._seenStringValues == null) {
         this._reportError("Encountered shared text value reference, even though document header did not declared shared text value references may be included");
      }

      this._reportError("Invalid shared text value reference " + index + "; only got " + this._seenStringValueCount + " names in buffer (invalid content)");
   }

   protected void _reportInvalidChar(int c) throws JsonParseException {
      if (c < 32) {
         this._throwInvalidSpace(c);
      }

      this._reportInvalidInitial(c);
   }

   protected void _reportInvalidInitial(int mask) throws JsonParseException {
      this._reportError("Invalid UTF-8 start byte 0x" + Integer.toHexString(mask));
   }

   protected void _reportInvalidOther(int mask) throws JsonParseException {
      this._reportError("Invalid UTF-8 middle byte 0x" + Integer.toHexString(mask));
   }

   protected void _reportInvalidOther(int mask, int ptr) throws JsonParseException {
      this._inputPtr = ptr;
      this._reportInvalidOther(mask);
   }

   public static enum Feature {
      REQUIRE_HEADER(true);

      final boolean _defaultState;
      final int _mask;

      public static int collectDefaults() {
         int flags = 0;
         SmileParser.Feature[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SmileParser.Feature f = arr$[i$];
            if (f.enabledByDefault()) {
               flags |= f.getMask();
            }
         }

         return flags;
      }

      private Feature(boolean defaultState) {
         this._defaultState = defaultState;
         this._mask = 1 << this.ordinal();
      }

      public boolean enabledByDefault() {
         return this._defaultState;
      }

      public int getMask() {
         return this._mask;
      }
   }
}
