package org.codehaus.jackson.map.introspect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ClassIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

public class BasicClassIntrospector extends ClassIntrospector<BasicBeanDescription> {
   public static final BasicClassIntrospector.GetterMethodFilter DEFAULT_GETTER_FILTER = new BasicClassIntrospector.GetterMethodFilter();
   public static final BasicClassIntrospector.SetterMethodFilter DEFAULT_SETTER_FILTER = new BasicClassIntrospector.SetterMethodFilter();
   public static final BasicClassIntrospector.SetterAndGetterMethodFilter DEFAULT_SETTER_AND_GETTER_FILTER = new BasicClassIntrospector.SetterAndGetterMethodFilter();
   public static final BasicClassIntrospector instance = new BasicClassIntrospector();

   public BasicClassIntrospector() {
   }

   public BasicBeanDescription forSerialization(SerializationConfig cfg, JavaType type, ClassIntrospector.MixInResolver r) {
      AnnotationIntrospector ai = cfg.getAnnotationIntrospector();
      AnnotatedClass ac = AnnotatedClass.construct(type.getRawClass(), ai, r);
      ac.resolveMemberMethods(this.getSerializationMethodFilter(cfg), false);
      ac.resolveCreators(true);
      ac.resolveFields(false);
      return new BasicBeanDescription(cfg, type, ac);
   }

   public BasicBeanDescription forDeserialization(DeserializationConfig cfg, JavaType type, ClassIntrospector.MixInResolver r) {
      boolean useAnnotations = cfg.isAnnotationProcessingEnabled();
      AnnotationIntrospector ai = cfg.getAnnotationIntrospector();
      AnnotatedClass ac = AnnotatedClass.construct(type.getRawClass(), useAnnotations ? ai : null, r);
      ac.resolveMemberMethods(this.getDeserializationMethodFilter(cfg), true);
      ac.resolveCreators(true);
      ac.resolveFields(true);
      return new BasicBeanDescription(cfg, type, ac);
   }

   public BasicBeanDescription forCreation(DeserializationConfig cfg, JavaType type, ClassIntrospector.MixInResolver r) {
      boolean useAnnotations = cfg.isAnnotationProcessingEnabled();
      AnnotationIntrospector ai = cfg.getAnnotationIntrospector();
      AnnotatedClass ac = AnnotatedClass.construct(type.getRawClass(), useAnnotations ? ai : null, r);
      ac.resolveCreators(true);
      return new BasicBeanDescription(cfg, type, ac);
   }

   public BasicBeanDescription forClassAnnotations(MapperConfig<?> cfg, Class<?> c, ClassIntrospector.MixInResolver r) {
      boolean useAnnotations = cfg.isAnnotationProcessingEnabled();
      AnnotationIntrospector ai = cfg.getAnnotationIntrospector();
      AnnotatedClass ac = AnnotatedClass.construct(c, useAnnotations ? ai : null, r);
      return new BasicBeanDescription(cfg, cfg.constructType(c), ac);
   }

   public BasicBeanDescription forDirectClassAnnotations(MapperConfig<?> cfg, Class<?> c, ClassIntrospector.MixInResolver r) {
      boolean useAnnotations = cfg.isAnnotationProcessingEnabled();
      AnnotationIntrospector ai = cfg.getAnnotationIntrospector();
      AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(c, useAnnotations ? ai : null, r);
      return new BasicBeanDescription(cfg, cfg.constructType(c), ac);
   }

   protected MethodFilter getSerializationMethodFilter(SerializationConfig cfg) {
      return DEFAULT_GETTER_FILTER;
   }

   protected MethodFilter getDeserializationMethodFilter(DeserializationConfig cfg) {
      return (MethodFilter)(cfg.isEnabled(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS) ? DEFAULT_SETTER_AND_GETTER_FILTER : DEFAULT_SETTER_FILTER);
   }

   public static final class SetterAndGetterMethodFilter extends BasicClassIntrospector.SetterMethodFilter {
      public SetterAndGetterMethodFilter() {
      }

      public boolean includeMethod(Method m) {
         if (super.includeMethod(m)) {
            return true;
         } else if (!ClassUtil.hasGetterSignature(m)) {
            return false;
         } else {
            Class<?> rt = m.getReturnType();
            return Collection.class.isAssignableFrom(rt) || Map.class.isAssignableFrom(rt);
         }
      }
   }

   public static class SetterMethodFilter implements MethodFilter {
      public SetterMethodFilter() {
      }

      public boolean includeMethod(Method m) {
         if (Modifier.isStatic(m.getModifiers())) {
            return false;
         } else {
            int pcount = m.getParameterTypes().length;
            switch(pcount) {
            case 1:
               return true;
            case 2:
               return true;
            default:
               return false;
            }
         }
      }
   }

   public static class GetterMethodFilter implements MethodFilter {
      private GetterMethodFilter() {
      }

      public boolean includeMethod(Method m) {
         return ClassUtil.hasGetterSignature(m);
      }
   }
}
