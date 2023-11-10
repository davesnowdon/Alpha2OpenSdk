package org.codehaus.jackson.map;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import org.codehaus.jackson.FormatSchema;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.PrettyPrinter;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.io.SegmentedStringWriter;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.codehaus.jackson.util.MinimalPrettyPrinter;
import org.codehaus.jackson.util.VersionUtil;

public class ObjectWriter implements Versioned {
   protected static final PrettyPrinter NULL_PRETTY_PRINTER = new MinimalPrettyPrinter();
   protected final SerializationConfig _config;
   protected final SerializerProvider _provider;
   protected final SerializerFactory _serializerFactory;
   protected final JsonFactory _jsonFactory;
   protected final JavaType _rootType;
   protected final PrettyPrinter _prettyPrinter;
   protected final FormatSchema _schema;

   protected ObjectWriter(ObjectMapper mapper, SerializationConfig config, JavaType rootType, PrettyPrinter pp) {
      this._config = config;
      this._provider = mapper._serializerProvider;
      this._serializerFactory = mapper._serializerFactory;
      this._jsonFactory = mapper._jsonFactory;
      this._rootType = rootType;
      this._prettyPrinter = pp;
      this._schema = null;
   }

   protected ObjectWriter(ObjectMapper mapper, SerializationConfig config) {
      this._config = config;
      this._provider = mapper._serializerProvider;
      this._serializerFactory = mapper._serializerFactory;
      this._jsonFactory = mapper._jsonFactory;
      this._rootType = null;
      this._prettyPrinter = null;
      this._schema = null;
   }

   protected ObjectWriter(ObjectMapper mapper, SerializationConfig config, FormatSchema s) {
      this._config = config;
      this._provider = mapper._serializerProvider;
      this._serializerFactory = mapper._serializerFactory;
      this._jsonFactory = mapper._jsonFactory;
      this._rootType = null;
      this._prettyPrinter = null;
      this._schema = s;
   }

   protected ObjectWriter(ObjectWriter base, SerializationConfig config, JavaType rootType, PrettyPrinter pp, FormatSchema s) {
      this._config = config;
      this._provider = base._provider;
      this._serializerFactory = base._serializerFactory;
      this._jsonFactory = base._jsonFactory;
      this._rootType = rootType;
      this._prettyPrinter = pp;
      this._schema = s;
   }

   protected ObjectWriter(ObjectWriter base, SerializationConfig config) {
      this._config = config;
      this._provider = base._provider;
      this._serializerFactory = base._serializerFactory;
      this._jsonFactory = base._jsonFactory;
      this._schema = base._schema;
      this._rootType = base._rootType;
      this._prettyPrinter = base._prettyPrinter;
   }

   public Version version() {
      return VersionUtil.versionFor(this.getClass());
   }

   public ObjectWriter withView(Class<?> view) {
      return view == this._config.getSerializationView() ? this : new ObjectWriter(this, this._config.withView(view));
   }

   public ObjectWriter withType(JavaType rootType) {
      return rootType == this._rootType ? this : new ObjectWriter(this, this._config, rootType, this._prettyPrinter, this._schema);
   }

   public ObjectWriter withType(Class<?> rootType) {
      return this.withType(this._config.constructType(rootType));
   }

   public ObjectWriter withType(TypeReference<?> rootType) {
      return this.withType(this._config.getTypeFactory().constructType(rootType.getType()));
   }

   public ObjectWriter withPrettyPrinter(PrettyPrinter pp) {
      if (pp == this._prettyPrinter) {
         return this;
      } else {
         if (pp == null) {
            pp = NULL_PRETTY_PRINTER;
         }

         return new ObjectWriter(this, this._config, this._rootType, pp, this._schema);
      }
   }

   public ObjectWriter withDefaultPrettyPrinter() {
      return this.withPrettyPrinter(new DefaultPrettyPrinter());
   }

   public ObjectWriter withFilters(FilterProvider filterProvider) {
      return filterProvider == this._config.getFilterProvider() ? this : new ObjectWriter(this, this._config.withFilters(filterProvider));
   }

   public ObjectWriter withSchema(FormatSchema schema) {
      return this._schema == schema ? this : new ObjectWriter(this, this._config, this._rootType, this._prettyPrinter, schema);
   }

