package org.codehaus.jackson.xc;

import java.io.IOException;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.SerializerBase;
import org.codehaus.jackson.node.ObjectNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomElementJsonSerializer extends SerializerBase<Element> {
   public DomElementJsonSerializer() {
      super(Element.class);
   }

   public void serialize(Element value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      jgen.writeStartObject();
      jgen.writeStringField("name", value.getTagName());
      if (value.getNamespaceURI() != null) {
         jgen.writeStringField("namespace", value.getNamespaceURI());
      }

      NamedNodeMap attributes = value.getAttributes();
      if (attributes != null && attributes.getLength() > 0) {
         jgen.writeArrayFieldStart("attributes");

         for(int i = 0; i < attributes.getLength(); ++i) {
            Attr attribute = (Attr)attributes.item(i);
            jgen.writeStartObject();
            jgen.writeStringField("$", attribute.getValue());
            jgen.writeStringField("name", attribute.getName());
            String ns = attribute.getNamespaceURI();
            if (ns != null) {
               jgen.writeStringField("namespace", ns);
            }

            jgen.writeEndObject();
         }

         jgen.writeEndArray();
      }

      NodeList children = value.getChildNodes();
      if (children != null && children.getLength() > 0) {
         jgen.writeArrayFieldStart("children");

         for(int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            switch(child.getNodeType()) {
            case 1:
               this.serialize((Element)child, jgen, provider);
            case 2:
            default:
               break;
            case 3:
            case 4:
               jgen.writeStartObject();
               jgen.writeStringField("$", child.getNodeValue());
               jgen.writeEndObject();
            }
         }

         jgen.writeEndArray();
      }

      jgen.writeEndObject();
   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      ObjectNode o = this.createSchemaNode("object", true);
      o.put("name", (JsonNode)this.createSchemaNode("string"));
      o.put("namespace", (JsonNode)this.createSchemaNode("string", true));
      o.put("attributes", (JsonNode)this.createSchemaNode("array", true));
      o.put("children", (JsonNode)this.createSchemaNode("array", true));
      return o;
   }
}
