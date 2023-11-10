package org.codehaus.jackson.map;

import java.text.DateFormat;
import java.util.HashMap;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.NopAnnotationIntrospector;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.jsontype.SubtypeResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.type.ClassKey;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.util.LinkedNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.type.JavaType;

public class DeserializationConfig extends MapperConfig<DeserializationConfig> {
   protected static final int DEFAULT_FEATURE_FLAGS = DeserializationConfig.Feature.collectDefaults();
   protected int _featureFlags;
   protected LinkedNode<DeserializationProblemHandler> _problemHandlers;
   protected AbstractTypeResolver _abstractTypeResolver;
   protected JsonNodeFactory _nodeFactory;

   public DeserializationConfig(ClassIntrospector<? extends BeanDescription> intr, AnnotationIntrospector annIntr, VisibilityChecker<?> vc, SubtypeResolver subtypeResolver, PropertyNamingStrategy propertyNamingStrategy, TypeFactory typeFactory, HandlerInstantiator handlerInstantiator) {
      super(intr, annIntr, vc, subtypeResolver, propertyNamingStrategy, typeFactory, handlerInstantiator);
      this._featureFlags = DEFAULT_FEATURE_FLAGS;
      this._nodeFactory = JsonNodeFactory.instance;
   }

   protected DeserializationConfig(DeserializationConfig src) {
      this(src, src._base);
   }

   private DeserializationConfig(DeserializationConfig src, HashMap<ClassKey, Class<?>> mixins, SubtypeResolver str) {
      this(src, src._base);
      this._mixInAnnotations = mixins;
      this._subtypeResolver = str;
   }

   protected DeserializationConfig(DeserializationConfig src, MapperConfig.Base base) {
      super(src, base, src._subtypeResolver);
      this._featureFlags = DEFAULT_FEATURE_FLAGS;
      this._featureFlags = src._featureFlags;
      this._abstractTypeResolver = src._abstractTypeResolver;
      this._problemHandlers = src._problemHandlers;
      this._nodeFactory = src._nodeFactory;
   }

   protected DeserializationConfig(DeserializationConfig src, JsonNodeFactory f) {
      super(src);
      this._featureFlags = DEFAULT_FEATURE_FLAGS;
      this._featureFlags = src._featureFlags;
      this._abstractTypeResolver = src._abstractTypeResolver;
      this._problemHandlers = src._problemHandlers;
      this._nodeFactory = f;
   }

   public DeserializationConfig withClassIntrospector(ClassIntrospector<? extends BeanDescription> ci) {
      return new DeserializationConfig(this, this._base.withClassIntrospector(ci));
   }

   public DeserializationConfig withAnnotationIntrospector(AnnotationIntrospector ai) {
      return new DeserializationConfig(this, this._base.withAnnotationIntrospector(ai));
   }

   public DeserializationConfig withVisibilityChecker(VisibilityChecker<?> vc) {
      return new DeserializationConfig(this, this._base.withVisibilityChecker(vc));
   }

   public DeserializationConfig withTypeResolverBuilder(TypeResolverBuilder<?> trb) {
      return new DeserializationConfig(this, this._base.withTypeResolverBuilder(trb));
   }

   public DeserializationConfig withSubtypeResolver(SubtypeResolver str) {
      DeserializationConfig cfg = new DeserializationConfig(this);
      cfg._subtypeResolver = str;
      return cfg;
   }

   public DeserializationConfig withPropertyNamingStrategy(PropertyNamingStrategy pns) {
      return new DeserializationConfig(this, this._base.withPropertyNamingStrategy(pns));
   }

   public DeserializationConfig withTypeFactory(TypeFactory tf) {
      return tf == this._base.getTypeFactory() ? this : new DeserializationConfig(this, this._base.withTypeFactory(tf));
   }

   public DeserializationConfig withDateFormat(DateFormat df) {
      return df == this._base.getDateFormat() ? this : new DeserializationConfig(this, this._base.withDateFormat(df));
   }

   public DeserializationConfig withHandlerInstantiator(HandlerInstantiator hi) {
      return hi == this._base.getHandlerInstantiator() ? this : new DeserializationConfig(this, this._base.withHandlerInstantiator(hi));
   }

   public DeserializationConfig withNodeFactory(JsonNodeFactory f) {
      return new DeserializationConfig(this, f);
   }

   public void enable(DeserializationConfig.Feature f) {
      this._featureFlags |= f.getMask();
   }

   public void disable(DeserializationConfig.Feature f) {
      this._featureFlags &= ~f.getMask();
   }

   public void set(DeserializationConfig.Feature f, boolean state) {
      if (state) {
         this.enable(f);
      } else {
         this.disable(f);
      }

   }

   public final boolean isEnabled(DeserializationConfig.Feature f) {
      return (this._featureFlags & f.getMask()) != 0;
   }

   public void fromAnnotations(Class<?> cls) {
      AnnotationIntrospector ai = this.getAnnotationIntrospector();
      AnnotatedClass ac = AnnotatedClass.construct(cls, ai, (ClassIntrospector.MixInResolver)null);
      VisibilityChecker<?> prevVc = this.getDefaultVisibilityChecker();
      this._base = this._base.withVisibilityChecker(ai.findAutoDetectVisibility(ac, prevVc));
   }

