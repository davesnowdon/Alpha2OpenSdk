package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.type.JavaType;

@JacksonStdImpl
public class CollectionDeserializer extends ContainerDeserializer<Collection<Object>> {
   protected final JavaType _collectionType;
   final JsonDeserializer<Object> _valueDeserializer;
   final TypeDeserializer _valueTypeDeserializer;
   final Constructor<Collection<Object>> _defaultCtor;

   public CollectionDeserializer(JavaType collectionType, JsonDeserializer<Object> valueDeser, TypeDeserializer valueTypeDeser, Constructor<Collection<Object>> ctor) {
      super(collectionType.getRawClass());
      this._collectionType = collectionType;
      this._valueDeserializer = valueDeser;
      this._valueTypeDeserializer = valueTypeDeser;
      if (ctor == null) {
         throw new IllegalArgumentException("No default constructor found for container class " + collectionType.getRawClass().getName());
      } else {
         this._defaultCtor = ctor;
      }
   }

   public JavaType getContentType() {
      return this._collectionType.getContentType();
   }

   public JsonDeserializer<Object> getContentDeserializer() {
      return this._valueDeserializer;
   }

   public Collection<Object> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      Collection result;
      try {
         result = (Collection)this._defaultCtor.newInstance();
      } catch (Exception var5) {
         throw ctxt.instantiationException(this._collectionType.getRawClass(), (Throwable)var5);
      }

      return this.deserialize(jp, ctxt, result);
   }

   public Collection<Object> deserialize(JsonParser jp, DeserializationContext ctxt, Collection<Object> result) throws IOException, JsonProcessingException {
      if (!jp.isExpectedStartArrayToken()) {
         return this.handleNonArray(jp, ctxt, result);
      } else {
         JsonDeserializer<Object> valueDes = this._valueDeserializer;

         JsonToken t;
         Object value;
         for(TypeDeserializer typeDeser = this._valueTypeDeserializer; (t = jp.nextToken()) != JsonToken.END_ARRAY; result.add(value)) {
            if (t == JsonToken.VALUE_NULL) {
               value = null;
            } else if (typeDeser == null) {
               value = valueDes.deserialize(jp, ctxt);
            } else {
               value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
            }
         }

         return result;
      }
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
   }

   private final Collection<Object> handleNonArray(JsonParser jp, DeserializationContext ctxt, Collection<Object> result) throws IOException, JsonProcessingException {
      if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
         throw ctxt.mappingException(this._collectionType.getRawClass());
      } else {
         JsonDeserializer<Object> valueDes = this._valueDeserializer;
         TypeDeserializer typeDeser = this._valueTypeDeserializer;
         JsonToken t = jp.getCurrentToken();
         Object value;
         if (t == JsonToken.VALUE_NULL) {
            value = null;
         } else if (typeDeser == null) {
            value = valueDes.deserialize(jp, ctxt);
         } else {
            value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
         }

         result.add(value);
         return result;
      }
   }
}
