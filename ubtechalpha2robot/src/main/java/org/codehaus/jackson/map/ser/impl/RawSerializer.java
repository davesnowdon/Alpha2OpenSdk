package org.codehaus.jackson.map.ser.impl;

import java.io.IOException;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.ser.SerializerBase;

@JacksonStdImpl
public class RawSerializer extends SerializerBase {
   public RawSerializer(Class<?> cls) {
      super(cls, false);
   }

   public void serialize(T value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      jgen.writeRawValue(value.toString());
   }

   public void serializeWithType(T value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonProcessingException {
      typeSer.writeTypePrefixForScalar(value, jgen);
      this.serialize(value, jgen, provider);
      typeSer.writeTypeSuffixForScalar(value, jgen);
   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      return this.createSchemaNode("string", true);
   }
}
