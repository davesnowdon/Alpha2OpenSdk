package org.codehaus.jackson.map;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.introspect.NopAnnotationIntrospector;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.type.JavaType;

public abstract class AnnotationIntrospector {
   public AnnotationIntrospector() {
   }

   public static AnnotationIntrospector nopInstance() {
      return NopAnnotationIntrospector.instance;
   }

   public static AnnotationIntrospector pair(AnnotationIntrospector a1, AnnotationIntrospector a2) {
      return new AnnotationIntrospector.Pair(a1, a2);
   }

   public Collection<AnnotationIntrospector> allIntrospectors() {
      return Collections.singletonList(this);
   }

   public Collection<AnnotationIntrospector> allIntrospectors(Collection<AnnotationIntrospector> result) {
      result.add(this);
      return result;
   }

   public abstract boolean isHandled(Annotation var1);

   public abstract Boolean findCachability(AnnotatedClass var1);

   public abstract String findRootName(AnnotatedClass var1);

   public abstract String[] findPropertiesToIgnore(AnnotatedClass var1);

   public abstract Boolean findIgnoreUnknownProperties(AnnotatedClass var1);

   public Boolean isIgnorableType(AnnotatedClass ac) {
      return null;
   }

   public Object findFilterId(AnnotatedClass ac) {
      return null;
   }

