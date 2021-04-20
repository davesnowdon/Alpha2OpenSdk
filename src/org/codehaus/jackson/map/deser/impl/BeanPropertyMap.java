package org.codehaus.jackson.map.deser.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.codehaus.jackson.map.deser.SettableBeanProperty;

public final class BeanPropertyMap {
   private final BeanPropertyMap.Bucket[] _buckets;
   private final int _hashMask;
   private final int _size;

   public BeanPropertyMap(Collection<SettableBeanProperty> properties) {
      this._size = properties.size();
      int bucketCount = findSize(this._size);
      this._hashMask = bucketCount - 1;
      BeanPropertyMap.Bucket[] buckets = new BeanPropertyMap.Bucket[bucketCount];

      SettableBeanProperty property;
      String key;
      int index;
      for(Iterator i$ = properties.iterator(); i$.hasNext(); buckets[index] = new BeanPropertyMap.Bucket(buckets[index], key, property)) {
         property = (SettableBeanProperty)i$.next();
         key = property.getName();
         index = key.hashCode() & this._hashMask;
      }

      this._buckets = buckets;
   }

   public void assignIndexes() {
      int index = 0;
      BeanPropertyMap.Bucket[] arr$ = this._buckets;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         for(BeanPropertyMap.Bucket bucket = arr$[i$]; bucket != null; bucket = bucket.next) {
            bucket.value.assignIndex(index++);
         }
      }

   }

   private static final int findSize(int size) {
      int needed = size <= 32 ? size + size : size + (size >> 2);

      int result;
      for(result = 2; result < needed; result += result) {
      }

      return result;
   }

   public int size() {
      return this._size;
   }

   public Iterator<SettableBeanProperty> allProperties() {
      return new BeanPropertyMap.IteratorImpl(this._buckets);
   }

   public SettableBeanProperty find(String key) {
      int index = key.hashCode() & this._hashMask;
      BeanPropertyMap.Bucket bucket = this._buckets[index];
      if (bucket == null) {
         return null;
      } else if (bucket.key == key) {
         return bucket.value;
      } else {
         do {
            if ((bucket = bucket.next) == null) {
               return this._findWithEquals(key, index);
            }
         } while(bucket.key != key);

         return bucket.value;
      }
   }

   public void replace(SettableBeanProperty property) {
      String name = property.getName();
      int index = name.hashCode() & this._buckets.length - 1;
      BeanPropertyMap.Bucket tail = null;
      boolean found = false;

      for(BeanPropertyMap.Bucket bucket = this._buckets[index]; bucket != null; bucket = bucket.next) {
         if (!found && bucket.key.equals(name)) {
            found = true;
         } else {
            tail = new BeanPropertyMap.Bucket(tail, bucket.key, bucket.value);
         }
      }

      if (!found) {
         throw new NoSuchElementException("No entry '" + property + "' found, can't replace");
      } else {
         this._buckets[index] = new BeanPropertyMap.Bucket(tail, name, property);
      }
   }

   private SettableBeanProperty _findWithEquals(String key, int index) {
      for(BeanPropertyMap.Bucket bucket = this._buckets[index]; bucket != null; bucket = bucket.next) {
         if (key.equals(bucket.key)) {
            return bucket.value;
         }
      }

      return null;
   }

   private static final class IteratorImpl implements Iterator<SettableBeanProperty> {
      private final BeanPropertyMap.Bucket[] _buckets;
      private BeanPropertyMap.Bucket _currentBucket;
      private int _nextBucketIndex;

      public IteratorImpl(BeanPropertyMap.Bucket[] buckets) {
         this._buckets = buckets;
         int i = 0;
         int len = this._buckets.length;

         while(i < len) {
            BeanPropertyMap.Bucket b = this._buckets[i++];
            if (b != null) {
               this._currentBucket = b;
               break;
            }
         }

         this._nextBucketIndex = i;
      }

      public boolean hasNext() {
         return this._currentBucket != null;
      }

      public SettableBeanProperty next() {
         BeanPropertyMap.Bucket curr = this._currentBucket;
         if (curr == null) {
            throw new NoSuchElementException();
         } else {
            BeanPropertyMap.Bucket b;
            for(b = curr.next; b == null && this._nextBucketIndex < this._buckets.length; b = this._buckets[this._nextBucketIndex++]) {
            }

            this._currentBucket = b;
            return curr.value;
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private static final class Bucket {
      public final BeanPropertyMap.Bucket next;
      public final String key;
      public final SettableBeanProperty value;

      public Bucket(BeanPropertyMap.Bucket next, String key, SettableBeanProperty value) {
         this.next = next;
         this.key = key;
         this.value = value;
      }
   }
}
