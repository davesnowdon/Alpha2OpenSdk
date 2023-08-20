package org.codehaus.jackson.smile;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.PrettyPrinter;
import org.codehaus.jackson.SerializableString;
import org.codehaus.jackson.impl.JsonGeneratorBase;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.io.SerializedString;

public class SmileGenerator extends JsonGeneratorBase {
   private static final int MIN_BUFFER_LENGTH = 770;
   protected static final byte TOKEN_BYTE_LONG_STRING_ASCII = -32;
   protected static final byte TOKEN_BYTE_LONG_STRING_UNICODE = -28;
   protected static final byte TOKEN_BYTE_INT_32 = 36;
   protected static final byte TOKEN_BYTE_INT_64 = 37;
   protected static final byte TOKEN_BYTE_BIG_INTEGER = 38;
   protected static final byte TOKEN_BYTE_FLOAT_32 = 40;
   protected static final byte TOKEN_BYTE_FLOAT_64 = 41;
   protected static final byte TOKEN_BYTE_BIG_DECIMAL = 42;
   protected static final int SURR1_FIRST = 55296;
   protected static final int SURR1_LAST = 56319;
   protected static final int SURR2_FIRST = 56320;
   protected static final int SURR2_LAST = 57343;
   protected static final long MIN_INT_AS_LONG = -2147483648L;
   protected static final long MAX_INT_AS_LONG = 2147483647L;
   protected final IOContext _ioContext;
   protected final OutputStream _out;
   protected int _smileFeatures;
   protected final SmileBufferRecycler<SmileGenerator.SharedStringNode> _smileBufferRecycler;
   protected byte[] _outputBuffer;
   protected int _outputTail = 0;
   protected final int _outputEnd;
   protected char[] _charBuffer;
   protected final int _charBufferLength;
   protected int _bytesWritten;
   protected SmileGenerator.SharedStringNode[] _seenNames;
   protected int _seenNameCount;
   protected SmileGenerator.SharedStringNode[] _seenStringValues;
   protected int _seenStringValueCount;
   protected boolean _bufferRecyclable;
   protected static final ThreadLocal<SoftReference<SmileBufferRecycler<SmileGenerator.SharedStringNode>>> _smileRecyclerRef = new ThreadLocal();

   public SmileGenerator(IOContext ctxt, int jsonFeatures, int smileFeatures, ObjectCodec codec, OutputStream out) {
      super(jsonFeatures, codec);
      this._smileFeatures = smileFeatures;
      this._ioContext = ctxt;
      this._smileBufferRecycler = _smileBufferRecycler();
      this._out = out;
      this._bufferRecyclable = true;
      this._outputBuffer = ctxt.allocWriteEncodingBuffer();
      this._outputEnd = this._outputBuffer.length;
      this._charBuffer = ctxt.allocConcatBuffer();
      this._charBufferLength = this._charBuffer.length;
      if (this._outputEnd < 770) {
         throw new IllegalStateException("Internal encoding buffer length (" + this._outputEnd + ") too short, must be at least " + 770);
      } else {
         if ((smileFeatures & SmileGenerator.Feature.CHECK_SHARED_NAMES.getMask()) == 0) {
            this._seenNames = null;
            this._seenNameCount = -1;
         } else {
            this._seenNames = (SmileGenerator.SharedStringNode[])this._smileBufferRecycler.allocSeenNamesBuffer();
            if (this._seenNames == null) {
               this._seenNames = new SmileGenerator.SharedStringNode[64];
            }

            this._seenNameCount = 0;
         }

         if ((smileFeatures & SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES.getMask()) == 0) {
            this._seenStringValues = null;
            this._seenStringValueCount = -1;
         } else {
            this._seenStringValues = (SmileGenerator.SharedStringNode[])this._smileBufferRecycler.allocSeenStringValuesBuffer();
            if (this._seenStringValues == null) {
               this._seenStringValues = new SmileGenerator.SharedStringNode[64];
            }

            this._seenStringValueCount = 0;
         }

      }
   }

   public SmileGenerator(IOContext ctxt, int jsonFeatures, int smileFeatures, ObjectCodec codec, OutputStream out, byte[] outputBuffer, int offset, boolean bufferRecyclable) {
      super(jsonFeatures, codec);
      this._smileFeatures = smileFeatures;
      this._ioContext = ctxt;
      this._smileBufferRecycler = _smileBufferRecycler();
      this._out = out;
      this._bufferRecyclable = bufferRecyclable;
      this._outputTail = offset;
      this._outputBuffer = outputBuffer;
      this._outputEnd = this._outputBuffer.length;
      this._charBuffer = ctxt.allocConcatBuffer();
      this._charBufferLength = this._charBuffer.length;
      if (this._outputEnd < 770) {
         throw new IllegalStateException("Internal encoding buffer length (" + this._outputEnd + ") too short, must be at least " + 770);
      } else {
         if ((smileFeatures & SmileGenerator.Feature.CHECK_SHARED_NAMES.getMask()) == 0) {
            this._seenNames = null;
            this._seenNameCount = -1;
         } else {
            this._seenNames = (SmileGenerator.SharedStringNode[])this._smileBufferRecycler.allocSeenNamesBuffer();
            if (this._seenNames == null) {
               this._seenNames = new SmileGenerator.SharedStringNode[64];
            }

            this._seenNameCount = 0;
         }

         if ((smileFeatures & SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES.getMask()) == 0) {
            this._seenStringValues = null;
            this._seenStringValueCount = -1;
         } else {
            this._seenStringValues = (SmileGenerator.SharedStringNode[])this._smileBufferRecycler.allocSeenStringValuesBuffer();
            if (this._seenStringValues == null) {
               this._seenStringValues = new SmileGenerator.SharedStringNode[64];
            }

            this._seenStringValueCount = 0;
         }

      }
   }

   public void writeHeader() throws IOException {
      int last = 0;
      if ((this._smileFeatures & SmileGenerator.Feature.CHECK_SHARED_NAMES.getMask()) != 0) {
         last |= 1;
      }

      if ((this._smileFeatures & SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES.getMask()) != 0) {
         last |= 2;
      }

      if ((this._smileFeatures & SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT.getMask()) == 0) {
         last |= 4;
      }

      this._writeBytes((byte)58, (byte)41, (byte)10, (byte)last);
   }

