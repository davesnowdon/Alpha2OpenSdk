package org.codehaus.jackson.util;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public final class InternCache extends LinkedHashMap<String, String> {
   private static final int MAX_ENTRIES = 192;
   public static final InternCache instance = new InternCache();

   private InternCache() {
      super(192, 0.8F, true);
   }

   protected boolean removeEldestEntry(Entry<String, String> eldest) {
      return this.size() > 192;
   }

   public synchronized String intern(String input) {
      String result = (String)this.get(input);
      if (result == null) {
         result = input.intern();
         this.put(result, result);
      }

      return result;
   }
}
