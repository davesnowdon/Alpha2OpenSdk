package org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import org.codehaus.jackson.map.util.Annotations;

public final class AnnotationMap implements Annotations {
   protected HashMap<Class<? extends Annotation>, Annotation> _annotations;

   public AnnotationMap() {
   }

   public <A extends Annotation> A get(Class<A> cls) {
      return this._annotations == null ? null : (Annotation)this._annotations.get(cls);
   }

   public int size() {
      return this._annotations == null ? 0 : this._annotations.size();
   }

   public void addIfNotPresent(Annotation ann) {
      if (this._annotations == null || !this._annotations.containsKey(ann.annotationType())) {
         this._add(ann);
      }

   }

   public void add(Annotation ann) {
      this._add(ann);
   }

   public String toString() {
      return this._annotations == null ? "[null]" : this._annotations.toString();
   }

   protected final void _add(Annotation ann) {
      if (this._annotations == null) {
         this._annotations = new HashMap();
      }

      this._annotations.put(ann.annotationType(), ann);
   }
}
