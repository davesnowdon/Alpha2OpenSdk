package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ResolvableDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.codehaus.jackson.map.deser.impl.BeanPropertyMap;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.type.ClassKey;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.util.TokenBuffer;

@JsonCachable
public class BeanDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer {
   protected final AnnotatedClass _forClass;
   protected final JavaType _beanType;
   protected final BeanProperty _property;
   protected final Constructor<?> _defaultConstructor;
   protected final Creator.StringBased _stringCreator;
   protected final Creator.NumberBased _numberCreator;
   protected final Creator.Delegating _delegatingCreator;
   protected final Creator.PropertyBased _propertyBasedCreator;
   protected final BeanPropertyMap _beanProperties;
   protected final SettableAnyProperty _anySetter;
   protected final HashSet<String> _ignorableProps;
   protected final boolean _ignoreAllUnknown;
   protected final Map<String, SettableBeanProperty> _backRefs;
   protected HashMap<ClassKey, JsonDeserializer<Object>> _subDeserializers;

   public BeanDeserializer(AnnotatedClass forClass, JavaType type, BeanProperty property, CreatorContainer creators, BeanPropertyMap properties, Map<String, SettableBeanProperty> backRefs, HashSet<String> ignorableProps, boolean ignoreAllUnknown, SettableAnyProperty anySetter) {
      super(type);
      this._forClass = forClass;
      this._beanType = type;
      this._property = property;
      this._beanProperties = properties;
      this._backRefs = backRefs;
      this._ignorableProps = ignorableProps;
      this._ignoreAllUnknown = ignoreAllUnknown;
      this._anySetter = anySetter;
      this._stringCreator = creators.stringCreator();
      this._numberCreator = creators.numberCreator();
      this._delegatingCreator = creators.delegatingCreator();
      this._propertyBasedCreator = creators.propertyBasedCreator();
      if (this._delegatingCreator == null && this._propertyBasedCreator == null) {
         this._defaultConstructor = creators.getDefaultConstructor();
      } else {
         this._defaultConstructor = null;
      }

   }

   protected BeanDeserializer(BeanDeserializer src) {
      super(src._beanType);
      this._forClass = src._forClass;
      this._beanType = src._beanType;
      this._property = src._property;
      this._beanProperties = src._beanProperties;
      this._backRefs = src._backRefs;
      this._ignorableProps = src._ignorableProps;
      this._ignoreAllUnknown = src._ignoreAllUnknown;
      this._anySetter = src._anySetter;
      this._defaultConstructor = src._defaultConstructor;
      this._stringCreator = src._stringCreator;
      this._numberCreator = src._numberCreator;
      this._delegatingCreator = src._delegatingCreator;
      this._propertyBasedCreator = src._propertyBasedCreator;
   }

   public boolean hasProperty(String propertyName) {
      return this._beanProperties.find(propertyName) != null;
   }

   public int getPropertyCount() {
      return this._beanProperties.size();
   }

