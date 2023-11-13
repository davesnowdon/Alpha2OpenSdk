package org.codehaus.jackson.map.deser;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;

public abstract class FromStringDeserializer extends StdScalarDeserializer {
   protected FromStringDeserializer(Class vc) {
      super(vc);
   }

   public static Iterable all() {
      ArrayList all = new ArrayList();
      all.add(new FromStringDeserializer.UUIDDeserializer());
      all.add(new FromStringDeserializer.URLDeserializer());
      all.add(new FromStringDeserializer.URIDeserializer());
      all.add(new FromStringDeserializer.CurrencyDeserializer());
      all.add(new FromStringDeserializer.PatternDeserializer());
      all.add(new FromStringDeserializer.LocaleDeserializer());
      all.add(new FromStringDeserializer.InetAddressDeserializer());
      all.add(new FromStringDeserializer.TimeZoneDeserializer());
      return all;
   }

   public final T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
         String text = jp.getText().trim();
         if (text.length() == 0) {
            return null;
         } else {
            try {
               T result = this._deserialize(text, ctxt);
               if (result != null) {
                  return result;
               }
            } catch (IllegalArgumentException var5) {
            }

            throw ctxt.weirdStringException(this._valueClass, "not a valid textual representation");
         }
      } else if (jp.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT) {
         Object ob = jp.getEmbeddedObject();
         if (ob == null) {
            return null;
         } else {
            return this._valueClass.isAssignableFrom(ob.getClass()) ? ob : this._deserializeEmbedded(ob, ctxt);
         }
      } else {
         throw ctxt.mappingException(this._valueClass);
      }
   }

   protected abstract Object _deserialize(String var1, DeserializationContext var2) throws IOException, JsonProcessingException;

   protected Object _deserializeEmbedded(Object ob, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      throw ctxt.mappingException("Don't know how to convert embedded Object of type " + ob.getClass().getName() + " into " + this._valueClass.getName());
   }

   protected static class TimeZoneDeserializer extends FromStringDeserializer<TimeZone> {
      public TimeZoneDeserializer() {
         super(TimeZone.class);
      }

      protected TimeZone _deserialize(String value, DeserializationContext ctxt) throws IOException {
         return TimeZone.getTimeZone(value);
      }
   }

   protected static class InetAddressDeserializer extends FromStringDeserializer<InetAddress> {
      public InetAddressDeserializer() {
         super(InetAddress.class);
      }

      protected InetAddress _deserialize(String value, DeserializationContext ctxt) throws IOException {
         return InetAddress.getByName(value);
      }
   }

   protected static class LocaleDeserializer extends FromStringDeserializer<Locale> {
      public LocaleDeserializer() {
         super(Locale.class);
      }

      protected Locale _deserialize(String value, DeserializationContext ctxt) throws IOException {
         int ix = value.indexOf(95);
         if (ix < 0) {
            return new Locale(value);
         } else {
            String first = value.substring(0, ix);
            value = value.substring(ix + 1);
            ix = value.indexOf(95);
            if (ix < 0) {
               return new Locale(first, value);
            } else {
               String second = value.substring(0, ix);
               return new Locale(first, second, value.substring(ix + 1));
            }
         }
      }
   }

   public static class PatternDeserializer extends FromStringDeserializer<Pattern> {
      public PatternDeserializer() {
         super(Pattern.class);
      }

      protected Pattern _deserialize(String value, DeserializationContext ctxt) throws IllegalArgumentException {
         return Pattern.compile(value);
      }
   }

   public static class CurrencyDeserializer extends FromStringDeserializer<Currency> {
      public CurrencyDeserializer() {
         super(Currency.class);
      }

      protected Currency _deserialize(String value, DeserializationContext ctxt) throws IllegalArgumentException {
         return Currency.getInstance(value);
      }
   }

   public static class URIDeserializer extends FromStringDeserializer<URI> {
      public URIDeserializer() {
         super(URI.class);
      }

      protected URI _deserialize(String value, DeserializationContext ctxt) throws IllegalArgumentException {
         return URI.create(value);
      }
   }

   public static class URLDeserializer extends FromStringDeserializer<URL> {
      public URLDeserializer() {
         super(URL.class);
      }

      protected URL _deserialize(String value, DeserializationContext ctxt) throws IOException {
         return new URL(value);
      }
   }

   public static class UUIDDeserializer extends FromStringDeserializer<UUID> {
      public UUIDDeserializer() {
         super(UUID.class);
      }

      protected UUID _deserialize(String value, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         return UUID.fromString(value);
      }

      protected UUID _deserializeEmbedded(Object ob, DeserializationContext ctxt) throws IOException, JsonProcessingException {
         if (ob instanceof byte[]) {
            byte[] bytes = (byte[])((byte[])ob);
            if (bytes.length != 16) {
               ctxt.mappingException("Can only construct UUIDs from 16 byte arrays; got " + bytes.length + " bytes");
            }

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
            long l1 = in.readLong();
            long l2 = in.readLong();
            return new UUID(l1, l2);
         } else {
            super._deserializeEmbedded(ob, ctxt);
            return null;
         }
      }
   }
}
