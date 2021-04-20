package org.codehaus.jackson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.ref.SoftReference;
import java.net.URL;
import org.codehaus.jackson.format.InputAccessor;
import org.codehaus.jackson.format.MatchStrength;
import org.codehaus.jackson.impl.ByteSourceBootstrapper;
import org.codehaus.jackson.impl.ReaderBasedParser;
import org.codehaus.jackson.impl.Utf8Generator;
import org.codehaus.jackson.impl.WriterBasedGenerator;
import org.codehaus.jackson.io.CharacterEscapes;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.io.InputDecorator;
import org.codehaus.jackson.io.OutputDecorator;
import org.codehaus.jackson.io.UTF8Writer;
import org.codehaus.jackson.sym.BytesToNameCanonicalizer;
import org.codehaus.jackson.sym.CharsToNameCanonicalizer;
import org.codehaus.jackson.util.BufferRecycler;
import org.codehaus.jackson.util.VersionUtil;

public class JsonFactory implements Versioned {
   public static final String FORMAT_NAME_JSON = "JSON";
   static final int DEFAULT_PARSER_FEATURE_FLAGS = JsonParser.Feature.collectDefaults();
   static final int DEFAULT_GENERATOR_FEATURE_FLAGS = JsonGenerator.Feature.collectDefaults();
   protected static final ThreadLocal<SoftReference<BufferRecycler>> _recyclerRef = new ThreadLocal();
   protected CharsToNameCanonicalizer _rootCharSymbols;
   protected BytesToNameCanonicalizer _rootByteSymbols;
   protected ObjectCodec _objectCodec;
   protected int _parserFeatures;
   protected int _generatorFeatures;
   protected CharacterEscapes _characterEscapes;
   protected InputDecorator _inputDecorator;
   protected OutputDecorator _outputDecorator;

   public JsonFactory() {
      this((ObjectCodec)null);
   }

   public JsonFactory(ObjectCodec oc) {
      this._rootCharSymbols = CharsToNameCanonicalizer.createRoot();
      this._rootByteSymbols = BytesToNameCanonicalizer.createRoot();
      this._parserFeatures = DEFAULT_PARSER_FEATURE_FLAGS;
      this._generatorFeatures = DEFAULT_GENERATOR_FEATURE_FLAGS;
      this._objectCodec = oc;
   }

   public String getFormatName() {
      return this.getClass() == JsonFactory.class ? "JSON" : null;
   }

   public MatchStrength hasFormat(InputAccessor acc) throws IOException {
      return this.getClass() == JsonFactory.class ? this.hasJSONFormat(acc) : null;
   }

   protected MatchStrength hasJSONFormat(InputAccessor acc) throws IOException {
      return ByteSourceBootstrapper.hasJSONFormat(acc);
   }

   public Version version() {
      return VersionUtil.versionFor(Utf8Generator.class);
   }

   public final JsonFactory configure(JsonParser.Feature f, boolean state) {
      if (state) {
         this.enable(f);
      } else {
         this.disable(f);
      }

      return this;
   }

   public JsonFactory enable(JsonParser.Feature f) {
      this._parserFeatures |= f.getMask();
      return this;
   }

   public JsonFactory disable(JsonParser.Feature f) {
      this._parserFeatures &= ~f.getMask();
      return this;
   }

   public final boolean isEnabled(JsonParser.Feature f) {
      return (this._parserFeatures & f.getMask()) != 0;
   }

   /** @deprecated */
   public final void enableParserFeature(JsonParser.Feature f) {
      this.enable(f);
   }

   /** @deprecated */
   public final void disableParserFeature(JsonParser.Feature f) {
      this.disable(f);
   }

   /** @deprecated */
   public final void setParserFeature(JsonParser.Feature f, boolean state) {
      this.configure(f, state);
   }

   /** @deprecated */
   public final boolean isParserFeatureEnabled(JsonParser.Feature f) {
      return (this._parserFeatures & f.getMask()) != 0;
   }

   public InputDecorator getInputDecorator() {
      return this._inputDecorator;
   }

   public JsonFactory setInputDecorator(InputDecorator d) {
      this._inputDecorator = d;
      return this;
   }

   public final JsonFactory configure(JsonGenerator.Feature f, boolean state) {
      if (state) {
         this.enable(f);
      } else {
         this.disable(f);
      }

      return this;
   }

   public JsonFactory enable(JsonGenerator.Feature f) {
      this._generatorFeatures |= f.getMask();
      return this;
   }