   public void resolve(DeserializationConfig config, DeserializerProvider provider) throws JsonMappingException {
      Iterator it = this._beanProperties.allProperties();

      while(it.hasNext()) {
         SettableBeanProperty prop = (SettableBeanProperty)it.next();
         if (!prop.hasValueDeserializer()) {
            prop.setValueDeserializer(this.findDeserializer(config, provider, prop.getType(), prop));
         }

         String refName = prop.getManagedReferenceName();
         if (refName != null) {
            JsonDeserializer<?> valueDeser = prop._valueDeserializer;
            SettableBeanProperty backProp = null;
            boolean isContainer = false;
            if (valueDeser instanceof BeanDeserializer) {
               backProp = ((BeanDeserializer)valueDeser).findBackReference(refName);
            } else {
               if (!(valueDeser instanceof ContainerDeserializer)) {
                  if (valueDeser instanceof AbstractDeserializer) {
                     throw new IllegalArgumentException("Can not handle managed/back reference for abstract types (property " + this._beanType.getRawClass().getName() + "." + prop.getName() + ")");
                  }

                  throw new IllegalArgumentException("Can not handle managed/back reference '" + refName + "': type for value deserializer is not BeanDeserializer or ContainerDeserializer, but " + valueDeser.getClass().getName());
               }

               JsonDeserializer<?> contentDeser = ((ContainerDeserializer)valueDeser).getContentDeserializer();
               if (!(contentDeser instanceof BeanDeserializer)) {
                  throw new IllegalArgumentException("Can not handle managed/back reference '" + refName + "': value deserializer is of type ContainerDeserializer, but content type is not handled by a BeanDeserializer " + " (instead it's of type " + contentDeser.getClass().getName() + ")");
               }

               backProp = ((BeanDeserializer)contentDeser).findBackReference(refName);
               isContainer = true;
            }

            if (backProp == null) {
               throw new IllegalArgumentException("Can not handle managed/back reference '" + refName + "': no back reference property found from type " + prop.getType());
            }

            JavaType referredType = this._beanType;
            JavaType backRefType = backProp.getType();
            if (!backRefType.getRawClass().isAssignableFrom(referredType.getRawClass())) {
               throw new IllegalArgumentException("Can not handle managed/back reference '" + refName + "': back reference type (" + backRefType.getRawClass().getName() + ") not compatible with managed type (" + referredType.getRawClass().getName() + ")");
            }

            this._beanProperties.replace(new SettableBeanProperty.ManagedReferenceProperty(refName, prop, backProp, this._forClass.getAnnotations(), isContainer));
         }
      }

      if (this._anySetter != null && !this._anySetter.hasValueDeserializer()) {
         this._anySetter.setValueDeserializer(this.findDeserializer(config, provider, this._anySetter.getType(), this._anySetter.getProperty()));
      }

      if (this._delegatingCreator != null) {
         BeanProperty.Std property = new BeanProperty.Std((String)null, this._delegatingCreator.getValueType(), this._forClass.getAnnotations(), this._delegatingCreator.getCreator());
         JsonDeserializer<Object> deser = this.findDeserializer(config, provider, this._delegatingCreator.getValueType(), property);
         this._delegatingCreator.setDeserializer(deser);
      }

      if (this._propertyBasedCreator != null) {
         Iterator i$ = this._propertyBasedCreator.properties().iterator();

         while(i$.hasNext()) {
            SettableBeanProperty prop = (SettableBeanProperty)i$.next();
            if (!prop.hasValueDeserializer()) {
               prop.setValueDeserializer(this.findDeserializer(config, provider, prop.getType(), prop));
            }
         }
      }

   }

