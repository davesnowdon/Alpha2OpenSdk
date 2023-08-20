package org.msgpack.template.builder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.NotNullable;
import org.msgpack.annotation.Optional;
import org.msgpack.packer.Packer;
import org.msgpack.template.FieldOption;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;
import org.msgpack.template.builder.beans.BeanInfo;
import org.msgpack.template.builder.beans.IntrospectionException;
import org.msgpack.template.builder.beans.Introspector;
import org.msgpack.template.builder.beans.PropertyDescriptor;
import org.msgpack.unpacker.Unpacker;

public class ReflectionBeansTemplateBuilder extends ReflectionTemplateBuilder {
   private static Logger LOG = Logger.getLogger(ReflectionBeansTemplateBuilder.class.getName());

   public ReflectionBeansTemplateBuilder(TemplateRegistry registry) {
      super(registry, (ClassLoader)null);
   }

   public boolean matchType(Type targetType, boolean hasAnnotation) {
      Class<?> targetClass = (Class)targetType;
      boolean matched = matchAtBeansClassTemplateBuilder(targetClass, hasAnnotation);
      if (matched && LOG.isLoggable(Level.FINE)) {
         LOG.fine("matched type: " + targetClass.getName());
      }

      return matched;
   }

   protected ReflectionTemplateBuilder.ReflectionFieldTemplate[] toTemplates(FieldEntry[] entries) {
      ReflectionTemplateBuilder.ReflectionFieldTemplate[] tmpls = new ReflectionTemplateBuilder.ReflectionFieldTemplate[entries.length];

      for(int i = 0; i < entries.length; ++i) {
         FieldEntry e = entries[i];
         Class<?> type = e.getType();
         if (type.isPrimitive()) {
            tmpls[i] = new ReflectionBeansTemplateBuilder.ReflectionBeansFieldTemplate(e);
         } else {
            Template tmpl = this.registry.lookup(e.getGenericType());
            tmpls[i] = new ReflectionTemplateBuilder.FieldTemplateImpl(e, tmpl);
         }
      }

      return tmpls;
   }

   public FieldEntry[] toFieldEntries(Class<?> targetClass, FieldOption implicitOption) {
      BeanInfo desc;
      try {
         desc = Introspector.getBeanInfo(targetClass);
      } catch (IntrospectionException var11) {
         throw new TemplateBuildException("Class must be java beans class:" + targetClass.getName());
      }

      PropertyDescriptor[] props = desc.getPropertyDescriptors();
      ArrayList<PropertyDescriptor> list = new ArrayList();

      for(int i = 0; i < props.length; ++i) {
         PropertyDescriptor pd = props[i];
         if (!this.isIgnoreProperty(pd)) {
            list.add(pd);
         }
      }

      props = new PropertyDescriptor[list.size()];
      list.toArray(props);
      BeansFieldEntry[] entries = new BeansFieldEntry[props.length];

      int insertIndex;
      for(insertIndex = 0; insertIndex < props.length; ++insertIndex) {
         PropertyDescriptor p = props[insertIndex];
         int index = this.getPropertyIndex(p);
         if (index >= 0) {
            if (entries[index] != null) {
               throw new TemplateBuildException("duplicated index: " + index);
            }

            if (index >= entries.length) {
               throw new TemplateBuildException("invalid index: " + index);
            }

            entries[index] = new BeansFieldEntry(p);
            props[index] = null;
         }
      }

      insertIndex = 0;

      int i;
      for(i = 0; i < props.length; ++i) {
         PropertyDescriptor p = props[i];
         if (p != null) {
            while(entries[insertIndex] != null) {
               ++insertIndex;
            }

            entries[insertIndex] = new BeansFieldEntry(p);
         }
      }

      for(i = 0; i < entries.length; ++i) {
         BeansFieldEntry e = entries[i];
         FieldOption op = this.getPropertyOption(e, implicitOption);
         e.setOption(op);
      }

      return entries;
   }

   private FieldOption getPropertyOption(BeansFieldEntry e, FieldOption implicitOption) {
      FieldOption forGetter = this.getMethodOption(e.getPropertyDescriptor().getReadMethod());
      if (forGetter != FieldOption.DEFAULT) {
         return forGetter;
      } else {
         FieldOption forSetter = this.getMethodOption(e.getPropertyDescriptor().getWriteMethod());
         return forSetter != FieldOption.DEFAULT ? forSetter : implicitOption;
      }
   }

   private FieldOption getMethodOption(Method method) {
      if (isAnnotated(method, Ignore.class)) {
         return FieldOption.IGNORE;
      } else if (isAnnotated(method, Optional.class)) {
         return FieldOption.OPTIONAL;
      } else {
         return isAnnotated(method, NotNullable.class) ? FieldOption.NOTNULLABLE : FieldOption.DEFAULT;
      }
   }

   private int getPropertyIndex(PropertyDescriptor desc) {
      int getterIndex = this.getMethodIndex(desc.getReadMethod());
      if (getterIndex >= 0) {
         return getterIndex;
      } else {
         int setterIndex = this.getMethodIndex(desc.getWriteMethod());
         return setterIndex;
      }
   }

   private int getMethodIndex(Method method) {
      Index a = (Index)method.getAnnotation(Index.class);
      return a == null ? -1 : a.value();
   }

   private boolean isIgnoreProperty(PropertyDescriptor desc) {
      if (desc == null) {
         return true;
      } else {
         Method getter = desc.getReadMethod();
         Method setter = desc.getWriteMethod();
         return getter == null || setter == null || !Modifier.isPublic(getter.getModifiers()) || !Modifier.isPublic(setter.getModifiers()) || isAnnotated(getter, Ignore.class) || isAnnotated(setter, Ignore.class);
      }
   }

   static class ReflectionBeansFieldTemplate extends ReflectionTemplateBuilder.ReflectionFieldTemplate {
      ReflectionBeansFieldTemplate(FieldEntry entry) {
         super(entry);
      }

      public void write(Packer packer, Object v, boolean required) throws IOException {
         packer.write(v);
      }

      public Object read(Unpacker unpacker, Object to, boolean required) throws IOException {
         Object o = unpacker.read(this.entry.getType());
         this.entry.set(to, o);
         return o;
      }
   }
}
