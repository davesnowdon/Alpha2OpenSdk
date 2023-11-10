package org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ResolvableSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.type.JavaType;

@JacksonStdImpl
public final class JsonValueSerializer extends SerializerBase<Object> implements ResolvableSerializer, SchemaAware {
   protected final Method _accessorMethod;
   protected JsonSerializer<Object> _valueSerializer;
   protected final BeanProperty _property;
   protected boolean _forceTypeInformation;

   public JsonValueSerializer(Method valueMethod, JsonSerializer<Object> ser, BeanProperty property) {
      super(Object.class);
      this._accessorMethod = valueMethod;
      this._valueSerializer = ser;
      this._property = property;
   }

   public void serialize(Object bean, JsonGenerator jgen, SerializerProvider prov) throws IOException, JsonGenerationException {
      try {
         Object value = this._accessorMethod.invoke(bean);
         if (value == null) {
            prov.defaultSerializeNull(jgen);
         } else {
            JsonSerializer<Object> ser = this._valueSerializer;
            if (ser == null) {
               Class<?> c = value.getClass();
               ser = prov.findTypedValueSerializer(c, true, this._property);
            }

            ser.serialize(value, jgen, prov);
         }
      } catch (IOException var7) {
         throw var7;
      } catch (Exception var8) {
         Object t;
         for(t = var8; t instanceof InvocationTargetException && ((Throwable)t).getCause() != null; t = ((Throwable)t).getCause()) {
         }

         if (t instanceof Error) {
            throw (Error)t;
         } else {
            throw JsonMappingException.wrapWithPath((Throwable)t, bean, this._accessorMethod.getName() + "()");
         }
      }
   }

   public void serializeWithType(Object bean, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonProcessingException {
      Object value = null;

      try {
         value = this._accessorMethod.invoke(bean);
         if (value == null) {
            provider.defaultSerializeNull(jgen);
         } else {
            JsonSerializer<Object> ser = this._valueSerializer;
            if (ser != null) {
               if (this._forceTypeInformation) {
                  typeSer.writeTypePrefixForScalar(bean, jgen);
               }

               ser.serializeWithType(value, jgen, provider, typeSer);
               if (this._forceTypeInformation) {
                  typeSer.writeTypeSuffixForScalar(bean, jgen);
               }

            } else {
               Class<?> c = value.getClass();
               ser = provider.findTypedValueSerializer(c, true, this._property);
               ser.serialize(value, jgen, provider);
            }
         }
      } catch (IOException var8) {
         throw var8;
      } catch (Exception var9) {
         Object t;
         for(t = var9; t instanceof InvocationTargetException && ((Throwable)t).getCause() != null; t = ((Throwable)t).getCause()) {
         }

         if (t instanceof Error) {
            throw (Error)t;
         } else {
            throw JsonMappingException.wrapWithPath((Throwable)t, bean, this._accessorMethod.getName() + "()");
         }
      }
   }

   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      return this._valueSerializer instanceof SchemaAware ? ((SchemaAware)this._valueSerializer).getSchema(provider, (Type)null) : JsonSchema.getDefaultSchemaNode();
   }

   public void resolve(SerializerProvider provider) throws JsonMappingException {
      if (this._valueSerializer == null && (provider.isEnabled(SerializationConfig.Feature.USE_STATIC_TYPING) || Modifier.isFinal(this._accessorMethod.getReturnType().getModifiers()))) {
         JavaType t = provider.constructType(this._accessorMethod.getGenericReturnType());
         this._valueSerializer = provider.findTypedValueSerializer(t, false, this._property);
         this._forceTypeInformation = this.isNaturalTypeWithStdHandling(t, this._valueSerializer);
      }

   }

   protected boolean isNaturalTypeWithStdHandling(JavaType type, JsonSerializer<?> ser) {
      Class<?> cls = type.getRawClass();
      if (type.isPrimitive()) {
         if (cls != Integer.TYPE && cls != Boolean.TYPE && cls != Double.TYPE) {
            return false;
         }
      } else if (cls != String.class && cls != Integer.class && cls != Boolean.class && cls != Double.class) {
         return false;
      }

      return ser.getClass().getAnnotation(JacksonStdImpl.class) != null;
   }

   public String toString() {
      return "(@JsonValue serializer for method " + this._accessorMethod.getDeclaringClass() + "#" + this._accessorMethod.getName() + ")";
   }
}
