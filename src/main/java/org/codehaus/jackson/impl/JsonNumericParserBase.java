package org.codehaus.jackson.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.io.NumberInput;

public abstract class JsonNumericParserBase extends JsonParserBase {
   protected static final int NR_UNKNOWN = 0;
   protected static final int NR_INT = 1;
   protected static final int NR_LONG = 2;
   protected static final int NR_BIGINT = 4;
   protected static final int NR_DOUBLE = 8;
   protected static final int NR_BIGDECIMAL = 16;
   static final BigDecimal BD_MIN_LONG = new BigDecimal(-9223372036854775808L);
   static final BigDecimal BD_MAX_LONG = new BigDecimal(9223372036854775807L);
   static final BigDecimal BD_MIN_INT = new BigDecimal(-9223372036854775808L);
   static final BigDecimal BD_MAX_INT = new BigDecimal(9223372036854775807L);
   static final long MIN_INT_L = -2147483648L;
   static final long MAX_INT_L = 2147483647L;
   static final double MIN_LONG_D = -9.223372036854776E18D;
   static final double MAX_LONG_D = 9.223372036854776E18D;
   static final double MIN_INT_D = -2.147483648E9D;
   static final double MAX_INT_D = 2.147483647E9D;
   protected static final int INT_0 = 48;
   protected static final int INT_1 = 49;
   protected static final int INT_2 = 50;
   protected static final int INT_3 = 51;
   protected static final int INT_4 = 52;
   protected static final int INT_5 = 53;
   protected static final int INT_6 = 54;
   protected static final int INT_7 = 55;
   protected static final int INT_8 = 56;
   protected static final int INT_9 = 57;
   protected static final int INT_MINUS = 45;
   protected static final int INT_PLUS = 43;
   protected static final int INT_DECIMAL_POINT = 46;
   protected static final int INT_e = 101;
   protected static final int INT_E = 69;
   protected static final char CHAR_NULL = '\u0000';
   protected int _numTypesValid = 0;
   protected int _numberInt;
   protected long _numberLong;
   protected double _numberDouble;
   protected BigInteger _numberBigInt;
   protected BigDecimal _numberBigDecimal;
   protected boolean _numberNegative;
   protected int _intLength;
   protected int _fractLength;
   protected int _expLength;

   protected JsonNumericParserBase(IOContext ctxt, int features) {
      super(ctxt, features);
   }

   protected final JsonToken reset(boolean negative, int intLen, int fractLen, int expLen) {
      return fractLen < 1 && expLen < 1 ? this.resetInt(negative, intLen) : this.resetFloat(negative, intLen, fractLen, expLen);
   }

   protected final JsonToken resetInt(boolean negative, int intLen) {
      this._numberNegative = negative;
      this._intLength = intLen;
      this._fractLength = 0;
      this._expLength = 0;
      this._numTypesValid = 0;
      return JsonToken.VALUE_NUMBER_INT;
   }

   protected final JsonToken resetFloat(boolean negative, int intLen, int fractLen, int expLen) {
      this._numberNegative = negative;
      this._intLength = intLen;
      this._fractLength = fractLen;
      this._expLength = expLen;
      this._numTypesValid = 0;
      return JsonToken.VALUE_NUMBER_FLOAT;
   }

   protected final JsonToken resetAsNaN(String valueStr, double value) {
      this._textBuffer.resetWithString(valueStr);
      this._numberDouble = value;
      this._numTypesValid = 8;
      return JsonToken.VALUE_NUMBER_FLOAT;
   }

   public Number getNumberValue() throws IOException, JsonParseException {
      if (this._numTypesValid == 0) {
         this._parseNumericValue(0);
      }

      if (this._currToken == JsonToken.VALUE_NUMBER_INT) {
         if ((this._numTypesValid & 1) != 0) {
            return this._numberInt;
         } else if ((this._numTypesValid & 2) != 0) {
            return this._numberLong;
         } else {
            return (Number)((this._numTypesValid & 4) != 0 ? this._numberBigInt : this._numberBigDecimal);
         }
      } else if ((this._numTypesValid & 16) != 0) {
         return this._numberBigDecimal;
      } else {
         if ((this._numTypesValid & 8) == 0) {
            this._throwInternal();
         }

         return this._numberDouble;
      }
   }

   public JsonParser.NumberType getNumberType() throws IOException, JsonParseException {
      if (this._numTypesValid == 0) {
         this._parseNumericValue(0);
      }

      if (this._currToken == JsonToken.VALUE_NUMBER_INT) {
         if ((this._numTypesValid & 1) != 0) {
            return JsonParser.NumberType.INT;
         } else {
            return (this._numTypesValid & 2) != 0 ? JsonParser.NumberType.LONG : JsonParser.NumberType.BIG_INTEGER;
         }
      } else {
         return (this._numTypesValid & 16) != 0 ? JsonParser.NumberType.BIG_DECIMAL : JsonParser.NumberType.DOUBLE;
      }
   }

