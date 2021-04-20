package org.codehaus.jackson.map.deser;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.AbstractTypeResolver;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializerFactory;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.Deserializers;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.KeyDeserializers;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

public class BeanDeserializerFactory extends BasicDeserializerFactory {
   private static final Class<?>[] INIT_CAUSE_PARAMS = new Class[]{Throwable.class};
   public static final BeanDeserializerFactory instance = new BeanDeserializerFactory((DeserializerFactory.Config)null);
   protected final DeserializerFactory.Config _factoryConfig;

   /** @deprecated */
   @Deprecated
   public BeanDeserializerFactory() {
      this((DeserializerFactory.Config)null);
   }

   public BeanDeserializerFactory(DeserializerFactory.Config config) {
      if (config == null) {
         config = new BeanDeserializerFactory.ConfigImpl();
      }

      this._factoryConfig = (DeserializerFactory.Config)config;
   }

   public final DeserializerFactory.Config getConfig() {
      return this._factoryConfig;
   }

   public DeserializerFactory withConfig(DeserializerFactory.Config config) {
      if (this._factoryConfig == config) {
         return this;
      } else if (this.getClass() != BeanDeserializerFactory.class) {
         throw new IllegalStateException("Subtype of BeanDeserializerFactory (" + this.getClass().getName() + ") has not properly overridden method 'withAdditionalDeserializers': can not instantiate subtype with " + "additional deserializer definitions");
      } else {
         return new BeanDeserializerFactory(config);
      }
   }

   public KeyDeserializer createKeyDeserializer(DeserializationConfig config, JavaType type, BeanProperty property) throws JsonMappingException {
      if (this._factoryConfig.hasKeyDeserializers()) {
         BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspectClassAnnotations(type.getRawClass());
         Iterator i$ = this._factoryConfig.keyDeserializers().iterator();

         while(i$.hasNext()) {
            KeyDeserializers d = (KeyDeserializers)i$.next();
            KeyDeserializer deser = d.findKeyDeserializer(type, config, beanDesc, property);
            if (deser != null) {
               return deser;
            }
         }
      }

      return null;
   }

   protected JsonDeserializer<?> _findCustomArrayDeserializer(ArrayType type, DeserializationConfig config, DeserializerProvider provider, BeanProperty property, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      Iterator i$ = this._factoryConfig.deserializers().iterator();

      JsonDeserializer deser;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         Deserializers d = (Deserializers)i$.next();
         deser = d.findArrayDeserializer(type, config, provider, property, elementTypeDeserializer, elementDeserializer);
      } while(deser == null);