   public final Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.START_OBJECT) {
         jp.nextToken();
         return this.deserializeFromObject(jp, ctxt);
      } else {
         switch(t) {
         case VALUE_STRING:
            return this.deserializeFromString(jp, ctxt);
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return this.deserializeFromNumber(jp, ctxt);
         case VALUE_EMBEDDED_OBJECT:
            return jp.getEmbeddedObject();
         case VALUE_TRUE:
         case VALUE_FALSE:
         case START_ARRAY:
            return this.deserializeUsingCreator(jp, ctxt);
         case FIELD_NAME:
         case END_OBJECT:
            return this.deserializeFromObject(jp, ctxt);
         default:
            throw ctxt.mappingException(this.getBeanClass());
         }
      }
   }

   public Object deserialize(JsonParser jp, DeserializationContext ctxt, Object bean) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.START_OBJECT) {
         t = jp.nextToken();
      }

      for(; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
         String propName = jp.getCurrentName();
         SettableBeanProperty prop = this._beanProperties.find(propName);
         jp.nextToken();
         if (prop != null) {
            try {
               prop.deserializeAndSet(jp, ctxt, bean);
            } catch (Exception var8) {
               this.wrapAndThrow(var8, bean, propName, ctxt);
            }
         } else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
            jp.skipChildren();
         } else if (this._anySetter != null) {
            this._anySetter.deserializeAndSet(jp, ctxt, bean, propName);
         } else {
            this.handleUnknownProperty(jp, ctxt, bean, propName);
         }
      }

      return bean;
   }

   public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
      return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
   }

   public final Class<?> getBeanClass() {
      return this._beanType.getRawClass();
   }

   public JavaType getValueType() {
      return this._beanType;
   }

   public Iterator<SettableBeanProperty> properties() {
      if (this._beanProperties == null) {
         throw new IllegalStateException("Can only call before BeanDeserializer has been resolved");
      } else {
         return this._beanProperties.allProperties();
      }
   }

   public SettableBeanProperty findBackReference(String logicalName) {
      return this._backRefs == null ? null : (SettableBeanProperty)this._backRefs.get(logicalName);
   }

   public Object deserializeFromObject(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (this._defaultConstructor == null) {
         if (this._propertyBasedCreator != null) {
            return this._deserializeUsingPropertyBased(jp, ctxt);
         } else if (this._delegatingCreator != null) {
            return this._delegatingCreator.deserialize(jp, ctxt);
         } else if (this._beanType.isAbstract()) {
            throw JsonMappingException.from(jp, "Can not instantiate abstract type " + this._beanType + " (need to add/enable type information?)");
         } else {
            throw JsonMappingException.from(jp, "No suitable constructor found for type " + this._beanType + ": can not instantiate from JSON object (need to add/enable type information?)");
         }
      } else {
         Object bean;
         for(bean = this.constructDefaultInstance(); jp.getCurrentToken() != JsonToken.END_OBJECT; jp.nextToken()) {
            String propName = jp.getCurrentName();
            jp.nextToken();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
               try {
                  prop.deserializeAndSet(jp, ctxt, bean);
               } catch (Exception var7) {
                  this.wrapAndThrow(var7, bean, propName, ctxt);
               }
            } else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
               jp.skipChildren();
            } else if (this._anySetter != null) {
               try {
                  this._anySetter.deserializeAndSet(jp, ctxt, bean, propName);
               } catch (Exception var8) {
                  this.wrapAndThrow(var8, bean, propName, ctxt);
               }
            } else {
               this.handleUnknownProperty(jp, ctxt, bean, propName);
            }
         }

         return bean;
      }
   }

   public Object deserializeFromString(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (this._stringCreator != null) {
         return this._stringCreator.construct(jp.getText());
      } else if (this._delegatingCreator != null) {
         return this._delegatingCreator.deserialize(jp, ctxt);
      } else if (ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) && jp.getTextLength() == 0) {
         return null;
      } else {
         throw ctxt.instantiationException(this.getBeanClass(), "no suitable creator method found to deserialize from JSON String");
      }
   }

   public Object deserializeFromNumber(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (this._numberCreator != null) {
         switch(jp.getNumberType()) {
         case INT:
            return this._numberCreator.construct(jp.getIntValue());
         case LONG:
            return this._numberCreator.construct(jp.getLongValue());
         }
      }

      if (this._delegatingCreator != null) {
         return this._delegatingCreator.deserialize(jp, ctxt);
      } else {
         throw ctxt.instantiationException(this.getBeanClass(), "no suitable creator method found to deserialize from JSON Number");
      }
   }

   public Object deserializeUsingCreator(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (this._delegatingCreator != null) {
         try {
            return this._delegatingCreator.deserialize(jp, ctxt);
         } catch (Exception var4) {
            this.wrapInstantiationProblem(var4, ctxt);
         }
      }

      throw ctxt.mappingException(this.getBeanClass());
   }

   protected final Object _deserializeUsingPropertyBased(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      Creator.PropertyBased creator = this._propertyBasedCreator;
      PropertyValueBuffer buffer = creator.startBuilding(jp, ctxt);
      TokenBuffer unknown = null;
      JsonToken t = jp.getCurrentToken();

      Object bean;
      while(true) {
         if (t != JsonToken.FIELD_NAME) {
            Object bean;
            try {
               bean = creator.build(buffer);
            } catch (Exception var12) {
               this.wrapInstantiationProblem(var12, ctxt);
               return null;
            }

            if (unknown != null) {
               if (bean.getClass() != this._beanType.getRawClass()) {
                  return this.handlePolymorphic((JsonParser)null, ctxt, bean, unknown);
               }

               return this.handleUnknownProperties(ctxt, bean, unknown);
            }

            return bean;
         }

         String propName = jp.getCurrentName();
         jp.nextToken();
         SettableBeanProperty prop = creator.findCreatorProperty(propName);
         if (prop != null) {
            Object value = prop.deserialize(jp, ctxt);
            if (buffer.assignParameter(prop.getCreatorIndex(), value)) {
               jp.nextToken();

               try {
                  bean = creator.build(buffer);
                  break;
               } catch (Exception var13) {
                  this.wrapAndThrow(var13, this._beanType.getRawClass(), propName, ctxt);
               }
            }
         } else {
            prop = this._beanProperties.find(propName);
            if (prop != null) {
               buffer.bufferProperty(prop, prop.deserialize(jp, ctxt));
            } else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
               jp.skipChildren();
            } else if (this._anySetter != null) {
               buffer.bufferAnyProperty(this._anySetter, propName, this._anySetter.deserialize(jp, ctxt));
            } else {
               if (unknown == null) {
                  unknown = new TokenBuffer(jp.getCodec());
               }

               unknown.writeFieldName(propName);
               unknown.copyCurrentStructure(jp);
            }
         }

         t = jp.nextToken();
      }

      if (bean.getClass() != this._beanType.getRawClass()) {
         return this.handlePolymorphic(jp, ctxt, bean, unknown);
      } else {
         if (unknown != null) {
            bean = this.handleUnknownProperties(ctxt, bean, unknown);
         }

         return this.deserialize(jp, ctxt, bean);
      }
   }

   protected void handleUnknownProperty(JsonParser jp, DeserializationContext ctxt, Object beanOrClass, String propName) throws IOException, JsonProcessingException {
      if (!this._ignoreAllUnknown && (this._ignorableProps == null || !this._ignorableProps.contains(propName))) {
         super.handleUnknownProperty(jp, ctxt, beanOrClass, propName);
      } else {
         jp.skipChildren();
      }
   }

   protected Object handleUnknownProperties(DeserializationContext ctxt, Object bean, TokenBuffer unknownTokens) throws IOException, JsonProcessingException {
      unknownTokens.writeEndObject();
      JsonParser bufferParser = unknownTokens.asParser();

      while(bufferParser.nextToken() != JsonToken.END_OBJECT) {
         String propName = bufferParser.getCurrentName();
         bufferParser.nextToken();
         this.handleUnknownProperty(bufferParser, ctxt, bean, propName);
      }

      return bean;
   }

   protected Object handlePolymorphic(JsonParser jp, DeserializationContext ctxt, Object bean, TokenBuffer unknownTokens) throws IOException, JsonProcessingException {
      JsonDeserializer<Object> subDeser = this._findSubclassDeserializer(ctxt, bean, unknownTokens);
      if (subDeser != null) {
         if (unknownTokens != null) {
            unknownTokens.writeEndObject();
            JsonParser p2 = unknownTokens.asParser();
            p2.nextToken();
            bean = subDeser.deserialize(p2, ctxt, bean);
         }

         if (jp != null) {
            bean = subDeser.deserialize(jp, ctxt, bean);
         }

         return bean;
      } else {
         if (unknownTokens != null) {
            bean = this.handleUnknownProperties(ctxt, bean, unknownTokens);
         }

         if (jp != null) {
            bean = this.deserialize(jp, ctxt, bean);
         }

         return bean;
      }
   }

   protected JsonDeserializer<Object> _findSubclassDeserializer(DeserializationContext ctxt, Object bean, TokenBuffer unknownTokens) throws IOException, JsonProcessingException {
      JsonDeserializer subDeser;
      synchronized(this) {
         subDeser = this._subDeserializers == null ? null : (JsonDeserializer)this._subDeserializers.get(new ClassKey(bean.getClass()));
      }

      if (subDeser != null) {
         return subDeser;
      } else {
         DeserializerProvider deserProv = ctxt.getDeserializerProvider();
         if (deserProv != null) {
            JavaType type = ctxt.constructType(bean.getClass());
            subDeser = deserProv.findValueDeserializer(ctxt.getConfig(), type, this._property);
            if (subDeser != null) {
               synchronized(this) {
                  if (this._subDeserializers == null) {
                     this._subDeserializers = new HashMap();
                  }

                  this._subDeserializers.put(new ClassKey(bean.getClass()), subDeser);
               }
            }
         }

         return subDeser;
      }
   }

   protected Object constructDefaultInstance() {
      try {
         return this._defaultConstructor.newInstance();
      } catch (Exception var2) {
         ClassUtil.unwrapAndThrowAsIAE(var2);
         return null;
      }
   }

   public void wrapAndThrow(Throwable t, Object bean, String fieldName, DeserializationContext ctxt) throws IOException {
      while(t instanceof InvocationTargetException && t.getCause() != null) {
         t = t.getCause();
      }

      if (t instanceof Error) {
         throw (Error)t;
      } else {
         boolean wrap = ctxt == null || ctxt.isEnabled(DeserializationConfig.Feature.WRAP_EXCEPTIONS);
         if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonMappingException)) {
               throw (IOException)t;
            }
         } else if (!wrap && t instanceof RuntimeException) {
            throw (RuntimeException)t;
         }

         throw JsonMappingException.wrapWithPath(t, bean, fieldName);
      }
   }

   public void wrapAndThrow(Throwable t, Object bean, int index, DeserializationContext ctxt) throws IOException {
      while(t instanceof InvocationTargetException && t.getCause() != null) {
         t = t.getCause();
      }

      if (t instanceof Error) {
         throw (Error)t;
      } else {
         boolean wrap = ctxt == null || ctxt.isEnabled(DeserializationConfig.Feature.WRAP_EXCEPTIONS);
         if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonMappingException)) {
               throw (IOException)t;
            }
         } else if (!wrap && t instanceof RuntimeException) {
            throw (RuntimeException)t;
         }

         throw JsonMappingException.wrapWithPath(t, bean, index);
      }
   }

   protected void wrapInstantiationProblem(Throwable t, DeserializationContext ctxt) throws IOException {
      while(t instanceof InvocationTargetException && t.getCause() != null) {
         t = t.getCause();
      }

      if (t instanceof Error) {
         throw (Error)t;
      } else {
         boolean wrap = ctxt == null || ctxt.isEnabled(DeserializationConfig.Feature.WRAP_EXCEPTIONS);
         if (t instanceof IOException) {
            throw (IOException)t;
         } else if (!wrap && t instanceof RuntimeException) {
            throw (RuntimeException)t;
         } else {
            throw ctxt.instantiationException(this._beanType.getRawClass(), t);
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public void wrapAndThrow(Throwable t, Object bean, String fieldName) throws IOException {
      this.wrapAndThrow(t, bean, fieldName, (DeserializationContext)null);
   }

   /** @deprecated */
   @Deprecated
   public void wrapAndThrow(Throwable t, Object bean, int index) throws IOException {
      this.wrapAndThrow(t, bean, index, (DeserializationContext)null);
   }
}
