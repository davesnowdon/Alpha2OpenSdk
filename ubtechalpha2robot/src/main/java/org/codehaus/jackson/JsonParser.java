package org.codehaus.jackson;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.type.TypeReference;

public abstract class JsonParser implements Closeable, Versioned {
   private static final int MIN_BYTE_I = -128;
   private static final int MAX_BYTE_I = 127;
   private static final int MIN_SHORT_I = -32768;
   private static final int MAX_SHORT_I = 32767;
   protected int _features;
   protected JsonToken _currToken;
   protected JsonToken _lastClearedToken;

   protected JsonParser() {
   }

   protected JsonParser(int features) {
      this._features = features;
   }

   public abstract ObjectCodec getCodec();

   public abstract void setCodec(ObjectCodec var1);

   public void setSchema(FormatSchema schema) {
      throw new UnsupportedOperationException("Parser of type " + this.getClass().getName() + " does not support schema of type '" + schema.getSchemaType() + "'");
   }

   public boolean canUseSchema(FormatSchema schema) {
      return false;
   }

   public Version version() {
      return Version.unknownVersion();
   }

   public Object getInputSource() {
      return null;
   }

   public abstract void close() throws IOException;

   public int releaseBuffered(OutputStream out) throws IOException {
      return -1;
   }

   public int releaseBuffered(Writer w) throws IOException {
      return -1;
   }

   public JsonParser enable(JsonParser.Feature f) {
      this._features |= f.getMask();
      return this;
   }

   public JsonParser disable(JsonParser.Feature f) {
      this._features &= ~f.getMask();
      return this;
   }

   public JsonParser configure(JsonParser.Feature f, boolean state) {
      if (state) {
         this.enableFeature(f);
      } else {
         this.disableFeature(f);
      }

      return this;
   }

   public boolean isEnabled(JsonParser.Feature f) {
      return (this._features & f.getMask()) != 0;
   }

   /** @deprecated */
   public void setFeature(JsonParser.Feature f, boolean state) {
      this.configure(f, state);
   }

   /** @deprecated */
   public void enableFeature(JsonParser.Feature f) {
      this.enable(f);
   }

   /** @deprecated */
   public void disableFeature(JsonParser.Feature f) {
      this.disable(f);
   }

   /** @deprecated */
   public final boolean isFeatureEnabled(JsonParser.Feature f) {
      return this.isEnabled(f);
   }

   public abstract JsonToken nextToken() throws IOException, JsonParseException;

   public JsonToken nextValue() throws IOException, JsonParseException {
      JsonToken t = this.nextToken();
      if (t == JsonToken.FIELD_NAME) {
         t = this.nextToken();
      }

      return t;
   }

   public abstract JsonParser skipChildren() throws IOException, JsonParseException;

   public abstract boolean isClosed();

   public JsonToken getCurrentToken() {
      return this._currToken;
   }

   public boolean hasCurrentToken() {
      return this._currToken != null;
   }

   public void clearCurrentToken() {
      if (this._currToken != null) {
         this._lastClearedToken = this._currToken;
         this._currToken = null;
      }

   }

   public abstract String getCurrentName() throws IOException, JsonParseException;

   public abstract JsonStreamContext getParsingContext();

   public abstract JsonLocation getTokenLocation();

   public abstract JsonLocation getCurrentLocation();

   public JsonToken getLastClearedToken() {
      return this._lastClearedToken;
   }

   public boolean isExpectedStartArrayToken() {
      return this.getCurrentToken() == JsonToken.START_ARRAY;
   }

   public abstract String getText() throws IOException, JsonParseException;

   public abstract char[] getTextCharacters() throws IOException, JsonParseException;

   public abstract int getTextLength() throws IOException, JsonParseException;

   public abstract int getTextOffset() throws IOException, JsonParseException;

   public boolean hasTextCharacters() {
      return false;
   }

   public abstract Number getNumberValue() throws IOException, JsonParseException;

   public abstract JsonParser.NumberType getNumberType() throws IOException, JsonParseException;

   public byte getByteValue() throws IOException, JsonParseException {
      int value = this.getIntValue();
      if (value >= -128 && value <= 127) {
         return (byte)value;
      } else {
         throw this._constructError("Numeric value (" + this.getText() + ") out of range of Java byte");
      }
   }

   public short getShortValue() throws IOException, JsonParseException {
      int value = this.getIntValue();
      if (value >= -32768 && value <= 32767) {
         return (short)value;
      } else {
         throw this._constructError("Numeric value (" + this.getText() + ") out of range of Java short");
      }
   }