      return deser;
   }

   protected JsonDeserializer<?> _findCustomCollectionDeserializer(CollectionType type, DeserializationConfig config, DeserializerProvider provider, BasicBeanDescription beanDesc, BeanProperty property, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      Iterator i$ = this._factoryConfig.deserializers().iterator();

      JsonDeserializer deser;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         Deserializers d = (Deserializers)i$.next();
         deser = d.findCollectionDeserializer(type, config, provider, beanDesc, property, elementTypeDeserializer, elementDeserializer);
      } while(deser == null);

      return deser;
   }

   protected JsonDeserializer<?> _findCustomCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config, DeserializerProvider provider, BasicBeanDescription beanDesc, BeanProperty property, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      Iterator i$ = this._factoryConfig.deserializers().iterator();

      JsonDeserializer deser;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         Deserializers d = (Deserializers)i$.next();
         deser = d.findCollectionLikeDeserializer(type, config, provider, beanDesc, property, elementTypeDeserializer, elementDeserializer);
      } while(deser == null);

      return deser;
   }

   protected JsonDeserializer<?> _findCustomEnumDeserializer(Class<?> type, DeserializationConfig config, BasicBeanDescription beanDesc, BeanProperty property) throws JsonMappingException {
      Iterator i$ = this._factoryConfig.deserializers().iterator();

      JsonDeserializer deser;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         Deserializers d = (Deserializers)i$.next();
         deser = d.findEnumDeserializer(type, config, beanDesc, property);
      } while(deser == null);

      return deser;
   }

   protected JsonDeserializer<?> _findCustomMapDeserializer(MapType type, DeserializationConfig config, DeserializerProvider provider, BasicBeanDescription beanDesc, BeanProperty property, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      Iterator i$ = this._factoryConfig.deserializers().iterator();

      JsonDeserializer deser;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         Deserializers d = (Deserializers)i$.next();
         deser = d.findMapDeserializer(type, config, provider, beanDesc, property, keyDeserializer, elementTypeDeserializer, elementDeserializer);
      } while(deser == null);

      return deser;
   }

   protected JsonDeserializer<?> _findCustomMapLikeDeserializer(MapLikeType type, DeserializationConfig config, DeserializerProvider provider, BasicBeanDescription beanDesc, BeanProperty property, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      Iterator i$ = this._factoryConfig.deserializers().iterator();

      JsonDeserializer deser;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         Deserializers d = (Deserializers)i$.next();
         deser = d.findMapLikeDeserializer(type, config, provider, beanDesc, property, keyDeserializer, elementTypeDeserializer, elementDeserializer);
      } while(deser == null);

      return deser;
   }

   protected JsonDeserializer<?> _findCustomTreeNodeDeserializer(Class<? extends JsonNode> type, DeserializationConfig config, BeanProperty property) throws JsonMappingException {
      Iterator i$ = this._factoryConfig.deserializers().iterator();

      JsonDeserializer deser;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         Deserializers d = (Deserializers)i$.next();
         deser = d.findTreeNodeDeserializer(type, config, property);
      } while(deser == null);

      return deser;
   }

   protected JsonDeserializer<Object> _findCustomBeanDeserializer(JavaType type, DeserializationConfig config, DeserializerProvider provider, BasicBeanDescription beanDesc, BeanProperty property) throws JsonMappingException {
      Iterator i$ = this._factoryConfig.deserializers().iterator();

      JsonDeserializer deser;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         Deserializers d = (Deserializers)i$.next();
         deser = d.findBeanDeserializer(type, config, provider, beanDesc, property);
      } while(deser == null);

      return deser;
   }

   public JsonDeserializer<Object> createBeanDeserializer(DeserializationConfig config, DeserializerProvider p, JavaType type, BeanProperty property) throws JsonMappingException {
      if (type.isAbstract()) {
         type = this.mapAbstractType(config, type);
      }

      BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspect(type);
      JsonDeserializer<Object> ad = this.findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
      if (ad != null) {
         return ad;
      } else {
         JavaType newType = this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, (String)null);
         if (newType.getRawClass() != type.getRawClass()) {
            type = newType;
            beanDesc = (BasicBeanDescription)config.introspect(newType);
         }

         JsonDeserializer<Object> custom = this._findCustomBeanDeserializer(type, config, p, beanDesc, property);
         if (custom != null) {
            return custom;
         } else if (type.isThrowable()) {
            return this.buildThrowableDeserializer(config, type, beanDesc, property);
         } else {
            if (type.isAbstract()) {
               JavaType concreteType = this.materializeAbstractType(config, beanDesc);
               if (concreteType != null) {
                  beanDesc = (BasicBeanDescription)config.introspect(concreteType);
                  return this.buildBeanDeserializer(config, concreteType, beanDesc, property);
               }
            }

            JsonDeserializer<Object> deser = this.findStdBeanDeserializer(config, p, type, property);
            if (deser != null) {
               return deser;
            } else if (!this.isPotentialBeanType(type.getRawClass())) {
               return null;
            } else {
               return (JsonDeserializer)(type.isAbstract() ? new AbstractDeserializer(type) : this.buildBeanDeserializer(config, type, beanDesc, property));
            }
         }
      }
   }

   protected JavaType mapAbstractType(DeserializationConfig config, JavaType type) throws JsonMappingException {
      while(true) {
         JavaType next = this._mapAbstractType2(config, type);
         if (next == null) {
            return type;
         }

         Class<?> prevCls = type.getRawClass();
         Class<?> nextCls = next.getRawClass();
         if (prevCls == nextCls || !prevCls.isAssignableFrom(nextCls)) {
            throw new IllegalArgumentException("Invalid abstract type resolution from " + type + " to " + next + ": latter is not a subtype of former");
         }

         type = next;
      }
   }

   protected JavaType _mapAbstractType2(DeserializationConfig config, JavaType type) throws JsonMappingException {
      Class<?> currClass = type.getRawClass();
      if (this._factoryConfig.hasAbstractTypeResolvers()) {
         Iterator i$ = this._factoryConfig.abstractTypeResolvers().iterator();

         while(i$.hasNext()) {
            AbstractTypeResolver resolver = (AbstractTypeResolver)i$.next();
            JavaType concrete = resolver.findTypeMapping(config, type);
            if (concrete != null && concrete.getRawClass() != currClass) {
               return concrete;
            }
         }
      }

      AbstractTypeResolver resolver = config.getAbstractTypeResolver();
      if (resolver != null) {
         JavaType concrete = resolver.findTypeMapping(config, type);
         if (concrete != null && concrete.getRawClass() != currClass) {
            return concrete;
         }
      }

      return null;
   }

   protected JavaType materializeAbstractType(DeserializationConfig config, BasicBeanDescription beanDesc) throws JsonMappingException {
      AbstractTypeResolver resolver = config.getAbstractTypeResolver();
      if (resolver == null && !this._factoryConfig.hasAbstractTypeResolvers()) {
         return null;
      } else {
         JavaType abstractType = beanDesc.getType();
         AnnotationIntrospector intr = config.getAnnotationIntrospector();
         if (intr.findTypeResolver(config, beanDesc.getClassInfo(), abstractType) != null) {
            return null;
         } else {
            if (resolver != null) {
               JavaType concrete = resolver.resolveAbstractType(config, abstractType);
               if (concrete != null) {
                  return concrete;
               }
            }

            Iterator i$ = this._factoryConfig.abstractTypeResolvers().iterator();

            JavaType concrete;
            do {
               if (!i$.hasNext()) {
                  return null;
               }

               AbstractTypeResolver r = (AbstractTypeResolver)i$.next();
               concrete = r.resolveAbstractType(config, abstractType);
            } while(concrete == null);

            return concrete;
         }
      }
   }

   public JsonDeserializer<Object> buildBeanDeserializer(DeserializationConfig config, JavaType type, BasicBeanDescription beanDesc, BeanProperty property) throws JsonMappingException {
      BeanDeserializerBuilder builder = this.constructBeanDeserializerBuilder(beanDesc);
      builder.setCreators(this.findDeserializerCreators(config, beanDesc));
      this.addBeanProps(config, beanDesc, builder);
      this.addReferenceProperties(config, beanDesc, builder);
      BeanDeserializerModifier mod;
      if (this._factoryConfig.hasDeserializerModifiers()) {
         for(Iterator i$ = this._factoryConfig.deserializerModifiers().iterator(); i$.hasNext(); builder = mod.updateBuilder(config, beanDesc, builder)) {
            mod = (BeanDeserializerModifier)i$.next();
         }
      }

      JsonDeserializer<?> deserializer = builder.build(property);
      BeanDeserializerModifier mod;
      if (this._factoryConfig.hasDeserializerModifiers()) {
         for(Iterator i$ = this._factoryConfig.deserializerModifiers().iterator(); i$.hasNext(); deserializer = mod.modifyDeserializer(config, beanDesc, deserializer)) {
            mod = (BeanDeserializerModifier)i$.next();
         }
      }

      return deserializer;
   }

   public JsonDeserializer<Object> buildThrowableDeserializer(DeserializationConfig config, JavaType type, BasicBeanDescription beanDesc, BeanProperty property) throws JsonMappingException {
      BeanDeserializerBuilder builder = this.constructBeanDeserializerBuilder(beanDesc);
      builder.setCreators(this.findDeserializerCreators(config, beanDesc));
      this.addBeanProps(config, beanDesc, builder);
      AnnotatedMethod am = beanDesc.findMethod("initCause", INIT_CAUSE_PARAMS);
      if (am != null) {
         SettableBeanProperty prop = this.constructSettableProperty(config, beanDesc, "cause", am);
         if (prop != null) {
            builder.addProperty(prop);
         }
      }

      builder.addIgnorable("localizedMessage");
      builder.addIgnorable("message");
      BeanDeserializerModifier mod;
      if (this._factoryConfig.hasDeserializerModifiers()) {
         for(Iterator i$ = this._factoryConfig.deserializerModifiers().iterator(); i$.hasNext(); builder = mod.updateBuilder(config, beanDesc, builder)) {
            mod = (BeanDeserializerModifier)i$.next();
         }
      }

      JsonDeserializer<?> deserializer = builder.build(property);
      if (deserializer instanceof BeanDeserializer) {
         deserializer = new ThrowableDeserializer((BeanDeserializer)deserializer);
      }

      BeanDeserializerModifier mod;
      if (this._factoryConfig.hasDeserializerModifiers()) {
         for(Iterator i$ = this._factoryConfig.deserializerModifiers().iterator(); i$.hasNext(); deserializer = mod.modifyDeserializer(config, beanDesc, (JsonDeserializer)deserializer)) {
            mod = (BeanDeserializerModifier)i$.next();
         }
      }

      return (JsonDeserializer)deserializer;
   }

   protected BeanDeserializerBuilder constructBeanDeserializerBuilder(BasicBeanDescription beanDesc) {
      return new BeanDeserializerBuilder(beanDesc);
   }

   protected CreatorContainer findDeserializerCreators(DeserializationConfig config, BasicBeanDescription beanDesc) throws JsonMappingException {
      boolean fixAccess = config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
      CreatorContainer creators = new CreatorContainer(beanDesc, fixAccess);
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      if (beanDesc.getType().isConcrete()) {
         Constructor<?> defaultCtor = beanDesc.findDefaultConstructor();
         if (defaultCtor != null) {
            if (fixAccess) {
               ClassUtil.checkAndFixAccess(defaultCtor);
            }

            creators.setDefaultConstructor(defaultCtor);
         }
      }

      VisibilityChecker<?> vchecker = config.getDefaultVisibilityChecker();
      if (!config.isEnabled(DeserializationConfig.Feature.AUTO_DETECT_CREATORS)) {
         vchecker = vchecker.withCreatorVisibility(JsonAutoDetect.Visibility.NONE);
      }

      vchecker = config.getAnnotationIntrospector().findAutoDetectVisibility(beanDesc.getClassInfo(), vchecker);
      this._addDeserializerConstructors(config, beanDesc, vchecker, intr, creators);
      this._addDeserializerFactoryMethods(config, beanDesc, vchecker, intr, creators);
      return creators;
   }

   protected void _addDeserializerConstructors(DeserializationConfig config, BasicBeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorContainer creators) throws JsonMappingException {
      Iterator i$ = beanDesc.getConstructors().iterator();

      while(true) {
         AnnotatedConstructor ctor;
         int argCount;
         boolean isCreator;
         boolean isVisible;
         SettableBeanProperty[] properties;
         do {
            while(true) {
               do {
                  if (!i$.hasNext()) {
                     return;
                  }

                  ctor = (AnnotatedConstructor)i$.next();
                  argCount = ctor.getParameterCount();
               } while(argCount < 1);

               isCreator = intr.hasCreatorAnnotation(ctor);
               isVisible = vchecker.isCreatorVisible((AnnotatedMember)ctor);
               if (argCount != 1) {
                  break;
               }

               AnnotatedParameter param = ctor.getParameter(0);
               String name = intr.findPropertyNameForParam(param);
               if (name != null && name.length() != 0) {
                  properties = new SettableBeanProperty[]{this.constructCreatorProperty(config, beanDesc, name, 0, param)};
                  creators.addPropertyConstructor(ctor, properties);
               } else {
                  Class<?> type = ctor.getParameterClass(0);
                  if (type == String.class) {
                     if (isCreator || isVisible) {
                        creators.addStringConstructor(ctor);
                     }
                  } else if (type != Integer.TYPE && type != Integer.class) {
                     if (type != Long.TYPE && type != Long.class) {
                        if (isCreator) {
                           creators.addDelegatingConstructor(ctor);
                        }
                     } else if (isCreator || isVisible) {
                        creators.addLongConstructor(ctor);
                     }
                  } else if (isCreator || isVisible) {
                     creators.addIntConstructor(ctor);
                  }
               }
            }
         } while(!isCreator && !isVisible);

         boolean annotationFound = false;
         boolean notAnnotatedParamFound = false;
         properties = new SettableBeanProperty[argCount];

         for(int i = 0; i < argCount; ++i) {
            AnnotatedParameter param = ctor.getParameter(i);
            String name = param == null ? null : intr.findPropertyNameForParam(param);
            notAnnotatedParamFound |= name == null || name.length() == 0;
            annotationFound |= !notAnnotatedParamFound;
            if (notAnnotatedParamFound && (annotationFound || isCreator)) {
               throw new IllegalArgumentException("Argument #" + i + " of constructor " + ctor + " has no property name annotation; must have name when multiple-paramater constructor annotated as Creator");
            }

            properties[i] = this.constructCreatorProperty(config, beanDesc, name, i, param);
         }

         if (annotationFound) {
            creators.addPropertyConstructor(ctor, properties);
         }
      }
   }

   protected void _addDeserializerFactoryMethods(DeserializationConfig config, BasicBeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorContainer creators) throws JsonMappingException {
      Iterator i$ = beanDesc.getFactoryMethods().iterator();

      while(true) {
         AnnotatedMethod factory;
         int argCount;
         while(true) {
            do {
               if (!i$.hasNext()) {
                  return;
               }

               factory = (AnnotatedMethod)i$.next();
               argCount = factory.getParameterCount();
            } while(argCount < 1);

            boolean isCreator = intr.hasCreatorAnnotation(factory);
            if (argCount == 1) {
               String name = intr.findPropertyNameForParam(factory.getParameter(0));
               if (name != null && name.length() != 0) {
                  break;
               }

               Class<?> type = factory.getParameterClass(0);
               if (type == String.class) {
                  if (isCreator || vchecker.isCreatorVisible((AnnotatedMember)factory)) {
                     creators.addStringFactory(factory);
                  }
               } else if (type != Integer.TYPE && type != Integer.class) {
                  if (type != Long.TYPE && type != Long.class) {
                     if (intr.hasCreatorAnnotation(factory)) {
                        creators.addDelegatingFactory(factory);
                     }
                  } else if (isCreator || vchecker.isCreatorVisible((AnnotatedMember)factory)) {
                     creators.addLongFactory(factory);
                  }
               } else if (isCreator || vchecker.isCreatorVisible((AnnotatedMember)factory)) {
                  creators.addIntFactory(factory);
               }
            } else {
               if (!intr.hasCreatorAnnotation(factory)) {
                  continue;
               }
               break;
            }
         }

         SettableBeanProperty[] properties = new SettableBeanProperty[argCount];

         for(int i = 0; i < argCount; ++i) {
            AnnotatedParameter param = factory.getParameter(i);
            String name = intr.findPropertyNameForParam(param);
            if (name == null || name.length() == 0) {
               throw new IllegalArgumentException("Argument #" + i + " of factory method " + factory + " has no property name annotation; must have when multiple-paramater static method annotated as Creator");
            }

            properties[i] = this.constructCreatorProperty(config, beanDesc, name, i, param);
         }

         creators.addPropertyFactory(factory, properties);
      }
   }

   protected void addBeanProps(DeserializationConfig config, BasicBeanDescription beanDesc, BeanDeserializerBuilder builder) throws JsonMappingException {
      VisibilityChecker<?> vchecker = config.getDefaultVisibilityChecker();
      if (!config.isEnabled(DeserializationConfig.Feature.AUTO_DETECT_SETTERS)) {
         vchecker = vchecker.withSetterVisibility(JsonAutoDetect.Visibility.NONE);
      }

      if (!config.isEnabled(DeserializationConfig.Feature.AUTO_DETECT_FIELDS)) {
         vchecker = vchecker.withFieldVisibility(JsonAutoDetect.Visibility.NONE);
      }

      vchecker = config.getAnnotationIntrospector().findAutoDetectVisibility(beanDesc.getClassInfo(), vchecker);
      Map<String, AnnotatedMethod> setters = beanDesc.findSetters(vchecker);
      AnnotatedMethod anySetter = beanDesc.findAnySetter();
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      boolean ignoreAny = false;
      Boolean B = intr.findIgnoreUnknownProperties(beanDesc.getClassInfo());
      if (B != null) {
         ignoreAny = B;
         builder.setIgnoreUnknownProperties(ignoreAny);
      }

      HashSet<String> ignored = ArrayBuilders.arrayToSet(intr.findPropertiesToIgnore(beanDesc.getClassInfo()));
      Iterator i$ = ignored.iterator();

      while(i$.hasNext()) {
         String propName = (String)i$.next();
         builder.addIgnorable(propName);
      }

      AnnotatedClass ac = beanDesc.getClassInfo();
      Iterator i$ = ac.ignoredMemberMethods().iterator();

      String name;
      while(i$.hasNext()) {
         AnnotatedMethod am = (AnnotatedMethod)i$.next();
         name = beanDesc.okNameForSetter(am);
         if (name != null) {
            builder.addIgnorable(name);
         }
      }

      i$ = ac.ignoredFields().iterator();

      while(i$.hasNext()) {
         AnnotatedField af = (AnnotatedField)i$.next();
         builder.addIgnorable(af.getName());
      }

      HashMap<Class<?>, Boolean> ignoredTypes = new HashMap();
      i$ = setters.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, AnnotatedMethod> en = (Entry)i$.next();
         name = (String)en.getKey();
         if (!ignored.contains(name)) {
            AnnotatedMethod setter = (AnnotatedMethod)en.getValue();
            Class<?> type = setter.getParameterClass(0);
            if (this.isIgnorableType(config, beanDesc, type, ignoredTypes)) {
               builder.addIgnorable(name);
            } else {
               SettableBeanProperty prop = this.constructSettableProperty(config, beanDesc, name, setter);
               if (prop != null) {
                  builder.addProperty(prop);
               }
            }
         }
      }

      if (anySetter != null) {
         builder.setAnySetter(this.constructAnySetter(config, beanDesc, anySetter));
      }

      HashSet<String> addedProps = new HashSet(setters.keySet());
      LinkedHashMap<String, AnnotatedField> fieldsByProp = beanDesc.findDeserializableFields(vchecker, addedProps);
      Iterator i$ = fieldsByProp.entrySet().iterator();

      Class rt;
      while(i$.hasNext()) {
         Entry<String, AnnotatedField> en = (Entry)i$.next();
         String name = (String)en.getKey();
         if (!ignored.contains(name) && !builder.hasProperty(name)) {
            AnnotatedField field = (AnnotatedField)en.getValue();
            rt = field.getRawType();
            if (this.isIgnorableType(config, beanDesc, rt, ignoredTypes)) {
               builder.addIgnorable(name);
            } else {
               SettableBeanProperty prop = this.constructSettableProperty(config, beanDesc, name, field);
               if (prop != null) {
                  builder.addProperty(prop);
                  addedProps.add(name);
               }
            }
         }
      }

      if (config.isEnabled(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS)) {
         Map<String, AnnotatedMethod> getters = beanDesc.findGetters(vchecker, addedProps);
         Iterator i$ = getters.entrySet().iterator();

         while(true) {
            Entry en;
            AnnotatedMethod getter;
            do {
               if (!i$.hasNext()) {
                  return;
               }

               en = (Entry)i$.next();
               getter = (AnnotatedMethod)en.getValue();
               rt = getter.getRawType();
            } while(!Collection.class.isAssignableFrom(rt) && !Map.class.isAssignableFrom(rt));

            String name = (String)en.getKey();
            if (!ignored.contains(name) && !builder.hasProperty(name)) {
               builder.addProperty(this.constructSetterlessProperty(config, beanDesc, name, getter));
               addedProps.add(name);
            }
         }
      }
   }

   protected void addReferenceProperties(DeserializationConfig config, BasicBeanDescription beanDesc, BeanDeserializerBuilder builder) throws JsonMappingException {
      Map<String, AnnotatedMember> refs = beanDesc.findBackReferenceProperties();
      if (refs != null) {
         Iterator i$ = refs.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, AnnotatedMember> en = (Entry)i$.next();
            String name = (String)en.getKey();
            AnnotatedMember m = (AnnotatedMember)en.getValue();
            if (m instanceof AnnotatedMethod) {
               builder.addBackReferenceProperty(name, this.constructSettableProperty(config, beanDesc, m.getName(), (AnnotatedMethod)m));
            } else {
               builder.addBackReferenceProperty(name, this.constructSettableProperty(config, beanDesc, m.getName(), (AnnotatedField)m));
            }
         }
      }

   }

   protected SettableAnyProperty constructAnySetter(DeserializationConfig config, BasicBeanDescription beanDesc, AnnotatedMethod setter) throws JsonMappingException {
      if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
         setter.fixAccess();
      }

      JavaType type = beanDesc.bindingsForBeanType().resolveType(setter.getParameterType(1));
      BeanProperty.Std property = new BeanProperty.Std(setter.getName(), type, beanDesc.getClassAnnotations(), setter);
      type = this.resolveType(config, beanDesc, type, setter, property);
      JsonDeserializer<Object> deser = this.findDeserializerFromAnnotation(config, setter, property);
      if (deser != null) {
         SettableAnyProperty prop = new SettableAnyProperty(property, setter, type);
         prop.setValueDeserializer(deser);
         return prop;
      } else {
         type = this.modifyTypeByAnnotation(config, setter, type, property.getName());
         return new SettableAnyProperty(property, setter, type);
      }
   }

   protected SettableBeanProperty constructSettableProperty(DeserializationConfig config, BasicBeanDescription beanDesc, String name, AnnotatedMethod setter) throws JsonMappingException {
      if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
         setter.fixAccess();
      }

      JavaType t0 = beanDesc.bindingsForBeanType().resolveType(setter.getParameterType(0));
      BeanProperty.Std property = new BeanProperty.Std(name, t0, beanDesc.getClassAnnotations(), setter);
      JavaType type = this.resolveType(config, beanDesc, t0, setter, property);
      if (type != t0) {
         property = property.withType(type);
      }

      JsonDeserializer<Object> propDeser = this.findDeserializerFromAnnotation(config, setter, property);
      type = this.modifyTypeByAnnotation(config, setter, type, name);
      TypeDeserializer typeDeser = (TypeDeserializer)type.getTypeHandler();
      SettableBeanProperty prop = new SettableBeanProperty.MethodProperty(name, type, typeDeser, beanDesc.getClassAnnotations(), setter);
      if (propDeser != null) {
         prop.setValueDeserializer(propDeser);
      }

      AnnotationIntrospector.ReferenceProperty ref = config.getAnnotationIntrospector().findReferenceType(setter);
      if (ref != null && ref.isManagedReference()) {
         prop.setManagedReferenceName(ref.getName());
      }

      return prop;
   }

   protected SettableBeanProperty constructSettableProperty(DeserializationConfig config, BasicBeanDescription beanDesc, String name, AnnotatedField field) throws JsonMappingException {
      if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
         field.fixAccess();
      }

      JavaType t0 = beanDesc.bindingsForBeanType().resolveType(field.getGenericType());
      BeanProperty.Std property = new BeanProperty.Std(name, t0, beanDesc.getClassAnnotations(), field);
      JavaType type = this.resolveType(config, beanDesc, t0, field, property);
      if (type != t0) {
         property = property.withType(type);
      }

      JsonDeserializer<Object> propDeser = this.findDeserializerFromAnnotation(config, field, property);
      type = this.modifyTypeByAnnotation(config, field, type, name);
      TypeDeserializer typeDeser = (TypeDeserializer)type.getTypeHandler();
      SettableBeanProperty prop = new SettableBeanProperty.FieldProperty(name, type, typeDeser, beanDesc.getClassAnnotations(), field);
      if (propDeser != null) {
         prop.setValueDeserializer(propDeser);
      }

      AnnotationIntrospector.ReferenceProperty ref = config.getAnnotationIntrospector().findReferenceType(field);
      if (ref != null && ref.isManagedReference()) {
         prop.setManagedReferenceName(ref.getName());
      }

      return prop;
   }

   protected SettableBeanProperty constructSetterlessProperty(DeserializationConfig config, BasicBeanDescription beanDesc, String name, AnnotatedMethod getter) throws JsonMappingException {
      if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
         getter.fixAccess();
      }

      JavaType type = getter.getType(beanDesc.bindingsForBeanType());
      BeanProperty.Std property = new BeanProperty.Std(name, type, beanDesc.getClassAnnotations(), getter);
      JsonDeserializer<Object> propDeser = this.findDeserializerFromAnnotation(config, getter, property);
      type = this.modifyTypeByAnnotation(config, getter, type, name);
      TypeDeserializer typeDeser = (TypeDeserializer)type.getTypeHandler();
      SettableBeanProperty prop = new SettableBeanProperty.SetterlessProperty(name, type, typeDeser, beanDesc.getClassAnnotations(), getter);
      if (propDeser != null) {
         prop.setValueDeserializer(propDeser);
      }

      return prop;
   }

   protected boolean isPotentialBeanType(Class<?> type) {
      String typeStr = ClassUtil.canBeABeanType(type);
      if (typeStr != null) {
         throw new IllegalArgumentException("Can not deserialize Class " + type.getName() + " (of type " + typeStr + ") as a Bean");
      } else if (ClassUtil.isProxyType(type)) {
         throw new IllegalArgumentException("Can not deserialize Proxy class " + type.getName() + " as a Bean");
      } else {
         typeStr = ClassUtil.isLocalType(type);
         if (typeStr != null) {
            throw new IllegalArgumentException("Can not deserialize Class " + type.getName() + " (of type " + typeStr + ") as a Bean");
         } else {
            return true;
         }
      }
   }

   protected boolean isIgnorableType(DeserializationConfig config, BasicBeanDescription beanDesc, Class<?> type, Map<Class<?>, Boolean> ignoredTypes) {
      Boolean status = (Boolean)ignoredTypes.get(type);
      if (status == null) {
         BasicBeanDescription desc = (BasicBeanDescription)config.introspectClassAnnotations(type);
         status = config.getAnnotationIntrospector().isIgnorableType(desc.getClassInfo());
         if (status == null) {
            status = Boolean.FALSE;
         }
      }

      return status;
   }

   public static class ConfigImpl extends DeserializerFactory.Config {
      protected static final KeyDeserializers[] NO_KEY_DESERIALIZERS = new KeyDeserializers[0];
      protected static final BeanDeserializerModifier[] NO_MODIFIERS = new BeanDeserializerModifier[0];
      protected static final AbstractTypeResolver[] NO_ABSTRACT_TYPE_RESOLVERS = new AbstractTypeResolver[0];
      protected final Deserializers[] _additionalDeserializers;
      protected final KeyDeserializers[] _additionalKeyDeserializers;
      protected final BeanDeserializerModifier[] _modifiers;
      protected final AbstractTypeResolver[] _abstractTypeResolvers;

      public ConfigImpl() {
         this((Deserializers[])null, (KeyDeserializers[])null, (BeanDeserializerModifier[])null, (AbstractTypeResolver[])null);
      }

      protected ConfigImpl(Deserializers[] allAdditionalDeserializers, KeyDeserializers[] allAdditionalKeyDeserializers, BeanDeserializerModifier[] modifiers, AbstractTypeResolver[] atr) {
         this._additionalDeserializers = allAdditionalDeserializers == null ? BeanDeserializerFactory.NO_DESERIALIZERS : allAdditionalDeserializers;
         this._additionalKeyDeserializers = allAdditionalKeyDeserializers == null ? NO_KEY_DESERIALIZERS : allAdditionalKeyDeserializers;
         this._modifiers = modifiers == null ? NO_MODIFIERS : modifiers;
         this._abstractTypeResolvers = atr == null ? NO_ABSTRACT_TYPE_RESOLVERS : atr;
      }

      public DeserializerFactory.Config withAdditionalDeserializers(Deserializers additional) {
         if (additional == null) {
            throw new IllegalArgumentException("Can not pass null Deserializers");
         } else {
            Deserializers[] all = (Deserializers[])ArrayBuilders.insertInListNoDup(this._additionalDeserializers, additional);
            return new BeanDeserializerFactory.ConfigImpl(all, this._additionalKeyDeserializers, this._modifiers, this._abstractTypeResolvers);
         }
      }

      public DeserializerFactory.Config withAdditionalKeyDeserializers(KeyDeserializers additional) {
         if (additional == null) {
            throw new IllegalArgumentException("Can not pass null KeyDeserializers");
         } else {
            KeyDeserializers[] all = (KeyDeserializers[])ArrayBuilders.insertInListNoDup(this._additionalKeyDeserializers, additional);
            return new BeanDeserializerFactory.ConfigImpl(this._additionalDeserializers, all, this._modifiers, this._abstractTypeResolvers);
         }
      }

      public DeserializerFactory.Config withDeserializerModifier(BeanDeserializerModifier modifier) {
         if (modifier == null) {
            throw new IllegalArgumentException("Can not pass null modifier");
         } else {
            BeanDeserializerModifier[] all = (BeanDeserializerModifier[])ArrayBuilders.insertInListNoDup(this._modifiers, modifier);
            return new BeanDeserializerFactory.ConfigImpl(this._additionalDeserializers, this._additionalKeyDeserializers, all, this._abstractTypeResolvers);
         }
      }

      public DeserializerFactory.Config withAbstractTypeResolver(AbstractTypeResolver resolver) {
         if (resolver == null) {
            throw new IllegalArgumentException("Can not pass null resolver");
         } else {
            AbstractTypeResolver[] all = (AbstractTypeResolver[])ArrayBuilders.insertInListNoDup(this._abstractTypeResolvers, resolver);
            return new BeanDeserializerFactory.ConfigImpl(this._additionalDeserializers, this._additionalKeyDeserializers, this._modifiers, all);
         }
      }

      public boolean hasDeserializers() {
         return this._additionalDeserializers.length > 0;
      }

      public boolean hasKeyDeserializers() {
         return this._additionalKeyDeserializers.length > 0;
      }

      public boolean hasDeserializerModifiers() {
         return this._modifiers.length > 0;
      }

      public boolean hasAbstractTypeResolvers() {
         return this._abstractTypeResolvers.length > 0;
      }

      public Iterable<Deserializers> deserializers() {
         return ArrayBuilders.arrayAsIterable(this._additionalDeserializers);
      }

      public Iterable<KeyDeserializers> keyDeserializers() {
         return ArrayBuilders.arrayAsIterable(this._additionalKeyDeserializers);
      }

      public Iterable<BeanDeserializerModifier> deserializerModifiers() {
         return ArrayBuilders.arrayAsIterable(this._modifiers);
      }

      public Iterable<AbstractTypeResolver> abstractTypeResolvers() {
         return ArrayBuilders.arrayAsIterable(this._abstractTypeResolvers);
      }
   }
}
