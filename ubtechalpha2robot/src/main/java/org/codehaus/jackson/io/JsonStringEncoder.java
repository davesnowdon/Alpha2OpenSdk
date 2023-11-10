package org.codehaus.jackson.io;

import java.lang.ref.SoftReference;
import org.codehaus.jackson.util.BufferRecycler;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.util.CharTypes;
import org.codehaus.jackson.util.TextBuffer;

public final class JsonStringEncoder {
   private static final char[] HEX_CHARS = CharTypes.copyHexChars();
   private static final byte[] HEX_BYTES = CharTypes.copyHexBytes();
   private static final int SURR1_FIRST = 55296;
   private static final int SURR1_LAST = 56319;
   private static final int SURR2_FIRST = 56320;
   private static final int SURR2_LAST = 57343;
   private static final int INT_BACKSLASH = 92;
   private static final int INT_U = 117;
   private static final int INT_0 = 48;
   protected static final ThreadLocal<SoftReference<JsonStringEncoder>> _threadEncoder = new ThreadLocal();
   protected TextBuffer _textBuffer;
   protected ByteArrayBuilder _byteBuilder;
   protected final char[] _quoteBuffer = new char[6];

   public JsonStringEncoder() {
      this._quoteBuffer[0] = '\\';
      this._quoteBuffer[2] = '0';
      this._quoteBuffer[3] = '0';
   }

   public static JsonStringEncoder getInstance() {
      SoftReference<JsonStringEncoder> ref = (SoftReference)_threadEncoder.get();
      JsonStringEncoder enc = ref == null ? null : (JsonStringEncoder)ref.get();
      if (enc == null) {
         enc = new JsonStringEncoder();
         _threadEncoder.set(new SoftReference(enc));
      }

      return enc;
   }

   public char[] quoteAsString(String input) {
      TextBuffer textBuffer = this._textBuffer;
      if (textBuffer == null) {
         this._textBuffer = textBuffer = new TextBuffer((BufferRecycler)null);
      }

      char[] outputBuffer = textBuffer.emptyAndGetCurrentSegment();
      int[] escCodes = CharTypes.get7BitOutputEscapes();
      int escCodeCount = escCodes.length;
      int inPtr = 0;
      int inputLen = input.length();
      int outPtr = 0;

      label40:
      while(inPtr < inputLen) {
         while(true) {
            char c = input.charAt(inPtr);
            if (c < escCodeCount && escCodes[c] != 0) {
               int escCode = escCodes[input.charAt(inPtr++)];
               int length = this._appendSingleEscape(escCode, this._quoteBuffer);
               if (outPtr + length > outputBuffer.length) {
                  int first = outputBuffer.length - outPtr;
                  if (first > 0) {
                     System.arraycopy(this._quoteBuffer, 0, outputBuffer, outPtr, first);
                  }

                  outputBuffer = textBuffer.finishCurrentSegment();
                  int second = length - first;
                  System.arraycopy(this._quoteBuffer, first, outputBuffer, outPtr, second);
                  outPtr += second;
               } else {
                  System.arraycopy(this._quoteBuffer, 0, outputBuffer, outPtr, length);
                  outPtr += length;
               }
               break;
            }

            if (outPtr >= outputBuffer.length) {
               outputBuffer = textBuffer.finishCurrentSegment();
               outPtr = 0;
            }

            outputBuffer[outPtr++] = c;
            ++inPtr;
            if (inPtr >= inputLen) {
               break label40;
            }
         }
      }

      textBuffer.setCurrentLength(outPtr);
      return textBuffer.contentsAsArray();
   }

