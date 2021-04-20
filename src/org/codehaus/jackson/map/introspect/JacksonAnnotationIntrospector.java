package org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JacksonAnnotation;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonClass;
import org.codehaus.jackson.annotate.JsonContentClass;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonGetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonIgnoreType;
import org.codehaus.jackson.annotate.JsonKeyClass;
import org.codehaus.jackson.annotate.JsonManagedReference;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonRawValue;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonTypeIdResolver;
import org.codehaus.jackson.map.annotate.JsonTypeResolver;
import org.codehaus.jackson.map.annotate.JsonView;
import org.codehaus.jackson.map.annotate.NoClass;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.jsontype.impl.StdTypeResolverBuilder;
import org.codehaus.jackson.map.ser.impl.RawSerializer;
import org.codehaus.jackson.type.JavaType;

public class JacksonAnnotationIntrospector extends AnnotationIntrospector {
   public JacksonAnnotationIntrospector() {
   }

   public boolean isHandled(Annotation ann) {
      Class<? extends Annotation> acls = ann.annotationType();
      return acls.getAnnotation(JacksonAnnotation.class) != null;
   }

   public String findEnumValue(Enum<?> value) {
      return value.name();
   }

   public Boolean findCachability(AnnotatedClass ac) {
      JsonCachable ann = (JsonCachable)ac.getAnnotation(JsonCachable.class);
      if (ann == null) {
         return null;
      } else {
         return ann.value() ? Boolean.TRUE : Boolean.FALSE;
      }
   }

   public String findRootName(AnnotatedClass ac) {
      return null;
   }

   public String[] findPropertiesToIgnore(AnnotatedClass ac) {
      JsonIgnoreProperties ignore = (JsonIgnoreProperties)ac.getAnnotation(JsonIgnoreProperties.class);
      return ignore == null ? null : ignore.value();
   }

   public Boolean findIgnoreUnknownProperties(AnnotatedClass ac) {
      JsonIgnoreProperties ignore = (JsonIgnoreProperties)ac.getAnnotation(JsonIgnoreProperties.class);
      return ignore == null ? null : ignore.ignoreUnknown();
   }

   public Boolean isIgnorableType(AnnotatedClass ac) {
      JsonIgnoreType ignore = (JsonIgnoreType)ac.getAnnotation(JsonIgnoreType.class);
      return ignore == null ? null : ignore.value();
   }

   public Object findFilterId(AnnotatedClass ac) {
      JsonFilter ann = (JsonFilter)ac.getAnnotation(JsonFilter.class);
      if (ann != null) {
         String id = ann.value();
         if (id.length() > 0) {
            return id;
         }
      }

      return null;
   }

