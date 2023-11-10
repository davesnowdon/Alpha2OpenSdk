package org.codehaus.jackson.node;

import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.SerializerProvider;

public final class POJONode extends ValueNode {
   protected final Object _value;

   public POJONode(Object v) {
      this._value = v;
   }

   public JsonToken asToken() {
      return JsonToken.VALUE_EMBEDDED_OBJECT;
   }

   public boolean isPojo() {
      return true;
   }

   public String getValueAsText() {
      return this._value == null ? "null" : this._value.toString();
   }

   public boolean getValueAsBoolean(boolean defaultValue) {
      return this._value != null && this._value instanceof Boolean ? (Boolean)this._value : defaultValue;
   }

   public int getValueAsInt(int defaultValue) {
      return this._value instanceof Number ? ((Number)this._value).intValue() : defaultValue;
   }

   public long getValueAsLong(long defaultValue) {
      return this._value instanceof Number ? ((Number)this._value).longValue() : defaultValue;
   }

   public double getValueAsDouble(double defaultValue) {
      return this._value instanceof Number ? ((Number)this._value).doubleValue() : defaultValue;
   }

   public final void serialize(JsonGenerator jg, SerializerProvider provider) throws IOException, JsonProcessingException {
      if (this._value == null) {
         jg.writeNull();
      } else {
         jg.writeObject(this._value);
      }

   }

   public Object getPojo() {
      return this._value;
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         POJONode other = (POJONode)o;
         if (this._value == null) {
            return other._value == null;
         } else {
            return this._value.equals(other._value);
         }
      }
   }

   public int hashCode() {
      return this._value.hashCode();
   }

   public String toString() {
      return String.valueOf(this._value);
   }
}
