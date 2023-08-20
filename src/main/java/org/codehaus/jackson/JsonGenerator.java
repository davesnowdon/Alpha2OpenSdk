package org.codehaus.jackson;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.io.CharacterEscapes;
import org.codehaus.jackson.io.SerializedString;

public abstract class JsonGenerator implements Closeable, Versioned {
   protected PrettyPrinter _cfgPrettyPrinter;

   protected JsonGenerator() {
   }

   public void setSchema(FormatSchema schema) {
      throw new UnsupportedOperationException("Generator of type " + this.getClass().getName() + " does not support schema of type '" + schema.getSchemaType() + "'");
   }

   public boolean canUseSchema(FormatSchema schema) {
      return false;
   }

   public Version version() {
      return Version.unknownVersion();
   }

   public Object getOutputTarget() {
      return null;
   }

   public abstract JsonGenerator enable(JsonGenerator.Feature var1);

   public abstract JsonGenerator disable(JsonGenerator.Feature var1);

   public JsonGenerator configure(JsonGenerator.Feature f, boolean state) {
      if (state) {
         this.enable(f);
      } else {
         this.disable(f);
      }

      return this;
   }

   public abstract boolean isEnabled(JsonGenerator.Feature var1);

   public abstract JsonGenerator setCodec(ObjectCodec var1);

   public abstract ObjectCodec getCodec();

   /** @deprecated */
   @Deprecated
   public void enableFeature(JsonGenerator.Feature f) {
      this.enable(f);
   }

   /** @deprecated */
   @Deprecated
   public void disableFeature(JsonGenerator.Feature f) {
      this.disable(f);
   }

   /** @deprecated */
   @Deprecated
   public void setFeature(JsonGenerator.Feature f, boolean state) {
      this.configure(f, state);
   }

   /** @deprecated */
   @Deprecated
   public boolean isFeatureEnabled(JsonGenerator.Feature f) {
      return this.isEnabled(f);
   }

   public JsonGenerator setPrettyPrinter(PrettyPrinter pp) {
      this._cfgPrettyPrinter = pp;
      return this;
   }

   public abstract JsonGenerator useDefaultPrettyPrinter();

   public JsonGenerator setHighestNonEscapedChar(int charCode) {
      return this;
   }

   public int getHighestEscapedChar() {
      return 0;
   }

   public CharacterEscapes getCharacterEscapes() {
      return null;
   }

   public JsonGenerator setCharacterEscapes(CharacterEscapes esc) {
      return this;
   }

   public abstract void writeStartArray() throws IOException, JsonGenerationException;

   public abstract void writeEndArray() throws IOException, JsonGenerationException;

   public abstract void writeStartObject() throws IOException, JsonGenerationException;

   public abstract void writeEndObject() throws IOException, JsonGenerationException;

   public abstract void writeFieldName(String var1) throws IOException, JsonGenerationException;

   public void writeFieldName(SerializedString name) throws IOException, JsonGenerationException {
      this.writeFieldName(name.getValue());
   }

   public void writeFieldName(SerializableString name) throws IOException, JsonGenerationException {
      this.writeFieldName(name.getValue());
   }

   public abstract void writeString(String var1) throws IOException, JsonGenerationException;

   public abstract void writeString(char[] var1, int var2, int var3) throws IOException, JsonGenerationException;

   public void writeString(SerializableString text) throws IOException, JsonGenerationException {
      this.writeString(text.getValue());
   }

   public abstract void writeRawUTF8String(byte[] var1, int var2, int var3) throws IOException, JsonGenerationException;

   public abstract void writeUTF8String(byte[] var1, int var2, int var3) throws IOException, JsonGenerationException;

   public abstract void writeRaw(String var1) throws IOException, JsonGenerationException;

   public abstract void writeRaw(String var1, int var2, int var3) throws IOException, JsonGenerationException;

   public abstract void writeRaw(char[] var1, int var2, int var3) throws IOException, JsonGenerationException;

