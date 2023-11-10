package org.msgpack.io;

import java.util.Iterator;
import java.util.LinkedList;

public final class LinkedBufferOutput extends BufferedOutput {
   private LinkedList<LinkedBufferOutput.Link> link = new LinkedList();
   private int size;

   public LinkedBufferOutput(int bufferSize) {
      super(bufferSize);
   }

   public byte[] toByteArray() {
      byte[] bytes = new byte[this.size + this.filled];
      int off = 0;

      LinkedBufferOutput.Link l;
      for(Iterator i$ = this.link.iterator(); i$.hasNext(); off += l.size) {
         l = (LinkedBufferOutput.Link)i$.next();
         System.arraycopy(l.buffer, l.offset, bytes, off, l.size);
      }

      if (this.filled > 0) {
         System.arraycopy(this.buffer, 0, bytes, off, this.filled);
      }

      return bytes;
   }

   public int getSize() {
      return this.size + this.filled;
   }

   protected boolean flushBuffer(byte[] b, int off, int len) {
      this.link.add(new LinkedBufferOutput.Link(b, off, len));
      this.size += len;
      return false;
   }

   public void clear() {
      this.link.clear();
      this.size = 0;
      this.filled = 0;
   }

   public void close() {
   }

   private static final class Link {
      final byte[] buffer;
      final int offset;
      final int size;

      Link(byte[] buffer, int offset, int size) {
         this.buffer = buffer;
         this.offset = offset;
         this.size = size;
      }
   }
}
