package org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.SerializerProvider;

public final class StdKeySerializer extends SerializerBase<Object> {
   static final StdKeySerializer instace = new StdKeySerializer();

   public StdKeySerializer() {
      super(Object.class);
   }

   public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      String keyStr = value.getClass() == String.class ? (String)value : value.toString();
      jgen.writeFieldName(keyStr);
   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      return this.createSchemaNode("string");
   }
}
