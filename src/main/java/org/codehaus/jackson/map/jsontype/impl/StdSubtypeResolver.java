package org.codehaus.jackson.map.jsontype.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.SubtypeResolver;

public class StdSubtypeResolver extends SubtypeResolver {
   protected LinkedHashSet<NamedType> _registeredSubtypes;

   public StdSubtypeResolver() {
   }

   public void registerSubtypes(NamedType... types) {
      if (this._registeredSubtypes == null) {
         this._registeredSubtypes = new LinkedHashSet();
      }

      NamedType[] arr$ = types;
      int len$ = types.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         NamedType type = arr$[i$];
         this._registeredSubtypes.add(type);
      }

   }

   public void registerSubtypes(Class<?>... classes) {
      NamedType[] types = new NamedType[classes.length];
      int i = 0;

      for(int len = classes.length; i < len; ++i) {
         types[i] = new NamedType(classes[i]);
      }

      this.registerSubtypes(types);
   }

   public Collection<NamedType> collectAndResolveSubtypes(AnnotatedMember property, MapperConfig<?> config, AnnotationIntrospector ai) {
      HashMap<NamedType, NamedType> collected = new HashMap();
      Iterator i$;
      NamedType nt;
      AnnotatedClass ac;
      if (this._registeredSubtypes != null) {
         Class<?> rawBase = property.getRawType();
         i$ = this._registeredSubtypes.iterator();

         while(i$.hasNext()) {
            nt = (NamedType)i$.next();
            if (rawBase.isAssignableFrom(nt.getType())) {
               ac = AnnotatedClass.constructWithoutSuperTypes(nt.getType(), ai, config);
               this._collectAndResolve(ac, nt, config, ai, collected);
            }
         }
      }

      Collection<NamedType> st = ai.findSubtypes(property);
      if (st != null) {
         i$ = st.iterator();

         while(i$.hasNext()) {
            nt = (NamedType)i$.next();
            ac = AnnotatedClass.constructWithoutSuperTypes(nt.getType(), ai, config);
            this._collectAndResolve(ac, nt, config, ai, collected);
         }
      }

      NamedType rootType = new NamedType(property.getRawType(), (String)null);
      AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(property.getRawType(), ai, config);
      this._collectAndResolve(ac, rootType, config, ai, collected);
      return new ArrayList(collected.values());
   }

   public Collection<NamedType> collectAndResolveSubtypes(AnnotatedClass type, MapperConfig<?> config, AnnotationIntrospector ai) {
      HashMap<NamedType, NamedType> subtypes = new HashMap();
      if (this._registeredSubtypes != null) {
         Class<?> rawBase = type.getRawType();
         Iterator i$ = this._registeredSubtypes.iterator();

         while(i$.hasNext()) {
            NamedType subtype = (NamedType)i$.next();
            if (rawBase.isAssignableFrom(subtype.getType())) {
               AnnotatedClass curr = AnnotatedClass.constructWithoutSuperTypes(subtype.getType(), ai, config);
               this._collectAndResolve(curr, subtype, config, ai, subtypes);
            }
         }
      }

      NamedType rootType = new NamedType(type.getRawType(), (String)null);
      this._collectAndResolve(type, rootType, config, ai, subtypes);
      return new ArrayList(subtypes.values());
   }

   protected void _collectAndResolve(AnnotatedClass annotatedType, NamedType namedType, MapperConfig<?> config, AnnotationIntrospector ai, HashMap<NamedType, NamedType> collectedSubtypes) {
      if (!namedType.hasName()) {
         String name = ai.findTypeName(annotatedType);
         if (name != null) {
            namedType = new NamedType(namedType.getType(), name);
         }
      }

      if (collectedSubtypes.containsKey(namedType)) {
         if (namedType.hasName()) {
            NamedType prev = (NamedType)collectedSubtypes.get(namedType);
            if (!prev.hasName()) {
               collectedSubtypes.put(namedType, namedType);
            }
         }

      } else {
         collectedSubtypes.put(namedType, namedType);
         Collection<NamedType> st = ai.findSubtypes(annotatedType);
         NamedType subtype;
         AnnotatedClass subtypeClass;
         if (st != null && !st.isEmpty()) {
            for(Iterator i$ = st.iterator(); i$.hasNext(); this._collectAndResolve(subtypeClass, subtype, config, ai, collectedSubtypes)) {
               subtype = (NamedType)i$.next();
               subtypeClass = AnnotatedClass.constructWithoutSuperTypes(subtype.getType(), ai, config);
               if (!subtype.hasName()) {
                  subtype = new NamedType(subtype.getType(), ai.findTypeName(subtypeClass));
               }
            }
         }

      }
   }
}
