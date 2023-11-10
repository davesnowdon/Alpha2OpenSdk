package org.codehaus.jackson.node;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.JsonParser;

public abstract class NumericNode extends ValueNode {
   protected NumericNode() {
   }

   public final boolean isNumber() {
      return true;
   }

   public abstract JsonParser.NumberType getNumberType();

   public abstract Number getNumberValue();

   public abstract int getIntValue();

   public abstract long getLongValue();

   public abstract double getDoubleValue();

   public abstract BigDecimal getDecimalValue();

   public abstract BigInteger getBigIntegerValue();

   public abstract String getValueAsText();

   public int getValueAsInt() {
      return this.getIntValue();
   }

   public int getValueAsInt(int defaultValue) {
      return this.getIntValue();
   }

   public long getValueAsLong() {
      return this.getLongValue();
   }

   public long getValueAsLong(long defaultValue) {
      return this.getLongValue();
   }

   public double getValueAsDouble() {
      return this.getDoubleValue();
   }

   public double getValueAsDouble(double defaultValue) {
      return this.getDoubleValue();
   }
}
