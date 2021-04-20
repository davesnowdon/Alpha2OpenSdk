package org.codehaus.jackson;

import java.io.IOException;

public class JsonProcessingException extends IOException {
   static final long serialVersionUID = 123L;
   protected JsonLocation mLocation;

   protected JsonProcessingException(String msg, JsonLocation loc, Throwable rootCause) {
      super(msg);
      if (rootCause != null) {
         this.initCause(rootCause);
      }

      this.mLocation = loc;
   }

   protected JsonProcessingException(String msg) {
      super(msg);
   }

   protected JsonProcessingException(String msg, JsonLocation loc) {
      this(msg, loc, (Throwable)null);
   }

   protected JsonProcessingException(String msg, Throwable rootCause) {
      this(msg, (JsonLocation)null, rootCause);
   }

   protected JsonProcessingException(Throwable rootCause) {
      this((String)null, (JsonLocation)null, rootCause);
   }

   public JsonLocation getLocation() {
      return this.mLocation;
   }

   public String getMessage() {
      String msg = super.getMessage();
      if (msg == null) {
         msg = "N/A";
      }

      JsonLocation loc = this.getLocation();
      if (loc != null) {
         StringBuilder sb = new StringBuilder();
         sb.append(msg);
         sb.append('\n');
         sb.append(" at ");
         sb.append(loc.toString());
         return sb.toString();
      } else {
         return msg;
      }
   }

   public String toString() {
      return this.getClass().getName() + ": " + this.getMessage();
   }
}