   protected static final SmileBufferRecycler<SmileGenerator.SharedStringNode> _smileBufferRecycler() {
      SoftReference<SmileBufferRecycler<SmileGenerator.SharedStringNode>> ref = (SoftReference)_smileRecyclerRef.get();
      SmileBufferRecycler<SmileGenerator.SharedStringNode> br = ref == null ? null : (SmileBufferRecycler)ref.get();
      if (br == null) {
         br = new SmileBufferRecycler();
         _smileRecyclerRef.set(new SoftReference(br));
      }

      return br;
   }

   public JsonGenerator useDefaultPrettyPrinter() {
      return this;
   }

   public JsonGenerator setPrettyPrinter(PrettyPrinter pp) {
      return this;
   }

   public Object getOutputTarget() {
      return this._out;
   }

   public final void writeFieldName(String name) throws IOException, JsonGenerationException {
      if (this._writeContext.writeFieldName(name) == 4) {
         this._reportError("Can not write a field name, expecting a value");
      }

      this._writeFieldName(name);
   }

   public final void writeFieldName(SerializedString name) throws IOException, JsonGenerationException {
      if (this._writeContext.writeFieldName(name.getValue()) == 4) {
         this._reportError("Can not write a field name, expecting a value");
      }

      this._writeFieldName((SerializableString)name);
   }

   public final void writeFieldName(SerializableString name) throws IOException, JsonGenerationException {
      if (this._writeContext.writeFieldName(name.getValue()) == 4) {
         this._reportError("Can not write a field name, expecting a value");
      }

      this._writeFieldName(name);
   }

   public final void writeStringField(String fieldName, String value) throws IOException, JsonGenerationException {
      if (this._writeContext.writeFieldName(fieldName) == 4) {
         this._reportError("Can not write a field name, expecting a value");
      }

      this._writeFieldName(fieldName);
      this.writeString(value);
   }

   public SmileGenerator enable(SmileGenerator.Feature f) {
      this._smileFeatures |= f.getMask();
      return this;
   }

   public SmileGenerator disable(SmileGenerator.Feature f) {
      this._smileFeatures &= ~f.getMask();
      return this;
   }

   public final boolean isEnabled(SmileGenerator.Feature f) {
      return (this._smileFeatures & f.getMask()) != 0;
   }

   public SmileGenerator configure(SmileGenerator.Feature f, boolean state) {
      if (state) {
         this.enable(f);
      } else {
         this.disable(f);
      }

      return this;
   }

   public void writeRaw(byte b) throws IOException, JsonGenerationException {
      this._writeByte((byte)-8);
   }

   public void writeBytes(byte[] data, int offset, int len) throws IOException {
      this._writeBytes(data, offset, len);
   }

   public final void writeStartArray() throws IOException, JsonGenerationException {
      this._verifyValueWrite("start an array");
      this._writeContext = this._writeContext.createChildArrayContext();
      if (this._cfgPrettyPrinter != null) {
         this._cfgPrettyPrinter.writeStartArray(this);
      } else {
         this._writeByte((byte)-8);
      }

   }

   public final void writeEndArray() throws IOException, JsonGenerationException {
      if (!this._writeContext.inArray()) {
         this._reportError("Current context not an ARRAY but " + this._writeContext.getTypeDesc());
      }

      if (this._cfgPrettyPrinter != null) {
         this._cfgPrettyPrinter.writeEndArray(this, this._writeContext.getEntryCount());
      } else {
         this._writeByte((byte)-7);
      }

      this._writeContext = this._writeContext.getParent();
   }

   public final void writeStartObject() throws IOException, JsonGenerationException {
      this._verifyValueWrite("start an object");
      this._writeContext = this._writeContext.createChildObjectContext();
      if (this._cfgPrettyPrinter != null) {
         this._cfgPrettyPrinter.writeStartObject(this);
      } else {
         this._writeByte((byte)-6);
      }

   }

   public final void writeEndObject() throws IOException, JsonGenerationException {
      if (!this._writeContext.inObject()) {
         this._reportError("Current context not an object but " + this._writeContext.getTypeDesc());
      }

      this._writeContext = this._writeContext.getParent();
      if (this._cfgPrettyPrinter != null) {
         this._cfgPrettyPrinter.writeEndObject(this, this._writeContext.getEntryCount());
      } else {
         this._writeByte((byte)-5);
      }

   }

   private final void _writeFieldName(String name) throws IOException, JsonGenerationException {
      int len = name.length();
      if (len == 0) {
         this._writeByte((byte)32);
      } else {
         int origOffset;
         if (this._seenNameCount >= 0) {
            origOffset = this._findSeenName(name);
            if (origOffset >= 0) {
               this._writeSharedNameReference(origOffset);
               return;
            }
         }

         if (len > 56) {
            this._writeNonShortFieldName(name, len);
         } else {
            if (this._outputTail + 196 >= this._outputEnd) {
               this._flushBuffer();
            }

            name.getChars(0, len, this._charBuffer, 0);
            origOffset = this._outputTail++;
            int byteLen = this._shortUTF8Encode(this._charBuffer, 0, len);
            byte typeToken;
            if (byteLen == len) {
               if (byteLen <= 64) {
                  typeToken = (byte)(127 + byteLen);
               } else {
                  typeToken = 52;
                  this._outputBuffer[this._outputTail++] = -4;
               }
            } else if (byteLen <= 56) {
               typeToken = (byte)(190 + byteLen);
            } else {
               typeToken = 52;
               this._outputBuffer[this._outputTail++] = -4;
            }

            this._outputBuffer[origOffset] = typeToken;
            if (this._seenNameCount >= 0) {
               this._addSeenName(name);
            }

         }
      }
   }

   private final void _writeNonShortFieldName(String name, int len) throws IOException, JsonGenerationException {
      this._writeByte((byte)52);
      if (len > this._charBufferLength) {
         this._slowUTF8Encode(name);
      } else {
         name.getChars(0, len, this._charBuffer, 0);
         int maxLen = len + len + len;
         if (maxLen <= this._outputBuffer.length) {
            if (this._outputTail + maxLen >= this._outputEnd) {
               this._flushBuffer();
            }

            this._shortUTF8Encode(this._charBuffer, 0, len);
         } else {
            this._mediumUTF8Encode(this._charBuffer, 0, len);
         }
      }

      if (this._seenNameCount >= 0) {
         this._addSeenName(name);
      }

      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = -4;
   }

