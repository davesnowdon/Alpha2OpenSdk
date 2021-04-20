package org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;
import java.util.HashMap;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.type.JavaType;

public abstract class TypeDeserializerBase extends TypeDeserializer {
   protected final TypeIdResolver _idResolver;
   protected final JavaType _baseType;
   protected final BeanProperty _property;
   protected final HashMap<String, JsonDeserializer<Object>> _deserializers;

   protected TypeDeserializerBase(JavaType baseType, TypeIdResolver idRes, BeanProperty property) {
      this._baseType = baseType;
      this._idResolver = idRes;
      this._property = property;
      this._deserializers = new HashMap();
   }

   public abstract JsonTypeInfo.As getTypeInclusion();

   public String baseTypeName() {
      return this._baseType.getRawClass().getName();
   }

   public String getPropertyName() {
      return null;
   }

   public TypeIdResolver getTypeIdResolver() {
      return this._idResolver;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append('[').append(this.getClass().getName());
      sb.append("; base-type:").append(this._baseType);
      sb.append("; id-resolver: ").append(this._idResolver);
      sb.append(']');
      return sb.toString();
   }

   protected final JsonDeserializer<Object> _findDeserializer(DeserializationContext ctxt, String typeId) throws IOException, JsonProcessingException {
      synchronized(this._deserializers) {
         JsonDeserializer<Object> deser = (JsonDeserializer)this._deserializers.get(typeId);
         if (deser == null) {
            JavaType type = this._idResolver.typeFromId(typeId);
            if (type == null) {
               throw ctxt.unknownTypeException(this._baseType, typeId);
            }

            if (this._baseType != null && this._baseType.getClass() == type.getClass()) {
               type = this._baseType.narrowBy(type.getRawClass());
            }

            deser = ctxt.getDeserializerProvider().findValueDeserializer(ctxt.getConfig(), type, this._property);
            this._deserializers.put(typeId, deser);
         }

         return deser;
      }
   }
}
