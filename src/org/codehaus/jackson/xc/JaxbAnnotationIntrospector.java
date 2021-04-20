package org.codehaus.jackson.xc;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.XmlElement.DEFAULT;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.jsontype.impl.StdTypeResolverBuilder;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.util.VersionUtil;

public class JaxbAnnotationIntrospector extends AnnotationIntrospector implements Versioned {
   protected static final String MARKER_FOR_DEFAULT = "##default";
   protected final String _jaxbPackageName = XmlElement.class.getPackage().getName();
   protected final JsonSerializer<?> _dataHandlerSerializer;
   protected final JsonDeserializer<?> _dataHandlerDeserializer;
   private static final ThreadLocal<SoftReference<JaxbAnnotationIntrospector.PropertyDescriptors>> _propertyDescriptors = new ThreadLocal();

   public JaxbAnnotationIntrospector() {
      JsonSerializer<?> dataHandlerSerializer = null;
      JsonDeserializer dataHandlerDeserializer = null;

      try {
         dataHandlerSerializer = (JsonSerializer)Class.forName("org.codehaus.jackson.xc.DataHandlerJsonSerializer").newInstance();
         dataHandlerDeserializer = (JsonDeserializer)Class.forName("org.codehaus.jackson.xc.DataHandlerJsonDeserializer").newInstance();
      } catch (Throwable var4) {
      }

      this._dataHandlerSerializer = dataHandlerSerializer;
      this._dataHandlerDeserializer = dataHandlerDeserializer;
   }

   public Version version() {
      return VersionUtil.versionFor(this.getClass());
   }

   public boolean isHandled(Annotation ann) {
      Class<?> cls = ann.annotationType();
      Package pkg = cls.getPackage();
      String pkgName = pkg != null ? pkg.getName() : cls.getName();
      if (pkgName.startsWith(this._jaxbPackageName)) {
         return true;
      } else {
         return cls == JsonCachable.class;
      }
   }

   public Boolean findCachability(AnnotatedClass ac) {
      JsonCachable ann = (JsonCachable)ac.getAnnotation(JsonCachable.class);
      if (ann != null) {
         return ann.value() ? Boolean.TRUE : Boolean.FALSE;
      } else {
         return null;
      }
   }

   public String findRootName(AnnotatedClass ac) {
      XmlRootElement elem = this.findRootElementAnnotation(ac);
      if (elem != null) {
         String name = elem.name();
         return "##default".equals(name) ? "" : name;
      } else {
         return null;
      }
   }

   public String[] findPropertiesToIgnore(AnnotatedClass ac) {
      return null;
   }

   public Boolean findIgnoreUnknownProperties(AnnotatedClass ac) {
      return null;
   }

   public Boolean isIgnorableType(AnnotatedClass ac) {
      return null;
   }

