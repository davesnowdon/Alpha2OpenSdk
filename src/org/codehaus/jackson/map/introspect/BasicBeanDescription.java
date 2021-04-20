package org.codehaus.jackson.map.introspect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

public class BasicBeanDescription extends BeanDescription {
   protected final MapperConfig<?> _config;
   protected final AnnotationIntrospector _annotationIntrospector;
   protected final AnnotatedClass _classInfo;
   protected TypeBindings _bindings;

   public BasicBeanDescription(MapperConfig<?> config, JavaType type, AnnotatedClass ac) {
      super(type);
      this._config = config;
      this._annotationIntrospector = config.getAnnotationIntrospector();
      this._classInfo = ac;
   }

   public boolean hasKnownClassAnnotations() {
      return this._classInfo.hasAnnotations();
   }

   public Annotations getClassAnnotations() {
      return this._classInfo.getAnnotations();
   }

   public TypeBindings bindingsForBeanType() {
      if (this._bindings == null) {
         this._bindings = new TypeBindings(this._config.getTypeFactory(), this._type);
      }

      return this._bindings;
   }

   public AnnotatedClass getClassInfo() {
      return this._classInfo;
   }

   public AnnotatedMethod findMethod(String name, Class<?>[] paramTypes) {
      return this._classInfo.findMethod(name, paramTypes);
   }

   public Object instantiateBean(boolean fixAccess) {
      AnnotatedConstructor ac = this._classInfo.getDefaultConstructor();
      if (ac == null) {
         return null;
      } else {
         if (fixAccess) {
            ac.fixAccess();
         }

         try {
            return ac.getAnnotated().newInstance();
         } catch (Exception var5) {
            Object t;
            for(t = var5; ((Throwable)t).getCause() != null; t = ((Throwable)t).getCause()) {
            }

            if (t instanceof Error) {
               throw (Error)t;
            } else if (t instanceof RuntimeException) {
               throw (RuntimeException)t;
            } else {
               throw new IllegalArgumentException("Failed to instantiate bean of type " + this._classInfo.getAnnotated().getName() + ": (" + t.getClass().getName() + ") " + ((Throwable)t).getMessage(), (Throwable)t);
            }
         }
      }
   }

   public LinkedHashMap<String, AnnotatedMethod> findGetters(VisibilityChecker<?> visibilityChecker, Collection<String> ignoredProperties) {
      LinkedHashMap<String, AnnotatedMethod> results = new LinkedHashMap();
      PropertyNamingStrategy naming = this._config.getPropertyNamingStrategy();
      Iterator i$ = this._classInfo.memberMethods().iterator();

      AnnotatedMethod am;
      String propName;
      AnnotatedMethod old;
      do {
         do {
            while(true) {
               do {
                  if (!i$.hasNext()) {
                     return results;
                  }

                  am = (AnnotatedMethod)i$.next();
               } while(am.getParameterCount() != 0);

               propName = this._annotationIntrospector.findGettablePropertyName(am);
               if (propName != null) {
                  if (propName.length() == 0) {
                     propName = this.okNameForAnyGetter(am, am.getName());
                     if (propName == null) {
                        propName = am.getName();
                     }

                     if (naming != null) {
                        propName = naming.nameForGetterMethod(this._config, am, propName);
                     }
                  }
                  break;
               }

               propName = am.getName();
               if (propName.startsWith("get")) {
                  if (!visibilityChecker.isGetterVisible(am)) {
                     continue;
                  }

                  propName = this.okNameForGetter(am, propName);
               } else {
                  if (!visibilityChecker.isIsGetterVisible(am)) {
                     continue;
                  }

                  propName = this.okNameForIsGetter(am, propName);
               }

               if (propName != null && !this._annotationIntrospector.hasAnyGetterAnnotation(am)) {
                  if (naming != null) {
                     propName = naming.nameForGetterMethod(this._config, am, propName);
                  }
                  break;
               }
            }
         } while(ignoredProperties != null && ignoredProperties.contains(propName));

         old = (AnnotatedMethod)results.put(propName, am);
      } while(old == null);

      String oldDesc = old.getFullName();
      String newDesc = am.getFullName();
      throw new IllegalArgumentException("Conflicting getter definitions for property \"" + propName + "\": " + oldDesc + " vs " + newDesc);
   }

