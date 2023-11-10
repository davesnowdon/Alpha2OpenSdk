package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializationProblemHandler;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.util.LinkedNode;
import org.codehaus.jackson.map.util.ObjectBuffer;
import org.codehaus.jackson.type.JavaType;

public class StdDeserializationContext extends DeserializationContext {
   static final int MAX_ERROR_STR_LEN = 500;
   protected JsonParser _parser;
   protected final DeserializerProvider _deserProvider;
   protected ArrayBuilders _arrayBuilders;
   protected ObjectBuffer _objectBuffer;
   protected DateFormat _dateFormat;

   public StdDeserializationContext(DeserializationConfig config, JsonParser jp, DeserializerProvider prov) {
      super(config);
      this._parser = jp;
      this._deserProvider = prov;
   }

   public DeserializerProvider getDeserializerProvider() {
      return this._deserProvider;
   }

   public JsonParser getParser() {
      return this._parser;
   }

   public final ObjectBuffer leaseObjectBuffer() {
      ObjectBuffer buf = this._objectBuffer;
      if (buf == null) {
         buf = new ObjectBuffer();
      } else {
         this._objectBuffer = null;
      }

      return buf;
   }

   public final void returnObjectBuffer(ObjectBuffer buf) {
      if (this._objectBuffer == null || buf.initialCapacity() >= this._objectBuffer.initialCapacity()) {
         this._objectBuffer = buf;
      }

   }

   public final ArrayBuilders getArrayBuilders() {
      if (this._arrayBuilders == null) {
         this._arrayBuilders = new ArrayBuilders();
      }

      return this._arrayBuilders;
   }

   public Date parseDate(String dateStr) throws IllegalArgumentException {
      try {
         return this.getDateFormat().parse(dateStr);
      } catch (ParseException var3) {
         throw new IllegalArgumentException(var3.getMessage());
      }
   }

   public Calendar constructCalendar(Date d) {
      Calendar c = Calendar.getInstance();
      c.setTime(d);
      return c;
   }

   public boolean handleUnknownProperty(JsonParser jp, JsonDeserializer<?> deser, Object instanceOrClass, String propName) throws IOException, JsonProcessingException {
      LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers();
      if (h != null) {
         JsonParser oldParser = this._parser;
         this._parser = jp;

         try {
            while(h != null) {
               if (((DeserializationProblemHandler)h.value()).handleUnknownProperty(this, deser, instanceOrClass, propName)) {
                  boolean var7 = true;
                  return var7;
               }

               h = h.next();
            }

            return false;
         } finally {
            this._parser = oldParser;
         }
      } else {
         return false;
      }
   }

   public JsonMappingException mappingException(Class<?> targetClass) {
      String clsName = this._calcName(targetClass);
      return JsonMappingException.from(this._parser, "Can not deserialize instance of " + clsName + " out of " + this._parser.getCurrentToken() + " token");
   }

   public JsonMappingException instantiationException(Class<?> instClass, Throwable t) {
      return JsonMappingException.from(this._parser, "Can not construct instance of " + instClass.getName() + ", problem: " + t.getMessage(), t);
   }

   public JsonMappingException instantiationException(Class<?> instClass, String msg) {
      return JsonMappingException.from(this._parser, "Can not construct instance of " + instClass.getName() + ", problem: " + msg);
   }

   public JsonMappingException weirdStringException(Class<?> instClass, String msg) {
      return JsonMappingException.from(this._parser, "Can not construct instance of " + instClass.getName() + " from String value '" + this._valueDesc() + "': " + msg);
   }

   public JsonMappingException weirdNumberException(Class<?> instClass, String msg) {
      return JsonMappingException.from(this._parser, "Can not construct instance of " + instClass.getName() + " from number value (" + this._valueDesc() + "): " + msg);
   }

   public JsonMappingException weirdKeyException(Class<?> keyClass, String keyValue, String msg) {
      return JsonMappingException.from(this._parser, "Can not construct Map key of type " + keyClass.getName() + " from String \"" + this._desc(keyValue) + "\": " + msg);
   }

   public JsonMappingException wrongTokenException(JsonParser jp, JsonToken expToken, String msg) {
      return JsonMappingException.from(jp, "Unexpected token (" + jp.getCurrentToken() + "), expected " + expToken + ": " + msg);
   }

   public JsonMappingException unknownFieldException(Object instanceOrClass, String fieldName) {
      return UnrecognizedPropertyException.from(this._parser, instanceOrClass, fieldName);
   }

   public JsonMappingException unknownTypeException(JavaType type, String id) {
      return JsonMappingException.from(this._parser, "Could not resolve type id '" + id + "' into a subtype of " + type);
   }

   protected DateFormat getDateFormat() {
      if (this._dateFormat == null) {
         this._dateFormat = (DateFormat)this._config.getDateFormat().clone();
      }

      return this._dateFormat;
   }

   protected String determineClassName(Object instance) {
      return ClassUtil.getClassDescription(instance);
   }

   protected String _calcName(Class<?> cls) {
      return cls.isArray() ? this._calcName(cls.getComponentType()) + "[]" : cls.getName();
   }

   protected String _valueDesc() {
      try {
         return this._desc(this._parser.getText());
      } catch (Exception var2) {
         return "[N/A]";
      }
   }

   protected String _desc(String desc) {
      if (desc.length() > 500) {
         desc = desc.substring(0, 500) + "]...[" + desc.substring(desc.length() - 500);
      }

      return desc;
   }
}
