package org.msgpack.type;

import java.io.IOException;
import java.math.BigInteger;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;

class BigIntegerValueImpl extends IntegerValue {
   private BigInteger value;
   private static BigInteger BYTE_MAX = BigInteger.valueOf(127L);
   private static BigInteger SHORT_MAX = BigInteger.valueOf(32767L);
   private static BigInteger INT_MAX = BigInteger.valueOf(2147483647L);
   private static BigInteger LONG_MAX = BigInteger.valueOf(9223372036854775807L);
   private static BigInteger BYTE_MIN = BigInteger.valueOf(-128L);
   private static BigInteger SHORT_MIN = BigInteger.valueOf(-32768L);
   private static BigInteger INT_MIN = BigInteger.valueOf(-2147483648L);
   private static BigInteger LONG_MIN = BigInteger.valueOf(-9223372036854775808L);

   BigIntegerValueImpl(BigInteger value) {
      this.value = value;
   }

   public byte getByte() {
      if (this.value.compareTo(BYTE_MAX) <= 0 && this.value.compareTo(BYTE_MIN) >= 0) {
         return this.value.byteValue();
      } else {
         throw new MessageTypeException();
      }
   }

   public short getShort() {
      if (this.value.compareTo(SHORT_MAX) <= 0 && this.value.compareTo(SHORT_MIN) >= 0) {
         return this.value.shortValue();
      } else {
         throw new MessageTypeException();
      }
   }

   public int getInt() {
      if (this.value.compareTo(INT_MAX) <= 0 && this.value.compareTo(INT_MIN) >= 0) {
         return this.value.intValue();
      } else {
         throw new MessageTypeException();
      }
   }

   public long getLong() {
      if (this.value.compareTo(LONG_MAX) <= 0 && this.value.compareTo(LONG_MIN) >= 0) {
         return this.value.longValue();
      } else {
         throw new MessageTypeException();
      }
   }

   public BigInteger getBigInteger() {
      return this.value;
   }

   public byte byteValue() {
      return this.value.byteValue();
   }

   public short shortValue() {
      return this.value.shortValue();
   }

   public int intValue() {
      return this.value.intValue();
   }

   public long longValue() {
      return this.value.longValue();
   }

   public BigInteger bigIntegerValue() {
      return this.value;
   }

   public float floatValue() {
      return this.value.floatValue();
   }

   public double doubleValue() {
      return this.value.doubleValue();
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
         return !v.isIntegerValue() ? false : this.value.equals(v.asIntegerValue().bigIntegerValue());
      }
   }

   public int hashCode() {
      if (INT_MIN.compareTo(this.value) <= 0 && this.value.compareTo(INT_MAX) <= 0) {
         return (int)this.value.longValue();
      } else if (LONG_MIN.compareTo(this.value) <= 0 && this.value.compareTo(LONG_MAX) <= 0) {
         long v = this.value.longValue();
         return (int)(v ^ v >>> 32);
      } else {
         return this.value.hashCode();
      }
   }

   public String toString() {
      return this.value.toString();
   }

   public StringBuilder toString(StringBuilder sb) {
      return sb.append(this.value.toString());
   }
}
