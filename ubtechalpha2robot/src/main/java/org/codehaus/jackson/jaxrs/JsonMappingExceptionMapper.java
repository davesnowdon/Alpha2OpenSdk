package org.codehaus.jackson.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.map.JsonMappingException;

@Provider
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {
   public JsonMappingExceptionMapper() {
   }

   public Response toResponse(JsonMappingException exception) {
      return Response.status(Status.BAD_REQUEST).entity(exception.getMessage()).type("text/plain").build();
   }
}
