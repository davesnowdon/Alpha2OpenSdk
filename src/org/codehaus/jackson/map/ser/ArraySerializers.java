package org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ResolvableSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.node.ObjectNode;

public final class ArraySerializers {
   private ArraySerializers() {
   }

   @JacksonStdImpl
   public static final class DoubleArraySerializer extends ArraySerializers.AsArraySerializer<double[]> {
      public DoubleArraySerializer() {
         super(double[].class, (TypeSerializer)null, (BeanProperty)null);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return this;
      }

      public void serializeContents(double[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            jgen.writeNumber(value[i]);
         }

      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         o.put("items", (JsonNode)this.createSchemaNode("number"));
         return o;
      }
   }

   @JacksonStdImpl
   public static final class FloatArraySerializer extends ArraySerializers.AsArraySerializer<float[]> {
      public FloatArraySerializer() {
         this((TypeSerializer)null);
      }

      public FloatArraySerializer(TypeSerializer vts) {
         super(float[].class, vts, (BeanProperty)null);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return new ArraySerializers.FloatArraySerializer(vts);
      }

      public void serializeContents(float[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            jgen.writeNumber(value[i]);
         }

      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         o.put("items", (JsonNode)this.createSchemaNode("number"));
         return o;
      }
   }

   @JacksonStdImpl
   public static final class LongArraySerializer extends ArraySerializers.AsArraySerializer<long[]> {
      public LongArraySerializer() {
         this((TypeSerializer)null);
      }

      public LongArraySerializer(TypeSerializer vts) {
         super(long[].class, vts, (BeanProperty)null);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return new ArraySerializers.LongArraySerializer(vts);
      }

      public void serializeContents(long[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            jgen.writeNumber(value[i]);
         }

      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         o.put("items", (JsonNode)this.createSchemaNode("number", true));
         return o;
      }
   }

   @JacksonStdImpl
   public static final class IntArraySerializer extends ArraySerializers.AsArraySerializer<int[]> {
      public IntArraySerializer() {
         super(int[].class, (TypeSerializer)null, (BeanProperty)null);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return this;
      }

      public void serializeContents(int[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            jgen.writeNumber(value[i]);
         }

      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         o.put("items", (JsonNode)this.createSchemaNode("integer"));
         return o;
      }
   }

   @JacksonStdImpl
   public static final class CharArraySerializer extends SerializerBase<char[]> {
      public CharArraySerializer() {
         super(char[].class);
      }

      public void serialize(char[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         if (provider.isEnabled(SerializationConfig.Feature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)) {
            jgen.writeStartArray();
            this._writeArrayContents(jgen, value);
            jgen.writeEndArray();
         } else {
            jgen.writeString(value, 0, value.length);
         }

      }

      public void serializeWithType(char[] value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
         if (provider.isEnabled(SerializationConfig.Feature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)) {
            typeSer.writeTypePrefixForArray(value, jgen);
            this._writeArrayContents(jgen, value);
            typeSer.writeTypeSuffixForArray(value, jgen);
         } else {
            typeSer.writeTypePrefixForScalar(value, jgen);
            jgen.writeString(value, 0, value.length);
            typeSer.writeTypeSuffixForScalar(value, jgen);
         }

      }

      private final void _writeArrayContents(JsonGenerator jgen, char[] value) throws IOException, JsonGenerationException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            jgen.writeString(value, i, 1);
         }

      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         ObjectNode itemSchema = this.createSchemaNode("string");
         itemSchema.put("type", "string");
         o.put("items", (JsonNode)itemSchema);
         return o;
      }
   }

   @JacksonStdImpl
   public static final class ShortArraySerializer extends ArraySerializers.AsArraySerializer<short[]> {
      public ShortArraySerializer() {
         this((TypeSerializer)null);
      }

      public ShortArraySerializer(TypeSerializer vts) {
         super(short[].class, vts, (BeanProperty)null);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return new ArraySerializers.ShortArraySerializer(vts);
      }

      public void serializeContents(short[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            jgen.writeNumber(value[i]);
         }

      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         o.put("items", (JsonNode)this.createSchemaNode("integer"));
         return o;
      }
   }

   @JacksonStdImpl
   public static final class ByteArraySerializer extends SerializerBase<byte[]> {
      public ByteArraySerializer() {
         super(byte[].class);
      }

      public void serialize(byte[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeBinary(value);
      }

      public void serializeWithType(byte[] value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
         typeSer.writeTypePrefixForScalar(value, jgen);
         jgen.writeBinary(value);
         typeSer.writeTypeSuffixForScalar(value, jgen);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         ObjectNode itemSchema = this.createSchemaNode("string");
         o.put("items", (JsonNode)itemSchema);
         return o;
      }
   }

   @JacksonStdImpl
   public static final class BooleanArraySerializer extends ArraySerializers.AsArraySerializer<boolean[]> {
      public BooleanArraySerializer() {
         super(boolean[].class, (TypeSerializer)null, (BeanProperty)null);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return this;
      }

      public void serializeContents(boolean[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            jgen.writeBoolean(value[i]);
         }

      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         o.put("items", (JsonNode)this.createSchemaNode("boolean"));
         return o;
      }
   }

   @JacksonStdImpl
   public static final class StringArraySerializer extends ArraySerializers.AsArraySerializer<String[]> implements ResolvableSerializer {
      protected JsonSerializer<Object> _elementSerializer;

      public StringArraySerializer(BeanProperty prop) {
         super(String[].class, (TypeSerializer)null, prop);
      }

      public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
         return this;
      }

      public void serializeContents(String[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         int len = value.length;
         if (len != 0) {
            if (this._elementSerializer != null) {
               this.serializeContentsSlow(value, jgen, provider, this._elementSerializer);
            } else {
               for(int i = 0; i < len; ++i) {
                  String str = value[i];
                  if (str == null) {
                     jgen.writeNull();
                  } else {
                     jgen.writeString(value[i]);
                  }
               }

            }
         }
      }

      private void serializeContentsSlow(String[] value, JsonGenerator jgen, SerializerProvider provider, JsonSerializer<Object> ser) throws IOException, JsonGenerationException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            String str = value[i];
            if (str == null) {
               provider.defaultSerializeNull(jgen);
            } else {
               ser.serialize(value[i], jgen, provider);
            }
         }

      }

      public void resolve(SerializerProvider provider) throws JsonMappingException {
         JsonSerializer<Object> ser = provider.findValueSerializer(String.class, this._property);
         if (ser != null && ser.getClass().getAnnotation(JacksonStdImpl.class) == null) {
            this._elementSerializer = ser;
         }

      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         o.put("items", (JsonNode)this.createSchemaNode("string"));
         return o;
      }
   }

   public abstract static class AsArraySerializer<T> extends ContainerSerializerBase<T> {
      protected final TypeSerializer _valueTypeSerializer;
      protected final BeanProperty _property;

      protected AsArraySerializer(Class<T> cls, TypeSerializer vts, BeanProperty property) {
         super(cls);
         this._valueTypeSerializer = vts;
         this._property = property;
      }

      public final void serialize(T value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeStartArray();
         this.serializeContents(value, jgen, provider);
         jgen.writeEndArray();
      }

      public final void serializeWithType(T value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
         typeSer.writeTypePrefixForArray(value, jgen);
         this.serializeContents(value, jgen, provider);
         typeSer.writeTypeSuffixForArray(value, jgen);
      }

      protected abstract void serializeContents(T var1, JsonGenerator var2, SerializerProvider var3) throws IOException, JsonGenerationException;
   }
}
