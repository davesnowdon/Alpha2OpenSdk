package org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.util.JsonParserSequence;
import org.codehaus.jackson.util.TokenBuffer;

public class AsPropertyTypeDeserializer extends AsArrayTypeDeserializer {
   protected final String _typePropertyName;

   public AsPropertyTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property, String typePropName) {
      super(bt, idRes, property);
      this._typePropertyName = typePropName;
   }

   public JsonTypeInfo.As getTypeInclusion() {
      return JsonTypeInfo.As.PROPERTY;
   }

   public String getPropertyName() {
      return this._typePropertyName;
   }

   public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = ((JsonParser)jp).getCurrentToken();
      if (t == JsonToken.START_OBJECT) {
         t = ((JsonParser)jp).nextToken();
      } else if (t != JsonToken.FIELD_NAME) {
         throw ctxt.wrongTokenException((JsonParser)jp, JsonToken.START_OBJECT, "need JSON Object to contain As.PROPERTY type information (for class " + this.baseTypeName() + ")");
      }

      for(TokenBuffer tb = null; t == JsonToken.FIELD_NAME; t = ((JsonParser)jp).nextToken()) {
         String name = ((JsonParser)jp).getCurrentName();
         ((JsonParser)jp).nextToken();
         if (this._typePropertyName.equals(name)) {
            String typeId = ((JsonParser)jp).getText();
            JsonDeserializer<Object> deser = this._findDeserializer(ctxt, typeId);
            if (tb != null) {
               jp = JsonParserSequence.createFlattened(tb.asParser((JsonParser)jp), (JsonParser)jp);
            }

            ((JsonParser)jp).nextToken();
            return deser.deserialize((JsonParser)jp, ctxt);
         }

         if (tb == null) {
            tb = new TokenBuffer((ObjectCodec)null);
         }

         tb.writeFieldName(name);
         tb.copyCurrentStructure((JsonParser)jp);
      }

      throw ctxt.wrongTokenException((JsonParser)jp, JsonToken.FIELD_NAME, "missing property '" + this._typePropertyName + "' that is to contain type id  (for class " + this.baseTypeName() + ")");
   }

   public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return jp.getCurrentToken() == JsonToken.START_ARRAY ? super.deserializeTypedFromArray(jp, ctxt) : this.deserializeTypedFromObject(jp, ctxt);
   }
}
