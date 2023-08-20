package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.util.EnumSet;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.TypeDeserializer;

public final class EnumSetDeserializer extends StdDeserializer<EnumSet<?>> {
   final Class<Enum> _enumClass;
   final EnumDeserializer _enumDeserializer;

   public EnumSetDeserializer(EnumResolver enumRes) {
      super(EnumSet.class);
      this._enumDeserializer = new EnumDeserializer(enumRes);
      this._enumClass = enumRes.getEnumClass();
   }

   public EnumSet<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (!jp.isExpectedStartArrayToken()) {
         throw ctxt.mappingException(EnumSet.class);
      } else {
         EnumSet result = this.constructSet();

         JsonToken t;
         while((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            if (t == JsonToken.VALUE_NULL) {
               throw ctxt.mappingException(this._enumClass);
            }

            Enum<?> value = this._enumDeserializer.deserialize(jp, ctxt);
            result.add(value);
         }

         return result;
      }
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
   }

   private EnumSet constructSet() {
      return EnumSet.noneOf(this._enumClass);
   }
}
