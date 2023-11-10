package org.codehaus.jackson.map.deser;

import java.io.IOException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class JsonNodeDeserializer extends BaseNodeDeserializer<JsonNode> {
   /** @deprecated */
   @Deprecated
   public static final JsonNodeDeserializer instance = new JsonNodeDeserializer();

   protected JsonNodeDeserializer() {
      super(JsonNode.class);
   }

   public static JsonDeserializer<? extends JsonNode> getDeserializer(Class<?> nodeClass) {
      if (nodeClass == ObjectNode.class) {
         return JsonNodeDeserializer.ObjectDeserializer.getInstance();
      } else {
         return (JsonDeserializer)(nodeClass == ArrayNode.class ? JsonNodeDeserializer.ArrayDeserializer.getInstance() : instance);
      }
   }

   public JsonNode deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return this.deserializeAny(jp, ctxt);
   }

   static final class ArrayDeserializer extends BaseNodeDeserializer<ArrayNode> {
      protected static final JsonNodeDeserializer.ArrayDeserializer _instance = new JsonNodeDeserializer.ArrayDeserializer();

      protected ArrayDeserializer() {
         super(ArrayNode.class);
      }

      public static JsonNodeDeserializer.ArrayDeserializer getInstance() {
         return _instance;
      }

      public ArrayNode deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (jp.isExpectedStartArrayToken()) {
            return this.deserializeArray(jp, ctxt);
         } else {
            throw ctxt.mappingException(ArrayNode.class);
         }
      }
   }

   static final class ObjectDeserializer extends BaseNodeDeserializer<ObjectNode> {
      protected static final JsonNodeDeserializer.ObjectDeserializer _instance = new JsonNodeDeserializer.ObjectDeserializer();

      protected ObjectDeserializer() {
         super(ObjectNode.class);
      }

      public static JsonNodeDeserializer.ObjectDeserializer getInstance() {
         return _instance;
      }

      public ObjectNode deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
            jp.nextToken();
            return this.deserializeObject(jp, ctxt);
         } else if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
            return this.deserializeObject(jp, ctxt);
         } else {
            throw ctxt.mappingException(ObjectNode.class);
         }
      }
   }
}
