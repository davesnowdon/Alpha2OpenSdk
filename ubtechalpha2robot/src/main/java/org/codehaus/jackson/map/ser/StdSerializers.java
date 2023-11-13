package org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializable;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.schema.JsonSerializableSchema;
import org.codehaus.jackson.util.TokenBuffer;

public class StdSerializers {
   protected StdSerializers() {
   }

   @JacksonStdImpl
   public static final class TokenBufferSerializer extends SerializerBase<TokenBuffer> {
      public TokenBufferSerializer() {
         super(TokenBuffer.class);
      }

      public void serialize(TokenBuffer value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         value.serialize(jgen);
      }

      public final void serializeWithType(TokenBuffer value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
         typeSer.writeTypePrefixForScalar(value, jgen);
         this.serialize(value, jgen, provider);
         typeSer.writeTypeSuffixForScalar(value, jgen);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("any", true);
      }
   }

   @JacksonStdImpl
   public static final class SerializableWithTypeSerializer extends SerializerBase<JsonSerializableWithType> {
      protected static final StdSerializers.SerializableWithTypeSerializer instance = new StdSerializers.SerializableWithTypeSerializer();

      private SerializableWithTypeSerializer() {
         super(JsonSerializableWithType.class);
      }

      public void serialize(JsonSerializableWithType value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         value.serialize(jgen, provider);
      }

      public final void serializeWithType(JsonSerializableWithType value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
         value.serializeWithType(jgen, provider, typeSer);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
         ObjectNode objectNode = this.createObjectNode();
         String schemaType = "any";
         String objectProperties = null;
         String itemDefinition = null;
         if (typeHint != null) {
            Class<?> rawClass = TypeFactory.rawClass(typeHint);
            if (rawClass.isAnnotationPresent(JsonSerializableSchema.class)) {
               JsonSerializableSchema schemaInfo = (JsonSerializableSchema)rawClass.getAnnotation(JsonSerializableSchema.class);
               schemaType = schemaInfo.schemaType();
               if (!"##irrelevant".equals(schemaInfo.schemaObjectPropertiesDefinition())) {
                  objectProperties = schemaInfo.schemaObjectPropertiesDefinition();
               }

               if (!"##irrelevant".equals(schemaInfo.schemaItemDefinition())) {
                  itemDefinition = schemaInfo.schemaItemDefinition();
               }
            }
         }

         objectNode.put("type", schemaType);
         if (objectProperties != null) {
            try {
               objectNode.put("properties", (JsonNode)(new ObjectMapper()).readValue(objectProperties, JsonNode.class));
            } catch (IOException var10) {
               throw new IllegalStateException(var10);
            }
         }

         if (itemDefinition != null) {
            try {
               objectNode.put("items", (JsonNode)(new ObjectMapper()).readValue(itemDefinition, JsonNode.class));
            } catch (IOException var9) {
               throw new IllegalStateException(var9);
            }
         }

         return objectNode;
      }
   }

   @JacksonStdImpl
   public static final class SerializableSerializer extends SerializerBase<JsonSerializable> {
      protected static final StdSerializers.SerializableSerializer instance = new StdSerializers.SerializableSerializer();

      private SerializableSerializer() {
         super(JsonSerializable.class);
      }

      public void serialize(JsonSerializable value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         value.serialize(jgen, provider);
      }

      public final void serializeWithType(JsonSerializable value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
         if (value instanceof JsonSerializableWithType) {
            ((JsonSerializableWithType)value).serializeWithType(jgen, provider, typeSer);
         } else {
            this.serialize(value, jgen, provider);
         }

      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
         ObjectNode objectNode = this.createObjectNode();
         String schemaType = "any";
         String objectProperties = null;
         String itemDefinition = null;
         if (typeHint != null) {
            Class<?> rawClass = TypeFactory.type(typeHint).getRawClass();
            if (rawClass.isAnnotationPresent(JsonSerializableSchema.class)) {
               JsonSerializableSchema schemaInfo = (JsonSerializableSchema)rawClass.getAnnotation(JsonSerializableSchema.class);
               schemaType = schemaInfo.schemaType();
               if (!"##irrelevant".equals(schemaInfo.schemaObjectPropertiesDefinition())) {
                  objectProperties = schemaInfo.schemaObjectPropertiesDefinition();
               }

               if (!"##irrelevant".equals(schemaInfo.schemaItemDefinition())) {
                  itemDefinition = schemaInfo.schemaItemDefinition();
               }
            }
         }

         objectNode.put("type", schemaType);
         if (objectProperties != null) {
            try {
               objectNode.put("properties", (JsonNode)(new ObjectMapper()).readValue(objectProperties, JsonNode.class));
            } catch (IOException var10) {
               throw new IllegalStateException(var10);
            }
         }

         if (itemDefinition != null) {
            try {
               objectNode.put("items", (JsonNode)(new ObjectMapper()).readValue(itemDefinition, JsonNode.class));
            } catch (IOException var9) {
               throw new IllegalStateException(var9);
            }
         }

         return objectNode;
      }
   }

