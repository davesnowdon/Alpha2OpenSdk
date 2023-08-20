package org.codehaus.jackson.map.ser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerFactory;
import org.codehaus.jackson.map.Serializers;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

public class BeanSerializerFactory extends BasicSerializerFactory {
   public static final BeanSerializerFactory instance = new BeanSerializerFactory((SerializerFactory.Config)null);
   protected final SerializerFactory.Config _factoryConfig;

   /** @deprecated */
   @Deprecated
   protected BeanSerializerFactory() {
      this((SerializerFactory.Config)null);
   }

   protected BeanSerializerFactory(SerializerFactory.Config config) {
      if (config == null) {
         config = new BeanSerializerFactory.ConfigImpl();
      }

      this._factoryConfig = (SerializerFactory.Config)config;
   }

   public SerializerFactory.Config getConfig() {
      return this._factoryConfig;
   }

   public SerializerFactory withConfig(SerializerFactory.Config config) {
      if (this._factoryConfig == config) {
         return this;
      } else if (this.getClass() != BeanSerializerFactory.class) {
         throw new IllegalStateException("Subtype of BeanSerializerFactory (" + this.getClass().getName() + ") has not properly overridden method 'withAdditionalSerializers': can not instantiate subtype with " + "additional serializer definitions");
      } else {
         return new BeanSerializerFactory(config);
      }
   }

   protected Iterable<Serializers> customSerializers() {
      return this._factoryConfig.serializers();
   }

   public JsonSerializer<Object> createSerializer(SerializationConfig config, JavaType origType, BeanProperty property) throws JsonMappingException {
      BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspect(origType);
      JsonSerializer<?> ser = this.findSerializerFromAnnotation(config, beanDesc.getClassInfo(), property);
      if (ser != null) {
         return ser;
      } else {
         JavaType type = this.modifyTypeByAnnotation(config, beanDesc.getClassInfo(), origType);
         boolean staticTyping = type != origType;
         if (origType.isContainerType()) {
            return this.buildContainerSerializer(config, type, beanDesc, property, staticTyping);
         } else {
            Iterator i$ = this._factoryConfig.serializers().iterator();

            do {
               if (!i$.hasNext()) {
                  ser = this.findSerializerByLookup(type, config, beanDesc, property, staticTyping);
                  if (ser != null) {
                     return ser;
                  }

                  ser = this.findSerializerByPrimaryType(type, config, beanDesc, property, staticTyping);
                  if (ser != null) {
                     return ser;
                  }

                  ser = this.findBeanSerializer(config, type, beanDesc, property);
                  if (ser == null) {
                     ser = super.findSerializerByAddonType(config, type, beanDesc, property, staticTyping);
                  }

                  return ser;
               }

               Serializers serializers = (Serializers)i$.next();
               ser = serializers.findSerializer(config, type, beanDesc, property);
            } while(ser == null);

            return ser;
         }
      }
   }

   public JsonSerializer<Object> createKeySerializer(SerializationConfig config, JavaType type, BeanProperty property) {
      if (!this._factoryConfig.hasKeySerializers()) {
         return null;
      } else {
         BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspectClassAnnotations(type.getRawClass());
         JsonSerializer<?> ser = null;
         Iterator i$ = this._factoryConfig.keySerializers().iterator();

         while(i$.hasNext()) {
            Serializers serializers = (Serializers)i$.next();
            ser = serializers.findSerializer(config, type, beanDesc, property);
            if (ser != null) {
               break;
            }
         }

         return ser;
      }
   }

   public JsonSerializer<Object> findBeanSerializer(SerializationConfig config, JavaType type, BasicBeanDescription beanDesc, BeanProperty property) throws JsonMappingException {
      if (!this.isPotentialBeanType(type.getRawClass())) {
         return null;
      } else {
         JsonSerializer<Object> serializer = this.constructBeanSerializer(config, beanDesc, property);
         BeanSerializerModifier mod;
         if (this._factoryConfig.hasSerializerModifiers()) {
            for(Iterator i$ = this._factoryConfig.serializerModifiers().iterator(); i$.hasNext(); serializer = mod.modifySerializer(config, beanDesc, serializer)) {
               mod = (BeanSerializerModifier)i$.next();
            }
         }

         return serializer;
      }
   }

