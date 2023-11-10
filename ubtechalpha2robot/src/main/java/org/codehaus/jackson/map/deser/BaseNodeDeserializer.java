package org.codehaus.jackson.map.deser;

import java.io.IOException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

abstract class BaseNodeDeserializer<N extends JsonNode> extends StdDeserializer<N> {
   public BaseNodeDeserializer(Class<N> nodeClass) {
      super(nodeClass);
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
   }

   protected void _reportProblem(JsonParser jp, String msg) throws JsonMappingException {
      throw new JsonMappingException(msg, jp.getTokenLocation());
   }

   protected void _handleDuplicateField(String fieldName, ObjectNode objectNode, JsonNode oldValue, JsonNode newValue) throws JsonProcessingException {
   }

   protected final ObjectNode deserializeObject(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      ObjectNode node = ctxt.getNodeFactory().objectNode();
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.START_OBJECT) {
         t = jp.nextToken();
      }

      for(; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
         String fieldName = jp.getCurrentName();
         jp.nextToken();
         JsonNode value = this.deserializeAny(jp, ctxt);
         JsonNode old = node.put(fieldName, value);
         if (old != null) {
            this._handleDuplicateField(fieldName, node, old, value);
         }
      }

      return node;
   }

   protected final ArrayNode deserializeArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      ArrayNode node = ctxt.getNodeFactory().arrayNode();

      while(jp.nextToken() != JsonToken.END_ARRAY) {
         node.add(this.deserializeAny(jp, ctxt));
      }

      return node;
   }

   protected final JsonNode deserializeAny(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonNodeFactory nodeFactory = ctxt.getNodeFactory();
      JsonParser.NumberType nt;
      switch(jp.getCurrentToken()) {
      case START_OBJECT:
      case FIELD_NAME:
         return this.deserializeObject(jp, ctxt);
      case START_ARRAY:
         return this.deserializeArray(jp, ctxt);
      case VALUE_STRING:
         return nodeFactory.textNode(jp.getText());
      case VALUE_NUMBER_INT:
         nt = jp.getNumberType();
         if (nt != JsonParser.NumberType.BIG_INTEGER && !ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
            if (nt == JsonParser.NumberType.INT) {
               return nodeFactory.numberNode(jp.getIntValue());
            }

            return nodeFactory.numberNode(jp.getLongValue());
         }

         return nodeFactory.numberNode(jp.getBigIntegerValue());
      case VALUE_NUMBER_FLOAT:
         nt = jp.getNumberType();
         if (nt != JsonParser.NumberType.BIG_DECIMAL && !ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
            return nodeFactory.numberNode(jp.getDoubleValue());
         }

         return nodeFactory.numberNode(jp.getDecimalValue());
      case VALUE_TRUE:
         return nodeFactory.booleanNode(true);
      case VALUE_FALSE:
         return nodeFactory.booleanNode(false);
      case VALUE_NULL:
         return nodeFactory.nullNode();
      case END_OBJECT:
      case END_ARRAY:
      default:
         throw ctxt.mappingException(this.getValueClass());
      }
   }
}
