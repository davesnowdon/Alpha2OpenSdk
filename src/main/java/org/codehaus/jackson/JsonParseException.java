package org.codehaus.jackson;

public class JsonParseException extends JsonProcessingException {
   static final long serialVersionUID = 123L;

   public JsonParseException(String msg, JsonLocation loc) {
      super(msg, loc);
   }

   public JsonParseException(String msg, JsonLocation loc, Throwable root) {
      super(msg, loc, root);
   }
}
