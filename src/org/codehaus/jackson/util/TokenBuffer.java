package org.codehaus.jackson.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.SerializableString;
import org.codehaus.jackson.impl.JsonParserMinimalBase;
import org.codehaus.jackson.impl.JsonReadContext;
import org.codehaus.jackson.impl.JsonWriteContext;
import org.codehaus.jackson.io.SerializedString;

public class TokenBuffer extends JsonGenerator {
   protected static final int DEFAULT_PARSER_FEATURES = JsonParser.Feature.collectDefaults();
   protected ObjectCodec _objectCodec;
   protected int _generatorFeatures;
   protected boolean _closed;
   protected TokenBuffer.Segment _first;
   protected TokenBuffer.Segment _last;
   protected int _appendOffset;
   protected JsonWriteContext _writeContext;

   public TokenBuffer(ObjectCodec codec) {
      this._objectCodec = codec;
      this._generatorFeatures = DEFAULT_PARSER_FEATURES;
      this._writeContext = JsonWriteContext.createRootContext();
      this._first = this._last = new TokenBuffer.Segment();
      this._appendOffset = 0;
   }

   public JsonParser asParser() {
      return this.asParser(this._objectCodec);
   }

   public JsonParser asParser(ObjectCodec codec) {
      return new TokenBuffer.Parser(this._first, codec);
   }

   public JsonParser asParser(JsonParser src) {
      TokenBuffer.Parser p = new TokenBuffer.Parser(this._first, src.getCodec());
      p.setLocation(src.getTokenLocation());
      return p;
   }

   public void serialize(JsonGenerator jgen) throws IOException, JsonGenerationException {
      TokenBuffer.Segment segment = this._first;
      int ptr = -1;

      while(true) {
         ++ptr;
         if (ptr >= 16) {
            ptr = 0;
            segment = segment.next();
            if (segment == null) {
               break;
            }
         }

         JsonToken t = segment.type(ptr);
         if (t == null) {
            break;
         }

         Object n;
         switch(t) {
         case START_OBJECT:
            jgen.writeStartObject();
            break;
         case END_OBJECT:
            jgen.writeEndObject();
            break;
         case START_ARRAY:
            jgen.writeStartArray();
            break;
         case END_ARRAY:
            jgen.writeEndArray();
            break;
         case FIELD_NAME:
            n = segment.get(ptr);
            if (n instanceof SerializableString) {
               jgen.writeFieldName((SerializableString)n);
            } else {
               jgen.writeFieldName((String)n);
            }
            break;
         case VALUE_STRING:
            n = segment.get(ptr);
            if (n instanceof SerializableString) {
               jgen.writeString((SerializableString)n);
            } else {
               jgen.writeString((String)n);
            }
            break;
         case VALUE_NUMBER_INT:
            Number n = (Number)segment.get(ptr);
            if (n instanceof BigInteger) {
               jgen.writeNumber((BigInteger)n);
            } else if (n instanceof Long) {
               jgen.writeNumber(n.longValue());
            } else {
               jgen.writeNumber(n.intValue());
            }
            break;
         case VALUE_NUMBER_FLOAT:
            n = segment.get(ptr);
            if (n instanceof BigDecimal) {
               jgen.writeNumber((BigDecimal)n);
            } else if (n instanceof Float) {
               jgen.writeNumber((Float)n);
            } else if (n instanceof Double) {
               jgen.writeNumber((Double)n);
            } else if (n == null) {
               jgen.writeNull();
            } else {
               if (!(n instanceof String)) {
                  throw new JsonGenerationException("Unrecognized value type for VALUE_NUMBER_FLOAT: " + n.getClass().getName() + ", can not serialize");
               }

               jgen.writeNumber((String)n);
            }
            break;
         case VALUE_TRUE:
            jgen.writeBoolean(true);
            break;
         case VALUE_FALSE:
            jgen.writeBoolean(false);
            break;
         case VALUE_NULL:
            jgen.writeNull();
            break;
         case VALUE_EMBEDDED_OBJECT:
            jgen.writeObject(segment.get(ptr));
            break;
         default:
            throw new RuntimeException("Internal error: should never end up through this code path");
         }
      }

   }

