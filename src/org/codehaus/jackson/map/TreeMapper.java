package org.codehaus.jackson.map;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.NullNode;

/** @deprecated */
@Deprecated
public class TreeMapper extends JsonNodeFactory {
   protected ObjectMapper _objectMapper;

   public TreeMapper() {
      this((ObjectMapper)null);
   }

   public TreeMapper(ObjectMapper m) {
      this._objectMapper = m;
   }

   public JsonFactory getJsonFactory() {
      return this.objectMapper().getJsonFactory();
   }

   public JsonNode readTree(JsonParser jp) throws IOException, JsonParseException {
      JsonToken t = jp.getCurrentToken();
      if (t == null) {
         t = jp.nextToken();
         if (t == null) {
            return null;
         }
      }

      return this.objectMapper().readTree(jp);
   }

   public JsonNode readTree(File src) throws IOException, JsonParseException {
      JsonNode n = (JsonNode)this.objectMapper().readValue(src, JsonNode.class);
      return (JsonNode)(n == null ? NullNode.instance : n);
   }

   public JsonNode readTree(URL src) throws IOException, JsonParseException {
      JsonNode n = (JsonNode)this.objectMapper().readValue(src, JsonNode.class);
      return (JsonNode)(n == null ? NullNode.instance : n);
   }

   public JsonNode readTree(InputStream src) throws IOException, JsonParseException {
      JsonNode n = (JsonNode)this.objectMapper().readValue(src, JsonNode.class);
      return (JsonNode)(n == null ? NullNode.instance : n);
   }

   public JsonNode readTree(Reader src) throws IOException, JsonParseException {
      JsonNode n = (JsonNode)this.objectMapper().readValue(src, JsonNode.class);
      return (JsonNode)(n == null ? NullNode.instance : n);
   }

   public JsonNode readTree(String jsonContent) throws IOException, JsonParseException {
      JsonNode n = (JsonNode)this.objectMapper().readValue(jsonContent, JsonNode.class);
      return (JsonNode)(n == null ? NullNode.instance : n);
   }

   public JsonNode readTree(byte[] jsonContent) throws IOException, JsonParseException {
      JsonNode n = (JsonNode)this.objectMapper().readValue(jsonContent, 0, jsonContent.length, (Class)JsonNode.class);
      return (JsonNode)(n == null ? NullNode.instance : n);
   }

   public void writeTree(JsonNode rootNode, File dst) throws IOException, JsonParseException {
      this.objectMapper().writeValue((File)dst, rootNode);
   }

   public void writeTree(JsonNode rootNode, Writer dst) throws IOException, JsonParseException {
      this.objectMapper().writeValue((Writer)dst, rootNode);
   }

   public void writeTree(JsonNode rootNode, OutputStream dst) throws IOException, JsonParseException {
      this.objectMapper().writeValue((OutputStream)dst, rootNode);
   }

   protected synchronized ObjectMapper objectMapper() {
      if (this._objectMapper == null) {
         this._objectMapper = new ObjectMapper();
      }

      return this._objectMapper;
   }
}
