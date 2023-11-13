package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.io.NumberInput;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ResolvableDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.util.TokenBuffer;

public abstract class StdDeserializer extends JsonDeserializer {
   protected final Class _valueClass;

   protected StdDeserializer(Class vc) {
      this._valueClass = vc;
   }

   protected StdDeserializer(JavaType valueType) {
      this._valueClass = valueType == null ? null : valueType.getRawClass();
   }

   public Class getValueClass() {
      return this._valueClass;
   }

   public JavaType getValueType() {
      return null;
   }

   protected boolean isDefaultSerializer(JsonDeserializer<?> deserializer) {
      return deserializer != null && deserializer.getClass().getAnnotation(JacksonStdImpl.class) != null;
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
   }

   protected final boolean _parseBooleanPrimitive(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.VALUE_TRUE) {
         return true;
      } else if (t == JsonToken.VALUE_FALSE) {
         return false;
      } else if (t == JsonToken.VALUE_NULL) {
         return false;
      } else if (t == JsonToken.VALUE_NUMBER_INT) {
         return jp.getIntValue() != 0;
      } else if (t == JsonToken.VALUE_STRING) {
         String text = jp.getText().trim();
         if ("true".equals(text)) {
            return true;
         } else if (!"false".equals(text) && text.length() != 0) {
            throw ctxt.weirdStringException(this._valueClass, "only \"true\" or \"false\" recognized");
         } else {
            return Boolean.FALSE;
         }
      } else {
         throw ctxt.mappingException(this._valueClass);
      }
   }

   protected final Boolean _parseBoolean(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.VALUE_TRUE) {
         return Boolean.TRUE;
      } else if (t == JsonToken.VALUE_FALSE) {
         return Boolean.FALSE;
      } else if (t == JsonToken.VALUE_NULL) {
         return null;
      } else if (t == JsonToken.VALUE_NUMBER_INT) {
         return jp.getIntValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
      } else if (t == JsonToken.VALUE_STRING) {
         String text = jp.getText().trim();
         if ("true".equals(text)) {
            return Boolean.TRUE;
         } else if (!"false".equals(text) && text.length() != 0) {
            throw ctxt.weirdStringException(this._valueClass, "only \"true\" or \"false\" recognized");
         } else {
            return Boolean.FALSE;
         }
      } else {
         throw ctxt.mappingException(this._valueClass);
      }
   }

   protected final Short _parseShort(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.VALUE_NULL) {
         return null;
      } else if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
         int value = this._parseIntPrimitive(jp, ctxt);
         if (value >= -32768 && value <= 32767) {
            return (short)value;
         } else {
            throw ctxt.weirdStringException(this._valueClass, "overflow, value can not be represented as 16-bit value");
         }
      } else {
         return jp.getShortValue();
      }
   }

   protected final short _parseShortPrimitive(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      int value = this._parseIntPrimitive(jp, ctxt);
      if (value >= -32768 && value <= 32767) {
         return (short)value;
      } else {
         throw ctxt.weirdStringException(this._valueClass, "overflow, value can not be represented as 16-bit value");
      }
   }

   protected final int _parseIntPrimitive(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
         if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();

            try {
               int len = text.length();
               if (len > 9) {
                  long l = Long.parseLong(text);
                  if (l >= -2147483648L && l <= 2147483647L) {
                     return (int)l;
                  } else {
                     throw ctxt.weirdStringException(this._valueClass, "Overflow: numeric value (" + text + ") out of range of int (" + -2147483648 + " - " + 2147483647 + ")");
                  }
               } else {
                  return len == 0 ? 0 : NumberInput.parseInt(text);
               }
            } catch (IllegalArgumentException var8) {
               throw ctxt.weirdStringException(this._valueClass, "not a valid int value");
            }
         } else if (t == JsonToken.VALUE_NULL) {
            return 0;
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      } else {
         return jp.getIntValue();
      }
   }

   protected final Integer _parseInteger(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
         if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();

            try {
               int len = text.length();
               if (len > 9) {
                  long l = Long.parseLong(text);
                  if (l >= -2147483648L && l <= 2147483647L) {
                     return (int)l;
                  } else {
                     throw ctxt.weirdStringException(this._valueClass, "Overflow: numeric value (" + text + ") out of range of Integer (" + -2147483648 + " - " + 2147483647 + ")");
                  }
               } else {
                  return len == 0 ? null : NumberInput.parseInt(text);
               }
            } catch (IllegalArgumentException var8) {
               throw ctxt.weirdStringException(this._valueClass, "not a valid Integer value");
            }
         } else if (t == JsonToken.VALUE_NULL) {
            return null;
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      } else {
         return jp.getIntValue();
      }
   }

   protected final Long _parseLong(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
         if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
               return null;
            } else {
               try {
                  return NumberInput.parseLong(text);
               } catch (IllegalArgumentException var6) {
                  throw ctxt.weirdStringException(this._valueClass, "not a valid Long value");
               }
            }
         } else if (t == JsonToken.VALUE_NULL) {
            return null;
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      } else {
         return jp.getLongValue();
      }
   }

   protected final long _parseLongPrimitive(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
         if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
               return 0L;
            } else {
               try {
                  return NumberInput.parseLong(text);
               } catch (IllegalArgumentException var6) {
                  throw ctxt.weirdStringException(this._valueClass, "not a valid long value");
               }
            }
         } else if (t == JsonToken.VALUE_NULL) {
            return 0L;
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      } else {
         return jp.getLongValue();
      }
   }

   protected final Float _parseFloat(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
         if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
               return null;
            } else {
               switch(text.charAt(0)) {
               case '-':
                  if (!"-Infinity".equals(text) && !"-INF".equals(text)) {
                     break;
                  }

                  return -1.0F / 0.0;
               case 'I':
                  if ("Infinity".equals(text) || "INF".equals(text)) {
                     return 1.0F / 0.0;
                  }
                  break;
               case 'N':
                  if ("NaN".equals(text)) {
                     return 0.0F / 0.0;
                  }
               }

               try {
                  return Float.parseFloat(text);
               } catch (IllegalArgumentException var6) {
                  throw ctxt.weirdStringException(this._valueClass, "not a valid Float value");
               }
            }
         } else if (t == JsonToken.VALUE_NULL) {
            return null;
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      } else {
         return jp.getFloatValue();
      }
   }

   protected final float _parseFloatPrimitive(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
         if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
               return 0.0F;
            } else {
               switch(text.charAt(0)) {
               case '-':
                  if (!"-Infinity".equals(text) && !"-INF".equals(text)) {
                     break;
                  }

                  return -1.0F / 0.0;
               case 'I':
                  if ("Infinity".equals(text) || "INF".equals(text)) {
                     return 1.0F / 0.0;
                  }
                  break;
               case 'N':
                  if ("NaN".equals(text)) {
                     return 0.0F / 0.0;
                  }
               }

               try {
                  return Float.parseFloat(text);
               } catch (IllegalArgumentException var6) {
                  throw ctxt.weirdStringException(this._valueClass, "not a valid float value");
               }
            }
         } else if (t == JsonToken.VALUE_NULL) {
            return 0.0F;
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      } else {
         return jp.getFloatValue();
      }
   }

   protected final Double _parseDouble(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
         if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
               return null;
            } else {
               switch(text.charAt(0)) {
               case '-':
                  if (!"-Infinity".equals(text) && !"-INF".equals(text)) {
                     break;
                  }

                  return -1.0D / 0.0;
               case 'I':
                  if ("Infinity".equals(text) || "INF".equals(text)) {
                     return 1.0D / 0.0;
                  }
                  break;
               case 'N':
                  if ("NaN".equals(text)) {
                     return 0.0D / 0.0;
                  }
               }

               try {
                  return parseDouble(text);
               } catch (IllegalArgumentException var6) {
                  throw ctxt.weirdStringException(this._valueClass, "not a valid Double value");
               }
            }
         } else if (t == JsonToken.VALUE_NULL) {
            return null;
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      } else {
         return jp.getDoubleValue();
      }
   }

   protected final double _parseDoublePrimitive(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
         if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
               return 0.0D;
            } else {
               switch(text.charAt(0)) {
               case '-':
                  if (!"-Infinity".equals(text) && !"-INF".equals(text)) {
                     break;
                  }

                  return -1.0D / 0.0;
               case 'I':
                  if ("Infinity".equals(text) || "INF".equals(text)) {
                     return 1.0D / 0.0;
                  }
                  break;
               case 'N':
                  if ("NaN".equals(text)) {
                     return 0.0D / 0.0;
                  }
               }

               try {
                  return parseDouble(text);
               } catch (IllegalArgumentException var6) {
                  throw ctxt.weirdStringException(this._valueClass, "not a valid double value");
               }
            }
         } else if (t == JsonToken.VALUE_NULL) {
            return 0.0D;
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      } else {
         return jp.getDoubleValue();
      }
   }

   protected Date _parseDate(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();

      try {
         if (t == JsonToken.VALUE_NUMBER_INT) {
            return new Date(jp.getLongValue());
         } else if (t == JsonToken.VALUE_STRING) {
            String str = jp.getText().trim();
            return str.length() == 0 ? null : ctxt.parseDate(str);
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      } catch (IllegalArgumentException var5) {
         throw ctxt.weirdStringException(this._valueClass, "not a valid representation (error: " + var5.getMessage() + ")");
      }
   }

   protected static final double parseDouble(String numStr) throws NumberFormatException {
      return "2.2250738585072012e-308".equals(numStr) ? 2.2250738585072014E-308D : Double.parseDouble(numStr);
   }

   protected JsonDeserializer<Object> findDeserializer(DeserializationConfig config, DeserializerProvider provider, JavaType type, BeanProperty property) throws JsonMappingException {
      JsonDeserializer<Object> deser = provider.findValueDeserializer(config, type, property);
      return deser;
   }

   protected void handleUnknownProperty(JsonParser jp, DeserializationContext ctxt, Object instanceOrClass, String propName) throws IOException, JsonProcessingException {
      if (instanceOrClass == null) {
         instanceOrClass = this.getValueClass();
      }

      if (!ctxt.handleUnknownProperty(jp, this, instanceOrClass, propName)) {
         this.reportUnknownProperty(ctxt, instanceOrClass, propName);
         jp.skipChildren();
      }
   }

   protected void reportUnknownProperty(DeserializationContext ctxt, Object instanceOrClass, String fieldName) throws IOException, JsonProcessingException {
      if (ctxt.isEnabled(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES)) {
         throw ctxt.unknownFieldException(instanceOrClass, fieldName);
      }
   }

   @JacksonStdImpl
   public static class TokenBufferDeserializer extends StdScalarDeserializer<TokenBuffer> {
      public TokenBufferDeserializer() {
         super(TokenBuffer.class);
      }

      public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         TokenBuffer tb = new TokenBuffer(jp.getCodec());
         tb.copyCurrentStructure(jp);
         return tb;
      }
   }

   public static class StackTraceElementDeserializer extends StdScalarDeserializer<StackTraceElement> {
      public StackTraceElementDeserializer() {
         super(StackTraceElement.class);
      }

      public StackTraceElement deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         JsonToken t = jp.getCurrentToken();
         if (t == JsonToken.START_OBJECT) {
            String className = "";
            String methodName = "";
            String fileName = "";
            int lineNumber = -1;

            while((t = jp.nextValue()) != JsonToken.END_OBJECT) {
               String propName = jp.getCurrentName();
               if ("className".equals(propName)) {
                  className = jp.getText();
               } else if ("fileName".equals(propName)) {
                  fileName = jp.getText();
               } else if ("lineNumber".equals(propName)) {
                  if (!t.isNumeric()) {
                     throw JsonMappingException.from(jp, "Non-numeric token (" + t + ") for property 'lineNumber'");
                  }

                  lineNumber = jp.getIntValue();
               } else if ("methodName".equals(propName)) {
                  methodName = jp.getText();
               } else if (!"nativeMethod".equals(propName)) {
                  this.handleUnknownProperty(jp, ctxt, this._valueClass, propName);
               }
            }

            return new StackTraceElement(className, methodName, fileName, lineNumber);
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      }
   }

   public static class SqlDateDeserializer extends StdScalarDeserializer<java.sql.Date> {
      public SqlDateDeserializer() {
         super(java.sql.Date.class);
      }

      public java.sql.Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         Date d = this._parseDate(jp, ctxt);
         return d == null ? null : new java.sql.Date(d.getTime());
      }
   }

   @JacksonStdImpl
   public static class CalendarDeserializer extends StdScalarDeserializer<Calendar> {
      Class<? extends Calendar> _calendarClass;

      public CalendarDeserializer() {
         this((Class)null);
      }

      public CalendarDeserializer(Class<? extends Calendar> cc) {
         super(Calendar.class);
         this._calendarClass = cc;
      }

      public Calendar deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         Date d = this._parseDate(jp, ctxt);
         if (d == null) {
            return null;
         } else if (this._calendarClass == null) {
            return ctxt.constructCalendar(d);
         } else {
            try {
               Calendar c = (Calendar)this._calendarClass.newInstance();
               c.setTimeInMillis(d.getTime());
               return c;
            } catch (Exception var5) {
               throw ctxt.instantiationException(this._calendarClass, (Throwable)var5);
            }
         }
      }
   }

   @JacksonStdImpl
   public static class BigIntegerDeserializer extends StdScalarDeserializer<BigInteger> {
      public BigIntegerDeserializer() {
         super(BigInteger.class);
      }

      public BigInteger deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         JsonToken t = jp.getCurrentToken();
         if (t == JsonToken.VALUE_NUMBER_INT) {
            switch(jp.getNumberType()) {
            case INT:
            case LONG:
               return BigInteger.valueOf(jp.getLongValue());
            }
         } else {
            if (t == JsonToken.VALUE_NUMBER_FLOAT) {
               return jp.getDecimalValue().toBigInteger();
            }

            if (t != JsonToken.VALUE_STRING) {
               throw ctxt.mappingException(this._valueClass);
            }
         }

         String text = jp.getText().trim();
         if (text.length() == 0) {
            return null;
         } else {
            try {
               return new BigInteger(text);
            } catch (IllegalArgumentException var6) {
               throw ctxt.weirdStringException(this._valueClass, "not a valid representation");
            }
         }
      }
   }

   @JacksonStdImpl
   public static class BigDecimalDeserializer extends StdScalarDeserializer<BigDecimal> {
      public BigDecimalDeserializer() {
         super(BigDecimal.class);
      }

      public BigDecimal deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         JsonToken t = jp.getCurrentToken();
         if (t != JsonToken.VALUE_NUMBER_INT && t != JsonToken.VALUE_NUMBER_FLOAT) {
            if (t == JsonToken.VALUE_STRING) {
               String text = jp.getText().trim();
               if (text.length() == 0) {
                  return null;
               } else {
                  try {
                     return new BigDecimal(text);
                  } catch (IllegalArgumentException var6) {
                     throw ctxt.weirdStringException(this._valueClass, "not a valid representation");
                  }
               }
            } else {
               throw ctxt.mappingException(this._valueClass);
            }
         } else {
            return jp.getDecimalValue();
         }
      }
   }

   public static class AtomicReferenceDeserializer extends StdScalarDeserializer<AtomicReference<?>> implements ResolvableDeserializer {
      protected final JavaType _referencedType;
      protected final BeanProperty _property;
      protected JsonDeserializer<?> _valueDeserializer;

      public AtomicReferenceDeserializer(JavaType referencedType, BeanProperty property) {
         super(AtomicReference.class);
         this._referencedType = referencedType;
         this._property = property;
      }

      public AtomicReference<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         return new AtomicReference(this._valueDeserializer.deserialize(jp, ctxt));
      }

      public void resolve(DeserializationConfig config, DeserializerProvider provider) throws JsonMappingException {
         this._valueDeserializer = provider.findValueDeserializer(config, this._referencedType, this._property);
      }
   }

   public static final class AtomicBooleanDeserializer extends StdScalarDeserializer<AtomicBoolean> {
      public AtomicBooleanDeserializer() {
         super(AtomicBoolean.class);
      }

      public AtomicBoolean deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         return new AtomicBoolean(this._parseBooleanPrimitive(jp, ctxt));
      }
   }

   @JacksonStdImpl
   public static final class NumberDeserializer extends StdScalarDeserializer<Number> {
      public NumberDeserializer() {
         super(Number.class);
      }

      public Number deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         JsonToken t = jp.getCurrentToken();
         if (t == JsonToken.VALUE_NUMBER_INT) {
            return (Number)(ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS) ? jp.getBigIntegerValue() : jp.getNumberValue());
         } else if (t == JsonToken.VALUE_NUMBER_FLOAT) {
            return (Number)(ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS) ? jp.getDecimalValue() : jp.getDoubleValue());
         } else if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();

            try {
               if (text.indexOf(46) >= 0) {
                  return (Number)(ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS) ? new BigDecimal(text) : new Double(text));
               } else if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
                  return new BigInteger(text);
               } else {
                  long value = Long.parseLong(text);
                  return (Number)(value <= 2147483647L && value >= -2147483648L ? (int)value : value);
               }
            } catch (IllegalArgumentException var7) {
               throw ctxt.weirdStringException(this._valueClass, "not a valid number");
            }
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      }

      public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
         switch(jp.getCurrentToken()) {
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
         case VALUE_STRING:
            return this.deserialize(jp, ctxt);
         default:
            return typeDeserializer.deserializeTypedFromScalar(jp, ctxt);
         }
      }
   }

   @JacksonStdImpl
   public static final class DoubleDeserializer extends StdDeserializer.PrimitiveOrWrapperDeserializer<Double> {
      public DoubleDeserializer(Class<Double> cls, Double nvl) {
         super(cls, nvl);
      }

      public Double deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         return this._parseDouble(jp, ctxt);
      }

      public Double deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
         return this._parseDouble(jp, ctxt);
      }
   }

   @JacksonStdImpl
   public static final class FloatDeserializer extends StdDeserializer.PrimitiveOrWrapperDeserializer<Float> {
      public FloatDeserializer(Class<Float> cls, Float nvl) {
         super(cls, nvl);
      }

      public Float deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         return this._parseFloat(jp, ctxt);
      }
   }

   @JacksonStdImpl
   public static final class LongDeserializer extends StdDeserializer.PrimitiveOrWrapperDeserializer<Long> {
      public LongDeserializer(Class<Long> cls, Long nvl) {
         super(cls, nvl);
      }

      public Long deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         return this._parseLong(jp, ctxt);
      }
   }

   @JacksonStdImpl
   public static final class IntegerDeserializer extends StdDeserializer.PrimitiveOrWrapperDeserializer<Integer> {
      public IntegerDeserializer(Class<Integer> cls, Integer nvl) {
         super(cls, nvl);
      }

      public Integer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         return this._parseInteger(jp, ctxt);
      }

      public Integer deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
         return this._parseInteger(jp, ctxt);
      }
   }

   @JacksonStdImpl
   public static final class CharacterDeserializer extends StdDeserializer.PrimitiveOrWrapperDeserializer<Character> {
      public CharacterDeserializer(Class<Character> cls, Character nvl) {
         super(cls, nvl);
      }

      public Character deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         JsonToken t = jp.getCurrentToken();
         if (t == JsonToken.VALUE_NUMBER_INT) {
            int value = jp.getIntValue();
            if (value >= 0 && value <= 65535) {
               return (char)value;
            }
         } else if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText();
            if (text.length() == 1) {
               return text.charAt(0);
            }
         }

         throw ctxt.mappingException(this._valueClass);
      }
   }

   @JacksonStdImpl
   public static final class ShortDeserializer extends StdDeserializer.PrimitiveOrWrapperDeserializer<Short> {
      public ShortDeserializer(Class<Short> cls, Short nvl) {
         super(cls, nvl);
      }

      public Short deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         return this._parseShort(jp, ctxt);
      }
   }

   @JacksonStdImpl
   public static final class ByteDeserializer extends StdDeserializer.PrimitiveOrWrapperDeserializer<Byte> {
      public ByteDeserializer(Class<Byte> cls, Byte nvl) {
         super(cls, nvl);
      }

      public Byte deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         int value = this._parseIntPrimitive(jp, ctxt);
         if (value >= -128 && value <= 127) {
            return (byte)value;
         } else {
            throw ctxt.weirdStringException(this._valueClass, "overflow, value can not be represented as 8-bit value");
         }
      }
   }

   @JacksonStdImpl
   public static final class BooleanDeserializer extends StdDeserializer.PrimitiveOrWrapperDeserializer<Boolean> {
      public BooleanDeserializer(Class<Boolean> cls, Boolean nvl) {
         super(cls, nvl);
      }

      public Boolean deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         return this._parseBoolean(jp, ctxt);
      }

      public Boolean deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
         return this._parseBoolean(jp, ctxt);
      }
   }

   @JacksonStdImpl
   public static final class ClassDeserializer extends StdScalarDeserializer<Class<?>> {
      public ClassDeserializer() {
         super(Class.class);
      }

      public Class<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         JsonToken curr = jp.getCurrentToken();
         if (curr == JsonToken.VALUE_STRING) {
            String className = jp.getText();
            if (className.indexOf(46) < 0) {
               if ("int".equals(className)) {
                  return Integer.TYPE;
               }

               if ("long".equals(className)) {
                  return Long.TYPE;
               }

               if ("float".equals(className)) {
                  return Float.TYPE;
               }

               if ("double".equals(className)) {
                  return Double.TYPE;
               }

               if ("boolean".equals(className)) {
                  return Boolean.TYPE;
               }

               if ("byte".equals(className)) {
                  return Byte.TYPE;
               }

               if ("char".equals(className)) {
                  return Character.TYPE;
               }

               if ("short".equals(className)) {
                  return Short.TYPE;
               }

               if ("void".equals(className)) {
                  return Void.TYPE;
               }
            }

            try {
               return Class.forName(jp.getText());
            } catch (ClassNotFoundException var6) {
               throw ctxt.instantiationException(this._valueClass, (Throwable)var6);
            }
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      }
   }

   @JacksonStdImpl
   public static final class StringDeserializer extends StdScalarDeserializer<String> {
      public StringDeserializer() {
         super(String.class);
      }

      public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         JsonToken curr = jp.getCurrentToken();
         if (curr == JsonToken.VALUE_STRING) {
            return jp.getText();
         } else if (curr == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object ob = jp.getEmbeddedObject();
            if (ob == null) {
               return null;
            } else {
               return ob instanceof byte[] ? Base64Variants.getDefaultVariant().encode((byte[])((byte[])ob), false) : ob.toString();
            }
         } else if (curr.isScalarValue()) {
            return jp.getText();
         } else {
            throw ctxt.mappingException(this._valueClass);
         }
      }

      public String deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
         return this.deserialize(jp, ctxt);
      }
   }

   protected abstract static class PrimitiveOrWrapperDeserializer extends StdScalarDeserializer {
      final T _nullValue;

      protected PrimitiveOrWrapperDeserializer(Class vc, T nvl) {
         super(vc);
         this._nullValue = nvl;
      }

      public final T getNullValue() {
         return this._nullValue;
      }
   }
}