   public void writeValue(JsonGenerator jgen, Object value) throws IOException, JsonGenerationException, JsonMappingException {
      if (this._config.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
         this._writeCloseableValue(jgen, value, this._config);
      } else {
         if (this._rootType == null) {
            this._provider.serializeValue(this._config, jgen, value, this._serializerFactory);
         } else {
            this._provider.serializeValue(this._config, jgen, value, this._rootType, this._serializerFactory);
         }

         if (this._config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
         }
      }

   }

   public void writeValue(File resultFile, Object value) throws IOException, JsonGenerationException, JsonMappingException {
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator(resultFile, JsonEncoding.UTF8), value);
   }

   public void writeValue(OutputStream out, Object value) throws IOException, JsonGenerationException, JsonMappingException {
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8), value);
   }

   public void writeValue(Writer w, Object value) throws IOException, JsonGenerationException, JsonMappingException {
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator(w), value);
   }

   public String writeValueAsString(Object value) throws IOException, JsonGenerationException, JsonMappingException {
      SegmentedStringWriter sw = new SegmentedStringWriter(this._jsonFactory._getBufferRecycler());
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator((Writer)sw), value);
      return sw.getAndClear();
   }

   public byte[] writeValueAsBytes(Object value) throws IOException, JsonGenerationException, JsonMappingException {
      ByteArrayBuilder bb = new ByteArrayBuilder(this._jsonFactory._getBufferRecycler());
      this._configAndWriteValue(this._jsonFactory.createJsonGenerator((OutputStream)bb, JsonEncoding.UTF8), value);
      byte[] result = bb.toByteArray();
      bb.release();
      return result;
   }

   public boolean canSerialize(Class<?> type) {
      return this._provider.hasSerializerFor(this._config, type, this._serializerFactory);
   }

   protected final void _configAndWriteValue(JsonGenerator jgen, Object value) throws IOException, JsonGenerationException, JsonMappingException {
      if (this._prettyPrinter != null) {
         PrettyPrinter pp = this._prettyPrinter;
         jgen.setPrettyPrinter(pp == NULL_PRETTY_PRINTER ? null : pp);
      } else if (this._config.isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
         jgen.useDefaultPrettyPrinter();
      }

      if (this._schema != null) {
         jgen.setSchema(this._schema);
      }

      if (this._config.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
         this._configAndWriteCloseable(jgen, value, this._config);
      } else {
         boolean closed = false;

         try {
            if (this._rootType == null) {
               this._provider.serializeValue(this._config, jgen, value, this._serializerFactory);
            } else {
               this._provider.serializeValue(this._config, jgen, value, this._rootType, this._serializerFactory);
            }

            closed = true;
            jgen.close();
         } finally {
            if (!closed) {
               try {
                  jgen.close();
               } catch (IOException var10) {
               }
            }

         }

      }
   }

   private final void _configAndWriteCloseable(JsonGenerator jgen, Object value, SerializationConfig cfg) throws IOException, JsonGenerationException, JsonMappingException {
      Closeable toClose = (Closeable)value;

      try {
         if (this._rootType == null) {
            this._provider.serializeValue(cfg, jgen, value, this._serializerFactory);
         } else {
            this._provider.serializeValue(cfg, jgen, value, this._rootType, this._serializerFactory);
         }

         if (this._schema != null) {
            jgen.setSchema(this._schema);
         }

         JsonGenerator tmpJgen = jgen;
         jgen = null;
         tmpJgen.close();
         Closeable tmpToClose = toClose;
         toClose = null;
         tmpToClose.close();
      } finally {
         if (jgen != null) {
            try {
               jgen.close();
            } catch (IOException var15) {
            }
         }

         if (toClose != null) {
            try {
               toClose.close();
            } catch (IOException var14) {
            }
         }

      }

   }

   private final void _writeCloseableValue(JsonGenerator jgen, Object value, SerializationConfig cfg) throws IOException, JsonGenerationException, JsonMappingException {
      Closeable toClose = (Closeable)value;

      try {
         if (this._rootType == null) {
            this._provider.serializeValue(cfg, jgen, value, this._serializerFactory);
         } else {
            this._provider.serializeValue(cfg, jgen, value, this._rootType, this._serializerFactory);
         }

         if (this._config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
         }

         Closeable tmpToClose = toClose;
         toClose = null;
         tmpToClose.close();
      } finally {
         if (toClose != null) {
            try {
               toClose.close();
            } catch (IOException var11) {
            }
         }

      }

   }
}