   protected final void _writeFieldName(SerializableString name) throws IOException, JsonGenerationException {
      int charLen = name.charLength();
      if (charLen == 0) {
         this._writeByte((byte)32);
      } else {
         byte[] bytes = name.asUnquotedUTF8();
         int byteLen = bytes.length;
         if (byteLen != charLen) {
            this._writeFieldNameUnicode(name, bytes);
         } else {
            if (this._seenNameCount >= 0) {
               int ix = this._findSeenName(name.getValue());
               if (ix >= 0) {
                  this._writeSharedNameReference(ix);
                  return;
               }
            }

            if (byteLen <= 64) {
               if (this._outputTail + byteLen >= this._outputEnd) {
                  this._flushBuffer();
               }

               this._outputBuffer[this._outputTail++] = (byte)(127 + byteLen);
               System.arraycopy(bytes, 0, this._outputBuffer, this._outputTail, byteLen);
               this._outputTail += byteLen;
               if (this._seenNameCount >= 0) {
                  this._addSeenName(name.getValue());
               }

            } else {
               if (this._outputTail >= this._outputEnd) {
                  this._flushBuffer();
               }

               this._outputBuffer[this._outputTail++] = 52;
               if (this._outputTail + byteLen + 1 < this._outputEnd) {
                  System.arraycopy(bytes, 0, this._outputBuffer, this._outputTail, byteLen);
                  this._outputTail += byteLen;
               } else {
                  this._flushBuffer();
                  if (byteLen < 770) {
                     System.arraycopy(bytes, 0, this._outputBuffer, this._outputTail, byteLen);
                     this._outputTail += byteLen;
                  } else {
                     if (this._outputTail > 0) {
                        this._flushBuffer();
                     }

                     this._out.write(bytes, 0, byteLen);
                  }
               }

               this._outputBuffer[this._outputTail++] = -4;
               if (this._seenNameCount >= 0) {
                  this._addSeenName(name.getValue());
               }

            }
         }
      }
   }

   protected final void _writeFieldNameUnicode(SerializableString name, byte[] bytes) throws IOException, JsonGenerationException {
      int byteLen;
      if (this._seenNameCount >= 0) {
         byteLen = this._findSeenName(name.getValue());
         if (byteLen >= 0) {
            this._writeSharedNameReference(byteLen);
            return;
         }
      }

      byteLen = bytes.length;
      if (byteLen <= 56) {
         if (this._outputTail + byteLen >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = (byte)(190 + byteLen);
         System.arraycopy(bytes, 0, this._outputBuffer, this._outputTail, byteLen);
         this._outputTail += byteLen;
         if (this._seenNameCount >= 0) {
            this._addSeenName(name.getValue());
         }

      } else {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 52;
         if (this._outputTail + byteLen + 1 < this._outputEnd) {
            System.arraycopy(bytes, 0, this._outputBuffer, this._outputTail, byteLen);
            this._outputTail += byteLen;
         } else {
            this._flushBuffer();
            if (byteLen < 770) {
               System.arraycopy(bytes, 0, this._outputBuffer, this._outputTail, byteLen);
               this._outputTail += byteLen;
            } else {
               if (this._outputTail > 0) {
                  this._flushBuffer();
               }

               this._out.write(bytes, 0, byteLen);
            }
         }

         this._outputBuffer[this._outputTail++] = -4;
         if (this._seenNameCount >= 0) {
            this._addSeenName(name.getValue());
         }

      }
   }

   private final void _writeSharedNameReference(int ix) throws IOException, JsonGenerationException {
      if (ix >= this._seenNameCount) {
         throw new IllegalArgumentException("Internal error: trying to write shared name with index " + ix + "; but have only seen " + this._seenNameCount + " so far!");
      } else {
         if (ix < 64) {
            this._writeByte((byte)(64 + ix));
         } else {
            this._writeBytes((byte)(48 + (ix >> 8)), (byte)ix);
         }

      }
   }

   public void writeString(String text) throws IOException, JsonGenerationException {
      if (text == null) {
         this.writeNull();
      } else {
         this._verifyValueWrite("write String value");
         int len = text.length();
         if (len == 0) {
            this._writeByte((byte)32);
         } else if (len > 65) {
            this._writeNonSharedString(text, len);
         } else {
            int origOffset;
            if (this._seenStringValueCount >= 0) {
               origOffset = this._findSeenStringValue(text);
               if (origOffset >= 0) {
                  this._writeSharedStringValueReference(origOffset);
                  return;
               }
            }

            if (this._outputTail + 196 >= this._outputEnd) {
               this._flushBuffer();
            }

            text.getChars(0, len, this._charBuffer, 0);
            origOffset = this._outputTail++;
            int byteLen = this._shortUTF8Encode(this._charBuffer, 0, len);
            if (byteLen <= 64) {
               if (this._seenStringValueCount >= 0) {
                  this._addSeenStringValue(text);
               }

               if (byteLen == len) {
                  this._outputBuffer[origOffset] = (byte)(63 + byteLen);
               } else {
                  this._outputBuffer[origOffset] = (byte)(126 + byteLen);
               }
            } else {
               this._outputBuffer[origOffset] = (byte)(byteLen == len ? -32 : -28);
               this._outputBuffer[this._outputTail++] = -4;
            }

         }
      }
   }

   private final void _writeSharedStringValueReference(int ix) throws IOException, JsonGenerationException {
      if (ix >= this._seenStringValueCount) {
         throw new IllegalArgumentException("Internal error: trying to write shared String value with index " + ix + "; but have only seen " + this._seenStringValueCount + " so far!");
      } else {
         if (ix < 31) {
            this._writeByte((byte)(1 + ix));
         } else {
            this._writeBytes((byte)(236 + (ix >> 8)), (byte)ix);
         }

      }
   }

   private final void _writeNonSharedString(String text, int len) throws IOException, JsonGenerationException {
      if (len > this._charBufferLength) {
         this._writeByte((byte)-28);
         this._slowUTF8Encode(text);
         this._writeByte((byte)-4);
      } else {
         text.getChars(0, len, this._charBuffer, 0);
         int maxLen = len + len + len + 2;
         if (maxLen > this._outputBuffer.length) {
            this._writeByte((byte)-28);
            this._mediumUTF8Encode(this._charBuffer, 0, len);
            this._writeByte((byte)-4);
         } else {
            if (this._outputTail + maxLen >= this._outputEnd) {
               this._flushBuffer();
            }

            int origOffset = this._outputTail;
            this._writeByte((byte)-32);
            int byteLen = this._shortUTF8Encode(this._charBuffer, 0, len);
            if (byteLen > len) {
               this._outputBuffer[origOffset] = -28;
            }

            this._outputBuffer[this._outputTail++] = -4;
         }
      }
   }

   public void writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      if (len <= 65 && this._seenStringValueCount >= 0 && len > 0) {
         this.writeString(new String(text, offset, len));
      } else {
         this._verifyValueWrite("write String value");
         if (len == 0) {
            this._writeByte((byte)32);
         } else {
            int origOffset;
            int byteLen;
            if (len <= 64) {
               if (this._outputTail + 196 >= this._outputEnd) {
                  this._flushBuffer();
               }

               origOffset = this._outputTail++;
               byteLen = this._shortUTF8Encode(text, offset, offset + len);
               byte typeToken;
               if (byteLen <= 64) {
                  if (byteLen == len) {
                     typeToken = (byte)(63 + byteLen);
                  } else {
                     typeToken = (byte)(126 + byteLen);
                  }
               } else {
                  typeToken = -28;
                  this._outputBuffer[this._outputTail++] = -4;
               }

               this._outputBuffer[origOffset] = typeToken;
            } else {
               origOffset = len + len + len + 2;
               if (origOffset <= this._outputBuffer.length) {
                  if (this._outputTail + origOffset >= this._outputEnd) {
                     this._flushBuffer();
                  }

                  byteLen = this._outputTail;
                  this._writeByte((byte)-28);
                  int byteLen = this._shortUTF8Encode(text, offset, offset + len);
                  if (byteLen == len) {
                     this._outputBuffer[byteLen] = -32;
                  }

                  this._outputBuffer[this._outputTail++] = -4;
               } else {
                  this._writeByte((byte)-28);
                  this._mediumUTF8Encode(text, offset, offset + len);
                  this._writeByte((byte)-4);
               }
            }

         }
      }
   }

