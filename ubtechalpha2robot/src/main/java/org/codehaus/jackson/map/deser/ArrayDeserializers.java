package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.util.ObjectBuffer;
import org.codehaus.jackson.type.JavaType;

public class ArrayDeserializers {
   HashMap<JavaType, JsonDeserializer<Object>> _allDeserializers = new HashMap();
   static final ArrayDeserializers instance = new ArrayDeserializers();

   private ArrayDeserializers() {
      this.add(Boolean.TYPE, new ArrayDeserializers.BooleanDeser());
      this.add(Byte.TYPE, new ArrayDeserializers.ByteDeser());
      this.add(Short.TYPE, new ArrayDeserializers.ShortDeser());
      this.add(Integer.TYPE, new ArrayDeserializers.IntDeser());
      this.add(Long.TYPE, new ArrayDeserializers.LongDeser());
      this.add(Float.TYPE, new ArrayDeserializers.FloatDeser());
      this.add(Double.TYPE, new ArrayDeserializers.DoubleDeser());
      this.add(String.class, new ArrayDeserializers.StringDeser());
      this.add(Character.TYPE, new ArrayDeserializers.CharDeser());
   }

   public static HashMap<JavaType, JsonDeserializer<Object>> getAll() {
      return instance._allDeserializers;
   }

   private void add(Class<?> cls, JsonDeserializer<?> deser) {
      this._allDeserializers.put(TypeFactory.defaultInstance().constructType((Type)cls), deser);
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
   }

   @JacksonStdImpl
   static final class DoubleDeser extends ArrayDeserializers.ArrayDeser<double[]> {
      public DoubleDeser() {
         super(double[].class);
      }

      public double[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!jp.isExpectedStartArrayToken()) {
            return this.handleNonArray(jp, ctxt);
         } else {
            ArrayBuilders.DoubleBuilder builder = ctxt.getArrayBuilders().getDoubleBuilder();
            double[] chunk = (double[])builder.resetAndStart();

            int ix;
            double value;
            for(ix = 0; jp.nextToken() != JsonToken.END_ARRAY; chunk[ix++] = value) {
               value = this._parseDoublePrimitive(jp, ctxt);
               if (ix >= chunk.length) {
                  chunk = (double[])builder.appendCompletedChunk(chunk, ix);
                  ix = 0;
               }
            }

            return (double[])builder.completeAndClearBuffer(chunk, ix);
         }
      }

