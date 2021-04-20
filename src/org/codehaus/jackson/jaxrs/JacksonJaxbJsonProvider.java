package org.codehaus.jackson.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.map.ObjectMapper;

@Provider
@Consumes({"application/json", "text/json"})
@Produces({"application/json", "text/json"})
public class JacksonJaxbJsonProvider extends JacksonJsonProvider {
   public static final Annotations[] DEFAULT_ANNOTATIONS;

   public JacksonJaxbJsonProvider() {
      this((ObjectMapper)null, DEFAULT_ANNOTATIONS);
   }

   public JacksonJaxbJsonProvider(Annotations... annotationsToUse) {
      this((ObjectMapper)null, annotationsToUse);
   }

   public JacksonJaxbJsonProvider(ObjectMapper mapper, Annotations[] annotationsToUse) {
      super(mapper, annotationsToUse);
   }

   static {
      DEFAULT_ANNOTATIONS = new Annotations[]{Annotations.JACKSON, Annotations.JAXB};
   }
}
