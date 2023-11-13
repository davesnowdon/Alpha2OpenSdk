package org.codehaus.jackson.map.ser.impl;

import java.lang.reflect.Type;
import java.util.Collection;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.SerializerBase;
import org.codehaus.jackson.node.ObjectNode;

public abstract class StaticListSerializerBase extends SerializerBase {
   protected final BeanProperty _property;

   protected StaticListSerializerBase(Class<?> cls, BeanProperty property) {
      super(cls, false);
      this._property = property;
   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      ObjectNode o = this.createSchemaNode("array", true);
      o.put("items", this.contentSchema());
      return o;
   }

   protected abstract JsonNode contentSchema();
}