   public DeserializationConfig createUnshared(SubtypeResolver subtypeResolver) {
      HashMap<ClassKey, Class<?>> mixins = this._mixInAnnotations;
      this._mixInAnnotationsShared = true;
      return new DeserializationConfig(this, mixins, subtypeResolver);
   }

   public AnnotationIntrospector getAnnotationIntrospector() {
      return (AnnotationIntrospector)(this.isEnabled(DeserializationConfig.Feature.USE_ANNOTATIONS) ? super.getAnnotationIntrospector() : NopAnnotationIntrospector.instance);
   }

   public <T extends BeanDescription> T introspectClassAnnotations(Class<?> cls) {
      return this.getClassIntrospector().forClassAnnotations(this, cls, this);
   }

   public <T extends BeanDescription> T introspectDirectClassAnnotations(Class<?> cls) {
      return this.getClassIntrospector().forDirectClassAnnotations(this, cls, this);
   }

   public boolean isAnnotationProcessingEnabled() {
      return this.isEnabled(DeserializationConfig.Feature.USE_ANNOTATIONS);
   }

   public boolean canOverrideAccessModifiers() {
      return this.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
   }

   public LinkedNode<DeserializationProblemHandler> getProblemHandlers() {
      return this._problemHandlers;
   }

   public void addHandler(DeserializationProblemHandler h) {
      if (!LinkedNode.contains(this._problemHandlers, h)) {
         this._problemHandlers = new LinkedNode(h, this._problemHandlers);
      }

   }

   public void clearHandlers() {
      this._problemHandlers = null;
   }

   public Base64Variant getBase64Variant() {
      return Base64Variants.getDefaultVariant();
   }

   public final JsonNodeFactory getNodeFactory() {
      return this._nodeFactory;
   }

   public <T extends BeanDescription> T introspect(JavaType type) {
      return this.getClassIntrospector().forDeserialization(this, type, this);
   }

   public <T extends BeanDescription> T introspectForCreation(JavaType type) {
      return this.getClassIntrospector().forCreation(this, type, this);
   }

   public JsonDeserializer<Object> deserializerInstance(Annotated annotated, Class<? extends JsonDeserializer<?>> deserClass) {
      HandlerInstantiator hi = this.getHandlerInstantiator();
      if (hi != null) {
         JsonDeserializer<?> deser = hi.deserializerInstance(this, annotated, deserClass);
         if (deser != null) {
            return deser;
         }
      }

      return (JsonDeserializer)ClassUtil.createInstance(deserClass, this.canOverrideAccessModifiers());
   }

   public KeyDeserializer keyDeserializerInstance(Annotated annotated, Class<? extends KeyDeserializer> keyDeserClass) {
      HandlerInstantiator hi = this.getHandlerInstantiator();
      if (hi != null) {
         KeyDeserializer keyDeser = hi.keyDeserializerInstance(this, annotated, keyDeserClass);
         if (keyDeser != null) {
            return keyDeser;
         }
      }

      return (KeyDeserializer)ClassUtil.createInstance(keyDeserClass, this.canOverrideAccessModifiers());
   }

   /** @deprecated */
   @Deprecated
   public DeserializationConfig createUnshared(TypeResolverBuilder<?> typer, VisibilityChecker<?> vc, SubtypeResolver str) {
      return this.createUnshared(str).withTypeResolverBuilder(typer).withVisibilityChecker(vc);
   }

   /** @deprecated */
   @Deprecated
   public void setNodeFactory(JsonNodeFactory nf) {
      this._nodeFactory = nf;
   }

   /** @deprecated */
   @Deprecated
   public AbstractTypeResolver getAbstractTypeResolver() {
      return this._abstractTypeResolver;
   }

   /** @deprecated */
   @Deprecated
   public void setAbstractTypeResolver(AbstractTypeResolver atr) {
      this._abstractTypeResolver = atr;
   }

   public static enum Feature {
      USE_ANNOTATIONS(true),
      AUTO_DETECT_SETTERS(true),
      AUTO_DETECT_CREATORS(true),
      AUTO_DETECT_FIELDS(true),
      USE_GETTERS_AS_SETTERS(true),
      CAN_OVERRIDE_ACCESS_MODIFIERS(true),
      USE_BIG_DECIMAL_FOR_FLOATS(false),
      USE_BIG_INTEGER_FOR_INTS(false),
      READ_ENUMS_USING_TO_STRING(false),
      FAIL_ON_UNKNOWN_PROPERTIES(true),
      FAIL_ON_NULL_FOR_PRIMITIVES(false),
      FAIL_ON_NUMBERS_FOR_ENUMS(false),
      WRAP_EXCEPTIONS(true),
      /** @deprecated */
      @Deprecated
      WRAP_ROOT_VALUE(false),
      ACCEPT_EMPTY_STRING_AS_NULL_OBJECT(false),
      ACCEPT_SINGLE_VALUE_AS_ARRAY(false);

      final boolean _defaultState;

      public static int collectDefaults() {
         int flags = 0;
         DeserializationConfig.Feature[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            DeserializationConfig.Feature f = arr$[i$];
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
