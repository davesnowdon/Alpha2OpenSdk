package org.codehaus.jackson.map;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.jsontype.SubtypeResolver;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.jsontype.impl.StdSubtypeResolver;
import org.codehaus.jackson.map.type.ClassKey;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.util.StdDateFormat;
import org.codehaus.jackson.type.JavaType;

public abstract class MapperConfig<T extends MapperConfig<T>> implements ClassIntrospector.MixInResolver {
   protected static final DateFormat DEFAULT_DATE_FORMAT;
   protected MapperConfig.Base _base;
   protected HashMap<ClassKey, Class<?>> _mixInAnnotations;
   protected boolean _mixInAnnotationsShared;
   protected SubtypeResolver _subtypeResolver;

   protected MapperConfig(ClassIntrospector<? extends BeanDescription> ci, AnnotationIntrospector ai, VisibilityChecker<?> vc, SubtypeResolver str, PropertyNamingStrategy pns, TypeFactory tf, HandlerInstantiator hi) {
      this._base = new MapperConfig.Base(ci, ai, vc, pns, tf, (TypeResolverBuilder)null, DEFAULT_DATE_FORMAT, hi);
      this._subtypeResolver = str;
      this._mixInAnnotationsShared = true;
   }

   protected MapperConfig(MapperConfig<?> src) {
      this(src, src._base, src._subtypeResolver);
   }

   protected MapperConfig(MapperConfig<?> src, MapperConfig.Base base, SubtypeResolver str) {
      this._base = base;
      this._subtypeResolver = str;
      this._mixInAnnotationsShared = true;
      this._mixInAnnotations = src._mixInAnnotations;
   }

   public abstract void fromAnnotations(Class<?> var1);

   public abstract T createUnshared(SubtypeResolver var1);

   public abstract T withClassIntrospector(ClassIntrospector<? extends BeanDescription> var1);

   public abstract T withAnnotationIntrospector(AnnotationIntrospector var1);

   public abstract T withVisibilityChecker(VisibilityChecker<?> var1);

   public abstract T withTypeResolverBuilder(TypeResolverBuilder<?> var1);

   public abstract T withSubtypeResolver(SubtypeResolver var1);

   public abstract T withPropertyNamingStrategy(PropertyNamingStrategy var1);

   public abstract T withTypeFactory(TypeFactory var1);

   public abstract T withDateFormat(DateFormat var1);

   public abstract T withHandlerInstantiator(HandlerInstantiator var1);

   public ClassIntrospector<? extends BeanDescription> getClassIntrospector() {
      return this._base.getClassIntrospector();
   }

   public AnnotationIntrospector getAnnotationIntrospector() {
      return this._base.getAnnotationIntrospector();
   }

   public final void insertAnnotationIntrospector(AnnotationIntrospector introspector) {
      this._base = this._base.withAnnotationIntrospector(AnnotationIntrospector.Pair.create(introspector, this.getAnnotationIntrospector()));
   }

   public final void appendAnnotationIntrospector(AnnotationIntrospector introspector) {
      this._base = this._base.withAnnotationIntrospector(AnnotationIntrospector.Pair.create(this.getAnnotationIntrospector(), introspector));
   }

   public final VisibilityChecker<?> getDefaultVisibilityChecker() {
      return this._base.getVisibilityChecker();
   }

   public final PropertyNamingStrategy getPropertyNamingStrategy() {
      return this._base.getPropertyNamingStrategy();
   }

   public final HandlerInstantiator getHandlerInstantiator() {
      return this._base.getHandlerInstantiator();
   }

   public final void setMixInAnnotations(Map<Class<?>, Class<?>> sourceMixins) {
      HashMap<ClassKey, Class<?>> mixins = null;
      if (sourceMixins != null && sourceMixins.size() > 0) {
         mixins = new HashMap(sourceMixins.size());
         Iterator i$ = sourceMixins.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<Class<?>, Class<?>> en = (Entry)i$.next();
            mixins.put(new ClassKey((Class)en.getKey()), en.getValue());
         }
      }

