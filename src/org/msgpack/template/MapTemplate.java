package org.msgpack.template;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class MapTemplate<K, V> extends AbstractTemplate<Map<K, V>> {
   private Template<K> keyTemplate;
   private Template<V> valueTemplate;

   public MapTemplate(Template<K> keyTemplate, Template<V> valueTemplate) {
      this.keyTemplate = keyTemplate;
      this.valueTemplate = valueTemplate;
   }

   public void write(Packer pk, Map<K, V> target, boolean required) throws IOException {
      if (!(target instanceof Map)) {
         if (target == null) {
            if (required) {
               throw new MessageTypeException("Attempted to write null");
            } else {
               pk.writeNil();
            }
         } else {
            throw new MessageTypeException("Target is not a Map but " + target.getClass());
         }
      } else {
         pk.writeMapBegin(target.size());
         Iterator i$ = target.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<K, V> pair = (Entry)i$.next();
            this.keyTemplate.write(pk, pair.getKey());
            this.valueTemplate.write(pk, pair.getValue());
         }

         pk.writeMapEnd();
      }
   }

   public Map<K, V> read(Unpacker u, Map<K, V> to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         int n = u.readMapBegin();
         Object map;
         if (to != null) {
            map = to;
            to.clear();
         } else {
            map = new HashMap(n);
         }

         for(int i = 0; i < n; ++i) {
            K key = this.keyTemplate.read(u, (Object)null);
            V value = this.valueTemplate.read(u, (Object)null);
            ((Map)map).put(key, value);
         }

         u.readMapEnd();
         return (Map)map;
      }
   }
}
