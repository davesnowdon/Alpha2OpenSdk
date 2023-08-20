package org.codehaus.jackson.node;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.io.NumberOutput;
import org.codehaus.jackson.map.SerializerProvider;

public final class DoubleNode extends NumericNode {
   protected final double _value;

   public DoubleNode(double v) {
      this._value = v;
   }

   public static DoubleNode valueOf(double v) {
      return new DoubleNode(v);
   }

   public JsonToken asToken() {
      return JsonToken.VALUE_NUMBER_FLOAT;
   }

   public JsonParser.NumberType getNumberType() {
      return JsonParser.NumberType.DOUBLE;
   }

   public boolean isFloatingPointNumber() {
      return true;
   }

   public boolean isDouble() {
      return true;
   }

   public Number getNumberValue() {
      return this._value;
   }

   public int getIntValue() {
      return (int)this._value;
   }

   public long getLongValue() {
      return (long)this._value;
   }

   public double getDoubleValue() {
      return this._value;
   }

   public BigDecimal getDecimalValue() {
      return BigDecimal.valueOf(this._value);
   }

   public BigInteger getBigIntegerValue() {
      return this.getDecimalValue().toBigInteger();
   }

   public String getValueAsText() {
      return NumberOutput.toString(this._value);
   }

   public final void serialize(JsonGenerator jg, SerializerProvider provider) throws IOException, JsonProcessingException {
      jg.writeNumber(this._value);
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         return ((DoubleNode)o)._value == this._value;
      }
   }

   public int hashCode() {
      long l = Double.doubleToLongBits(this._value);
      return (int)l ^ (int)(l >> 32);
   }
}
