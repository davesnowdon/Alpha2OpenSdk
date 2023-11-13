package org.msgpack.template.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.msgpack.annotation.Beans;
import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;
import org.msgpack.annotation.MessagePackBeans;
import org.msgpack.annotation.MessagePackMessage;
import org.msgpack.annotation.MessagePackOrdinalEnum;
import org.msgpack.annotation.NotNullable;
import org.msgpack.annotation.Optional;
import org.msgpack.annotation.OrdinalEnum;
import org.msgpack.template.FieldList;
import org.msgpack.template.FieldOption;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;

public abstract class AbstractTemplateBuilder implements TemplateBuilder {
   protected TemplateRegistry registry;

   protected AbstractTemplateBuilder(TemplateRegistry registry) {
      this.registry = registry;
   }

   public Objectemplate buildTemplate(Type targetType) throws TemplateBuildException {
      Class targetClass = (Class)targetType;
      this.checkClassValidation(targetClass);
      FieldOption fieldOption = this.getFieldOption(targetClass);
      FieldEntry[] entries = this.toFieldEntries(targetClass, fieldOption);
      return this.buildTemplate(targetClass, entries);
   }

   public Objectemplate buildTemplate(Class targetClass, FieldList fieldList) throws TemplateBuildException {
      this.checkClassValidation(targetClass);
      FieldEntry[] entries = this.toFieldEntries(targetClass, fieldList);
      return this.buildTemplate(targetClass, entries);
   }

   protected abstract Objectemplate buildTemplate(Class var1, FieldEntry[] var2);

   protected void checkClassValidation(Class targetClass) {
      if (Modifier.isAbstract(targetClass.getModifiers())) {
         throw new TemplateBuildException("Cannot build template for abstract class: " + targetClass.getName());
      } else if (targetClass.isInterface()) {
         throw new TemplateBuildException("Cannot build template for interface: " + targetClass.getName());
      } else if (targetClass.isArray()) {
         throw new TemplateBuildException("Cannot build template for array class: " + targetClass.getName());
      } else if (targetClass.isPrimitive()) {
         throw new TemplateBuildException("Cannot build template of primitive type: " + targetClass.getName());
      }
   }

   protected FieldOption getFieldOption(Class<?> targetClass) {
      Message m = (Message)targetClass.getAnnotation(Message.class);
      if (m == null) {
         return FieldOption.DEFAULT;
      } else {
         MessagePackMessage mpm = (MessagePackMessage)targetClass.getAnnotation(MessagePackMessage.class);
         return mpm == null ? FieldOption.DEFAULT : m.value();
      }
   }

   private FieldEntry[] toFieldEntries(Class<?> targetClass, FieldList flist) {
      List<FieldList.Entry> src = flist.getList();
      FieldEntry[] entries = new FieldEntry[src.size()];

      for(int i = 0; i < src.size(); ++i) {
         FieldList.Entry s = (FieldList.Entry)src.get(i);
         if (s.isAvailable()) {
            try {
               entries[i] = new DefaultFieldEntry(targetClass.getDeclaredField(s.getName()), s.getOption());
            } catch (SecurityException var8) {
               throw new TemplateBuildException(var8);
            } catch (NoSuchFieldException var9) {
               throw new TemplateBuildException(var9);
            }
         } else {
            entries[i] = new DefaultFieldEntry();
         }
      }

      return entries;
   }

   protected FieldEntry[] toFieldEntries(Class<?> targetClass, FieldOption from) {
      Field[] fields = this.getFields(targetClass);
      List<FieldEntry> indexed = new ArrayList();
      int maxIndex = -1;
      Field[] arr$ = fields;
      int i = fields.length;

      for(int i$ = 0; i$ < i; ++i$) {
         Field f = arr$[i$];
         FieldOption opt = this.getFieldOption(f, from);
         if (opt != FieldOption.IGNORE) {
            int index = this.getFieldIndex(f, maxIndex);
            if (indexed.size() > index && indexed.get(index) != null) {
               throw new TemplateBuildException("duplicated index: " + index);
            }

            if (index < 0) {
               throw new TemplateBuildException("invalid index: " + index);
            }

            while(indexed.size() <= index) {
               indexed.add((Object)null);
            }

            indexed.set(index, new DefaultFieldEntry(f, opt));
            if (maxIndex < index) {
               maxIndex = index;
            }
         }
      }

      FieldEntry[] entries = new FieldEntry[maxIndex + 1];

      for(i = 0; i < indexed.size(); ++i) {
         FieldEntry e = (FieldEntry)indexed.get(i);
         if (e == null) {
            entries[i] = new DefaultFieldEntry();
         } else {
            entries[i] = e;
         }
      }

      return entries;
   }

