package org.msgpack.io;

import java.io.EOFException;

public class EndOfBufferException extends EOFException {
   public EndOfBufferException() {
   }

   public EndOfBufferException(String s) {
      super(s);
   }
}
