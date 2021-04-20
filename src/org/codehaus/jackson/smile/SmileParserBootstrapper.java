package org.codehaus.jackson.smile;

import java.io.IOException;
import java.io.InputStream;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.format.InputAccessor;
import org.codehaus.jackson.format.MatchStrength;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.sym.BytesToNameCanonicalizer;

public class SmileParserBootstrapper {
   final IOContext _context;
   final InputStream _in;
   final byte[] _inputBuffer;
   private int _inputPtr;
   private int _inputEnd;
   private final boolean _bufferRecyclable;
   protected int _inputProcessed;

   public SmileParserBootstrapper(IOContext ctxt, InputStream in) {
      this._context = ctxt;
      this._in = in;
      this._inputBuffer = ctxt.allocReadIOBuffer();
      this._inputEnd = this._inputPtr = 0;
      this._inputProcessed = 0;
      this._bufferRecyclable = true;
   }

   public SmileParserBootstrapper(IOContext ctxt, byte[] inputBuffer, int inputStart, int inputLen) {
      this._context = ctxt;
      this._in = null;
      this._inputBuffer = inputBuffer;
      this._inputPtr = inputStart;
      this._inputEnd = inputStart + inputLen;
      this._inputProcessed = -inputStart;
      this._bufferRecyclable = false;
   }

   public SmileParser constructParser(int generalParserFeatures, int smileFeatures, ObjectCodec codec, BytesToNameCanonicalizer rootByteSymbols) throws IOException, JsonParseException {
      boolean intern = JsonParser.Feature.INTERN_FIELD_NAMES.enabledIn(generalParserFeatures);
      BytesToNameCanonicalizer can = rootByteSymbols.makeChild(true, intern);
      this.ensureLoaded(1);
      SmileParser p = new SmileParser(this._context, generalParserFeatures, smileFeatures, codec, can, this._in, this._inputBuffer, this._inputPtr, this._inputEnd, this._bufferRecyclable);
      boolean hadSig = false;
      if (this._inputPtr < this._inputEnd && this._inputBuffer[this._inputPtr] == 58) {
         hadSig = p.handleSignature(true, true);
      }

      if (!hadSig && (smileFeatures & SmileParser.Feature.REQUIRE_HEADER.getMask()) != 0) {
         byte firstByte = this._inputPtr < this._inputEnd ? this._inputBuffer[this._inputPtr] : 0;
         String msg;
         if (firstByte != 123 && firstByte != 91) {
            msg = "Input does not start with Smile format header (first byte = 0x" + Integer.toHexString(firstByte & 255) + ") and parser has REQUIRE_HEADER enabled: can not parse";
         } else {
            msg = "Input does not start with Smile format header (first byte = 0x" + Integer.toHexString(firstByte & 255) + ") -- rather, it starts with '" + (char)firstByte + "' (plain JSON input?) -- can not parse";
         }

         throw new JsonParseException(msg, JsonLocation.NA);
      } else {
         return p;
      }
   }

   public static MatchStrength hasSmileFormat(InputAccessor acc) throws IOException {
      if (!acc.hasMoreBytes()) {
         return MatchStrength.INCONCLUSIVE;
      } else {
         byte b1 = acc.nextByte();
         if (!acc.hasMoreBytes()) {
            return MatchStrength.INCONCLUSIVE;
         } else {
            byte b2 = acc.nextByte();
            if (b1 == 58) {
               if (b2 != 41) {
                  return MatchStrength.NO_MATCH;
               } else if (!acc.hasMoreBytes()) {
                  return MatchStrength.INCONCLUSIVE;
               } else {
                  return acc.nextByte() == 10 ? MatchStrength.FULL_MATCH : MatchStrength.NO_MATCH;
               }
            } else if (b1 == -6) {
               if (b2 == 52) {
                  return MatchStrength.SOLID_MATCH;
               } else {
                  int ch = b2 & 255;
                  return ch >= 128 && ch < 248 ? MatchStrength.SOLID_MATCH : MatchStrength.NO_MATCH;
               }
            } else if (b1 == -8) {
               if (!acc.hasMoreBytes()) {
                  return MatchStrength.INCONCLUSIVE;
               } else {
                  return !likelySmileValue(b2) && !possibleSmileValue(b2, true) ? MatchStrength.NO_MATCH : MatchStrength.SOLID_MATCH;
               }
            } else {
               return !likelySmileValue(b1) && !possibleSmileValue(b2, false) ? MatchStrength.NO_MATCH : MatchStrength.SOLID_MATCH;
            }
         }
      }
   }

   private static boolean likelySmileValue(byte b) {
      int ch = b & 255;
      if (ch >= 224) {
         switch(ch) {
         case -8:
         case -6:
         case 224:
         case 228:
         case 232:
            return true;
         default:
            return false;
         }
      } else {
         return ch >= 128 && ch <= 159;
      }
   }

   private static boolean possibleSmileValue(byte b, boolean lenient) {
      int ch = b & 255;
      if (ch >= 128) {
         return ch <= 224;
      } else {
         if (lenient) {
            if (ch >= 64) {
               return true;
            }

            if (ch > -32) {
               return ch < 44;
            }
         }

         return false;
      }
   }

   protected boolean ensureLoaded(int minimum) throws IOException {
      if (this._in == null) {
         return false;
      } else {
         int count;
         for(int gotten = this._inputEnd - this._inputPtr; gotten < minimum; gotten += count) {
            count = this._in.read(this._inputBuffer, this._inputEnd, this._inputBuffer.length - this._inputEnd);
            if (count < 1) {
               return false;
            }

            this._inputEnd += count;
         }

         return true;
      }
   }
}
