package org.codehaus.jackson.map;

import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;

public abstract class TypeSerializer {
   public TypeSerializer() {
   }

   public abstract JsonTypeInfo.As getTypeInclusion();

   public abstract String getPropertyName();

   public abstract TypeIdResolver getTypeIdResolver();

   public abstract void writeTypePrefixForScalar(Object var1, JsonGenerator var2) throws IOException, JsonProcessingException;

   public abstract void writeTypePrefixForObject(Object var1, JsonGenerator var2) throws IOException, JsonProcessingException;

   public abstract void writeTypePrefixForArray(Object var1, JsonGenerator var2) throws IOException, JsonProcessingException;

   public abstract void writeTypeSuffixForScalar(Object var1, JsonGenerator var2) throws IOException, JsonProcessingException;

   public abstract void writeTypeSuffixForObject(Object var1, JsonGenerator var2) throws IOException, JsonProcessingException;

   public abstract void writeTypeSuffixForArray(Object var1, JsonGenerator var2) throws IOException, JsonProcessingException;

   public void writeTypePrefixForScalar(Object value, JsonGenerator jgen, Class<?> type) throws IOException, JsonProcessingException {
      this.writeTypePrefixForScalar(value, jgen);
   }

   public void writeTypePrefixForObject(Object value, JsonGenerator jgen, Class<?> type) throws IOException, JsonProcessingException {
      this.writeTypePrefixForObject(value, jgen);
   }

   public void writeTypePrefixForArray(Object value, JsonGenerator jgen, Class<?> type) throws IOException, JsonProcessingException {
      this.writeTypePrefixForArray(value, jgen);
   }
}
