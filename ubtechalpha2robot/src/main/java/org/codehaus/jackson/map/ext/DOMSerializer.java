package org.codehaus.jackson.map.ext;

import java.io.IOException;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.SerializerBase;
import org.w3c.dom.Node;
import org.w3c.dom.DOMImplementationSource;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class DOMSerializer extends SerializerBase<Node> {
   protected final DOMImplementationLS _domImpl;

   public DOMSerializer() {
      super(Node.class);

      DOMImplementationSource registry;
      try {
         registry = DOMImplementationRegistry.newInstance();
      } catch (Exception var3) {
         throw new IllegalStateException("Could not instantiate DOMImplementationRegistry: " + var3.getMessage(), var3);
      }

      this._domImpl = (DOMImplementationLS)registry.getDOMImplementation("LS");
   }

   public void serialize(Node value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      if (this._domImpl == null) {
         throw new IllegalStateException("Could not find DOM LS");
      } else {
         LSSerializer writer = this._domImpl.createLSSerializer();
         jgen.writeString(writer.writeToString(value));
      }
   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      return this.createSchemaNode("string", true);
   }
}