   public String toString() {
      int MAX_COUNT = true;
      StringBuilder sb = new StringBuilder();
      sb.append("[TokenBuffer: ");
      JsonParser jp = this.asParser();
      int count = 0;

      while(true) {
         JsonToken t;
         try {
            t = jp.nextToken();
         } catch (IOException var7) {
            throw new IllegalStateException(var7);
         }

         if (t == null) {
            if (count >= 100) {
               sb.append(" ... (truncated ").append(count - 100).append(" entries)");
            }

            sb.append(']');
            return sb.toString();
         }

         if (count < 100) {
            if (count > 0) {
               sb.append(", ");
            }

            sb.append(t.toString());
         }

         ++count;
      }
   }

   public JsonGenerator enable(JsonGenerator.Feature f) {
      this._generatorFeatures |= f.getMask();
      return this;
   }

   public JsonGenerator disable(JsonGenerator.Feature f) {
      this._generatorFeatures &= ~f.getMask();
      return this;
   }

   public boolean isEnabled(JsonGenerator.Feature f) {
      return (this._generatorFeatures & f.getMask()) != 0;
   }

   public JsonGenerator useDefaultPrettyPrinter() {
      return this;
   }

   public JsonGenerator setCodec(ObjectCodec oc) {
      this._objectCodec = oc;
      return this;
   }

   public ObjectCodec getCodec() {
      return this._objectCodec;
   }

   public final JsonWriteContext getOutputContext() {
      return this._writeContext;
   }

   public void flush() throws IOException {
   }

   public void close() throws IOException {
      this._closed = true;
   }

   public boolean isClosed() {
      return this._closed;
   }

   public final void writeStartArray() throws IOException, JsonGenerationException {
      this._append(JsonToken.START_ARRAY);
      this._writeContext = this._writeContext.createChildArrayContext();
   }

   public final void writeEndArray() throws IOException, JsonGenerationException {
      this._append(JsonToken.END_ARRAY);
      JsonWriteContext c = this._writeContext.getParent();
      if (c != null) {
         this._writeContext = c;
      }

   }

   public final void writeStartObject() throws IOException, JsonGenerationException {
      this._append(JsonToken.START_OBJECT);
      this._writeContext = this._writeContext.createChildObjectContext();
   }

   public final void writeEndObject() throws IOException, JsonGenerationException {
      this._append(JsonToken.END_OBJECT);
      JsonWriteContext c = this._writeContext.getParent();
      if (c != null) {
         this._writeContext = c;
      }

   }

   public final void writeFieldName(String name) throws IOException, JsonGenerationException {
      this._append(JsonToken.FIELD_NAME, name);
      this._writeContext.writeFieldName(name);
   }

   public void writeFieldName(SerializableString name) throws IOException, JsonGenerationException {
      this._append(JsonToken.FIELD_NAME, name);
      this._writeContext.writeFieldName(name.getValue());
   }

   public void writeFieldName(SerializedString name) throws IOException, JsonGenerationException {
      this._append(JsonToken.FIELD_NAME, name);
      this._writeContext.writeFieldName(name.getValue());
   }

   public void writeString(String text) throws IOException, JsonGenerationException {
      if (text == null) {
         this.writeNull();
      } else {
         this._append(JsonToken.VALUE_STRING, text);
      }

   }