   public final void writeString(SerializableString sstr) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write String value");
      String str = sstr.getValue();
      int len = str.length();
      if (len == 0) {
         this._writeByte((byte)32);
      } else {
         if (len <= 65 && this._seenStringValueCount >= 0) {
            int ix = this._findSeenStringValue(str);
            if (ix >= 0) {
               this._writeSharedStringValueReference(ix);
               return;
            }
         }

         byte[] raw = sstr.asUnquotedUTF8();
         int byteLen = raw.length;
         int typeToken;
         if (byteLen <= 64) {
            if (this._outputTail + byteLen + 1 >= this._outputEnd) {
               this._flushBuffer();
            }

            typeToken = byteLen == len ? 63 + byteLen : 126 + byteLen;
            this._outputBuffer[this._outputTail++] = (byte)typeToken;
            System.arraycopy(raw, 0, this._outputBuffer, this._outputTail, byteLen);
            this._outputTail += byteLen;
            if (this._seenStringValueCount >= 0) {
               this._addSeenStringValue(sstr.getValue());
            }
         } else {
            typeToken = byteLen == len ? -32 : -28;
            this._writeByte((byte)typeToken);
            this._writeBytes(raw, 0, raw.length);
            this._writeByte((byte)-4);
         }

      }
   }

   public void writeRawUTF8String(byte[] text, int offset, int len) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write String value");
      if (len == 0) {
         this._writeByte((byte)32);
      } else if (this._seenStringValueCount >= 0) {
         throw new UnsupportedOperationException("Can not use direct UTF-8 write methods when 'Feature.CHECK_SHARED_STRING_VALUES' enabled");
      } else {
         if (len <= 65) {
            if (this._outputTail + len >= this._outputEnd) {
               this._flushBuffer();
            }

            if (len == 1) {
               this._outputBuffer[this._outputTail++] = 64;
               this._outputBuffer[this._outputTail++] = text[offset];
            } else {
               this._outputBuffer[this._outputTail++] = (byte)(126 + len);
               System.arraycopy(text, offset, this._outputBuffer, this._outputTail, len);
               this._outputTail += len;
            }
         } else {
            int maxLen = len + len + len + 2;
            if (maxLen <= this._outputBuffer.length) {
               if (this._outputTail + maxLen >= this._outputEnd) {
                  this._flushBuffer();
               }

               this._outputBuffer[this._outputTail++] = -28;
               System.arraycopy(text, offset, this._outputBuffer, this._outputTail, len);
               this._outputTail += len;
               this._outputBuffer[this._outputTail++] = -4;
            } else {
               this._writeByte((byte)-28);
               this._writeBytes(text, offset, len);
               this._writeByte((byte)-4);
            }
         }

      }
   }

   public final void writeUTF8String(byte[] text, int offset, int len) throws IOException, JsonGenerationException {
      this.writeRawUTF8String(text, offset, len);
   }

   public void writeRaw(String text) throws IOException, JsonGenerationException {
      throw this._notSupported();
   }

   public void writeRaw(String text, int offset, int len) throws IOException, JsonGenerationException {
      throw this._notSupported();
   }

   public void writeRaw(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      throw this._notSupported();
   }

   public void writeRaw(char c) throws IOException, JsonGenerationException {
      throw this._notSupported();
   }

   public void writeRawValue(String text) throws IOException, JsonGenerationException {
      throw this._notSupported();
   }

   public void writeRawValue(String text, int offset, int len) throws IOException, JsonGenerationException {
      throw this._notSupported();
   }

   public void writeRawValue(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      throw this._notSupported();
   }

   public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException, JsonGenerationException {
      if (data == null) {
         this.writeNull();
      } else {
         this._verifyValueWrite("write Binary value");
         if (this.isEnabled(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT)) {
            this._writeByte((byte)-24);
            this._write7BitBinaryWithLength(data, offset, len);
         } else {
            this._writeByte((byte)-3);
            this._writePositiveVInt(len);
            this._writeBytes(data, offset, len);
         }

      }
   }

   public void writeBoolean(boolean state) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write boolean value");
      if (state) {
         this._writeByte((byte)35);
      } else {
         this._writeByte((byte)34);
      }

   }

   public void writeNull() throws IOException, JsonGenerationException {
      this._verifyValueWrite("write null value");
      this._writeByte((byte)33);
   }

   public void writeNumber(int i) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write number");
      i = SmileUtil.zigzagEncode(i);
      if (i <= 63 && i >= 0) {
         if (i <= 31) {
            this._writeByte((byte)(192 + i));
         } else {
            this._writeBytes((byte)36, (byte)(128 + i));
         }
      } else {
         byte b0 = (byte)(128 + (i & 63));
         i >>>= 6;
         if (i <= 127) {
            this._writeBytes((byte)36, (byte)i, b0);
         } else {
            byte b1 = (byte)(i & 127);
            i >>= 7;
            if (i <= 127) {
               this._writeBytes((byte)36, (byte)i, b1, b0);
            } else {
               byte b2 = (byte)(i & 127);
               i >>= 7;
               if (i <= 127) {
                  this._writeBytes((byte)36, (byte)i, b2, b1, b0);
               } else {
                  byte b3 = (byte)(i & 127);
                  this._writeBytes((byte)36, (byte)(i >> 7), b3, b2, b1, b0);
               }
            }
         }
      }
   }

   public void writeNumber(long l) throws IOException, JsonGenerationException {
      if (l <= 2147483647L && l >= -2147483648L) {
         this.writeNumber((int)l);
      } else {
         this._verifyValueWrite("write number");
         l = SmileUtil.zigzagEncode(l);
         int i = (int)l;
         byte b0 = (byte)(128 + (i & 63));
         byte b1 = (byte)(i >> 6 & 127);
         byte b2 = (byte)(i >> 13 & 127);
         byte b3 = (byte)(i >> 20 & 127);
         l >>>= 27;
         byte b4 = (byte)((int)l & 127);
         i = (int)(l >> 7);
         if (i == 0) {
            this._writeBytes((byte)37, b4, b3, b2, b1, b0);
         } else if (i <= 127) {
            this._writeBytes((byte)37, (byte)i);
            this._writeBytes(b4, b3, b2, b1, b0);
         } else {
            byte b5 = (byte)(i & 127);
            i >>= 7;
            if (i <= 127) {
               this._writeBytes((byte)37, (byte)i);
               this._writeBytes(b5, b4, b3, b2, b1, b0);
            } else {
               byte b6 = (byte)(i & 127);
               i >>= 7;
               if (i <= 127) {
                  this._writeBytes((byte)37, (byte)i, b6);
                  this._writeBytes(b5, b4, b3, b2, b1, b0);
               } else {
                  byte b7 = (byte)(i & 127);
                  i >>= 7;
                  if (i <= 127) {
                     this._writeBytes((byte)37, (byte)i, b7, b6);
                     this._writeBytes(b5, b4, b3, b2, b1, b0);
                  } else {
                     byte b8 = (byte)(i & 127);
                     i >>= 7;
                     this._writeBytes((byte)37, (byte)i, b8, b7, b6);
                     this._writeBytes(b5, b4, b3, b2, b1, b0);
                  }
               }
            }
         }
      }
   }

   public void writeNumber(BigInteger v) throws IOException, JsonGenerationException {
      if (v == null) {
         this.writeNull();
      } else {
         this._verifyValueWrite("write number");
         this._writeByte((byte)38);
         byte[] data = v.toByteArray();
         this._write7BitBinaryWithLength(data, 0, data.length);
      }
   }

   public void writeNumber(double d) throws IOException, JsonGenerationException {
      this._ensureRoomForOutput(11);
      this._verifyValueWrite("write number");
      long l = Double.doubleToRawLongBits(d);
      this._outputBuffer[this._outputTail++] = 41;
      int hi5 = (int)(l >>> 35);
      this._outputBuffer[this._outputTail + 4] = (byte)(hi5 & 127);
      hi5 >>= 7;
      this._outputBuffer[this._outputTail + 3] = (byte)(hi5 & 127);
      hi5 >>= 7;
      this._outputBuffer[this._outputTail + 2] = (byte)(hi5 & 127);
      hi5 >>= 7;
      this._outputBuffer[this._outputTail + 1] = (byte)(hi5 & 127);
      hi5 >>= 7;
      this._outputBuffer[this._outputTail] = (byte)hi5;
      this._outputTail += 5;
      int lo4 = (int)(l >> 28);
      this._outputBuffer[this._outputTail++] = (byte)(lo4 & 127);
      lo4 = (int)l;
      this._outputBuffer[this._outputTail + 3] = (byte)(lo4 & 127);
      lo4 >>= 7;
      this._outputBuffer[this._outputTail + 2] = (byte)(lo4 & 127);
      lo4 >>= 7;
      this._outputBuffer[this._outputTail + 1] = (byte)(lo4 & 127);
      lo4 >>= 7;
      this._outputBuffer[this._outputTail] = (byte)(lo4 & 127);
      this._outputTail += 4;
   }

   public void writeNumber(float f) throws IOException, JsonGenerationException {
      this._ensureRoomForOutput(6);
      this._verifyValueWrite("write number");
      int i = Float.floatToRawIntBits(f);
      this._outputBuffer[this._outputTail++] = 40;
      this._outputBuffer[this._outputTail + 4] = (byte)(i & 127);
      i >>= 7;
      this._outputBuffer[this._outputTail + 3] = (byte)(i & 127);
      i >>= 7;
      this._outputBuffer[this._outputTail + 2] = (byte)(i & 127);
      i >>= 7;
      this._outputBuffer[this._outputTail + 1] = (byte)(i & 127);
      i >>= 7;
      this._outputBuffer[this._outputTail] = (byte)(i & 127);
      this._outputTail += 5;
   }

   public void writeNumber(BigDecimal dec) throws IOException, JsonGenerationException {
      if (dec == null) {
         this.writeNull();
      } else {
         this._verifyValueWrite("write number");
         this._writeByte((byte)42);
         int scale = dec.scale();
         this._writeSignedVInt(scale);
         BigInteger unscaled = dec.unscaledValue();
         byte[] data = unscaled.toByteArray();
         this._write7BitBinaryWithLength(data, 0, data.length);
      }
   }

   public void writeNumber(String encodedValue) throws IOException, JsonGenerationException, UnsupportedOperationException {
      throw this._notSupported();
   }

   protected final void _verifyValueWrite(String typeMsg) throws IOException, JsonGenerationException {
      int status = this._writeContext.writeValue();
      if (status == 5) {
         this._reportError("Can not " + typeMsg + ", expecting field name");
      }

   }

   public final void flush() throws IOException {
      this._flushBuffer();
      if (this.isEnabled(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)) {
         this._out.flush();
      }

   }

   public void close() throws IOException {
      boolean wasClosed = this._closed;
      super.close();
      if (this._outputBuffer != null && this.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)) {
         label36:
         while(true) {
            while(true) {
               JsonStreamContext ctxt = this.getOutputContext();
               if (ctxt.inArray()) {
                  this.writeEndArray();
               } else {
                  if (!ctxt.inObject()) {
                     break label36;
                  }

                  this.writeEndObject();
               }
            }
         }
      }

      if (!wasClosed && this.isEnabled(SmileGenerator.Feature.WRITE_END_MARKER)) {
         this._writeByte((byte)-1);
      }

      this._flushBuffer();
      if (!this._ioContext.isResourceManaged() && !this.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET)) {
         this._out.flush();
      } else {
         this._out.close();
      }

      this._releaseBuffers();
   }

   private final int _shortUTF8Encode(char[] str, int i, int end) {
      int ptr = this._outputTail;
      byte[] outBuf = this._outputBuffer;

      do {
         int c = str[i];
         if (c > 127) {
            return this._shortUTF8Encode2(str, i, end, ptr);
         }

         outBuf[ptr++] = (byte)c;
         ++i;
      } while(i < end);

      int codedLen = ptr - this._outputTail;
      this._outputTail = ptr;
      return codedLen;
   }

   private final int _shortUTF8Encode2(char[] str, int i, int end, int outputPtr) {
      byte[] outBuf = this._outputBuffer;

      while(true) {
         int c;
         while(i < end) {
            int c = str[i++];
            if (c <= 127) {
               outBuf[outputPtr++] = (byte)c;
            } else if (c < 2048) {
               outBuf[outputPtr++] = (byte)(192 | c >> 6);
               outBuf[outputPtr++] = (byte)(128 | c & 63);
            } else if (c >= '\ud800' && c <= '\udfff') {
               if (c > '\udbff') {
                  this._throwIllegalSurrogate(c);
               }

               if (i >= end) {
                  this._throwIllegalSurrogate(c);
               }

               c = this._convertSurrogate(c, str[i++]);
               if (c > 1114111) {
                  this._throwIllegalSurrogate(c);
               }

               outBuf[outputPtr++] = (byte)(240 | c >> 18);
               outBuf[outputPtr++] = (byte)(128 | c >> 12 & 63);
               outBuf[outputPtr++] = (byte)(128 | c >> 6 & 63);
               outBuf[outputPtr++] = (byte)(128 | c & 63);
            } else {
               outBuf[outputPtr++] = (byte)(224 | c >> 12);
               outBuf[outputPtr++] = (byte)(128 | c >> 6 & 63);
               outBuf[outputPtr++] = (byte)(128 | c & 63);
            }
         }

         c = outputPtr - this._outputTail;
         this._outputTail = outputPtr;
         return c;
      }
   }

   private void _slowUTF8Encode(String str) throws IOException {
      int len = str.length();
      int inputPtr = 0;
      int bufferEnd = this._outputEnd - 4;

      while(true) {
         while(true) {
            char c;
            label52:
            while(true) {
               if (inputPtr >= len) {
                  return;
               }

               if (this._outputTail >= bufferEnd) {
                  this._flushBuffer();
               }

               c = str.charAt(inputPtr++);
               if (c > 127) {
                  break;
               }

               this._outputBuffer[this._outputTail++] = (byte)c;
               int maxInCount = len - inputPtr;
               int maxOutCount = bufferEnd - this._outputTail;
               if (maxInCount > maxOutCount) {
                  maxInCount = maxOutCount;
               }

               for(maxInCount += inputPtr; inputPtr < maxInCount; this._outputBuffer[this._outputTail++] = (byte)c) {
                  c = str.charAt(inputPtr++);
                  if (c > 127) {
                     break label52;
                  }
               }
            }

            if (c < 2048) {
               this._outputBuffer[this._outputTail++] = (byte)(192 | c >> 6);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c & 63);
            } else if (c >= '\ud800' && c <= '\udfff') {
               if (c > '\udbff') {
                  this._throwIllegalSurrogate(c);
               }

               if (inputPtr >= len) {
                  this._throwIllegalSurrogate(c);
               }

               int c = this._convertSurrogate(c, str.charAt(inputPtr++));
               if (c > 1114111) {
                  this._throwIllegalSurrogate(c);
               }

               this._outputBuffer[this._outputTail++] = (byte)(240 | c >> 18);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c >> 12 & 63);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c >> 6 & 63);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c & 63);
            } else {
               this._outputBuffer[this._outputTail++] = (byte)(224 | c >> 12);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c >> 6 & 63);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c & 63);
            }
         }
      }
   }

   private void _mediumUTF8Encode(char[] str, int inputPtr, int inputEnd) throws IOException {
      int bufferEnd = this._outputEnd - 4;

      while(true) {
         while(true) {
            char c;
            label52:
            while(true) {
               if (inputPtr >= inputEnd) {
                  return;
               }

               if (this._outputTail >= bufferEnd) {
                  this._flushBuffer();
               }

               c = str[inputPtr++];
               if (c > 127) {
                  break;
               }

               this._outputBuffer[this._outputTail++] = (byte)c;
               int maxInCount = inputEnd - inputPtr;
               int maxOutCount = bufferEnd - this._outputTail;
               if (maxInCount > maxOutCount) {
                  maxInCount = maxOutCount;
               }

               for(maxInCount += inputPtr; inputPtr < maxInCount; this._outputBuffer[this._outputTail++] = (byte)c) {
                  c = str[inputPtr++];
                  if (c > 127) {
                     break label52;
                  }
               }
            }

            if (c < 2048) {
               this._outputBuffer[this._outputTail++] = (byte)(192 | c >> 6);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c & 63);
            } else if (c >= '\ud800' && c <= '\udfff') {
               if (c > '\udbff') {
                  this._throwIllegalSurrogate(c);
               }

               if (inputPtr >= inputEnd) {
                  this._throwIllegalSurrogate(c);
               }

               int c = this._convertSurrogate(c, str[inputPtr++]);
               if (c > 1114111) {
                  this._throwIllegalSurrogate(c);
               }

               this._outputBuffer[this._outputTail++] = (byte)(240 | c >> 18);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c >> 12 & 63);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c >> 6 & 63);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c & 63);
            } else {
               this._outputBuffer[this._outputTail++] = (byte)(224 | c >> 12);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c >> 6 & 63);
               this._outputBuffer[this._outputTail++] = (byte)(128 | c & 63);
            }
         }
      }
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

   private final void _ensureRoomForOutput(int needed) throws IOException {
      if (this._outputTail + needed >= this._outputEnd) {
         this._flushBuffer();
      }

   }

   private final void _writeByte(byte b) throws IOException {
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = b;
   }

   private final void _writeBytes(byte b1, byte b2) throws IOException {
      if (this._outputTail + 1 >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = b1;
      this._outputBuffer[this._outputTail++] = b2;
   }

   private final void _writeBytes(byte b1, byte b2, byte b3) throws IOException {
      if (this._outputTail + 2 >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = b1;
      this._outputBuffer[this._outputTail++] = b2;
      this._outputBuffer[this._outputTail++] = b3;
   }

   private final void _writeBytes(byte b1, byte b2, byte b3, byte b4) throws IOException {
      if (this._outputTail + 3 >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = b1;
      this._outputBuffer[this._outputTail++] = b2;
      this._outputBuffer[this._outputTail++] = b3;
      this._outputBuffer[this._outputTail++] = b4;
   }

   private final void _writeBytes(byte b1, byte b2, byte b3, byte b4, byte b5) throws IOException {
      if (this._outputTail + 4 >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = b1;
      this._outputBuffer[this._outputTail++] = b2;
      this._outputBuffer[this._outputTail++] = b3;
      this._outputBuffer[this._outputTail++] = b4;
      this._outputBuffer[this._outputTail++] = b5;
   }

   private final void _writeBytes(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6) throws IOException {
      if (this._outputTail + 5 >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = b1;
      this._outputBuffer[this._outputTail++] = b2;
      this._outputBuffer[this._outputTail++] = b3;
      this._outputBuffer[this._outputTail++] = b4;
      this._outputBuffer[this._outputTail++] = b5;
      this._outputBuffer[this._outputTail++] = b6;
   }

   private final void _writeBytes(byte[] data, int offset, int len) throws IOException {
      if (len != 0) {
         if (this._outputTail + len >= this._outputEnd) {
            this._writeBytesLong(data, offset, len);
         } else {
            System.arraycopy(data, offset, this._outputBuffer, this._outputTail, len);
            this._outputTail += len;
         }
      }
   }

   private final void _writeBytesLong(byte[] data, int offset, int len) throws IOException {
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      while(true) {
         int currLen = Math.min(len, this._outputEnd - this._outputTail);
         System.arraycopy(data, offset, this._outputBuffer, this._outputTail, currLen);
         this._outputTail += currLen;
         if ((len -= currLen) == 0) {
            return;
         }

         offset += currLen;
         this._flushBuffer();
      }
   }

   private void _writePositiveVInt(int i) throws IOException {
      this._ensureRoomForOutput(5);
      byte b0 = (byte)(128 + (i & 63));
      i >>= 6;
      if (i <= 127) {
         if (i > 0) {
            this._outputBuffer[this._outputTail++] = (byte)i;
         }

         this._outputBuffer[this._outputTail++] = b0;
      } else {
         byte b1 = (byte)(i & 127);
         i >>= 7;
         if (i <= 127) {
            this._outputBuffer[this._outputTail++] = (byte)i;
            this._outputBuffer[this._outputTail++] = b1;
            this._outputBuffer[this._outputTail++] = b0;
         } else {
            byte b2 = (byte)(i & 127);
            i >>= 7;
            if (i <= 127) {
               this._outputBuffer[this._outputTail++] = (byte)i;
               this._outputBuffer[this._outputTail++] = b2;
               this._outputBuffer[this._outputTail++] = b1;
               this._outputBuffer[this._outputTail++] = b0;
            } else {
               byte b3 = (byte)(i & 127);
               this._outputBuffer[this._outputTail++] = (byte)(i >> 7);
               this._outputBuffer[this._outputTail++] = b3;
               this._outputBuffer[this._outputTail++] = b2;
               this._outputBuffer[this._outputTail++] = b1;
               this._outputBuffer[this._outputTail++] = b0;
            }
         }

      }
   }

   private void _writeSignedVInt(int input) throws IOException {
      this._writePositiveVInt(SmileUtil.zigzagEncode(input));
   }

   protected void _write7BitBinaryWithLength(byte[] data, int offset, int len) throws IOException {
      this._writePositiveVInt(len);

      byte i;
      int i;
      while(len >= 7) {
         if (this._outputTail + 8 >= this._outputEnd) {
            this._flushBuffer();
         }

         i = data[offset++];
         this._outputBuffer[this._outputTail++] = (byte)(i >> 1 & 127);
         i = i << 8 | data[offset++] & 255;
         this._outputBuffer[this._outputTail++] = (byte)(i >> 2 & 127);
         i = i << 8 | data[offset++] & 255;
         this._outputBuffer[this._outputTail++] = (byte)(i >> 3 & 127);
         i = i << 8 | data[offset++] & 255;
         this._outputBuffer[this._outputTail++] = (byte)(i >> 4 & 127);
         i = i << 8 | data[offset++] & 255;
         this._outputBuffer[this._outputTail++] = (byte)(i >> 5 & 127);
         i = i << 8 | data[offset++] & 255;
         this._outputBuffer[this._outputTail++] = (byte)(i >> 6 & 127);
         i = i << 8 | data[offset++] & 255;
         this._outputBuffer[this._outputTail++] = (byte)(i >> 7 & 127);
         this._outputBuffer[this._outputTail++] = (byte)(i & 127);
         len -= 7;
      }

      if (len > 0) {
         if (this._outputTail + 7 >= this._outputEnd) {
            this._flushBuffer();
         }

         i = data[offset++];
         this._outputBuffer[this._outputTail++] = (byte)(i >> 1 & 127);
         if (len > 1) {
            i = (i & 1) << 8 | data[offset++] & 255;
            this._outputBuffer[this._outputTail++] = (byte)(i >> 2 & 127);
            if (len > 2) {
               i = (i & 3) << 8 | data[offset++] & 255;
               this._outputBuffer[this._outputTail++] = (byte)(i >> 3 & 127);
               if (len > 3) {
                  i = (i & 7) << 8 | data[offset++] & 255;
                  this._outputBuffer[this._outputTail++] = (byte)(i >> 4 & 127);
                  if (len > 4) {
                     i = (i & 15) << 8 | data[offset++] & 255;
                     this._outputBuffer[this._outputTail++] = (byte)(i >> 5 & 127);
                     if (len > 5) {
                        i = (i & 31) << 8 | data[offset++] & 255;
                        this._outputBuffer[this._outputTail++] = (byte)(i >> 6 & 127);
                        this._outputBuffer[this._outputTail++] = (byte)(i & 63);
                     } else {
                        this._outputBuffer[this._outputTail++] = (byte)(i & 31);
                     }
                  } else {
                     this._outputBuffer[this._outputTail++] = (byte)(i & 15);
                  }
               } else {
                  this._outputBuffer[this._outputTail++] = (byte)(i & 7);
               }
            } else {
               this._outputBuffer[this._outputTail++] = (byte)(i & 3);
            }
         } else {
            this._outputBuffer[this._outputTail++] = (byte)(i & 1);
         }
      }

   }

   protected void _releaseBuffers() {
      byte[] buf = this._outputBuffer;
      if (buf != null && this._bufferRecyclable) {
         this._outputBuffer = null;
         this._ioContext.releaseWriteEncodingBuffer(buf);
      }

      char[] cbuf = this._charBuffer;
      if (cbuf != null) {
         this._charBuffer = null;
         this._ioContext.releaseConcatBuffer(cbuf);
      }

      SmileGenerator.SharedStringNode[] valueBuf = this._seenNames;
      if (valueBuf != null && valueBuf.length == 64) {
         this._seenNames = null;
         this._smileBufferRecycler.releaseSeenNamesBuffer(valueBuf);
      }

      valueBuf = this._seenStringValues;
      if (valueBuf != null && valueBuf.length == 64) {
         this._seenStringValues = null;
         this._smileBufferRecycler.releaseSeenStringValuesBuffer(valueBuf);
      }

   }

   protected final void _flushBuffer() throws IOException {
      if (this._outputTail > 0) {
         this._bytesWritten += this._outputTail;
         this._out.write(this._outputBuffer, 0, this._outputTail);
         this._outputTail = 0;
      }

   }

   private final int _findSeenName(String name) {
      int hash = name.hashCode();
      SmileGenerator.SharedStringNode head = this._seenNames[hash & this._seenNames.length - 1];
      if (head == null) {
         return -1;
      } else {
         SmileGenerator.SharedStringNode node = head;
         if (head.value == name) {
            return head.index;
         } else {
            while((node = node.next) != null) {
               if (node.value == name) {
                  return node.index;
               }
            }

            node = head;

            do {
               String value = node.value;
               if (value.hashCode() == hash && value.equals(name)) {
                  return node.index;
               }

               node = node.next;
            } while(node != null);

            return -1;
         }
      }
   }

   private final void _addSeenName(String name) {
      if (this._seenNameCount == this._seenNames.length) {
         if (this._seenNameCount == 1024) {
            Arrays.fill(this._seenNames, (Object)null);
            this._seenNameCount = 0;
         } else {
            SmileGenerator.SharedStringNode[] old = this._seenNames;
            this._seenNames = new SmileGenerator.SharedStringNode[1024];
            int mask = true;
            SmileGenerator.SharedStringNode[] arr$ = old;
            int len$ = old.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               for(SmileGenerator.SharedStringNode node = arr$[i$]; node != null; node = node.next) {
                  int ix = node.value.hashCode() & 1023;
                  node.next = this._seenNames[ix];
                  this._seenNames[ix] = node;
               }
            }
         }
      }

      int ix = name.hashCode() & this._seenNames.length - 1;
      this._seenNames[ix] = new SmileGenerator.SharedStringNode(name, this._seenNameCount, this._seenNames[ix]);
      ++this._seenNameCount;
   }

   private final int _findSeenStringValue(String text) {
      int hash = text.hashCode();
      SmileGenerator.SharedStringNode head = this._seenStringValues[hash & this._seenStringValues.length - 1];
      if (head != null) {
         SmileGenerator.SharedStringNode node = head;

         do {
            if (node.value == text) {
               return node.index;
            }

            node = node.next;
         } while(node != null);

         node = head;

         do {
            String value = node.value;
            if (value.hashCode() == hash && value.equals(text)) {
               return node.index;
            }

            node = node.next;
         } while(node != null);
      }

      return -1;
   }

   private final void _addSeenStringValue(String text) {
      if (this._seenStringValueCount == this._seenStringValues.length) {
         if (this._seenStringValueCount == 1024) {
            Arrays.fill(this._seenStringValues, (Object)null);
            this._seenStringValueCount = 0;
         } else {
            SmileGenerator.SharedStringNode[] old = this._seenStringValues;
            this._seenStringValues = new SmileGenerator.SharedStringNode[1024];
            int mask = true;
            SmileGenerator.SharedStringNode[] arr$ = old;
            int len$ = old.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               for(SmileGenerator.SharedStringNode node = arr$[i$]; node != null; node = node.next) {
                  int ix = node.value.hashCode() & 1023;
                  node.next = this._seenStringValues[ix];
                  this._seenStringValues[ix] = node;
               }
            }
         }
      }

      int ix = text.hashCode() & this._seenStringValues.length - 1;
      this._seenStringValues[ix] = new SmileGenerator.SharedStringNode(text, this._seenStringValueCount, this._seenStringValues[ix]);
      ++this._seenStringValueCount;
   }

   protected long outputOffset() {
      return (long)(this._bytesWritten + this._outputTail);
   }

   protected UnsupportedOperationException _notSupported() {
      return new UnsupportedOperationException();
   }

   protected static final class SharedStringNode {
      public final String value;
      public final int index;
      public SmileGenerator.SharedStringNode next;

      public SharedStringNode(String value, int index, SmileGenerator.SharedStringNode next) {
         this.value = value;
         this.index = index;
         this.next = next;
      }
   }

   public static enum Feature {
      WRITE_HEADER(true),
      WRITE_END_MARKER(false),
      ENCODE_BINARY_AS_7BIT(true),
      CHECK_SHARED_NAMES(true),
      CHECK_SHARED_STRING_VALUES(false);

      protected final boolean _defaultState;
      protected final int _mask;

      public static int collectDefaults() {
         int flags = 0;
         SmileGenerator.Feature[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SmileGenerator.Feature f = arr$[i$];
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
