package org.codehaus.jackson.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public abstract class OutputDecorator {
   public OutputDecorator() {
   }

   public abstract OutputStream decorate(IOContext var1, OutputStream var2) throws IOException;

   public abstract Writer decorate(IOContext var1, Writer var2) throws IOException;
}
