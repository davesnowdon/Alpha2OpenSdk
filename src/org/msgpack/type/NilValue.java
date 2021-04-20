package org.msgpack.type;

import java.io.IOException;
import org.msgpack.packer.Packer;

public class NilValue extends AbstractValue {
   private static NilValue instance = new NilValue();

   private NilValue() {
   }

   static NilValue getInstance() {
      return instance;
   }

   public ValueType getType() {
      return ValueType.NIL;
   }

   public boolean isNilValue() {
      return true;
   }

   public NilValue asNilValue() {
      return this;
   }

   public String toString() {
      return "null";
   }

   public StringBuilder toString(StringBuilder sb) {
      return sb.append("null");
   }

   public void writeTo(Packer pk) throws IOException {
      pk.writeNil();
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return !(o instanceof Value) ? false : ((Value)o).isNilValue();
      }
   }

   public int hashCode() {
      return 0;
   }
}