   public int getIntValue() throws IOException, JsonParseException {
      if ((this._numTypesValid & 1) == 0) {
         if (this._numTypesValid == 0) {
            this._parseNumericValue(1);
         }

         if ((this._numTypesValid & 1) == 0) {
            this.convertNumberToInt();
         }
      }

      return this._numberInt;
   }

   public long getLongValue() throws IOException, JsonParseException {
      if ((this._numTypesValid & 2) == 0) {
         if (this._numTypesValid == 0) {
            this._parseNumericValue(2);
         }

         if ((this._numTypesValid & 2) == 0) {
            this.convertNumberToLong();
         }
      }

      return this._numberLong;
   }

   public BigInteger getBigIntegerValue() throws IOException, JsonParseException {
      if ((this._numTypesValid & 4) == 0) {
         if (this._numTypesValid == 0) {
            this._parseNumericValue(4);
         }

         if ((this._numTypesValid & 4) == 0) {
            this.convertNumberToBigInteger();
         }
      }

      return this._numberBigInt;
   }

   public float getFloatValue() throws IOException, JsonParseException {
      double value = this.getDoubleValue();
      return (float)value;
   }

   public double getDoubleValue() throws IOException, JsonParseException {
      if ((this._numTypesValid & 8) == 0) {
         if (this._numTypesValid == 0) {
            this._parseNumericValue(8);
         }

         if ((this._numTypesValid & 8) == 0) {
            this.convertNumberToDouble();
         }
      }

      return this._numberDouble;
   }

   public BigDecimal getDecimalValue() throws IOException, JsonParseException {
      if ((this._numTypesValid & 16) == 0) {
         if (this._numTypesValid == 0) {
            this._parseNumericValue(16);
         }

         if ((this._numTypesValid & 16) == 0) {
            this.convertNumberToBigDecimal();
         }
      }

      return this._numberBigDecimal;
   }

   protected void _parseNumericValue(int expType) throws IOException, JsonParseException {
      if (this._currToken == JsonToken.VALUE_NUMBER_INT) {
         char[] buf = this._textBuffer.getTextBuffer();
         int offset = this._textBuffer.getTextOffset();
         int len = this._intLength;
         if (this._numberNegative) {
            ++offset;
         }

         if (len <= 9) {
            int i = NumberInput.parseInt(buf, offset, len);
            this._numberInt = this._numberNegative ? -i : i;
            this._numTypesValid = 1;
         } else if (len <= 18) {
            long l = NumberInput.parseLong(buf, offset, len);
            if (this._numberNegative) {
               l = -l;
            }

            if (len == 10) {
               if (this._numberNegative) {
                  if (l >= -2147483648L) {
                     this._numberInt = (int)l;
                     this._numTypesValid = 1;
                     return;
                  }
               } else if (l <= 2147483647L) {
                  this._numberInt = (int)l;
                  this._numTypesValid = 1;
                  return;
               }
            }

            this._numberLong = l;
            this._numTypesValid = 2;
         } else {
            this._parseSlowIntValue(expType, buf, offset, len);
         }
      } else if (this._currToken == JsonToken.VALUE_NUMBER_FLOAT) {
         this._parseSlowFloatValue(expType);
      } else {
         this._reportError("Current token (" + this._currToken + ") not numeric, can not use numeric value accessors");
      }
   }

   private final void _parseSlowFloatValue(int expType) throws IOException, JsonParseException {
      try {
         if (expType == 16) {
            this._numberBigDecimal = this._textBuffer.contentsAsDecimal();
            this._numTypesValid = 16;
         } else {
            this._numberDouble = this._textBuffer.contentsAsDouble();
            this._numTypesValid = 8;
         }
      } catch (NumberFormatException var3) {
         this._wrapError("Malformed numeric value '" + this._textBuffer.contentsAsString() + "'", var3);
      }

   }

   private final void _parseSlowIntValue(int expType, char[] buf, int offset, int len) throws IOException, JsonParseException {
      String numStr = this._textBuffer.contentsAsString();

      try {
         if (NumberInput.inLongRange(buf, offset, len, this._numberNegative)) {
            this._numberLong = Long.parseLong(numStr);
            this._numTypesValid = 2;
         } else {
            this._numberBigInt = new BigInteger(numStr);
            this._numTypesValid = 4;
         }
      } catch (NumberFormatException var7) {
         this._wrapError("Malformed numeric value '" + numStr + "'", var7);
      }

   }

