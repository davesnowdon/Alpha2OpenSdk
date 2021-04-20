package org.msgpack.type;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import org.msgpack.packer.Packer;
import org.msgpack.util.android.PortedImmutableEntry;

class SequentialMapValueImpl extends AbstractMapValue {
   private static SequentialMapValueImpl emptyInstance = new SequentialMapValueImpl(new Value[0], true);
   private Value[] array;

   public static MapValue getEmptyInstance() {
      return emptyInstance;
   }

   public Value[] getKeyValueArray() {
      return this.array;
   }

   SequentialMapValueImpl(Value[] array, boolean gift) {
      if (array.length % 2 != 0) {
         throw new IllegalArgumentException();
      } else {
         if (gift) {
            this.array = array;
         } else {
            this.array = new Value[array.length];
            System.arraycopy(array, 0, this.array, 0, array.length);
         }

      }
   }

   public Value get(Object key) {
      if (key == null) {
         return null;
      } else {
         for(int i = this.array.length - 2; i >= 0; i -= 2) {
            if (this.array[i].equals(key)) {
               return this.array[i + 1];
            }
         }

         return null;
      }
   }

   public Set<Entry<Value, Value>> entrySet() {
      return new SequentialMapValueImpl.EntrySet(this.array);
   }

   public Set<Value> keySet() {
      return new SequentialMapValueImpl.KeySet(this.array);
   }

   public Collection<Value> values() {
      return new SequentialMapValueImpl.ValueCollection(this.array);
   }

   public void writeTo(Packer pk) throws IOException {
      pk.writeMapBegin(this.array.length / 2);

      for(int i = 0; i < this.array.length; ++i) {
         this.array[i].writeTo(pk);
      }

      pk.writeMapEnd();
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Value)) {
         return false;
      } else {
         Value v = (Value)o;
         if (!v.isMapValue()) {
            return false;
         } else {
            Map<Value, Value> om = v.asMapValue();
            if (om.size() != this.array.length / 2) {
               return false;
            } else {
               try {
                  for(int i = 0; i < this.array.length; i += 2) {
                     Value key = this.array[i];
                     Value value = this.array[i + 1];
                     if (!value.equals(om.get(key))) {
                        return false;
                     }
                  }

                  return true;
               } catch (ClassCastException var7) {
                  return false;
               } catch (NullPointerException var8) {
                  return false;
               }
            }
         }
      }
   }

   public int hashCode() {
      int h = 0;

      for(int i = 0; i < this.array.length; i += 2) {
         h += this.array[i].hashCode() ^ this.array[i + 1].hashCode();
      }

      return h;
   }

   public String toString() {
      return this.toString(new StringBuilder()).toString();
   }

   public StringBuilder toString(StringBuilder sb) {
      if (this.array.length == 0) {
         return sb.append("{}");
      } else {
         sb.append("{");
         sb.append(this.array[0]);
         sb.append(":");
         sb.append(this.array[1]);

         for(int i = 2; i < this.array.length; i += 2) {
            sb.append(",");
            this.array[i].toString(sb);
            sb.append(":");
            this.array[i + 1].toString(sb);
         }

         sb.append("}");
         return sb;
      }
   }

   private static class ValueIterator implements Iterator<Value> {
      private Value[] array;
      private int pos;

      ValueIterator(Value[] array, int offset) {
         this.array = array;
         this.pos = offset;
      }

      public boolean hasNext() {
         return this.pos < this.array.length;
      }

      public Value next() {
         if (this.pos >= this.array.length) {
            throw new NoSuchElementException();
         } else {
            Value v = this.array[this.pos];
            this.pos += 2;
            return v;
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private static class ValueCollection extends AbstractCollection<Value> {
      private Value[] array;

      ValueCollection(Value[] array) {
         this.array = array;
      }

      public int size() {
         return this.array.length / 2;
      }

      public Iterator<Value> iterator() {
         return new SequentialMapValueImpl.ValueIterator(this.array, 1);
      }
   }

   private static class KeySet extends AbstractSet<Value> {
      private Value[] array;

      KeySet(Value[] array) {
         this.array = array;
      }

      public int size() {
         return this.array.length / 2;
      }

      public Iterator<Value> iterator() {
         return new SequentialMapValueImpl.ValueIterator(this.array, 0);
      }
   }

   private static class EntrySetIterator implements Iterator<Entry<Value, Value>> {
      private Value[] array;
      private int pos;
      private static final boolean hasDefaultImmutableEntry;

      EntrySetIterator(Value[] array) {
         this.array = array;
         this.pos = 0;
      }

      public boolean hasNext() {
         return this.pos < this.array.length;
      }

      public Entry<Value, Value> next() {
         if (this.pos >= this.array.length) {
            throw new NoSuchElementException();
         } else {
            Value key = this.array[this.pos];
            Value value = this.array[this.pos + 1];
            Entry<Value, Value> pair = (Entry)(hasDefaultImmutableEntry ? new SimpleImmutableEntry(key, value) : new PortedImmutableEntry(key, value));
            this.pos += 2;
            return pair;
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }

      static {
         boolean hasIt = true;

         try {
            Class.forName("java.util.AbstractMap.SimpleImmutableEntry");
         } catch (ClassNotFoundException var5) {
            hasIt = false;
         } finally {
            hasDefaultImmutableEntry = hasIt;
         }

      }
   }

   private static class EntrySet extends AbstractSet<Entry<Value, Value>> {
      private Value[] array;

      EntrySet(Value[] array) {
         this.array = array;
      }

      public int size() {
         return this.array.length / 2;
      }

      public Iterator<Entry<Value, Value>> iterator() {
         return new SequentialMapValueImpl.EntrySetIterator(this.array);
      }
   }
}