   public void writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      this.writeString(new String(text, offset, len));
   }

   public void writeString(SerializableString text) throws IOException, JsonGenerationException {
      if (text == null) {
         this.writeNull();
      } else {
         this._append(JsonToken.VALUE_STRING, text);
      }

   }

   public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeRaw(String text) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeRaw(String text, int offset, int len) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeRaw(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeRaw(char c) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeRawValue(String text) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeRawValue(String text, int offset, int len) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeRawValue(char[] text, int offset, int len) throws IOException, JsonGenerationException {
      this._reportUnsupportedOperation();
   }

   public void writeNumber(int i) throws IOException, JsonGenerationException {
      this._append(JsonToken.VALUE_NUMBER_INT, i);
   }

   public void writeNumber(long l) throws IOException, JsonGenerationException {
      this._append(JsonToken.VALUE_NUMBER_INT, l);
   }

   public void writeNumber(double d) throws IOException, JsonGenerationException {
      this._append(JsonToken.VALUE_NUMBER_FLOAT, d);
   }

   public void writeNumber(float f) throws IOException, JsonGenerationException {
      this._append(JsonToken.VALUE_NUMBER_FLOAT, f);
   }

   public void writeNumber(BigDecimal dec) throws IOException, JsonGenerationException {
      if (dec == null) {
         this.writeNull();
      } else {
         this._append(JsonToken.VALUE_NUMBER_FLOAT, dec);
      }

   }

   public void writeNumber(BigInteger v) throws IOException, JsonGenerationException {
      if (v == null) {
         this.writeNull();
      } else {
         this._append(JsonToken.VALUE_NUMBER_INT, v);
      }

   }

   public void writeNumber(String encodedValue) throws IOException, JsonGenerationException {
      this._append(JsonToken.VALUE_NUMBER_FLOAT, encodedValue);
   }

   public void writeBoolean(boolean state) throws IOException, JsonGenerationException {
      this._append(state ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE);
   }

   public void writeNull() throws IOException, JsonGenerationException {
      this._append(JsonToken.VALUE_NULL);
   }

   public void writeObject(Object value) throws IOException, JsonProcessingException {
      this._append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
   }

   public void writeTree(JsonNode rootNode) throws IOException, JsonProcessingException {
      this._append(JsonToken.VALUE_EMBEDDED_OBJECT, rootNode);
   }

   public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException, JsonGenerationException {
      byte[] copy = new byte[len];
      System.arraycopy(data, offset, copy, 0, len);
      this.writeObject(copy);
   }

   public void copyCurrentEvent(JsonParser jp) throws IOException, JsonProcessingException {
      switch(jp.getCurrentToken()) {
      case START_OBJECT:
         this.writeStartObject();
         break;
      case END_OBJECT:
         this.writeEndObject();
         break;
      case START_ARRAY:
         this.writeStartArray();
         break;
      case END_ARRAY:
         this.writeEndArray();
         break;
      case FIELD_NAME:
         this.writeFieldName(jp.getCurrentName());
         break;
      case VALUE_STRING:
         if (jp.hasTextCharacters()) {
            this.writeString(jp.getTextCharacters(), jp.getTextOffset(), jp.getTextLength());
         } else {
            this.writeString(jp.getText());
         }
         break;
      case VALUE_NUMBER_INT:
         switch(jp.getNumberType()) {
         case INT:
            this.writeNumber(jp.getIntValue());
            return;
         case BIG_INTEGER:
            this.writeNumber(jp.getBigIntegerValue());
            return;
         default:
            this.writeNumber(jp.getLongValue());
            return;
         }
      case VALUE_NUMBER_FLOAT:
         switch(jp.getNumberType()) {
         case BIG_DECIMAL:
            this.writeNumber(jp.getDecimalValue());
            return;
         case FLOAT:
            this.writeNumber(jp.getFloatValue());
            return;
         default:
            this.writeNumber(jp.getDoubleValue());
            return;
         }
      case VALUE_TRUE:
         this.writeBoolean(true);
         break;
      case VALUE_FALSE:
         this.writeBoolean(false);
         break;
      case VALUE_NULL:
         this.writeNull();
         break;
      case VALUE_EMBEDDED_OBJECT:
         this.writeObject(jp.getEmbeddedObject());
         break;
      default:
         throw new RuntimeException("Internal error: should never end up through this code path");
      }

   }

   public void copyCurrentStructure(JsonParser jp) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.FIELD_NAME) {
         this.writeFieldName(jp.getCurrentName());
         t = jp.nextToken();
      }

      switch(t) {
      case START_OBJECT:
         this.writeStartObject();

         while(jp.nextToken() != JsonToken.END_OBJECT) {
            this.copyCurrentStructure(jp);
         }

         this.writeEndObject();
         break;
      case START_ARRAY:
         this.writeStartArray();

         while(jp.nextToken() != JsonToken.END_ARRAY) {
            this.copyCurrentStructure(jp);
         }

         this.writeEndArray();
         break;
      default:
         this.copyCurrentEvent(jp);
      }

   }

   protected final void _append(JsonToken type) {
      TokenBuffer.Segment next = this._last.append(this._appendOffset, type);
      if (next == null) {
         ++this._appendOffset;
      } else {
         this._last = next;
         this._appendOffset = 1;
      }

   }

   protected final void _append(JsonToken type, Object value) {
      TokenBuffer.Segment next = this._last.append(this._appendOffset, type, value);
      if (next == null) {
         ++this._appendOffset;
      } else {
         this._last = next;
         this._appendOffset = 1;
      }

   }

   protected void _reportUnsupportedOperation() {
      throw new UnsupportedOperationException("Called operation not supported for TokenBuffer");
   }

   protected static final class Segment {
      public static final int TOKENS_PER_SEGMENT = 16;
      private static final JsonToken[] TOKEN_TYPES_BY_INDEX = new JsonToken[16];
      protected TokenBuffer.Segment _next;
      protected long _tokenTypes;
      protected final Object[] _tokens = new Object[16];

      public Segment() {
      }

      public JsonToken type(int index) {
         long l = this._tokenTypes;
         if (index > 0) {
            l >>= index << 2;
         }

         int ix = (int)l & 15;
         return TOKEN_TYPES_BY_INDEX[ix];
      }

      public Object get(int index) {
         return this._tokens[index];
      }

      public TokenBuffer.Segment next() {
         return this._next;
      }

      public TokenBuffer.Segment append(int index, JsonToken tokenType) {
         if (index < 16) {
            this.set(index, tokenType);
            return null;
         } else {
            this._next = new TokenBuffer.Segment();
            this._next.set(0, tokenType);
            return this._next;
         }
      }

      public TokenBuffer.Segment append(int index, JsonToken tokenType, Object value) {
         if (index < 16) {
            this.set(index, tokenType, value);
            return null;
         } else {
            this._next = new TokenBuffer.Segment();
            this._next.set(0, tokenType, value);
            return this._next;
         }
      }

      public void set(int index, JsonToken tokenType) {
         long typeCode = (long)tokenType.ordinal();
         if (index > 0) {
            typeCode <<= index << 2;
         }

         this._tokenTypes |= typeCode;
      }

      public void set(int index, JsonToken tokenType, Object value) {
         this._tokens[index] = value;
         long typeCode = (long)tokenType.ordinal();
         if (index > 0) {
            typeCode <<= index << 2;
         }

         this._tokenTypes |= typeCode;
      }

      static {
         JsonToken[] t = JsonToken.values();
         System.arraycopy(t, 1, TOKEN_TYPES_BY_INDEX, 1, Math.min(15, t.length - 1));
      }
   }

   protected static final class Parser extends JsonParserMinimalBase {
      protected ObjectCodec _codec;
      protected TokenBuffer.Segment _segment;
      protected int _segmentPtr;
      protected JsonReadContext _parsingContext;
      protected boolean _closed;
      protected transient ByteArrayBuilder _byteBuilder;
      protected JsonLocation _location = null;

      public Parser(TokenBuffer.Segment firstSeg, ObjectCodec codec) {
         super(0);
         this._segment = firstSeg;
         this._segmentPtr = -1;
         this._codec = codec;
         this._parsingContext = JsonReadContext.createRootContext(-1, -1);
      }

      public void setLocation(JsonLocation l) {
         this._location = l;
      }

      public ObjectCodec getCodec() {
         return this._codec;
      }

      public void setCodec(ObjectCodec c) {
         this._codec = c;
      }

      public JsonToken peekNextToken() throws IOException, JsonParseException {
         if (this._closed) {
            return null;
         } else {
            TokenBuffer.Segment seg = this._segment;
            int ptr = this._segmentPtr + 1;
            if (ptr >= 16) {
               ptr = 0;
               seg = seg == null ? null : seg.next();
            }

            return seg == null ? null : seg.type(ptr);
         }
      }

      public void close() throws IOException {
         if (!this._closed) {
            this._closed = true;
         }

      }

      public JsonToken nextToken() throws IOException, JsonParseException {
         if (!this._closed && this._segment != null) {
            if (++this._segmentPtr >= 16) {
               this._segmentPtr = 0;
               this._segment = this._segment.next();
               if (this._segment == null) {
                  return null;
               }
            }

            this._currToken = this._segment.type(this._segmentPtr);
            if (this._currToken == JsonToken.FIELD_NAME) {
               Object ob = this._currentObject();
               String name = ob instanceof String ? (String)ob : ob.toString();
               this._parsingContext.setCurrentName(name);
            } else if (this._currToken == JsonToken.START_OBJECT) {
               this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
            } else if (this._currToken == JsonToken.START_ARRAY) {
               this._parsingContext = this._parsingContext.createChildArrayContext(-1, -1);
            } else if (this._currToken == JsonToken.END_OBJECT || this._currToken == JsonToken.END_ARRAY) {
               this._parsingContext = this._parsingContext.getParent();
               if (this._parsingContext == null) {
                  this._parsingContext = JsonReadContext.createRootContext(-1, -1);
               }
            }

            return this._currToken;
         } else {
            return null;
         }
      }

      public boolean isClosed() {
         return this._closed;
      }

      public JsonStreamContext getParsingContext() {
         return this._parsingContext;
      }

      public JsonLocation getTokenLocation() {
         return this.getCurrentLocation();
      }

      public JsonLocation getCurrentLocation() {
         return this._location == null ? JsonLocation.NA : this._location;
      }

      public String getCurrentName() {
         return this._parsingContext.getCurrentName();
      }

      public String getText() {
         Object ob;
         if (this._currToken != JsonToken.VALUE_STRING && this._currToken != JsonToken.FIELD_NAME) {
            if (this._currToken == null) {
               return null;
            } else {
               switch(this._currToken) {
               case VALUE_NUMBER_INT:
               case VALUE_NUMBER_FLOAT:
                  ob = this._currentObject();
                  return ob == null ? null : ob.toString();
               default:
                  return this._currToken.asString();
               }
            }
         } else {
            ob = this._currentObject();
            if (ob instanceof String) {
               return (String)ob;
            } else {
               return ob == null ? null : ob.toString();
            }
         }
      }

      public char[] getTextCharacters() {
         String str = this.getText();
         return str == null ? null : str.toCharArray();
      }

      public int getTextLength() {
         String str = this.getText();
         return str == null ? 0 : str.length();
      }

      public int getTextOffset() {
         return 0;
      }

      public boolean hasTextCharacters() {
         return false;
      }

      public BigInteger getBigIntegerValue() throws IOException, JsonParseException {
         Number n = this.getNumberValue();
         if (n instanceof BigInteger) {
            return (BigInteger)n;
         } else {
            switch(this.getNumberType()) {
            case BIG_DECIMAL:
               return ((BigDecimal)n).toBigInteger();
            default:
               return BigInteger.valueOf(n.longValue());
            }
         }
      }

      public BigDecimal getDecimalValue() throws IOException, JsonParseException {
         Number n = this.getNumberValue();
         if (n instanceof BigDecimal) {
            return (BigDecimal)n;
         } else {
            switch(this.getNumberType()) {
            case INT:
            case LONG:
               return BigDecimal.valueOf(n.longValue());
            case BIG_INTEGER:
               return new BigDecimal((BigInteger)n);
            case BIG_DECIMAL:
            case FLOAT:
            default:
               return BigDecimal.valueOf(n.doubleValue());
            }
         }
      }

      public double getDoubleValue() throws IOException, JsonParseException {
         return this.getNumberValue().doubleValue();
      }

      public float getFloatValue() throws IOException, JsonParseException {
         return this.getNumberValue().floatValue();
      }

      public int getIntValue() throws IOException, JsonParseException {
         return this._currToken == JsonToken.VALUE_NUMBER_INT ? ((Number)this._currentObject()).intValue() : this.getNumberValue().intValue();
      }

      public long getLongValue() throws IOException, JsonParseException {
         return this.getNumberValue().longValue();
      }

      public JsonParser.NumberType getNumberType() throws IOException, JsonParseException {
         Number n = this.getNumberValue();
         if (n instanceof Integer) {
            return JsonParser.NumberType.INT;
         } else if (n instanceof Long) {
            return JsonParser.NumberType.LONG;
         } else if (n instanceof Double) {
            return JsonParser.NumberType.DOUBLE;
         } else if (n instanceof BigDecimal) {
            return JsonParser.NumberType.BIG_DECIMAL;
         } else if (n instanceof Float) {
            return JsonParser.NumberType.FLOAT;
         } else {
            return n instanceof BigInteger ? JsonParser.NumberType.BIG_INTEGER : null;
         }
      }

      public final Number getNumberValue() throws IOException, JsonParseException {
         this._checkIsNumber();
         return (Number)this._currentObject();
      }

      public Object getEmbeddedObject() {
         return this._currToken == JsonToken.VALUE_EMBEDDED_OBJECT ? this._currentObject() : null;
      }

      public byte[] getBinaryValue(Base64Variant b64variant) throws IOException, JsonParseException {
         if (this._currToken == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object ob = this._currentObject();
            if (ob instanceof byte[]) {
               return (byte[])((byte[])ob);
            }
         }

         if (this._currToken != JsonToken.VALUE_STRING) {
            throw this._constructError("Current token (" + this._currToken + ") not VALUE_STRING (or VALUE_EMBEDDED_OBJECT with byte[]), can not access as binary");
         } else {
            String str = this.getText();
            if (str == null) {
               return null;
            } else {
               ByteArrayBuilder builder = this._byteBuilder;
               if (builder == null) {
                  this._byteBuilder = builder = new ByteArrayBuilder(100);
               }

               this._decodeBase64(str, builder, b64variant);
               return builder.toByteArray();
            }
         }
      }

      protected void _decodeBase64(String str, ByteArrayBuilder builder, Base64Variant b64variant) throws IOException, JsonParseException {
         int ptr = 0;
         int len = str.length();

         while(ptr < len) {
            char ch;
            do {
               ch = str.charAt(ptr++);
               if (ptr >= len) {
                  return;
               }
            } while(ch <= ' ');

            int bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
               this._reportInvalidBase64(b64variant, ch, 0, (String)null);
            }

            int decodedData = bits;
            if (ptr >= len) {
               this._reportBase64EOF();
            }

            ch = str.charAt(ptr++);
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
               this._reportInvalidBase64(b64variant, ch, 1, (String)null);
            }

            decodedData = decodedData << 6 | bits;
            if (ptr >= len) {
               this._reportBase64EOF();
            }

            ch = str.charAt(ptr++);
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
               if (bits != -2) {
                  this._reportInvalidBase64(b64variant, ch, 2, (String)null);
               }

               if (ptr >= len) {
                  this._reportBase64EOF();
               }

               ch = str.charAt(ptr++);
               if (!b64variant.usesPaddingChar(ch)) {
                  this._reportInvalidBase64(b64variant, ch, 3, "expected padding character '" + b64variant.getPaddingChar() + "'");
               }

               decodedData >>= 4;
               builder.append(decodedData);
            } else {
               decodedData = decodedData << 6 | bits;
               if (ptr >= len) {
                  this._reportBase64EOF();
               }

               ch = str.charAt(ptr++);
               bits = b64variant.decodeBase64Char(ch);
               if (bits < 0) {
                  if (bits != -2) {
                     this._reportInvalidBase64(b64variant, ch, 3, (String)null);
                  }

                  decodedData >>= 2;
                  builder.appendTwoBytes(decodedData);
               } else {
                  decodedData = decodedData << 6 | bits;
                  builder.appendThreeBytes(decodedData);
               }
            }
         }

      }

      protected final Object _currentObject() {
         return this._segment.get(this._segmentPtr);
      }

      protected final void _checkIsNumber() throws JsonParseException {
         if (this._currToken == null || !this._currToken.isNumeric()) {
            throw this._constructError("Current token (" + this._currToken + ") not numeric, can not use numeric value accessors");
         }
      }

      protected void _reportInvalidBase64(Base64Variant b64variant, char ch, int bindex, String msg) throws JsonParseException {
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

         throw this._constructError(base);
      }

      protected void _reportBase64EOF() throws JsonParseException {
         throw this._constructError("Unexpected end-of-String in base64 content");
      }

      protected void _handleEOF() throws JsonParseException {
         this._throwInternal();
      }
   }
}
