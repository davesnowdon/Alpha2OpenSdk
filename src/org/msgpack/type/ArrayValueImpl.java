package org.msgpack.type;

import java.io.IOException;
import java.util.ListIterator;
import org.msgpack.packer.Packer;

class ArrayValueImpl extends AbstractArrayValue {
   private static ArrayValueImpl emptyInstance = new ArrayValueImpl(new Value[0], true);
   private Value[] array;

   public static ArrayValue getEmptyInstance() {
      return emptyInstance;
   }

   public Value[] getElementArray() {
      return this.array;
   }

   ArrayValueImpl(Value[] array, boolean gift) {
      if (gift) {
         this.array = array;
      } else {
         this.array = new Value[array.length];
         System.arraycopy(array, 0, this.array, 0, array.length);
      }

   }

   public int size() {
      return this.array.length;
   }

   public boolean isEmpty() {
      return this.array.length == 0;
   }

   public Value get(int index) {
      if (index >= 0 && this.array.length > index) {
         return this.array[index];
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public int indexOf(Object o) {
      if (o == null) {
         return -1;
      } else {
         for(int i = 0; i < this.array.length; ++i) {
            if (this.array[i].equals(o)) {
               return i;
            }
         }

         return -1;
      }
   }

   public int lastIndexOf(Object o) {
      if (o == null) {
         return -1;
      } else {
         for(int i = this.array.length - 1; i >= 0; --i) {
            if (this.array[i].equals(o)) {
               return i;
            }
         }

         return -1;
      }
   }

   public void writeTo(Packer pk) throws IOException {
      pk.writeArrayBegin(this.array.length);

      for(int i = 0; i < this.array.length; ++i) {
         this.array[i].writeTo(pk);
      }

      pk.writeArrayEnd();
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Value)) {
         return false;
      } else {
         Value v = (Value)o;
         if (!v.isArrayValue()) {
            return false;
         } else if (v.getClass() == ArrayValueImpl.class) {
            return this.equals((ArrayValueImpl)v);
         } else {
            ListIterator<Value> oi = v.asArrayValue().listIterator();

            for(int i = 0; i < this.array.length; ++i) {
               if (!oi.hasNext() || !this.array[i].equals(oi.next())) {
                  return false;
               }
            }

            return !oi.hasNext();
         }
      }
   }

   private boolean equals(ArrayValueImpl o) {
      if (this.array.length != o.array.length) {
         return false;
      } else {
         for(int i = 0; i < this.array.length; ++i) {
            if (!this.array[i].equals(o.array[i])) {
               return false;
            }
         }

         return true;
      }
   }

   public int hashCode() {
      int h = 1;

      for(int i = 0; i < this.array.length; ++i) {
         Value obj = this.array[i];
         h = 31 * h + obj.hashCode();
      }

      return h;
   }

   public String toString() {
      return this.toString(new StringBuilder()).toString();
   }

   public StringBuilder toString(StringBuilder sb) {
      if (this.array.length == 0) {
         return sb.append("[]");
      } else {
         sb.append("[");
         sb.append(this.array[0]);

         for(int i = 1; i < this.array.length; ++i) {
            sb.append(",");
            this.array[i].toString(sb);
         }

         sb.append("]");
         return sb;
      }
   }
}
