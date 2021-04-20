package org.msgpack.util.android;

import java.io.Serializable;
import java.util.Map.Entry;

public class PortedImmutableEntry<K, V> implements Entry<K, V>, Serializable {
   private static final long serialVersionUID = -4564047655287765373L;
   private final K key;
   private final V value;

   public PortedImmutableEntry(K theKey, V theValue) {
      this.key = theKey;
      this.value = theValue;
   }

   public PortedImmutableEntry(Entry<? extends K, ? extends V> copyFrom) {
      this.key = copyFrom.getKey();
      this.value = copyFrom.getValue();
   }

   public K getKey() {
      return this.key;
   }

   public V getValue() {
      return this.value;
   }

   public V setValue(V object) {
      throw new UnsupportedOperationException();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof Entry)) {
         return false;
      } else {
         boolean var10000;
         label43: {
            label29: {
               Entry<?, ?> entry = (Entry)object;
               if (this.key == null) {
                  if (entry.getKey() != null) {
                     break label29;
                  }
               } else if (!this.key.equals(entry.getKey())) {
                  break label29;
               }

               if (this.value == null) {
                  if (entry.getValue() == null) {
                     break label43;
                  }
               } else if (this.value.equals(entry.getValue())) {
                  break label43;
               }
            }

            var10000 = false;
            return var10000;
         }

         var10000 = true;
         return var10000;
      }
   }

   public int hashCode() {
      return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
   }

   public String toString() {
      return this.key + "=" + this.value;
   }
}
