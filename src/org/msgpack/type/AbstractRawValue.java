package org.msgpack.type;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

abstract class AbstractRawValue extends AbstractValue implements RawValue {
   static final String UTF8 = "UTF-8";
   private static final char[] HEX_TABLE = "0123456789ABCDEF".toCharArray();

   AbstractRawValue() {
   }

   public ValueType getType() {
      return ValueType.RAW;
   }

   public boolean isRawValue() {
      return true;
   }

   public RawValue asRawValue() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Value)) {
         return false;
      } else {
         Value v = (Value)o;
         return !v.isRawValue() ? false : Arrays.equals(this.getByteArray(), v.asRawValue().getByteArray());
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.getByteArray());
   }

   public String toString() {
      return this.toString(new StringBuilder()).toString();
   }

   public StringBuilder toString(StringBuilder sb) {
      String s;
      if (this.getClass() == StringRawValueImpl.class) {
         s = this.getString();
      } else {
         CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder().onMalformedInput(CodingErrorAction.IGNORE).onUnmappableCharacter(CodingErrorAction.IGNORE);

         try {
            s = decoder.decode(ByteBuffer.wrap(this.getByteArray())).toString();
         } catch (CharacterCodingException var5) {
            s = new String(this.getByteArray());
         }
      }

      sb.append("\"");

      for(int i = 0; i < s.length(); ++i) {
         char ch = s.charAt(i);
         if (ch < ' ') {
            switch(ch) {
            case '\b':
               sb.append("\\b");
               break;
            case '\t':
               sb.append("\\t");
               break;
            case '\n':
               sb.append("\\n");
               break;
            case '\u000b':
            default:
               this.escapeChar(sb, ch);
               break;
            case '\f':
               sb.append("\\f");
               break;
            case '\r':
               sb.append("\\r");
            }
         } else if (ch <= 127) {
            switch(ch) {
            case '"':
               sb.append("\\\"");
               break;
            case '\\':
               sb.append("\\\\");
               break;
            default:
               sb.append(ch);
            }
         } else if (ch >= '\ud800' && ch <= '\udfff') {
            this.escapeChar(sb, ch);
         } else {
            sb.append(ch);
         }
      }

      sb.append("\"");
      return sb;
   }

   private void escapeChar(StringBuilder sb, int ch) {
      sb.append("\\u");
      sb.append(HEX_TABLE[ch >> 12 & 15]);
      sb.append(HEX_TABLE[ch >> 8 & 15]);
      sb.append(HEX_TABLE[ch >> 4 & 15]);
      sb.append(HEX_TABLE[ch & 15]);
   }
}
