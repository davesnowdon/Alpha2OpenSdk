package org.codehaus.jackson.map.deser;

import java.lang.reflect.Type;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

class StdDeserializers {
   final HashMap<JavaType, JsonDeserializer<Object>> _deserializers = new HashMap();

   private StdDeserializers() {
      this.add(new UntypedObjectDeserializer());
      StdDeserializer<?> strDeser = new StdDeserializer.StringDeserializer();
      this.add(strDeser, String.class);
      this.add(strDeser, CharSequence.class);
      this.add(new StdDeserializer.ClassDeserializer());
      this.add(new StdDeserializer.BooleanDeserializer(Boolean.class, (Boolean)null));
      this.add(new StdDeserializer.ByteDeserializer(Byte.class, (Byte)null));
      this.add(new StdDeserializer.ShortDeserializer(Short.class, (Short)null));
      this.add(new StdDeserializer.CharacterDeserializer(Character.class, (Character)null));
      this.add(new StdDeserializer.IntegerDeserializer(Integer.class, (Integer)null));
      this.add(new StdDeserializer.LongDeserializer(Long.class, (Long)null));
      this.add(new StdDeserializer.FloatDeserializer(Float.class, (Float)null));
      this.add(new StdDeserializer.DoubleDeserializer(Double.class, (Double)null));
      this.add(new StdDeserializer.BooleanDeserializer(Boolean.TYPE, Boolean.FALSE));
      this.add(new StdDeserializer.ByteDeserializer(Byte.TYPE, (byte)0));
      this.add(new StdDeserializer.ShortDeserializer(Short.TYPE, Short.valueOf((short)0)));
      this.add(new StdDeserializer.CharacterDeserializer(Character.TYPE, '\u0000'));
      this.add(new StdDeserializer.IntegerDeserializer(Integer.TYPE, 0));
      this.add(new StdDeserializer.LongDeserializer(Long.TYPE, 0L));
      this.add(new StdDeserializer.FloatDeserializer(Float.TYPE, 0.0F));
      this.add(new StdDeserializer.DoubleDeserializer(Double.TYPE, 0.0D));
      this.add(new StdDeserializer.NumberDeserializer());
      this.add(new StdDeserializer.BigDecimalDeserializer());
      this.add(new StdDeserializer.BigIntegerDeserializer());
      this.add(new DateDeserializer());
      this.add(new StdDeserializer.SqlDateDeserializer());
      this.add(new TimestampDeserializer());
      this.add(new StdDeserializer.CalendarDeserializer());
      this.add(new StdDeserializer.CalendarDeserializer(GregorianCalendar.class), GregorianCalendar.class);
      Iterator i$ = FromStringDeserializer.all().iterator();

      while(i$.hasNext()) {
         StdDeserializer<?> deser = (FromStringDeserializer)i$.next();
         this.add(deser);
      }

      this.add(new StdDeserializer.StackTraceElementDeserializer());
      this.add(new StdDeserializer.TokenBufferDeserializer());
      this.add(new StdDeserializer.AtomicBooleanDeserializer());
   }

   public static HashMap<JavaType, JsonDeserializer<Object>> constructAll() {
      return (new StdDeserializers())._deserializers;
   }

   private void add(StdDeserializer<?> stdDeser) {
      this.add(stdDeser, stdDeser.getValueClass());
   }

   private void add(StdDeserializer<?> stdDeser, Class<?> valueClass) {
      this._deserializers.put(TypeFactory.defaultInstance().constructType((Type)valueClass), stdDeser);
   }
}
