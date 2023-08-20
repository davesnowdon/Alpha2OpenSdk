package org.codehaus.jackson.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.SerializableString;
import org.codehaus.jackson.io.CharacterEscapes;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.io.NumberOutput;
import org.codehaus.jackson.io.SerializedString;
import org.codehaus.jackson.util.CharTypes;

public class Utf8Generator extends JsonGeneratorBase {
   private static final byte BYTE_u = 117;
   private static final byte BYTE_0 = 48;
   private static final byte BYTE_LBRACKET = 91;
   private static final byte BYTE_RBRACKET = 93;
   private static final byte BYTE_LCURLY = 123;
   private static final byte BYTE_RCURLY = 125;
   private static final byte BYTE_BACKSLASH = 92;
   private static final byte BYTE_SPACE = 32;
   private static final byte BYTE_COMMA = 44;
   private static final byte BYTE_COLON = 58;
   private static final byte BYTE_QUOTE = 34;
   protected static final int SURR1_FIRST = 55296;
   protected static final int SURR1_LAST = 56319;
   protected static final int SURR2_FIRST = 56320;
   protected static final int SURR2_LAST = 57343;
   private static final int MAX_BYTES_TO_BUFFER = 512;
   static final byte[] HEX_CHARS = CharTypes.copyHexBytes();
   private static final byte[] NULL_BYTES = new byte[]{110, 117, 108, 108};
   private static final byte[] TRUE_BYTES = new byte[]{116, 114, 117, 101};
   private static final byte[] FALSE_BYTES = new byte[]{102, 97, 108, 115, 101};
   protected static final int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();
   protected final IOContext _ioContext;
   protected final OutputStream _outputStream;
   protected int[] _outputEscapes;
   protected int _maximumNonEscapedChar;
   protected CharacterEscapes _characterEscapes;
   protected byte[] _outputBuffer;
   protected int _outputTail;
   protected final int _outputEnd;
   protected final int _outputMaxContiguous;
   protected char[] _charBuffer;
   protected final int _charBufferLength;
   protected byte[] _entityBuffer;
   protected boolean _bufferRecyclable;

