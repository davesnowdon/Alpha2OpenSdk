package org.msgpack.type;

import java.io.IOException;
import org.msgpack.packer.Packer;

class TrueValueImpl extends AbstractBooleanValue {
   private static TrueValueImpl instance = new TrueValueImpl();

   private TrueValueImpl() {
   }

   static TrueValueImpl getInstance() {
      return instance;
   }

   public boolean getBoolean() {
      return true;
   }

   public void writeTo(Packer pk) throws IOException {
      pk.write(true);
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
            return v.asBooleanValue().getBoolean();
         }
      }
   }

   public int hashCode() {
      return 1231;
   }

   public String toString() {
      return "true";
   }

   public StringBuilder toString(StringBuilder sb) {
      return sb.append("true");
   }
}
