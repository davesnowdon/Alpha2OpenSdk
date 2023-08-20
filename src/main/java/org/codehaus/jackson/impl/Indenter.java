package org.codehaus.jackson.impl;

import java.io.IOException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

public interface Indenter {
   void writeIndentation(JsonGenerator var1, int var2) throws IOException, JsonGenerationException;

   boolean isInline();
}
