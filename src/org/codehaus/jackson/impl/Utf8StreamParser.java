package org.codehaus.jackson.impl;

import java.io.IOException;
import java.io.InputStream;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.sym.BytesToNameCanonicalizer;
import org.codehaus.jackson.sym.Name;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.util.CharTypes;

public final class Utf8StreamParser extends StreamBasedParserBase {
   static final byte BYTE_LF = 10;
   private static final int[] sInputCodesUtf8 = CharTypes.getInputCodeUtf8();
   private static final int[] sInputCodesLatin1 = CharTypes.getInputCodeLatin1();
   protected ObjectCodec _objectCodec;
   protected final BytesToNameCanonicalizer _symbols;
   protected int[] _quadBuffer = new int[16];
   protected boolean _tokenIncomplete = false;
   private int _quad1;

   public Utf8StreamParser(IOContext ctxt, int features, InputStream in, ObjectCodec codec, BytesToNameCanonicalizer sym, byte[] inputBuffer, int start, int end, boolean bufferRecyclable) {
      super(ctxt, features, in, inputBuffer, start, end, bufferRecyclable);
      this._objectCodec = codec;
      this._symbols = sym;
      if (!JsonParser.Feature.CANONICALIZE_FIELD_NAMES.enabledIn(features)) {
         this._throwInternal();
      }

   }

   public ObjectCodec getCodec() {
      return this._objectCodec;
   }

   public void setCodec(ObjectCodec c) {
      this._objectCodec = c;
   }