   public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass ac, VisibilityChecker<?> checker) {
      return checker;
   }

   public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
      return null;
   }

   public TypeResolverBuilder<?> findPropertyTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType baseType) {
      return null;
   }

   public TypeResolverBuilder<?> findPropertyContentTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType containerType) {
      return null;
   }

   public List<NamedType> findSubtypes(Annotated a) {
      return null;
   }

   public String findTypeName(AnnotatedClass ac) {
      return null;
   }

   public AnnotationIntrospector.ReferenceProperty findReferenceType(AnnotatedMember member) {
      return null;
   }

   public abstract boolean isIgnorableMethod(AnnotatedMethod var1);

   public abstract boolean isIgnorableConstructor(AnnotatedConstructor var1);

   public abstract boolean isIgnorableField(AnnotatedField var1);

   public Object findSerializer(Annotated am) {
      return this.findSerializer(am, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public Object findSerializer(Annotated am, BeanProperty property) {
      return property != null ? this.findSerializer(am) : null;
   }

   public Class<? extends JsonSerializer<?>> findKeySerializer(Annotated am) {
      return null;
   }

   public Class<? extends JsonSerializer<?>> findContentSerializer(Annotated am) {
      return null;
   }

   public JsonSerialize.Inclusion findSerializationInclusion(Annotated a, JsonSerialize.Inclusion defValue) {
      return defValue;
   }

   public abstract Class<?> findSerializationType(Annotated var1);

   public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
      return null;
   }

   public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
      return null;
   }

   public abstract JsonSerialize.Typing findSerializationTyping(Annotated var1);

   public abstract Class<?>[] findSerializationViews(Annotated var1);

   public abstract String[] findSerializationPropertyOrder(AnnotatedClass var1);

   public abstract Boolean findSerializationSortAlphabetically(AnnotatedClass var1);

   public abstract String findGettablePropertyName(AnnotatedMethod var1);

   public abstract boolean hasAsValueAnnotation(AnnotatedMethod var1);

   public abstract String findEnumValue(Enum<?> var1);

   public abstract String findSerializablePropertyName(AnnotatedField var1);

   public Object findDeserializer(Annotated am) {
      return this.findDeserializer(am, (BeanProperty)null);
   }

   /** @deprecated */
   @Deprecated
   public Object findDeserializer(Annotated am, BeanProperty property) {
      return property != null ? this.findDeserializer(am) : null;
   }

   public abstract Class<? extends KeyDeserializer> findKeyDeserializer(Annotated var1);

   public abstract Class<? extends JsonDeserializer<?>> findContentDeserializer(Annotated var1);

   public abstract Class<?> findDeserializationType(Annotated var1, JavaType var2, String var3);

   public abstract Class<?> findDeserializationKeyType(Annotated var1, JavaType var2, String var3);

   public abstract Class<?> findDeserializationContentType(Annotated var1, JavaType var2, String var3);

   public abstract String findSettablePropertyName(AnnotatedMethod var1);

   public boolean hasAnySetterAnnotation(AnnotatedMethod am) {
      return false;
   }

   public boolean hasAnyGetterAnnotation(AnnotatedMethod am) {
      return false;
   }

   public boolean hasCreatorAnnotation(Annotated a) {
      return false;
   }

   public abstract String findDeserializablePropertyName(AnnotatedField var1);

   public abstract String findPropertyNameForParam(AnnotatedParameter var1);

   public static class Pair extends AnnotationIntrospector {
      protected final AnnotationIntrospector _primary;
      protected final AnnotationIntrospector _secondary;

      public Pair(AnnotationIntrospector p, AnnotationIntrospector s) {
         this._primary = p;
         this._secondary = s;
      }

      public static AnnotationIntrospector create(AnnotationIntrospector primary, AnnotationIntrospector secondary) {
         if (primary == null) {
            return secondary;
         } else {
            return (AnnotationIntrospector)(secondary == null ? primary : new AnnotationIntrospector.Pair(primary, secondary));
         }
      }

      public Collection<AnnotationIntrospector> allIntrospectors() {
         return this.allIntrospectors(new ArrayList());
      }

      public Collection<AnnotationIntrospector> allIntrospectors(Collection<AnnotationIntrospector> result) {
         this._primary.allIntrospectors(result);
         this._secondary.allIntrospectors(result);
         return result;
      }

      public boolean isHandled(Annotation ann) {
         return this._primary.isHandled(ann) || this._secondary.isHandled(ann);
      }

      public Boolean findCachability(AnnotatedClass ac) {
         Boolean result = this._primary.findCachability(ac);
         if (result == null) {
            result = this._secondary.findCachability(ac);
         }

         return result;
      }

      public String findRootName(AnnotatedClass ac) {
         String name1 = this._primary.findRootName(ac);
         if (name1 == null) {
            return this._secondary.findRootName(ac);
         } else if (name1.length() > 0) {
            return name1;
         } else {
            String name2 = this._secondary.findRootName(ac);
            return name2 == null ? name1 : name2;
         }
      }

      public String[] findPropertiesToIgnore(AnnotatedClass ac) {
         String[] result = this._primary.findPropertiesToIgnore(ac);
         if (result == null) {
            result = this._secondary.findPropertiesToIgnore(ac);
         }

         return result;
      }

      public Boolean findIgnoreUnknownProperties(AnnotatedClass ac) {
         Boolean result = this._primary.findIgnoreUnknownProperties(ac);
         if (result == null) {
            result = this._secondary.findIgnoreUnknownProperties(ac);
         }

         return result;
      }

      public Boolean isIgnorableType(AnnotatedClass ac) {
         Boolean result = this._primary.isIgnorableType(ac);
         if (result == null) {
            result = this._secondary.isIgnorableType(ac);
         }

         return result;
      }

      public Object findFilterId(AnnotatedClass ac) {
         Object id = this._primary.findFilterId(ac);
         if (id == null) {
            id = this._secondary.findFilterId(ac);
         }

         return id;
      }

      public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass ac, VisibilityChecker<?> checker) {
         checker = this._secondary.findAutoDetectVisibility(ac, checker);
         return this._primary.findAutoDetectVisibility(ac, checker);
      }

      public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
         TypeResolverBuilder<?> b = this._primary.findTypeResolver(config, ac, baseType);
         if (b == null) {
            b = this._secondary.findTypeResolver(config, ac, baseType);
         }

         return b;
      }

      public TypeResolverBuilder<?> findPropertyTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType baseType) {
         TypeResolverBuilder<?> b = this._primary.findPropertyTypeResolver(config, am, baseType);
         if (b == null) {
            b = this._secondary.findPropertyTypeResolver(config, am, baseType);
         }

         return b;
      }

      public TypeResolverBuilder<?> findPropertyContentTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType baseType) {
         TypeResolverBuilder<?> b = this._primary.findPropertyContentTypeResolver(config, am, baseType);
         if (b == null) {
            b = this._secondary.findPropertyContentTypeResolver(config, am, baseType);
         }

         return b;
      }

      public List<NamedType> findSubtypes(Annotated a) {
         List<NamedType> types1 = this._primary.findSubtypes(a);
         List<NamedType> types2 = this._secondary.findSubtypes(a);
         if (types1 != null && !types1.isEmpty()) {
            if (types2 != null && !types2.isEmpty()) {
               ArrayList<NamedType> result = new ArrayList(types1.size() + types2.size());
               result.addAll(types1);
               result.addAll(types2);
               return result;
            } else {
               return types1;
            }
         } else {
            return types2;
         }
      }

      public String findTypeName(AnnotatedClass ac) {
         String name = this._primary.findTypeName(ac);
         if (name == null || name.length() == 0) {
            name = this._secondary.findTypeName(ac);
         }

         return name;
      }

      public AnnotationIntrospector.ReferenceProperty findReferenceType(AnnotatedMember member) {
         AnnotationIntrospector.ReferenceProperty ref = this._primary.findReferenceType(member);
         if (ref == null) {
            ref = this._secondary.findReferenceType(member);
         }

         return ref;
      }

      public boolean isIgnorableMethod(AnnotatedMethod m) {
         return this._primary.isIgnorableMethod(m) || this._secondary.isIgnorableMethod(m);
      }

      public boolean isIgnorableConstructor(AnnotatedConstructor c) {
         return this._primary.isIgnorableConstructor(c) || this._secondary.isIgnorableConstructor(c);
      }

      public boolean isIgnorableField(AnnotatedField f) {
         return this._primary.isIgnorableField(f) || this._secondary.isIgnorableField(f);
      }

      public Object findSerializer(Annotated am, BeanProperty property) {
         Object result = this._primary.findSerializer(am, property);
         if (result == null) {
            result = this._secondary.findSerializer(am, property);
         }

         return result;
      }

      public Object findSerializer(Annotated am) {
         Object result = this._primary.findSerializer(am);
         if (result == null) {
            result = this._secondary.findSerializer(am);
         }

         return result;
      }

      public Class<? extends JsonSerializer<?>> findKeySerializer(Annotated a) {
         Class<? extends JsonSerializer<?>> result = this._primary.findKeySerializer(a);
         if (result == null || result == JsonSerializer.None.class) {
            result = this._secondary.findKeySerializer(a);
         }

         return result;
      }

      public Class<? extends JsonSerializer<?>> findContentSerializer(Annotated a) {
         Class<? extends JsonSerializer<?>> result = this._primary.findContentSerializer(a);
         if (result == null || result == JsonSerializer.None.class) {
            result = this._secondary.findContentSerializer(a);
         }

         return result;
      }

      public JsonSerialize.Inclusion findSerializationInclusion(Annotated a, JsonSerialize.Inclusion defValue) {
         defValue = this._secondary.findSerializationInclusion(a, defValue);
         defValue = this._primary.findSerializationInclusion(a, defValue);
         return defValue;
      }

      public Class<?> findSerializationType(Annotated a) {
         Class<?> result = this._primary.findSerializationType(a);
         if (result == null) {
            result = this._secondary.findSerializationType(a);
         }

         return result;
      }

      public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
         Class<?> result = this._primary.findSerializationKeyType(am, baseType);
         if (result == null) {
            result = this._secondary.findSerializationKeyType(am, baseType);
         }

         return result;
      }

      public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
         Class<?> result = this._primary.findSerializationContentType(am, baseType);
         if (result == null) {
            result = this._secondary.findSerializationContentType(am, baseType);
         }

         return result;
      }

      public JsonSerialize.Typing findSerializationTyping(Annotated a) {
         JsonSerialize.Typing result = this._primary.findSerializationTyping(a);
         if (result == null) {
            result = this._secondary.findSerializationTyping(a);
         }

         return result;
      }

      public Class<?>[] findSerializationViews(Annotated a) {
         Class<?>[] result = this._primary.findSerializationViews(a);
         if (result == null) {
            result = this._secondary.findSerializationViews(a);
         }

         return result;
      }

      public String[] findSerializationPropertyOrder(AnnotatedClass ac) {
         String[] result = this._primary.findSerializationPropertyOrder(ac);
         if (result == null) {
            result = this._secondary.findSerializationPropertyOrder(ac);
         }

         return result;
      }

      public Boolean findSerializationSortAlphabetically(AnnotatedClass ac) {
         Boolean result = this._primary.findSerializationSortAlphabetically(ac);
         if (result == null) {
            result = this._secondary.findSerializationSortAlphabetically(ac);
         }

         return result;
      }

      public String findGettablePropertyName(AnnotatedMethod am) {
         String result = this._primary.findGettablePropertyName(am);
         if (result == null) {
            result = this._secondary.findGettablePropertyName(am);
         } else if (result.length() == 0) {
            String str2 = this._secondary.findGettablePropertyName(am);
            if (str2 != null) {
               result = str2;
            }
         }

         return result;
      }

      public boolean hasAsValueAnnotation(AnnotatedMethod am) {
         return this._primary.hasAsValueAnnotation(am) || this._secondary.hasAsValueAnnotation(am);
      }

      public String findEnumValue(Enum<?> value) {
         String result = this._primary.findEnumValue(value);
         if (result == null) {
            result = this._secondary.findEnumValue(value);
         }

         return result;
      }

      public String findSerializablePropertyName(AnnotatedField af) {
         String result = this._primary.findSerializablePropertyName(af);
         if (result == null) {
            result = this._secondary.findSerializablePropertyName(af);
         } else if (result.length() == 0) {
            String str2 = this._secondary.findSerializablePropertyName(af);
            if (str2 != null) {
               result = str2;
            }
         }

         return result;
      }

      public Object findDeserializer(Annotated am) {
         Object result = this._primary.findDeserializer(am);
         if (result == null) {
            result = this._secondary.findDeserializer(am);
         }

         return result;
      }

      public Object findDeserializer(Annotated am, BeanProperty property) {
         Object result = this._primary.findDeserializer(am, property);
         if (result == null) {
            result = this._secondary.findDeserializer(am, property);
         }

         return result;
      }

      public Class<? extends KeyDeserializer> findKeyDeserializer(Annotated am) {
         Class<? extends KeyDeserializer> result = this._primary.findKeyDeserializer(am);
         if (result == null || result == KeyDeserializer.None.class) {
            result = this._secondary.findKeyDeserializer(am);
         }

         return result;
      }

      public Class<? extends JsonDeserializer<?>> findContentDeserializer(Annotated am) {
         Class<? extends JsonDeserializer<?>> result = this._primary.findContentDeserializer(am);
         if (result == null || result == JsonDeserializer.None.class) {
            result = this._secondary.findContentDeserializer(am);
         }

         return result;
      }

      public Class<?> findDeserializationType(Annotated am, JavaType baseType, String propName) {
         Class<?> result = this._primary.findDeserializationType(am, baseType, propName);
         if (result == null) {
            result = this._secondary.findDeserializationType(am, baseType, propName);
         }

         return result;
      }

      public Class<?> findDeserializationKeyType(Annotated am, JavaType baseKeyType, String propName) {
         Class<?> result = this._primary.findDeserializationKeyType(am, baseKeyType, propName);
         if (result == null) {
            result = this._secondary.findDeserializationKeyType(am, baseKeyType, propName);
         }

         return result;
      }

      public Class<?> findDeserializationContentType(Annotated am, JavaType baseContentType, String propName) {
         Class<?> result = this._primary.findDeserializationContentType(am, baseContentType, propName);
         if (result == null) {
            result = this._secondary.findDeserializationContentType(am, baseContentType, propName);
         }

         return result;
      }

      public String findSettablePropertyName(AnnotatedMethod am) {
         String result = this._primary.findSettablePropertyName(am);
         if (result == null) {
            result = this._secondary.findSettablePropertyName(am);
         } else if (result.length() == 0) {
            String str2 = this._secondary.findSettablePropertyName(am);
            if (str2 != null) {
               result = str2;
            }
         }

         return result;
      }

      public boolean hasAnySetterAnnotation(AnnotatedMethod am) {
         return this._primary.hasAnySetterAnnotation(am) || this._secondary.hasAnySetterAnnotation(am);
      }

      public boolean hasAnyGetterAnnotation(AnnotatedMethod am) {
         return this._primary.hasAnyGetterAnnotation(am) || this._secondary.hasAnyGetterAnnotation(am);
      }

      public boolean hasCreatorAnnotation(Annotated a) {
         return this._primary.hasCreatorAnnotation(a) || this._secondary.hasCreatorAnnotation(a);
      }

      public String findDeserializablePropertyName(AnnotatedField af) {
         String result = this._primary.findDeserializablePropertyName(af);
         if (result == null) {
            result = this._secondary.findDeserializablePropertyName(af);
         } else if (result.length() == 0) {
            String str2 = this._secondary.findDeserializablePropertyName(af);
            if (str2 != null) {
               result = str2;
            }
         }

         return result;
      }

      public String findPropertyNameForParam(AnnotatedParameter param) {
         String result = this._primary.findPropertyNameForParam(param);
         if (result == null) {
            result = this._secondary.findPropertyNameForParam(param);
         }

         return result;
      }
   }

   public static class ReferenceProperty {
      private final AnnotationIntrospector.ReferenceProperty.Type _type;
      private final String _name;

      public ReferenceProperty(AnnotationIntrospector.ReferenceProperty.Type t, String n) {
         this._type = t;
         this._name = n;
      }

      public static AnnotationIntrospector.ReferenceProperty managed(String name) {
         return new AnnotationIntrospector.ReferenceProperty(AnnotationIntrospector.ReferenceProperty.Type.MANAGED_REFERENCE, name);
      }

      public static AnnotationIntrospector.ReferenceProperty back(String name) {
         return new AnnotationIntrospector.ReferenceProperty(AnnotationIntrospector.ReferenceProperty.Type.BACK_REFERENCE, name);
      }

      public AnnotationIntrospector.ReferenceProperty.Type getType() {
         return this._type;
      }

      public String getName() {
         return this._name;
      }

      public boolean isManagedReference() {
         return this._type == AnnotationIntrospector.ReferenceProperty.Type.MANAGED_REFERENCE;
      }

      public boolean isBackReference() {
         return this._type == AnnotationIntrospector.ReferenceProperty.Type.BACK_REFERENCE;
      }

      public static enum Type {
         MANAGED_REFERENCE,
         BACK_REFERENCE;

         private Type() {
         }
      }
   }
}
