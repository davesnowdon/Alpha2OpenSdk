package org.msgpack.unpacker;

import java.io.IOException;

public class SizeLimitException extends IOException {
   public SizeLimitException() {
   }

   public SizeLimitException(String message) {
      super(message);
   }

   public SizeLimitException(String message, Throwable cause) {
      super(message, cause);
   }

   public SizeLimitException(Throwable cause) {
      super(cause);
   }
}
