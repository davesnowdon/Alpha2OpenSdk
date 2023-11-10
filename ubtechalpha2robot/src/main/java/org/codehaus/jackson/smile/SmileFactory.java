package org.codehaus.jackson.smile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.format.InputAccessor;
import org.codehaus.jackson.format.MatchStrength;
import org.codehaus.jackson.io.IOContext;

public class SmileFactory extends JsonFactory {
   public static final String FORMAT_NAME_SMILE = "Smile";
   static final int DEFAULT_SMILE_PARSER_FEATURE_FLAGS = SmileParser.Feature.collectDefaults();
   static final int DEFAULT_SMILE_GENERATOR_FEATURE_FLAGS = SmileGenerator.Feature.collectDefaults();
   protected boolean _cfgDelegateToTextual;
   protected int _smileParserFeatures;
   protected int _smileGeneratorFeatures;

   public SmileFactory() {
      this((ObjectCodec)null);
   }

   public SmileFactory(ObjectCodec oc) {
      super(oc);
      this._smileParserFeatures = DEFAULT_SMILE_PARSER_FEATURE_FLAGS;
      this._smileGeneratorFeatures = DEFAULT_SMILE_GENERATOR_FEATURE_FLAGS;
   }

   public void delegateToTextual(boolean state) {
      this._cfgDelegateToTextual = state;
   }

   public String getFormatName() {
      return "Smile";
   }

   public MatchStrength hasFormat(InputAccessor acc) throws IOException {
      return SmileParserBootstrapper.hasSmileFormat(acc);
   }

   public final SmileFactory configure(SmileParser.Feature f, boolean state) {
      if (state) {
         this.enable(f);
      } else {
         this.disable(f);
      }

      return this;
   }

   public SmileFactory enable(SmileParser.Feature f) {
      this._smileParserFeatures |= f.getMask();
      return this;
   }

   public SmileFactory disable(SmileParser.Feature f) {
      this._smileParserFeatures &= ~f.getMask();
      return this;
   }

   public final boolean isEnabled(SmileParser.Feature f) {
      return (this._smileParserFeatures & f.getMask()) != 0;
   }

   public final SmileFactory configure(SmileGenerator.Feature f, boolean state) {
      if (state) {
         this.enable(f);
      } else {
         this.disable(f);
      }

      return this;
   }

   public SmileFactory enable(SmileGenerator.Feature f) {
      this._smileGeneratorFeatures |= f.getMask();
      return this;
   }

   public SmileFactory disable(SmileGenerator.Feature f) {
      this._smileGeneratorFeatures &= ~f.getMask();
      return this;
   }

   public final boolean isEnabled(SmileGenerator.Feature f) {
      return (this._smileGeneratorFeatures & f.getMask()) != 0;
   }

   public SmileParser createJsonParser(File f) throws IOException, JsonParseException {
      return this._createJsonParser((InputStream)(new FileInputStream(f)), this._createContext(f, true));
   }

   public SmileParser createJsonParser(URL url) throws IOException, JsonParseException {
      return this._createJsonParser(this._optimizedStreamFromURL(url), this._createContext(url, true));
   }

   public SmileParser createJsonParser(InputStream in) throws IOException, JsonParseException {
      return this._createJsonParser(in, this._createContext(in, false));
   }

   public SmileParser createJsonParser(byte[] data) throws IOException, JsonParseException {
      return this._createJsonParser(data, 0, data.length, this._createContext(data, true));
   }

   public SmileParser createJsonParser(byte[] data, int offset, int len) throws IOException, JsonParseException {
      return this._createJsonParser(data, offset, len, this._createContext(data, true));
   }

   public SmileGenerator createJsonGenerator(OutputStream out, JsonEncoding enc) throws IOException {
      return this.createJsonGenerator(out);
   }

   public SmileGenerator createJsonGenerator(OutputStream out) throws IOException {
      IOContext ctxt = this._createContext(out, false);
      return this._createJsonGenerator(out, ctxt);
   }

   protected SmileParser _createJsonParser(InputStream in, IOContext ctxt) throws IOException, JsonParseException {
      return (new SmileParserBootstrapper(ctxt, in)).constructParser(this._parserFeatures, this._smileParserFeatures, this._objectCodec, this._rootByteSymbols);
   }

   protected JsonParser _createJsonParser(Reader r, IOContext ctxt) throws IOException, JsonParseException {
      if (this._cfgDelegateToTextual) {
         return super._createJsonParser(r, ctxt);
      } else {
         throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
      }
   }

   protected SmileParser _createJsonParser(byte[] data, int offset, int len, IOContext ctxt) throws IOException, JsonParseException {
      return (new SmileParserBootstrapper(ctxt, data, offset, len)).constructParser(this._parserFeatures, this._smileParserFeatures, this._objectCodec, this._rootByteSymbols);
   }

   protected JsonGenerator _createJsonGenerator(Writer out, IOContext ctxt) throws IOException {
      if (this._cfgDelegateToTextual) {
         return super._createJsonGenerator(out, ctxt);
      } else {
         throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
      }
   }

   protected Writer _createWriter(OutputStream out, JsonEncoding enc, IOContext ctxt) throws IOException {
      if (this._cfgDelegateToTextual) {
         return super._createWriter(out, enc, ctxt);
      } else {
         throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
      }
   }

   protected SmileGenerator _createJsonGenerator(OutputStream out, IOContext ctxt) throws IOException {
      int feats = this._smileGeneratorFeatures;
      SmileGenerator gen = new SmileGenerator(ctxt, this._generatorFeatures, feats, this._objectCodec, out);
      if ((feats & SmileGenerator.Feature.WRITE_HEADER.getMask()) != 0) {
         gen.writeHeader();
      } else {
         if ((feats & SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES.getMask()) != 0) {
            throw new JsonGenerationException("Inconsistent settings: WRITE_HEADER disabled, but CHECK_SHARED_STRING_VALUES enabled; can not construct generator due to possible data loss (either enable WRITE_HEADER, or disable CHECK_SHARED_STRING_VALUES to resolve)");
         }

         if ((feats & SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT.getMask()) == 0) {
            throw new JsonGenerationException("Inconsistent settings: WRITE_HEADER disabled, but ENCODE_BINARY_AS_7BIT disabled; can not construct generator due to possible data loss (either enable WRITE_HEADER, or ENCODE_BINARY_AS_7BIT to resolve)");
         }
      }

      return gen;
   }
}
