package org.codehaus.jackson.impl;

import java.io.IOException;
import java.io.Writer;
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

public final class WriterBasedGenerator extends JsonGeneratorBase {
   protected static final int SHORT_WRITE = 32;
   protected static final char[] HEX_CHARS = CharTypes.copyHexChars();
   protected static final int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();
   protected final IOContext _ioContext;
   protected final Writer _writer;
   protected int[] _outputEscapes;
   protected int _maximumNonEscapedChar;
   protected CharacterEscapes _characterEscapes;
   protected SerializableString _currentEscape;
   protected char[] _outputBuffer;
   protected int _outputHead;
   protected int _outputTail;
   protected int _outputEnd;
   protected char[] _entityBuffer;

   public WriterBasedGenerator(IOContext ctxt, int features, ObjectCodec codec, Writer w) {
      super(features, codec);
      this._outputEscapes = sOutputEscapes;
      this._outputHead = 0;
      this._outputTail = 0;
      this._ioContext = ctxt;
      this._writer = w;
      this._outputBuffer = ctxt.allocConcatBuffer();
      this._outputEnd = this._outputBuffer.length;
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
      return this._writer;
   }

   public final void writeFieldName(String name) throws IOException, JsonGenerationException {
      int status = this._writeContext.writeFieldName(name);
      if (status == 4) {
         this._reportError("Can not write a field name, expecting a value");
      }

      this._writeFieldName(name, status == 1);
   }

