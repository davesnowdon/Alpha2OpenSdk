package org.msgpack.type;

import java.math.BigInteger;

public abstract class IntegerValue extends NumberValue {
   public IntegerValue() {
   }

   public ValueType getType() {
      return ValueType.INTEGER;
   }

   public boolean isIntegerValue() {
      return true;
   }

   public IntegerValue asIntegerValue() {
      return this;
   }

   public abstract byte getByte();

   public abstract short getShort();

   public abstract int getInt();

   public abstract long getLong();

   public BigInteger getBigInteger() {
      return this.bigIntegerValue();
   }
}
