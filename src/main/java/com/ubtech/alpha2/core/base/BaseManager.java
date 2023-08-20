package com.ubtech.alpha2.core.base;

import android.content.Context;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.ubtech.alpha2.core.network.http.HttpClientManager;
import java.io.IOException;
import java.io.InputStream;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public abstract class BaseManager {
   protected static final int JOSN = 1;
   protected static final int XML = 2;
   protected static final int BOTH = 3;
   protected Context mContext;
   protected HttpClientManager httpManager;
   protected ObjectMapper jsonMapper;
   protected XStream xmlMapper;

   public BaseManager(Context context) {
      this(context, 3);
   }

   public BaseManager(Context context, int parseType) {
      this.mContext = context;
      this.httpManager = HttpClientManager.getInstance(context);
      switch(parseType) {
      case 1:
         this.jsonMapper = this.getJSONMapper();
         break;
      case 2:
         this.xmlMapper = this.getXMLMapper();
         break;
      default:
         this.jsonMapper = this.getJSONMapper();
         this.xmlMapper = this.getXMLMapper();
      }

   }

   public ObjectMapper getJSONMapper() {
      if (this.jsonMapper == null) {
         this.jsonMapper = new ObjectMapper();
         this.jsonMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);
         this.jsonMapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      }

      return this.jsonMapper;
   }

   public XStream getXMLMapper() {
      if (this.xmlMapper == null) {
         this.xmlMapper = new XStream(new XppDriver()) {
            protected MapperWrapper wrapMapper(MapperWrapper next) {
               return new MapperWrapper(next) {
                  public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                     return definedIn == Object.class ? false : super.shouldSerializeMember(definedIn, fieldName);
                  }
               };
            }
         };
         this.xmlMapper.autodetectAnnotations(true);
      }

      return this.xmlMapper;
   }

   public <T> T xmlToBean(String xml, Class<T> cls) {
      this.xmlMapper.processAnnotations(cls);
      T obj = this.xmlMapper.fromXML(xml);
      return obj;
   }

   public <T> T xmlToBean(InputStream xml, Class<T> cls) {
      this.xmlMapper.processAnnotations(cls);
      T obj = this.xmlMapper.fromXML(xml);
      return obj;
   }

   public <T> T jsonToBean(String json, Class<T> cls) {
      try {
         T obj = this.jsonMapper.readValue(json, cls);
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

   public String beanToJson(Object object) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, Boolean.TRUE);
      String json = null;

      try {
         json = mapper.writeValueAsString(object);
      } catch (JsonGenerationException var5) {
         var5.printStackTrace();
      } catch (JsonMappingException var6) {
         var6.printStackTrace();
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      return json;
   }
}
