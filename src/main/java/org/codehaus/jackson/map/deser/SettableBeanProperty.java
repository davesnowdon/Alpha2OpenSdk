package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.util.InternCache;

public abstract class SettableBeanProperty implements BeanProperty {
   protected final String _propName;
   protected final JavaType _type;
   protected final Annotations _contextAnnotations;
   protected JsonDeserializer<Object> _valueDeserializer;
   protected TypeDeserializer _valueTypeDeserializer;
   protected SettableBeanProperty.NullProvider _nullProvider;
   protected String _managedReferenceName;
   protected int _propertyIndex = -1;

   protected SettableBeanProperty(String propName, JavaType type, TypeDeserializer typeDeser, Annotations contextAnnotations) {
      if (propName != null && propName.length() != 0) {
         this._propName = InternCache.instance.intern(propName);
      } else {
         this._propName = "";
      }

      this._type = type;
      this._contextAnnotations = contextAnnotations;
      this._valueTypeDeserializer = typeDeser;
   }

   protected SettableBeanProperty(SettableBeanProperty src) {
      this._propName = src._propName;
      this._type = src._type;
      this._contextAnnotations = src._contextAnnotations;
      this._valueDeserializer = src._valueDeserializer;
      this._valueTypeDeserializer = src._valueTypeDeserializer;
      this._nullProvider = src._nullProvider;
      this._managedReferenceName = src._managedReferenceName;
      this._propertyIndex = src._propertyIndex;
   }

   public void setValueDeserializer(JsonDeserializer<Object> deser) {
      if (this._valueDeserializer != null) {
         throw new IllegalStateException("Already had assigned deserializer for property '" + this.getName() + "' (class " + this.getDeclaringClass().getName() + ")");
      } else {
         this._valueDeserializer = deser;
         Object nvl = this._valueDeserializer.getNullValue();
         this._nullProvider = nvl == null ? null : new SettableBeanProperty.NullProvider(this._type, nvl);
      }
   }

   public void setManagedReferenceName(String n) {
      this._managedReferenceName = n;
   }

   public void assignIndex(int index) {
      if (this._propertyIndex != -1) {
         throw new IllegalStateException("Property '" + this.getName() + "' already had index (" + this._propertyIndex + "), trying to assign " + index);
      } else {
         this._propertyIndex = index;
      }
   }

   public final String getName() {
      return this._propName;
   }

   public JavaType getType() {
      return this._type;
   }

   public abstract <A extends Annotation> A getAnnotation(Class<A> var1);

   public abstract AnnotatedMember getMember();

   public <A extends Annotation> A getContextAnnotation(Class<A> acls) {
      return this._contextAnnotations.get(acls);
   }

   protected final Class<?> getDeclaringClass() {
      return this.getMember().getDeclaringClass();
   }

   /** @deprecated */
   @Deprecated
   public String getPropertyName() {
      return this._propName;
   }

   public String getManagedReferenceName() {
      return this._managedReferenceName;
   }

   public boolean hasValueDeserializer() {
      return this._valueDeserializer != null;
   }

   public JsonDeserializer<Object> getValueDeserializer() {
      return this._valueDeserializer;
   }

   public int getCreatorIndex() {
      return -1;
   }

   public int getProperytIndex() {
      return this._propertyIndex;
   }

   public abstract void deserializeAndSet(JsonParser var1, DeserializationContext var2, Object var3) throws IOException, JsonProcessingException;

   public abstract void set(Object var1, Object var2) throws IOException;

