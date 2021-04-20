package org.msgpack.unpacker;

import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.msgpack.packer.Unconverter;
import org.msgpack.type.Value;

public class UnpackerIterator implements Iterator<Value> {
   private final AbstractUnpacker u;
   private final Unconverter uc;
   private IOException exception;

   public UnpackerIterator(AbstractUnpacker u) {
      this.u = u;
      this.uc = new Unconverter(u.msgpack);
   }

   public boolean hasNext() {
      if (this.uc.getResult() != null) {
         return true;
      } else {
         try {
            this.u.readValue(this.uc);
         } catch (EOFException var2) {
            return false;
         } catch (IOException var3) {
            this.exception = var3;
            return false;
         }

         return this.uc.getResult() != null;
      }
   }

   public Value next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         Value v = this.uc.getResult();
         this.uc.resetResult();
         return v;
      }
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }

   public IOException getException() {
      return this.exception;
   }
}
