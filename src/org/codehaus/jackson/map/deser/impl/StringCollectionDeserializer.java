package org.codehaus.jackson.map.deser.impl;

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
import org.codehaus.jackson.map.deser.ContainerDeserializer;
import org.codehaus.jackson.type.JavaType;

@JacksonStdImpl
public final class StringCollectionDeserializer extends ContainerDeserializer<Collection<String>> {
   protected final JavaType _collectionType;
   protected final JsonDeserializer<String> _valueDeserializer;
   protected final boolean _isDefaultDeserializer;
   final Constructor<Collection<String>> _defaultCtor;

   public StringCollectionDeserializer(JavaType collectionType, JsonDeserializer<?> valueDeser, Constructor<?> ctor) {
      super(collectionType.getRawClass());
      this._collectionType = collectionType;
      this._valueDeserializer = valueDeser;
      this._defaultCtor = ctor;
      this._isDefaultDeserializer = this.isDefaultSerializer(valueDeser);
   }

   public JavaType getContentType() {
      return this._collectionType.getContentType();
   }

   public JsonDeserializer<Object> getContentDeserializer() {
      JsonDeserializer<?> deser = this._valueDeserializer;
      return deser;
   }

   public Collection<String> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      Collection result;
      try {
         result = (Collection)this._defaultCtor.newInstance();
      } catch (Exception var5) {
         throw ctxt.instantiationException(this._collectionType.getRawClass(), (Throwable)var5);
      }

      return this.deserialize(jp, ctxt, result);
   }

   public Collection<String> deserialize(JsonParser jp, DeserializationContext ctxt, Collection<String> result) throws IOException, JsonProcessingException {
      if (!jp.isExpectedStartArrayToken()) {
         return this.handleNonArray(jp, ctxt, result);
      } else if (!this._isDefaultDeserializer) {
         return this.deserializeUsingCustom(jp, ctxt, result);
      } else {
         JsonToken t;
         while((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            result.add(t == JsonToken.VALUE_NULL ? null : jp.getText());
         }

         return result;
      }
   }

   private Collection<String> deserializeUsingCustom(JsonParser jp, DeserializationContext ctxt, Collection<String> result) throws IOException, JsonProcessingException {
      JsonToken t;
      String value;
      for(JsonDeserializer deser = this._valueDeserializer; (t = jp.nextToken()) != JsonToken.END_ARRAY; result.add(value)) {
         if (t == JsonToken.VALUE_NULL) {
            value = null;
         } else {
            value = (String)deser.deserialize(jp, ctxt);
         }
      }

      return result;
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
   }

   private final Collection<String> handleNonArray(JsonParser jp, DeserializationContext ctxt, Collection<String> result) throws IOException, JsonProcessingException {
      if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
         throw ctxt.mappingException(this._collectionType.getRawClass());
      } else {
         JsonDeserializer<String> valueDes = this._valueDeserializer;
         JsonToken t = jp.getCurrentToken();
         String value;
         if (t == JsonToken.VALUE_NULL) {
            value = null;
         } else {
            value = valueDes == null ? jp.getText() : (String)valueDes.deserialize(jp, ctxt);
         }

         result.add(value);
         return result;
      }
   }
}