   public JsonFactory disable(JsonGenerator.Feature f) {
      this._generatorFeatures &= ~f.getMask();
      return this;
   }

   public final boolean isEnabled(JsonGenerator.Feature f) {
      return (this._generatorFeatures & f.getMask()) != 0;
   }

   /** @deprecated */
   @Deprecated
   public final void enableGeneratorFeature(JsonGenerator.Feature f) {
      this.enable(f);
   }

   /** @deprecated */
   @Deprecated
   public final void disableGeneratorFeature(JsonGenerator.Feature f) {
      this.disable(f);
   }

   /** @deprecated */
   @Deprecated
   public final void setGeneratorFeature(JsonGenerator.Feature f, boolean state) {
      this.configure(f, state);
   }

   /** @deprecated */
   @Deprecated
   public final boolean isGeneratorFeatureEnabled(JsonGenerator.Feature f) {
      return this.isEnabled(f);
   }

   public CharacterEscapes getCharacterEscapes() {
      return this._characterEscapes;
   }

   public JsonFactory setCharacterEscapes(CharacterEscapes esc) {
      this._characterEscapes = esc;
      return this;
   }

   public OutputDecorator getOutputDecorator() {
      return this._outputDecorator;
   }

   public JsonFactory setOutputDecorator(OutputDecorator d) {
      this._outputDecorator = d;
      return this;
   }

   public JsonFactory setCodec(ObjectCodec oc) {
      this._objectCodec = oc;
      return this;
   }

   public ObjectCodec getCodec() {
      return this._objectCodec;
   }

   public JsonParser createJsonParser(File f) throws IOException, JsonParseException {
      IOContext ctxt = this._createContext(f, true);
      InputStream in = new FileInputStream(f);
      if (this._inputDecorator != null) {
         in = this._inputDecorator.decorate(ctxt, (InputStream)in);
      }

      return this._createJsonParser((InputStream)in, ctxt);
   }

   public JsonParser createJsonParser(URL url) throws IOException, JsonParseException {
      IOContext ctxt = this._createContext(url, true);
      InputStream in = this._optimizedStreamFromURL(url);
      if (this._inputDecorator != null) {
         in = this._inputDecorator.decorate(ctxt, in);
      }

      return this._createJsonParser(in, ctxt);
   }

   public JsonParser createJsonParser(InputStream in) throws IOException, JsonParseException {
      IOContext ctxt = this._createContext(in, false);
      if (this._inputDecorator != null) {
         in = this._inputDecorator.decorate(ctxt, in);
      }

      return this._createJsonParser(in, ctxt);
   }

   public JsonParser createJsonParser(Reader r) throws IOException, JsonParseException {
      IOContext ctxt = this._createContext(r, false);
      if (this._inputDecorator != null) {
         r = this._inputDecorator.decorate(ctxt, r);
      }

      return this._createJsonParser(r, ctxt);
   }

   public JsonParser createJsonParser(byte[] data) throws IOException, JsonParseException {
      IOContext ctxt = this._createContext(data, true);
      if (this._inputDecorator != null) {
         InputStream in = this._inputDecorator.decorate(ctxt, data, 0, data.length);
         if (in != null) {
            return this._createJsonParser(in, ctxt);
         }
      }

      return this._createJsonParser(data, 0, data.length, ctxt);
   }

   public JsonParser createJsonParser(byte[] data, int offset, int len) throws IOException, JsonParseException {
      IOContext ctxt = this._createContext(data, true);
      if (this._inputDecorator != null) {
         InputStream in = this._inputDecorator.decorate(ctxt, data, offset, len);
         if (in != null) {
            return this._createJsonParser(in, ctxt);
         }
      }

      return this._createJsonParser(data, offset, len, ctxt);
   }

   public JsonParser createJsonParser(String content) throws IOException, JsonParseException {
      Reader r = new StringReader(content);
      IOContext ctxt = this._createContext(r, true);
      if (this._inputDecorator != null) {
         r = this._inputDecorator.decorate(ctxt, (Reader)r);
      }

      return this._createJsonParser((Reader)r, ctxt);
   }

   public JsonGenerator createJsonGenerator(OutputStream out, JsonEncoding enc) throws IOException {
      IOContext ctxt = this._createContext(out, false);
      ctxt.setEncoding(enc);
      if (enc == JsonEncoding.UTF8) {
         if (this._outputDecorator != null) {
            out = this._outputDecorator.decorate(ctxt, out);
         }

         return this._createUTF8JsonGenerator(out, ctxt);
      } else {
         Writer w = this._createWriter(out, enc, ctxt);
         if (this._outputDecorator != null) {
            w = this._outputDecorator.decorate(ctxt, w);
         }

         return this._createJsonGenerator(w, ctxt);
      }
   }

