package org.msgpack.type;

import java.io.IOException;
import java.math.BigInteger;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;

class LongValueImpl extends IntegerValue {
   private long value;
   private static long BYTE_MAX = 127L;
   private static long SHORT_MAX = 32767L;
   private static long INT_MAX = 2147483647L;
   private static long BYTE_MIN = -128L;
   private static long SHORT_MIN = -32768L;
   private static long INT_MIN = -2147483648L;

   LongValueImpl(long value) {
      this.value = value;
   }

   public byte getByte() {
      if (this.value <= BYTE_MAX && this.value >= BYTE_MIN) {
         return (byte)((int)this.value);
      } else {
         throw new MessageTypeException();
      }
   }

   public short getShort() {
      if (this.value <= SHORT_MAX && this.value >= SHORT_MIN) {
         return (short)((int)this.value);
      } else {
         throw new MessageTypeException();
      }
   }

   public int getInt() {
      if (this.value <= INT_MAX && this.value >= INT_MIN) {
         return (int)this.value;
      } else {
         throw new MessageTypeException();
      }
   }

   public long getLong() {
      return this.value;
   }

   public BigInteger getBigInteger() {
      return BigInteger.valueOf(this.value);
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
      return this.value;
   }

   public BigInteger bigIntegerValue() {
      return BigInteger.valueOf(this.value);
   }

   public float floatValue() {
      return (float)this.value;
   }

   public double doubleValue() {
      return (double)this.value;
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
         if (!v.isIntegerValue()) {
            return false;
         } else {
            try {
               return this.value == v.asIntegerValue().getLong();
            } catch (MessageTypeException var4) {
               return false;
            }
         }
      }
   }

   public int hashCode() {
      return INT_MIN <= this.value && this.value <= INT_MAX ? (int)this.value : (int)(this.value ^ this.value >>> 32);
   }

   public String toString() {
      return Long.toString(this.value);
   }

   public StringBuilder toString(StringBuilder sb) {
      return sb.append(Long.toString(this.value));
   }
}
