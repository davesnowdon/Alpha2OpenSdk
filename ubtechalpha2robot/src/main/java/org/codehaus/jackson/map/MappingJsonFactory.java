package org.codehaus.jackson.map;

import java.io.IOException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.format.InputAccessor;
import org.codehaus.jackson.format.MatchStrength;

public class MappingJsonFactory extends JsonFactory {
   public MappingJsonFactory() {
      this((ObjectMapper)null);
   }

   public MappingJsonFactory(ObjectMapper mapper) {
      super(mapper);
      if (mapper == null) {
         this.setCodec(new ObjectMapper(this));
      }

   }

   public final ObjectMapper getCodec() {
      return (ObjectMapper)this._objectCodec;
   }

   public String getFormatName() {
      return "JSON";
   }

   public MatchStrength hasFormat(InputAccessor acc) throws IOException {
      return this.hasJSONFormat(acc);
   }
}
