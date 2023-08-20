package org.msgpack;

public class MessageTypeException extends RuntimeException {
   public MessageTypeException() {
   }

   public MessageTypeException(String message) {
      super(message);
   }

   public MessageTypeException(String message, Throwable cause) {
      super(message, cause);
   }

   public MessageTypeException(Throwable cause) {
      super(cause);
   }
}
