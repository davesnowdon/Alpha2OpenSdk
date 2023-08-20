package org.codehaus.jackson.map.ser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.type.JavaType;

public class PropertyBuilder {
   protected final SerializationConfig _config;
   protected final BasicBeanDescription _beanDesc;
   protected final JsonSerialize.Inclusion _outputProps;
   protected final AnnotationIntrospector _annotationIntrospector;
   protected Object _defaultBean;

   public PropertyBuilder(SerializationConfig config, BasicBeanDescription beanDesc) {
      this._config = config;
      this._beanDesc = beanDesc;
      this._outputProps = beanDesc.findSerializationInclusion(config.getSerializationInclusion());
      this._annotationIntrospector = this._config.getAnnotationIntrospector();
   }

   public Annotations getClassAnnotations() {
      return this._beanDesc.getClassAnnotations();
   }

   protected BeanPropertyWriter buildWriter(String name, JavaType declaredType, JsonSerializer<Object> ser, TypeSerializer typeSer, TypeSerializer contentTypeSer, AnnotatedMember am, boolean defaultUseStaticTyping) {
      Field f;
      Method m;
      if (am instanceof AnnotatedField) {
         m = null;
         f = ((AnnotatedField)am).getAnnotated();
      } else {
         m = ((AnnotatedMethod)am).getAnnotated();
         f = null;
      }

      JavaType serializationType = this.findSerializationType(am, defaultUseStaticTyping, declaredType);
      if (contentTypeSer != null) {
         if (serializationType == null) {
            serializationType = declaredType;
         }

         JavaType ct = serializationType.getContentType();
         if (ct == null) {
            throw new IllegalStateException("Problem trying to create BeanPropertyWriter for property '" + name + "' (of type " + this._beanDesc.getType() + "); serialization type " + serializationType + " has no content");
         }

         serializationType = serializationType.withContentTypeHandler(contentTypeSer);
         ct = serializationType.getContentType();
      }

      Object suppValue = null;
      boolean suppressNulls = false;
      JsonSerialize.Inclusion methodProps = this._annotationIntrospector.findSerializationInclusion(am, this._outputProps);
      if (methodProps != null) {
         switch(methodProps) {
         case NON_DEFAULT:
            suppValue = this.getDefaultValue(name, m, f);
            if (suppValue == null) {
               suppressNulls = true;
            }
            break;
         case NON_NULL:
            suppressNulls = true;
         }
      }

      return new BeanPropertyWriter(am, this._beanDesc.getClassAnnotations(), name, declaredType, ser, typeSer, serializationType, m, f, suppressNulls, suppValue);
   }

   protected JavaType findSerializationType(Annotated a, boolean useStaticTyping, JavaType declaredType) {
      Class<?> serClass = this._annotationIntrospector.findSerializationType(a);
      if (serClass != null) {
         Class<?> rawDeclared = declaredType.getRawClass();
         if (serClass.isAssignableFrom(rawDeclared)) {
            declaredType = declaredType.widenBy(serClass);
         } else {
            if (!rawDeclared.isAssignableFrom(serClass)) {
               throw new IllegalArgumentException("Illegal concrete-type annotation for method '" + a.getName() + "': class " + serClass.getName() + " not a super-type of (declared) class " + rawDeclared.getName());
            }

            declaredType = declaredType.forcedNarrowBy(serClass);
         }

         useStaticTyping = true;
      }

      JavaType secondary = BeanSerializerFactory.modifySecondaryTypesByAnnotation(this._config, a, declaredType);
      if (secondary != declaredType) {
         useStaticTyping = true;
         declaredType = secondary;
      }

      if (!useStaticTyping) {
         JsonSerialize.Typing typing = this._annotationIntrospector.findSerializationTyping(a);
         if (typing != null) {
            useStaticTyping = typing == JsonSerialize.Typing.STATIC;
         }
      }

      return useStaticTyping ? declaredType : null;
   }

   protected Object getDefaultBean() {
      if (this._defaultBean == null) {
         this._defaultBean = this._beanDesc.instantiateBean(this._config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS));
         if (this._defaultBean == null) {
            Class<?> cls = this._beanDesc.getClassInfo().getAnnotated();
            throw new IllegalArgumentException("Class " + cls.getName() + " has no default constructor; can not instantiate default bean value to support 'properties=JsonSerialize.Inclusion.NON_DEFAULT' annotation");
         }
      }

      return this._defaultBean;
   }

   protected Object getDefaultValue(String name, Method m, Field f) {
      Object defaultBean = this.getDefaultBean();

      try {
         return m != null ? m.invoke(defaultBean) : f.get(defaultBean);
      } catch (Exception var6) {
         return this._throwWrapped(var6, name, defaultBean);
      }
   }

   protected Object _throwWrapped(Exception e, String propName, Object defaultBean) {
      Object t;
      for(t = e; ((Throwable)t).getCause() != null; t = ((Throwable)t).getCause()) {
      }

      if (t instanceof Error) {
         throw (Error)t;
      } else if (t instanceof RuntimeException) {
         throw (RuntimeException)t;
      } else {
         throw new IllegalArgumentException("Failed to get property '" + propName + "' of default " + defaultBean.getClass().getName() + " instance");
      }
   }
}