      private final double[] handleNonArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(this._valueClass);
         } else {
            return new double[]{this._parseDoublePrimitive(jp, ctxt)};
         }
      }
   }

   @JacksonStdImpl
   static final class FloatDeser extends ArrayDeserializers.ArrayDeser<float[]> {
      public FloatDeser() {
         super(float[].class);
      }

      public float[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!jp.isExpectedStartArrayToken()) {
            return this.handleNonArray(jp, ctxt);
         } else {
            ArrayBuilders.FloatBuilder builder = ctxt.getArrayBuilders().getFloatBuilder();
            float[] chunk = (float[])builder.resetAndStart();

            int ix;
            float value;
            for(ix = 0; jp.nextToken() != JsonToken.END_ARRAY; chunk[ix++] = value) {
               value = this._parseFloatPrimitive(jp, ctxt);
               if (ix >= chunk.length) {
                  chunk = (float[])builder.appendCompletedChunk(chunk, ix);
                  ix = 0;
               }
            }

            return (float[])builder.completeAndClearBuffer(chunk, ix);
         }
      }

      private final float[] handleNonArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(this._valueClass);
         } else {
            return new float[]{this._parseFloatPrimitive(jp, ctxt)};
         }
      }
   }

   @JacksonStdImpl
   static final class LongDeser extends ArrayDeserializers.ArrayDeser<long[]> {
      public LongDeser() {
         super(long[].class);
      }

      public long[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!jp.isExpectedStartArrayToken()) {
            return this.handleNonArray(jp, ctxt);
         } else {
            ArrayBuilders.LongBuilder builder = ctxt.getArrayBuilders().getLongBuilder();
            long[] chunk = (long[])builder.resetAndStart();

            int ix;
            long value;
            for(ix = 0; jp.nextToken() != JsonToken.END_ARRAY; chunk[ix++] = value) {
               value = this._parseLongPrimitive(jp, ctxt);
               if (ix >= chunk.length) {
                  chunk = (long[])builder.appendCompletedChunk(chunk, ix);
                  ix = 0;
               }
            }

            return (long[])builder.completeAndClearBuffer(chunk, ix);
         }
      }

      private final long[] handleNonArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(this._valueClass);
         } else {
            return new long[]{this._parseLongPrimitive(jp, ctxt)};
         }
      }
   }

   @JacksonStdImpl
   static final class IntDeser extends ArrayDeserializers.ArrayDeser<int[]> {
      public IntDeser() {
         super(int[].class);
      }

      public int[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!jp.isExpectedStartArrayToken()) {
            return this.handleNonArray(jp, ctxt);
         } else {
            ArrayBuilders.IntBuilder builder = ctxt.getArrayBuilders().getIntBuilder();
            int[] chunk = (int[])builder.resetAndStart();

            int ix;
            int value;
            for(ix = 0; jp.nextToken() != JsonToken.END_ARRAY; chunk[ix++] = value) {
               value = this._parseIntPrimitive(jp, ctxt);
               if (ix >= chunk.length) {
                  chunk = (int[])builder.appendCompletedChunk(chunk, ix);
                  ix = 0;
               }
            }

            return (int[])builder.completeAndClearBuffer(chunk, ix);
         }
      }

      private final int[] handleNonArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(this._valueClass);
         } else {
            return new int[]{this._parseIntPrimitive(jp, ctxt)};
         }
      }
   }

   @JacksonStdImpl
   static final class ShortDeser extends ArrayDeserializers.ArrayDeser<short[]> {
      public ShortDeser() {
         super(short[].class);
      }

      public short[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!jp.isExpectedStartArrayToken()) {
            return this.handleNonArray(jp, ctxt);
         } else {
            ArrayBuilders.ShortBuilder builder = ctxt.getArrayBuilders().getShortBuilder();
            short[] chunk = (short[])builder.resetAndStart();

            int ix;
            short value;
            for(ix = 0; jp.nextToken() != JsonToken.END_ARRAY; chunk[ix++] = value) {
               value = this._parseShortPrimitive(jp, ctxt);
               if (ix >= chunk.length) {
                  chunk = (short[])builder.appendCompletedChunk(chunk, ix);
                  ix = 0;
               }
            }

            return (short[])builder.completeAndClearBuffer(chunk, ix);
         }
      }

      private final short[] handleNonArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(this._valueClass);
         } else {
            return new short[]{this._parseShortPrimitive(jp, ctxt)};
         }
      }
   }

   @JacksonStdImpl
   static final class ByteDeser extends ArrayDeserializers.ArrayDeser<byte[]> {
      public ByteDeser() {
         super(byte[].class);
      }

      public byte[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         JsonToken t = jp.getCurrentToken();
         if (t == JsonToken.VALUE_STRING) {
            return jp.getBinaryValue(ctxt.getBase64Variant());
         } else {
            if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
               Object ob = jp.getEmbeddedObject();
               if (ob == null) {
                  return null;
               }

               if (ob instanceof byte[]) {
                  return (byte[])((byte[])ob);
               }
            }

            if (!jp.isExpectedStartArrayToken()) {
               return this.handleNonArray(jp, ctxt);
            } else {
               ArrayBuilders.ByteBuilder builder = ctxt.getArrayBuilders().getByteBuilder();
               byte[] chunk = (byte[])builder.resetAndStart();

               int ix;
               byte value;
               for(ix = 0; (t = jp.nextToken()) != JsonToken.END_ARRAY; chunk[ix++] = value) {
                  if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
                     if (t != JsonToken.VALUE_NULL) {
                        throw ctxt.mappingException(this._valueClass.getComponentType());
                     }

                     value = 0;
                  } else {
                     value = jp.getByteValue();
                  }

                  if (ix >= chunk.length) {
                     chunk = (byte[])builder.appendCompletedChunk(chunk, ix);
                     ix = 0;
                  }
               }

               return (byte[])builder.completeAndClearBuffer(chunk, ix);
            }
         }
      }

      private final byte[] handleNonArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(this._valueClass);
         } else {
            JsonToken t = jp.getCurrentToken();
            byte value;
            if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
               if (t != JsonToken.VALUE_NULL) {
                  throw ctxt.mappingException(this._valueClass.getComponentType());
               }

               value = 0;
            } else {
               value = jp.getByteValue();
            }

            return new byte[]{value};
         }
      }
   }

   @JacksonStdImpl
   static final class BooleanDeser extends ArrayDeserializers.ArrayDeser<boolean[]> {
      public BooleanDeser() {
         super(boolean[].class);
      }

      public boolean[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!jp.isExpectedStartArrayToken()) {
            return this.handleNonArray(jp, ctxt);
         } else {
            ArrayBuilders.BooleanBuilder builder = ctxt.getArrayBuilders().getBooleanBuilder();
            boolean[] chunk = (boolean[])builder.resetAndStart();

            int ix;
            boolean value;
            for(ix = 0; jp.nextToken() != JsonToken.END_ARRAY; chunk[ix++] = value) {
               value = this._parseBooleanPrimitive(jp, ctxt);
               if (ix >= chunk.length) {
                  chunk = (boolean[])builder.appendCompletedChunk(chunk, ix);
                  ix = 0;
               }
            }

            return (boolean[])builder.completeAndClearBuffer(chunk, ix);
         }
      }

      private final boolean[] handleNonArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(this._valueClass);
         } else {
            return new boolean[]{this._parseBooleanPrimitive(jp, ctxt)};
         }
      }
   }

   @JacksonStdImpl
   static final class CharDeser extends ArrayDeserializers.ArrayDeser<char[]> {
      public CharDeser() {
         super(char[].class);
      }

      public char[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         JsonToken t = jp.getCurrentToken();
         if (t == JsonToken.VALUE_STRING) {
            char[] buffer = jp.getTextCharacters();
            int offset = jp.getTextOffset();
            int len = jp.getTextLength();
            char[] result = new char[len];
            System.arraycopy(buffer, offset, result, 0, len);
            return result;
         } else if (jp.isExpectedStartArrayToken()) {
            StringBuilder sb = new StringBuilder(64);

            while((t = jp.nextToken()) != JsonToken.END_ARRAY) {
               if (t != JsonToken.VALUE_STRING) {
                  throw ctxt.mappingException(Character.TYPE);
               }

               String str = jp.getText();
               if (str.length() != 1) {
                  throw JsonMappingException.from(jp, "Can not convert a JSON String of length " + str.length() + " into a char element of char array");
               }

               sb.append(str.charAt(0));
            }

            return sb.toString().toCharArray();
         } else {
            if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
               Object ob = jp.getEmbeddedObject();
               if (ob == null) {
                  return null;
               }

               if (ob instanceof char[]) {
                  return (char[])((char[])ob);
               }

               if (ob instanceof String) {
                  return ((String)ob).toCharArray();
               }

               if (ob instanceof byte[]) {
                  return Base64Variants.getDefaultVariant().encode((byte[])((byte[])ob), false).toCharArray();
               }
            }

            throw ctxt.mappingException(this._valueClass);
         }
      }
   }

   @JacksonStdImpl
   static final class StringDeser extends ArrayDeserializers.ArrayDeser<String[]> {
      public StringDeser() {
         super(String[].class);
      }

      public String[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!jp.isExpectedStartArrayToken()) {
            return this.handleNonArray(jp, ctxt);
         } else {
            ObjectBuffer buffer = ctxt.leaseObjectBuffer();
            Object[] chunk = buffer.resetAndStart();

            int ix;
            JsonToken t;
            String value;
            for(ix = 0; (t = jp.nextToken()) != JsonToken.END_ARRAY; chunk[ix++] = value) {
               value = t == JsonToken.VALUE_NULL ? null : jp.getText();
               if (ix >= chunk.length) {
                  chunk = buffer.appendCompletedChunk(chunk);
                  ix = 0;
               }
            }

            String[] result = (String[])buffer.completeAndClearBuffer(chunk, ix, String.class);
            ctxt.returnObjectBuffer(buffer);
            return result;
         }
      }

      private final String[] handleNonArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(this._valueClass);
         } else {
            return new String[]{jp.getCurrentToken() == JsonToken.VALUE_NULL ? null : jp.getText()};
         }
      }
   }

   abstract static class ArrayDeser extends StdDeserializer {
      protected ArrayDeser(Class cls) {
         super(cls);
      }

      public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
         return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
      }
   }
}
