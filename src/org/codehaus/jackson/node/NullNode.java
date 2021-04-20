package org.codehaus.jackson.node;

import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.SerializerProvider;

public final class NullNode extends ValueNode {
   public static final NullNode instance = new NullNode();

   private NullNode() {
   }

   public static NullNode getInstance() {
      return instance;
   }

   public JsonToken asToken() {
      return JsonToken.VALUE_NULL;
   }

   public boolean isNull() {
      return true;
   }

   public String getValueAsText() {
      return "null";
   }

   public int getValueAsInt(int defaultValue) {
      return 0;
   }

   public long getValueAsLong(long defaultValue) {
      return 0L;
   }

   public double getValueAsDouble(double defaultValue) {
      return 0.0D;
   }

   public final void serialize(JsonGenerator jg, SerializerProvider provider) throws IOException, JsonProcessingException {
      jg.writeNull();
   }

   public boolean equals(Object o) {
      return o == this;
   }
}
