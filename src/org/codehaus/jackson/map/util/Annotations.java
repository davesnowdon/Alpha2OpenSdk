package org.codehaus.jackson.map.util;

import java.lang.annotation.Annotation;

public interface Annotations {
   <A extends Annotation> A get(Class<A> var1);

   int size();
}
