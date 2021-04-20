package org.msgpack.template.builder;

public class TemplateBuildException extends RuntimeException {
   public TemplateBuildException(String reason) {
      super(reason);
   }

   public TemplateBuildException(String reason, Throwable t) {
      super(reason, t);
   }

   public TemplateBuildException(Throwable t) {
      super(t);
   }
}