   private Field[] getFields(Class<?> targetClass) {
      List<Field[]> succ = new ArrayList();
      int total = 0;

      for(Class c = targetClass; c != Object.class; c = c.getSuperclass()) {
         Field[] fields = c.getDeclaredFields();
         total += fields.length;
         succ.add(fields);
      }

      Field[] result = new Field[total];
      int off = 0;

      for(int i = succ.size() - 1; i >= 0; --i) {
         Field[] fields = (Field[])succ.get(i);
         System.arraycopy(fields, 0, result, off, fields.length);
         off += fields.length;
      }

      return result;
   }

   private FieldOption getFieldOption(Field field, FieldOption from) {
      int mod = field.getModifiers();
      if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod) && !Modifier.isTransient(mod)) {
         if (isAnnotated((AccessibleObject)field, Ignore.class)) {
            return FieldOption.IGNORE;
         } else if (isAnnotated((AccessibleObject)field, Optional.class)) {
            return FieldOption.OPTIONAL;
         } else if (isAnnotated((AccessibleObject)field, NotNullable.class)) {
            return FieldOption.NOTNULLABLE;
         } else if (from != FieldOption.DEFAULT) {
            return from;
         } else {
            return field.getType().isPrimitive() ? FieldOption.NOTNULLABLE : FieldOption.OPTIONAL;
         }
      } else {
         return FieldOption.IGNORE;
      }
   }

   private int getFieldIndex(Field field, int maxIndex) {
      Index a = (Index)field.getAnnotation(Index.class);
      return a == null ? maxIndex + 1 : a.value();
   }

   public void writeTemplate(Type targetType, String directoryName) {
      throw new UnsupportedOperationException(targetType.toString());
   }

   public Objectemplate loadTemplate(Type targetType) {
      return null;
   }

   public static boolean isAnnotated(Class<?> targetClass, Class<? extends Annotation> with) {
      return targetClass.getAnnotation(with) != null;
   }

   public static boolean isAnnotated(AccessibleObject accessibleObject, Class<? extends Annotation> with) {
      return accessibleObject.getAnnotation(with) != null;
   }

   public static boolean matchAtClassTemplateBuilder(Class<?> targetClass, boolean hasAnnotation) {
      if (hasAnnotation) {
         return isAnnotated(targetClass, Message.class) || isAnnotated(targetClass, MessagePackMessage.class);
      } else {
         return !targetClass.isEnum() && !targetClass.isInterface();
      }
   }

   public static boolean matchAtBeansClassTemplateBuilder(Type targetType, boolean hasAnnotation) {
      Class<?> targetClass = (Class)targetType;
      if (hasAnnotation) {
         return isAnnotated((Class)targetType, Beans.class) || isAnnotated((Class)targetType, MessagePackBeans.class);
      } else {
         return !targetClass.isEnum() || !targetClass.isInterface();
      }
   }

   public static boolean matchAtArrayTemplateBuilder(Class<?> targetClass, boolean hasAnnotation) {
      return targetClass instanceof GenericArrayType ? true : targetClass.isArray();
   }

   public static boolean matchAtOrdinalEnumTemplateBuilder(Class<?> targetClass, boolean hasAnnotation) {
      if (!hasAnnotation) {
         return targetClass.isEnum();
      } else {
         return isAnnotated(targetClass, OrdinalEnum.class) || isAnnotated(targetClass, MessagePackOrdinalEnum.class);
      }
   }
}
