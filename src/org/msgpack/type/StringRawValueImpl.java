package org.msgpack.type;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;

class StringRawValueImpl extends AbstractRawValue {
   private String string;

   StringRawValueImpl(String string) {
      this.string = string;
   }

   public byte[] getByteArray() {
      try {
         return this.string.getBytes("UTF-8");
      } catch (UnsupportedEncodingException var2) {
         throw new MessageTypeException(var2);
      }
   }

   public String getString() {
      return this.string;
   }

   public void writeTo(Packer pk) throws IOException {
      pk.write(this.string);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Value)) {
         return false;
      } else {
         Value v = (Value)o;
         if (!v.isRawValue()) {
            return false;
         } else {
            return v.getClass() == StringRawValueImpl.class ? this.string.equals(((StringRawValueImpl)v).string) : Arrays.equals(this.getByteArray(), v.asRawValue().getByteArray());
         }
      }
   }
}
