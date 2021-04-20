package org.codehaus.jackson.map;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.util.ObjectBuffer;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.type.JavaType;

public abstract class DeserializationContext {
   protected final DeserializationConfig _config;
   protected final int _featureFlags;

   protected DeserializationContext(DeserializationConfig config) {
      this._config = config;
      this._featureFlags = config._featureFlags;
   }

   public DeserializationConfig getConfig() {
      return this._config;
   }

   public DeserializerProvider getDeserializerProvider() {
      return null;
   }

   public boolean isEnabled(DeserializationConfig.Feature feat) {
      return (this._featureFlags & feat.getMask()) != 0;
   }

   public Base64Variant getBase64Variant() {
      return this._config.getBase64Variant();
   }

   public abstract JsonParser getParser();

   public final JsonNodeFactory getNodeFactory() {
      return this._config.getNodeFactory();
   }

   public JavaType constructType(Class<?> cls) {
      return this._config.constructType(cls);
   }

   public abstract ObjectBuffer leaseObjectBuffer();

   public abstract void returnObjectBuffer(ObjectBuffer var1);

   public abstract ArrayBuilders getArrayBuilders();

   public abstract Date parseDate(String var1) throws IllegalArgumentException;

   public abstract Calendar constructCalendar(Date var1);

   public abstract boolean handleUnknownProperty(JsonParser var1, JsonDeserializer<?> var2, Object var3, String var4) throws IOException, JsonProcessingException;

   public abstract JsonMappingException mappingException(Class<?> var1);

   public JsonMappingException mappingException(String message) {
      return JsonMappingException.from(this.getParser(), message);
   }

   public abstract JsonMappingException instantiationException(Class<?> var1, Throwable var2);

   public abstract JsonMappingException instantiationException(Class<?> var1, String var2);

   public abstract JsonMappingException weirdStringException(Class<?> var1, String var2);

   public abstract JsonMappingException weirdNumberException(Class<?> var1, String var2);

   public abstract JsonMappingException weirdKeyException(Class<?> var1, String var2, String var3);

   public abstract JsonMappingException wrongTokenException(JsonParser var1, JsonToken var2, String var3);

   public abstract JsonMappingException unknownFieldException(Object var1, String var2);

   public abstract JsonMappingException unknownTypeException(JavaType var1, String var2);
}
