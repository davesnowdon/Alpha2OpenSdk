package org.codehaus.jackson.map.jsontype.impl;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;

public abstract class TypeSerializerBase extends TypeSerializer {
   protected final TypeIdResolver _idResolver;
   protected final BeanProperty _property;

   protected TypeSerializerBase(TypeIdResolver idRes, BeanProperty property) {
      this._idResolver = idRes;
      this._property = property;
   }

   public abstract JsonTypeInfo.As getTypeInclusion();

   public String getPropertyName() {
      return null;
   }

   public TypeIdResolver getTypeIdResolver() {
      return this._idResolver;
   }
}
