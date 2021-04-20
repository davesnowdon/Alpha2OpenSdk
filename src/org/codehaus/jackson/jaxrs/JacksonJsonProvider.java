package org.codehaus.jackson.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.type.ClassKey;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.util.JSONPObject;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.util.VersionUtil;

@Provider
@Consumes({"application/json", "text/json"})
@Produces({"application/json", "text/json"})
public class JacksonJsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object>, Versioned {
   public static final Annotations[] BASIC_ANNOTATIONS;
   public static final HashSet<ClassKey> _untouchables;
   public static final Class<?>[] _unreadableClasses;
   public static final Class<?>[] _unwritableClasses;
   protected final MapperConfigurator _mapperConfig;
   protected HashSet<ClassKey> _cfgCustomUntouchables;
   protected String _jsonpFunctionName;
   @Context
   protected Providers _providers;
   protected boolean _cfgCheckCanSerialize;
   protected boolean _cfgCheckCanDeserialize;

   public JacksonJsonProvider() {
      this((ObjectMapper)null, BASIC_ANNOTATIONS);
   }

   public JacksonJsonProvider(Annotations... annotationsToUse) {
      this((ObjectMapper)null, annotationsToUse);
   }

   public JacksonJsonProvider(ObjectMapper mapper) {
      this(mapper, BASIC_ANNOTATIONS);
   }

   public JacksonJsonProvider(ObjectMapper mapper, Annotations[] annotationsToUse) {
      this._cfgCheckCanSerialize = false;
      this._cfgCheckCanDeserialize = false;
      this._mapperConfig = new MapperConfigurator(mapper, annotationsToUse);
   }

   public Version version() {
      return VersionUtil.versionFor(this.getClass());
   }

   public void checkCanDeserialize(boolean state) {
      this._cfgCheckCanDeserialize = state;
   }

   public void checkCanSerialize(boolean state) {
      this._cfgCheckCanSerialize = state;
   }

   public void setAnnotationsToUse(Annotations[] annotationsToUse) {
      this._mapperConfig.setAnnotationsToUse(annotationsToUse);
   }

   public void setMapper(ObjectMapper m) {
      this._mapperConfig.setMapper(m);
   }

   public JacksonJsonProvider configure(DeserializationConfig.Feature f, boolean state) {
      this._mapperConfig.configure(f, state);
      return this;
   }

   public JacksonJsonProvider configure(SerializationConfig.Feature f, boolean state) {
      this._mapperConfig.configure(f, state);
      return this;
   }

   public JacksonJsonProvider configure(JsonParser.Feature f, boolean state) {
      this._mapperConfig.configure(f, state);
      return this;
   }

   public JacksonJsonProvider configure(JsonGenerator.Feature f, boolean state) {
      this._mapperConfig.configure(f, state);
      return this;
   }

   public JacksonJsonProvider enable(DeserializationConfig.Feature f, boolean state) {
      this._mapperConfig.configure(f, true);
      return this;
   }

   public JacksonJsonProvider enable(SerializationConfig.Feature f, boolean state) {
      this._mapperConfig.configure(f, true);
      return this;
   }

   public JacksonJsonProvider enable(JsonParser.Feature f, boolean state) {
      this._mapperConfig.configure(f, true);
      return this;
   }

   public JacksonJsonProvider enable(JsonGenerator.Feature f, boolean state) {
      this._mapperConfig.configure(f, true);
      return this;
   }

   public JacksonJsonProvider disable(DeserializationConfig.Feature f, boolean state) {
      this._mapperConfig.configure(f, false);
      return this;
   }

   public JacksonJsonProvider disable(SerializationConfig.Feature f, boolean state) {
      this._mapperConfig.configure(f, false);
      return this;
   }

   public JacksonJsonProvider disable(JsonParser.Feature f, boolean state) {
      this._mapperConfig.configure(f, false);
      return this;
   }

   public JacksonJsonProvider disable(JsonGenerator.Feature f, boolean state) {
      this._mapperConfig.configure(f, false);
      return this;
   }

   public void addUntouchable(Class<?> type) {
      if (this._cfgCustomUntouchables == null) {
         this._cfgCustomUntouchables = new HashSet();
      }

      this._cfgCustomUntouchables.add(new ClassKey(type));
   }

   public void setJSONPFunctionName(String fname) {
      this._jsonpFunctionName = fname;
   }

   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      if (!this.isJsonType(mediaType)) {
         return false;
      } else if (_untouchables.contains(new ClassKey(type))) {
         return false;
      } else {
         Class[] arr$ = _unreadableClasses;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Class<?> cls = arr$[i$];
            if (cls.isAssignableFrom(type)) {
               return false;
            }
         }

