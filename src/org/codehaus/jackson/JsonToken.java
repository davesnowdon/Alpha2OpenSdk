package org.codehaus.jackson;

public enum JsonToken {
   NOT_AVAILABLE((String)null),
   START_OBJECT("{"),
   END_OBJECT("}"),
   START_ARRAY("["),
   END_ARRAY("]"),
   FIELD_NAME((String)null),
   VALUE_EMBEDDED_OBJECT((String)null),
   VALUE_STRING((String)null),
   VALUE_NUMBER_INT((String)null),
   VALUE_NUMBER_FLOAT((String)null),
   VALUE_TRUE("true"),
   VALUE_FALSE("false"),
   VALUE_NULL("null");

   final String _serialized;
   final char[] _serializedChars;
   final byte[] _serializedBytes;

   private JsonToken(String token) {
      if (token == null) {
         this._serialized = null;
         this._serializedChars = null;
         this._serializedBytes = null;
      } else {
         this._serialized = token;
         this._serializedChars = token.toCharArray();
         int len = this._serializedChars.length;
         this._serializedBytes = new byte[len];

         for(int i = 0; i < len; ++i) {
            this._serializedBytes[i] = (byte)this._serializedChars[i];
         }
      }

   }

   public String asString() {
      return this._serialized;
   }

   public char[] asCharArray() {
      return this._serializedChars;
   }

   public byte[] asByteArray() {
      return this._serializedBytes;
   }

   public boolean isNumeric() {
      return this == VALUE_NUMBER_INT || this == VALUE_NUMBER_FLOAT;
   }

   public boolean isScalarValue() {
      return this.ordinal() >= VALUE_EMBEDDED_OBJECT.ordinal();
   }
}
