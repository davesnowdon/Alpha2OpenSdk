package org.codehaus.jackson;

import java.io.IOException;

public interface PrettyPrinter {
   void writeRootValueSeparator(JsonGenerator var1) throws IOException, JsonGenerationException;

   void writeStartObject(JsonGenerator var1) throws IOException, JsonGenerationException;

   void writeEndObject(JsonGenerator var1, int var2) throws IOException, JsonGenerationException;

   void writeObjectEntrySeparator(JsonGenerator var1) throws IOException, JsonGenerationException;

   void writeObjectFieldValueSeparator(JsonGenerator var1) throws IOException, JsonGenerationException;

   void writeStartArray(JsonGenerator var1) throws IOException, JsonGenerationException;

   void writeEndArray(JsonGenerator var1, int var2) throws IOException, JsonGenerationException;

   void writeArrayValueSeparator(JsonGenerator var1) throws IOException, JsonGenerationException;

   void beforeArrayValues(JsonGenerator var1) throws IOException, JsonGenerationException;

   void beforeObjectEntries(JsonGenerator var1) throws IOException, JsonGenerationException;
}
