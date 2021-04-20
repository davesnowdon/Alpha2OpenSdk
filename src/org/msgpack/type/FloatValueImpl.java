package org.msgpack.type;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.msgpack.packer.Packer;

class FloatValueImpl extends FloatValue {
   private float value;

   FloatValueImpl(float value) {
      this.value = value;
   }

   public float getFloat() {
      return this.value;
   }

   public double getDouble() {
      return (double)this.value;
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
      return (new BigDecimal((double)this.value)).toBigInteger();
   }

   public float floatValue() {
      return this.value;
   }

   public double doubleValue() {
      return (double)this.value;
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
            return (double)this.value == v.asFloatValue().getDouble();
         }
      }
   }

   public void writeTo(Packer pk) throws IOException {
      pk.write(this.value);
   }

   public int hashCode() {
      return Float.floatToIntBits(this.value);
   }

   public String toString() {
      return Float.toString(this.value);
   }

   public StringBuilder toString(StringBuilder sb) {
      return sb.append(Float.toString(this.value));
   }
}