   public abstract void writeRaw(char var1) throws IOException, JsonGenerationException;

   public abstract void writeRawValue(String var1) throws IOException, JsonGenerationException;

   public abstract void writeRawValue(String var1, int var2, int var3) throws IOException, JsonGenerationException;

   public abstract void writeRawValue(char[] var1, int var2, int var3) throws IOException, JsonGenerationException;

   public abstract void writeBinary(Base64Variant var1, byte[] var2, int var3, int var4) throws IOException, JsonGenerationException;

   public void writeBinary(byte[] data, int offset, int len) throws IOException, JsonGenerationException {
      this.writeBinary(Base64Variants.getDefaultVariant(), data, offset, len);
   }

   public void writeBinary(byte[] data) throws IOException, JsonGenerationException {
      this.writeBinary(Base64Variants.getDefaultVariant(), data, 0, data.length);
   }

   public abstract void writeNumber(int var1) throws IOException, JsonGenerationException;

   public abstract void writeNumber(long var1) throws IOException, JsonGenerationException;

   public abstract void writeNumber(BigInteger var1) throws IOException, JsonGenerationException;

   public abstract void writeNumber(double var1) throws IOException, JsonGenerationException;

   public abstract void writeNumber(float var1) throws IOException, JsonGenerationException;

   public abstract void writeNumber(BigDecimal var1) throws IOException, JsonGenerationException;

   public abstract void writeNumber(String var1) throws IOException, JsonGenerationException, UnsupportedOperationException;

   public abstract void writeBoolean(boolean var1) throws IOException, JsonGenerationException;

   public abstract void writeNull() throws IOException, JsonGenerationException;

   public abstract void writeObject(Object var1) throws IOException, JsonProcessingException;

   public abstract void writeTree(JsonNode var1) throws IOException, JsonProcessingException;

   public void writeStringField(String fieldName, String value) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeString(value);
   }

   public final void writeBooleanField(String fieldName, boolean value) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeBoolean(value);
   }

   public final void writeNullField(String fieldName) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeNull();
   }

   public final void writeNumberField(String fieldName, int value) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeNumber(value);
   }

   public final void writeNumberField(String fieldName, long value) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeNumber(value);
   }

   public final void writeNumberField(String fieldName, double value) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeNumber(value);
   }

   public final void writeNumberField(String fieldName, float value) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeNumber(value);
   }

   public final void writeNumberField(String fieldName, BigDecimal value) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeNumber(value);
   }

   public final void writeBinaryField(String fieldName, byte[] data) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeBinary(data);
   }

   public final void writeArrayFieldStart(String fieldName) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeStartArray();
   }

   public final void writeObjectFieldStart(String fieldName) throws IOException, JsonGenerationException {
      this.writeFieldName(fieldName);
      this.writeStartObject();
   }

   public final void writeObjectField(String fieldName, Object pojo) throws IOException, JsonProcessingException {
      this.writeFieldName(fieldName);
      this.writeObject(pojo);
   }

   public abstract void copyCurrentEvent(JsonParser var1) throws IOException, JsonProcessingException;

   public abstract void copyCurrentStructure(JsonParser var1) throws IOException, JsonProcessingException;

   public abstract JsonStreamContext getOutputContext();

   public abstract void flush() throws IOException;

   public abstract boolean isClosed();

   public abstract void close() throws IOException;

   public static enum Feature {
      AUTO_CLOSE_TARGET(true),
      AUTO_CLOSE_JSON_CONTENT(true),
      QUOTE_FIELD_NAMES(true),
      QUOTE_NON_NUMERIC_NUMBERS(true),
      WRITE_NUMBERS_AS_STRINGS(false),
      FLUSH_PASSED_TO_STREAM(true),
      ESCAPE_NON_ASCII(false);

      final boolean _defaultState;
      final int _mask;

      public static int collectDefaults() {
         int flags = 0;
         JsonGenerator.Feature[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            JsonGenerator.Feature f = arr$[i$];
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
