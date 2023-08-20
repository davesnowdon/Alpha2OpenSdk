package org.msgpack.type;

public abstract class FloatValue extends NumberValue {
   public FloatValue() {
   }

   public ValueType getType() {
      return ValueType.FLOAT;
   }

   public boolean isFloatValue() {
      return true;
   }

   public FloatValue asFloatValue() {
      return this;
   }

   public abstract float getFloat();

   public abstract double getDouble();
}
