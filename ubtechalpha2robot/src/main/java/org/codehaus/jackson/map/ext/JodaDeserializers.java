package org.codehaus.jackson.map.ext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.codehaus.jackson.map.deser.StdScalarDeserializer;
import org.codehaus.jackson.map.util.Provider;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class JodaDeserializers implements Provider<StdDeserializer<?>> {
   public JodaDeserializers() {
   }

   public Collection<StdDeserializer<?>> provide() {
      return Arrays.asList(new JodaDeserializers.DateTimeDeserializer(DateTime.class), new JodaDeserializers.DateTimeDeserializer(ReadableDateTime.class), new JodaDeserializers.DateTimeDeserializer(ReadableInstant.class), new JodaDeserializers.LocalDateDeserializer(), new JodaDeserializers.LocalDateTimeDeserializer(), new JodaDeserializers.DateMidnightDeserializer());
   }

   public static class DateMidnightDeserializer extends JodaDeserializers.JodaDeserializer<DateMidnight> {
      public DateMidnightDeserializer() {
         super(DateMidnight.class);
      }

      public DateMidnight deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (jp.isExpectedStartArrayToken()) {
            jp.nextToken();
            int year = jp.getIntValue();
            jp.nextToken();
            int month = jp.getIntValue();
            jp.nextToken();
            int day = jp.getIntValue();
            if (jp.nextToken() != JsonToken.END_ARRAY) {
               throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "after DateMidnight ints");
            } else {
               return new DateMidnight(year, month, day);
            }
         } else {
            switch(jp.getCurrentToken()) {
            case VALUE_NUMBER_INT:
               return new DateMidnight(jp.getLongValue());
            case VALUE_STRING:
               DateTime local = this.parseLocal(jp);
               if (local == null) {
                  return null;
               }

               return local.toDateMidnight();
            default:
               throw ctxt.wrongTokenException(jp, JsonToken.START_ARRAY, "expected JSON Array, Number or String");
            }
         }
      }
   }

   public static class LocalDateTimeDeserializer extends JodaDeserializers.JodaDeserializer<LocalDateTime> {
      public LocalDateTimeDeserializer() {
         super(LocalDateTime.class);
      }

      public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (jp.isExpectedStartArrayToken()) {
            jp.nextToken();
            int year = jp.getIntValue();
            jp.nextToken();
            int month = jp.getIntValue();
            jp.nextToken();
            int day = jp.getIntValue();
            jp.nextToken();
            int hour = jp.getIntValue();
            jp.nextToken();
            int minute = jp.getIntValue();
            jp.nextToken();
            int second = jp.getIntValue();
            int millisecond = 0;
            if (jp.nextToken() != JsonToken.END_ARRAY) {
               millisecond = jp.getIntValue();
               jp.nextToken();
            }

            if (jp.getCurrentToken() != JsonToken.END_ARRAY) {
               throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "after LocalDateTime ints");
            } else {
               return new LocalDateTime(year, month, day, hour, minute, second, millisecond);
            }
         } else {
            switch(jp.getCurrentToken()) {
            case VALUE_NUMBER_INT:
               return new LocalDateTime(jp.getLongValue());
            case VALUE_STRING:
               DateTime local = this.parseLocal(jp);
               if (local == null) {
                  return null;
               }

               return local.toLocalDateTime();
            default:
               throw ctxt.wrongTokenException(jp, JsonToken.START_ARRAY, "expected JSON Array or Number");
            }
         }
      }
   }

   public static class LocalDateDeserializer extends JodaDeserializers.JodaDeserializer<LocalDate> {
      public LocalDateDeserializer() {
         super(LocalDate.class);
      }

      public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (jp.isExpectedStartArrayToken()) {
            jp.nextToken();
            int year = jp.getIntValue();
            jp.nextToken();
            int month = jp.getIntValue();
            jp.nextToken();
            int day = jp.getIntValue();
            if (jp.nextToken() != JsonToken.END_ARRAY) {
               throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "after LocalDate ints");
            } else {
               return new LocalDate(year, month, day);
            }
         } else {
            switch(jp.getCurrentToken()) {
            case VALUE_NUMBER_INT:
               return new LocalDate(jp.getLongValue());
            case VALUE_STRING:
               DateTime local = this.parseLocal(jp);
               if (local == null) {
                  return null;
               }

               return local.toLocalDate();
            default:
               throw ctxt.wrongTokenException(jp, JsonToken.START_ARRAY, "expected JSON Array, String or Number");
            }
         }
      }
   }

   public static class DateTimeDeserializer<T extends ReadableInstant> extends JodaDeserializers.JodaDeserializer<T> {
      public DateTimeDeserializer(Class<T> cls) {
         super(cls);
      }

      public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         JsonToken t = jp.getCurrentToken();
         if (t == JsonToken.VALUE_NUMBER_INT) {
            return new DateTime(jp.getLongValue(), DateTimeZone.UTC);
         } else if (t == JsonToken.VALUE_STRING) {
            String str = jp.getText().trim();
            return str.length() == 0 ? null : new DateTime(str, DateTimeZone.UTC);
         } else {
            throw ctxt.mappingException(this.getValueClass());
         }
      }
   }

   abstract static class JodaDeserializer<T> extends StdScalarDeserializer<T> {
      static final DateTimeFormatter _localDateTimeFormat = ISODateTimeFormat.localDateOptionalTimeParser();

      protected JodaDeserializer(Class<T> cls) {
         super(cls);
      }

      protected DateTime parseLocal(JsonParser jp) throws IOException, JsonProcessingException {
         String str = jp.getText().trim();
         return str.length() == 0 ? null : _localDateTimeFormat.parseDateTime(str);
      }
   }
}