   public AnnotatedMethod findJsonValueMethod() {
      AnnotatedMethod found = null;
      Iterator i$ = this._classInfo.memberMethods().iterator();

      while(i$.hasNext()) {
         AnnotatedMethod am = (AnnotatedMethod)i$.next();
         if (this._annotationIntrospector.hasAsValueAnnotation(am)) {
            if (found != null) {
               throw new IllegalArgumentException("Multiple methods with active 'as-value' annotation (" + found.getName() + "(), " + am.getName() + ")");
            }

            if (!ClassUtil.hasGetterSignature(am.getAnnotated())) {
               throw new IllegalArgumentException("Method " + am.getName() + "() marked with an 'as-value' annotation, but does not have valid getter signature (non-static, takes no args, returns a value)");
            }

            found = am;
         }
      }

      return found;
   }

   public Constructor<?> findDefaultConstructor() {
      AnnotatedConstructor ac = this._classInfo.getDefaultConstructor();
      return ac == null ? null : ac.getAnnotated();
   }

   public List<AnnotatedConstructor> getConstructors() {
      return this._classInfo.getConstructors();
   }

   public List<AnnotatedMethod> getFactoryMethods() {
      List<AnnotatedMethod> candidates = this._classInfo.getStaticMethods();
      if (candidates.isEmpty()) {
         return candidates;
      } else {
         ArrayList<AnnotatedMethod> result = new ArrayList();
         Iterator i$ = candidates.iterator();

         while(i$.hasNext()) {
            AnnotatedMethod am = (AnnotatedMethod)i$.next();
            if (this.isFactoryMethod(am)) {
               result.add(am);
            }
         }

         return result;
      }
   }

