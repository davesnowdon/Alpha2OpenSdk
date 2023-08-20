package org.codehaus.jackson.impl;

import java.io.IOException;
import java.io.Reader;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.io.IOContext;

public abstract class ReaderBasedNumericParser extends ReaderBasedParserBase {
   public ReaderBasedNumericParser(IOContext pc, int features, Reader r) {
      super(pc, features, r);
   }

   protected final JsonToken parseNumberText(int ch) throws IOException, JsonParseException {
      boolean negative;
      int startPtr;
      label135: {
         negative = ch == 45;
         int ptr = this._inputPtr;
         startPtr = ptr - 1;
         int inputLen = this._inputEnd;
         if (negative) {
            if (ptr >= this._inputEnd) {
               break label135;
            }

            ch = this._inputBuffer[ptr++];
            if (ch > 57 || ch < 48) {
               this._inputPtr = ptr;
               return this._handleInvalidNumberStart(ch, true);
            }
         }

         if (ch != 48) {
            label105:
            for(int intLen = 1; ptr < this._inputEnd; ++intLen) {
               int ch = this._inputBuffer[ptr++];
               if (ch < '0' || ch > '9') {
                  int fractLen = 0;
                  if (ch == '.') {
                     while(true) {
                        if (ptr >= inputLen) {
                           break label105;
                        }

                        ch = this._inputBuffer[ptr++];
                        if (ch < '0' || ch > '9') {
                           if (fractLen == 0) {
                              this.reportUnexpectedNumberChar(ch, "Decimal point not followed by a digit");
                           }
                           break;
                        }

                        ++fractLen;
                     }
                  }

                  int expLen = 0;
                  if (ch == 'e' || ch == 'E') {
                     if (ptr >= inputLen) {
                        break;
                     }

                     ch = this._inputBuffer[ptr++];
                     if (ch == '-' || ch == '+') {
                        if (ptr >= inputLen) {
                           break;
                        }

                        ch = this._inputBuffer[ptr++];
                     }

                     while(ch <= '9' && ch >= '0') {
                        ++expLen;
                        if (ptr >= inputLen) {
                           break label105;
                        }

                        ch = this._inputBuffer[ptr++];
                     }

                     if (expLen == 0) {
                        this.reportUnexpectedNumberChar(ch, "Exponent indicator not followed by a digit");
                     }
                  }

                  --ptr;
                  this._inputPtr = ptr;
                  int len = ptr - startPtr;
                  this._textBuffer.resetWithShared(this._inputBuffer, startPtr, len);
                  return this.reset(negative, intLen, fractLen, expLen);
               }
            }
         }
      }

      this._inputPtr = negative ? startPtr + 1 : startPtr;
      return this.parseNumberText2(negative);
   }

   private final JsonToken parseNumberText2(boolean negative) throws IOException, JsonParseException {
      char[] outBuf = this._textBuffer.emptyAndGetCurrentSegment();
      int outPtr = 0;
      if (negative) {
         outBuf[outPtr++] = '-';
      }

      int intLen = 0;
      char c = this._inputPtr < this._inputEnd ? this._inputBuffer[this._inputPtr++] : this.getNextChar("No digit following minus sign");
      if (c == '0') {
         c = this._verifyNoLeadingZeroes();
      }

      boolean eof;
      for(eof = false; c >= '0' && c <= '9'; c = this._inputBuffer[this._inputPtr++]) {
         ++intLen;
         if (outPtr >= outBuf.length) {
            outBuf = this._textBuffer.finishCurrentSegment();
            outPtr = 0;
         }

         outBuf[outPtr++] = c;
         if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
            c = 0;
            eof = true;
            break;
         }
      }

      if (intLen == 0) {
         this.reportInvalidNumber("Missing integer part (next char " + _getCharDesc(c) + ")");
      }

      int fractLen = 0;
      if (c == '.') {
         outBuf[outPtr++] = c;

         while(true) {
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
               eof = true;
               break;
            }

            c = this._inputBuffer[this._inputPtr++];
            if (c < '0' || c > '9') {
               break;
            }

            ++fractLen;
            if (outPtr >= outBuf.length) {
               outBuf = this._textBuffer.finishCurrentSegment();
               outPtr = 0;
            }

            outBuf[outPtr++] = c;
         }

         if (fractLen == 0) {
            this.reportUnexpectedNumberChar(c, "Decimal point not followed by a digit");
         }
      }

      int expLen = 0;
      if (c == 'e' || c == 'E') {
         if (outPtr >= outBuf.length) {
            outBuf = this._textBuffer.finishCurrentSegment();
            outPtr = 0;
         }

         outBuf[outPtr++] = c;
         c = this._inputPtr < this._inputEnd ? this._inputBuffer[this._inputPtr++] : this.getNextChar("expected a digit for number exponent");
         if (c == '-' || c == '+') {
            if (outPtr >= outBuf.length) {
               outBuf = this._textBuffer.finishCurrentSegment();
               outPtr = 0;
            }

            outBuf[outPtr++] = c;
            c = this._inputPtr < this._inputEnd ? this._inputBuffer[this._inputPtr++] : this.getNextChar("expected a digit for number exponent");
         }

         while(c <= '9' && c >= '0') {
            ++expLen;
            if (outPtr >= outBuf.length) {
               outBuf = this._textBuffer.finishCurrentSegment();
               outPtr = 0;
            }

            outBuf[outPtr++] = c;
            if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
               eof = true;
               break;
            }

            c = this._inputBuffer[this._inputPtr++];
         }

         if (expLen == 0) {
            this.reportUnexpectedNumberChar(c, "Exponent indicator not followed by a digit");
         }
      }

      if (!eof) {
         --this._inputPtr;
      }

      this._textBuffer.setCurrentLength(outPtr);
      return this.reset(negative, intLen, fractLen, expLen);
   }

   private final char _verifyNoLeadingZeroes() throws IOException, JsonParseException {
      if (this._inputPtr >= this._inputEnd && !this.loadMore()) {
         return '0';
      } else {
         char ch = this._inputBuffer[this._inputPtr];
         if (ch >= '0' && ch <= '9') {
            if (!this.isEnabled(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS)) {
               this.reportInvalidNumber("Leading zeroes not allowed");
            }

            ++this._inputPtr;
            if (ch == '0') {
               while(this._inputPtr < this._inputEnd || this.loadMore()) {
                  ch = this._inputBuffer[this._inputPtr];
                  if (ch < '0' || ch > '9') {
                     return '0';
                  }

                  ++this._inputPtr;
                  if (ch != '0') {
                     break;
                  }
               }
            }

            return ch;
         } else {
            return '0';
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
}