   public JsonGenerator createJsonGenerator(Writer out) throws IOException {
      IOContext ctxt = this._createContext(out, false);
      if (this._outputDecorator != null) {
         out = this._outputDecorator.decorate(ctxt, out);
      }

      return this._createJsonGenerator(out, ctxt);
   }

   public JsonGenerator createJsonGenerator(OutputStream out) throws IOException {
      return this.createJsonGenerator(out, JsonEncoding.UTF8);
   }

   public JsonGenerator createJsonGenerator(File f, JsonEncoding enc) throws IOException {
      OutputStream out = new FileOutputStream(f);
      IOContext ctxt = this._createContext(out, true);
      ctxt.setEncoding(enc);
      if (enc == JsonEncoding.UTF8) {
         if (this._outputDecorator != null) {
            out = this._outputDecorator.decorate(ctxt, (OutputStream)out);
         }

         return this._createUTF8JsonGenerator((OutputStream)out, ctxt);
      } else {
         Writer w = this._createWriter((OutputStream)out, enc, ctxt);
         if (this._outputDecorator != null) {
            w = this._outputDecorator.decorate(ctxt, w);
         }

         return this._createJsonGenerator(w, ctxt);
      }
   }

   protected JsonParser _createJsonParser(InputStream in, IOContext ctxt) throws IOException, JsonParseException {
      return (new ByteSourceBootstrapper(ctxt, in)).constructParser(this._parserFeatures, this._objectCodec, this._rootByteSymbols, this._rootCharSymbols);
   }

   protected JsonParser _createJsonParser(Reader r, IOContext ctxt) throws IOException, JsonParseException {
      return new ReaderBasedParser(ctxt, this._parserFeatures, r, this._objectCodec, this._rootCharSymbols.makeChild(this.isEnabled(JsonParser.Feature.CANONICALIZE_FIELD_NAMES), this.isEnabled(JsonParser.Feature.INTERN_FIELD_NAMES)));
   }

   protected JsonParser _createJsonParser(byte[] data, int offset, int len, IOContext ctxt) throws IOException, JsonParseException {
      return (new ByteSourceBootstrapper(ctxt, data, offset, len)).constructParser(this._parserFeatures, this._objectCodec, this._rootByteSymbols, this._rootCharSymbols);
   }

   protected JsonGenerator _createJsonGenerator(Writer out, IOContext ctxt) throws IOException {
      WriterBasedGenerator gen = new WriterBasedGenerator(ctxt, this._generatorFeatures, this._objectCodec, out);
      if (this._characterEscapes != null) {
         gen.setCharacterEscapes(this._characterEscapes);
      }

      return gen;
   }

   protected JsonGenerator _createUTF8JsonGenerator(OutputStream out, IOContext ctxt) throws IOException {
      Utf8Generator gen = new Utf8Generator(ctxt, this._generatorFeatures, this._objectCodec, out);
      if (this._characterEscapes != null) {
         gen.setCharacterEscapes(this._characterEscapes);
      }

      return gen;
   }

   protected Writer _createWriter(OutputStream out, JsonEncoding enc, IOContext ctxt) throws IOException {
      return (Writer)(enc == JsonEncoding.UTF8 ? new UTF8Writer(ctxt, out) : new OutputStreamWriter(out, enc.getJavaName()));
   }

   protected IOContext _createContext(Object srcRef, boolean resourceManaged) {
      return new IOContext(this._getBufferRecycler(), srcRef, resourceManaged);
   }

   public BufferRecycler _getBufferRecycler() {
      SoftReference<BufferRecycler> ref = (SoftReference)_recyclerRef.get();
      BufferRecycler br = ref == null ? null : (BufferRecycler)ref.get();
      if (br == null) {
         br = new BufferRecycler();
         _recyclerRef.set(new SoftReference(br));
      }

      return br;
   }

   protected InputStream _optimizedStreamFromURL(URL url) throws IOException {
      if ("file".equals(url.getProtocol())) {
         String host = url.getHost();
         if (host == null || host.length() == 0) {
            return new FileInputStream(url.getPath());
         }
      }

      return url.openStream();
   }
}
