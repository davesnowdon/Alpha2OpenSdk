package org.codehaus.jackson.impl;

import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.format.InputAccessor;
import org.codehaus.jackson.format.MatchStrength;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.io.MergedStream;
import org.codehaus.jackson.io.UTF32Reader;
import org.codehaus.jackson.sym.BytesToNameCanonicalizer;
import org.codehaus.jackson.sym.CharsToNameCanonicalizer;

public final class ByteSourceBootstrapper {
   static final byte UTF8_BOM_1 = -17;
   static final byte UTF8_BOM_2 = -69;
   static final byte UTF8_BOM_3 = -65;
   final IOContext _context;
   final InputStream _in;
   final byte[] _inputBuffer;
   private int _inputPtr;
   private int _inputEnd;
   private final boolean _bufferRecyclable;
   protected int _inputProcessed;
   protected boolean _bigEndian = true;
   protected int _bytesPerChar = 0;

   public ByteSourceBootstrapper(IOContext ctxt, InputStream in) {
      this._context = ctxt;
      this._in = in;
      this._inputBuffer = ctxt.allocReadIOBuffer();
      this._inputEnd = this._inputPtr = 0;
      this._inputProcessed = 0;
      this._bufferRecyclable = true;
   }

   public ByteSourceBootstrapper(IOContext ctxt, byte[] inputBuffer, int inputStart, int inputLen) {
      this._context = ctxt;
      this._in = null;
      this._inputBuffer = inputBuffer;
      this._inputPtr = inputStart;
      this._inputEnd = inputStart + inputLen;
      this._inputProcessed = -inputStart;
      this._bufferRecyclable = false;
   }

   public JsonEncoding detectEncoding() throws IOException, JsonParseException {
      boolean foundEncoding = false;
      int quad;
      if (this.ensureLoaded(4)) {
         quad = this._inputBuffer[this._inputPtr] << 24 | (this._inputBuffer[this._inputPtr + 1] & 255) << 16 | (this._inputBuffer[this._inputPtr + 2] & 255) << 8 | this._inputBuffer[this._inputPtr + 3] & 255;
         if (this.handleBOM(quad)) {
            foundEncoding = true;
         } else if (this.checkUTF32(quad)) {
            foundEncoding = true;
         } else if (this.checkUTF16(quad >>> 16)) {
            foundEncoding = true;
         }
      } else if (this.ensureLoaded(2)) {
         quad = (this._inputBuffer[this._inputPtr] & 255) << 8 | this._inputBuffer[this._inputPtr + 1] & 255;
         if (this.checkUTF16(quad)) {
            foundEncoding = true;
         }
      }

      JsonEncoding enc;
      if (!foundEncoding) {
         enc = JsonEncoding.UTF8;
      } else if (this._bytesPerChar == 2) {
         enc = this._bigEndian ? JsonEncoding.UTF16_BE : JsonEncoding.UTF16_LE;
      } else {
         if (this._bytesPerChar != 4) {
            throw new RuntimeException("Internal error");
         }

         enc = this._bigEndian ? JsonEncoding.UTF32_BE : JsonEncoding.UTF32_LE;
      }

      this._context.setEncoding(enc);
      return enc;
   }

   public Reader constructReader() throws IOException {
      JsonEncoding enc = this._context.getEncoding();
      switch(enc) {
      case UTF32_BE:
      case UTF32_LE:
         return new UTF32Reader(this._context, this._in, this._inputBuffer, this._inputPtr, this._inputEnd, this._context.getEncoding().isBigEndian());
      case UTF16_BE:
      case UTF16_LE:
      case UTF8:
         InputStream in = this._in;
         if (in == null) {
            in = new ByteArrayInputStream(this._inputBuffer, this._inputPtr, this._inputEnd);
         } else if (this._inputPtr < this._inputEnd) {
            in = new MergedStream(this._context, (InputStream)in, this._inputBuffer, this._inputPtr, this._inputEnd);
         }

         return new InputStreamReader((InputStream)in, enc.getJavaName());
      default:
         throw new RuntimeException("Internal error");
      }
   }

   public JsonParser constructParser(int features, ObjectCodec codec, BytesToNameCanonicalizer rootByteSymbols, CharsToNameCanonicalizer rootCharSymbols) throws IOException, JsonParseException {
      JsonEncoding enc = this.detectEncoding();
      boolean canonicalize = JsonParser.Feature.CANONICALIZE_FIELD_NAMES.enabledIn(features);
      boolean intern = JsonParser.Feature.INTERN_FIELD_NAMES.enabledIn(features);
      if (enc == JsonEncoding.UTF8 && canonicalize) {
         BytesToNameCanonicalizer can = rootByteSymbols.makeChild(canonicalize, intern);
         return new Utf8StreamParser(this._context, features, this._in, codec, can, this._inputBuffer, this._inputPtr, this._inputEnd, this._bufferRecyclable);
      } else {
         return new ReaderBasedParser(this._context, features, this.constructReader(), codec, rootCharSymbols.makeChild(canonicalize, intern));
      }
   }

