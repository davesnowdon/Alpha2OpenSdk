package org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

public abstract class AnnotatedWithParams extends AnnotatedMember {
   protected final AnnotationMap _annotations;
   protected final AnnotationMap[] _paramAnnotations;

   protected AnnotatedWithParams(AnnotationMap classAnn, AnnotationMap[] paramAnnotations) {
      this._annotations = classAnn;
      this._paramAnnotations = paramAnnotations;
   }

   public final void addOrOverride(Annotation a) {
      this._annotations.add(a);
   }

   public final void addOrOverrideParam(int paramIndex, Annotation a) {
      AnnotationMap old = this._paramAnnotations[paramIndex];
      if (old == null) {
         old = new AnnotationMap();
         this._paramAnnotations[paramIndex] = old;
      }

      old.add(a);
   }

   public final void addIfNotPresent(Annotation a) {
      this._annotations.addIfNotPresent(a);
   }

   protected JavaType getType(TypeBindings bindings, TypeVariable<?>[] typeParams) {
      if (typeParams != null && typeParams.length > 0) {
         bindings = bindings.childInstance();
         TypeVariable[] arr$ = typeParams;
         int len$ = typeParams.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            TypeVariable<?> var = arr$[i$];
            String name = var.getName();
            bindings._addPlaceholder(name);
            Type lowerBound = var.getBounds()[0];
            JavaType type = lowerBound == null ? TypeFactory.unknownType() : bindings.resolveType(lowerBound);
            bindings.addBinding(var.getName(), type);
         }
      }

      return bindings.resolveType(this.getGenericType());
   }

   public final <A extends Annotation> A getAnnotation(Class<A> acls) {
      return this._annotations.get(acls);
   }

   public final AnnotationMap getParameterAnnotations(int index) {
      return this._paramAnnotations != null && index >= 0 && index <= this._paramAnnotations.length ? this._paramAnnotations[index] : null;
   }

   public abstract AnnotatedParameter getParameter(int var1);

   public abstract int getParameterCount();

   public abstract Class<?> getParameterClass(int var1);

   public abstract Type getParameterType(int var1);

   public final JavaType resolveParameterType(int index, TypeBindings bindings) {
      return bindings.resolveType(this.getParameterType(index));
   }

   public final int getAnnotationCount() {
      return this._annotations.size();
   }
}
