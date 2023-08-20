package org.codehaus.jackson.impl;

import java.io.IOException;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.io.NumberInput;

public abstract class JsonParserMinimalBase extends JsonParser {
   protected static final int INT_TAB = 9;
   protected static final int INT_LF = 10;
   protected static final int INT_CR = 13;
   protected static final int INT_SPACE = 32;
   protected static final int INT_LBRACKET = 91;
   protected static final int INT_RBRACKET = 93;
   protected static final int INT_LCURLY = 123;
   protected static final int INT_RCURLY = 125;
   protected static final int INT_QUOTE = 34;
   protected static final int INT_BACKSLASH = 92;
   protected static final int INT_SLASH = 47;
   protected static final int INT_COLON = 58;
   protected static final int INT_COMMA = 44;
   protected static final int INT_ASTERISK = 42;
   protected static final int INT_APOSTROPHE = 39;
   protected static final int INT_b = 98;
   protected static final int INT_f = 102;
   protected static final int INT_n = 110;
   protected static final int INT_r = 114;
   protected static final int INT_t = 116;
   protected static final int INT_u = 117;

   protected JsonParserMinimalBase() {
   }

   protected JsonParserMinimalBase(int features) {
      super(features);
   }

   public abstract JsonToken nextToken() throws IOException, JsonParseException;

   public JsonParser skipChildren() throws IOException, JsonParseException {
      if (this._currToken != JsonToken.START_OBJECT && this._currToken != JsonToken.START_ARRAY) {
         return this;
      } else {
         int open = 1;

         while(true) {
            JsonToken t = this.nextToken();
            if (t == null) {
               this._handleEOF();
               return this;
            }

            switch(t) {
            case START_OBJECT:
            case START_ARRAY:
               ++open;
               break;
            case END_OBJECT:
            case END_ARRAY:
               --open;
               if (open == 0) {
                  return this;
               }
            }
         }
      }
   }

   protected abstract void _handleEOF() throws JsonParseException;

   public abstract String getCurrentName() throws IOException, JsonParseException;

   public abstract void close() throws IOException;

   public abstract boolean isClosed();

   public abstract JsonStreamContext getParsingContext();

   public abstract String getText() throws IOException, JsonParseException;

   public abstract char[] getTextCharacters() throws IOException, JsonParseException;

   public abstract boolean hasTextCharacters();

   public abstract int getTextLength() throws IOException, JsonParseException;

   public abstract int getTextOffset() throws IOException, JsonParseException;

   public abstract byte[] getBinaryValue(Base64Variant var1) throws IOException, JsonParseException;

   public boolean getValueAsBoolean(boolean defaultValue) throws IOException, JsonParseException {
      if (this._currToken != null) {
         switch(this._currToken) {
         case VALUE_NUMBER_INT:
            return this.getIntValue() != 0;
         case VALUE_TRUE:
            return true;
         case VALUE_FALSE:
         case VALUE_NULL:
            return false;
         case VALUE_EMBEDDED_OBJECT:
            Object value = this.getEmbeddedObject();
            if (value instanceof Boolean) {
               return (Boolean)value;
            }
         case VALUE_STRING:
            String str = this.getText().trim();
            if ("true".equals(str)) {
               return true;
            }
         }
      }

      return defaultValue;
   }

   public int getValueAsInt(int defaultValue) throws IOException, JsonParseException {
      if (this._currToken != null) {
         switch(this._currToken) {
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return this.getIntValue();
         case VALUE_TRUE:
            return 1;
         case VALUE_FALSE:
         case VALUE_NULL:
            return 0;
         case VALUE_EMBEDDED_OBJECT:
            Object value = this.getEmbeddedObject();
            if (value instanceof Number) {
               return ((Number)value).intValue();
            }
            break;
         case VALUE_STRING:
            return NumberInput.parseAsInt(this.getText(), defaultValue);
         }
      }

      return defaultValue;
   }

   public long getValueAsLong(long defaultValue) throws IOException, JsonParseException {
      if (this._currToken != null) {
         switch(this._currToken) {
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return this.getLongValue();
         case VALUE_TRUE:
            return 1L;
         case VALUE_FALSE:
         case VALUE_NULL:
            return 0L;
         case VALUE_EMBEDDED_OBJECT:
            Object value = this.getEmbeddedObject();
            if (value instanceof Number) {
               return ((Number)value).longValue();
            }
            break;
         case VALUE_STRING:
            return NumberInput.parseAsLong(this.getText(), defaultValue);
         }
      }

      return defaultValue;
   }

   public double getValueAsDouble(double defaultValue) throws IOException, JsonParseException {
      if (this._currToken != null) {
         switch(this._currToken) {
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return this.getDoubleValue();
         case VALUE_TRUE:
            return 1.0D;
         case VALUE_FALSE:
         case VALUE_NULL:
            return 0.0D;
         case VALUE_EMBEDDED_OBJECT:
            Object value = this.getEmbeddedObject();
            if (value instanceof Number) {
               return ((Number)value).doubleValue();
            }
            break;
         case VALUE_STRING:
            return NumberInput.parseAsDouble(this.getText(), defaultValue);
         }
      }

      return defaultValue;
   }

   protected void _reportUnexpectedChar(int ch, String comment) throws JsonParseException {
      String msg = "Unexpected character (" + _getCharDesc(ch) + ")";
      if (comment != null) {
         msg = msg + ": " + comment;
      }

      this._reportError(msg);
   }

   protected void _reportInvalidEOF() throws JsonParseException {
      this._reportInvalidEOF(" in " + this._currToken);
   }

   protected void _reportInvalidEOF(String msg) throws JsonParseException {
      this._reportError("Unexpected end-of-input" + msg);
   }

   protected void _reportInvalidEOFInValue() throws JsonParseException {
      this._reportInvalidEOF(" in a value");
   }

   protected void _throwInvalidSpace(int i) throws JsonParseException {
      char c = (char)i;
      String msg = "Illegal character (" + _getCharDesc(c) + "): only regular white space (\\r, \\n, \\t) is allowed between tokens";
      this._reportError(msg);
   }

   protected void _throwUnquotedSpace(int i, String ctxtDesc) throws JsonParseException {
      if (!this.isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS) || i >= 32) {
         char c = (char)i;
         String msg = "Illegal unquoted character (" + _getCharDesc(c) + "): has to be escaped using backslash to be included in " + ctxtDesc;
         this._reportError(msg);
      }

   }

   protected char _handleUnrecognizedCharacterEscape(char ch) throws JsonProcessingException {
      if (this.isEnabled(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)) {
         return ch;
      } else if (ch == '\'' && this.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES)) {
         return ch;
      } else {
         this._reportError("Unrecognized character escape " + _getCharDesc(ch));
         return ch;
      }
   }

   protected static final String _getCharDesc(int ch) {
      char c = (char)ch;
      if (Character.isISOControl(c)) {
         return "(CTRL-CHAR, code " + ch + ")";
      } else {
         return ch > 255 ? "'" + c + "' (code " + ch + " / 0x" + Integer.toHexString(ch) + ")" : "'" + c + "' (code " + ch + ")";
      }
   }

   protected final void _reportError(String msg) throws JsonParseException {
      throw this._constructError(msg);
   }

   protected final void _wrapError(String msg, Throwable t) throws JsonParseException {
      throw this._constructError(msg, t);
   }

   protected final void _throwInternal() {
      throw new RuntimeException("Internal error: this code path should never get executed");
   }

   protected final JsonParseException _constructError(String msg, Throwable t) {
      return new JsonParseException(msg, this.getCurrentLocation(), t);
   }
}