   public static MatchStrength hasJSONFormat(InputAccessor acc) throws IOException {
      if (!acc.hasMoreBytes()) {
         return MatchStrength.INCONCLUSIVE;
      } else {
         byte b = acc.nextByte();
         if (b == -17) {
            if (!acc.hasMoreBytes()) {
               return MatchStrength.INCONCLUSIVE;
            }

            if (acc.nextByte() != -69) {
               return MatchStrength.NO_MATCH;
            }

            if (!acc.hasMoreBytes()) {
               return MatchStrength.INCONCLUSIVE;
            }

            if (acc.nextByte() != -65) {
               return MatchStrength.NO_MATCH;
            }

            if (!acc.hasMoreBytes()) {
               return MatchStrength.INCONCLUSIVE;
            }

            b = acc.nextByte();
         }

         int ch = skipSpace(acc, b);
         if (ch < 0) {
            return MatchStrength.INCONCLUSIVE;
         } else if (ch == 123) {
            ch = skipSpace(acc);
            if (ch < 0) {
               return MatchStrength.INCONCLUSIVE;
            } else {
               return ch != 34 && ch != 125 ? MatchStrength.NO_MATCH : MatchStrength.SOLID_MATCH;
            }
         } else if (ch == 91) {
            ch = skipSpace(acc);
            if (ch < 0) {
               return MatchStrength.INCONCLUSIVE;
            } else {
               return ch != 93 && ch != 91 ? MatchStrength.SOLID_MATCH : MatchStrength.SOLID_MATCH;
            }
         } else {
            MatchStrength strength = MatchStrength.WEAK_MATCH;
            if (ch == 34) {
               return strength;
            } else if (ch <= 57 && ch >= 48) {
               return strength;
            } else if (ch == 45) {
               ch = skipSpace(acc);
               if (ch < 0) {
                  return MatchStrength.INCONCLUSIVE;
               } else {
                  return ch <= 57 && ch >= 48 ? strength : MatchStrength.NO_MATCH;
               }
            } else if (ch == 110) {
               return tryMatch(acc, "ull", strength);
            } else if (ch == 116) {
               return tryMatch(acc, "rue", strength);
            } else {
               return ch == 102 ? tryMatch(acc, "alse", strength) : MatchStrength.NO_MATCH;
            }
         }
      }
   }

   private static final MatchStrength tryMatch(InputAccessor acc, String matchStr, MatchStrength fullMatchStrength) throws IOException {
      int i = 0;

      for(int len = matchStr.length(); i < len; ++i) {
         if (!acc.hasMoreBytes()) {
            return MatchStrength.INCONCLUSIVE;
         }

         if (acc.nextByte() != matchStr.charAt(i)) {
            return MatchStrength.NO_MATCH;
         }
      }

      return fullMatchStrength;
   }

   private static final int skipSpace(InputAccessor acc) throws IOException {
      return !acc.hasMoreBytes() ? -1 : skipSpace(acc, acc.nextByte());
   }

   private static final int skipSpace(InputAccessor acc, byte b) throws IOException {
      while(true) {
         int ch = b & 255;
         if (ch != 32 && ch != 13 && ch != 10 && ch != 9) {
            return ch;
         }

         if (!acc.hasMoreBytes()) {
            return -1;
         }

         b = acc.nextByte();
         ch = b & 255;
      }
   }

   private boolean handleBOM(int quad) throws IOException {
      switch(quad) {
      case -131072:
         this._inputPtr += 4;
         this._bytesPerChar = 4;
         this._bigEndian = false;
         return true;
      case 65279:
         this._bigEndian = true;
         this._inputPtr += 4;
         this._bytesPerChar = 4;
         return true;
      case 65534:
         this.reportWeirdUCS4("2143");
      case -16842752:
         this.reportWeirdUCS4("3412");
      default:
         int msw = quad >>> 16;
         if (msw == 65279) {
            this._inputPtr += 2;
            this._bytesPerChar = 2;
            this._bigEndian = true;
            return true;
         } else if (msw == 65534) {
            this._inputPtr += 2;
            this._bytesPerChar = 2;
            this._bigEndian = false;
            return true;
         } else if (quad >>> 8 == 15711167) {
            this._inputPtr += 3;
            this._bytesPerChar = 1;
            this._bigEndian = true;
            return true;
         } else {
            return false;
         }
      }
   }

   private boolean checkUTF32(int quad) throws IOException {
      if (quad >> 8 == 0) {
         this._bigEndian = true;
      } else if ((quad & 16777215) == 0) {
         this._bigEndian = false;
      } else if ((quad & -16711681) == 0) {
         this.reportWeirdUCS4("3412");
      } else {
         if ((quad & -65281) != 0) {
            return false;
         }

         this.reportWeirdUCS4("2143");
      }

      this._bytesPerChar = 4;
      return true;
   }

   private boolean checkUTF16(int i16) {
      if ((i16 & '\uff00') == 0) {
         this._bigEndian = true;
      } else {
         if ((i16 & 255) != 0) {
            return false;
         }

         this._bigEndian = false;
      }

      this._bytesPerChar = 2;
      return true;
   }

   private void reportWeirdUCS4(String type) throws IOException {
      throw new CharConversionException("Unsupported UCS-4 endianness (" + type + ") detected");
   }

   protected boolean ensureLoaded(int minimum) throws IOException {
      int count;
      for(int gotten = this._inputEnd - this._inputPtr; gotten < minimum; gotten += count) {
         if (this._in == null) {
            count = -1;
         } else {
            count = this._in.read(this._inputBuffer, this._inputEnd, this._inputBuffer.length - this._inputEnd);
         }

         if (count < 1) {
            return false;
         }

         this._inputEnd += count;
      }

      return true;
   }
}
