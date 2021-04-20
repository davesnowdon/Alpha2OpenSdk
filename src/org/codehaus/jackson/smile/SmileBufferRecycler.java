package org.codehaus.jackson.smile;

import java.util.Arrays;

public class SmileBufferRecycler<T> {
   public static final int DEFAULT_NAME_BUFFER_LENGTH = 64;
   public static final int DEFAULT_STRING_VALUE_BUFFER_LENGTH = 64;
   protected T[] _seenNamesBuffer;
   protected T[] _seenStringValuesBuffer;

   public SmileBufferRecycler() {
   }

   public T[] allocSeenNamesBuffer() {
      T[] result = this._seenNamesBuffer;
      if (result != null) {
         this._seenNamesBuffer = null;
         Arrays.fill(result, (Object)null);
      }

      return result;
   }

   public T[] allocSeenStringValuesBuffer() {
      T[] result = this._seenStringValuesBuffer;
      if (result != null) {
         this._seenStringValuesBuffer = null;
         Arrays.fill(result, (Object)null);
      }

      return result;
   }

   public void releaseSeenNamesBuffer(T[] buffer) {
      this._seenNamesBuffer = buffer;
   }

   public void releaseSeenStringValuesBuffer(T[] buffer) {
      this._seenStringValuesBuffer = buffer;
   }
}