   public TypeSerializer findPropertyTypeSerializer(JavaType baseType, SerializationConfig config, AnnotatedMember accessor, BeanProperty property) throws JsonMappingException {
      AnnotationIntrospector ai = config.getAnnotationIntrospector();
      TypeResolverBuilder<?> b = ai.findPropertyTypeResolver(config, accessor, baseType);
      if (b == null) {
         return this.createTypeSerializer(config, baseType, property);
      } else {
         Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes((AnnotatedMember)accessor, config, ai);
         return b.buildTypeSerializer(config, baseType, subtypes, property);
      }
   }

   public TypeSerializer findPropertyContentTypeSerializer(JavaType containerType, SerializationConfig config, AnnotatedMember accessor, BeanProperty property) throws JsonMappingException {
      JavaType contentType = containerType.getContentType();
      AnnotationIntrospector ai = config.getAnnotationIntrospector();
      TypeResolverBuilder<?> b = ai.findPropertyContentTypeResolver(config, accessor, containerType);
      if (b == null) {
         return this.createTypeSerializer(config, contentType, property);
      } else {
         Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes((AnnotatedMember)accessor, config, ai);
         return b.buildTypeSerializer(config, contentType, subtypes, property);
      }
   }

   protected JsonSerializer<Object> constructBeanSerializer(SerializationConfig config, BasicBeanDescription beanDesc, BeanProperty property) throws JsonMappingException {
      if (beanDesc.getBeanClass() == Object.class) {
         throw new IllegalArgumentException("Can not create bean serializer for Object.class");
      } else {
         BeanSerializerBuilder builder = this.constructBeanSerializerBuilder(beanDesc);
         List<BeanPropertyWriter> props = this.findBeanProperties(config, beanDesc);
         AnnotatedMethod anyGetter = beanDesc.findAnyGetter();
         Iterator i$;
         BeanSerializerModifier mod;
         if (this._factoryConfig.hasSerializerModifiers()) {
            if (props == null) {
               props = new ArrayList();
            }

            for(i$ = this._factoryConfig.serializerModifiers().iterator(); i$.hasNext(); props = mod.changeProperties(config, beanDesc, (List)props)) {
               mod = (BeanSerializerModifier)i$.next();
            }
         }

         List props;
         if (props != null && ((List)props).size() != 0) {
            props = this.filterBeanProperties(config, beanDesc, (List)props);
            props = this.sortBeanProperties(config, beanDesc, props);
         } else {
            if (anyGetter == null) {
               if (beanDesc.hasKnownClassAnnotations()) {
                  return builder.createDummy();
               }

               return null;
            }

            props = Collections.emptyList();
         }

         if (this._factoryConfig.hasSerializerModifiers()) {
            for(i$ = this._factoryConfig.serializerModifiers().iterator(); i$.hasNext(); props = mod.orderProperties(config, beanDesc, props)) {
               mod = (BeanSerializerModifier)i$.next();
            }
         }

         builder.setProperties(props);
         builder.setFilterId(this.findFilterId(config, beanDesc));
         if (anyGetter != null) {
            JavaType type = anyGetter.getType(beanDesc.bindingsForBeanType());
            boolean staticTyping = config.isEnabled(SerializationConfig.Feature.USE_STATIC_TYPING);
            JavaType valueType = type.getContentType();
            TypeSerializer typeSer = this.createTypeSerializer(config, valueType, property);
            MapSerializer mapSer = MapSerializer.construct((String[])null, type, staticTyping, typeSer, property, (JsonSerializer)null, (JsonSerializer)null);
            builder.setAnyGetter(new AnyGetterWriter(anyGetter, mapSer));
         }

         this.processViews(config, builder);
         if (this._factoryConfig.hasSerializerModifiers()) {
            for(i$ = this._factoryConfig.serializerModifiers().iterator(); i$.hasNext(); builder = mod.updateBuilder(config, beanDesc, builder)) {
               mod = (BeanSerializerModifier)i$.next();
            }
         }

         return builder.build();
      }
   }

   protected BeanPropertyWriter constructFilteredBeanWriter(BeanPropertyWriter writer, Class<?>[] inViews) {
      return FilteredBeanPropertyWriter.constructViewBased(writer, inViews);
   }

