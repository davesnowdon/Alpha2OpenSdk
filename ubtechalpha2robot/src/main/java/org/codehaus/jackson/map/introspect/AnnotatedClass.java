package org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ClassIntrospector;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.util.ClassUtil;

public final class AnnotatedClass extends Annotated {
   private static final AnnotationMap[] NO_ANNOTATION_MAPS = new AnnotationMap[0];
   protected final Class<?> _class;
   protected final Collection<Class<?>> _superTypes;
   protected final AnnotationIntrospector _annotationIntrospector;
   protected final ClassIntrospector.MixInResolver _mixInResolver;
   protected final Class<?> _primaryMixIn;
   protected AnnotationMap _classAnnotations;
   protected AnnotatedConstructor _defaultConstructor;
   protected List<AnnotatedConstructor> _constructors;
   protected List<AnnotatedMethod> _creatorMethods;
   protected AnnotatedMethodMap _memberMethods;
   protected List<AnnotatedField> _fields;
   protected List<AnnotatedMethod> _ignoredMethods;
   protected List<AnnotatedField> _ignoredFields;

   private AnnotatedClass(Class<?> cls, List<Class<?>> superTypes, AnnotationIntrospector aintr, ClassIntrospector.MixInResolver mir) {
      this._class = cls;
      this._superTypes = superTypes;
      this._annotationIntrospector = aintr;
      this._mixInResolver = mir;
      this._primaryMixIn = this._mixInResolver == null ? null : this._mixInResolver.findMixInClassFor(this._class);
   }

   public static AnnotatedClass construct(Class<?> cls, AnnotationIntrospector aintr, ClassIntrospector.MixInResolver mir) {
      List<Class<?>> st = ClassUtil.findSuperTypes(cls, (Class)null);
      AnnotatedClass ac = new AnnotatedClass(cls, st, aintr, mir);
      ac.resolveClassAnnotations();
      return ac;
   }

   public static AnnotatedClass constructWithoutSuperTypes(Class<?> cls, AnnotationIntrospector aintr, ClassIntrospector.MixInResolver mir) {
      List<Class<?>> empty = Collections.emptyList();
      AnnotatedClass ac = new AnnotatedClass(cls, empty, aintr, mir);
      ac.resolveClassAnnotations();
      return ac;
   }

   public Class<?> getAnnotated() {
      return this._class;
   }

   public int getModifiers() {
      return this._class.getModifiers();
   }

   public String getName() {
      return this._class.getName();
   }

   public <A extends Annotation> A getAnnotation(Class<A> acls) {
      return this._classAnnotations == null ? null : this._classAnnotations.get(acls);
   }

   public Type getGenericType() {
      return this._class;
   }

   public Class<?> getRawType() {
      return this._class;
   }

   public Annotations getAnnotations() {
      return this._classAnnotations;
   }

   public boolean hasAnnotations() {
      return this._classAnnotations.size() > 0;
   }

   public AnnotatedConstructor getDefaultConstructor() {
      return this._defaultConstructor;
   }

   public List<AnnotatedConstructor> getConstructors() {
      return this._constructors == null ? Collections.emptyList() : this._constructors;
   }

   public List<AnnotatedMethod> getStaticMethods() {
      return this._creatorMethods == null ? Collections.emptyList() : this._creatorMethods;
   }

   public Iterable<AnnotatedMethod> memberMethods() {
      return this._memberMethods;
   }

   public Iterable<AnnotatedMethod> ignoredMemberMethods() {
      if (this._ignoredMethods == null) {
         List<AnnotatedMethod> l = Collections.emptyList();
         return l;
      } else {
         return this._ignoredMethods;
      }
   }

   public int getMemberMethodCount() {
      return this._memberMethods.size();
   }

   public AnnotatedMethod findMethod(String name, Class<?>[] paramTypes) {
      return this._memberMethods.find(name, paramTypes);
   }

   public int getFieldCount() {
      return this._fields == null ? 0 : this._fields.size();
   }

   public Iterable<AnnotatedField> fields() {
      if (this._fields == null) {
         List<AnnotatedField> l = Collections.emptyList();
         return l;
      } else {
         return this._fields;
      }
   }

   public Iterable<AnnotatedField> ignoredFields() {
      if (this._ignoredFields == null) {
         List<AnnotatedField> l = Collections.emptyList();
         return l;
      } else {
         return this._ignoredFields;
      }
   }

