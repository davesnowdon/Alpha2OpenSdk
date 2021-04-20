package org.msgpack.type;

import java.io.IOException;
import org.msgpack.packer.Packer;

class FalseValueImpl extends AbstractBooleanValue {
   private static FalseValueImpl instance = new FalseValueImpl();

   private FalseValueImpl() {
   }

   static FalseValueImpl getInstance() {
      return instance;
   }

   public boolean getBoolean() {
      return false;
   }

   public void writeTo(Packer pk) throws IOException {
      pk.write(false);
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Value)) {
         return false;
      } else {
         Value v = (Value)o;
         if (!v.isBooleanValue()) {
            return false;
         } else {
            return !v.asBooleanValue().getBoolean();
         }
      }
   }

   public int hashCode() {
      return 1237;
   }

   public String toString() {
      return "false";
   }

   public StringBuilder toString(StringBuilder sb) {
      return sb.append("false");
   }
}
