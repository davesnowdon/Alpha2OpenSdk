package org.codehaus.jackson.xc;

import java.io.IOException;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.codehaus.jackson.node.ArrayNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DomElementJsonDeserializer extends StdDeserializer<Element> {
   private final DocumentBuilder builder;

   public DomElementJsonDeserializer() {
      super(Element.class);

      try {
         DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
         bf.setNamespaceAware(true);
         this.builder = bf.newDocumentBuilder();
      } catch (ParserConfigurationException var2) {
         throw new RuntimeException();
      }
   }

   public DomElementJsonDeserializer(DocumentBuilder builder) {
      super(Element.class);
      this.builder = builder;
   }

   public Element deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      Document document = this.builder.newDocument();
      return this.fromNode(document, jp.readValueAsTree());
   }

   protected Element fromNode(Document document, JsonNode jsonNode) throws IOException {
      String ns = jsonNode.get("namespace") != null ? jsonNode.get("namespace").getValueAsText() : null;
      String name = jsonNode.get("name") != null ? jsonNode.get("name").getValueAsText() : null;
      if (name == null) {
         throw new JsonMappingException("No name for DOM element was provided in the JSON object.");
      } else {
         Element element = document.createElementNS(ns, name);
         JsonNode attributesNode = jsonNode.get("attributes");
         if (attributesNode != null && attributesNode instanceof ArrayNode) {
            Iterator atts = attributesNode.getElements();

            while(atts.hasNext()) {
               JsonNode node = (JsonNode)atts.next();
               ns = node.get("namespace") != null ? node.get("namespace").getValueAsText() : null;
               name = node.get("name") != null ? node.get("name").getValueAsText() : null;
               String value = node.get("$") != null ? node.get("$").getValueAsText() : null;
               if (name != null) {
                  element.setAttributeNS(ns, name, value);
               }
            }
         }

         JsonNode childsNode = jsonNode.get("children");
         if (childsNode != null && childsNode instanceof ArrayNode) {
            Iterator els = childsNode.getElements();

            while(els.hasNext()) {
               JsonNode node = (JsonNode)els.next();
               name = node.get("name") != null ? node.get("name").getValueAsText() : null;
               String value = node.get("$") != null ? node.get("$").getValueAsText() : null;
               if (value != null) {
                  element.appendChild(document.createTextNode(value));
               } else if (name != null) {
                  element.appendChild(this.fromNode(document, node));
               }
            }
         }

         return element;
      }
   }
}