   public byte[] quoteAsUTF8(String text) {
      ByteArrayBuilder byteBuilder = this._byteBuilder;
      if (byteBuilder == null) {
         this._byteBuilder = byteBuilder = new ByteArrayBuilder((BufferRecycler)null);
      }

      int inputPtr = 0;
      int inputEnd = text.length();
      int outputPtr = 0;
      byte[] outputBuffer = byteBuilder.resetAndGetFirstSegment();

      label83:
      while(inputPtr < inputEnd) {
         int[] escCodes = CharTypes.get7BitOutputEscapes();

         do {
            int ch = text.charAt(inputPtr);
            if (ch > 127 || escCodes[ch] != 0) {
               if (outputPtr >= outputBuffer.length) {
                  outputBuffer = byteBuilder.finishCurrentSegment();
                  outputPtr = 0;
               }

               ch = text.charAt(inputPtr++);
               if (ch <= 127) {
                  int escape = escCodes[ch];
                  outputPtr = this._appendByteEscape(ch, escape, byteBuilder, outputPtr);
                  outputBuffer = byteBuilder.getCurrentSegment();
               } else {
                  int ch;
                  if (ch <= 2047) {
                     outputBuffer[outputPtr++] = (byte)(192 | ch >> 6);
                     ch = 128 | ch & 63;
                  } else if (ch >= '\ud800' && ch <= '\udfff') {
                     if (ch > '\udbff') {
                        this._throwIllegalSurrogate(ch);
                     }

                     if (inputPtr >= inputEnd) {
                        this._throwIllegalSurrogate(ch);
                     }

                     ch = this._convertSurrogate(ch, text.charAt(inputPtr++));
                     if (ch > 1114111) {
                        this._throwIllegalSurrogate(ch);
                     }

                     outputBuffer[outputPtr++] = (byte)(240 | ch >> 18);
                     if (outputPtr >= outputBuffer.length) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputPtr = 0;
                     }

                     outputBuffer[outputPtr++] = (byte)(128 | ch >> 12 & 63);
                     if (outputPtr >= outputBuffer.length) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputPtr = 0;
                     }

                     outputBuffer[outputPtr++] = (byte)(128 | ch >> 6 & 63);
                     ch = 128 | ch & 63;
                  } else {
                     outputBuffer[outputPtr++] = (byte)(224 | ch >> 12);
                     if (outputPtr >= outputBuffer.length) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputPtr = 0;
                     }

                     outputBuffer[outputPtr++] = (byte)(128 | ch >> 6 & 63);
                     ch = 128 | ch & 63;
                  }

                  if (outputPtr >= outputBuffer.length) {
                     outputBuffer = byteBuilder.finishCurrentSegment();
                     outputPtr = 0;
                  }

                  outputBuffer[outputPtr++] = (byte)ch;
               }
               continue label83;
            }

            if (outputPtr >= outputBuffer.length) {
               outputBuffer = byteBuilder.finishCurrentSegment();
               outputPtr = 0;
            }

