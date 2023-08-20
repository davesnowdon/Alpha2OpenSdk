package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.reflect.Array;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.util.ObjectBuffer;
import org.codehaus.jackson.type.JavaType;

@JacksonStdImpl
public class ArrayDeserializer extends ContainerDeserializer<Object[]> {
   protected final JavaType _arrayType;
   protected final boolean _untyped;
   protected final Class<?> _elementClass;
   protected final JsonDeserializer<Object> _elementDeserializer;
   final TypeDeserializer _elementTypeDeserializer;

   /** @deprecated */
   @Deprecated
   public ArrayDeserializer(ArrayType arrayType, JsonDeserializer<Object> elemDeser) {
      this(arrayType, elemDeser, (TypeDeserializer)null);
   }

   public ArrayDeserializer(ArrayType arrayType, JsonDeserializer<Object> elemDeser, TypeDeserializer elemTypeDeser) {
      super(Object[].class);
      this._arrayType = arrayType;
      this._elementClass = arrayType.getContentType().getRawClass();
      this._untyped = this._elementClass == Object.class;
      this._elementDeserializer = elemDeser;
      this._elementTypeDeserializer = elemTypeDeser;
   }

   public JavaType getContentType() {
      return this._arrayType.getContentType();
   }

   public JsonDeserializer<Object> getContentDeserializer() {
      return this._elementDeserializer;
   }

   public Object[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (!jp.isExpectedStartArrayToken()) {
         return this.handleNonArray(jp, ctxt);
      } else {
         ObjectBuffer buffer = ctxt.leaseObjectBuffer();
         Object[] chunk = buffer.resetAndStart();
         int ix = 0;

         JsonToken t;
         Object value;
         for(TypeDeserializer typeDeser = this._elementTypeDeserializer; (t = jp.nextToken()) != JsonToken.END_ARRAY; chunk[ix++] = value) {
            if (t == JsonToken.VALUE_NULL) {
               value = null;
            } else if (typeDeser == null) {
               value = this._elementDeserializer.deserialize(jp, ctxt);
            } else {
               value = this._elementDeserializer.deserializeWithType(jp, ctxt, typeDeser);
            }

            if (ix >= chunk.length) {
               chunk = buffer.appendCompletedChunk(chunk);
               ix = 0;
            }
         }

         Object[] result;
         if (this._untyped) {
            result = buffer.completeAndClearBuffer(chunk, ix);
         } else {
            result = buffer.completeAndClearBuffer(chunk, ix, this._elementClass);
         }

         ctxt.returnObjectBuffer(buffer);
         return result;
      }
   }

   public Object[] deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return (Object[])((Object[])typeDeserializer.deserializeTypedFromArray(jp, ctxt));
   }

   protected Byte[] deserializeFromBase64(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      byte[] b = jp.getBinaryValue(ctxt.getBase64Variant());
      Byte[] result = new Byte[b.length];
      int i = 0;

      for(int len = b.length; i < len; ++i) {
         result[i] = b[i];
      }

      return result;
   }

   private final Object[] handleNonArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
         if (jp.getCurrentToken() == JsonToken.VALUE_STRING && this._elementClass == Byte.class) {
            return this.deserializeFromBase64(jp, ctxt);
         } else {
            throw ctxt.mappingException(this._arrayType.getRawClass());
         }
      } else {
         JsonToken t = jp.getCurrentToken();
         Object value;
         if (t == JsonToken.VALUE_NULL) {
            value = null;
         } else if (this._elementTypeDeserializer == null) {
            value = this._elementDeserializer.deserialize(jp, ctxt);
         } else {
            value = this._elementDeserializer.deserializeWithType(jp, ctxt, this._elementTypeDeserializer);
         }

         Object[] result;
         if (this._untyped) {
            result = new Object[1];
         } else {
            result = (Object[])((Object[])Array.newInstance(this._elementClass, 1));
         }

         result[0] = value;
         return result;
      }
   }
}