   public final Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonToken t = jp.getCurrentToken();
      if (t == JsonToken.VALUE_NULL) {
         return this._nullProvider == null ? null : this._nullProvider.nullValue(ctxt);
      } else {
         return this._valueTypeDeserializer != null ? this._valueDeserializer.deserializeWithType(jp, ctxt, this._valueTypeDeserializer) : this._valueDeserializer.deserialize(jp, ctxt);
      }
   }

   protected void _throwAsIOE(Exception e, Object value) throws IOException {
      if (e instanceof IllegalArgumentException) {
         String actType = value == null ? "[NULL]" : value.getClass().getName();
         StringBuilder msg = (new StringBuilder("Problem deserializing property '")).append(this.getPropertyName());
         msg.append("' (expected type: ").append(this.getType());
         msg.append("; actual type: ").append(actType).append(")");
         String origMsg = e.getMessage();
         if (origMsg != null) {
            msg.append(", problem: ").append(origMsg);
         } else {
            msg.append(" (no error message provided)");
         }

         throw new JsonMappingException(msg.toString(), (JsonLocation)null, e);
      } else {
         this._throwAsIOE(e);
      }
   }

   protected IOException _throwAsIOE(Exception e) throws IOException {
      if (e instanceof IOException) {
         throw (IOException)e;
      } else if (e instanceof RuntimeException) {
         throw (RuntimeException)e;
      } else {
         Object th;
         for(th = e; ((Throwable)th).getCause() != null; th = ((Throwable)th).getCause()) {
         }

         throw new JsonMappingException(((Throwable)th).getMessage(), (JsonLocation)null, (Throwable)th);
      }
   }

   public String toString() {
      return "[property '" + this.getName() + "']";
   }

   protected static final class NullProvider {
      private final Object _nullValue;
      private final boolean _isPrimitive;
      private final Class<?> _rawType;

      protected NullProvider(JavaType type, Object nullValue) {
         this._nullValue = nullValue;
         this._isPrimitive = type.isPrimitive();
         this._rawType = type.getRawClass();
      }

      public Object nullValue(DeserializationContext ctxt) throws JsonProcessingException {
         if (this._isPrimitive && ctxt.isEnabled(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
            throw ctxt.mappingException("Can not map JSON null into type " + this._rawType.getName() + " (set DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES to 'false' to allow)");
         } else {
            return this._nullValue;
         }
      }
   }

   public static final class ManagedReferenceProperty extends SettableBeanProperty {
      protected final String _referenceName;
      protected final boolean _isContainer;
      protected final SettableBeanProperty _managedProperty;
      protected final SettableBeanProperty _backProperty;

      public ManagedReferenceProperty(String refName, SettableBeanProperty forward, SettableBeanProperty backward, Annotations contextAnnotations, boolean isContainer) {
         super(forward.getName(), forward.getType(), forward._valueTypeDeserializer, contextAnnotations);
         this._referenceName = refName;
         this._managedProperty = forward;
         this._backProperty = backward;
         this._isContainer = isContainer;
      }

      public <A extends Annotation> A getAnnotation(Class<A> acls) {
         return this._managedProperty.getAnnotation(acls);
      }

      public AnnotatedMember getMember() {
         return this._managedProperty.getMember();
      }

      public void deserializeAndSet(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException, JsonProcessingException {
         this.set(instance, this._managedProperty.deserialize(jp, ctxt));
      }

      public final void set(Object instance, Object value) throws IOException {
         this._managedProperty.set(instance, value);
         if (value != null) {
            if (this._isContainer) {
               if (value instanceof Object[]) {
                  Object[] arr$ = (Object[])((Object[])value);
                  int len$ = arr$.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     Object ob = arr$[i$];
                     if (ob != null) {
                        this._backProperty.set(ob, instance);
                     }
                  }
               } else {
                  Iterator i$;
                  Object ob;
                  if (value instanceof Collection) {
                     i$ = ((Collection)value).iterator();

                     while(i$.hasNext()) {
                        ob = i$.next();
                        if (ob != null) {
                           this._backProperty.set(ob, instance);
                        }
                     }
                  } else {
                     if (!(value instanceof Map)) {
                        throw new IllegalStateException("Unsupported container type (" + value.getClass().getName() + ") when resolving reference '" + this._referenceName + "'");
                     }

                     i$ = ((Map)value).values().iterator();

                     while(i$.hasNext()) {
                        ob = i$.next();
                        if (ob != null) {
                           this._backProperty.set(ob, instance);
                        }
                     }
                  }
               }
            } else {
               this._backProperty.set(value, instance);
            }
         }

      }
   }

   public static final class CreatorProperty extends SettableBeanProperty {
      protected final AnnotatedParameter _annotated;
      protected final int _index;

      public CreatorProperty(String name, JavaType type, TypeDeserializer typeDeser, Annotations contextAnnotations, AnnotatedParameter param, int index) {
         super(name, type, typeDeser, contextAnnotations);
         this._annotated = param;
         this._index = index;
      }

      public <A extends Annotation> A getAnnotation(Class<A> acls) {
         return this._annotated.getAnnotation(acls);
      }

      public AnnotatedMember getMember() {
         return this._annotated;
      }

      public int getCreatorIndex() {
         return this._index;
      }

      public void deserializeAndSet(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException, JsonProcessingException {
         this.set(instance, this.deserialize(jp, ctxt));
      }

      public void set(Object instance, Object value) throws IOException {
      }
   }

   public static final class FieldProperty extends SettableBeanProperty {
      protected final AnnotatedField _annotated;
      protected final Field _field;

      public FieldProperty(String name, JavaType type, TypeDeserializer typeDeser, Annotations contextAnnotations, AnnotatedField field) {
         super(name, type, typeDeser, contextAnnotations);
         this._annotated = field;
         this._field = field.getAnnotated();
      }

      public <A extends Annotation> A getAnnotation(Class<A> acls) {
         return this._annotated.getAnnotation(acls);
      }

      public AnnotatedMember getMember() {
         return this._annotated;
      }

      public void deserializeAndSet(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException, JsonProcessingException {
         this.set(instance, this.deserialize(jp, ctxt));
      }

      public final void set(Object instance, Object value) throws IOException {
         try {
            this._field.set(instance, value);
         } catch (Exception var4) {
            this._throwAsIOE(var4, value);
         }

      }
   }

   public static final class SetterlessProperty extends SettableBeanProperty {
      protected final AnnotatedMethod _annotated;
      protected final Method _getter;

      public SetterlessProperty(String name, JavaType type, TypeDeserializer typeDeser, Annotations contextAnnotations, AnnotatedMethod method) {
         super(name, type, typeDeser, contextAnnotations);
         this._annotated = method;
         this._getter = method.getAnnotated();
      }

      public <A extends Annotation> A getAnnotation(Class<A> acls) {
         return this._annotated.getAnnotation(acls);
      }

      public AnnotatedMember getMember() {
         return this._annotated;
      }

      public final void deserializeAndSet(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException, JsonProcessingException {
         JsonToken t = jp.getCurrentToken();
         if (t != JsonToken.VALUE_NULL) {
            Object toModify;
            try {
               toModify = this._getter.invoke(instance);
            } catch (Exception var7) {
               this._throwAsIOE(var7);
               return;
            }

            if (toModify == null) {
               throw new JsonMappingException("Problem deserializing 'setterless' property '" + this.getName() + "': get method returned null");
            } else {
               this._valueDeserializer.deserialize(jp, ctxt, toModify);
            }
         }
      }

      public final void set(Object instance, Object value) throws IOException {
         throw new UnsupportedOperationException("Should never call 'set' on setterless property");
      }
   }

   public static final class MethodProperty extends SettableBeanProperty {
      protected final AnnotatedMethod _annotated;
      protected final Method _setter;

      public MethodProperty(String name, JavaType type, TypeDeserializer typeDeser, Annotations contextAnnotations, AnnotatedMethod method) {
         super(name, type, typeDeser, contextAnnotations);
         this._annotated = method;
         this._setter = method.getAnnotated();
      }

      public <A extends Annotation> A getAnnotation(Class<A> acls) {
         return this._annotated.getAnnotation(acls);
      }

      public AnnotatedMember getMember() {
         return this._annotated;
      }

      public void deserializeAndSet(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException, JsonProcessingException {
         this.set(instance, this.deserialize(jp, ctxt));
      }

      public final void set(Object instance, Object value) throws IOException {
         try {
            this._setter.invoke(instance, value);
         } catch (Exception var4) {
            this._throwAsIOE(var4, value);
         }

      }
   }
}