   protected void convertNumberToInt() throws IOException, JsonParseException {
      if ((this._numTypesValid & 2) != 0) {
         int result = (int)this._numberLong;
         if ((long)result != this._numberLong) {
            this._reportError("Numeric value (" + this.getText() + ") out of range of int");
         }

         this._numberInt = result;
      } else if ((this._numTypesValid & 4) != 0) {
         this._numberInt = this._numberBigInt.intValue();
      } else if ((this._numTypesValid & 8) != 0) {
         if (this._numberDouble < -2.147483648E9D || this._numberDouble > 2.147483647E9D) {
            this.reportOverflowInt();
         }

         this._numberInt = (int)this._numberDouble;
      } else if ((this._numTypesValid & 16) != 0) {
         if (BD_MIN_INT.compareTo(this._numberBigDecimal) > 0 || BD_MAX_INT.compareTo(this._numberBigDecimal) < 0) {
            this.reportOverflowInt();
         }

         this._numberInt = this._numberBigDecimal.intValue();
      } else {
         this._throwInternal();
      }

      this._numTypesValid |= 1;
   }

   protected void convertNumberToLong() throws IOException, JsonParseException {
      if ((this._numTypesValid & 1) != 0) {
         this._numberLong = (long)this._numberInt;
      } else if ((this._numTypesValid & 4) != 0) {
         this._numberLong = this._numberBigInt.longValue();
      } else if ((this._numTypesValid & 8) != 0) {
         if (this._numberDouble < -9.223372036854776E18D || this._numberDouble > 9.223372036854776E18D) {
            this.reportOverflowLong();
         }

         this._numberLong = (long)this._numberDouble;
      } else if ((this._numTypesValid & 16) != 0) {
         if (BD_MIN_LONG.compareTo(this._numberBigDecimal) > 0 || BD_MAX_LONG.compareTo(this._numberBigDecimal) < 0) {
            this.reportOverflowLong();
         }

         this._numberLong = this._numberBigDecimal.longValue();
      } else {
         this._throwInternal();
      }

      this._numTypesValid |= 2;
   }

   protected void convertNumberToBigInteger() throws IOException, JsonParseException {
      if ((this._numTypesValid & 16) != 0) {
         this._numberBigInt = this._numberBigDecimal.toBigInteger();
      } else if ((this._numTypesValid & 2) != 0) {
         this._numberBigInt = BigInteger.valueOf(this._numberLong);
      } else if ((this._numTypesValid & 1) != 0) {
         this._numberBigInt = BigInteger.valueOf((long)this._numberInt);
      } else if ((this._numTypesValid & 8) != 0) {
         this._numberBigInt = BigDecimal.valueOf(this._numberDouble).toBigInteger();
      } else {
         this._throwInternal();
      }

      this._numTypesValid |= 4;
   }

   protected void convertNumberToDouble() throws IOException, JsonParseException {
      if ((this._numTypesValid & 16) != 0) {
         this._numberDouble = this._numberBigDecimal.doubleValue();
      } else if ((this._numTypesValid & 4) != 0) {
         this._numberDouble = this._numberBigInt.doubleValue();
      } else if ((this._numTypesValid & 2) != 0) {
         this._numberDouble = (double)this._numberLong;
      } else if ((this._numTypesValid & 1) != 0) {
         this._numberDouble = (double)this._numberInt;
      } else {
         this._throwInternal();
      }

      this._numTypesValid |= 8;
   }

   protected void convertNumberToBigDecimal() throws IOException, JsonParseException {
      if ((this._numTypesValid & 8) != 0) {
         this._numberBigDecimal = new BigDecimal(this.getText());
      } else if ((this._numTypesValid & 4) != 0) {
         this._numberBigDecimal = new BigDecimal(this._numberBigInt);
      } else if ((this._numTypesValid & 2) != 0) {
         this._numberBigDecimal = BigDecimal.valueOf(this._numberLong);
      } else if ((this._numTypesValid & 1) != 0) {
         this._numberBigDecimal = BigDecimal.valueOf((long)this._numberInt);
      } else {
         this._throwInternal();
      }

      this._numTypesValid |= 16;
   }

   protected void reportUnexpectedNumberChar(int ch, String comment) throws JsonParseException {
      String msg = "Unexpected character (" + _getCharDesc(ch) + ") in numeric value";
      if (comment != null) {
         msg = msg + ": " + comment;
      }

      this._reportError(msg);
   }

   protected void reportInvalidNumber(String msg) throws JsonParseException {
      this._reportError("Invalid numeric value: " + msg);
   }

   protected void reportOverflowInt() throws IOException, JsonParseException {
      this._reportError("Numeric value (" + this.getText() + ") out of range of int (" + -2147483648 + " - " + 2147483647 + ")");
   }

   protected void reportOverflowLong() throws IOException, JsonParseException {
      this._reportError("Numeric value (" + this.getText() + ") out of range of long (" + -9223372036854775808L + " - " + 9223372036854775807L + ")");
   }
}
