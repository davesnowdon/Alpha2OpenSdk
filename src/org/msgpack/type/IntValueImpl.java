package org.msgpack.type;

import java.io.IOException;
import java.math.BigInteger;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;

class IntValueImpl extends IntegerValue {
   private int value;
   private static int BYTE_MAX = 127;
   private static int SHORT_MAX = 32767;
   private static int BYTE_MIN = -128;
   private static int SHORT_MIN = -32768;

   IntValueImpl(int value) {
      this.value = value;
   }

   public byte getByte() {
      if (this.value <= BYTE_MAX && this.value >= BYTE_MIN) {
         return (byte)this.value;
      } else {
         throw new MessageTypeException();
      }
   }

   public short getShort() {
      if (this.value <= SHORT_MAX && this.value >= SHORT_MIN) {
         return (short)this.value;
      } else {
         throw new MessageTypeException();
      }
   }

   public int getInt() {
      return this.value;
   }

   public long getLong() {
      return (long)this.value;
   }

   public BigInteger getBigInteger() {
      return BigInteger.valueOf((long)this.value);
   }

   public byte byteValue() {
      return (byte)this.value;
   }

   public short shortValue() {
      return (short)this.value;
   }

   public int intValue() {
      return this.value;
   }

   public long longValue() {
      return (long)this.value;
   }

   public BigInteger bigIntegerValue() {
      return BigInteger.valueOf((long)this.value);
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
               return this.value == v.asIntegerValue().getInt();
            } catch (MessageTypeException var4) {
               return false;
            }
         }
      }
   }

   public int hashCode() {
      return this.value;
   }

   public String toString() {
      return Integer.toString(this.value);
   }

   public StringBuilder toString(StringBuilder sb) {
      return sb.append(Integer.toString(this.value));
   }
}