      this._mixInAnnotationsShared = false;
      this._mixInAnnotations = mixins;
   }

   public final void addMixInAnnotations(Class<?> target, Class<?> mixinSource) {
      if (this._mixInAnnotations == null) {
         this._mixInAnnotationsShared = false;
         this._mixInAnnotations = new HashMap();
      } else if (this._mixInAnnotationsShared) {
         this._mixInAnnotationsShared = false;
         this._mixInAnnotations = new HashMap(this._mixInAnnotations);
      }

      this._mixInAnnotations.put(new ClassKey(target), mixinSource);
   }

   public final Class<?> findMixInClassFor(Class<?> cls) {
      return this._mixInAnnotations == null ? null : (Class)this._mixInAnnotations.get(new ClassKey(cls));
   }

   public final int mixInCount() {
      return this._mixInAnnotations == null ? 0 : this._mixInAnnotations.size();
   }

   public final TypeResolverBuilder<?> getDefaultTyper(JavaType baseType) {
      return this._base.getTypeResolverBuilder();
   }

   public final SubtypeResolver getSubtypeResolver() {
      if (this._subtypeResolver == null) {
         this._subtypeResolver = new StdSubtypeResolver();
      }

      return this._subtypeResolver;
   }

   public final TypeFactory getTypeFactory() {
      return this._base.getTypeFactory();
   }

   public final JavaType constructType(Class<?> cls) {
      return this.getTypeFactory().constructType((Type)cls);
   }

   public final DateFormat getDateFormat() {
      return this._base.getDateFormat();
   }

   public abstract <DESC extends BeanDescription> DESC introspectClassAnnotations(Class<?> var1);

   public abstract <DESC extends BeanDescription> DESC introspectDirectClassAnnotations(Class<?> var1);

   public abstract boolean isAnnotationProcessingEnabled();

   public abstract boolean canOverrideAccessModifiers();

   public TypeResolverBuilder<?> typeResolverBuilderInstance(Annotated annotated, Class<? extends TypeResolverBuilder<?>> builderClass) {
      HandlerInstantiator hi = this.getHandlerInstantiator();
      if (hi != null) {
         TypeResolverBuilder<?> builder = hi.typeResolverBuilderInstance(this, annotated, builderClass);
         if (builder != null) {
            return builder;
         }
      }

      return (TypeResolverBuilder)ClassUtil.createInstance(builderClass, this.canOverrideAccessModifiers());
   }

   public TypeIdResolver typeIdResolverInstance(Annotated annotated, Class<? extends TypeIdResolver> resolverClass) {
      HandlerInstantiator hi = this.getHandlerInstantiator();
      if (hi != null) {
         TypeIdResolver builder = hi.typeIdResolverInstance(this, annotated, resolverClass);
         if (builder != null) {
            return builder;
         }
      }

      return (TypeIdResolver)ClassUtil.createInstance(resolverClass, this.canOverrideAccessModifiers());
   }

   /** @deprecated */
   @Deprecated
   public abstract T createUnshared(TypeResolverBuilder<?> var1, VisibilityChecker<?> var2, SubtypeResolver var3);

   /** @deprecated */
   @Deprecated
   public final void setIntrospector(ClassIntrospector<? extends BeanDescription> ci) {
      this._base = this._base.withClassIntrospector(ci);
   }

   /** @deprecated */
   @Deprecated
   public final void setAnnotationIntrospector(AnnotationIntrospector ai) {
      this._base = this._base.withAnnotationIntrospector(ai);
   }

   /** @deprecated */
   @Deprecated
   public void setDateFormat(DateFormat df) {
      if (df == null) {
         df = StdDateFormat.instance;
      }

      this._base = this._base.withDateFormat((DateFormat)df);
   }

   /** @deprecated */
   @Deprecated
   public final void setSubtypeResolver(SubtypeResolver str) {
      this._subtypeResolver = str;
   }

   static {
      DEFAULT_DATE_FORMAT = StdDateFormat.instance;
   }

   public static class Base {
      protected final ClassIntrospector<? extends BeanDescription> _classIntrospector;
      protected final AnnotationIntrospector _annotationIntrospector;
      protected final VisibilityChecker<?> _visibilityChecker;
      protected final PropertyNamingStrategy _propertyNamingStrategy;
      protected final TypeFactory _typeFactory;
      protected final TypeResolverBuilder<?> _typeResolverBuilder;
      protected final DateFormat _dateFormat;
      protected final HandlerInstantiator _handlerInstantiator;

      public Base(ClassIntrospector<? extends BeanDescription> ci, AnnotationIntrospector ai, VisibilityChecker<?> vc, PropertyNamingStrategy pns, TypeFactory tf, TypeResolverBuilder<?> typer, DateFormat dateFormat, HandlerInstantiator hi) {
         this._classIntrospector = ci;
         this._annotationIntrospector = ai;
         this._visibilityChecker = vc;
         this._propertyNamingStrategy = pns;
         this._typeFactory = tf;
         this._typeResolverBuilder = typer;
         this._dateFormat = dateFormat;
         this._handlerInstantiator = hi;
      }

      public MapperConfig.Base withClassIntrospector(ClassIntrospector<? extends BeanDescription> ci) {
         return new MapperConfig.Base(ci, this._annotationIntrospector, this._visibilityChecker, this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
      }

      public MapperConfig.Base withAnnotationIntrospector(AnnotationIntrospector ai) {
         return new MapperConfig.Base(this._classIntrospector, ai, this._visibilityChecker, this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
      }

      public MapperConfig.Base withVisibilityChecker(VisibilityChecker<?> vc) {
         return new MapperConfig.Base(this._classIntrospector, this._annotationIntrospector, vc, this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
      }

      public MapperConfig.Base withPropertyNamingStrategy(PropertyNamingStrategy pns) {
         return new MapperConfig.Base(this._classIntrospector, this._annotationIntrospector, this._visibilityChecker, pns, this._typeFactory, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
      }

      public MapperConfig.Base withTypeFactory(TypeFactory tf) {
         return new MapperConfig.Base(this._classIntrospector, this._annotationIntrospector, this._visibilityChecker, this._propertyNamingStrategy, tf, this._typeResolverBuilder, this._dateFormat, this._handlerInstantiator);
      }

      public MapperConfig.Base withTypeResolverBuilder(TypeResolverBuilder<?> typer) {
         return new MapperConfig.Base(this._classIntrospector, this._annotationIntrospector, this._visibilityChecker, this._propertyNamingStrategy, this._typeFactory, typer, this._dateFormat, this._handlerInstantiator);
      }

      public MapperConfig.Base withDateFormat(DateFormat df) {
         return new MapperConfig.Base(this._classIntrospector, this._annotationIntrospector, this._visibilityChecker, this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, df, this._handlerInstantiator);
      }

      public MapperConfig.Base withHandlerInstantiator(HandlerInstantiator hi) {
         return new MapperConfig.Base(this._classIntrospector, this._annotationIntrospector, this._visibilityChecker, this._propertyNamingStrategy, this._typeFactory, this._typeResolverBuilder, this._dateFormat, hi);
      }

      public ClassIntrospector<? extends BeanDescription> getClassIntrospector() {
         return this._classIntrospector;
      }

      public AnnotationIntrospector getAnnotationIntrospector() {
         return this._annotationIntrospector;
      }

      public VisibilityChecker<?> getVisibilityChecker() {
         return this._visibilityChecker;
      }

      public PropertyNamingStrategy getPropertyNamingStrategy() {
         return this._propertyNamingStrategy;
      }

      public TypeFactory getTypeFactory() {
         return this._typeFactory;
      }

      public TypeResolverBuilder<?> getTypeResolverBuilder() {
         return this._typeResolverBuilder;
      }

      public DateFormat getDateFormat() {
         return this._dateFormat;
      }

      public HandlerInstantiator getHandlerInstantiator() {
         return this._handlerInstantiator;
      }
   }
}