   protected void resolveClassAnnotations() {
      this._classAnnotations = new AnnotationMap();
      if (this._primaryMixIn != null) {
         this._addClassMixIns(this._classAnnotations, this._class, this._primaryMixIn);
      }

      Annotation[] arr$ = this._class.getDeclaredAnnotations();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Annotation a = arr$[i$];
         if (this._annotationIntrospector.isHandled(a)) {
            this._classAnnotations.addIfNotPresent(a);
         }
      }

      Iterator i$ = this._superTypes.iterator();

      while(i$.hasNext()) {
         Class<?> cls = (Class)i$.next();
         this._addClassMixIns(this._classAnnotations, cls);
         Annotation[] arr$ = cls.getDeclaredAnnotations();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Annotation a = arr$[i$];
            if (this._annotationIntrospector.isHandled(a)) {
               this._classAnnotations.addIfNotPresent(a);
            }
         }
      }

      this._addClassMixIns(this._classAnnotations, Object.class);
   }

   protected void _addClassMixIns(AnnotationMap annotations, Class<?> toMask) {
      if (this._mixInResolver != null) {
         this._addClassMixIns(annotations, toMask, this._mixInResolver.findMixInClassFor(toMask));
      }

   }

   protected void _addClassMixIns(AnnotationMap annotations, Class<?> toMask, Class<?> mixin) {
      if (mixin != null) {
         Annotation[] arr$ = mixin.getDeclaredAnnotations();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Annotation a = arr$[i$];
            if (this._annotationIntrospector.isHandled(a)) {
               annotations.addIfNotPresent(a);
            }
         }

         Iterator i$ = ClassUtil.findSuperTypes(mixin, toMask).iterator();

         while(i$.hasNext()) {
            Class<?> parent = (Class)i$.next();
            Annotation[] arr$ = parent.getDeclaredAnnotations();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Annotation a = arr$[i$];
               if (this._annotationIntrospector.isHandled(a)) {
                  annotations.addIfNotPresent(a);
               }
            }
         }

      }
   }

   public void resolveCreators(boolean includeAll) {
      this._constructors = null;
      Constructor[] arr$ = this._class.getDeclaredConstructors();
      int len$ = arr$.length;

      int i$;
      for(i$ = 0; i$ < len$; ++i$) {
         Constructor<?> ctor = arr$[i$];
         switch(ctor.getParameterTypes().length) {
         case 0:
            this._defaultConstructor = this._constructConstructor(ctor, true);
            break;
         default:
            if (includeAll) {
               if (this._constructors == null) {
                  this._constructors = new ArrayList();
               }

               this._constructors.add(this._constructConstructor(ctor, false));
            }
         }
      }

      if (this._primaryMixIn != null && (this._defaultConstructor != null || this._constructors != null)) {
         this._addConstructorMixIns(this._primaryMixIn);
      }

      int i;
      if (this._annotationIntrospector != null) {
         if (this._defaultConstructor != null && this._annotationIntrospector.isIgnorableConstructor(this._defaultConstructor)) {
            this._defaultConstructor = null;
         }

         if (this._constructors != null) {
            i = this._constructors.size();

            while(true) {
               --i;
               if (i < 0) {
                  break;
               }

               if (this._annotationIntrospector.isIgnorableConstructor((AnnotatedConstructor)this._constructors.get(i))) {
                  this._constructors.remove(i);
               }
            }
         }
      }

      this._creatorMethods = null;
      if (includeAll) {
         Method[] arr$ = this._class.getDeclaredMethods();
         len$ = arr$.length;

         for(i$ = 0; i$ < len$; ++i$) {
            Method m = arr$[i$];
            if (Modifier.isStatic(m.getModifiers())) {
               int argCount = m.getParameterTypes().length;
               if (argCount >= 1) {
                  if (this._creatorMethods == null) {
                     this._creatorMethods = new ArrayList();
                  }

                  this._creatorMethods.add(this._constructCreatorMethod(m));
               }
            }
         }

         if (this._primaryMixIn != null && this._creatorMethods != null) {
            this._addFactoryMixIns(this._primaryMixIn);
         }

         if (this._annotationIntrospector != null && this._creatorMethods != null) {
            i = this._creatorMethods.size();

            while(true) {
               --i;
               if (i < 0) {
                  break;
               }

               if (this._annotationIntrospector.isIgnorableMethod((AnnotatedMethod)this._creatorMethods.get(i))) {
                  this._creatorMethods.remove(i);
               }
            }
         }
      }

   }

   protected void _addConstructorMixIns(Class<?> mixin) {
      MemberKey[] ctorKeys = null;
      int ctorCount = this._constructors == null ? 0 : this._constructors.size();
      Constructor[] arr$ = mixin.getDeclaredConstructors();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Constructor<?> ctor = arr$[i$];
         switch(ctor.getParameterTypes().length) {
         case 0:
            if (this._defaultConstructor != null) {
               this._addMixOvers(ctor, this._defaultConstructor, false);
            }
            continue;
         }

         if (ctorKeys == null) {
            ctorKeys = new MemberKey[ctorCount];

            for(int i = 0; i < ctorCount; ++i) {
               ctorKeys[i] = new MemberKey(((AnnotatedConstructor)this._constructors.get(i)).getAnnotated());
            }
         }

         MemberKey key = new MemberKey(ctor);

         for(int i = 0; i < ctorCount; ++i) {
            if (key.equals(ctorKeys[i])) {
               this._addMixOvers(ctor, (AnnotatedConstructor)this._constructors.get(i), true);
               break;
            }
         }
      }

   }

   protected void _addFactoryMixIns(Class<?> mixin) {
      MemberKey[] methodKeys = null;
      int methodCount = this._creatorMethods.size();
      Method[] arr$ = mixin.getDeclaredMethods();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Method m = arr$[i$];
         if (Modifier.isStatic(m.getModifiers()) && m.getParameterTypes().length != 0) {
            if (methodKeys == null) {
               methodKeys = new MemberKey[methodCount];

               for(int i = 0; i < methodCount; ++i) {
                  methodKeys[i] = new MemberKey(((AnnotatedMethod)this._creatorMethods.get(i)).getAnnotated());
               }
            }

            MemberKey key = new MemberKey(m);

            for(int i = 0; i < methodCount; ++i) {
               if (key.equals(methodKeys[i])) {
                  this._addMixOvers(m, (AnnotatedMethod)this._creatorMethods.get(i), true);
                  break;
               }
            }
         }
      }

   }

   public void resolveMemberMethods(MethodFilter methodFilter, boolean collectIgnored) {
      this._memberMethods = new AnnotatedMethodMap();
      AnnotatedMethodMap mixins = new AnnotatedMethodMap();
      this._addMemberMethods(this._class, methodFilter, this._memberMethods, this._primaryMixIn, mixins);
      Iterator it = this._superTypes.iterator();

      while(it.hasNext()) {
         Class<?> cls = (Class)it.next();
         Class<?> mixin = this._mixInResolver == null ? null : this._mixInResolver.findMixInClassFor(cls);
         this._addMemberMethods(cls, methodFilter, this._memberMethods, mixin, mixins);
      }

      if (this._mixInResolver != null) {
         Class<?> mixin = this._mixInResolver.findMixInClassFor(Object.class);
         if (mixin != null) {
            this._addMethodMixIns(methodFilter, this._memberMethods, mixin, mixins);
         }
      }

      if (this._annotationIntrospector != null) {
         AnnotatedMethod am;
         if (!mixins.isEmpty()) {
            it = mixins.iterator();

            while(it.hasNext()) {
               am = (AnnotatedMethod)it.next();

               try {
                  Method m = Object.class.getDeclaredMethod(am.getName(), am.getParameterClasses());
                  if (m != null) {
                     AnnotatedMethod am = this._constructMethod(m);
                     this._addMixOvers(am.getAnnotated(), am, false);
                     this._memberMethods.add(am);
                  }
               } catch (Exception var8) {
               }
            }
         }

         it = this._memberMethods.iterator();

         while(it.hasNext()) {
            am = (AnnotatedMethod)it.next();
            if (this._annotationIntrospector.isIgnorableMethod(am)) {
               it.remove();
               if (collectIgnored) {
                  this._ignoredMethods = ArrayBuilders.addToList(this._ignoredMethods, am);
               }
            }
         }
      }

   }

   protected void _addMemberMethods(Class<?> cls, MethodFilter methodFilter, AnnotatedMethodMap methods, Class<?> mixInCls, AnnotatedMethodMap mixIns) {
      if (mixInCls != null) {
         this._addMethodMixIns(methodFilter, methods, mixInCls, mixIns);
      }

      if (cls != null) {
         Method[] arr$ = cls.getDeclaredMethods();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Method m = arr$[i$];
            if (this._isIncludableMethod(m, methodFilter)) {
               AnnotatedMethod old = methods.find(m);
               if (old == null) {
                  AnnotatedMethod newM = this._constructMethod(m);
                  methods.add(newM);
                  old = mixIns.remove(m);
                  if (old != null) {
                     this._addMixOvers(old.getAnnotated(), newM, false);
                  }
               } else {
                  this._addMixUnders(m, old);
                  if (old.getDeclaringClass().isInterface() && !m.getDeclaringClass().isInterface()) {
                     methods.add(old.withMethod(m));
                  }
               }
            }
         }

      }
   }

   protected void _addMethodMixIns(MethodFilter methodFilter, AnnotatedMethodMap methods, Class<?> mixInCls, AnnotatedMethodMap mixIns) {
      Method[] arr$ = mixInCls.getDeclaredMethods();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Method m = arr$[i$];
         if (this._isIncludableMethod(m, methodFilter)) {
            AnnotatedMethod am = methods.find(m);
            if (am != null) {
               this._addMixUnders(m, am);
            } else {
               mixIns.add(this._constructMethod(m));
            }
         }
      }

   }

   public void resolveFields(boolean collectIgnored) {
      LinkedHashMap<String, AnnotatedField> foundFields = new LinkedHashMap();
      this._addFields(foundFields, this._class);
      if (this._annotationIntrospector != null) {
         Iterator it = foundFields.entrySet().iterator();

         while(it.hasNext()) {
            AnnotatedField f = (AnnotatedField)((Entry)it.next()).getValue();
            if (this._annotationIntrospector.isIgnorableField(f)) {
               it.remove();
               if (collectIgnored) {
                  this._ignoredFields = ArrayBuilders.addToList(this._ignoredFields, f);
               }
            }
         }
      }

      if (foundFields.isEmpty()) {
         this._fields = Collections.emptyList();
      } else {
         this._fields = new ArrayList(foundFields.size());
         this._fields.addAll(foundFields.values());
      }

   }

   protected void _addFields(Map<String, AnnotatedField> fields, Class<?> c) {
      Class<?> parent = c.getSuperclass();
      if (parent != null) {
         this._addFields(fields, parent);
         Field[] arr$ = c.getDeclaredFields();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Field f = arr$[i$];
            if (this._isIncludableField(f)) {
               fields.put(f.getName(), this._constructField(f));
            }
         }

         if (this._mixInResolver != null) {
            Class<?> mixin = this._mixInResolver.findMixInClassFor(c);
            if (mixin != null) {
               this._addFieldMixIns(mixin, fields);
            }
         }
      }

   }

   protected void _addFieldMixIns(Class<?> mixin, Map<String, AnnotatedField> fields) {
      Field[] arr$ = mixin.getDeclaredFields();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Field mixinField = arr$[i$];
         if (this._isIncludableField(mixinField)) {
            String name = mixinField.getName();
            AnnotatedField maskedField = (AnnotatedField)fields.get(name);
            if (maskedField != null) {
               Annotation[] arr$ = mixinField.getDeclaredAnnotations();
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  Annotation a = arr$[i$];
                  if (this._annotationIntrospector.isHandled(a)) {
                     maskedField.addOrOverride(a);
                  }
               }
            }
         }
      }

   }

   protected AnnotatedMethod _constructMethod(Method m) {
      return this._annotationIntrospector == null ? new AnnotatedMethod(m, this._emptyAnnotationMap(), (AnnotationMap[])null) : new AnnotatedMethod(m, this._collectRelevantAnnotations(m.getDeclaredAnnotations()), (AnnotationMap[])null);
   }

   protected AnnotatedConstructor _constructConstructor(Constructor<?> ctor, boolean defaultCtor) {
      return this._annotationIntrospector == null ? new AnnotatedConstructor(ctor, this._emptyAnnotationMap(), this._emptyAnnotationMaps(ctor.getParameterTypes().length)) : new AnnotatedConstructor(ctor, this._collectRelevantAnnotations(ctor.getDeclaredAnnotations()), defaultCtor ? null : this._collectRelevantAnnotations(ctor.getParameterAnnotations()));
   }

   protected AnnotatedMethod _constructCreatorMethod(Method m) {
      return this._annotationIntrospector == null ? new AnnotatedMethod(m, this._emptyAnnotationMap(), this._emptyAnnotationMaps(m.getParameterTypes().length)) : new AnnotatedMethod(m, this._collectRelevantAnnotations(m.getDeclaredAnnotations()), this._collectRelevantAnnotations(m.getParameterAnnotations()));
   }

   protected AnnotatedField _constructField(Field f) {
      return this._annotationIntrospector == null ? new AnnotatedField(f, this._emptyAnnotationMap()) : new AnnotatedField(f, this._collectRelevantAnnotations(f.getDeclaredAnnotations()));
   }

   protected AnnotationMap[] _collectRelevantAnnotations(Annotation[][] anns) {
      int len = anns.length;
      AnnotationMap[] result = new AnnotationMap[len];

      for(int i = 0; i < len; ++i) {
         result[i] = this._collectRelevantAnnotations(anns[i]);
      }

      return result;
   }

   protected AnnotationMap _collectRelevantAnnotations(Annotation[] anns) {
      AnnotationMap annMap = new AnnotationMap();
      if (anns != null) {
         Annotation[] arr$ = anns;
         int len$ = anns.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Annotation a = arr$[i$];
            if (this._annotationIntrospector.isHandled(a)) {
               annMap.add(a);
            }
         }
      }

      return annMap;
   }

   private AnnotationMap _emptyAnnotationMap() {
      return new AnnotationMap();
   }

   private AnnotationMap[] _emptyAnnotationMaps(int count) {
      if (count == 0) {
         return NO_ANNOTATION_MAPS;
      } else {
         AnnotationMap[] maps = new AnnotationMap[count];

         for(int i = 0; i < count; ++i) {
            maps[i] = this._emptyAnnotationMap();
         }

         return maps;
      }
   }

   protected boolean _isIncludableMethod(Method m, MethodFilter filter) {
      if (filter != null && !filter.includeMethod(m)) {
         return false;
      } else {
         return !m.isSynthetic() && !m.isBridge();
      }
   }

   private boolean _isIncludableField(Field f) {
      if (f.isSynthetic()) {
         return false;
      } else {
         int mods = f.getModifiers();
         return !Modifier.isStatic(mods) && !Modifier.isTransient(mods);
      }
   }

   protected void _addMixOvers(Constructor<?> mixin, AnnotatedConstructor target, boolean addParamAnnotations) {
      Annotation[] arr$ = mixin.getDeclaredAnnotations();
      int i = arr$.length;

      int len;
      for(len = 0; len < i; ++len) {
         Annotation a = arr$[len];
         if (this._annotationIntrospector.isHandled(a)) {
            target.addOrOverride(a);
         }
      }

      if (addParamAnnotations) {
         Annotation[][] pa = mixin.getParameterAnnotations();
         i = 0;

         for(len = pa.length; i < len; ++i) {
            Annotation[] arr$ = pa[i];
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Annotation a = arr$[i$];
               target.addOrOverrideParam(i, a);
            }
         }
      }

   }

   protected void _addMixOvers(Method mixin, AnnotatedMethod target, boolean addParamAnnotations) {
      Annotation[] arr$ = mixin.getDeclaredAnnotations();
      int i = arr$.length;

      int len;
      for(len = 0; len < i; ++len) {
         Annotation a = arr$[len];
         if (this._annotationIntrospector.isHandled(a)) {
            target.addOrOverride(a);
         }
      }

      if (addParamAnnotations) {
         Annotation[][] pa = mixin.getParameterAnnotations();
         i = 0;

         for(len = pa.length; i < len; ++i) {
            Annotation[] arr$ = pa[i];
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Annotation a = arr$[i$];
               target.addOrOverrideParam(i, a);
            }
         }
      }

   }

   protected void _addMixUnders(Method src, AnnotatedMethod target) {
      Annotation[] arr$ = src.getDeclaredAnnotations();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Annotation a = arr$[i$];
         if (this._annotationIntrospector.isHandled(a)) {
            target.addIfNotPresent(a);
         }
      }

   }

   public String toString() {
      return "[AnnotedClass " + this._class.getName() + "]";
   }
}
