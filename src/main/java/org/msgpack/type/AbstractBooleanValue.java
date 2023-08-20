package org.msgpack.type;

abstract class AbstractBooleanValue extends AbstractValue implements BooleanValue {
   AbstractBooleanValue() {
   }

   public ValueType getType() {
      return ValueType.BOOLEAN;
   }

   public boolean isBooleanValue() {
      return true;
   }

   public boolean isTrue() {
      return this.getBoolean();
   }

   public boolean isFalse() {
      return !this.getBoolean();
   }

   public BooleanValue asBooleanValue() {
      return this;
   }
}