   public String getText() throws IOException, JsonParseException {
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

               if (!this._parsingContext.inObject()) {
                  return this._nextTokenNotInObject(i);
               } else {
                  Name n = this._parseFieldName(i);
                  this._parsingContext.setCurrentName(n.getName());
                  this._currToken = JsonToken.FIELD_NAME;
                  i = this._skipWS();
                  if (i != 58) {
                     this._reportUnexpectedChar(i, "was expecting a colon to separate field name and value");
                  }

                  i = this._skipWS();
                  if (i == 34) {
                     this._tokenIncomplete = true;
                     this._nextToken = JsonToken.VALUE_STRING;
                     return this._currToken;
                  } else {
                     JsonToken t;
                     switch(i) {
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
                        t = JsonToken.START_OBJECT;
                        break;
                     default:
                        t = this._handleUnexpectedValue(i);
                     }

                     this._nextToken = t;
                     return this._currToken;
                  }
               }
            }
         }
      }
   }

   private final JsonToken _nextTokenNotInObject(int i) throws IOException, JsonParseException {
      if (i == 34) {
         this._tokenIncomplete = true;
         return this._currToken = JsonToken.VALUE_STRING;
      } else {
         switch(i) {
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
            return this._currToken = this.parseNumberText(i);
         case 91:
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
            return this._currToken = JsonToken.START_ARRAY;
         case 93:
         case 125:
            this._reportUnexpectedChar(i, "expected a value");
         case 116:
            this._matchToken(JsonToken.VALUE_TRUE);
            return this._currToken = JsonToken.VALUE_TRUE;
         case 102:
            this._matchToken(JsonToken.VALUE_FALSE);
            return this._currToken = JsonToken.VALUE_FALSE;
         case 110:
            this._matchToken(JsonToken.VALUE_NULL);
            return this._currToken = JsonToken.VALUE_NULL;
         case 123:
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
            return this._currToken = JsonToken.START_OBJECT;
         default:
            return this._currToken = this._handleUnexpectedValue(i);
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

   protected final JsonToken parseNumberText(int c) throws IOException, JsonParseException {
      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
      int outPtr = 0;
      boolean negative = c == 45;
      if (negative) {
         outBuf[outPtr++] = '-';
         if (this._inputPtr >= this._inputEnd) {
            this.loadMoreGuaranteed();
         }

         c = this._inputBuffer[this._inputPtr++] & 255;
         if (c < 48 || c > 57) {
            return this._handleInvalidNumberStart(c, true);
         }
      }

      if (c == 48) {
         c = this._verifyNoLeadingZeroes();
      }

      outBuf[outPtr++] = (char)c;
      int intLen = 1;
      int end = this._inputPtr + outBuf.length;
      if (end > this._inputEnd) {
         end = this._inputEnd;
      }

      while(this._inputPtr < end) {
         c = this._inputBuffer[this._inputPtr++] & 255;
         if (c < 48 || c > 57) {
            if (c != 46 && c != 101 && c != 69) {
               --this._inputPtr;
               this._textBuffer.setCurrentLength(outPtr);
               return this.resetInt(negative, intLen);
            }

            return this._parseFloatText(outBuf, outPtr, c, negative, intLen);
         }

         ++intLen;
         outBuf[outPtr++] = (char)c;
      }

      return this._parserNumber2(outBuf, outPtr, negative, intLen);
   }

   private final JsonToken _parserNumber2(char[] outBuf, int outPtr, boolean negative, int intPartLength) throws IOException, JsonParseException {
      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         int c = this._inputBuffer[this._inputPtr++] & 255;
         if (c > 57 || c < 48) {
            if (c != 46 && c != 101 && c != 69) {
               --this._inputPtr;
               this._textBuffer.setCurrentLength(outPtr);
               return this.resetInt(negative, intPartLength);
            }

            return this._parseFloatText(outBuf, outPtr, c, negative, intPartLength);
         }

         if (outPtr >= outBuf.length) {
            outBuf = this._textBuffer.finishCurrentSegment();
            outPtr = 0;
         }

         outBuf[outPtr++] = (char)c;
         ++intPartLength;
      }

      this._textBuffer.setCurrentLength(outPtr);
      return this.resetInt(negative, intPartLength);
   }

   private final int _verifyNoLeadingZeroes() throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
         return 48;
      } else {
         int ch = this._inputBuffer[this._inputPtr] & 255;
         if (ch >= 48 && ch <= 57) {
            if (!this.isEnabled(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS)) {
               this.reportInvalidNumber("Leading zeroes not allowed");
            }

            ++this._inputPtr;
            if (ch == 48) {
               while(this._inputPtr < this._inputEnd || this.loadMore()) {
                  ch = this._inputBuffer[this._inputPtr] & 255;
                  if (ch < 48 || ch > 57) {
                     return 48;
                  }

                  ++this._inputPtr;
                  if (ch != 48) {
                     break;
                  }
               }
            }

            return ch;
         } else {
            return 48;
         }
      }
   }

   private final JsonToken _parseFloatText(char[] outBuf, int outPtr, int c, boolean negative, int integerPartLength) throws IOException, JsonParseException {
      int fractLen = 0;
      boolean eof = false;
      if (c == 46) {
         outBuf[outPtr++] = (char)c;

         while(true) {
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
               eof = true;
               break;
            }

            c = this._inputBuffer[this._inputPtr++] & 255;
            if (c < 48 || c > 57) {
               break;
            }

            ++fractLen;
            if (outPtr >= outBuf.length) {
               outBuf = this._textBuffer.finishCurrentSegment();
               outPtr = 0;
            }

            outBuf[outPtr++] = (char)c;
         }

         if (fractLen == 0) {
            this.reportUnexpectedNumberChar(c, "Decimal point not followed by a digit");
         }
      }

      int expLen = 0;
      if (c == 101 || c == 69) {
         if (outPtr >= outBuf.length) {
            outBuf = this._textBuffer.finishCurrentSegment();
            outPtr = 0;
         }

         outBuf[outPtr++] = (char)c;
         if (this._inputPtr >= this._inputEnd) {
            this.loadMoreGuaranteed();
         }

         c = this._inputBuffer[this._inputPtr++] & 255;
         if (c == 45 || c == 43) {
            if (outPtr >= outBuf.length) {
               outBuf = this._textBuffer.finishCurrentSegment();
               outPtr = 0;
            }

            outBuf[outPtr++] = (char)c;
            if (this._inputPtr >= this._inputEnd) {
               this.loadMoreGuaranteed();
            }

            c = this._inputBuffer[this._inputPtr++] & 255;
         }

         while(c <= 57 && c >= 48) {
            ++expLen;
            if (outPtr >= outBuf.length) {
               outBuf = this._textBuffer.finishCurrentSegment();
               outPtr = 0;
            }

            outBuf[outPtr++] = (char)c;
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
               eof = true;
               break;
            }

            c = this._inputBuffer[this._inputPtr++] & 255;
         }

         if (expLen == 0) {
            this.reportUnexpectedNumberChar(c, "Exponent indicator not followed by a digit");
         }
      }

      if (!eof) {
         --this._inputPtr;
      }

      this._textBuffer.setCurrentLength(outPtr);
      return this.resetFloat(negative, integerPartLength, fractLen, expLen);
   }

   protected final Name _parseFieldName(int i) throws IOException, JsonParseException {
      if (i != 34) {
         return this._handleUnusualFieldName(i);
      } else if (this._inputPtr + 9 > this._inputEnd) {
         return this.slowParseFieldName();
      } else {
         byte[] input = this._inputBuffer;
         int[] codes = sInputCodesLatin1;
         int q = input[this._inputPtr++] & 255;
         if (codes[q] == 0) {
            i = input[this._inputPtr++] & 255;
            if (codes[i] == 0) {
               q = q << 8 | i;
               i = input[this._inputPtr++] & 255;
               if (codes[i] == 0) {
                  q = q << 8 | i;
                  i = input[this._inputPtr++] & 255;
                  if (codes[i] == 0) {
                     q = q << 8 | i;
                     i = input[this._inputPtr++] & 255;
                     if (codes[i] == 0) {
                        this._quad1 = q;
                        return this.parseMediumFieldName(i, codes);
                     } else {
                        return i == 34 ? this.findName(q, 4) : this.parseFieldName(q, i, 4);
                     }
                  } else {
                     return i == 34 ? this.findName(q, 3) : this.parseFieldName(q, i, 3);
                  }
               } else {
                  return i == 34 ? this.findName(q, 2) : this.parseFieldName(q, i, 2);
               }
            } else {
               return i == 34 ? this.findName(q, 1) : this.parseFieldName(q, i, 1);
            }
         } else {
            return q == 34 ? BytesToNameCanonicalizer.getEmptyName() : this.parseFieldName(0, q, 0);
         }
      }
   }

   protected final Name parseMediumFieldName(int q2, int[] codes) throws IOException, JsonParseException {
      int i = this._inputBuffer[this._inputPtr++] & 255;
      if (codes[i] != 0) {
         return i == 34 ? this.findName(this._quad1, q2, 1) : this.parseFieldName(this._quad1, q2, i, 1);
      } else {
         q2 = q2 << 8 | i;
         i = this._inputBuffer[this._inputPtr++] & 255;
         if (codes[i] != 0) {
            return i == 34 ? this.findName(this._quad1, q2, 2) : this.parseFieldName(this._quad1, q2, i, 2);
         } else {
            q2 = q2 << 8 | i;
            i = this._inputBuffer[this._inputPtr++] & 255;
            if (codes[i] != 0) {
               return i == 34 ? this.findName(this._quad1, q2, 3) : this.parseFieldName(this._quad1, q2, i, 3);
            } else {
               q2 = q2 << 8 | i;
               i = this._inputBuffer[this._inputPtr++] & 255;
               if (codes[i] != 0) {
                  return i == 34 ? this.findName(this._quad1, q2, 4) : this.parseFieldName(this._quad1, q2, i, 4);
               } else {
                  this._quadBuffer[0] = this._quad1;
                  this._quadBuffer[1] = q2;
                  return this.parseLongFieldName(i);
               }
            }
         }
      }
   }

   protected Name parseLongFieldName(int q) throws IOException, JsonParseException {
      int[] codes = sInputCodesLatin1;

      int qlen;
      int i;
      for(qlen = 2; this._inputEnd - this._inputPtr >= 4; q = i) {
         i = this._inputBuffer[this._inputPtr++] & 255;
         if (codes[i] != 0) {
            if (i == 34) {
               return this.findName(this._quadBuffer, qlen, q, 1);
            }

            return this.parseEscapedFieldName(this._quadBuffer, qlen, q, i, 1);
         }

         q = q << 8 | i;
         i = this._inputBuffer[this._inputPtr++] & 255;
         if (codes[i] != 0) {
            if (i == 34) {
               return this.findName(this._quadBuffer, qlen, q, 2);
            }

            return this.parseEscapedFieldName(this._quadBuffer, qlen, q, i, 2);
         }

         q = q << 8 | i;
         i = this._inputBuffer[this._inputPtr++] & 255;
         if (codes[i] != 0) {
            if (i == 34) {
               return this.findName(this._quadBuffer, qlen, q, 3);
            }

            return this.parseEscapedFieldName(this._quadBuffer, qlen, q, i, 3);
         }

         q = q << 8 | i;
         i = this._inputBuffer[this._inputPtr++] & 255;
         if (codes[i] != 0) {
            if (i == 34) {
               return this.findName(this._quadBuffer, qlen, q, 4);
            }

            return this.parseEscapedFieldName(this._quadBuffer, qlen, q, i, 4);
         }

         if (qlen >= this._quadBuffer.length) {
            this._quadBuffer = growArrayBy(this._quadBuffer, qlen);
         }

         this._quadBuffer[qlen++] = q;
      }

      return this.parseEscapedFieldName(this._quadBuffer, qlen, 0, q, 0);
   }

   protected Name slowParseFieldName() throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
         this._reportInvalidEOF(": was expecting closing '\"' for name");
      }

      int i = this._inputBuffer[this._inputPtr++] & 255;
      return i == 34 ? BytesToNameCanonicalizer.getEmptyName() : this.parseEscapedFieldName(this._quadBuffer, 0, 0, i, 0);
   }

   private final Name parseFieldName(int q1, int ch, int lastQuadBytes) throws IOException, JsonParseException {
      return this.parseEscapedFieldName(this._quadBuffer, 0, q1, ch, lastQuadBytes);
   }

   private final Name parseFieldName(int q1, int q2, int ch, int lastQuadBytes) throws IOException, JsonParseException {
      this._quadBuffer[0] = q1;
      return this.parseEscapedFieldName(this._quadBuffer, 1, q2, ch, lastQuadBytes);
   }

   protected Name parseEscapedFieldName(int[] quads, int qlen, int currQuad, int ch, int currQuadBytes) throws IOException, JsonParseException {
      int[] codes = sInputCodesLatin1;

      while(true) {
         if (codes[ch] != 0) {
            if (ch == 34) {
               if (currQuadBytes > 0) {
                  if (qlen >= quads.length) {
                     this._quadBuffer = quads = growArrayBy(quads, quads.length);
                  }

                  quads[qlen++] = currQuad;
               }

               Name name = this._symbols.findName(quads, qlen);
               if (name == null) {
                  name = this.addName(quads, qlen, currQuadBytes);
               }

               return name;
            }

            if (ch != 92) {
               this._throwUnquotedSpace(ch, "name");
            } else {
               ch = this._decodeEscaped();
            }

            if (ch > 127) {
               if (currQuadBytes >= 4) {
                  if (qlen >= quads.length) {
                     this._quadBuffer = quads = growArrayBy(quads, quads.length);
                  }

                  quads[qlen++] = currQuad;
                  currQuad = 0;
                  currQuadBytes = 0;
               }

               if (ch < 2048) {
                  currQuad = currQuad << 8 | 192 | ch >> 6;
                  ++currQuadBytes;
               } else {
                  currQuad = currQuad << 8 | 224 | ch >> 12;
                  ++currQuadBytes;
                  if (currQuadBytes >= 4) {
                     if (qlen >= quads.length) {
                        this._quadBuffer = quads = growArrayBy(quads, quads.length);
                     }

                     quads[qlen++] = currQuad;
                     currQuad = 0;
                     currQuadBytes = 0;
                  }

                  currQuad = currQuad << 8 | 128 | ch >> 6 & 63;
                  ++currQuadBytes;
               }

               ch = 128 | ch & 63;
            }
         }

         if (currQuadBytes < 4) {
            ++currQuadBytes;
            currQuad = currQuad << 8 | ch;
         } else {
            if (qlen >= quads.length) {
               this._quadBuffer = quads = growArrayBy(quads, quads.length);
            }

            quads[qlen++] = currQuad;
            currQuad = ch;
            currQuadBytes = 1;
         }

         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOF(" in field name");
         }

         ch = this._inputBuffer[this._inputPtr++] & 255;
      }
   }

   protected final Name _handleUnusualFieldName(int ch) throws IOException, JsonParseException {
      if (ch == 39 && this.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES)) {
         return this._parseApostropheFieldName();
      } else {
         if (!this.isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)) {
            this._reportUnexpectedChar(ch, "was expecting double-quote to start field name");
         }

         int[] codes = CharTypes.getInputCodeUtf8JsNames();
         if (codes[ch] != 0) {
            this._reportUnexpectedChar(ch, "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
         }

         int[] quads = this._quadBuffer;
         int qlen = 0;
         int currQuad = 0;
         int currQuadBytes = 0;

         while(true) {
            if (currQuadBytes < 4) {
               ++currQuadBytes;
               currQuad = currQuad << 8 | ch;
            } else {
               if (qlen >= quads.length) {
                  this._quadBuffer = quads = growArrayBy(quads, quads.length);
               }

               quads[qlen++] = currQuad;
               currQuad = ch;
               currQuadBytes = 1;
            }

            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
               this._reportInvalidEOF(" in field name");
            }

            ch = this._inputBuffer[this._inputPtr] & 255;
            if (codes[ch] != 0) {
               if (currQuadBytes > 0) {
                  if (qlen >= quads.length) {
                     this._quadBuffer = quads = growArrayBy(quads, quads.length);
                  }

                  quads[qlen++] = currQuad;
               }

               Name name = this._symbols.findName(quads, qlen);
               if (name == null) {
                  name = this.addName(quads, qlen, currQuadBytes);
               }

               return name;
            }

            ++this._inputPtr;
         }
      }
   }

   protected final Name _parseApostropheFieldName() throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
         this._reportInvalidEOF(": was expecting closing ''' for name");
      }

      int ch = this._inputBuffer[this._inputPtr++] & 255;
      if (ch == 39) {
         return BytesToNameCanonicalizer.getEmptyName();
      } else {
         int[] quads = this._quadBuffer;
         int qlen = 0;
         int currQuad = 0;
         int currQuadBytes = 0;

         for(int[] codes = sInputCodesLatin1; ch != 39; ch = this._inputBuffer[this._inputPtr++] & 255) {
            if (ch != 34 && codes[ch] != 0) {
               if (ch != 92) {
                  this._throwUnquotedSpace(ch, "name");
               } else {
                  ch = this._decodeEscaped();
               }

               if (ch > 127) {
                  if (currQuadBytes >= 4) {
                     if (qlen >= quads.length) {
                        this._quadBuffer = quads = growArrayBy(quads, quads.length);
                     }

                     quads[qlen++] = currQuad;
                     currQuad = 0;
                     currQuadBytes = 0;
                  }

                  if (ch < 2048) {
                     currQuad = currQuad << 8 | 192 | ch >> 6;
                     ++currQuadBytes;
                  } else {
                     currQuad = currQuad << 8 | 224 | ch >> 12;
                     ++currQuadBytes;
                     if (currQuadBytes >= 4) {
                        if (qlen >= quads.length) {
                           this._quadBuffer = quads = growArrayBy(quads, quads.length);
                        }

                        quads[qlen++] = currQuad;
                        currQuad = 0;
                        currQuadBytes = 0;
                     }

                     currQuad = currQuad << 8 | 128 | ch >> 6 & 63;
                     ++currQuadBytes;
                  }

                  ch = 128 | ch & 63;
               }
            }

            if (currQuadBytes < 4) {
               ++currQuadBytes;
               currQuad = currQuad << 8 | ch;
            } else {
               if (qlen >= quads.length) {
                  this._quadBuffer = quads = growArrayBy(quads, quads.length);
               }

               quads[qlen++] = currQuad;
               currQuad = ch;
               currQuadBytes = 1;
            }

            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
               this._reportInvalidEOF(" in field name");
            }
         }

         if (currQuadBytes > 0) {
            if (qlen >= quads.length) {
               this._quadBuffer = quads = growArrayBy(quads, quads.length);
            }

            quads[qlen++] = currQuad;
         }

         Name name = this._symbols.findName(quads, qlen);
         if (name == null) {
            name = this.addName(quads, qlen, currQuadBytes);
         }

         return name;
      }
   }

   private final Name findName(int q1, int lastQuadBytes) throws JsonParseException {
      Name name = this._symbols.findName(q1);
      if (name != null) {
         return name;
      } else {
         this._quadBuffer[0] = q1;
         return this.addName(this._quadBuffer, 1, lastQuadBytes);
      }
   }

   private final Name findName(int q1, int q2, int lastQuadBytes) throws JsonParseException {
      Name name = this._symbols.findName(q1, q2);
      if (name != null) {
         return name;
      } else {
         this._quadBuffer[0] = q1;
         this._quadBuffer[1] = q2;
         return this.addName(this._quadBuffer, 2, lastQuadBytes);
      }
   }

   private final Name findName(int[] quads, int qlen, int lastQuad, int lastQuadBytes) throws JsonParseException {
      if (qlen >= quads.length) {
         this._quadBuffer = quads = growArrayBy(quads, quads.length);
      }

      quads[qlen++] = lastQuad;
      Name name = this._symbols.findName(quads, qlen);
      return name == null ? this.addName(quads, qlen, lastQuadBytes) : name;
   }

   private final Name addName(int[] quads, int qlen, int lastQuadBytes) throws JsonParseException {
      int byteLen = (qlen << 2) - 4 + lastQuadBytes;
      int lastQuad;
      if (lastQuadBytes < 4) {
         lastQuad = quads[qlen - 1];
         quads[qlen - 1] = lastQuad << (4 - lastQuadBytes << 3);
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
               this._reportInvalidEOF(" in field name");
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
         quads[qlen - 1] = lastQuad;
      }

      return this._symbols.addName(baseName, quads, qlen);
   }

   protected void _finishString() throws IOException, JsonParseException {
      int ptr = this._inputPtr;
      if (ptr >= this._inputEnd) {
         this.loadMoreGuaranteed();
         ptr = this._inputPtr;
      }

      int outPtr = 0;
      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
      int[] codes = sInputCodesUtf8;
      int max = Math.min(this._inputEnd, ptr + outBuf.length);

      int c;
      for(byte[] inputBuffer = this._inputBuffer; ptr < max; outBuf[outPtr++] = (char)c) {
         c = inputBuffer[ptr] & 255;
         if (codes[c] != 0) {
            if (c == 34) {
               this._inputPtr = ptr + 1;
               this._textBuffer.setCurrentLength(outPtr);
               return;
            }
            break;
         }

         ++ptr;
      }

      this._inputPtr = ptr;
      this._finishString2(outBuf, outPtr);
   }

   private final void _finishString2(char[] outBuf, int outPtr) throws IOException, JsonParseException {
      int[] codes = sInputCodesUtf8;
      byte[] inputBuffer = this._inputBuffer;

      while(true) {
         label54:
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

            int c;
            for(int max = Math.min(this._inputEnd, ptr + (outBuf.length - outPtr)); ptr < max; outBuf[outPtr++] = (char)c) {
               c = inputBuffer[ptr++] & 255;
               if (codes[c] != 0) {
                  this._inputPtr = ptr;
                  if (c == 34) {
                     this._textBuffer.setCurrentLength(outPtr);
                     return;
                  }

                  switch(codes[c]) {
                  case 1:
                     c = this._decodeEscaped();
                     break;
                  case 2:
                     c = this._decodeUtf8_2(c);
                     break;
                  case 3:
                     if (this._inputEnd - this._inputPtr >= 2) {
                        c = this._decodeUtf8_3fast(c);
                     } else {
                        c = this._decodeUtf8_3(c);
                     }
                     break;
                  case 4:
                     c = this._decodeUtf8_4(c);
                     outBuf[outPtr++] = (char)('\ud800' | c >> 10);
                     if (outPtr >= outBuf.length) {
                        outBuf = this._textBuffer.finishCurrentSegment();
                        outPtr = 0;
                     }

                     c = '\udc00' | c & 1023;
                     break;
                  default:
                     if (c < 32) {
                        this._throwUnquotedSpace(c, "string value");
                     } else {
                        this._reportInvalidChar(c);
                     }
                  }

                  if (outPtr >= outBuf.length) {
                     outBuf = this._textBuffer.finishCurrentSegment();
                     outPtr = 0;
                  }

                  outBuf[outPtr++] = (char)c;
                  continue label54;
               }
            }

            this._inputPtr = ptr;
         }
      }
   }

   protected void _skipString() throws IOException, JsonParseException {
      this._tokenIncomplete = false;
      int[] codes = sInputCodesUtf8;
      byte[] inputBuffer = this._inputBuffer;

      while(true) {
         label34:
         while(true) {
            int ptr = this._inputPtr;
            int max = this._inputEnd;
            if (ptr >= max) {
               this.loadMoreGuaranteed();
               ptr = this._inputPtr;
               max = this._inputEnd;
            }

            while(ptr < max) {
               int c = inputBuffer[ptr++] & 255;
               if (codes[c] != 0) {
                  this._inputPtr = ptr;
                  if (c == 34) {
                     return;
                  }

                  switch(codes[c]) {
                  case 1:
                     this._decodeEscaped();
                     continue label34;
                  case 2:
                     this._skipUtf8_2(c);
                     continue label34;
                  case 3:
                     this._skipUtf8_3(c);
                     continue label34;
                  case 4:
                     this._skipUtf8_4(c);
                     continue label34;
                  default:
                     if (c < 32) {
                        this._throwUnquotedSpace(c, "string value");
                     } else {
                        this._reportInvalidChar(c);
                     }
                     continue label34;
                  }
               }
            }

            this._inputPtr = ptr;
         }
      }
   }

   protected JsonToken _handleUnexpectedValue(int c) throws IOException, JsonParseException {
      switch(c) {
      case 39:
         if (this.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES)) {
            return this._handleApostropheValue();
         }
         break;
      case 43:
         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOFInValue();
         }

         return this._handleInvalidNumberStart(this._inputBuffer[this._inputPtr++] & 255, false);
      case 78:
         if (this._matchToken("NaN", 1)) {
            if (this.isEnabled(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
               return this.resetAsNaN("NaN", 0.0D / 0.0);
            }

            this._reportError("Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
         }

         this._reportUnexpectedChar(this._inputBuffer[this._inputPtr++] & 255, "expected 'NaN' or a valid value");
      }

      this._reportUnexpectedChar(c, "expected a valid value (number, String, array, object, 'true', 'false' or 'null')");
      return null;
   }

   protected JsonToken _handleApostropheValue() throws IOException, JsonParseException {
      int c = false;
      int outPtr = 0;
      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
      int[] codes = sInputCodesUtf8;
      byte[] inputBuffer = this._inputBuffer;

      while(true) {
         while(true) {
            if (this._inputPtr >= this._inputEnd) {
               this.loadMoreGuaranteed();
            }

            if (outPtr >= outBuf.length) {
               outBuf = this._textBuffer.finishCurrentSegment();
               outPtr = 0;
            }

            int max = this._inputEnd;
            int max2 = this._inputPtr + (outBuf.length - outPtr);
            if (max2 < max) {
               max = max2;
            }

            while(this._inputPtr < max) {
               int c = inputBuffer[this._inputPtr++] & 255;
               if (c == 39 || codes[c] != 0) {
                  if (c == 39) {
                     this._textBuffer.setCurrentLength(outPtr);
                     return JsonToken.VALUE_STRING;
                  }

                  switch(codes[c]) {
                  case 1:
                     if (c != 34) {
                        c = this._decodeEscaped();
                     }
                     break;
                  case 2:
                     c = this._decodeUtf8_2(c);
                     break;
                  case 3:
                     if (this._inputEnd - this._inputPtr >= 2) {
                        c = this._decodeUtf8_3fast(c);
                     } else {
                        c = this._decodeUtf8_3(c);
                     }
                     break;
                  case 4:
                     c = this._decodeUtf8_4(c);
                     outBuf[outPtr++] = (char)('\ud800' | c >> 10);
                     if (outPtr >= outBuf.length) {
                        outBuf = this._textBuffer.finishCurrentSegment();
                        outPtr = 0;
                     }

                     c = '\udc00' | c & 1023;
                     break;
                  default:
                     if (c < 32) {
                        this._throwUnquotedSpace(c, "string value");
                     }

                     this._reportInvalidChar(c);
                  }

                  if (outPtr >= outBuf.length) {
                     outBuf = this._textBuffer.finishCurrentSegment();
                     outPtr = 0;
                  }

                  outBuf[outPtr++] = (char)c;
                  break;
               }

               outBuf[outPtr++] = (char)c;
            }
         }
      }
   }

   protected JsonToken _handleInvalidNumberStart(int ch, boolean negative) throws IOException, JsonParseException {
      if (ch == 73) {
         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOFInValue();
         }

         ch = this._inputBuffer[this._inputPtr++];
         String match;
         if (ch == 78) {
            match = negative ? "-INF" : "+INF";
            if (this._matchToken(match, 3)) {
               if (this.isEnabled(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                  return this.resetAsNaN(match, negative ? -1.0D / 0.0 : 1.0D / 0.0);
               }

               this._reportError("Non-standard token '" + match + "': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            }
         } else if (ch == 110) {
            match = negative ? "-Infinity" : "+Infinity";
            if (this._matchToken(match, 3)) {
               if (this.isEnabled(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                  return this.resetAsNaN(match, negative ? -1.0D / 0.0 : 1.0D / 0.0);
               }

               this._reportError("Non-standard token '" + match + "': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            }
         }
      }

      this.reportUnexpectedNumberChar(ch, "expected digit (0-9) to follow minus sign, for valid numeric value");
      return null;
   }

   protected void _matchToken(JsonToken token) throws IOException, JsonParseException {
      byte[] matchBytes = token.asByteArray();
      int i = 1;

      for(int len = matchBytes.length; i < len; ++i) {
         if (this._inputPtr >= this._inputEnd) {
            this.loadMoreGuaranteed();
         }

         if (matchBytes[i] != this._inputBuffer[this._inputPtr]) {
            this._reportInvalidToken(token.asString().substring(0, i), "'null', 'true' or 'false'");
         }

         ++this._inputPtr;
      }

   }

   protected final boolean _matchToken(String matchStr, int i) throws IOException, JsonParseException {
      int len = matchStr.length();

      do {
         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            this._reportInvalidEOF(" in a value");
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
         char c = (char)this._decodeCharForError(this._inputBuffer[this._inputPtr] & 255);
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
         int i = this._inputBuffer[this._inputPtr++];
         char c = (char)this._decodeCharForError(i);
         if (!Character.isJavaIdentifierPart(c)) {
            break;
         }

         ++this._inputPtr;
         sb.append(c);
      }

      this._reportError("Unrecognized token '" + sb.toString() + "': was expecting " + msg);
   }

   private final int _skipWS() throws IOException, JsonParseException {
      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         int i = this._inputBuffer[this._inputPtr++] & 255;
         if (i > 32) {
            if (i != 47) {
               return i;
            }

            this._skipComment();
         } else if (i != 32) {
            if (i == 10) {
               this._skipLF();
            } else if (i == 13) {
               this._skipCR();
            } else if (i != 9) {
               this._throwInvalidSpace(i);
            }
         }
      }

      throw this._constructError("Unexpected end-of-input within/between " + this._parsingContext.getTypeDesc() + " entries");
   }

   private final int _skipWSOrEnd() throws IOException, JsonParseException {
      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         int i = this._inputBuffer[this._inputPtr++] & 255;
         if (i > 32) {
            if (i != 47) {
               return i;
            }

            this._skipComment();
         } else if (i != 32) {
            if (i == 10) {
               this._skipLF();
            } else if (i == 13) {
               this._skipCR();
            } else if (i != 9) {
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

      int c = this._inputBuffer[this._inputPtr++] & 255;
      if (c == 47) {
         this._skipCppComment();
      } else if (c == 42) {
         this._skipCComment();
      } else {
         this._reportUnexpectedChar(c, "was expecting either '*' or '/' for a comment");
      }

   }

   private final void _skipCComment() throws IOException, JsonParseException {
      int[] codes = CharTypes.getInputCodeComment();

      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         int i = this._inputBuffer[this._inputPtr++] & 255;
         int code = codes[i];
         if (code != 0) {
            switch(code) {
            case 10:
               this._skipLF();
               break;
            case 13:
               this._skipCR();
               break;
            case 42:
               if (this._inputBuffer[this._inputPtr] == 47) {
                  ++this._inputPtr;
                  return;
               }
               break;
            default:
               this._reportInvalidChar(i);
            }
         }
      }

      this._reportInvalidEOF(" in a comment");
   }

   private final void _skipCppComment() throws IOException, JsonParseException {
      int[] codes = CharTypes.getInputCodeComment();

      while(this._inputPtr < this._inputEnd || this.loadMore()) {
         int i = this._inputBuffer[this._inputPtr++] & 255;
         int code = codes[i];
         if (code != 0) {
            switch(code) {
            case 10:
               this._skipLF();
               return;
            case 13:
               this._skipCR();
               return;
            case 42:
               break;
            default:
               this._reportInvalidChar(i);
            }
         }
      }

   }

   protected final char _decodeEscaped() throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
         this._reportInvalidEOF(" in character escape sequence");
      }

      int c = this._inputBuffer[this._inputPtr++];
      switch(c) {
      case 34:
      case 47:
      case 92:
         return (char)c;
      case 98:
         return '\b';
      case 102:
         return '\f';
      case 110:
         return '\n';
      case 114:
         return '\r';
      case 116:
         return '\t';
      case 117:
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
         return this._handleUnrecognizedCharacterEscape((char)this._decodeCharForError(c));
      }
   }

   protected int _decodeCharForError(int firstByte) throws IOException, JsonParseException {
      int c = firstByte;
      if (firstByte < 0) {
         byte needed;
         if ((firstByte & 224) == 192) {
            c = firstByte & 31;
            needed = 1;
         } else if ((firstByte & 240) == 224) {
            c = firstByte & 15;
            needed = 2;
         } else if ((firstByte & 248) == 240) {
            c = firstByte & 7;
            needed = 3;
         } else {
            this._reportInvalidInitial(firstByte & 255);
            needed = 1;
         }

         int d = this.nextByte();
         if ((d & 192) != 128) {
            this._reportInvalidOther(d & 255);
         }

         c = c << 6 | d & 63;
         if (needed > 1) {
            d = this.nextByte();
            if ((d & 192) != 128) {
               this._reportInvalidOther(d & 255);
            }

            c = c << 6 | d & 63;
            if (needed > 2) {
               d = this.nextByte();
               if ((d & 192) != 128) {
                  this._reportInvalidOther(d & 255);
               }

               c = c << 6 | d & 63;
            }
         }
      }

      return c;
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

   private final void _skipUtf8_2(int c) throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      int c = this._inputBuffer[this._inputPtr++];
      if ((c & 192) != 128) {
         this._reportInvalidOther(c & 255, this._inputPtr);
      }

   }

   private final void _skipUtf8_3(int c) throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      int c = this._inputBuffer[this._inputPtr++];
      if ((c & 192) != 128) {
         this._reportInvalidOther(c & 255, this._inputPtr);
      }

      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      c = this._inputBuffer[this._inputPtr++];
      if ((c & 192) != 128) {
         this._reportInvalidOther(c & 255, this._inputPtr);
      }

   }

   private final void _skipUtf8_4(int c) throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      int d = this._inputBuffer[this._inputPtr++];
      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      d = this._inputBuffer[this._inputPtr++];
      if ((d & 192) != 128) {
         this._reportInvalidOther(d & 255, this._inputPtr);
      }

   }

   protected final void _skipCR() throws IOException {
      if ((this._inputPtr < this._inputEnd || this.loadMore()) && this._inputBuffer[this._inputPtr] == 10) {
         ++this._inputPtr;
      }

      ++this._currInputRow;
      this._currInputRowStart = this._inputPtr;
   }

   protected final void _skipLF() throws IOException {
      ++this._currInputRow;
      this._currInputRowStart = this._inputPtr;
   }

   private int nextByte() throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd) {
         this.loadMoreGuaranteed();
      }

      return this._inputBuffer[this._inputPtr++] & 255;
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

   public static int[] growArrayBy(int[] arr, int more) {
      if (arr == null) {
         return new int[more];
      } else {
         int[] old = arr;
         int len = arr.length;
         arr = new int[len + more];
         System.arraycopy(old, 0, arr, 0, len);
         return arr;
      }
   }

   protected byte[] _decodeBase64(Base64Variant b64variant) throws IOException, JsonParseException {
      ByteArrayBuilder builder = this._getByteArrayBuilder();

      while(true) {
         while(true) {
            int ch;
            int bits;
            do {
               do {
                  if (this._inputPtr >= this._inputEnd) {
                     this.loadMoreGuaranteed();
                  }

                  ch = this._inputBuffer[this._inputPtr++] & 255;
               } while(ch <= 32);

               bits = b64variant.decodeBase64Char(ch);
               if (bits >= 0) {
                  break;
               }

               if (ch == 34) {
                  return builder.toByteArray();
               }

               bits = this._decodeBase64Escape(b64variant, ch, 0);
            } while(bits < 0);

            int decodedData = bits;
            if (this._inputPtr >= this._inputEnd) {
               this.loadMoreGuaranteed();
            }

            ch = this._inputBuffer[this._inputPtr++] & 255;
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
               bits = this._decodeBase64Escape(b64variant, ch, 1);
            }

            decodedData = decodedData << 6 | bits;
            if (this._inputPtr >= this._inputEnd) {
               this.loadMoreGuaranteed();
            }

            ch = this._inputBuffer[this._inputPtr++] & 255;
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
               if (bits != -2) {
                  bits = this._decodeBase64Escape(b64variant, ch, 2);
               }

               if (bits == -2) {
                  if (this._inputPtr >= this._inputEnd) {
                     this.loadMoreGuaranteed();
                  }

                  ch = this._inputBuffer[this._inputPtr++] & 255;
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

            ch = this._inputBuffer[this._inputPtr++] & 255;
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

   private final int _decodeBase64Escape(Base64Variant b64variant, int ch, int index) throws IOException, JsonParseException {
      if (ch != 92) {
         throw this.reportInvalidChar(b64variant, ch, index);
      } else {
         int unescaped = this._decodeEscaped();
         if (unescaped <= ' ' && index == 0) {
            return -1;
         } else {
            int bits = b64variant.decodeBase64Char((int)unescaped);
            if (bits < 0) {
               throw this.reportInvalidChar(b64variant, unescaped, index);
            } else {
               return bits;
            }
         }
      }
   }

   protected IllegalArgumentException reportInvalidChar(Base64Variant b64variant, int ch, int bindex) throws IllegalArgumentException {
      return this.reportInvalidChar(b64variant, ch, bindex, (String)null);
   }

   protected IllegalArgumentException reportInvalidChar(Base64Variant b64variant, int ch, int bindex, String msg) throws IllegalArgumentException {
      String base;
      if (ch <= 32) {
         base = "Illegal white space character (code 0x" + Integer.toHexString(ch) + ") as character #" + (bindex + 1) + " of 4-char base64 unit: can only used between units";
      } else if (b64variant.usesPaddingChar(ch)) {
         base = "Unexpected padding character ('" + b64variant.getPaddingChar() + "') as character #" + (bindex + 1) + " of 4-char base64 unit: padding only legal as 3rd or 4th character";
      } else if (Character.isDefined(ch) && !Character.isISOControl(ch)) {
         base = "Illegal character '" + (char)ch + "' (code 0x" + Integer.toHexString(ch) + ") in base64 content";
      } else {
         base = "Illegal character (code 0x" + Integer.toHexString(ch) + ") in base64 content";
      }

      if (msg != null) {
         base = base + ": " + msg;
      }

      return new IllegalArgumentException(base);
   }
}
