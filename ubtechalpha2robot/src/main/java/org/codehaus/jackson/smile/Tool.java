package org.codehaus.jackson.smile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class Tool {
   public static final String SUFFIX = ".lzf";
   public final JsonFactory jsonFactory = new JsonFactory();
   public final SmileFactory smileFactory = new SmileFactory();

   public Tool() {
      this.smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_NAMES, true);
      this.smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES, true);
      this.smileFactory.configure(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT, true);
      this.smileFactory.configure(SmileGenerator.Feature.WRITE_HEADER, true);
      this.smileFactory.configure(SmileGenerator.Feature.WRITE_END_MARKER, false);
      this.smileFactory.configure(SmileParser.Feature.REQUIRE_HEADER, false);
   }

   private void process(String[] args) throws IOException {
      String oper = null;
      String filename = null;
      if (args.length == 2) {
         oper = args[0];
         filename = args[1];
      } else if (args.length == 1) {
         oper = args[0];
      } else {
         this.showUsage();
      }

      boolean encode = "-e".equals(oper);
      if (encode) {
         this.encode(this.inputStream(filename));
      } else if ("-d".equals(oper)) {
         this.decode(this.inputStream(filename));
      } else if ("-v".equals(oper)) {
         this.verify(this.inputStream(filename), this.inputStream(filename));
      } else {
         this.showUsage();
      }

   }

   private InputStream inputStream(String filename) throws IOException {
      if (filename == null) {
         return System.in;
      } else {
         File src = new File(filename);
         if (!src.exists()) {
            System.err.println("File '" + filename + "' does not exist.");
            System.exit(1);
         }

         return new FileInputStream(src);
      }
   }

   private void decode(InputStream in) throws IOException {
      JsonParser jp = this.smileFactory.createJsonParser(in);
      JsonGenerator jg = this.jsonFactory.createJsonGenerator((OutputStream)System.out, JsonEncoding.UTF8);

      while(jp.nextToken() != null || jp.nextToken() != null) {
         jg.copyCurrentEvent(jp);
      }

      jp.close();
      jg.close();
   }

   private void encode(InputStream in) throws IOException {
      JsonParser jp = this.jsonFactory.createJsonParser(in);
      SmileGenerator jg = this.smileFactory.createJsonGenerator(System.out, JsonEncoding.UTF8);

      while(jp.nextToken() != null) {
         jg.copyCurrentEvent(jp);
      }

      jp.close();
      jg.close();
   }

   private void verify(InputStream in, InputStream in2) throws IOException {
      JsonParser jp = this.jsonFactory.createJsonParser(in);
      ByteArrayOutputStream bytes = new ByteArrayOutputStream(4000);
      SmileGenerator jg = this.smileFactory.createJsonGenerator(bytes, JsonEncoding.UTF8);

      while(jp.nextToken() != null) {
         jg.copyCurrentEvent(jp);
      }

      jp.close();
      jg.close();
      jp = this.jsonFactory.createJsonParser(in2);
      byte[] smile = bytes.toByteArray();
      JsonParser jp2 = this.smileFactory.createJsonParser(smile);
      int count = 0;

      String text1;
      String text2;
      do {
         JsonToken t;
         if ((t = jp.nextToken()) == null) {
            System.out.println("OK: verified " + count + " tokens (from " + smile.length + " bytes of Smile encoded data), input and encoded contents are identical");
            return;
         }

         JsonToken t2 = jp2.nextToken();
         ++count;
         if (t != t2) {
            throw new IOException("Input and encoded differ, token #" + count + "; expected " + t + ", got " + t2);
         }

         text1 = jp.getText();
         text2 = jp2.getText();
      } while(text1.equals(text2));

      throw new IOException("Input and encoded differ, token #" + count + "; expected text '" + text1 + "', got '" + text2 + "'");
   }

   protected void showUsage() {
      System.err.println("Usage: java " + this.getClass().getName() + " -e/-d [file]");
      System.err.println(" (if no file given, reads from stdin -- always writes to stdout)");
      System.err.println(" -d: decode Smile encoded input as JSON");
      System.err.println(" -e: encode JSON (text) input as Smile");
      System.err.println(" -v: encode JSON (text) input as Smile; read back, verify, do not write out");
      System.exit(1);
   }

   public static void main(String[] args) throws IOException {
      (new Tool()).process(args);
   }
}
