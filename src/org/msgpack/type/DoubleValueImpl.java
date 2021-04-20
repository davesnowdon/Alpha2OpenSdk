package org.msgpack.type;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.msgpack.packer.Packer;

class DoubleValueImpl extends FloatValue {
   private double value;

   DoubleValueImpl(double value) {
      this.value = value;
   }

   public float getFloat() {
      return (float)this.value;
   }

   public double getDouble() {
      return this.value;
   }

   public byte byteValue() {
      return (byte)((int)this.value);
   }

   public short shortValue() {
      return (short)((int)this.value);
   }

   public int intValue() {
      return (int)this.value;
   }

   public long longValue() {
      return (long)this.value;
   }

   public BigInteger bigIntegerValue() {
      return (new BigDecimal(this.value)).toBigInteger();
   }

   public float floatValue() {
      return (float)this.value;
   }

   public double doubleValue() {
      return this.value;
   }

   public void writeTo(Packer pk) throws IOException {
      pk.write(this.value);
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Value)) {
         return false;
      } else {
         Value v = (Value)o;
         if (!v.isFloatValue()) {
            return false;
         } else {
            return this.value == v.asFloatValue().getDouble();
         }
      }
   }

   public int hashCode() {
      long v = Double.doubleToLongBits(this.value);
      return (int)(v ^ v >>> 32);
   }

   public String toString() {
      return Double.toString(this.value);
   }

   public StringBuilder toString(StringBuilder sb) {
      return sb.append(Double.toString(this.value));
   }
}
