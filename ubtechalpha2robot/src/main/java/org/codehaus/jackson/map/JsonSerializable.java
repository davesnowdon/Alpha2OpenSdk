package org.codehaus.jackson.map;

import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;

/** @deprecated */
@Deprecated
public interface JsonSerializable {
   void serialize(JsonGenerator var1, SerializerProvider var2) throws IOException, JsonProcessingException;
}
