package org.codehaus.jackson.map;

import java.text.DateFormat;
import java.util.HashMap;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.jsontype.SubtypeResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.type.ClassKey;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

public class SerializationConfig extends MapperConfig<SerializationConfig> {
   protected static final int DEFAULT_FEATURE_FLAGS = SerializationConfig.Feature.collectDefaults();
   protected int _featureFlags;
   protected JsonSerialize.Inclusion _serializationInclusion;
   protected Class<?> _serializationView;
   protected FilterProvider _filterProvider;

   public SerializationConfig(ClassIntrospector<? extends BeanDescription> intr, AnnotationIntrospector annIntr, VisibilityChecker<?> vc, SubtypeResolver subtypeResolver, PropertyNamingStrategy propertyNamingStrategy, TypeFactory typeFactory, HandlerInstantiator handlerInstantiator) {
      super(intr, annIntr, vc, subtypeResolver, propertyNamingStrategy, typeFactory, handlerInstantiator);
      this._featureFlags = DEFAULT_FEATURE_FLAGS;
      this._serializationInclusion = null;
      this._filterProvider = null;
   }

   protected SerializationConfig(SerializationConfig src) {
      this(src, src._base);
   }

   protected SerializationConfig(SerializationConfig src, HashMap<ClassKey, Class<?>> mixins, SubtypeResolver str) {
      this(src, src._base);
      this._mixInAnnotations = mixins;
      this._subtypeResolver = str;
   }

   protected SerializationConfig(SerializationConfig src, MapperConfig.Base base) {
      super(src, base, src._subtypeResolver);
      this._featureFlags = DEFAULT_FEATURE_FLAGS;
      this._serializationInclusion = null;
      this._featureFlags = src._featureFlags;
      this._serializationInclusion = src._serializationInclusion;
      this._serializationView = src._serializationView;
      this._filterProvider = src._filterProvider;
   }

   protected SerializationConfig(SerializationConfig src, FilterProvider filters) {
      super(src);
      this._featureFlags = DEFAULT_FEATURE_FLAGS;
      this._serializationInclusion = null;
      this._featureFlags = src._featureFlags;
      this._serializationInclusion = src._serializationInclusion;
      this._serializationView = src._serializationView;
      this._filterProvider = filters;
   }

   protected SerializationConfig(SerializationConfig src, Class<?> view) {
      super(src);
      this._featureFlags = DEFAULT_FEATURE_FLAGS;
      this._serializationInclusion = null;
      this._featureFlags = src._featureFlags;
      this._serializationInclusion = src._serializationInclusion;
      this._serializationView = view;
      this._filterProvider = src._filterProvider;
   }

   public SerializationConfig withClassIntrospector(ClassIntrospector<? extends BeanDescription> ci) {
      return new SerializationConfig(this, this._base.withClassIntrospector(ci));
   }

   public SerializationConfig withAnnotationIntrospector(AnnotationIntrospector ai) {
      return new SerializationConfig(this, this._base.withAnnotationIntrospector(ai));
   }

   public SerializationConfig withVisibilityChecker(VisibilityChecker<?> vc) {
      return new SerializationConfig(this, this._base.withVisibilityChecker(vc));
   }

   public SerializationConfig withTypeResolverBuilder(TypeResolverBuilder<?> trb) {
      return new SerializationConfig(this, this._base.withTypeResolverBuilder(trb));
   }

   public SerializationConfig withSubtypeResolver(SubtypeResolver str) {
      SerializationConfig cfg = new SerializationConfig(this);
      cfg._subtypeResolver = str;
      return cfg;
   }

   public SerializationConfig withPropertyNamingStrategy(PropertyNamingStrategy pns) {
      return new SerializationConfig(this, this._base.withPropertyNamingStrategy(pns));
   }

   public SerializationConfig withTypeFactory(TypeFactory tf) {
      return new SerializationConfig(this, this._base.withTypeFactory(tf));
   }

   public SerializationConfig withDateFormat(DateFormat df) {
      SerializationConfig cfg = new SerializationConfig(this, this._base.withDateFormat(df));
      cfg.set(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, df == null);
      return cfg;
   }

   public SerializationConfig withHandlerInstantiator(HandlerInstantiator hi) {
      return new SerializationConfig(this, this._base.withHandlerInstantiator(hi));
   }

   public SerializationConfig withFilters(FilterProvider filterProvider) {
      return new SerializationConfig(this, filterProvider);
   }

   public SerializationConfig withView(Class<?> view) {
      return new SerializationConfig(this, view);
   }

   public void fromAnnotations(Class<?> cls) {
      AnnotationIntrospector ai = this.getAnnotationIntrospector();
      AnnotatedClass ac = AnnotatedClass.construct(cls, ai, (ClassIntrospector.MixInResolver)null);
      this._base = this._base.withVisibilityChecker(ai.findAutoDetectVisibility(ac, this.getDefaultVisibilityChecker()));
      JsonSerialize.Inclusion incl = ai.findSerializationInclusion(ac, (JsonSerialize.Inclusion)null);
      if (incl != this._serializationInclusion) {
         this.setSerializationInclusion(incl);
      }

      JsonSerialize.Typing typing = ai.findSerializationTyping(ac);
      if (typing != null) {
         this.set(SerializationConfig.Feature.USE_STATIC_TYPING, typing == JsonSerialize.Typing.STATIC);
      }

   }

