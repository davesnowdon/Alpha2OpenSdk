package org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;

public abstract class ScalarSerializerBase extends SerializerBase {
   protected ScalarSerializerBase(Class t) {
      super(t);
   }

   protected ScalarSerializerBase(Class t, boolean dummy) {
      super(t);
   }

   public void serializeWithType(T value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
      typeSer.writeTypePrefixForScalar(value, jgen);
      this.serialize(value, jgen, provider);
      typeSer.writeTypeSuffixForScalar(value, jgen);
   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      return this.createSchemaNode("string", true);
   }
}