   public Utf8Generator(IOContext ctxt, int features, ObjectCodec codec, OutputStream out) {
      super(features, codec);
      this._outputEscapes = sOutputEscapes;
      this._outputTail = 0;
      this._ioContext = ctxt;
      this._outputStream = out;
      this._bufferRecyclable = true;
      this._outputBuffer = ctxt.allocWriteEncodingBuffer();
      this._outputEnd = this._outputBuffer.length;
      this._outputMaxContiguous = this._outputEnd >> 3;
      this._charBuffer = ctxt.allocConcatBuffer();
      this._charBufferLength = this._charBuffer.length;
      if (this.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII)) {
         this.setHighestNonEscapedChar(127);
      }

   }

   public Utf8Generator(IOContext ctxt, int features, ObjectCodec codec, OutputStream out, byte[] outputBuffer, int outputOffset, boolean bufferRecyclable) {
      super(features, codec);
      this._outputEscapes = sOutputEscapes;
      this._outputTail = 0;
      this._ioContext = ctxt;
      this._outputStream = out;
      this._bufferRecyclable = bufferRecyclable;
      this._outputTail = outputOffset;
      this._outputBuffer = outputBuffer;
      this._outputEnd = this._outputBuffer.length;
      this._outputMaxContiguous = this._outputEnd >> 3;
      this._charBuffer = ctxt.allocConcatBuffer();
      this._charBufferLength = this._charBuffer.length;
      if (this.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII)) {
         this.setHighestNonEscapedChar(127);
      }

   }

   public JsonGenerator setHighestNonEscapedChar(int charCode) {
      this._maximumNonEscapedChar = charCode < 0 ? 0 : charCode;
      return this;
   }

   public int getHighestEscapedChar() {
      return this._maximumNonEscapedChar;
   }

   public JsonGenerator setCharacterEscapes(CharacterEscapes esc) {
      this._characterEscapes = esc;
      if (esc == null) {
         this._outputEscapes = sOutputEscapes;
      } else {
         this._outputEscapes = esc.getEscapeCodesForAscii();
      }

      return this;
   }

   public CharacterEscapes getCharacterEscapes() {
      return this._characterEscapes;
   }

   public Object getOutputTarget() {
      return this._outputStream;
   }

   public final void writeStringField(String fieldName, String value) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeString(value);
   }

   public final void writeFieldName(String name) throws IOException, JsonGenerationException {
      int status = this._writeContext.writeFieldName(name);
      if (status == 4) {
         this._reportError("Can not write a field name, expecting a value");
      }

      if (this._cfgPrettyPrinter != null) {
         this._writePPFieldName(name, status == 1);
      } else {
         if (status == 1) {
            if (this._outputTail >= this._outputEnd) {
               this._flushBuffer();
            }

            this._outputBuffer[this._outputTail++] = 44;
         }

         this._writeFieldName(name);
      }
   }

   public final void writeFieldName(SerializedString name) throws IOException, JsonGenerationException {
      int status = this._writeContext.writeFieldName(name.getValue());
      if (status == 4) {
         this._reportError("Can not write a field name, expecting a value");
      }

      if (this._cfgPrettyPrinter != null) {
         this._writePPFieldName((SerializableString)name, status == 1);
      } else {
         if (status == 1) {
            if (this._outputTail >= this._outputEnd) {
               this._flushBuffer();
            }

            this._outputBuffer[this._outputTail++] = 44;
         }

         this._writeFieldName((SerializableString)name);
      }
   }

   public final void writeFieldName(SerializableString name) throws IOException, JsonGenerationException {
      int status = this._writeContext.writeFieldName(name.getValue());
      if (status == 4) {
         this._reportError("Can not write a field name, expecting a value");
      }

      if (this._cfgPrettyPrinter != null) {
         this._writePPFieldName(name, status == 1);
      } else {
         if (status == 1) {
            if (this._outputTail >= this._outputEnd) {
               this._flushBuffer();
            }

            this._outputBuffer[this._outputTail++] = 44;
         }

         this._writeFieldName(name);
      }
   }

   public final void writeStartArray() throws IOException, JsonGenerationException {
      this._verifyValueWrite("start an array");
      this._writeContext = this._writeContext.createChildArrayContext();
      if (this._cfgPrettyPrinter != null) {
         this._cfgPrettyPrinter.writeStartArray(this);
      } else {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 91;
      }

   }

   public final void writeEndArray() throws IOException, JsonGenerationException {
      if (!this._writeContext.inArray()) {
         this._reportError("Current context not an ARRAY but " + this._writeContext.getTypeDesc());
      }

      if (this._cfgPrettyPrinter != null) {
         this._cfgPrettyPrinter.writeEndArray(this, this._writeContext.getEntryCount());
      } else {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 93;
      }

      this._writeContext = this._writeContext.getParent();
   }

   public final void writeStartObject() throws IOException, JsonGenerationException {
      this._verifyValueWrite("start an object");
      this._writeContext = this._writeContext.createChildObjectContext();
      if (this._cfgPrettyPrinter != null) {
         this._cfgPrettyPrinter.writeStartObject(this);
      } else {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 123;
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
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 125;
      }

   }

   protected final void _writeFieldName(String name) throws IOException, JsonGenerationException {
      if (!this.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES)) {
         this._writeStringSegments(name);
      } else {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 34;
         int len = name.length();
         if (len <= this._charBufferLength) {
            name.getChars(0, len, this._charBuffer, 0);
            if (len <= this._outputMaxContiguous) {
               if (this._outputTail + len > this._outputEnd) {
                  this._flushBuffer();
               }

               this._writeStringSegment(this._charBuffer, 0, len);
            } else {
               this._writeStringSegments(this._charBuffer, 0, len);
            }
         } else {
            this._writeStringSegments(name);
         }

         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 34;
      }
   }

   protected final void _writeFieldName(SerializableString name) throws IOException, JsonGenerationException {
      byte[] raw = name.asQuotedUTF8();
      if (!this.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES)) {
         this._writeBytes(raw);
      } else {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 34;
         int len = raw.length;
         if (this._outputTail + len + 1 < this._outputEnd) {
            System.arraycopy(raw, 0, this._outputBuffer, this._outputTail, len);
            this._outputTail += len;
            this._outputBuffer[this._outputTail++] = 34;
         } else {
            this._writeBytes(raw);
            if (this._outputTail >= this._outputEnd) {
               this._flushBuffer();
            }

            this._outputBuffer[this._outputTail++] = 34;
         }

      }
   }

   protected final void _writePPFieldName(String name, boolean commaBefore) throws IOException, JsonGenerationException {
      if (commaBefore) {
         this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
      } else {
         this._cfgPrettyPrinter.beforeObjectEntries(this);
      }

      if (this.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES)) {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 34;
         int len = name.length();
         if (len <= this._charBufferLength) {
            name.getChars(0, len, this._charBuffer, 0);
            if (len <= this._outputMaxContiguous) {
               if (this._outputTail + len > this._outputEnd) {
                  this._flushBuffer();
               }

               this._writeStringSegment(this._charBuffer, 0, len);
            } else {
               this._writeStringSegments(this._charBuffer, 0, len);
            }
         } else {
            this._writeStringSegments(name);
         }

         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 34;
      } else {
         this._writeStringSegments(name);
      }

   }

   protected final void _writePPFieldName(SerializableString name, boolean commaBefore) throws IOException, JsonGenerationException {
      if (commaBefore) {
         this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
      } else {
         this._cfgPrettyPrinter.beforeObjectEntries(this);
      }

      boolean addQuotes = this.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
      if (addQuotes) {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 34;
      }

      this._writeBytes(name.asQuotedUTF8());
      if (addQuotes) {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = 34;
      }

   }

   public void writeString(String text) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write text value");
      if (text == null) {
         this._writeNull();
      } else {
         int len = text.length();
         if (len > this._charBufferLength) {
            this._writeLongString(text);
         } else {
            text.getChars(0, len, this._charBuffer, 0);
            if (len > this._outputMaxContiguous) {
               this._writeLongString(this._charBuffer, 0, len);
            } else {
               if (this._outputTail + len + 2 > this._outputEnd) {
                  this._flushBuffer();
               }

               this._outputBuffer[this._outputTail++] = 34;
               this._writeStringSegment(this._charBuffer, 0, len);
               this._outputBuffer[this._outputTail++] = 34;
            }
         }
      }
   }

   private final void _writeLongString(String text) throws IOException, JsonGenerationException {
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
      this._writeStringSegments(text);
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
   }

   private final void _writeLongString(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
      this._writeStringSegments(this._charBuffer, 0, len);
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
   }

   public void writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write text value");
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
      if (len <= this._outputMaxContiguous) {
         if (this._outputTail + len > this._outputEnd) {
            this._flushBuffer();
         }

         this._writeStringSegment(text, offset, len);
      } else {
         this._writeStringSegments(text, offset, len);
      }

      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
   }

   public final void writeString(SerializableString text) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write text value");
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
      this._writeBytes(text.asQuotedUTF8());
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
   }

   public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write text value");
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
      this._writeBytes(text, offset, length);
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
   }

   public void writeUTF8String(byte[] text, int offset, int len) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write text value");
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
      if (len <= this._outputMaxContiguous) {
         this._writeUTF8Segment(text, offset, len);
      } else {
         this._writeUTF8Segments(text, offset, len);
      }

      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
   }

   public void writeRaw(String text) throws IOException, JsonGenerationException {
      int start = 0;

      int len2;
      for(int len = text.length(); len > 0; len -= len2) {
         char[] buf = this._charBuffer;
         int blen = buf.length;
         len2 = len < blen ? len : blen;
         text.getChars(start, start + len2, buf, 0);
         this.writeRaw((char[])buf, 0, len2);
         start += len2;
      }

   }

   public void writeRaw(String text, int offset, int len) throws IOException, JsonGenerationException {
      while(len > 0) {
         char[] buf = this._charBuffer;
         int blen = buf.length;
         int len2 = len < blen ? len : blen;
         text.getChars(offset, offset + len2, buf, 0);
         this.writeRaw((char[])buf, 0, len2);
         offset += len2;
         len -= len2;
      }

   }

   public final void writeRaw(char[] cbuf, int offset, int len) throws IOException, JsonGenerationException {
      int len3 = len + len + len;
      if (this._outputTail + len3 > this._outputEnd) {
         if (this._outputEnd < len3) {
            this._writeSegmentedRaw(cbuf, offset, len);
            return;
         }

         this._flushBuffer();
      }

      len += offset;

      while(offset < len) {
         while(true) {
            int ch = cbuf[offset];
            if (ch > 127) {
               ch = cbuf[offset++];
               if (ch < 2048) {
                  this._outputBuffer[this._outputTail++] = (byte)(192 | ch >> 6);
                  this._outputBuffer[this._outputTail++] = (byte)(128 | ch & 63);
               } else {
                  this._outputRawMultiByteChar(ch, cbuf, offset, len);
               }
            } else {
               this._outputBuffer[this._outputTail++] = (byte)ch;
               ++offset;
               if (offset >= len) {
                  return;
               }
            }
         }
      }

   }

   public void writeRaw(char ch) throws IOException, JsonGenerationException {
      if (this._outputTail + 3 >= this._outputEnd) {
         this._flushBuffer();
      }

      byte[] bbuf = this._outputBuffer;
      if (ch <= 127) {
         bbuf[this._outputTail++] = (byte)ch;
      } else if (ch < 2048) {
         bbuf[this._outputTail++] = (byte)(192 | ch >> 6);
         bbuf[this._outputTail++] = (byte)(128 | ch & 63);
      } else {
         this._outputRawMultiByteChar(ch, (char[])null, 0, 0);
      }

   }

   private final void _writeSegmentedRaw(char[] cbuf, int offset, int len) throws IOException, JsonGenerationException {
      int end = this._outputEnd;
      byte[] bbuf = this._outputBuffer;

      while(offset < len) {
         while(true) {
            int ch = cbuf[offset];
            if (ch >= 128) {
               if (this._outputTail + 3 >= this._outputEnd) {
                  this._flushBuffer();
               }

               ch = cbuf[offset++];
               if (ch < 2048) {
                  bbuf[this._outputTail++] = (byte)(192 | ch >> 6);
                  bbuf[this._outputTail++] = (byte)(128 | ch & 63);
               } else {
                  this._outputRawMultiByteChar(ch, cbuf, offset, len);
               }
            } else {
               if (this._outputTail >= end) {
                  this._flushBuffer();
               }

               bbuf[this._outputTail++] = (byte)ch;
               ++offset;
               if (offset >= len) {
                  return;
               }
            }
         }
      }

   }

   public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write binary value");
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
      this._writeBinary(b64variant, data, offset, offset + len);
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
   }

   public void writeNumber(int i) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write number");
      if (this._outputTail + 11 >= this._outputEnd) {
         this._flushBuffer();
      }

      if (this._cfgNumbersAsStrings) {
         this._writeQuotedInt(i);
      } else {
         this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
      }
   }

   private final void _writeQuotedInt(int i) throws IOException {
      if (this._outputTail + 13 >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
      this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
      this._outputBuffer[this._outputTail++] = 34;
   }

   public void writeNumber(long l) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write number");
      if (this._cfgNumbersAsStrings) {
         this._writeQuotedLong(l);
      } else {
         if (this._outputTail + 21 >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputTail = NumberOutput.outputLong(l, this._outputBuffer, this._outputTail);
      }
   }

   private final void _writeQuotedLong(long l) throws IOException {
      if (this._outputTail + 23 >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
      this._outputTail = NumberOutput.outputLong(l, this._outputBuffer, this._outputTail);
      this._outputBuffer[this._outputTail++] = 34;
   }

   public void writeNumber(BigInteger value) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write number");
      if (value == null) {
         this._writeNull();
      } else if (this._cfgNumbersAsStrings) {
         this._writeQuotedRaw(value);
      } else {
         this.writeRaw(value.toString());
      }

   }

   public void writeNumber(double d) throws IOException, JsonGenerationException {
      if (!this._cfgNumbersAsStrings && (!Double.isNaN(d) && !Double.isInfinite(d) || !this.isEnabled(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS))) {
         this._verifyValueWrite("write number");
         this.writeRaw(String.valueOf(d));
      } else {
         this.writeString(String.valueOf(d));
      }
   }

   public void writeNumber(float f) throws IOException, JsonGenerationException {
      if (!this._cfgNumbersAsStrings && (!Float.isNaN(f) && !Float.isInfinite(f) || !this.isEnabled(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS))) {
         this._verifyValueWrite("write number");
         this.writeRaw(String.valueOf(f));
      } else {
         this.writeString(String.valueOf(f));
      }
   }

   public void writeNumber(BigDecimal value) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write number");
      if (value == null) {
         this._writeNull();
      } else if (this._cfgNumbersAsStrings) {
         this._writeQuotedRaw(value);
      } else {
         this.writeRaw(value.toString());
      }

   }

   public void writeNumber(String encodedValue) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write number");
      if (this._cfgNumbersAsStrings) {
         this._writeQuotedRaw(encodedValue);
      } else {
         this.writeRaw(encodedValue);
      }

   }

   private final void _writeQuotedRaw(Object value) throws IOException {
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
      this.writeRaw(value.toString());
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = 34;
   }

   public void writeBoolean(boolean state) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write boolean value");
      if (this._outputTail + 5 >= this._outputEnd) {
         this._flushBuffer();
      }

      byte[] keyword = state ? TRUE_BYTES : FALSE_BYTES;
      int len = keyword.length;
      System.arraycopy(keyword, 0, this._outputBuffer, this._outputTail, len);
      this._outputTail += len;
   }

   public void writeNull() throws IOException, JsonGenerationException {
      this._verifyValueWrite("write null value");
      this._writeNull();
   }

   protected final void _verifyValueWrite(String typeMsg) throws IOException, JsonGenerationException {
      int status = this._writeContext.writeValue();
      if (status == 5) {
         this._reportError("Can not " + typeMsg + ", expecting field name");
      }

      if (this._cfgPrettyPrinter == null) {
         byte b;
         switch(status) {
         case 0:
         default:
            return;
         case 1:
            b = 44;
            break;
         case 2:
            b = 58;
            break;
         case 3:
            b = 32;
         }

         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail] = b;
         ++this._outputTail;
      } else {
         this._verifyPrettyValueWrite(typeMsg, status);
      }
   }

   protected final void _verifyPrettyValueWrite(String typeMsg, int status) throws IOException, JsonGenerationException {
      switch(status) {
      case 0:
         if (this._writeContext.inArray()) {
            this._cfgPrettyPrinter.beforeArrayValues(this);
         } else if (this._writeContext.inObject()) {
            this._cfgPrettyPrinter.beforeObjectEntries(this);
         }
         break;
      case 1:
         this._cfgPrettyPrinter.writeArrayValueSeparator(this);
         break;
      case 2:
         this._cfgPrettyPrinter.writeObjectFieldValueSeparator(this);
         break;
      case 3:
         this._cfgPrettyPrinter.writeRootValueSeparator(this);
         break;
      default:
         this._cantHappen();
      }

   }

   public final void flush() throws IOException {
      this._flushBuffer();
      if (this._outputStream != null && this.isEnabled(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)) {
         this._outputStream.flush();
      }

   }

   public void close() throws IOException {
      super.close();
      if (this._outputBuffer != null && this.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)) {
         label30:
         while(true) {
            while(true) {
               JsonStreamContext ctxt = this.getOutputContext();
               if (ctxt.inArray()) {
                  this.writeEndArray();
               } else {
                  if (!ctxt.inObject()) {
                     break label30;
                  }

                  this.writeEndObject();
               }
            }
         }
      }

      this._flushBuffer();
      if (!this._ioContext.isResourceManaged() && !this.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET)) {
         this._outputStream.flush();
      } else {
         this._outputStream.close();
      }

      this._releaseBuffers();
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

   }

   private final void _writeBytes(byte[] bytes) throws IOException {
      int len = bytes.length;
      if (this._outputTail + len > this._outputEnd) {
         this._flushBuffer();
         if (len > 512) {
            this._outputStream.write(bytes, 0, len);
            return;
         }
      }

      System.arraycopy(bytes, 0, this._outputBuffer, this._outputTail, len);
      this._outputTail += len;
   }

   private final void _writeBytes(byte[] bytes, int offset, int len) throws IOException {
      if (this._outputTail + len > this._outputEnd) {
         this._flushBuffer();
         if (len > 512) {
            this._outputStream.write(bytes, offset, len);
            return;
         }
      }

      System.arraycopy(bytes, offset, this._outputBuffer, this._outputTail, len);
      this._outputTail += len;
   }

   private final void _writeStringSegments(String text) throws IOException, JsonGenerationException {
      int left = text.length();
      int offset = 0;

      int len;
      for(char[] cbuf = this._charBuffer; left > 0; left -= len) {
         len = Math.min(this._outputMaxContiguous, left);
         text.getChars(offset, offset + len, cbuf, 0);
         if (this._outputTail + len > this._outputEnd) {
            this._flushBuffer();
         }

         this._writeStringSegment(cbuf, 0, len);
         offset += len;
      }

   }

   private final void _writeStringSegments(char[] cbuf, int offset, int totalLen) throws IOException, JsonGenerationException {
      do {
         int len = Math.min(this._outputMaxContiguous, totalLen);
         if (this._outputTail + len > this._outputEnd) {
            this._flushBuffer();
         }

         this._writeStringSegment(cbuf, offset, len);
         offset += len;
         totalLen -= len;
      } while(totalLen > 0);

   }

   private final void _writeStringSegment(char[] cbuf, int offset, int len) throws IOException, JsonGenerationException {
      len += offset;
      int outputPtr = this._outputTail;
      byte[] outputBuffer = this._outputBuffer;

      for(int[] escCodes = this._outputEscapes; offset < len; ++offset) {
         int ch = cbuf[offset];
         if (ch > 127 || escCodes[ch] != 0) {
            break;
         }

         outputBuffer[outputPtr++] = (byte)ch;
      }

      this._outputTail = outputPtr;
      if (offset < len) {
         if (this._characterEscapes != null) {
            this._writeCustomStringSegment2(cbuf, offset, len);
         } else if (this._maximumNonEscapedChar == 0) {
            this._writeStringSegment2(cbuf, offset, len);
         } else {
            this._writeStringSegmentASCII2(cbuf, offset, len);
         }
      }

   }

   private final void _writeStringSegment2(char[] cbuf, int offset, int end) throws IOException, JsonGenerationException {
      if (this._outputTail + 6 * (end - offset) > this._outputEnd) {
         this._flushBuffer();
      }

      int outputPtr = this._outputTail;
      byte[] outputBuffer = this._outputBuffer;
      int[] escCodes = this._outputEscapes;

      while(offset < end) {
         int ch = cbuf[offset++];
         if (ch <= 127) {
            if (escCodes[ch] == 0) {
               outputBuffer[outputPtr++] = (byte)ch;
            } else {
               int escape = escCodes[ch];
               if (escape > 0) {
                  outputBuffer[outputPtr++] = 92;
                  outputBuffer[outputPtr++] = (byte)escape;
               } else {
                  outputPtr = this._writeGenericEscape(ch, outputPtr);
               }
            }
         } else if (ch <= 2047) {
            outputBuffer[outputPtr++] = (byte)(192 | ch >> 6);
            outputBuffer[outputPtr++] = (byte)(128 | ch & 63);
         } else {
            outputPtr = this._outputMultiByteChar(ch, outputPtr);
         }
      }

      this._outputTail = outputPtr;
   }

   private final void _writeStringSegmentASCII2(char[] cbuf, int offset, int end) throws IOException, JsonGenerationException {
      if (this._outputTail + 6 * (end - offset) > this._outputEnd) {
         this._flushBuffer();
      }

      int outputPtr = this._outputTail;
      byte[] outputBuffer = this._outputBuffer;
      int[] escCodes = this._outputEscapes;
      int maxUnescaped = this._maximumNonEscapedChar;

      while(offset < end) {
         int ch = cbuf[offset++];
         if (ch <= 127) {
            if (escCodes[ch] == 0) {
               outputBuffer[outputPtr++] = (byte)ch;
            } else {
               int escape = escCodes[ch];
               if (escape > 0) {
                  outputBuffer[outputPtr++] = 92;
                  outputBuffer[outputPtr++] = (byte)escape;
               } else {
                  outputPtr = this._writeGenericEscape(ch, outputPtr);
               }
            }
         } else if (ch > maxUnescaped) {
            outputPtr = this._writeGenericEscape(ch, outputPtr);
         } else if (ch <= 2047) {
            outputBuffer[outputPtr++] = (byte)(192 | ch >> 6);
            outputBuffer[outputPtr++] = (byte)(128 | ch & 63);
         } else {
            outputPtr = this._outputMultiByteChar(ch, outputPtr);
         }
      }

      this._outputTail = outputPtr;
   }

   private final void _writeCustomStringSegment2(char[] cbuf, int offset, int end) throws IOException, JsonGenerationException {
      if (this._outputTail + 6 * (end - offset) > this._outputEnd) {
         this._flushBuffer();
      }

      int outputPtr = this._outputTail;
      byte[] outputBuffer = this._outputBuffer;
      int[] escCodes = this._outputEscapes;
      int maxUnescaped = this._maximumNonEscapedChar <= 0 ? '\uffff' : this._maximumNonEscapedChar;
      CharacterEscapes customEscapes = this._characterEscapes;

      while(offset < end) {
         int ch = cbuf[offset++];
         if (ch <= 127) {
            if (escCodes[ch] == 0) {
               outputBuffer[outputPtr++] = (byte)ch;
            } else {
               int escape = escCodes[ch];
               if (escape > 0) {
                  outputBuffer[outputPtr++] = 92;
                  outputBuffer[outputPtr++] = (byte)escape;
               } else if (escape == -2) {
                  SerializableString esc = customEscapes.getEscapeSequence(ch);
                  if (esc == null) {
                     throw new JsonGenerationException("Invalid custom escape definitions; custom escape not found for character code 0x" + Integer.toHexString(ch) + ", although was supposed to have one");
                  }

                  outputPtr = this._writeCustomEscape(outputBuffer, outputPtr, esc, end - offset);
               } else {
                  outputPtr = this._writeGenericEscape(ch, outputPtr);
               }
            }
         } else if (ch > maxUnescaped) {
            outputPtr = this._writeGenericEscape(ch, outputPtr);
         } else {
            SerializableString esc = customEscapes.getEscapeSequence(ch);
            if (esc != null) {
               outputPtr = this._writeCustomEscape(outputBuffer, outputPtr, esc, end - offset);
            } else if (ch <= 2047) {
               outputBuffer[outputPtr++] = (byte)(192 | ch >> 6);
               outputBuffer[outputPtr++] = (byte)(128 | ch & 63);
            } else {
               outputPtr = this._outputMultiByteChar(ch, outputPtr);
            }
         }
      }

      this._outputTail = outputPtr;
   }

   private int _writeCustomEscape(byte[] outputBuffer, int outputPtr, SerializableString esc, int remainingChars) throws IOException, JsonGenerationException {
      byte[] raw = esc.asUnquotedUTF8();
      int len = raw.length;
      if (len > 6) {
         return this._handleLongCustomEscape(outputBuffer, outputPtr, this._outputEnd, raw, remainingChars);
      } else {
         System.arraycopy(raw, 0, outputBuffer, outputPtr, len);
         return outputPtr + len;
      }
   }

   private int _handleLongCustomEscape(byte[] outputBuffer, int outputPtr, int outputEnd, byte[] raw, int remainingChars) throws IOException, JsonGenerationException {
      int len = raw.length;
      if (outputPtr + len > outputEnd) {
         this._outputTail = outputPtr;
         this._flushBuffer();
         outputPtr = this._outputTail;
         if (len > outputBuffer.length) {
            this._outputStream.write(raw, 0, len);
            return outputPtr;
         }

         System.arraycopy(raw, 0, outputBuffer, outputPtr, len);
         outputPtr += len;
      }

      if (outputPtr + 6 * remainingChars > outputEnd) {
         this._flushBuffer();
         return this._outputTail;
      } else {
         return outputPtr;
      }
   }

   private final void _writeUTF8Segments(byte[] utf8, int offset, int totalLen) throws IOException, JsonGenerationException {
      do {
         int len = Math.min(this._outputMaxContiguous, totalLen);
         this._writeUTF8Segment(utf8, offset, len);
         offset += len;
         totalLen -= len;
      } while(totalLen > 0);

   }

   private final void _writeUTF8Segment(byte[] utf8, int offset, int len) throws IOException, JsonGenerationException {
      int[] escCodes = this._outputEscapes;
      int ptr = offset;
      int end = offset + len;

      byte ch;
      do {
         if (ptr >= end) {
            if (this._outputTail + len > this._outputEnd) {
               this._flushBuffer();
            }

            System.arraycopy(utf8, offset, this._outputBuffer, this._outputTail, len);
            this._outputTail += len;
            return;
         }

         ch = utf8[ptr++];
      } while(ch < 0 || escCodes[ch] == 0);

      this._writeUTF8Segment2(utf8, offset, len);
   }

   private final void _writeUTF8Segment2(byte[] utf8, int offset, int len) throws IOException, JsonGenerationException {
      int outputPtr = this._outputTail;
      if (outputPtr + len * 6 > this._outputEnd) {
         this._flushBuffer();
         outputPtr = this._outputTail;
      }

      byte[] outputBuffer = this._outputBuffer;
      int[] escCodes = this._outputEscapes;
      len += offset;

      while(true) {
         while(offset < len) {
            byte b = utf8[offset++];
            if (b >= 0 && escCodes[b] != 0) {
               int escape = escCodes[b];
               if (escape > 0) {
                  outputBuffer[outputPtr++] = 92;
                  outputBuffer[outputPtr++] = (byte)escape;
               } else {
                  outputPtr = this._writeGenericEscape(b, outputPtr);
               }
            } else {
               outputBuffer[outputPtr++] = b;
            }
         }

         this._outputTail = outputPtr;
         return;
      }
   }

   protected void _writeBinary(Base64Variant b64variant, byte[] input, int inputPtr, int inputEnd) throws IOException, JsonGenerationException {
      int safeInputEnd = inputEnd - 3;
      int safeOutputEnd = this._outputEnd - 6;
      int chunksBeforeLF = b64variant.getMaxLineLength() >> 2;

      int inputLeft;
      while(inputPtr <= safeInputEnd) {
         if (this._outputTail > safeOutputEnd) {
            this._flushBuffer();
         }

         inputLeft = input[inputPtr++] << 8;
         inputLeft |= input[inputPtr++] & 255;
         inputLeft = inputLeft << 8 | input[inputPtr++] & 255;
         this._outputTail = b64variant.encodeBase64Chunk(inputLeft, this._outputBuffer, this._outputTail);
         --chunksBeforeLF;
         if (chunksBeforeLF <= 0) {
            this._outputBuffer[this._outputTail++] = 92;
            this._outputBuffer[this._outputTail++] = 110;
            chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
         }
      }

      inputLeft = inputEnd - inputPtr;
      if (inputLeft > 0) {
         if (this._outputTail > safeOutputEnd) {
            this._flushBuffer();
         }

         int b24 = input[inputPtr++] << 16;
         if (inputLeft == 2) {
            b24 |= (input[inputPtr++] & 255) << 8;
         }

         this._outputTail = b64variant.encodeBase64Partial(b24, inputLeft, this._outputBuffer, this._outputTail);
      }

   }

   private final int _outputRawMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputLen) throws IOException {
      if (ch >= 55296 && ch <= 57343) {
         if (inputOffset >= inputLen) {
            this._reportError("Split surrogate on writeRaw() input (last character)");
         }

         this._outputSurrogates(ch, cbuf[inputOffset]);
         return inputOffset + 1;
      } else {
         byte[] bbuf = this._outputBuffer;
         bbuf[this._outputTail++] = (byte)(224 | ch >> 12);
         bbuf[this._outputTail++] = (byte)(128 | ch >> 6 & 63);
         bbuf[this._outputTail++] = (byte)(128 | ch & 63);
         return inputOffset;
      }
   }

   protected final void _outputSurrogates(int surr1, int surr2) throws IOException {
      int c = this._decodeSurrogate(surr1, surr2);
      if (this._outputTail + 4 > this._outputEnd) {
         this._flushBuffer();
      }

      byte[] bbuf = this._outputBuffer;
      bbuf[this._outputTail++] = (byte)(240 | c >> 18);
      bbuf[this._outputTail++] = (byte)(128 | c >> 12 & 63);
      bbuf[this._outputTail++] = (byte)(128 | c >> 6 & 63);
      bbuf[this._outputTail++] = (byte)(128 | c & 63);
   }

   private final int _outputMultiByteChar(int ch, int outputPtr) throws IOException {
      byte[] bbuf = this._outputBuffer;
      if (ch >= 55296 && ch <= 57343) {
         bbuf[outputPtr++] = 92;
         bbuf[outputPtr++] = 117;
         bbuf[outputPtr++] = HEX_CHARS[ch >> 12 & 15];
         bbuf[outputPtr++] = HEX_CHARS[ch >> 8 & 15];
         bbuf[outputPtr++] = HEX_CHARS[ch >> 4 & 15];
         bbuf[outputPtr++] = HEX_CHARS[ch & 15];
      } else {
         bbuf[outputPtr++] = (byte)(224 | ch >> 12);
         bbuf[outputPtr++] = (byte)(128 | ch >> 6 & 63);
         bbuf[outputPtr++] = (byte)(128 | ch & 63);
      }

      return outputPtr;
   }

   protected final int _decodeSurrogate(int surr1, int surr2) throws IOException {
      if (surr2 < 56320 || surr2 > 57343) {
         String msg = "Incomplete surrogate pair: first char 0x" + Integer.toHexString(surr1) + ", second 0x" + Integer.toHexString(surr2);
         this._reportError(msg);
      }

      int c = 65536 + (surr1 - '\ud800' << 10) + (surr2 - '\udc00');
      return c;
   }

   private final void _writeNull() throws IOException {
      if (this._outputTail + 4 >= this._outputEnd) {
         this._flushBuffer();
      }

      System.arraycopy(NULL_BYTES, 0, this._outputBuffer, this._outputTail, 4);
      this._outputTail += 4;
   }

   private int _writeGenericEscape(int charToEscape, int outputPtr) throws IOException {
      byte[] bbuf = this._outputBuffer;
      bbuf[outputPtr++] = 92;
      bbuf[outputPtr++] = 117;
      if (charToEscape > 255) {
         int hi = charToEscape >> 8 & 255;
         bbuf[outputPtr++] = HEX_CHARS[hi >> 4];
         bbuf[outputPtr++] = HEX_CHARS[hi & 15];
         charToEscape &= 255;
      } else {
         bbuf[outputPtr++] = 48;
         bbuf[outputPtr++] = 48;
      }

      bbuf[outputPtr++] = HEX_CHARS[charToEscape >> 4];
      bbuf[outputPtr++] = HEX_CHARS[charToEscape & 15];
      return outputPtr;
   }

   protected final void _flushBuffer() throws IOException {
      int len = this._outputTail;
      if (len > 0) {
         this._outputTail = 0;
         this._outputStream.write(this._outputBuffer, 0, len);
      }

   }
}