   @JacksonStdImpl
   public static final class SqlTimeSerializer extends ScalarSerializerBase<Time> {
      public SqlTimeSerializer() {
         super(Time.class);
      }

      public void serialize(Time value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeString(value.toString());
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("string", true);
      }
   }

   @JacksonStdImpl
   public static final class SqlDateSerializer extends ScalarSerializerBase<Date> {
      public SqlDateSerializer() {
         super(Date.class);
      }

      public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeString(value.toString());
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("string", true);
      }
   }

   @JacksonStdImpl
   public static final class UtilDateSerializer extends ScalarSerializerBase<java.util.Date> {
      public static final StdSerializers.UtilDateSerializer instance = new StdSerializers.UtilDateSerializer();

      public UtilDateSerializer() {
         super(java.util.Date.class);
      }

      public void serialize(java.util.Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         provider.defaultSerializeDateValue(value, jgen);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS) ? "number" : "string", true);
      }
   }

   @JacksonStdImpl
   public static final class CalendarSerializer extends ScalarSerializerBase<Calendar> {
      public static final StdSerializers.CalendarSerializer instance = new StdSerializers.CalendarSerializer();

      public CalendarSerializer() {
         super(Calendar.class);
      }

      public void serialize(Calendar value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         provider.defaultSerializeDateValue(value.getTimeInMillis(), jgen);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS) ? "number" : "string", true);
      }
   }

   @JacksonStdImpl
   public static final class NumberSerializer extends ScalarSerializerBase<Number> {
      public static final StdSerializers.NumberSerializer instance = new StdSerializers.NumberSerializer();

      public NumberSerializer() {
         super(Number.class);
      }

      public void serialize(Number value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         if (value instanceof BigDecimal) {
            jgen.writeNumber((BigDecimal)value);
         } else if (value instanceof BigInteger) {
            jgen.writeNumber((BigInteger)value);
         } else if (value instanceof Double) {
            jgen.writeNumber((Double)value);
         } else if (value instanceof Float) {
            jgen.writeNumber((Float)value);
         } else {
            jgen.writeNumber(value.toString());
         }

      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("number", true);
      }
   }

   @JacksonStdImpl
   public static final class DoubleSerializer extends StdSerializers.NonTypedScalarSerializer<Double> {
      static final StdSerializers.DoubleSerializer instance = new StdSerializers.DoubleSerializer();

      public DoubleSerializer() {
         super(Double.class);
      }

      public void serialize(Double value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeNumber(value);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("number", true);
      }
   }

   @JacksonStdImpl
   public static final class FloatSerializer extends ScalarSerializerBase<Float> {
      static final StdSerializers.FloatSerializer instance = new StdSerializers.FloatSerializer();

      public FloatSerializer() {
         super(Float.class);
      }

      public void serialize(Float value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeNumber(value);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("number", true);
      }
   }

   @JacksonStdImpl
   public static final class LongSerializer extends ScalarSerializerBase<Long> {
      static final StdSerializers.LongSerializer instance = new StdSerializers.LongSerializer();

      public LongSerializer() {
         super(Long.class);
      }

      public void serialize(Long value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeNumber(value);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("number", true);
      }
   }

   @JacksonStdImpl
   public static final class IntLikeSerializer extends ScalarSerializerBase<Number> {
      static final StdSerializers.IntLikeSerializer instance = new StdSerializers.IntLikeSerializer();

      public IntLikeSerializer() {
         super(Number.class);
      }

      public void serialize(Number value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeNumber(value.intValue());
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("integer", true);
      }
   }

   @JacksonStdImpl
   public static final class IntegerSerializer extends StdSerializers.NonTypedScalarSerializer<Integer> {
      public IntegerSerializer() {
         super(Integer.class);
      }

      public void serialize(Integer value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeNumber(value);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("integer", true);
      }
   }

   @JacksonStdImpl
   public static final class StringSerializer extends StdSerializers.NonTypedScalarSerializer<String> {
      public StringSerializer() {
         super(String.class);
      }

      public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeString(value);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("string", true);
      }
   }

   @JacksonStdImpl
   public static final class BooleanSerializer extends StdSerializers.NonTypedScalarSerializer<Boolean> {
      final boolean _forPrimitive;

      public BooleanSerializer(boolean forPrimitive) {
         super(Boolean.class);
         this._forPrimitive = forPrimitive;
      }

      public void serialize(Boolean value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         jgen.writeBoolean(value);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("boolean", !this._forPrimitive);
      }
   }

   protected abstract static class NonTypedScalarSerializer extends ScalarSerializerBase {
      protected NonTypedScalarSerializer(Class t) {
         super(t);
      }

      public final void serializeWithType(Object value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
         this.serialize(value, jgen, provider);
      }
   }
}