            outputBuffer[outputPtr++] = (byte)ch;
            ++inputPtr;
         } while(inputPtr < inputEnd);

         return this._byteBuilder.completeAndCoalesce(outputPtr);
      }

      return this._byteBuilder.completeAndCoalesce(outputPtr);
   }

   public byte[] encodeAsUTF8(String text) {
      ByteArrayBuilder byteBuilder = this._byteBuilder;
      if (byteBuilder == null) {
         this._byteBuilder = byteBuilder = new ByteArrayBuilder((BufferRecycler)null);
      }

      int inputPtr = 0;
      int inputEnd = text.length();
      int outputPtr = 0;
      byte[] outputBuffer = byteBuilder.resetAndGetFirstSegment();

      int c;
      for(int outputEnd = outputBuffer.length; inputPtr < inputEnd; outputBuffer[outputPtr++] = (byte)(128 | c & 63)) {
         for(c = text.charAt(inputPtr++); c <= 127; c = text.charAt(inputPtr++)) {
            if (outputPtr >= outputEnd) {
               outputBuffer = byteBuilder.finishCurrentSegment();
               outputEnd = outputBuffer.length;
               outputPtr = 0;
            }

            outputBuffer[outputPtr++] = (byte)c;
            if (inputPtr >= inputEnd) {
               return this._byteBuilder.completeAndCoalesce(outputPtr);
            }
         }

         if (outputPtr >= outputEnd) {
            outputBuffer = byteBuilder.finishCurrentSegment();
            outputEnd = outputBuffer.length;
            outputPtr = 0;
         }

         if (c < 2048) {
            outputBuffer[outputPtr++] = (byte)(192 | c >> 6);
         } else if (c >= 55296 && c <= 57343) {
            if (c > 56319) {
               this._throwIllegalSurrogate(c);
            }

            if (inputPtr >= inputEnd) {
               this._throwIllegalSurrogate(c);
            }

            c = this._convertSurrogate(c, text.charAt(inputPtr++));
            if (c > 1114111) {
               this._throwIllegalSurrogate(c);
            }

            outputBuffer[outputPtr++] = (byte)(240 | c >> 18);
            if (outputPtr >= outputEnd) {
               outputBuffer = byteBuilder.finishCurrentSegment();
               outputEnd = outputBuffer.length;
               outputPtr = 0;
            }

            outputBuffer[outputPtr++] = (byte)(128 | c >> 12 & 63);
            if (outputPtr >= outputEnd) {
               outputBuffer = byteBuilder.finishCurrentSegment();
               outputEnd = outputBuffer.length;
               outputPtr = 0;
            }

            outputBuffer[outputPtr++] = (byte)(128 | c >> 6 & 63);
         } else {
            outputBuffer[outputPtr++] = (byte)(224 | c >> 12);
            if (outputPtr >= outputEnd) {
               outputBuffer = byteBuilder.finishCurrentSegment();
               outputEnd = outputBuffer.length;
               outputPtr = 0;
            }

            outputBuffer[outputPtr++] = (byte)(128 | c >> 6 & 63);
         }

         if (outputPtr >= outputEnd) {
            outputBuffer = byteBuilder.finishCurrentSegment();
            outputEnd = outputBuffer.length;
            outputPtr = 0;
         }
      }

      return this._byteBuilder.completeAndCoalesce(outputPtr);
   }

   private int _appendSingleEscape(int escCode, char[] quoteBuffer) {
      if (escCode < 0) {
         int value = -(escCode + 1);
         quoteBuffer[1] = 'u';
         quoteBuffer[4] = HEX_CHARS[value >> 4];
         quoteBuffer[5] = HEX_CHARS[value & 15];
         return 6;
      } else {
         quoteBuffer[1] = (char)escCode;
         return 2;
      }
   }

   private int _appendByteEscape(int ch, int escCode, ByteArrayBuilder byteBuilder, int ptr) {
      byteBuilder.setCurrentSegmentLength(ptr);
      byteBuilder.append(92);
      if (escCode < 0) {
         byteBuilder.append(117);
         if (ch > 255) {
            int hi = ch >> 8;
            byteBuilder.append(HEX_BYTES[hi >> 4]);
            byteBuilder.append(HEX_BYTES[hi & 15]);
            ch &= 255;
         } else {
            byteBuilder.append(48);
            byteBuilder.append(48);
         }

         byteBuilder.append(HEX_BYTES[ch >> 4]);
         byteBuilder.append(HEX_BYTES[ch & 15]);
      } else {
         byteBuilder.append((byte)escCode);
      }

      return byteBuilder.getCurrentSegmentLength();
   }

   private int _convertSurrogate(int firstPart, int secondPart) {
      if (secondPart >= 56320 && secondPart <= 57343) {
         return 65536 + (firstPart - '\ud800' << 10) + (secondPart - '\udc00');
      } else {
         throw new IllegalArgumentException("Broken surrogate pair: first char 0x" + Integer.toHexString(firstPart) + ", second 0x" + Integer.toHexString(secondPart) + "; illegal combination");
      }
   }

   private void _throwIllegalSurrogate(int code) {
      if (code > 1114111) {
         throw new IllegalArgumentException("Illegal character point (0x" + Integer.toHexString(code) + ") to output; max is 0x10FFFF as per RFC 4627");
      } else if (code >= 55296) {
         if (code <= 56319) {
            throw new IllegalArgumentException("Unmatched first part of surrogate pair (0x" + Integer.toHexString(code) + ")");
         } else {
            throw new IllegalArgumentException("Unmatched second part of surrogate pair (0x" + Integer.toHexString(code) + ")");
         }
      } else {
         throw new IllegalArgumentException("Illegal character point (0x" + Integer.toHexString(code) + ") to output");
      }
   }
}