   public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass ac, VisibilityChecker<?> checker) {
      XmlAccessType at = this.findAccessType(ac);
      if (at == null) {
         return checker;
      } else {
         switch(at) {
         case FIELD:
            return checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY).withSetterVisibility(JsonAutoDetect.Visibility.NONE).withGetterVisibility(JsonAutoDetect.Visibility.NONE).withIsGetterVisibility(JsonAutoDetect.Visibility.NONE);
         case NONE:
            return checker.withFieldVisibility(JsonAutoDetect.Visibility.NONE).withSetterVisibility(JsonAutoDetect.Visibility.NONE).withGetterVisibility(JsonAutoDetect.Visibility.NONE).withIsGetterVisibility(JsonAutoDetect.Visibility.NONE);
         case PROPERTY:
            return checker.withFieldVisibility(JsonAutoDetect.Visibility.NONE).withSetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY).withGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY).withIsGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY);
         case PUBLIC_MEMBER:
            return checker.withFieldVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY).withSetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY).withGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY).withIsGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY);
         default:
            return checker;
         }
      }
   }

   protected XmlAccessType findAccessType(Annotated ac) {
      XmlAccessorType at = (XmlAccessorType)this.findAnnotation(XmlAccessorType.class, ac, true, true, true);
      return at == null ? null : at.value();
   }

   public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
      return null;
   }

   public TypeResolverBuilder<?> findPropertyTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType baseType) {
      return baseType.isContainerType() ? null : this._typeResolverFromXmlElements(am);
   }

   public TypeResolverBuilder<?> findPropertyContentTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType containerType) {
      if (!containerType.isContainerType()) {
         throw new IllegalArgumentException("Must call method with a container type (got " + containerType + ")");
      } else {
         return this._typeResolverFromXmlElements(am);
      }
   }

   protected TypeResolverBuilder<?> _typeResolverFromXmlElements(AnnotatedMember am) {
      XmlElements elems = (XmlElements)this.findAnnotation(XmlElements.class, am, false, false, false);
      XmlElementRefs elemRefs = (XmlElementRefs)this.findAnnotation(XmlElementRefs.class, am, false, false, false);
      if (elems == null && elemRefs == null) {
         return null;
      } else {
         TypeResolverBuilder<?> b = new StdTypeResolverBuilder();
         TypeResolverBuilder<?> b = b.init(JsonTypeInfo.Id.NAME, (TypeIdResolver)null);
         b = b.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
         return b;
      }
   }

   public List<NamedType> findSubtypes(Annotated a) {
      XmlElements elems = (XmlElements)this.findAnnotation(XmlElements.class, a, false, false, false);
      int len$;
      if (elems != null) {
         ArrayList<NamedType> result = new ArrayList();
         XmlElement[] arr$ = elems.value();
         int len$ = arr$.length;

         for(len$ = 0; len$ < len$; ++len$) {
            XmlElement elem = arr$[len$];
            String name = elem.name();
            if ("##default".equals(name)) {
               name = null;
            }

            result.add(new NamedType(elem.type(), name));
         }

         return result;
      } else {
         XmlElementRefs elemRefs = (XmlElementRefs)this.findAnnotation(XmlElementRefs.class, a, false, false, false);
         if (elemRefs == null) {
            return null;
         } else {
            ArrayList<NamedType> result = new ArrayList();
            XmlElementRef[] arr$ = elemRefs.value();
            len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               XmlElementRef elemRef = arr$[i$];
               Class<?> refType = elemRef.type();
               if (!JAXBElement.class.isAssignableFrom(refType)) {
                  String name = elemRef.name();
                  if (name == null || "##default".equals(name)) {
                     XmlRootElement rootElement = (XmlRootElement)refType.getAnnotation(XmlRootElement.class);
                     if (rootElement != null) {
                        name = rootElement.name();
                     }
                  }

                  if (name == null || "##default".equals(name)) {
                     name = Introspector.decapitalize(refType.getSimpleName());
                  }

                  result.add(new NamedType(refType, name));
               }
            }

            return result;
         }
      }
   }

   public String findTypeName(AnnotatedClass ac) {
      XmlType type = (XmlType)this.findAnnotation(XmlType.class, ac, false, false, false);
      if (type != null) {
         String name = type.name();
         if (!"##default".equals(name)) {
            return name;
         }
      }

      return null;
   }

   public boolean isIgnorableMethod(AnnotatedMethod m) {
      return m.getAnnotation(XmlTransient.class) != null;
   }

   public boolean isIgnorableConstructor(AnnotatedConstructor c) {
      return false;
   }

   public boolean isIgnorableField(AnnotatedField f) {
      return f.getAnnotation(XmlTransient.class) != null;
   }

   public JsonSerializer<?> findSerializer(Annotated am, BeanProperty property) {
      XmlAdapter<Object, Object> adapter = this.findAdapter(am, true);
      if (adapter != null) {
         return new XmlAdapterJsonSerializer(adapter, property);
      } else {
         Class<?> type = am.getRawType();
         return type != null && this._dataHandlerSerializer != null && this.isDataHandler(type) ? this._dataHandlerSerializer : null;
      }
   }

   private boolean isDataHandler(Class<?> type) {
      return type != null && Object.class != type && ("javax.activation.DataHandler".equals(type.getName()) || this.isDataHandler(type.getSuperclass()));
   }

   public Class<?> findSerializationType(Annotated a) {
      XmlElement annotation = (XmlElement)this.findAnnotation(XmlElement.class, a, false, false, false);
      if (annotation != null && annotation.type() != DEFAULT.class) {
         Class<?> rawPropType = a.getRawType();
         return this.isIndexedType(rawPropType) ? null : annotation.type();
      } else {
         return null;
      }
   }

   public JsonSerialize.Inclusion findSerializationInclusion(Annotated a, JsonSerialize.Inclusion defValue) {
      XmlElementWrapper w = (XmlElementWrapper)a.getAnnotation(XmlElementWrapper.class);
      if (w != null) {
         return w.nillable() ? JsonSerialize.Inclusion.ALWAYS : JsonSerialize.Inclusion.NON_NULL;
      } else {
         XmlElement e = (XmlElement)a.getAnnotation(XmlElement.class);
         if (e != null) {
            return e.nillable() ? JsonSerialize.Inclusion.ALWAYS : JsonSerialize.Inclusion.NON_NULL;
         } else {
            return defValue;
         }
      }
   }

   public JsonSerialize.Typing findSerializationTyping(Annotated a) {
      return null;
   }

   public Class<?>[] findSerializationViews(Annotated a) {
      return null;
   }

   public String[] findSerializationPropertyOrder(AnnotatedClass ac) {
      XmlType type = (XmlType)this.findAnnotation(XmlType.class, ac, true, true, true);
      if (type == null) {
         return null;
      } else {
         String[] order = type.propOrder();
         if (order != null && order.length != 0) {
            JaxbAnnotationIntrospector.PropertyDescriptors props = this.getDescriptors(ac.getRawType());
            int i = 0;

            for(int len = order.length; i < len; ++i) {
               String propName = order[i];
               if (props.findByPropertyName(propName) == null && propName.length() != 0) {
                  StringBuilder sb = new StringBuilder();
                  sb.append("get");
                  sb.append(Character.toUpperCase(propName.charAt(0)));
                  if (propName.length() > 1) {
                     sb.append(propName.substring(1));
                  }

                  PropertyDescriptor desc = props.findByMethodName(sb.toString());
                  if (desc != null) {
                     order[i] = desc.getName();
                  }
               }
            }

            return order;
         } else {
            return null;
         }
      }
   }

   public Boolean findSerializationSortAlphabetically(AnnotatedClass ac) {
      XmlAccessorOrder order = (XmlAccessorOrder)this.findAnnotation(XmlAccessorOrder.class, ac, true, true, true);
      return order == null ? null : order.value() == XmlAccessOrder.ALPHABETICAL;
   }

   public String findGettablePropertyName(AnnotatedMethod am) {
      PropertyDescriptor desc = this.findPropertyDescriptor(am);
      return desc != null ? this.findJaxbSpecifiedPropertyName(desc) : null;
   }

   public boolean hasAsValueAnnotation(AnnotatedMethod am) {
      return false;
   }

   public String findEnumValue(Enum<?> e) {
      Class<?> enumClass = e.getDeclaringClass();
      String enumValue = e.name();

      try {
         XmlEnumValue xmlEnumValue = (XmlEnumValue)enumClass.getDeclaredField(enumValue).getAnnotation(XmlEnumValue.class);
         return xmlEnumValue != null ? xmlEnumValue.value() : enumValue;
      } catch (NoSuchFieldException var5) {
         throw new IllegalStateException("Could not locate Enum entry '" + enumValue + "' (Enum class " + enumClass.getName() + ")", var5);
      }
   }

   public String findSerializablePropertyName(AnnotatedField af) {
      if (this.isInvisible(af)) {
         return null;
      } else {
         Field field = af.getAnnotated();
         String name = findJaxbPropertyName(field, field.getType(), "");
         return name == null ? field.getName() : name;
      }
   }

   public JsonDeserializer<?> findDeserializer(Annotated am, BeanProperty property) {
      XmlAdapter<Object, Object> adapter = this.findAdapter(am, false);
      if (adapter != null) {
         return new XmlAdapterJsonDeserializer(adapter, property);
      } else {
         Class<?> type = am.getRawType();
         return type != null && this._dataHandlerDeserializer != null && this.isDataHandler(type) ? this._dataHandlerDeserializer : null;
      }
   }

   public Class<KeyDeserializer> findKeyDeserializer(Annotated am) {
      return null;
   }

   public Class<JsonDeserializer<?>> findContentDeserializer(Annotated am) {
      return null;
   }

   public Class<?> findDeserializationType(Annotated a, JavaType baseType, String propName) {
      return !baseType.isContainerType() ? this._doFindDeserializationType(a, baseType, propName) : null;
   }

   public Class<?> findDeserializationKeyType(Annotated am, JavaType baseKeyType, String propName) {
      return null;
   }

   public Class<?> findDeserializationContentType(Annotated a, JavaType baseContentType, String propName) {
      Class<?> type = this._doFindDeserializationType(a, baseContentType, propName);
      return type;
   }

   protected Class<?> _doFindDeserializationType(Annotated a, JavaType baseType, String propName) {
      if (a.hasAnnotation(XmlJavaTypeAdapter.class)) {
         return null;
      } else {
         XmlElement annotation = (XmlElement)this.findAnnotation(XmlElement.class, a, false, false, false);
         if (annotation != null) {
            Class<?> type = annotation.type();
            if (type != DEFAULT.class) {
               return type;
            }
         }

         if (a instanceof AnnotatedMethod && propName != null) {
            AnnotatedMethod am = (AnnotatedMethod)a;
            annotation = (XmlElement)this.findFieldAnnotation(XmlElement.class, am.getDeclaringClass(), propName);
            if (annotation != null && annotation.type() != DEFAULT.class) {
               return annotation.type();
            }
         }

         return null;
      }
   }

   public String findSettablePropertyName(AnnotatedMethod am) {
      PropertyDescriptor desc = this.findPropertyDescriptor(am);
      return desc != null ? this.findJaxbSpecifiedPropertyName(desc) : null;
   }

   public boolean hasAnySetterAnnotation(AnnotatedMethod am) {
      return false;
   }

   public boolean hasCreatorAnnotation(Annotated am) {
      return false;
   }

   public String findDeserializablePropertyName(AnnotatedField af) {
      if (this.isInvisible(af)) {
         return null;
      } else {
         Field field = af.getAnnotated();
         String name = findJaxbPropertyName(field, field.getType(), "");
         return name == null ? field.getName() : name;
      }
   }

   public String findPropertyNameForParam(AnnotatedParameter param) {
      return null;
   }

   protected boolean isInvisible(AnnotatedField f) {
      boolean invisible = true;
      Annotation[] arr$ = f.getAnnotated().getDeclaredAnnotations();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Annotation annotation = arr$[i$];
         if (this.isHandled(annotation)) {
            invisible = false;
         }
      }

      if (invisible) {
         XmlAccessType accessType = XmlAccessType.PUBLIC_MEMBER;
         XmlAccessorType at = (XmlAccessorType)this.findAnnotation(XmlAccessorType.class, f, true, true, true);
         if (at != null) {
            accessType = at.value();
         }

         invisible = accessType != XmlAccessType.FIELD && (accessType != XmlAccessType.PUBLIC_MEMBER || !Modifier.isPublic(f.getAnnotated().getModifiers()));
      }

      return invisible;
   }

   protected <A extends Annotation> A findAnnotation(Class<A> annotationClass, Annotated annotated, boolean includePackage, boolean includeClass, boolean includeSuperclasses) {
      Annotation annotation;
      if (annotated instanceof AnnotatedMethod) {
         PropertyDescriptor pd = this.findPropertyDescriptor((AnnotatedMethod)annotated);
         if (pd != null) {
            annotation = (new JaxbAnnotationIntrospector.AnnotatedProperty(pd)).getAnnotation(annotationClass);
            if (annotation != null) {
               return annotation;
            }
         }
      }

      AnnotatedElement annType = annotated.getAnnotated();
      annotation = null;
      Annotation annotation;
      Class memberClass;
      if (annotated instanceof AnnotatedParameter) {
         AnnotatedParameter param = (AnnotatedParameter)annotated;
         annotation = param.getAnnotation(annotationClass);
         if (annotation != null) {
            return annotation;
         }

         memberClass = param.getMember().getDeclaringClass();
      } else {
         A annotation = annType.getAnnotation(annotationClass);
         if (annotation != null) {
            return annotation;
         }

         if (annType instanceof Member) {
            memberClass = ((Member)annType).getDeclaringClass();
            if (includeClass) {
               annotation = memberClass.getAnnotation(annotationClass);
               if (annotation != null) {
                  return annotation;
               }
            }
         } else {
            if (!(annType instanceof Class)) {
               throw new IllegalStateException("Unsupported annotated member: " + annotated.getClass().getName());
            }

            memberClass = (Class)annType;
         }
      }

      if (memberClass != null) {
         if (includeSuperclasses) {
            for(Class superclass = memberClass.getSuperclass(); superclass != null && superclass != Object.class; superclass = superclass.getSuperclass()) {
               annotation = superclass.getAnnotation(annotationClass);
               if (annotation != null) {
                  return annotation;
               }
            }
         }

         if (includePackage) {
            return memberClass.getPackage().getAnnotation(annotationClass);
         }
      }

      return null;
   }

   protected <A extends Annotation> A findFieldAnnotation(Class<A> annotationType, Class<?> cls, String fieldName) {
      while(true) {
         Field[] arr$ = cls.getDeclaredFields();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Field f = arr$[i$];
            if (fieldName.equals(f.getName())) {
               return f.getAnnotation(annotationType);
            }
         }

         if (!cls.isInterface() && cls != Object.class) {
            cls = cls.getSuperclass();
            if (cls != null) {
               continue;
            }
         }

         return null;
      }
   }

   protected JaxbAnnotationIntrospector.PropertyDescriptors getDescriptors(Class<?> forClass) {
      SoftReference<JaxbAnnotationIntrospector.PropertyDescriptors> ref = (SoftReference)_propertyDescriptors.get();
      JaxbAnnotationIntrospector.PropertyDescriptors descriptors = ref == null ? null : (JaxbAnnotationIntrospector.PropertyDescriptors)ref.get();
      if (descriptors == null || descriptors.getBeanClass() != forClass) {
         try {
            descriptors = JaxbAnnotationIntrospector.PropertyDescriptors.find(forClass);
         } catch (IntrospectionException var5) {
            throw new IllegalArgumentException("Problem introspecting bean properties: " + var5.getMessage(), var5);
         }

         _propertyDescriptors.set(new SoftReference(descriptors));
      }

      return descriptors;
   }

   protected PropertyDescriptor findPropertyDescriptor(AnnotatedMethod m) {
      JaxbAnnotationIntrospector.PropertyDescriptors descs = this.getDescriptors(m.getDeclaringClass());
      PropertyDescriptor desc = descs.findByMethodName(m.getName());
      return desc;
   }

   protected String findJaxbSpecifiedPropertyName(PropertyDescriptor prop) {
      return findJaxbPropertyName(new JaxbAnnotationIntrospector.AnnotatedProperty(prop), prop.getPropertyType(), prop.getName());
   }

   protected static String findJaxbPropertyName(AnnotatedElement ae, Class<?> aeType, String defaultName) {
      XmlElementWrapper elementWrapper = (XmlElementWrapper)ae.getAnnotation(XmlElementWrapper.class);
      if (elementWrapper != null) {
         String name = elementWrapper.name();
         return !"##default".equals(name) ? name : defaultName;
      } else {
         XmlAttribute attribute = (XmlAttribute)ae.getAnnotation(XmlAttribute.class);
         if (attribute != null) {
            String name = attribute.name();
            return !"##default".equals(name) ? name : defaultName;
         } else {
            XmlElement element = (XmlElement)ae.getAnnotation(XmlElement.class);
            if (element != null) {
               String name = element.name();
               return !"##default".equals(name) ? name : defaultName;
            } else {
               XmlElementRef elementRef = (XmlElementRef)ae.getAnnotation(XmlElementRef.class);
               if (elementRef != null) {
                  String name = elementRef.name();
                  if (!"##default".equals(name)) {
                     return name;
                  }

                  if (aeType != null) {
                     XmlRootElement rootElement = (XmlRootElement)aeType.getAnnotation(XmlRootElement.class);
                     if (rootElement != null) {
                        name = rootElement.name();
                        if (!"##default".equals(name)) {
                           return name;
                        }

                        return Introspector.decapitalize(aeType.getSimpleName());
                     }
                  }
               }

               XmlValue valueInfo = (XmlValue)ae.getAnnotation(XmlValue.class);
               return valueInfo != null ? "value" : null;
            }
         }
      }
   }

   private XmlRootElement findRootElementAnnotation(AnnotatedClass ac) {
      return (XmlRootElement)this.findAnnotation(XmlRootElement.class, ac, true, false, true);
   }

   protected XmlAdapter<Object, Object> findAdapter(Annotated am, boolean forSerialization) {
      if (am instanceof AnnotatedClass) {
         return this.findAdapterForClass((AnnotatedClass)am, forSerialization);
      } else {
         Class<?> memberType = am.getRawType();
         if (memberType == Void.TYPE && am instanceof AnnotatedMethod) {
            memberType = ((AnnotatedMethod)am).getParameterClass(0);
         }

         Member member = (Member)am.getAnnotated();
         if (member != null) {
            Class<?> potentialAdaptee = member.getDeclaringClass();
            if (potentialAdaptee != null) {
               XmlJavaTypeAdapter adapterInfo = (XmlJavaTypeAdapter)potentialAdaptee.getAnnotation(XmlJavaTypeAdapter.class);
               if (adapterInfo != null) {
                  XmlAdapter<Object, Object> adapter = this.checkAdapter(adapterInfo, memberType);
                  if (adapter != null) {
                     return adapter;
                  }
               }
            }
         }

         XmlJavaTypeAdapter adapterInfo = (XmlJavaTypeAdapter)this.findAnnotation(XmlJavaTypeAdapter.class, am, true, false, false);
         if (adapterInfo != null) {
            XmlAdapter<Object, Object> adapter = this.checkAdapter(adapterInfo, memberType);
            if (adapter != null) {
               return adapter;
            }
         }

         XmlJavaTypeAdapters adapters = (XmlJavaTypeAdapters)this.findAnnotation(XmlJavaTypeAdapters.class, am, true, false, false);
         if (adapters != null) {
            XmlJavaTypeAdapter[] arr$ = adapters.value();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               XmlJavaTypeAdapter info = arr$[i$];
               XmlAdapter<Object, Object> adapter = this.checkAdapter(info, memberType);
               if (adapter != null) {
                  return adapter;
               }
            }
         }

         return null;
      }
   }

   private final XmlAdapter<Object, Object> checkAdapter(XmlJavaTypeAdapter adapterInfo, Class<?> typeNeeded) {
      Class<?> adaptedType = adapterInfo.type();
      if (adaptedType != javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT.class && !adaptedType.isAssignableFrom(typeNeeded)) {
         return null;
      } else {
         Class<? extends XmlAdapter> cls = adapterInfo.value();
         return (XmlAdapter)ClassUtil.createInstance(cls, false);
      }
   }

   protected XmlAdapter<Object, Object> findAdapterForClass(AnnotatedClass ac, boolean forSerialization) {
      XmlJavaTypeAdapter adapterInfo = (XmlJavaTypeAdapter)ac.getAnnotated().getAnnotation(XmlJavaTypeAdapter.class);
      if (adapterInfo != null) {
         Class<? extends XmlAdapter> cls = adapterInfo.value();
         return (XmlAdapter)ClassUtil.createInstance(cls, false);
      } else {
         return null;
      }
   }

   protected boolean isIndexedType(Class<?> raw) {
      return raw.isArray() || Collection.class.isAssignableFrom(raw) || Map.class.isAssignableFrom(raw);
   }

   protected static final class PropertyDescriptors {
      private final Class<?> _forClass;
      private final List<PropertyDescriptor> _properties;
      private Map<String, PropertyDescriptor> _byMethodName;
      private Map<String, PropertyDescriptor> _byPropertyName;

      public PropertyDescriptors(Class<?> forClass, List<PropertyDescriptor> properties) {
         this._forClass = forClass;
         this._properties = properties;
      }

      public Class<?> getBeanClass() {
         return this._forClass;
      }

      public PropertyDescriptor findByPropertyName(String name) {
         if (this._byPropertyName == null) {
            this._byPropertyName = new HashMap(this._properties.size());
            Iterator i$ = this._properties.iterator();

            while(i$.hasNext()) {
               PropertyDescriptor desc = (PropertyDescriptor)i$.next();
               this._byPropertyName.put(desc.getName(), desc);
            }
         }

         return (PropertyDescriptor)this._byPropertyName.get(name);
      }

      public PropertyDescriptor findByMethodName(String name) {
         if (this._byMethodName == null) {
            this._byMethodName = new HashMap(this._properties.size());
            Iterator i$ = this._properties.iterator();

            while(i$.hasNext()) {
               PropertyDescriptor desc = (PropertyDescriptor)i$.next();
               Method getter = desc.getReadMethod();
               if (getter != null) {
                  this._byMethodName.put(getter.getName(), desc);
               }

               Method setter = desc.getWriteMethod();
               if (setter != null) {
                  this._byMethodName.put(setter.getName(), desc);
               }
            }
         }

         return (PropertyDescriptor)this._byMethodName.get(name);
      }

      public static JaxbAnnotationIntrospector.PropertyDescriptors find(Class<?> forClass) throws IntrospectionException {
         BeanInfo beanInfo = Introspector.getBeanInfo(forClass);
         PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
         Object descriptors;
         if (pds.length == 0) {
            descriptors = Collections.emptyList();
         } else {
            descriptors = new ArrayList();
            Map<String, PropertyDescriptor> partials = null;
            PropertyDescriptor[] arr$ = beanInfo.getPropertyDescriptors();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               PropertyDescriptor pd = arr$[i$];
               Method read = pd.getReadMethod();
               if (read != null && read.getAnnotation(XmlTransient.class) != null) {
                  read = null;
               }

               String readName = read == null ? null : JaxbAnnotationIntrospector.findJaxbPropertyName(read, pd.getPropertyType(), (String)null);
               Method write = pd.getWriteMethod();
               if (write != null && write.getAnnotation(XmlTransient.class) != null) {
                  write = null;
               }

               if (read != null || write != null) {
                  String writeName = write == null ? null : JaxbAnnotationIntrospector.findJaxbPropertyName(write, pd.getPropertyType(), (String)null);
                  if (write == null) {
                     if (readName == null) {
                        readName = pd.getName();
                     }

                     partials = _processReadMethod(partials, read, readName, (List)descriptors);
                  } else if (read == null) {
                     if (writeName == null) {
                        writeName = pd.getName();
                     }

                     partials = _processWriteMethod(partials, write, writeName, (List)descriptors);
                  } else if (readName != null && writeName != null && !readName.equals(writeName)) {
                     partials = _processReadMethod(partials, read, readName, (List)descriptors);
                     partials = _processWriteMethod(partials, write, writeName, (List)descriptors);
                  } else {
                     String name;
                     if (readName != null) {
                        name = readName;
                     } else if (writeName != null) {
                        name = writeName;
                     } else {
                        name = pd.getName();
                     }

                     ((List)descriptors).add(new PropertyDescriptor(name, read, write));
                  }
               }
            }
         }

         return new JaxbAnnotationIntrospector.PropertyDescriptors(forClass, (List)descriptors);
      }

      private static Map<String, PropertyDescriptor> _processReadMethod(Map<String, PropertyDescriptor> partials, Method method, String propertyName, List<PropertyDescriptor> pds) throws IntrospectionException {
         PropertyDescriptor pd;
         if (partials == null) {
            partials = new HashMap();
         } else {
            pd = (PropertyDescriptor)((Map)partials).get(propertyName);
            if (pd != null) {
               pd.setReadMethod(method);
               if (pd.getWriteMethod() != null) {
                  pds.add(pd);
                  ((Map)partials).remove(propertyName);
                  return (Map)partials;
               }
            }
         }

         pd = new PropertyDescriptor(propertyName, method, (Method)null);
         ((Map)partials).put(propertyName, pd);
         return (Map)partials;
      }

      private static Map<String, PropertyDescriptor> _processWriteMethod(Map<String, PropertyDescriptor> partials, Method method, String propertyName, List<PropertyDescriptor> pds) throws IntrospectionException {
         if (partials == null) {
            partials = new HashMap();
         } else {
            PropertyDescriptor pd = (PropertyDescriptor)((Map)partials).get(propertyName);
            if (pd != null) {
               pd.setWriteMethod(method);
               if (pd.getReadMethod() != null) {
                  pds.add(pd);
                  ((Map)partials).remove(propertyName);
                  return (Map)partials;
               }
            }
         }

         ((Map)partials).put(propertyName, new PropertyDescriptor(propertyName, (Method)null, method));
         return (Map)partials;
      }
   }

   private static class AnnotatedProperty implements AnnotatedElement {
      private final PropertyDescriptor pd;

      private AnnotatedProperty(PropertyDescriptor pd) {
         this.pd = pd;
      }

      public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
         Method m = this.pd.getReadMethod();
         if (m != null && m.isAnnotationPresent(annotationClass)) {
            return true;
         } else {
            m = this.pd.getWriteMethod();
            return m != null && m.isAnnotationPresent(annotationClass);
         }
      }

      public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
         Method m = this.pd.getReadMethod();
         if (m != null) {
            T ann = m.getAnnotation(annotationClass);
            if (ann != null) {
               return ann;
            }
         }

         m = this.pd.getWriteMethod();
         return m != null ? m.getAnnotation(annotationClass) : null;
      }

      public Annotation[] getAnnotations() {
         throw new UnsupportedOperationException();
      }

      public Annotation[] getDeclaredAnnotations() {
         throw new UnsupportedOperationException();
      }
   }
}
