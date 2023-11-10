package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.ResolvableDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.type.JavaType;

@JacksonStdImpl
public class MapDeserializer extends ContainerDeserializer<Map<Object, Object>> implements ResolvableDeserializer {
   protected final JavaType _mapType;
   protected final KeyDeserializer _keyDeserializer;
   protected final JsonDeserializer<Object> _valueDeserializer;
   protected final TypeDeserializer _valueTypeDeserializer;
   protected final Constructor<Map<Object, Object>> _defaultCtor;
   protected Creator.PropertyBased _propertyBasedCreator;
   protected HashSet<String> _ignorableProperties;

   public MapDeserializer(JavaType mapType, Constructor<Map<Object, Object>> defCtor, KeyDeserializer keyDeser, JsonDeserializer<Object> valueDeser, TypeDeserializer valueTypeDeser) {
      super(Map.class);
      this._mapType = mapType;
      this._defaultCtor = defCtor;
      this._keyDeserializer = keyDeser;
      this._valueDeserializer = valueDeser;
      this._valueTypeDeserializer = valueTypeDeser;
   }

   public void setCreators(CreatorContainer creators) {
      this._propertyBasedCreator = creators.propertyBasedCreator();
   }

   public void setIgnorableProperties(String[] ignorable) {
      this._ignorableProperties = ignorable != null && ignorable.length != 0 ? ArrayBuilders.arrayToSet(ignorable) : null;
   }

   public JavaType getContentType() {
      return this._mapType.getContentType();
   }

   public JsonDeserializer<Object> getContentDeserializer() {
      return this._valueDeserializer;
   }

   public void resolve(DeserializationConfig config, DeserializerProvider provider) throws JsonMappingException {
      if (this._propertyBasedCreator != null) {
         Iterator i$ = this._propertyBasedCreator.properties().iterator();

         while(i$.hasNext()) {
            SettableBeanProperty prop = (SettableBeanProperty)i$.next();
            prop.setValueDeserializer(this.findDeserializer(config, provider, prop.getType(), prop));
         }
      }

   }

   public Map<Object, Object> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t != JsonToken.START_OBJECT && t != JsonToken.FIELD_NAME && t != JsonToken.END_OBJECT) {
         throw ctxt.mappingException(this.getMapClass());
      } else if (this._propertyBasedCreator != null) {
         return this._deserializeUsingCreator(jp, ctxt);
      } else if (this._defaultCtor == null) {
         throw ctxt.instantiationException(this.getMapClass(), "No default constructor found");
      } else {
         Map result;
         try {
            result = (Map)this._defaultCtor.newInstance();
         } catch (Exception var6) {
            throw ctxt.instantiationException(this.getMapClass(), (Throwable)var6);
         }

         this._readAndBind(jp, ctxt, result);
         return result;
      }
   }

   public Map<Object, Object> deserialize(JsonParser jp, DeserializationContext ctxt, Map<Object, Object> result) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t != JsonToken.START_OBJECT && t != JsonToken.FIELD_NAME) {
         throw ctxt.mappingException(this.getMapClass());
      } else {
         this._readAndBind(jp, ctxt, result);
         return result;
      }
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
   }

   public final Class<?> getMapClass() {
      return this._mapType.getRawClass();
   }

   public JavaType getValueType() {
      return this._mapType;
   }

   protected final void _readAndBind(JsonParser jp, DeserializationContext ctxt, Map<Object, Object> result) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.START_OBJECT) {
         t = jp.nextToken();
      }

      KeyDeserializer keyDes = this._keyDeserializer;
      JsonDeserializer<Object> valueDes = this._valueDeserializer;

      for(TypeDeserializer typeDeser = this._valueTypeDeserializer; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
         String fieldName = jp.getCurrentName();
         Object key = keyDes == null ? fieldName : keyDes.deserializeKey(fieldName, ctxt);
         t = jp.nextToken();
         if (this._ignorableProperties != null && this._ignorableProperties.contains(fieldName)) {
            jp.skipChildren();
         } else {
            Object value;
            if (t == JsonToken.VALUE_NULL) {
               value = null;
            } else if (typeDeser == null) {
               value = valueDes.deserialize(jp, ctxt);
            } else {
               value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
            }

            result.put(key, value);
         }
      }

   }

   public Map<Object, Object> _deserializeUsingCreator(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      Creator.PropertyBased creator = this._propertyBasedCreator;
      PropertyValueBuffer buffer = creator.startBuilding(jp, ctxt);
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.START_OBJECT) {
         t = jp.nextToken();
      }

      JsonDeserializer<Object> valueDes = this._valueDeserializer;

      for(TypeDeserializer typeDeser = this._valueTypeDeserializer; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
         String propName = jp.getCurrentName();
         t = jp.nextToken();
         if (this._ignorableProperties != null && this._ignorableProperties.contains(propName)) {
            jp.skipChildren();
         } else {
            SettableBeanProperty prop = creator.findCreatorProperty(propName);
            if (prop != null) {
               Object value = prop.deserialize(jp, ctxt);
               if (buffer.assignParameter(prop.getCreatorIndex(), value)) {
                  jp.nextToken();

                  Map result;
                  try {
                     result = (Map)creator.build(buffer);
                  } catch (Exception var13) {
                     this.wrapAndThrow(var13, this._mapType.getRawClass());
                     return null;
                  }

                  this._readAndBind(jp, ctxt, result);
                  return result;
               }
            } else {
               String fieldName = jp.getCurrentName();
               Object key = this._keyDeserializer == null ? fieldName : this._keyDeserializer.deserializeKey(fieldName, ctxt);
               Object value;
               if (t == JsonToken.VALUE_NULL) {
                  value = null;
               } else if (typeDeser == null) {
                  value = valueDes.deserialize(jp, ctxt);
               } else {
                  value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
               }

               buffer.bufferMapProperty(key, value);
            }
         }
      }

      try {
         return (Map)creator.build(buffer);
      } catch (Exception var14) {
         this.wrapAndThrow(var14, this._mapType.getRawClass());
         return null;
      }
   }

   protected void wrapAndThrow(Throwable t, Object ref) throws IOException {
      while(t instanceof InvocationTargetException && t.getCause() != null) {
         t = t.getCause();
      }

      if (t instanceof Error) {
         throw (Error)t;
      } else if (t instanceof IOException && !(t instanceof JsonMappingException)) {
         throw (IOException)t;
      } else {
         throw JsonMappingException.wrapWithPath(t, ref, (String)null);
      }
   }
}
