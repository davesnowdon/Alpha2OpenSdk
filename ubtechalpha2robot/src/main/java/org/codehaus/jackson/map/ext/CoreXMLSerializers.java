package org.codehaus.jackson.map.ext;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.SerializerBase;
import org.codehaus.jackson.map.ser.StdSerializers;
import org.codehaus.jackson.map.ser.ToStringSerializer;
import org.codehaus.jackson.map.util.Provider;

public class CoreXMLSerializers implements Provider<Entry<Class<?>, JsonSerializer<?>>> {
   static final HashMap<Class<?>, JsonSerializer<?>> _serializers = new HashMap();

   public CoreXMLSerializers() {
   }

   public Collection<Entry<Class<?>, JsonSerializer<?>>> provide() {
      return _serializers.entrySet();
   }

   static {
      ToStringSerializer tss = ToStringSerializer.instance;
      _serializers.put(Duration.class, tss);
      _serializers.put(XMLGregorianCalendar.class, new CoreXMLSerializers.XMLGregorianCalendarSerializer());
      _serializers.put(QName.class, tss);
   }

   public static class XMLGregorianCalendarSerializer extends SerializerBase<XMLGregorianCalendar> {
      public XMLGregorianCalendarSerializer() {
         super(XMLGregorianCalendar.class);
      }

      public void serialize(XMLGregorianCalendar value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
         StdSerializers.CalendarSerializer.instance.serialize((Calendar)value.toGregorianCalendar(), jgen, provider);
      }

      public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
         return StdSerializers.CalendarSerializer.instance.getSchema(provider, typeHint);
      }
   }
}
