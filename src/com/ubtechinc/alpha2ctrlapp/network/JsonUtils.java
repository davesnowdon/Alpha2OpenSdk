package com.ubtechinc.alpha2ctrlapp.network;

import java.io.IOException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class JsonUtils {
   private static JsonUtils jsonUtils;
   private ObjectMapper mapper = new ObjectMapper();

   public static JsonUtils getInstance() {
      if (jsonUtils == null) {
         jsonUtils = new JsonUtils();
      }

      return jsonUtils;
   }

   public JsonUtils() {
      this.mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, Boolean.TRUE);
      this.mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);
      this.mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public String getJson(Object obj) {
      String json = null;

      try {
         json = this.mapper.writeValueAsString(obj);
      } catch (JsonGenerationException var4) {
         var4.printStackTrace();
      } catch (JsonMappingException var5) {
         var5.printStackTrace();
      } catch (IOException var6) {
         var6.printStackTrace();
      }

      return json;
   }

   public <T> T jsonToBean(String json, Class<T> cls) {
      try {
         T obj = this.mapper.readValue(json, cls);
         return obj;
      } catch (JsonParseException var4) {
         var4.printStackTrace();
      } catch (JsonMappingException var5) {
         var5.printStackTrace();
      } catch (IOException var6) {
         var6.printStackTrace();
      }

      return null;
   }
}
