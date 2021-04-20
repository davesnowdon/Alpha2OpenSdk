package org.msgpack.io;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class LinkedBufferInput extends AbstractInput {
   LinkedList<ByteBuffer> link = new LinkedList();
   int writable = -1;
   private int nextAdvance;
   private byte[] tmpBuffer = new byte[8];
   private ByteBuffer tmpByteBuffer;
   private final int bufferSize;

   public LinkedBufferInput(int bufferSize) {
      this.tmpByteBuffer = ByteBuffer.wrap(this.tmpBuffer);
      this.bufferSize = bufferSize;
   }

   public int read(byte[] b, int off, int len) throws EOFException {
      if (this.link.isEmpty()) {
         return 0;
      } else {
         int olen = len;

         ByteBuffer bb;
         do {
            bb = (ByteBuffer)this.link.getFirst();
            if (len < bb.remaining()) {
               bb.get(b, off, len);
               this.incrReadByteCount(len);
               return olen;
            }

            int rem = bb.remaining();
            bb.get(b, off, rem);
            this.incrReadByteCount(rem);
            len -= rem;
            off += rem;
         } while(this.removeFirstLink(bb));

         return olen - len;
      }
   }

   public boolean tryRefer(BufferReferer ref, int len) throws IOException {
      ByteBuffer bb = null;

      try {
         bb = (ByteBuffer)this.link.getFirst();
      } catch (NoSuchElementException var10) {
      }

      if (bb == null) {
         throw new EndOfBufferException();
      } else if (bb.remaining() < len) {
         return false;
      } else {
         boolean success = false;
         int pos = bb.position();
         int lim = bb.limit();

         try {
            bb.limit(pos + len);
            ref.refer(bb, true);
            this.incrReadByteCount(len);
            success = true;
         } finally {
            bb.limit(lim);
            if (success) {
               bb.position(pos + len);
            } else {
               bb.position(pos);
            }

            if (bb.remaining() == 0) {
               this.removeFirstLink(bb);
            }

         }

         return true;
      }
   }

   public byte readByte() throws EOFException {
      ByteBuffer bb = null;

      try {
         bb = (ByteBuffer)this.link.getFirst();
      } catch (NoSuchElementException var3) {
      }

      if (bb != null && bb.remaining() != 0) {
         byte result = bb.get();
         this.incrReadOneByteCount();
         if (bb.remaining() == 0) {
            this.removeFirstLink(bb);
         }

         return result;
      } else {
         throw new EndOfBufferException();
      }
   }

   public void advance() {
      if (!this.link.isEmpty()) {
         int len = this.nextAdvance;

         ByteBuffer bb;
         do {
            bb = (ByteBuffer)this.link.getFirst();
            if (len < bb.remaining()) {
               bb.position(bb.position() + len);
               break;
            }

            len -= bb.remaining();
            bb.position(bb.position() + bb.remaining());
         } while(this.removeFirstLink(bb));

         this.incrReadByteCount(this.nextAdvance);
         this.nextAdvance = 0;
      }
   }

   private boolean removeFirstLink(ByteBuffer first) {
      if (this.link.size() == 1) {
         if (this.writable >= 0) {
            first.position(0);
            first.limit(0);
            this.writable = first.capacity();
            return false;
         } else {
            this.link.removeFirst();
            return false;
         }
      } else {
         this.link.removeFirst();
         return true;
      }
   }

   private void requireMore(int n) throws EOFException {
      int off = 0;

      int rem;
      for(Iterator i$ = this.link.iterator(); i$.hasNext(); off += rem) {
         ByteBuffer bb = (ByteBuffer)i$.next();
         if (n <= bb.remaining()) {
            rem = bb.position();
            bb.get(this.tmpBuffer, off, n);
            bb.position(rem);
            return;
         }

         rem = bb.remaining();
         int pos = bb.position();
         bb.get(this.tmpBuffer, off, rem);
         bb.position(pos);
         n -= rem;
      }

      throw new EndOfBufferException();
   }

   private ByteBuffer require(int n) throws EOFException {
      ByteBuffer bb = null;

      try {
         bb = (ByteBuffer)this.link.getFirst();
      } catch (NoSuchElementException var4) {
      }

      if (bb == null) {
         throw new EndOfBufferException();
      } else if (n <= bb.remaining()) {
         this.nextAdvance = n;
         return bb;
      } else {
         this.requireMore(n);
         this.nextAdvance = n;
         return this.tmpByteBuffer;
      }
   }

   public byte getByte() throws EOFException {
      ByteBuffer bb = this.require(1);
      return bb.get(bb.position());
   }

   public short getShort() throws EOFException {
      ByteBuffer bb = this.require(2);
      return bb.getShort(bb.position());
   }

   public int getInt() throws EOFException {
      ByteBuffer bb = this.require(4);
      return bb.getInt(bb.position());
   }

   public long getLong() throws EOFException {
      ByteBuffer bb = this.require(8);
      return bb.getLong(bb.position());
   }

   public float getFloat() throws EOFException {
      ByteBuffer bb = this.require(4);
      return bb.getFloat(bb.position());
   }

   public double getDouble() throws EOFException {
      ByteBuffer bb = this.require(8);
      return bb.getDouble(bb.position());
   }

   public void feed(byte[] b) {
      this.feed(b, 0, b.length, false);
   }

   public void feed(byte[] b, boolean reference) {
      this.feed(b, 0, b.length, reference);
   }

   public void feed(byte[] b, int off, int len) {
      this.feed(b, off, len, false);
   }

   public void feed(byte[] b, int off, int len, boolean reference) {
      if (reference) {
         if (this.writable > 0 && ((ByteBuffer)this.link.getLast()).remaining() == 0) {
            this.link.add(this.link.size() - 1, ByteBuffer.wrap(b, off, len));
         } else {
            this.link.addLast(ByteBuffer.wrap(b, off, len));
            this.writable = -1;
         }
      } else {
         ByteBuffer bb = null;

         try {
            bb = (ByteBuffer)this.link.getLast();
         } catch (NoSuchElementException var8) {
         }

         int sz;
         if (len <= this.writable) {
            sz = bb.position();
            bb.position(bb.limit());
            bb.limit(bb.limit() + len);
            bb.put(b, off, len);
            bb.position(sz);
            this.writable = bb.capacity() - bb.limit();
         } else {
            if (this.writable > 0) {
               sz = bb.position();
               bb.position(bb.limit());
               bb.limit(bb.limit() + this.writable);
               bb.put(b, off, this.writable);
               bb.position(sz);
               off += this.writable;
               len -= this.writable;
               this.writable = 0;
            }

            sz = Math.max(len, this.bufferSize);
            ByteBuffer nb = ByteBuffer.allocate(sz);
            nb.put(b, off, len);
            nb.limit(len);
            nb.position(0);
            this.link.addLast(nb);
            this.writable = sz - len;
         }
      }
   }

   public void feed(ByteBuffer b) {
      this.feed(b, false);
   }

   public void feed(ByteBuffer buf, boolean reference) {
      if (reference) {
         if (this.writable > 0 && ((ByteBuffer)this.link.getLast()).remaining() == 0) {
            this.link.add(this.link.size() - 1, buf);
         } else {
            this.link.addLast(buf);
            this.writable = -1;
         }
      } else {
         int rem = buf.remaining();
         ByteBuffer bb = null;

         try {
            bb = (ByteBuffer)this.link.getLast();
         } catch (NoSuchElementException var7) {
         }

         int sz;
         if (rem <= this.writable) {
            sz = bb.position();
            bb.position(bb.limit());
            bb.limit(bb.limit() + rem);
            bb.put(buf);
            bb.position(sz);
            this.writable = bb.capacity() - bb.limit();
         } else {
            if (this.writable > 0) {
               sz = bb.position();
               bb.position(bb.limit());
               bb.limit(bb.limit() + this.writable);
               buf.limit(this.writable);
               bb.put(buf);
               bb.position(sz);
               rem -= this.writable;
               buf.limit(buf.limit() + rem);
               this.writable = 0;
            }

            sz = Math.max(rem, this.bufferSize);
            ByteBuffer nb = ByteBuffer.allocate(sz);
            nb.put(buf);
            nb.limit(rem);
            nb.position(0);
            this.link.addLast(nb);
            this.writable = sz - rem;
         }
      }
   }

   public void clear() {
      if (this.writable >= 0) {
         ByteBuffer bb = (ByteBuffer)this.link.getLast();
         this.link.clear();
         bb.position(0);
         bb.limit(0);
         this.link.addLast(bb);
         this.writable = bb.capacity();
      } else {
         this.link.clear();
         this.writable = -1;
      }

   }

   public void copyReferencedBuffer() {
      if (!this.link.isEmpty()) {
         int size = 0;

         ByteBuffer bb;
         for(Iterator i$ = this.link.iterator(); i$.hasNext(); size += bb.remaining()) {
            bb = (ByteBuffer)i$.next();
         }

         if (size != 0) {
            if (this.writable >= 0) {
               ByteBuffer last = (ByteBuffer)this.link.removeLast();
               byte[] copy = new byte[size - last.remaining()];
               int off = 0;

               int len;
               for(Iterator i$ = this.link.iterator(); i$.hasNext(); off += len) {
                  ByteBuffer bb = (ByteBuffer)i$.next();
                  len = bb.remaining();
                  bb.get(copy, off, len);
               }

               this.link.clear();
               this.link.add(ByteBuffer.wrap(copy));
               this.link.add(last);
            } else {
               byte[] copy = new byte[size];
               int off = 0;

               int len;
               for(Iterator i$ = this.link.iterator(); i$.hasNext(); off += len) {
                  ByteBuffer bb = (ByteBuffer)i$.next();
                  len = bb.remaining();
                  bb.get(copy, off, len);
               }

               this.link.clear();
               this.link.add(ByteBuffer.wrap(copy));
               this.writable = 0;
            }

         }
      }
   }

   public int getSize() {
      int size = 0;

      ByteBuffer bb;
      for(Iterator i$ = this.link.iterator(); i$.hasNext(); size += bb.remaining()) {
         bb = (ByteBuffer)i$.next();
      }

      return size;
   }

   public void close() {
   }
}
