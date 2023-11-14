package com.ubtechinc.service.base;

import android.util.Log;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import java.io.InputStream;

public abstract class BaseManager {
   protected static final int JOSN = 1;
   protected static final int XML = 2;
   protected static final int BOTH = 3;
   protected XStream xmlMapper;

   public BaseManager() {
      this(3);
   }

   public BaseManager(int parseType) {
      switch(parseType) {
      case 1:
         break;
      case 2:
         this.xmlMapper = this.getXMLMapper();
         break;
      default:
         this.xmlMapper = this.getXMLMapper();
      }

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
      T obj  = (T) this.xmlMapper.fromXML(xml);
      return obj;
   }

   public <T> T xmlToBean(InputStream xml, Class<T> cls) {
      this.xmlMapper.processAnnotations(cls);
      T obj = (T) this.xmlMapper.fromXML(xml);
      return obj;
   }

   public <T> String beanToXml(T obj, Class<T> cls) {
      XStream xstream = new XStream(new DomDriver());
      xstream.processAnnotations(cls);
      Log.e("zdy", obj.toString());
      String xml = xstream.toXML(obj);
      return xml;
   }
}