   public Constructor<?> findSingleArgConstructor(Class<?>... argTypes) {
      Iterator i$ = this._classInfo.getConstructors().iterator();

      while(true) {
         AnnotatedConstructor ac;
         do {
            if (!i$.hasNext()) {
               return null;
            }

            ac = (AnnotatedConstructor)i$.next();
         } while(ac.getParameterCount() != 1);

         Class<?> actArg = ac.getParameterClass(0);
         Class[] arr$ = argTypes;
         int len$ = argTypes.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Class<?> expArg = arr$[i$];
            if (expArg == actArg) {
               return ac.getAnnotated();
            }
         }
      }
   }

   public Method findFactoryMethod(Class<?>... expArgTypes) {
      Iterator i$ = this._classInfo.getStaticMethods().iterator();

      while(true) {
         AnnotatedMethod am;
         do {
            if (!i$.hasNext()) {
               return null;
            }

            am = (AnnotatedMethod)i$.next();
         } while(!this.isFactoryMethod(am));

         Class<?> actualArgType = am.getParameterClass(0);
         Class[] arr$ = expArgTypes;
         int len$ = expArgTypes.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Class<?> expArgType = arr$[i$];
            if (actualArgType.isAssignableFrom(expArgType)) {
               return am.getAnnotated();
            }
         }
      }
   }

   protected boolean isFactoryMethod(AnnotatedMethod am) {
      Class<?> rt = am.getRawType();
      if (!this.getBeanClass().isAssignableFrom(rt)) {
         return false;
      } else if (this._annotationIntrospector.hasCreatorAnnotation(am)) {
         return true;
      } else {
         return "valueOf".equals(am.getName());
      }
   }

   public List<String> findCreatorPropertyNames() {
      List<String> names = null;

      label52:
      for(int i = 0; i < 2; ++i) {
         List<? extends AnnotatedWithParams> l = i == 0 ? this.getConstructors() : this.getFactoryMethods();
         Iterator i$ = l.iterator();

         while(true) {
            AnnotatedWithParams creator;
            int argCount;
            String name;
            do {
               do {
                  if (!i$.hasNext()) {
                     continue label52;
                  }

                  creator = (AnnotatedWithParams)i$.next();
                  argCount = creator.getParameterCount();
               } while(argCount < 1);

               name = this._annotationIntrospector.findPropertyNameForParam(creator.getParameter(0));
            } while(name == null);

            if (names == null) {
               names = new ArrayList();
            }

            names.add(name);

            for(int p = 1; p < argCount; ++p) {
               names.add(this._annotationIntrospector.findPropertyNameForParam(creator.getParameter(p)));
            }
         }
      }

      if (names == null) {
         return Collections.emptyList();
      } else {
         return names;
      }
   }

   public LinkedHashMap<String, AnnotatedField> findSerializableFields(VisibilityChecker<?> vchecker, Collection<String> ignoredProperties) {
      return this._findPropertyFields(vchecker, ignoredProperties, true);
   }

   public JsonSerialize.Inclusion findSerializationInclusion(JsonSerialize.Inclusion defValue) {
      return this._annotationIntrospector.findSerializationInclusion(this._classInfo, defValue);
   }

   public LinkedHashMap<String, AnnotatedMethod> findSetters(VisibilityChecker<?> vchecker) {
      LinkedHashMap<String, AnnotatedMethod> results = new LinkedHashMap();
      PropertyNamingStrategy naming = this._config.getPropertyNamingStrategy();
      Iterator i$ = this._classInfo.memberMethods().iterator();

      while(true) {
         AnnotatedMethod am;
         String propName;
         while(true) {
            do {
               if (!i$.hasNext()) {
                  return results;
               }

               am = (AnnotatedMethod)i$.next();
            } while(am.getParameterCount() != 1);

            propName = this._annotationIntrospector.findSettablePropertyName(am);
            if (propName != null) {
               if (propName.length() == 0) {
                  propName = this.okNameForSetter(am);
                  if (propName == null) {
                     propName = am.getName();
                  }

                  if (naming != null) {
                     propName = naming.nameForSetterMethod(this._config, am, propName);
                  }
               }
               break;
            }

            if (vchecker.isSetterVisible(am)) {
               propName = this.okNameForSetter(am);
               if (propName != null) {
                  if (naming != null) {
                     propName = naming.nameForSetterMethod(this._config, am, propName);
                  }
                  break;
               }
            }
         }

         AnnotatedMethod old = (AnnotatedMethod)results.put(propName, am);
         if (old != null) {
            if (old.getDeclaringClass() == am.getDeclaringClass()) {
               String oldDesc = old.getFullName();
               String newDesc = am.getFullName();
               throw new IllegalArgumentException("Conflicting setter definitions for property \"" + propName + "\": " + oldDesc + " vs " + newDesc);
            }

            results.put(propName, old);
         }
      }
   }

   public AnnotatedMethod findAnySetter() throws IllegalArgumentException {
      AnnotatedMethod found = null;
      Iterator i$ = this._classInfo.memberMethods().iterator();

      while(i$.hasNext()) {
         AnnotatedMethod am = (AnnotatedMethod)i$.next();
         if (this._annotationIntrospector.hasAnySetterAnnotation(am)) {
            if (found != null) {
               throw new IllegalArgumentException("Multiple methods with 'any-setter' annotation (" + found.getName() + "(), " + am.getName() + ")");
            }

            int pcount = am.getParameterCount();
            if (pcount != 2) {
               throw new IllegalArgumentException("Invalid 'any-setter' annotation on method " + am.getName() + "(): takes " + pcount + " parameters, should take 2");
            }

            Class<?> type = am.getParameterClass(0);
            if (type != String.class && type != Object.class) {
               throw new IllegalArgumentException("Invalid 'any-setter' annotation on method " + am.getName() + "(): first argument not of type String or Object, but " + type.getName());
            }

            found = am;
         }
      }

      return found;
   }

   public AnnotatedMethod findAnyGetter() throws IllegalArgumentException {
      AnnotatedMethod found = null;
      Iterator i$ = this._classInfo.memberMethods().iterator();

      while(i$.hasNext()) {
         AnnotatedMethod am = (AnnotatedMethod)i$.next();
         if (this._annotationIntrospector.hasAnyGetterAnnotation(am)) {
            if (found != null) {
               throw new IllegalArgumentException("Multiple methods with 'any-getter' annotation (" + found.getName() + "(), " + am.getName() + ")");
            }

            Class<?> type = am.getRawType();
            if (!Map.class.isAssignableFrom(type)) {
               throw new IllegalArgumentException("Invalid 'any-getter' annotation on method " + am.getName() + "(): return type is not instance of java.util.Map");
            }

            found = am;
         }
      }

      return found;
   }

   public Map<String, AnnotatedMember> findBackReferenceProperties() {
      HashMap<String, AnnotatedMember> result = null;
      Iterator i$ = this._classInfo.memberMethods().iterator();

      AnnotationIntrospector.ReferenceProperty prop;
      while(i$.hasNext()) {
         AnnotatedMethod am = (AnnotatedMethod)i$.next();
         if (am.getParameterCount() == 1) {
            prop = this._annotationIntrospector.findReferenceType(am);
            if (prop != null && prop.isBackReference()) {
               if (result == null) {
                  result = new HashMap();
               }

               if (result.put(prop.getName(), am) != null) {
                  throw new IllegalArgumentException("Multiple back-reference properties with name '" + prop.getName() + "'");
               }
            }
         }
      }

      i$ = this._classInfo.fields().iterator();

      while(i$.hasNext()) {
         AnnotatedField af = (AnnotatedField)i$.next();
         prop = this._annotationIntrospector.findReferenceType(af);
         if (prop != null && prop.isBackReference()) {
            if (result == null) {
               result = new HashMap();
            }

            if (result.put(prop.getName(), af) != null) {
               throw new IllegalArgumentException("Multiple back-reference properties with name '" + prop.getName() + "'");
            }
         }
      }

      return result;
   }

   public LinkedHashMap<String, AnnotatedField> findDeserializableFields(VisibilityChecker<?> vchecker, Collection<String> ignoredProperties) {
      return this._findPropertyFields(vchecker, ignoredProperties, false);
   }

   public String okNameForAnyGetter(AnnotatedMethod am, String name) {
      String str = this.okNameForIsGetter(am, name);
      if (str == null) {
         str = this.okNameForGetter(am, name);
      }

      return str;
   }

   public String okNameForGetter(AnnotatedMethod am, String name) {
      if (name.startsWith("get")) {
         if ("getCallbacks".equals(name)) {
            if (this.isCglibGetCallbacks(am)) {
               return null;
            }
         } else if ("getMetaClass".equals(name) && this.isGroovyMetaClassGetter(am)) {
            return null;
         }

         return this.mangleGetterName(am, name.substring(3));
      } else {
         return null;
      }
   }

   public String okNameForIsGetter(AnnotatedMethod am, String name) {
      if (name.startsWith("is")) {
         Class<?> rt = am.getRawType();
         return rt != Boolean.class && rt != Boolean.TYPE ? null : this.mangleGetterName(am, name.substring(2));
      } else {
         return null;
      }
   }

   protected String mangleGetterName(Annotated a, String basename) {
      return manglePropertyName(basename);
   }

   protected boolean isCglibGetCallbacks(AnnotatedMethod am) {
      Class<?> rt = am.getRawType();
      if (rt != null && rt.isArray()) {
         Class<?> compType = rt.getComponentType();
         Package pkg = compType.getPackage();
         if (pkg != null) {
            String pname = pkg.getName();
            if (pname.startsWith("net.sf.cglib") || pname.startsWith("org.hibernate.repackage.cglib")) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   protected boolean isGroovyMetaClassSetter(AnnotatedMethod am) {
      Class<?> argType = am.getParameterClass(0);
      Package pkg = argType.getPackage();
      return pkg != null && pkg.getName().startsWith("groovy.lang");
   }

   protected boolean isGroovyMetaClassGetter(AnnotatedMethod am) {
      Class<?> rt = am.getRawType();
      if (rt != null && !rt.isArray()) {
         Package pkg = rt.getPackage();
         return pkg != null && pkg.getName().startsWith("groovy.lang");
      } else {
         return false;
      }
   }

   public String okNameForSetter(AnnotatedMethod am) {
      String name = am.getName();
      if (name.startsWith("set")) {
         name = this.mangleSetterName(am, name.substring(3));
         if (name == null) {
            return null;
         } else {
            return "metaClass".equals(name) && this.isGroovyMetaClassSetter(am) ? null : name;
         }
      } else {
         return null;
      }
   }

   protected String mangleSetterName(Annotated a, String basename) {
      return manglePropertyName(basename);
   }

   public LinkedHashMap<String, AnnotatedField> _findPropertyFields(VisibilityChecker<?> vchecker, Collection<String> ignoredProperties, boolean forSerialization) {
      LinkedHashMap<String, AnnotatedField> results = new LinkedHashMap();
      PropertyNamingStrategy naming = this._config.getPropertyNamingStrategy();
      Iterator i$ = this._classInfo.fields().iterator();

      AnnotatedField af;
      String propName;
      AnnotatedField old;
      do {
         label45:
         do {
            do {
               if (!i$.hasNext()) {
                  return results;
               }

               af = (AnnotatedField)i$.next();
               propName = forSerialization ? this._annotationIntrospector.findSerializablePropertyName(af) : this._annotationIntrospector.findDeserializablePropertyName(af);
               if (propName != null) {
                  if (propName.length() == 0) {
                     propName = af.getName();
                     if (naming != null) {
                        propName = naming.nameForField(this._config, af, propName);
                     }
                  }
                  continue label45;
               }
            } while(!vchecker.isFieldVisible(af));

            propName = af.getName();
            if (naming != null) {
               propName = naming.nameForField(this._config, af, propName);
            }
         } while(ignoredProperties != null && ignoredProperties.contains(propName));

         old = (AnnotatedField)results.put(propName, af);
      } while(old == null || old.getDeclaringClass() != af.getDeclaringClass());

      String oldDesc = old.getFullName();
      String newDesc = af.getFullName();
      throw new IllegalArgumentException("Multiple fields representing property \"" + propName + "\": " + oldDesc + " vs " + newDesc);
   }

   public static String manglePropertyName(String basename) {
      int len = basename.length();
      if (len == 0) {
         return null;
      } else {
         StringBuilder sb = null;

         for(int i = 0; i < len; ++i) {
            char upper = basename.charAt(i);
            char lower = Character.toLowerCase(upper);
            if (upper == lower) {
               break;
            }

            if (sb == null) {
               sb = new StringBuilder(basename);
            }

            sb.setCharAt(i, lower);
         }

         return sb == null ? basename : sb.toString();
      }
   }

   public static String descFor(AnnotatedElement elem) {
      if (elem instanceof Class) {
         return "class " + ((Class)elem).getName();
      } else if (elem instanceof Method) {
         Method m = (Method)elem;
         return "method " + m.getName() + " (from class " + m.getDeclaringClass().getName() + ")";
      } else if (elem instanceof Constructor) {
         Constructor<?> ctor = (Constructor)elem;
         return "constructor() (from class " + ctor.getDeclaringClass().getName() + ")";
      } else {
         return "unknown type [" + elem.getClass() + "]";
      }
   }
}
