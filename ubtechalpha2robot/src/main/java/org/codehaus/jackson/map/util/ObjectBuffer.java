package org.codehaus.jackson.map.util;

import java.lang.reflect.Array;
import java.util.List;

public final class ObjectBuffer {
   static final int INITIAL_CHUNK_SIZE = 12;
   static final int SMALL_CHUNK_SIZE = 16384;
   static final int MAX_CHUNK_SIZE = 262144;
   private ObjectBuffer.Node _bufferHead;
   private ObjectBuffer.Node _bufferTail;
   private int _bufferedEntryCount;
   private Object[] _freeBuffer;

   public ObjectBuffer() {
   }

   public Object[] resetAndStart() {
      this._reset();
      return this._freeBuffer == null ? new Object[12] : this._freeBuffer;
   }

   public Object[] appendCompletedChunk(Object[] fullChunk) {
      ObjectBuffer.Node next = new ObjectBuffer.Node(fullChunk);
      if (this._bufferHead == null) {
         this._bufferHead = this._bufferTail = next;
      } else {
         this._bufferTail.linkNext(next);
         this._bufferTail = next;
      }

      int len = fullChunk.length;
      this._bufferedEntryCount += len;
      if (len < 16384) {
         len += len;
      } else {
         len += len >> 2;
      }

      return new Object[len];
   }

   public Object[] completeAndClearBuffer(Object[] lastChunk, int lastChunkEntries) {
      int totalSize = lastChunkEntries + this._bufferedEntryCount;
      Object[] result = new Object[totalSize];
      this._copyTo(result, totalSize, lastChunk, lastChunkEntries);
      return result;
   }

   public <T> T[] completeAndClearBuffer(Object[] lastChunk, int lastChunkEntries, Class<T> componentType) {
      int totalSize = lastChunkEntries + this._bufferedEntryCount;
      T[] result = (Object[])((Object[])Array.newInstance(componentType, totalSize));
      this._copyTo(result, totalSize, lastChunk, lastChunkEntries);
      this._reset();
      return result;
   }

   public void completeAndClearBuffer(Object[] lastChunk, int lastChunkEntries, List<Object> resultList) {
      for(ObjectBuffer.Node n = this._bufferHead; n != null; n = n.next()) {
         Object[] curr = n.getData();
         int i = 0;

         for(int len = curr.length; i < len; ++i) {
            resultList.add(curr[i]);
         }
      }

      for(int i = 0; i < lastChunkEntries; ++i) {
         resultList.add(lastChunk[i]);
      }

   }

   public int initialCapacity() {
      return this._freeBuffer == null ? 0 : this._freeBuffer.length;
   }

   public int bufferedSize() {
      return this._bufferedEntryCount;
   }

   protected void _reset() {
      if (this._bufferTail != null) {
         this._freeBuffer = this._bufferTail.getData();
      }

      this._bufferHead = this._bufferTail = null;
      this._bufferedEntryCount = 0;
   }

   protected final void _copyTo(Object resultArray, int totalSize, Object[] lastChunk, int lastChunkEntries) {
      int ptr = 0;

      for(ObjectBuffer.Node n = this._bufferHead; n != null; n = n.next()) {
         Object[] curr = n.getData();
         int len = curr.length;
         System.arraycopy(curr, 0, resultArray, ptr, len);
         ptr += len;
      }

      System.arraycopy(lastChunk, 0, resultArray, ptr, lastChunkEntries);
      ptr += lastChunkEntries;
      if (ptr != totalSize) {
         throw new IllegalStateException("Should have gotten " + totalSize + " entries, got " + ptr);
      }
   }

   static final class Node {
      final Object[] _data;
      ObjectBuffer.Node _next;

      public Node(Object[] data) {
         this._data = data;
      }

      public Object[] getData() {
         return this._data;
      }

      public ObjectBuffer.Node next() {
         return this._next;
      }

      public void linkNext(ObjectBuffer.Node next) {
         if (this._next != null) {
            throw new IllegalStateException();
         } else {
            this._next = next;
         }
      }
   }
}
