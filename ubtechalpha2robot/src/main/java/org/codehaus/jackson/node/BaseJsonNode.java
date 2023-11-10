package org.codehaus.jackson.node;

import java.io.IOException;
import java.util.List;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;

public abstract class BaseJsonNode extends JsonNode implements JsonSerializableWithType {
   protected BaseJsonNode() {
   }

   public JsonNode findValue(String fieldName) {
      return null;
   }

   public final JsonNode findPath(String fieldName) {
      JsonNode value = this.findValue(fieldName);
      return (JsonNode)(value == null ? MissingNode.getInstance() : value);
   }

   public ObjectNode findParent(String fieldName) {
      return null;
   }

   public List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar) {
      return foundSoFar;
   }

   public List<String> findValuesAsText(String fieldName, List<String> foundSoFar) {
      return foundSoFar;
   }

   public List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar) {
      return foundSoFar;
   }

   public JsonParser traverse() {
      return new TreeTraversingParser(this);
   }

   public abstract JsonToken asToken();

   public JsonParser.NumberType getNumberType() {
      return null;
   }

   public abstract void serialize(JsonGenerator var1, SerializerProvider var2) throws IOException, JsonProcessingException;

   public abstract void serializeWithType(JsonGenerator var1, SerializerProvider var2, TypeSerializer var3) throws IOException, JsonProcessingException;

   public final void writeTo(JsonGenerator jgen) throws IOException, JsonGenerationException {
      this.serialize(jgen, (SerializerProvider)null);
   }
}
