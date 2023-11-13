package org.codehaus.jackson.map.ext;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.FromStringDeserializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public abstract class DOMDeserializer extends FromStringDeserializer {
   static final DocumentBuilderFactory _parserFactory = DocumentBuilderFactory.newInstance();

   protected DOMDeserializer(Class cls) {
      super(cls);
   }

   public abstract Object _deserialize(String var1, DeserializationContext var2);

   protected final Document parse(String value) throws IllegalArgumentException {
      try {
         return _parserFactory.newDocumentBuilder().parse(new InputSource(new StringReader(value)));
      } catch (Exception var3) {
         throw new IllegalArgumentException("Failed to parse JSON String as XML: " + var3.getMessage(), var3);
      }
   }

   static {
      _parserFactory.setNamespaceAware(true);
   }

   public static class DocumentDeserializer extends DOMDeserializer<Document> {
      public DocumentDeserializer() {
         super(Document.class);
      }

      public Document _deserialize(String value, DeserializationContext ctxt) throws IllegalArgumentException {
         return this.parse(value);
      }
   }

   public static class NodeDeserializer extends DOMDeserializer<Node> {
      public NodeDeserializer() {
         super(Node.class);
      }

      public Node _deserialize(String value, DeserializationContext ctxt) throws IllegalArgumentException {
         return this.parse(value);
      }
   }
}