         if (_containedIn(type, this._cfgCustomUntouchables)) {
            return false;
         } else {
            if (this._cfgCheckCanSerialize) {
               ObjectMapper mapper = this.locateMapper(type, mediaType);
               if (!mapper.canDeserialize(mapper.constructType(type))) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
      ObjectMapper mapper = this.locateMapper(type, mediaType);
      JsonParser jp = mapper.getJsonFactory().createJsonParser(entityStream);
      jp.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
      return mapper.readValue(jp, mapper.constructType(genericType));
   }

   public long getSize(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return -1L;
   }

   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      if (!this.isJsonType(mediaType)) {
         return false;
      } else if (_untouchables.contains(new ClassKey(type))) {
         return false;
      } else {
         Class[] arr$ = _unwritableClasses;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Class<?> cls = arr$[i$];
            if (cls.isAssignableFrom(type)) {
               return false;
            }
         }

         if (_containedIn(type, this._cfgCustomUntouchables)) {
            return false;
         } else if (this._cfgCheckCanSerialize && !this.locateMapper(type, mediaType).canSerialize(type)) {
            return false;
         } else {
            return true;
         }
      }
   }

   public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
      ObjectMapper mapper = this.locateMapper(type, mediaType);
      JsonEncoding enc = this.findEncoding(mediaType, httpHeaders);
      JsonGenerator jg = mapper.getJsonFactory().createJsonGenerator(entityStream, enc);
      jg.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
      if (mapper.getSerializationConfig().isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
         jg.useDefaultPrettyPrinter();
      }

      JavaType rootType = null;
      if (genericType != null && value != null && genericType.getClass() != Class.class) {
         rootType = mapper.getTypeFactory().constructType(genericType);
         if (rootType.getRawClass() == Object.class) {
            rootType = null;
         }
      }

      if (this._jsonpFunctionName != null) {
         mapper.writeValue((JsonGenerator)jg, new JSONPObject(this._jsonpFunctionName, value, rootType));
      } else if (rootType != null) {
         mapper.typedWriter(rootType).writeValue(jg, value);
      } else {
         mapper.writeValue(jg, value);
      }

   }

   protected JsonEncoding findEncoding(MediaType mediaType, MultivaluedMap<String, Object> httpHeaders) {
      return JsonEncoding.UTF8;
   }

   protected boolean isJsonType(MediaType mediaType) {
      if (mediaType == null) {
         return true;
      } else {
         String subtype = mediaType.getSubtype();
         return "json".equalsIgnoreCase(subtype) || subtype.endsWith("+json");
      }
   }

   public ObjectMapper locateMapper(Class<?> type, MediaType mediaType) {
      ObjectMapper m = this._mapperConfig.getConfiguredMapper();
      if (m == null) {
         if (this._providers != null) {
            ContextResolver<ObjectMapper> resolver = this._providers.getContextResolver(ObjectMapper.class, mediaType);
            if (resolver == null) {
               resolver = this._providers.getContextResolver(ObjectMapper.class, (MediaType)null);
            }

            if (resolver != null) {
               m = (ObjectMapper)resolver.getContext(type);
            }
         }

         if (m == null) {
            m = this._mapperConfig.getDefaultMapper();
         }
      }

      return m;
   }

   protected static boolean _containedIn(Class<?> mainType, HashSet<ClassKey> set) {
      if (set != null) {
         ClassKey key = new ClassKey(mainType);
         if (set.contains(key)) {
            return true;
         }

         Iterator i$ = ClassUtil.findSuperTypes(mainType, (Class)null).iterator();

         while(i$.hasNext()) {
            Class<?> cls = (Class)i$.next();
            key.reset(cls);
            if (set.contains(key)) {
               return true;
            }
         }
      }

      return false;
   }

   static {
      BASIC_ANNOTATIONS = new Annotations[]{Annotations.JACKSON};
      _untouchables = new HashSet();
      _untouchables.add(new ClassKey(InputStream.class));
      _untouchables.add(new ClassKey(Reader.class));
      _untouchables.add(new ClassKey(OutputStream.class));
      _untouchables.add(new ClassKey(Writer.class));
      _untouchables.add(new ClassKey(byte[].class));
      _untouchables.add(new ClassKey(char[].class));
      _untouchables.add(new ClassKey(String.class));
      _untouchables.add(new ClassKey(StreamingOutput.class));
      _untouchables.add(new ClassKey(Response.class));
      _unreadableClasses = new Class[]{InputStream.class, Reader.class};
      _unwritableClasses = new Class[]{OutputStream.class, Writer.class, StreamingOutput.class, Response.class};
   }
}
