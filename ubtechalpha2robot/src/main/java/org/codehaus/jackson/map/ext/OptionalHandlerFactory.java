package org.codehaus.jackson.map.ext;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.codehaus.jackson.map.util.Provider;
import org.codehaus.jackson.type.JavaType;

public class OptionalHandlerFactory {
   private static final String PACKAGE_PREFIX_JODA_DATETIME = "org.joda.time.";
   private static final String PACKAGE_PREFIX_JAVAX_XML = "javax.xml.";
   private static final String SERIALIZERS_FOR_JODA_DATETIME = "org.codehaus.jackson.map.ext.JodaSerializers";
   private static final String SERIALIZERS_FOR_JAVAX_XML = "org.codehaus.jackson.map.ext.CoreXMLSerializers";
   private static final String DESERIALIZERS_FOR_JODA_DATETIME = "org.codehaus.jackson.map.ext.JodaDeserializers";
   private static final String DESERIALIZERS_FOR_JAVAX_XML = "org.codehaus.jackson.map.ext.CoreXMLDeserializers";
   private static final String CLASS_NAME_DOM_NODE = "org.w3c.dom.Node";
   private static final String CLASS_NAME_DOM_DOCUMENT = "org.w3c.dom.Node";
   private static final String SERIALIZER_FOR_DOM_NODE = "org.codehaus.jackson.map.ext.DOMSerializer";
   private static final String DESERIALIZER_FOR_DOM_DOCUMENT = "org.codehaus.jackson.map.ext.DOMDeserializer$DocumentDeserializer";
   private static final String DESERIALIZER_FOR_DOM_NODE = "org.codehaus.jackson.map.ext.DOMDeserializer$NodeDeserializer";
   public static final OptionalHandlerFactory instance = new OptionalHandlerFactory();

   protected OptionalHandlerFactory() {
   }

   public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type) {
      Class<?> rawType = type.getRawClass();
      String className = rawType.getName();
      String factoryName;
      if (className.startsWith("org.joda.time.")) {
         factoryName = "org.codehaus.jackson.map.ext.JodaSerializers";
      } else {
         if (!className.startsWith("javax.xml.") && !this.hasSupertypeStartingWith(rawType, "javax.xml.")) {
            if (this.doesImplement(rawType, "org.w3c.dom.Node")) {
               return (JsonSerializer)this.instantiate("org.codehaus.jackson.map.ext.DOMSerializer");
            }

            return null;
         }

         factoryName = "org.codehaus.jackson.map.ext.CoreXMLSerializers";
      }

      Object ob = this.instantiate(factoryName);
      if (ob == null) {
         return null;
      } else {
         Provider<Entry<Class<?>, JsonSerializer<?>>> prov = (Provider)ob;
         Collection<Entry<Class<?>, JsonSerializer<?>>> entries = prov.provide();
         Iterator i$ = entries.iterator();

         Entry entry;
         do {
            if (!i$.hasNext()) {
               i$ = entries.iterator();

               do {
                  if (!i$.hasNext()) {
                     return null;
                  }

                  entry = (Entry)i$.next();
               } while(!((Class)entry.getKey()).isAssignableFrom(rawType));

               return (JsonSerializer)entry.getValue();
            }

            entry = (Entry)i$.next();
         } while(rawType != entry.getKey());

         return (JsonSerializer)entry.getValue();
      }
   }

   public JsonDeserializer<?> findDeserializer(JavaType type, DeserializationConfig config, DeserializerProvider p) {
      Class<?> rawType = type.getRawClass();
      String className = rawType.getName();
      String factoryName;
      if (className.startsWith("org.joda.time.")) {
         factoryName = "org.codehaus.jackson.map.ext.JodaDeserializers";
      } else {
         if (!className.startsWith("javax.xml.") && !this.hasSupertypeStartingWith(rawType, "javax.xml.")) {
            if (this.doesImplement(rawType, "org.w3c.dom.Node")) {
               return (JsonDeserializer)this.instantiate("org.codehaus.jackson.map.ext.DOMDeserializer$DocumentDeserializer");
            }

            if (this.doesImplement(rawType, "org.w3c.dom.Node")) {
               return (JsonDeserializer)this.instantiate("org.codehaus.jackson.map.ext.DOMDeserializer$NodeDeserializer");
            }

            return null;
         }

         factoryName = "org.codehaus.jackson.map.ext.CoreXMLDeserializers";
      }

      Object ob = this.instantiate(factoryName);
      if (ob == null) {
         return null;
      } else {
         Provider<StdDeserializer<?>> prov = (Provider)ob;
         Collection<StdDeserializer<?>> entries = prov.provide();
         Iterator i$ = entries.iterator();

         StdDeserializer deser;
         do {
            if (!i$.hasNext()) {
               i$ = entries.iterator();

               do {
                  if (!i$.hasNext()) {
                     return null;
                  }

                  deser = (StdDeserializer)i$.next();
               } while(!deser.getValueClass().isAssignableFrom(rawType));

               return deser;
            }

            deser = (StdDeserializer)i$.next();
         } while(rawType != deser.getValueClass());

         return deser;
      }
   }

   private Object instantiate(String className) {
      try {
         return Class.forName(className).newInstance();
      } catch (LinkageError var3) {
      } catch (Exception var4) {
      }

      return null;
   }

   private boolean doesImplement(Class<?> actualType, String classNameToImplement) {
      for(Class type = actualType; type != null; type = type.getSuperclass()) {
         if (type.getName().equals(classNameToImplement)) {
            return true;
         }

         if (this.hasInterface(type, classNameToImplement)) {
            return true;
         }
      }

      return false;
   }

   private boolean hasInterface(Class<?> type, String interfaceToImplement) {
      Class<?>[] interfaces = type.getInterfaces();
      Class[] arr$ = interfaces;
      int len$ = interfaces.length;

      int i$;
      Class iface;
      for(i$ = 0; i$ < len$; ++i$) {
         iface = arr$[i$];
         if (iface.getName().equals(interfaceToImplement)) {
            return true;
         }
      }

      arr$ = interfaces;
      len$ = interfaces.length;

      for(i$ = 0; i$ < len$; ++i$) {
         iface = arr$[i$];
         if (this.hasInterface(iface, interfaceToImplement)) {
            return true;
         }
      }

      return false;
   }

   private boolean hasSupertypeStartingWith(Class<?> rawType, String prefix) {
      Class cls;
      for(cls = rawType.getSuperclass(); cls != null; cls = cls.getSuperclass()) {
         if (cls.getName().startsWith(prefix)) {
            return true;
         }
      }

      for(cls = rawType; cls != null; cls = cls.getSuperclass()) {
         if (this.hasInterfaceStartingWith(cls, prefix)) {
            return true;
         }
      }

      return false;
   }

   private boolean hasInterfaceStartingWith(Class<?> type, String prefix) {
      Class<?>[] interfaces = type.getInterfaces();
      Class[] arr$ = interfaces;
      int len$ = interfaces.length;

      int i$;
      Class iface;
      for(i$ = 0; i$ < len$; ++i$) {
         iface = arr$[i$];
         if (iface.getName().startsWith(prefix)) {
            return true;
         }
      }

      arr$ = interfaces;
      len$ = interfaces.length;

      for(i$ = 0; i$ < len$; ++i$) {
         iface = arr$[i$];
         if (this.hasInterfaceStartingWith(iface, prefix)) {
            return true;
         }
      }

      return false;
   }
}
