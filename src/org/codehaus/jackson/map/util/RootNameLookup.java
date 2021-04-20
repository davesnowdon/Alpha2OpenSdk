package org.codehaus.jackson.map.util;

import org.codehaus.jackson.io.SerializedString;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.type.ClassKey;
import org.codehaus.jackson.type.JavaType;

public class RootNameLookup {
   protected LRUMap<ClassKey, SerializedString> _rootNames;

   public RootNameLookup() {
   }

   public SerializedString findRootName(JavaType rootType, MapperConfig<?> config) {
      return this.findRootName(rootType.getRawClass(), config);
   }

   public synchronized SerializedString findRootName(Class<?> rootType, MapperConfig<?> config) {
      ClassKey key = new ClassKey(rootType);
      if (this._rootNames == null) {
         this._rootNames = new LRUMap(20, 200);
      } else {
         SerializedString name = (SerializedString)this._rootNames.get(key);
         if (name != null) {
            return name;
         }
      }

      BasicBeanDescription beanDesc = (BasicBeanDescription)config.introspectClassAnnotations(rootType);
      AnnotationIntrospector intr = config.getAnnotationIntrospector();
      AnnotatedClass ac = beanDesc.getClassInfo();
      String nameStr = intr.findRootName(ac);
      if (nameStr == null) {
         nameStr = rootType.getSimpleName();
      }

      SerializedString name = new SerializedString(nameStr);
      this._rootNames.put(key, name);
      return name;
   }
}
