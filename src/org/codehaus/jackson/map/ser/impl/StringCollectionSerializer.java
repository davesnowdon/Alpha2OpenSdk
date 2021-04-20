package org.codehaus.jackson.map.ser.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ResolvableSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;

@JacksonStdImpl
public class StringCollectionSerializer extends StaticListSerializerBase<Collection<String>> implements ResolvableSerializer {
   protected JsonSerializer<String> _serializer;

   public StringCollectionSerializer(BeanProperty property) {
      super(Collection.class, property);
   }

   protected JsonNode contentSchema() {
      return this.createSchemaNode("string", true);
   }

   public void resolve(SerializerProvider provider) throws JsonMappingException {
      JsonSerializer<?> ser = provider.findValueSerializer(String.class, this._property);
      if (!this.isDefaultSerializer(ser)) {
         this._serializer = ser;
      }

   }

   public void serialize(Collection<String> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      jgen.writeStartArray();
      if (this._serializer == null) {
         this.serializeContents(value, jgen, provider);
      } else {
         this.serializeUsingCustom(value, jgen, provider);
      }

      jgen.writeEndArray();
   }

   public void serializeWithType(Collection<String> value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
      typeSer.writeTypePrefixForArray(value, jgen);
      if (this._serializer == null) {
         this.serializeContents(value, jgen, provider);
      } else {
         this.serializeUsingCustom(value, jgen, provider);
      }

      typeSer.writeTypeSuffixForArray(value, jgen);
   }

   private final void serializeContents(Collection<String> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      if (this._serializer != null) {
         this.serializeUsingCustom(value, jgen, provider);
      } else {
         int i = 0;
         Iterator i$ = value.iterator();

         while(i$.hasNext()) {
            String str = (String)i$.next();

            try {
               if (str == null) {
                  provider.defaultSerializeNull(jgen);
               } else {
                  jgen.writeString(str);
               }

               ++i;
            } catch (Exception var8) {
               this.wrapAndThrow(provider, var8, value, i);
            }
         }

      }
   }

   private void serializeUsingCustom(Collection<String> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      JsonSerializer<String> ser = this._serializer;
      int i = 0;
      Iterator i$ = value.iterator();

      while(i$.hasNext()) {
         String str = (String)i$.next();

         try {
            if (str == null) {
               provider.defaultSerializeNull(jgen);
            } else {
               ser.serialize(str, jgen, provider);
            }
         } catch (Exception var9) {
            this.wrapAndThrow(provider, var9, value, i);
         }
      }

   }
}
