package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.util.ObjectBuffer;

@JacksonStdImpl
public class UntypedObjectDeserializer extends StdDeserializer<Object> {
   public UntypedObjectDeserializer() {
      super(Object.class);
   }

   public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      switch(jp.getCurrentToken()) {
      case VALUE_STRING:
         return jp.getText();
      case VALUE_NUMBER_INT:
         if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
            return jp.getBigIntegerValue();
         }

         return jp.getNumberValue();
      case VALUE_NUMBER_FLOAT:
         if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
            return jp.getDecimalValue();
         }

         return jp.getDoubleValue();
      case VALUE_TRUE:
         return Boolean.TRUE;
      case VALUE_FALSE:
         return Boolean.FALSE;
      case VALUE_EMBEDDED_OBJECT:
         return jp.getEmbeddedObject();
      case VALUE_NULL:
         return null;
      case START_ARRAY:
         return this.mapArray(jp, ctxt);
      case START_OBJECT:
      case FIELD_NAME:
         return this.mapObject(jp, ctxt);
      case END_ARRAY:
      case END_OBJECT:
      default:
         throw ctxt.mappingException(Object.class);
      }
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      switch(t) {
      case VALUE_STRING:
         return jp.getText();
      case VALUE_NUMBER_INT:
         if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
            return jp.getBigIntegerValue();
         }

         return jp.getIntValue();
      case VALUE_NUMBER_FLOAT:
         if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
            return jp.getDecimalValue();
         }

         return jp.getDoubleValue();
      case VALUE_TRUE:
         return Boolean.TRUE;
      case VALUE_FALSE:
         return Boolean.FALSE;
      case VALUE_EMBEDDED_OBJECT:
         return jp.getEmbeddedObject();
      case VALUE_NULL:
         return null;
      case START_ARRAY:
      case START_OBJECT:
      case FIELD_NAME:
         return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
      default:
         throw ctxt.mappingException(Object.class);
      }
   }

   protected List<Object> mapArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (jp.nextToken() == JsonToken.END_ARRAY) {
         return new ArrayList(4);
      } else {
         ObjectBuffer buffer = ctxt.leaseObjectBuffer();
         Object[] values = buffer.resetAndStart();
         int ptr = 0;
         int totalSize = 0;

         do {
            Object value = this.deserialize(jp, ctxt);
            ++totalSize;
            if (ptr >= values.length) {
               values = buffer.appendCompletedChunk(values);
               ptr = 0;
            }

            values[ptr++] = value;
         } while(jp.nextToken() != JsonToken.END_ARRAY);

         ArrayList<Object> result = new ArrayList(totalSize + (totalSize >> 3) + 1);
         buffer.completeAndClearBuffer(values, ptr, (List)result);
         return result;
      }
   }

   protected Map<String, Object> mapObject(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.START_OBJECT) {
         t = jp.nextToken();
      }

      if (t != JsonToken.FIELD_NAME) {
         return new LinkedHashMap(4);
      } else {
         String field1 = jp.getText();
         jp.nextToken();
         Object value1 = this.deserialize(jp, ctxt);
         if (jp.nextToken() != JsonToken.FIELD_NAME) {
            LinkedHashMap<String, Object> result = new LinkedHashMap(4);
            result.put(field1, value1);
            return result;
         } else {
            String field2 = jp.getText();
            jp.nextToken();
            Object value2 = this.deserialize(jp, ctxt);
            LinkedHashMap result;
            if (jp.nextToken() != JsonToken.FIELD_NAME) {
               result = new LinkedHashMap(4);
               result.put(field1, value1);
               result.put(field2, value2);
               return result;
            } else {
               result = new LinkedHashMap();
               result.put(field1, value1);
               result.put(field2, value2);

               do {
                  String fieldName = jp.getText();
                  jp.nextToken();
                  result.put(fieldName, this.deserialize(jp, ctxt));
               } while(jp.nextToken() != JsonToken.END_OBJECT);

               return result;
            }
         }
      }
   }
}