   public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass ac, VisibilityChecker<?> checker) {
      JsonAutoDetect ann = (JsonAutoDetect)ac.getAnnotation(JsonAutoDetect.class);
      return ann == null ? checker : checker.with(ann);
   }

   public AnnotationIntrospector.ReferenceProperty findReferenceType(AnnotatedMember member) {
      JsonManagedReference ref1 = (JsonManagedReference)member.getAnnotation(JsonManagedReference.class);
      if (ref1 != null) {
         return AnnotationIntrospector.ReferenceProperty.managed(ref1.value());
      } else {
         JsonBackReference ref2 = (JsonBackReference)member.getAnnotation(JsonBackReference.class);
         return ref2 != null ? AnnotationIntrospector.ReferenceProperty.back(ref2.value()) : null;
      }
   }

   public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
      return this._findTypeResolver(config, ac, baseType);
   }

   public TypeResolverBuilder<?> findPropertyTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType baseType) {
      return baseType.isContainerType() ? null : this._findTypeResolver(config, am, baseType);
   }

   public TypeResolverBuilder<?> findPropertyContentTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType containerType) {
      if (!containerType.isContainerType()) {
         throw new IllegalArgumentException("Must call method with a container type (got " + containerType + ")");
      } else {
         return this._findTypeResolver(config, am, containerType);
      }
   }

   public List<NamedType> findSubtypes(Annotated a) {
      JsonSubTypes t = (JsonSubTypes)a.getAnnotation(JsonSubTypes.class);
      if (t == null) {
         return null;
      } else {
         JsonSubTypes.Type[] types = t.value();
         ArrayList<NamedType> result = new ArrayList(types.length);
         JsonSubTypes.Type[] arr$ = types;
         int len$ = types.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            JsonSubTypes.Type type = arr$[i$];
            result.add(new NamedType(type.value(), type.name()));
         }

         return result;
      }
   }

   public String findTypeName(AnnotatedClass ac) {
      JsonTypeName tn = (JsonTypeName)ac.getAnnotation(JsonTypeName.class);
      return tn == null ? null : tn.value();
   }

   public boolean isIgnorableMethod(AnnotatedMethod m) {
      return this._isIgnorable(m);
   }

   public boolean isIgnorableConstructor(AnnotatedConstructor c) {
      return this._isIgnorable(c);
   }

   public boolean isIgnorableField(AnnotatedField f) {
      return this._isIgnorable(f);
   }

   public Object findSerializer(Annotated a, BeanProperty property) {
      JsonSerialize ann = (JsonSerialize)a.getAnnotation(JsonSerialize.class);
      if (ann != null) {
         Class<? extends JsonSerializer<?>> serClass = ann.using();
         if (serClass != JsonSerializer.None.class) {
            return serClass;
         }
      }

      JsonRawValue annRaw = (JsonRawValue)a.getAnnotation(JsonRawValue.class);
      if (annRaw != null && annRaw.value()) {
         Class<?> cls = a.getRawType();
         return new RawSerializer(cls);
      } else {
         return null;
      }
   }

   public Class<? extends JsonSerializer<?>> findKeySerializer(Annotated a) {
      JsonSerialize ann = (JsonSerialize)a.getAnnotation(JsonSerialize.class);
      if (ann != null) {
         Class<? extends JsonSerializer<?>> serClass = ann.keyUsing();
         if (serClass != JsonSerializer.None.class) {
            return serClass;
         }
      }

      return null;
   }

   public Class<? extends JsonSerializer<?>> findContentSerializer(Annotated a) {
      JsonSerialize ann = (JsonSerialize)a.getAnnotation(JsonSerialize.class);
      if (ann != null) {
         Class<? extends JsonSerializer<?>> serClass = ann.contentUsing();
         if (serClass != JsonSerializer.None.class) {
            return serClass;
         }
      }

      return null;
   }

   public JsonSerialize.Inclusion findSerializationInclusion(Annotated a, JsonSerialize.Inclusion defValue) {
      JsonSerialize ann = (JsonSerialize)a.getAnnotation(JsonSerialize.class);
      if (ann != null) {
         return ann.include();
      } else {
         JsonWriteNullProperties oldAnn = (JsonWriteNullProperties)a.getAnnotation(JsonWriteNullProperties.class);
         if (oldAnn != null) {
            boolean writeNulls = oldAnn.value();
            return writeNulls ? JsonSerialize.Inclusion.ALWAYS : JsonSerialize.Inclusion.NON_NULL;
         } else {
            return defValue;
         }
      }
   }

   public Class<?> findSerializationType(Annotated am) {
      JsonSerialize ann = (JsonSerialize)am.getAnnotation(JsonSerialize.class);
      if (ann != null) {
         Class<?> cls = ann.as();
         if (cls != NoClass.class) {
            return cls;
         }
      }

      return null;
   }

   public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
      JsonSerialize ann = (JsonSerialize)am.getAnnotation(JsonSerialize.class);
      if (ann != null) {
         Class<?> cls = ann.keyAs();
         if (cls != NoClass.class) {
            return cls;
         }
      }

      return null;
   }

   public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
      JsonSerialize ann = (JsonSerialize)am.getAnnotation(JsonSerialize.class);
      if (ann != null) {
         Class<?> cls = ann.contentAs();
         if (cls != NoClass.class) {
            return cls;
         }
      }

      return null;
   }

   public JsonSerialize.Typing findSerializationTyping(Annotated a) {
      JsonSerialize ann = (JsonSerialize)a.getAnnotation(JsonSerialize.class);
      return ann == null ? null : ann.typing();
   }

   public Class<?>[] findSerializationViews(Annotated a) {
      JsonView ann = (JsonView)a.getAnnotation(JsonView.class);
      return ann == null ? null : ann.value();
   }

   public String[] findSerializationPropertyOrder(AnnotatedClass ac) {
      JsonPropertyOrder order = (JsonPropertyOrder)ac.getAnnotation(JsonPropertyOrder.class);
      return order == null ? null : order.value();
   }

   public Boolean findSerializationSortAlphabetically(AnnotatedClass ac) {
      JsonPropertyOrder order = (JsonPropertyOrder)ac.getAnnotation(JsonPropertyOrder.class);
      return order == null ? null : order.alphabetic();
   }

   public String findGettablePropertyName(AnnotatedMethod am) {
      JsonProperty pann = (JsonProperty)am.getAnnotation(JsonProperty.class);
      if (pann != null) {
         return pann.value();
      } else {
         JsonGetter ann = (JsonGetter)am.getAnnotation(JsonGetter.class);
         if (ann != null) {
            return ann.value();
         } else {
            return !am.hasAnnotation(JsonSerialize.class) && !am.hasAnnotation(JsonView.class) ? null : "";
         }
      }
   }

   public boolean hasAsValueAnnotation(AnnotatedMethod am) {
      JsonValue ann = (JsonValue)am.getAnnotation(JsonValue.class);
      return ann != null && ann.value();
   }

   public String findSerializablePropertyName(AnnotatedField af) {
      JsonProperty pann = (JsonProperty)af.getAnnotation(JsonProperty.class);
      if (pann != null) {
         return pann.value();
      } else {
         return !af.hasAnnotation(JsonSerialize.class) && !af.hasAnnotation(JsonView.class) ? null : "";
      }
   }

   public Class<? extends JsonDeserializer<?>> findDeserializer(Annotated a, BeanProperty property) {
      JsonDeserialize ann = (JsonDeserialize)a.getAnnotation(JsonDeserialize.class);
      if (ann != null) {
         Class<? extends JsonDeserializer<?>> deserClass = ann.using();
         if (deserClass != JsonDeserializer.None.class) {
            return deserClass;
         }
      }

      return null;
   }

   public Class<? extends KeyDeserializer> findKeyDeserializer(Annotated a) {
      JsonDeserialize ann = (JsonDeserialize)a.getAnnotation(JsonDeserialize.class);
      if (ann != null) {
         Class<? extends KeyDeserializer> deserClass = ann.keyUsing();
         if (deserClass != KeyDeserializer.None.class) {
            return deserClass;
         }
      }

      return null;
   }

   public Class<? extends JsonDeserializer<?>> findContentDeserializer(Annotated a) {
      JsonDeserialize ann = (JsonDeserialize)a.getAnnotation(JsonDeserialize.class);
      if (ann != null) {
         Class<? extends JsonDeserializer<?>> deserClass = ann.contentUsing();
         if (deserClass != JsonDeserializer.None.class) {
            return deserClass;
         }
      }

      return null;
   }

   public Class<?> findDeserializationType(Annotated am, JavaType baseType, String propName) {
      JsonDeserialize ann = (JsonDeserialize)am.getAnnotation(JsonDeserialize.class);
      if (ann != null) {
         Class<?> cls = ann.as();
         if (cls != NoClass.class) {
            return cls;
         }
      }

      JsonClass oldAnn = (JsonClass)am.getAnnotation(JsonClass.class);
      if (oldAnn != null) {
         Class<?> cls = oldAnn.value();
         if (cls != NoClass.class) {
            return cls;
         }
      }

      return null;
   }

   public Class<?> findDeserializationKeyType(Annotated am, JavaType baseKeyType, String propName) {
      JsonDeserialize ann = (JsonDeserialize)am.getAnnotation(JsonDeserialize.class);
      if (ann != null) {
         Class<?> cls = ann.keyAs();
         if (cls != NoClass.class) {
            return cls;
         }
      }

      JsonKeyClass oldAnn = (JsonKeyClass)am.getAnnotation(JsonKeyClass.class);
      if (oldAnn != null) {
         Class<?> cls = oldAnn.value();
         if (cls != NoClass.class) {
            return cls;
         }
      }

      return null;
   }

   public Class<?> findDeserializationContentType(Annotated am, JavaType baseContentType, String propName) {
      JsonDeserialize ann = (JsonDeserialize)am.getAnnotation(JsonDeserialize.class);
      if (ann != null) {
         Class<?> cls = ann.contentAs();
         if (cls != NoClass.class) {
            return cls;
         }
      }

      JsonContentClass oldAnn = (JsonContentClass)am.getAnnotation(JsonContentClass.class);
      if (oldAnn != null) {
         Class<?> cls = oldAnn.value();
         if (cls != NoClass.class) {
            return cls;
         }
      }

      return null;
   }

   public String findSettablePropertyName(AnnotatedMethod am) {
      JsonProperty pann = (JsonProperty)am.getAnnotation(JsonProperty.class);
      if (pann != null) {
         return pann.value();
      } else {
         JsonSetter ann = (JsonSetter)am.getAnnotation(JsonSetter.class);
         if (ann != null) {
            return ann.value();
         } else {
            return !am.hasAnnotation(JsonDeserialize.class) && !am.hasAnnotation(JsonView.class) ? null : "";
         }
      }
   }

   public boolean hasAnySetterAnnotation(AnnotatedMethod am) {
      return am.hasAnnotation(JsonAnySetter.class);
   }

   public boolean hasAnyGetterAnnotation(AnnotatedMethod am) {
      return am.hasAnnotation(JsonAnyGetter.class);
   }

   public boolean hasCreatorAnnotation(Annotated a) {
      return a.hasAnnotation(JsonCreator.class);
   }

   public String findDeserializablePropertyName(AnnotatedField af) {
      JsonProperty pann = (JsonProperty)af.getAnnotation(JsonProperty.class);
      if (pann != null) {
         return pann.value();
      } else {
         return !af.hasAnnotation(JsonDeserialize.class) && !af.hasAnnotation(JsonView.class) ? null : "";
      }
   }

   public String findPropertyNameForParam(AnnotatedParameter param) {
      if (param != null) {
         JsonProperty pann = (JsonProperty)param.getAnnotation(JsonProperty.class);
         if (pann != null) {
            return pann.value();
         }
      }

      return null;
   }

   protected boolean _isIgnorable(Annotated a) {
      JsonIgnore ann = (JsonIgnore)a.getAnnotation(JsonIgnore.class);
      return ann != null && ann.value();
   }

   protected TypeResolverBuilder<?> _findTypeResolver(MapperConfig<?> config, Annotated ann, JavaType baseType) {
      JsonTypeInfo info = (JsonTypeInfo)ann.getAnnotation(JsonTypeInfo.class);
      JsonTypeResolver resAnn = (JsonTypeResolver)ann.getAnnotation(JsonTypeResolver.class);
      Object b;
      if (resAnn != null) {
         if (info == null) {
            return null;
         }

         b = config.typeResolverBuilderInstance(ann, resAnn.value());
      } else {
         if (info == null || info.use() == JsonTypeInfo.Id.NONE) {
            return null;
         }

         b = this._constructStdTypeResolverBuilder();
      }

      JsonTypeIdResolver idResInfo = (JsonTypeIdResolver)ann.getAnnotation(JsonTypeIdResolver.class);
      TypeIdResolver idRes = idResInfo == null ? null : config.typeIdResolverInstance(ann, idResInfo.value());
      if (idRes != null) {
         idRes.init(baseType);
      }

      TypeResolverBuilder<?> b = ((TypeResolverBuilder)b).init(info.use(), idRes);
      b = b.inclusion(info.include());
      b = b.typeProperty(info.property());
      return b;
   }

   protected StdTypeResolverBuilder _constructStdTypeResolverBuilder() {
      return new StdTypeResolverBuilder();
   }
}
