package org.codehaus.jackson.map.ser.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ser.SerializerCache;

public class JsonSerializerMap {
   private final JsonSerializerMap.Bucket[] _buckets;
   private final int _size;

   public JsonSerializerMap(Map<SerializerCache.TypeKey, JsonSerializer<Object>> serializers) {
      int size = findSize(serializers.size());
      this._size = size;
      int hashMask = size - 1;
      JsonSerializerMap.Bucket[] buckets = new JsonSerializerMap.Bucket[size];

      Entry entry;
      SerializerCache.TypeKey key;
      int index;
      for(Iterator i$ = serializers.entrySet().iterator(); i$.hasNext(); buckets[index] = new JsonSerializerMap.Bucket(buckets[index], key, (JsonSerializer)entry.getValue())) {
         entry = (Entry)i$.next();
         key = (SerializerCache.TypeKey)entry.getKey();
         index = key.hashCode() & hashMask;
      }

      this._buckets = buckets;
   }

   private static final int findSize(int size) {
      int needed = size <= 64 ? size + size : size + (size >> 2);

      int result;
      for(result = 8; result < needed; result += result) {
      }

      return result;
   }

   public int size() {
      return this._size;
   }

   public JsonSerializer<Object> find(SerializerCache.TypeKey key) {
      int index = key.hashCode() & this._buckets.length - 1;
      JsonSerializerMap.Bucket bucket = this._buckets[index];
      if (bucket == null) {
         return null;
      } else if (key.equals(bucket.key)) {
         return bucket.value;
      } else {
         do {
            if ((bucket = bucket.next) == null) {
               return null;
            }
         } while(!key.equals(bucket.key));

         return bucket.value;
      }
   }

   private static final class Bucket {
      public final SerializerCache.TypeKey key;
      public final JsonSerializer<Object> value;
      public final JsonSerializerMap.Bucket next;

      public Bucket(JsonSerializerMap.Bucket next, SerializerCache.TypeKey key, JsonSerializer<Object> value) {
         this.next = next;
         this.key = key;
         this.value = value;
      }
   }
}