   public final void writeStringField(String fieldName, String value) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeString(value);
   }

   public final void writeFieldName(SerializedString name) throws IOException, JsonGenerationException {
      int status = this._writeContext.writeFieldName(name.getValue());
      if (status == 4) {
         this._reportError("Can not write a field name, expecting a value");
      }

      this._writeFieldName((SerializableString)name, status == 1);
   }

   public final void writeFieldName(SerializableString name) throws IOException, JsonGenerationException {
      int status = this._writeContext.writeFieldName(name.getValue());
      if (status == 4) {
         this._reportError("Can not write a field name, expecting a value");
      }

      this._writeFieldName(name, status == 1);
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

         this._outputBuffer[this._outputTail++] = '[';
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

         this._outputBuffer[this._outputTail++] = ']';
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

         this._outputBuffer[this._outputTail++] = '{';
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

         this._outputBuffer[this._outputTail++] = '}';
      }

   }

   protected void _writeFieldName(String name, boolean commaBefore) throws IOException, JsonGenerationException {
      if (this._cfgPrettyPrinter != null) {
         this._writePPFieldName(name, commaBefore);
      } else {
         if (this._outputTail + 1 >= this._outputEnd) {
            this._flushBuffer();
         }

         if (commaBefore) {
            this._outputBuffer[this._outputTail++] = ',';
         }

         if (!this.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES)) {
            this._writeString(name);
         } else {
            this._outputBuffer[this._outputTail++] = '"';
            this._writeString(name);
            if (this._outputTail >= this._outputEnd) {
               this._flushBuffer();
            }

            this._outputBuffer[this._outputTail++] = '"';
         }
      }
   }

   public void _writeFieldName(SerializableString name, boolean commaBefore) throws IOException, JsonGenerationException {
      if (this._cfgPrettyPrinter != null) {
         this._writePPFieldName(name, commaBefore);
      } else {
         if (this._outputTail + 1 >= this._outputEnd) {
            this._flushBuffer();
         }

         if (commaBefore) {
            this._outputBuffer[this._outputTail++] = ',';
         }

         char[] quoted = name.asQuotedChars();
         if (!this.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES)) {
            this.writeRaw((char[])quoted, 0, quoted.length);
         } else {
            this._outputBuffer[this._outputTail++] = '"';
            int qlen = quoted.length;
            if (this._outputTail + qlen + 1 >= this._outputEnd) {
               this.writeRaw((char[])quoted, 0, qlen);
               if (this._outputTail >= this._outputEnd) {
                  this._flushBuffer();
               }

               this._outputBuffer[this._outputTail++] = '"';
            } else {
               System.arraycopy(quoted, 0, this._outputBuffer, this._outputTail, qlen);
               this._outputTail += qlen;
               this._outputBuffer[this._outputTail++] = '"';
            }

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

         this._outputBuffer[this._outputTail++] = '"';
         this._writeString(name);
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = '"';
      } else {
         this._writeString(name);
      }

   }

   protected final void _writePPFieldName(SerializableString name, boolean commaBefore) throws IOException, JsonGenerationException {
      if (commaBefore) {
         this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
      } else {
         this._cfgPrettyPrinter.beforeObjectEntries(this);
      }

      char[] quoted = name.asQuotedChars();
      if (this.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES)) {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = '"';
         this.writeRaw((char[])quoted, 0, quoted.length);
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = '"';
      } else {
         this.writeRaw((char[])quoted, 0, quoted.length);
      }

   }

   public void writeString(String text) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write text value");
      if (text == null) {
         this._writeNull();
      } else {
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = '"';
         this._writeString(text);
         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = '"';
      }
   }

   public void writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write text value");
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = '"';
      this._writeString(text, offset, len);
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = '"';
   }

   public final void writeString(SerializableString sstr) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write text value");
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = '"';
      char[] text = sstr.asQuotedChars();
      int len = text.length;
      if (len < 32) {
         int room = this._outputEnd - this._outputTail;
         if (len > room) {
            this._flushBuffer();
         }

         System.arraycopy(text, 0, this._outputBuffer, this._outputTail, len);
         this._outputTail += len;
      } else {
         this._flushBuffer();
         this._writer.write(text, 0, len);
      }

      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = '"';
   }

   public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeRaw(String text) throws IOException, JsonGenerationException {
      int len = text.length();
      int room = this._outputEnd - this._outputTail;
      if (room == 0) {
         this._flushBuffer();
         room = this._outputEnd - this._outputTail;
      }

      if (room >= len) {
         text.getChars(0, len, this._outputBuffer, this._outputTail);
         this._outputTail += len;
      } else {
         this.writeRawLong(text);
      }

   }

   public void writeRaw(String text, int start, int len) throws IOException, JsonGenerationException {
      int room = this._outputEnd - this._outputTail;
      if (room < len) {
         this._flushBuffer();
         room = this._outputEnd - this._outputTail;
      }

      if (room >= len) {
         text.getChars(start, start + len, this._outputBuffer, this._outputTail);
         this._outputTail += len;
      } else {
         this.writeRawLong(text.substring(start, start + len));
      }

   }

   public void writeRaw(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      if (len < 32) {
         int room = this._outputEnd - this._outputTail;
         if (len > room) {
            this._flushBuffer();
         }

         System.arraycopy(text, offset, this._outputBuffer, this._outputTail, len);
         this._outputTail += len;
      } else {
         this._flushBuffer();
         this._writer.write(text, offset, len);
      }
   }

   public void writeRaw(char c) throws IOException, JsonGenerationException {
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = c;
   }

   private void writeRawLong(String text) throws IOException, JsonGenerationException {
      int room = this._outputEnd - this._outputTail;
      text.getChars(0, room, this._outputBuffer, this._outputTail);
      this._outputTail += room;
      this._flushBuffer();
      int offset = room;

      int len;
      int amount;
      for(len = text.length() - room; len > this._outputEnd; len -= amount) {
         amount = this._outputEnd;
         text.getChars(offset, offset + amount, this._outputBuffer, 0);
         this._outputHead = 0;
         this._outputTail = amount;
         this._flushBuffer();
         offset += amount;
      }

      text.getChars(offset, offset + len, this._outputBuffer, 0);
      this._outputHead = 0;
      this._outputTail = len;
   }

   public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write binary value");
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = '"';
      this._writeBinary(b64variant, data, offset, offset + len);
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = '"';
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

      this._outputBuffer[this._outputTail++] = '"';
      this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
      this._outputBuffer[this._outputTail++] = '"';
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

      this._outputBuffer[this._outputTail++] = '"';
      this._outputTail = NumberOutput.outputLong(l, this._outputBuffer, this._outputTail);
      this._outputBuffer[this._outputTail++] = '"';
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

      this._outputBuffer[this._outputTail++] = '"';
      this.writeRaw(value.toString());
      if (this._outputTail >= this._outputEnd) {
         this._flushBuffer();
      }

      this._outputBuffer[this._outputTail++] = '"';
   }

   public void writeBoolean(boolean state) throws IOException, JsonGenerationException {
      this._verifyValueWrite("write boolean value");
      if (this._outputTail + 5 >= this._outputEnd) {
         this._flushBuffer();
      }

      int ptr = this._outputTail;
      char[] buf = this._outputBuffer;
      if (state) {
         buf[ptr] = 't';
         ++ptr;
         buf[ptr] = 'r';
         ++ptr;
         buf[ptr] = 'u';
         ++ptr;
         buf[ptr] = 'e';
      } else {
         buf[ptr] = 'f';
         ++ptr;
         buf[ptr] = 'a';
         ++ptr;
         buf[ptr] = 'l';
         ++ptr;
         buf[ptr] = 's';
         ++ptr;
         buf[ptr] = 'e';
      }

      this._outputTail = ptr + 1;
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
         byte c;
         switch(status) {
         case 0:
         default:
            return;
         case 1:
            c = 44;
            break;
         case 2:
            c = 58;
            break;
         case 3:
            c = 32;
         }

         if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail] = (char)c;
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
      if (this._writer != null && this.isEnabled(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)) {
         this._writer.flush();
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
         this._writer.flush();
      } else {
         this._writer.close();
      }

      this._releaseBuffers();
   }

   protected void _releaseBuffers() {
      char[] buf = this._outputBuffer;
      if (buf != null) {
         this._outputBuffer = null;
         this._ioContext.releaseConcatBuffer(buf);
      }

   }

   private void _writeString(String text) throws IOException, JsonGenerationException {
      int len = text.length();
      if (len > this._outputEnd) {
         this._writeLongString(text);
      } else {
         if (this._outputTail + len > this._outputEnd) {
            this._flushBuffer();
         }

         text.getChars(0, len, this._outputBuffer, this._outputTail);
         if (this._characterEscapes != null) {
            this._writeStringCustom(len);
         } else if (this._maximumNonEscapedChar != 0) {
            this._writeStringASCII(len, this._maximumNonEscapedChar);
         } else {
            this._writeString2(len);
         }

      }
   }

   private void _writeString2(int len) throws IOException, JsonGenerationException {
      int end = this._outputTail + len;
      int[] escCodes = this._outputEscapes;
      int escLen = escCodes.length;

      while(this._outputTail < end) {
         while(true) {
            char c = this._outputBuffer[this._outputTail];
            if (c < escLen && escCodes[c] != 0) {
               int flushLen = this._outputTail - this._outputHead;
               if (flushLen > 0) {
                  this._writer.write(this._outputBuffer, this._outputHead, flushLen);
               }

               char c = this._outputBuffer[this._outputTail++];
               this._prependOrWriteCharacterEscape(c, escCodes[c]);
            } else if (++this._outputTail >= end) {
               return;
            }
         }
      }

   }

   private void _writeLongString(String text) throws IOException, JsonGenerationException {
      this._flushBuffer();
      int textLen = text.length();
      int offset = 0;

      do {
         int max = this._outputEnd;
         int segmentLen = offset + max > textLen ? textLen - offset : max;
         text.getChars(offset, offset + segmentLen, this._outputBuffer, 0);
         if (this._characterEscapes != null) {
            this._writeSegmentCustom(segmentLen);
         } else if (this._maximumNonEscapedChar != 0) {
            this._writeSegmentASCII(segmentLen, this._maximumNonEscapedChar);
         } else {
            this._writeSegment(segmentLen);
         }

         offset += segmentLen;
      } while(offset < textLen);

   }

   private final void _writeSegment(int end) throws IOException, JsonGenerationException {
      int[] escCodes = this._outputEscapes;
      int escLen = escCodes.length;
      int ptr = 0;

      char c;
      for(int start = ptr; ptr < end; start = this._prependOrWriteCharacterEscape(this._outputBuffer, ptr, end, c, escCodes[c])) {
         do {
            c = this._outputBuffer[ptr];
            if (c < escLen && escCodes[c] != 0) {
               break;
            }

            ++ptr;
         } while(ptr < end);

         int flushLen = ptr - start;
         if (flushLen > 0) {
            this._writer.write(this._outputBuffer, start, flushLen);
            if (ptr >= end) {
               break;
            }
         }

         ++ptr;
      }

   }

   private final void _writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      if (this._characterEscapes != null) {
         this._writeStringCustom(text, offset, len);
      } else if (this._maximumNonEscapedChar != 0) {
         this._writeStringASCII(text, offset, len, this._maximumNonEscapedChar);
      } else {
         len += offset;
         int[] escCodes = this._outputEscapes;
         int escLen = escCodes.length;

         while(offset < len) {
            int start = offset;

            do {
               char c = text[offset];
               if (c < escLen && escCodes[c] != 0) {
                  break;
               }

               ++offset;
            } while(offset < len);

            int newAmount = offset - start;
            if (newAmount < 32) {
               if (this._outputTail + newAmount > this._outputEnd) {
                  this._flushBuffer();
               }

               if (newAmount > 0) {
                  System.arraycopy(text, start, this._outputBuffer, this._outputTail, newAmount);
                  this._outputTail += newAmount;
               }
            } else {
               this._flushBuffer();
               this._writer.write(text, start, newAmount);
            }

            if (offset >= len) {
               break;
            }

            char c = text[offset++];
            this._appendCharacterEscape(c, escCodes[c]);
         }

      }
   }

   private void _writeStringASCII(int len, int maxNonEscaped) throws IOException, JsonGenerationException {
      int end = this._outputTail + len;
      int[] escCodes = this._outputEscapes;
      int escLimit = Math.min(escCodes.length, this._maximumNonEscapedChar + 1);
      boolean var6 = false;

      while(this._outputTail < end) {
         char c;
         int escCode;
         while(true) {
            c = this._outputBuffer[this._outputTail];
            if (c < escLimit) {
               escCode = escCodes[c];
               if (escCode != 0) {
                  break;
               }
            } else if (c > maxNonEscaped) {
               escCode = -1;
               break;
            }

            if (++this._outputTail >= end) {
               return;
            }
         }

         int flushLen = this._outputTail - this._outputHead;
         if (flushLen > 0) {
            this._writer.write(this._outputBuffer, this._outputHead, flushLen);
         }

         ++this._outputTail;
         this._prependOrWriteCharacterEscape(c, escCode);
      }

   }

   private final void _writeSegmentASCII(int end, int maxNonEscaped) throws IOException, JsonGenerationException {
      int[] escCodes = this._outputEscapes;
      int escLimit = Math.min(escCodes.length, this._maximumNonEscapedChar + 1);
      int ptr = 0;
      int escCode = 0;

      char c;
      for(int start = ptr; ptr < end; start = this._prependOrWriteCharacterEscape(this._outputBuffer, ptr, end, c, escCode)) {
         do {
            c = this._outputBuffer[ptr];
            if (c < escLimit) {
               escCode = escCodes[c];
               if (escCode != 0) {
                  break;
               }
            } else if (c > maxNonEscaped) {
               escCode = -1;
               break;
            }

            ++ptr;
         } while(ptr < end);

         int flushLen = ptr - start;
         if (flushLen > 0) {
            this._writer.write(this._outputBuffer, start, flushLen);
            if (ptr >= end) {
               break;
            }
         }

         ++ptr;
      }

   }

   private final void _writeStringASCII(char[] text, int offset, int len, int maxNonEscaped) throws IOException, JsonGenerationException {
      len += offset;
      int[] escCodes = this._outputEscapes;
      int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);
      int escCode = 0;

      while(offset < len) {
         int start = offset;

         char c;
         do {
            c = text[offset];
            if (c < escLimit) {
               escCode = escCodes[c];
               if (escCode != 0) {
                  break;
               }
            } else if (c > maxNonEscaped) {
               escCode = -1;
               break;
            }

            ++offset;
         } while(offset < len);

         int newAmount = offset - start;
         if (newAmount < 32) {
            if (this._outputTail + newAmount > this._outputEnd) {
               this._flushBuffer();
            }

            if (newAmount > 0) {
               System.arraycopy(text, start, this._outputBuffer, this._outputTail, newAmount);
               this._outputTail += newAmount;
            }
         } else {
            this._flushBuffer();
            this._writer.write(text, start, newAmount);
         }

         if (offset >= len) {
            break;
         }

         ++offset;
         this._appendCharacterEscape(c, escCode);
      }

   }

   private void _writeStringCustom(int len) throws IOException, JsonGenerationException {
      int end = this._outputTail + len;
      int[] escCodes = this._outputEscapes;
      int maxNonEscaped = this._maximumNonEscapedChar < 1 ? '\uffff' : this._maximumNonEscapedChar;
      int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);
      int escCode = false;
      CharacterEscapes customEscapes = this._characterEscapes;

      while(this._outputTail < end) {
         char c;
         int escCode;
         while(true) {
            c = this._outputBuffer[this._outputTail];
            if (c < escLimit) {
               escCode = escCodes[c];
               if (escCode != 0) {
                  break;
               }
            } else {
               if (c > maxNonEscaped) {
                  escCode = -1;
                  break;
               }

               if ((this._currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                  escCode = -2;
                  break;
               }
            }

            if (++this._outputTail >= end) {
               return;
            }
         }

         int flushLen = this._outputTail - this._outputHead;
         if (flushLen > 0) {
            this._writer.write(this._outputBuffer, this._outputHead, flushLen);
         }

         ++this._outputTail;
         this._prependOrWriteCharacterEscape(c, escCode);
      }

   }

   private final void _writeSegmentCustom(int end) throws IOException, JsonGenerationException {
      int[] escCodes = this._outputEscapes;
      int maxNonEscaped = this._maximumNonEscapedChar < 1 ? '\uffff' : this._maximumNonEscapedChar;
      int escLimit = Math.min(escCodes.length, this._maximumNonEscapedChar + 1);
      CharacterEscapes customEscapes = this._characterEscapes;
      int ptr = 0;
      int escCode = false;

      char c;
      for(int start = ptr; ptr < end; start = this._prependOrWriteCharacterEscape(this._outputBuffer, ptr, end, c, escCodes[c])) {
         do {
            c = this._outputBuffer[ptr];
            if (c < escLimit) {
               int escCode = escCodes[c];
               if (escCode != 0) {
                  break;
               }
            } else {
               if (c > maxNonEscaped) {
                  escCode = true;
                  break;
               }

               if ((this._currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                  escCode = true;
                  break;
               }
            }

            ++ptr;
         } while(ptr < end);

         int flushLen = ptr - start;
         if (flushLen > 0) {
            this._writer.write(this._outputBuffer, start, flushLen);
            if (ptr >= end) {
               break;
            }
         }

         ++ptr;
      }

   }

   private final void _writeStringCustom(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      len += offset;
      int[] escCodes = this._outputEscapes;
      int maxNonEscaped = this._maximumNonEscapedChar < 1 ? '\uffff' : this._maximumNonEscapedChar;
      int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);
      CharacterEscapes customEscapes = this._characterEscapes;
      int escCode = 0;

      while(offset < len) {
         int start = offset;

         char c;
         do {
            c = text[offset];
            if (c < escLimit) {
               escCode = escCodes[c];
               if (escCode != 0) {
                  break;
               }
            } else {
               if (c > maxNonEscaped) {
                  escCode = -1;
                  break;
               }

               if ((this._currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                  escCode = -2;
                  break;
               }
            }

            ++offset;
         } while(offset < len);

         int newAmount = offset - start;
         if (newAmount < 32) {
            if (this._outputTail + newAmount > this._outputEnd) {
               this._flushBuffer();
            }

            if (newAmount > 0) {
               System.arraycopy(text, start, this._outputBuffer, this._outputTail, newAmount);
               this._outputTail += newAmount;
            }
         } else {
            this._flushBuffer();
            this._writer.write(text, start, newAmount);
         }

         if (offset >= len) {
            break;
         }

         ++offset;
         this._appendCharacterEscape(c, escCode);
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
            this._outputBuffer[this._outputTail++] = '\\';
            this._outputBuffer[this._outputTail++] = 'n';
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

   private final void _writeNull() throws IOException {
      if (this._outputTail + 4 >= this._outputEnd) {
         this._flushBuffer();
      }

      int ptr = this._outputTail;
      char[] buf = this._outputBuffer;
      buf[ptr] = 'n';
      ++ptr;
      buf[ptr] = 'u';
      ++ptr;
      buf[ptr] = 'l';
      ++ptr;
      buf[ptr] = 'l';
      this._outputTail = ptr + 1;
   }

   private final void _prependOrWriteCharacterEscape(char ch, int escCode) throws IOException, JsonGenerationException {
      char[] buf;
      if (escCode >= 0) {
         if (this._outputTail >= 2) {
            int ptr = this._outputTail - 2;
            this._outputHead = ptr;
            this._outputBuffer[ptr++] = '\\';
            this._outputBuffer[ptr] = (char)escCode;
         } else {
            buf = this._entityBuffer;
            if (buf == null) {
               buf = this._allocateEntityBuffer();
            }

            this._outputHead = this._outputTail;
            buf[1] = (char)escCode;
            this._writer.write(buf, 0, 2);
         }
      } else {
         int hi;
         int lo;
         if (escCode != -2) {
            if (this._outputTail >= 6) {
               buf = this._outputBuffer;
               hi = this._outputTail - 6;
               this._outputHead = hi;
               buf[hi] = '\\';
               ++hi;
               buf[hi] = 'u';
               if (ch > 255) {
                  lo = ch >> 8 & 255;
                  ++hi;
                  buf[hi] = HEX_CHARS[lo >> 4];
                  ++hi;
                  buf[hi] = HEX_CHARS[lo & 15];
                  ch = (char)(ch & 255);
               } else {
                  ++hi;
                  buf[hi] = '0';
                  ++hi;
                  buf[hi] = '0';
               }

               ++hi;
               buf[hi] = HEX_CHARS[ch >> 4];
               ++hi;
               buf[hi] = HEX_CHARS[ch & 15];
            } else {
               buf = this._entityBuffer;
               if (buf == null) {
                  buf = this._allocateEntityBuffer();
               }

               this._outputHead = this._outputTail;
               if (ch > 255) {
                  hi = ch >> 8 & 255;
                  lo = ch & 255;
                  buf[10] = HEX_CHARS[hi >> 4];
                  buf[11] = HEX_CHARS[hi & 15];
                  buf[12] = HEX_CHARS[lo >> 4];
                  buf[13] = HEX_CHARS[lo & 15];
                  this._writer.write(buf, 8, 6);
               } else {
                  buf[6] = HEX_CHARS[ch >> 4];
                  buf[7] = HEX_CHARS[ch & 15];
                  this._writer.write(buf, 2, 6);
               }

            }
         } else {
            String escape;
            if (this._currentEscape == null) {
               escape = this._characterEscapes.getEscapeSequence(ch).getValue();
            } else {
               escape = this._currentEscape.getValue();
               this._currentEscape = null;
            }

            hi = escape.length();
            if (this._outputTail >= hi) {
               lo = this._outputTail - hi;
               this._outputHead = lo;
               escape.getChars(0, hi, this._outputBuffer, lo);
            } else {
               this._outputHead = this._outputTail;
               this._writer.write(escape);
            }
         }
      }
   }

   private final int _prependOrWriteCharacterEscape(char[] buffer, int ptr, int end, char ch, int escCode) throws IOException, JsonGenerationException {
      char[] ent;
      if (escCode >= 0) {
         if (ptr > 1 && ptr < end) {
            ptr -= 2;
            buffer[ptr] = '\\';
            buffer[ptr + 1] = (char)escCode;
         } else {
            ent = this._entityBuffer;
            if (ent == null) {
               ent = this._allocateEntityBuffer();
            }

            ent[1] = (char)escCode;
            this._writer.write(ent, 0, 2);
         }

         return ptr;
      } else {
         int hi;
         if (escCode != -2) {
            if (ptr > 5 && ptr < end) {
               ptr -= 6;
               buffer[ptr++] = '\\';
               buffer[ptr++] = 'u';
               if (ch > 255) {
                  int hi = ch >> 8 & 255;
                  buffer[ptr++] = HEX_CHARS[hi >> 4];
                  buffer[ptr++] = HEX_CHARS[hi & 15];
                  ch = (char)(ch & 255);
               } else {
                  buffer[ptr++] = '0';
                  buffer[ptr++] = '0';
               }

               buffer[ptr++] = HEX_CHARS[ch >> 4];
               buffer[ptr] = HEX_CHARS[ch & 15];
               ptr -= 5;
            } else {
               ent = this._entityBuffer;
               if (ent == null) {
                  ent = this._allocateEntityBuffer();
               }

               this._outputHead = this._outputTail;
               if (ch > 255) {
                  hi = ch >> 8 & 255;
                  int lo = ch & 255;
                  ent[10] = HEX_CHARS[hi >> 4];
                  ent[11] = HEX_CHARS[hi & 15];
                  ent[12] = HEX_CHARS[lo >> 4];
                  ent[13] = HEX_CHARS[lo & 15];
                  this._writer.write(ent, 8, 6);
               } else {
                  ent[6] = HEX_CHARS[ch >> 4];
                  ent[7] = HEX_CHARS[ch & 15];
                  this._writer.write(ent, 2, 6);
               }
            }

            return ptr;
         } else {
            String escape;
            if (this._currentEscape == null) {
               escape = this._characterEscapes.getEscapeSequence(ch).getValue();
            } else {
               escape = this._currentEscape.getValue();
               this._currentEscape = null;
            }

            hi = escape.length();
            if (ptr >= hi && ptr < end) {
               ptr -= hi;
               escape.getChars(0, hi, buffer, ptr);
            } else {
               this._writer.write(escape);
            }

            return ptr;
         }
      }
   }

   private final void _appendCharacterEscape(char ch, int escCode) throws IOException, JsonGenerationException {
      if (escCode >= 0) {
         if (this._outputTail + 2 > this._outputEnd) {
            this._flushBuffer();
         }

         this._outputBuffer[this._outputTail++] = '\\';
         this._outputBuffer[this._outputTail++] = (char)escCode;
      } else if (escCode != -2) {
         if (this._outputTail + 2 > this._outputEnd) {
            this._flushBuffer();
         }

         int ptr = this._outputTail;
         char[] buf = this._outputBuffer;
         buf[ptr++] = '\\';
         buf[ptr++] = 'u';
         if (ch > 255) {
            int hi = ch >> 8 & 255;
            buf[ptr++] = HEX_CHARS[hi >> 4];
            buf[ptr++] = HEX_CHARS[hi & 15];
            ch = (char)(ch & 255);
         } else {
            buf[ptr++] = '0';
            buf[ptr++] = '0';
         }

         buf[ptr++] = HEX_CHARS[ch >> 4];
         buf[ptr] = HEX_CHARS[ch & 15];
         this._outputTail = ptr;
      } else {
         String escape;
         if (this._currentEscape == null) {
            escape = this._characterEscapes.getEscapeSequence(ch).getValue();
         } else {
            escape = this._currentEscape.getValue();
            this._currentEscape = null;
         }

         int len = escape.length();
         if (this._outputTail + len > this._outputEnd) {
            this._flushBuffer();
            if (len > this._outputEnd) {
               this._writer.write(escape);
               return;
            }
         }

         escape.getChars(0, len, this._outputBuffer, this._outputTail);
         this._outputTail += len;
      }
   }

   private char[] _allocateEntityBuffer() {
      char[] buf = new char[14];
      buf[0] = '\\';
      buf[2] = '\\';
      buf[3] = 'u';
      buf[4] = '0';
      buf[5] = '0';
      buf[8] = '\\';
      buf[9] = 'u';
      this._entityBuffer = buf;
      return buf;
   }

   protected final void _flushBuffer() throws IOException {
      int len = this._outputTail - this._outputHead;
      if (len > 0) {
         int offset = this._outputHead;
         this._outputTail = this._outputHead = 0;
         this._writer.write(this._outputBuffer, offset, len);
      }

   }
}
