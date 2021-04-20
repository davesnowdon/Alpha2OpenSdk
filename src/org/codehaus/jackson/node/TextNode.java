package org.codehaus.jackson.node;

import java.io.IOException;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.io.NumberInput;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.util.CharTypes;

public final class TextNode extends ValueNode {
   static final int INT_SPACE = 32;
   static final TextNode EMPTY_STRING_NODE = new TextNode("");
   final String _value;

   public TextNode(String v) {
      this._value = v;
   }

   public static TextNode valueOf(String v) {
      if (v == null) {
         return null;
      } else {
         return v.length() == 0 ? EMPTY_STRING_NODE : new TextNode(v);
      }
   }

   public JsonToken asToken() {
      return JsonToken.VALUE_STRING;
   }

   public boolean isTextual() {
      return true;
   }

   public String getTextValue() {
      return this._value;
   }

   public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
      ByteArrayBuilder builder = new ByteArrayBuilder(100);
      String str = this._value;
      int ptr = 0;
      int len = str.length();

      while(ptr < len) {
         char ch;
         do {
            ch = str.charAt(ptr++);
            if (ptr >= len) {
               return builder.toByteArray();
            }
         } while(ch <= ' ');

         int bits = b64variant.decodeBase64Char(ch);
         if (bits < 0) {
            this._reportInvalidBase64(b64variant, ch, 0);
         }

         int decodedData = bits;
         if (ptr >= len) {
            this._reportBase64EOF();
         }

         ch = str.charAt(ptr++);
         bits = b64variant.decodeBase64Char(ch);
         if (bits < 0) {
            this._reportInvalidBase64(b64variant, ch, 1);
         }

         decodedData = decodedData << 6 | bits;
         if (ptr >= len) {
            this._reportBase64EOF();
         }

         ch = str.charAt(ptr++);
         bits = b64variant.decodeBase64Char(ch);
         if (bits < 0) {
            if (bits != -2) {
               this._reportInvalidBase64(b64variant, ch, 2);
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
                  this._reportInvalidBase64(b64variant, ch, 3);
               }

               decodedData >>= 2;
               builder.appendTwoBytes(decodedData);
            } else {
               decodedData = decodedData << 6 | bits;
               builder.appendThreeBytes(decodedData);
            }
         }
      }

      return builder.toByteArray();
   }

   public byte[] getBinaryValue() throws IOException {
      return this.getBinaryValue(Base64Variants.getDefaultVariant());
   }

   public String getValueAsText() {
      return this._value;
   }

   public boolean getValueAsBoolean(boolean defaultValue) {
      return this._value != null && "true".equals(this._value.trim()) ? true : defaultValue;
   }

   public int getValueAsInt(int defaultValue) {
      return NumberInput.parseAsInt(this._value, defaultValue);
   }

   public long getValueAsLong(long defaultValue) {
      return NumberInput.parseAsLong(this._value, defaultValue);
   }

   public double getValueAsDouble(double defaultValue) {
      return NumberInput.parseAsDouble(this._value, defaultValue);
   }

   public final void serialize(JsonGenerator jg, SerializerProvider provider) throws IOException, JsonProcessingException {
      if (this._value == null) {
         jg.writeNull();
      } else {
         jg.writeString(this._value);
      }

   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else {
         return o.getClass() != this.getClass() ? false : ((TextNode)o)._value.equals(this._value);
      }
   }

   public int hashCode() {
      return this._value.hashCode();
   }

   public String toString() {
      int len = this._value.length();
      len = len + 2 + (len >> 4);
      StringBuilder sb = new StringBuilder(len);
      appendQuoted(sb, this._value);
      return sb.toString();
   }

   protected static void appendQuoted(StringBuilder sb, String content) {
      sb.append('"');
      CharTypes.appendQuoted(sb, content);
      sb.append('"');
   }

   protected void _reportInvalidBase64(Base64Variant b64variant, char ch, int bindex) throws JsonParseException {
      this._reportInvalidBase64(b64variant, ch, bindex, (String)null);
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

      throw new JsonParseException(base, JsonLocation.NA);
   }

   protected void _reportBase64EOF() throws JsonParseException {
      throw new JsonParseException("Unexpected end-of-String when base64 content", JsonLocation.NA);
   }
}