   public abstract int getIntValue() throws IOException, JsonParseException;

   public abstract long getLongValue() throws IOException, JsonParseException;

   public abstract BigInteger getBigIntegerValue() throws IOException, JsonParseException;

   public abstract float getFloatValue() throws IOException, JsonParseException;

   public abstract double getDoubleValue() throws IOException, JsonParseException;

   public abstract BigDecimal getDecimalValue() throws IOException, JsonParseException;

   public boolean getBooleanValue() throws IOException, JsonParseException {
      if (this._currToken == JsonToken.VALUE_TRUE) {
         return true;
      } else if (this._currToken == JsonToken.VALUE_FALSE) {
         return false;
      } else {
         throw new JsonParseException("Current token (" + this._currToken + ") not of boolean type", this.getCurrentLocation());
      }
   }

   public Object getEmbeddedObject() throws IOException, JsonParseException {
      return null;
   }

   public abstract byte[] getBinaryValue(Base64Variant var1) throws IOException, JsonParseException;

   public byte[] getBinaryValue() throws IOException, JsonParseException {
      return this.getBinaryValue(Base64Variants.getDefaultVariant());
   }

   public int getValueAsInt() throws IOException, JsonParseException {
      return this.getValueAsInt(0);
   }

   public int getValueAsInt(int defaultValue) throws IOException, JsonParseException {
      return defaultValue;
   }

   public long getValueAsLong() throws IOException, JsonParseException {
      return (long)this.getValueAsInt(0);
   }

   public long getValueAsLong(long defaultValue) throws IOException, JsonParseException {
      return defaultValue;
   }

   public double getValueAsDouble() throws IOException, JsonParseException {
      return this.getValueAsDouble(0.0D);
   }

   public double getValueAsDouble(double defaultValue) throws IOException, JsonParseException {
      return defaultValue;
   }

   public boolean getValueAsBoolean() throws IOException, JsonParseException {
      return this.getValueAsBoolean(false);
   }

   public boolean getValueAsBoolean(boolean defaultValue) throws IOException, JsonParseException {
      return defaultValue;
   }

   public Object readValueAs(Class valueType) throws IOException, JsonProcessingException {
      ObjectCodec codec = this.getCodec();
      if (codec == null) {
         throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into Java objects");
      } else {
         return codec.readValue(this, valueType);
      }
   }

   public Object readValueAs(TypeReference<?> valueTypeRef) throws IOException, JsonProcessingException {
      ObjectCodec codec = this.getCodec();
      if (codec == null) {
         throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into Java objects");
      } else {
         return codec.readValue(this, valueTypeRef);
      }
   }

   public JsonNode readValueAsTree() throws IOException, JsonProcessingException {
      ObjectCodec codec = this.getCodec();
      if (codec == null) {
         throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into JsonNode tree");
      } else {
         return codec.readTree(this);
      }
   }

   protected JsonParseException _constructError(String msg) {
      return new JsonParseException(msg, this.getCurrentLocation());
   }

   public static enum Feature {
      AUTO_CLOSE_SOURCE(true),
      ALLOW_COMMENTS(false),
      ALLOW_UNQUOTED_FIELD_NAMES(false),
      ALLOW_SINGLE_QUOTES(false),
      ALLOW_UNQUOTED_CONTROL_CHARS(false),
      ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER(false),
      ALLOW_NUMERIC_LEADING_ZEROS(false),
      ALLOW_NON_NUMERIC_NUMBERS(false),
      INTERN_FIELD_NAMES(true),
      CANONICALIZE_FIELD_NAMES(true);

      final boolean _defaultState;

      public static int collectDefaults() {
         int flags = 0;
         JsonParser.Feature[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            JsonParser.Feature f = arr$[i$];
            if (f.enabledByDefault()) {
               flags |= f.getMask();
            }
         }

         return flags;
      }

      private Feature(boolean defaultState) {
         this._defaultState = defaultState;
      }

      public boolean enabledByDefault() {
         return this._defaultState;
      }

      public boolean enabledIn(int flags) {
         return (flags & this.getMask()) != 0;
      }

      public int getMask() {
         return 1 << this.ordinal();
      }
   }

   public static enum NumberType {
      INT,
      LONG,
      BIG_INTEGER,
      FLOAT,
      DOUBLE,
      BIG_DECIMAL;

      private NumberType() {
      }
   }
}