   public SerializationConfig createUnshared(SubtypeResolver subtypeResolver) {
      HashMap<ClassKey, Class<?>> mixins = this._mixInAnnotations;
      this._mixInAnnotationsShared = true;
      return new SerializationConfig(this, mixins, subtypeResolver);
   }

   public AnnotationIntrospector getAnnotationIntrospector() {
      return this.isEnabled(SerializationConfig.Feature.USE_ANNOTATIONS) ? super.getAnnotationIntrospector() : AnnotationIntrospector.nopInstance();
   }

   public <T extends BeanDescription> T introspectClassAnnotations(Class<?> cls) {
      return this.getClassIntrospector().forClassAnnotations(this, cls, this);
   }

   public <T extends BeanDescription> T introspectDirectClassAnnotations(Class<?> cls) {
      return this.getClassIntrospector().forDirectClassAnnotations(this, cls, this);
   }

   public boolean isAnnotationProcessingEnabled() {
      return this.isEnabled(SerializationConfig.Feature.USE_ANNOTATIONS);
   }

   public boolean canOverrideAccessModifiers() {
      return this.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
   }

   public void enable(SerializationConfig.Feature f) {
      this._featureFlags |= f.getMask();
   }

   public void disable(SerializationConfig.Feature f) {
      this._featureFlags &= ~f.getMask();
   }

   public void set(SerializationConfig.Feature f, boolean state) {
      if (state) {
         this.enable(f);
      } else {
         this.disable(f);
      }

   }

   public final boolean isEnabled(SerializationConfig.Feature f) {
      return (this._featureFlags & f.getMask()) != 0;
   }

   public Class<?> getSerializationView() {
      return this._serializationView;
   }

   public JsonSerialize.Inclusion getSerializationInclusion() {
      if (this._serializationInclusion != null) {
         return this._serializationInclusion;
      } else {
         return this.isEnabled(SerializationConfig.Feature.WRITE_NULL_PROPERTIES) ? JsonSerialize.Inclusion.ALWAYS : JsonSerialize.Inclusion.NON_NULL;
      }
   }

   public void setSerializationInclusion(JsonSerialize.Inclusion props) {
      this._serializationInclusion = props;
      if (props == JsonSerialize.Inclusion.NON_NULL) {
         this.disable(SerializationConfig.Feature.WRITE_NULL_PROPERTIES);
      } else {
         this.enable(SerializationConfig.Feature.WRITE_NULL_PROPERTIES);
      }

   }

   public FilterProvider getFilterProvider() {
      return this._filterProvider;
   }

   public <T extends BeanDescription> T introspect(JavaType type) {
      return this.getClassIntrospector().forSerialization(this, type, this);
   }

   public JsonSerializer<Object> serializerInstance(Annotated annotated, Class<? extends JsonSerializer<?>> serClass) {
      HandlerInstantiator hi = this.getHandlerInstantiator();
      if (hi != null) {
         JsonSerializer<?> ser = hi.serializerInstance(this, annotated, serClass);
         if (ser != null) {
            return ser;
         }
      }

      return (JsonSerializer)ClassUtil.createInstance(serClass, this.canOverrideAccessModifiers());
   }

   /** @deprecated */
   @Deprecated
   public SerializationConfig createUnshared(TypeResolverBuilder<?> typer, VisibilityChecker<?> vc, SubtypeResolver str) {
      return this.createUnshared(str).withTypeResolverBuilder(typer).withVisibilityChecker(vc);
   }

   /** @deprecated */
   @Deprecated
   public final void setDateFormat(DateFormat df) {
      super.setDateFormat(df);
      this.set(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, df == null);
   }

   /** @deprecated */
   @Deprecated
   public void setSerializationView(Class<?> view) {
      this._serializationView = view;
   }

   public String toString() {
      return "[SerializationConfig: flags=0x" + Integer.toHexString(this._featureFlags) + "]";
   }

   public static enum Feature {
      USE_ANNOTATIONS(true),
      AUTO_DETECT_GETTERS(true),
      AUTO_DETECT_IS_GETTERS(true),
      AUTO_DETECT_FIELDS(true),
      CAN_OVERRIDE_ACCESS_MODIFIERS(true),
      /** @deprecated */
      @Deprecated
      WRITE_NULL_PROPERTIES(true),
      USE_STATIC_TYPING(false),
      DEFAULT_VIEW_INCLUSION(true),
      WRAP_ROOT_VALUE(false),
      INDENT_OUTPUT(false),
      SORT_PROPERTIES_ALPHABETICALLY(false),
      FAIL_ON_EMPTY_BEANS(true),
      WRAP_EXCEPTIONS(true),
      CLOSE_CLOSEABLE(false),
      FLUSH_AFTER_WRITE_VALUE(true),
      WRITE_DATES_AS_TIMESTAMPS(true),
      WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS(false),
      WRITE_ENUMS_USING_TO_STRING(false),
      WRITE_NULL_MAP_VALUES(true);

      final boolean _defaultState;

      public static int collectDefaults() {
         int flags = 0;
         SerializationConfig.Feature[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SerializationConfig.Feature f = arr$[i$];
            if (f.enabledByDefault()) {
               flags |= f.getMask();
            }
         }

         return flags;
      }

      private Feature(boolean defaultState) {
         this._defaultState = defaultState;
      }

      public boolean enabledByDefault() {
         return this._defaultState;
      }

      public int getMask() {
         return 1 << this.ordinal();
      }
   }
}