   protected PropertyBuilder constructPropertyBuilder(SerializationConfig config, BasicBeanDescription beanDesc) {
      return new PropertyBuilder(config, beanDesc);
   }

   protected BeanSerializerBuilder constructBeanSerializerBuilder(BasicBeanDescription beanDesc) {
      return new BeanSerializerBuilder(beanDesc);
   }

   protected Object findFilterId(SerializationConfig config, BasicBeanDescription beanDesc) {
      return config.getAnnotationIntrospector().findFilterId(beanDesc.getClassInfo());
   }

   protected boolean isPotentialBeanType(Class<?> type) {
      return ClassUtil.canBeABeanType(type) == null && !ClassUtil.isProxyType(type);
   }

   protected List<BeanPropertyWriter> findBeanProperties(SerializationConfig config, BasicBeanDescription beanDesc) throws JsonMappingException {
      VisibilityChecker<?> vchecker = config.getDefaultVisibilityChecker();
      if (!config.isEnabled(SerializationConfig.Feature.AUTO_DETECT_GETTERS)) {
         vchecker = vchecker.withGetterVisibility(JsonAutoDetect.Visibility.NONE);
      }

      if (!config.isEnabled(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS)) {
         vchecker = vchecker.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE);
      }

      if (!config.isEnabled(SerializationConfig.Feature.AUTO_DETECT_FIELDS)) {
         vchecker = vchecker.withFieldVisibility(JsonAutoDetect.Visibility.NONE);
      }

      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      vchecker = intr.findAutoDetectVisibility(beanDesc.getClassInfo(), vchecker);
      LinkedHashMap<String, AnnotatedMethod> methodsByProp = beanDesc.findGetters(vchecker, (Collection)null);
      LinkedHashMap<String, AnnotatedField> fieldsByProp = beanDesc.findSerializableFields(vchecker, methodsByProp.keySet());
      this.removeIgnorableTypes(config, beanDesc, methodsByProp);
      this.removeIgnorableTypes(config, beanDesc, fieldsByProp);
      if (methodsByProp.isEmpty() && fieldsByProp.isEmpty()) {
         return null;
      } else {
         boolean staticTyping = this.usesStaticTyping(config, beanDesc, (TypeSerializer)null, (BeanProperty)null);
         PropertyBuilder pb = this.constructPropertyBuilder(config, beanDesc);
         ArrayList<BeanPropertyWriter> props = new ArrayList(methodsByProp.size());
         TypeBindings typeBind = beanDesc.bindingsForBeanType();
         Iterator i$ = fieldsByProp.entrySet().iterator();

         while(true) {
            Entry en;
            AnnotationIntrospector.ReferenceProperty prop;
            do {
               if (!i$.hasNext()) {
                  i$ = methodsByProp.entrySet().iterator();

                  while(true) {
                     do {
                        if (!i$.hasNext()) {
                           return props;
                        }

                        en = (Entry)i$.next();
                        prop = intr.findReferenceType((AnnotatedMember)en.getValue());
                     } while(prop != null && prop.isBackReference());

                     props.add(this._constructWriter(config, typeBind, pb, staticTyping, (String)en.getKey(), (AnnotatedMember)en.getValue()));
                  }
               }

               en = (Entry)i$.next();
               prop = intr.findReferenceType((AnnotatedMember)en.getValue());
            } while(prop != null && prop.isBackReference());

            props.add(this._constructWriter(config, typeBind, pb, staticTyping, (String)en.getKey(), (AnnotatedMember)en.getValue()));
         }
      }
   }

   protected List<BeanPropertyWriter> filterBeanProperties(SerializationConfig config, BasicBeanDescription beanDesc, List<BeanPropertyWriter> props) {
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      AnnotatedClass ac = beanDesc.getClassInfo();
      String[] ignored = intr.findPropertiesToIgnore(ac);
      if (ignored != null && ignored.length > 0) {
         HashSet<String> ignoredSet = ArrayBuilders.arrayToSet(ignored);
         Iterator it = props.iterator();

         while(it.hasNext()) {
            if (ignoredSet.contains(((BeanPropertyWriter)it.next()).getName())) {
               it.remove();
            }
         }
      }

      return props;
   }

   protected List<BeanPropertyWriter> sortBeanProperties(SerializationConfig config, BasicBeanDescription beanDesc, List<BeanPropertyWriter> props) {
      List<String> creatorProps = beanDesc.findCreatorPropertyNames();
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      AnnotatedClass ac = beanDesc.getClassInfo();
      String[] propOrder = intr.findSerializationPropertyOrder(ac);
      Boolean alpha = intr.findSerializationSortAlphabetically(ac);
      boolean sort;
      if (alpha == null) {
         sort = config.isEnabled(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY);
      } else {
         sort = alpha;
      }

      if (sort || !creatorProps.isEmpty() || propOrder != null) {
         props = this._sortBeanProperties(props, creatorProps, propOrder, sort);
      }

      return props;
   }

   protected void processViews(SerializationConfig config, BeanSerializerBuilder builder) {
      List<BeanPropertyWriter> props = builder.getProperties();
      boolean includeByDefault = config.isEnabled(SerializationConfig.Feature.DEFAULT_VIEW_INCLUSION);
      int propCount = props.size();
      int viewsFound = 0;
      BeanPropertyWriter[] filtered = new BeanPropertyWriter[propCount];

      for(int i = 0; i < propCount; ++i) {
         BeanPropertyWriter bpw = (BeanPropertyWriter)props.get(i);
         Class<?>[] views = bpw.getViews();
         if (views == null) {
            if (includeByDefault) {
               filtered[i] = bpw;
            }
         } else {
            ++viewsFound;
            filtered[i] = this.constructFilteredBeanWriter(bpw, views);
         }
      }

      if (!includeByDefault || viewsFound != 0) {
         builder.setFilteredProperties(filtered);
      }
   }

   protected <T extends AnnotatedMember> void removeIgnorableTypes(SerializationConfig config, BasicBeanDescription beanDesc, Map<String, T> props) {
      if (!props.isEmpty()) {
         AnnotationIntrospector intr = config.getAnnotationIntrospector();
         Iterator<Entry<String, T>> it = props.entrySet().iterator();
         HashMap ignores = new HashMap();

         while(it.hasNext()) {
            Entry<String, T> entry = (Entry)it.next();
            Class<?> type = ((AnnotatedMember)entry.getValue()).getRawType();
            Boolean result = (Boolean)ignores.get(type);
            if (result == null) {
               BasicBeanDescription desc = (BasicBeanDescription)config.introspectClassAnnotations(type);
               AnnotatedClass ac = desc.getClassInfo();
               result = intr.isIgnorableType(ac);
               if (result == null) {
                  result = Boolean.FALSE;
               }

               ignores.put(type, result);
            }

            if (result) {
               it.remove();
            }
         }

      }
   }

   protected BeanPropertyWriter _constructWriter(SerializationConfig config, TypeBindings typeContext, PropertyBuilder pb, boolean staticTyping, String name, AnnotatedMember accessor) throws JsonMappingException {
      if (config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
         accessor.fixAccess();
      }

      JavaType type = accessor.getType(typeContext);
      BeanProperty.Std property = new BeanProperty.Std(name, type, pb.getClassAnnotations(), accessor);
      JsonSerializer<Object> annotatedSerializer = this.findSerializerFromAnnotation(config, accessor, property);
      TypeSerializer contentTypeSer = null;
      if (ClassUtil.isCollectionMapOrArray(type.getRawClass())) {
         contentTypeSer = this.findPropertyContentTypeSerializer(type, config, accessor, property);
      }

      TypeSerializer typeSer = this.findPropertyTypeSerializer(type, config, accessor, property);
      BeanPropertyWriter pbw = pb.buildWriter(name, type, annotatedSerializer, typeSer, contentTypeSer, accessor, staticTyping);
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      pbw.setViews(intr.findSerializationViews(accessor));
      return pbw;
   }

   protected List<BeanPropertyWriter> _sortBeanProperties(List<BeanPropertyWriter> props, List<String> creatorProps, String[] propertyOrder, boolean sort) {
      int size = props.size();
      Object all;
      if (sort) {
         all = new TreeMap();
      } else {
         all = new LinkedHashMap(size * 2);
      }

      Iterator i$ = props.iterator();

      while(i$.hasNext()) {
         BeanPropertyWriter w = (BeanPropertyWriter)i$.next();
         ((Map)all).put(w.getName(), w);
      }

      Map<String, BeanPropertyWriter> ordered = new LinkedHashMap(size * 2);
      if (propertyOrder != null) {
         String[] arr$ = propertyOrder;
         int len$ = propertyOrder.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String name = arr$[i$];
            BeanPropertyWriter w = (BeanPropertyWriter)((Map)all).get(name);
            if (w != null) {
               ordered.put(name, w);
            }
         }
      }

      Iterator i$ = creatorProps.iterator();

      while(i$.hasNext()) {
         String name = (String)i$.next();
         BeanPropertyWriter w = (BeanPropertyWriter)((Map)all).get(name);
         if (w != null) {
            ordered.put(name, w);
         }
      }

      ordered.putAll((Map)all);
      return new ArrayList(ordered.values());
   }

   public static class ConfigImpl extends SerializerFactory.Config {
      protected static final Serializers[] NO_SERIALIZERS = new Serializers[0];
      protected static final BeanSerializerModifier[] NO_MODIFIERS = new BeanSerializerModifier[0];
      protected final Serializers[] _additionalSerializers;
      protected final Serializers[] _additionalKeySerializers;
      protected final BeanSerializerModifier[] _modifiers;

      public ConfigImpl() {
         this((Serializers[])null, (Serializers[])null, (BeanSerializerModifier[])null);
      }

      protected ConfigImpl(Serializers[] allAdditionalSerializers, Serializers[] allAdditionalKeySerializers, BeanSerializerModifier[] modifiers) {
         this._additionalSerializers = allAdditionalSerializers == null ? NO_SERIALIZERS : allAdditionalSerializers;
         this._additionalKeySerializers = allAdditionalKeySerializers == null ? NO_SERIALIZERS : allAdditionalKeySerializers;
         this._modifiers = modifiers == null ? NO_MODIFIERS : modifiers;
      }

      public SerializerFactory.Config withAdditionalSerializers(Serializers additional) {
         if (additional == null) {
            throw new IllegalArgumentException("Can not pass null Serializers");
         } else {
            Serializers[] all = (Serializers[])ArrayBuilders.insertInListNoDup(this._additionalSerializers, additional);
            return new BeanSerializerFactory.ConfigImpl(all, this._additionalKeySerializers, this._modifiers);
         }
      }

      public SerializerFactory.Config withAdditionalKeySerializers(Serializers additional) {
         if (additional == null) {
            throw new IllegalArgumentException("Can not pass null Serializers");
         } else {
            Serializers[] all = (Serializers[])ArrayBuilders.insertInListNoDup(this._additionalKeySerializers, additional);
            return new BeanSerializerFactory.ConfigImpl(this._additionalSerializers, all, this._modifiers);
         }
      }

      public SerializerFactory.Config withSerializerModifier(BeanSerializerModifier modifier) {
         if (modifier == null) {
            throw new IllegalArgumentException("Can not pass null modifier");
         } else {
            BeanSerializerModifier[] modifiers = (BeanSerializerModifier[])ArrayBuilders.insertInListNoDup(this._modifiers, modifier);
            return new BeanSerializerFactory.ConfigImpl(this._additionalSerializers, this._additionalKeySerializers, modifiers);
         }
      }

      public boolean hasSerializers() {
         return this._additionalSerializers.length > 0;
      }

      public boolean hasKeySerializers() {
         return this._additionalKeySerializers.length > 0;
      }

      public boolean hasSerializerModifiers() {
         return this._modifiers.length > 0;
      }

      public Iterable<Serializers> serializers() {
         return ArrayBuilders.arrayAsIterable(this._additionalSerializers);
      }

      public Iterable<Serializers> keySerializers() {
         return ArrayBuilders.arrayAsIterable(this._additionalKeySerializers);
      }

      public Iterable<BeanSerializerModifier> serializerModifiers() {
         return ArrayBuilders.arrayAsIterable(this._modifiers);
      }
   }
}
