package org.msgpack.template.builder;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.BooleanArrayTemplate;
import org.msgpack.template.ByteArrayTemplate;
import org.msgpack.template.DoubleArrayTemplate;
import org.msgpack.template.FieldList;
import org.msgpack.template.FloatArrayTemplate;
import org.msgpack.template.IntegerArrayTemplate;
import org.msgpack.template.LongArrayTemplate;
import org.msgpack.template.ObjectArrayTemplate;
import org.msgpack.template.ShortArrayTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;
import org.msgpack.unpacker.Unpacker;

public class ArrayTemplateBuilder extends AbstractTemplateBuilder {
   private static final Logger LOG = Logger.getLogger(ArrayTemplateBuilder.class.getName());

   public ArrayTemplateBuilder(TemplateRegistry registry) {
      super(registry);
   }

   public boolean matchType(Type targetType, boolean forceBuild) {
      Class<?> targetClass = (Class)targetType;
      boolean matched = AbstractTemplateBuilder.matchAtArrayTemplateBuilder(targetClass, false);
      if (matched && LOG.isLoggable(Level.FINE)) {
         LOG.fine("matched type: " + targetClass.getName());
      }

      return matched;
   }

   public <T> Template<T> buildTemplate(Type arrayType) {
      int dim = 1;
      Object baseType;
      Class baseClass;
      if (arrayType instanceof GenericArrayType) {
         GenericArrayType type = (GenericArrayType)arrayType;

         for(baseType = type.getGenericComponentType(); baseType instanceof GenericArrayType; ++dim) {
            baseType = ((GenericArrayType)baseType).getGenericComponentType();
         }

         if (baseType instanceof ParameterizedType) {
            baseClass = (Class)((ParameterizedType)baseType).getRawType();
         } else {
            baseClass = (Class)baseType;
         }
      } else {
         Class<?> type = (Class)arrayType;

         for(baseClass = type.getComponentType(); baseClass.isArray(); ++dim) {
            baseClass = baseClass.getComponentType();
         }

         baseType = baseClass;
      }

      return this.toTemplate(arrayType, (Type)baseType, baseClass, dim);
   }

   private Template toTemplate(Type arrayType, Type genericBaseType, Class baseClass, int dim) {
      if (dim == 1) {
         if (baseClass == Boolean.TYPE) {
            return BooleanArrayTemplate.getInstance();
         } else if (baseClass == Short.TYPE) {
            return ShortArrayTemplate.getInstance();
         } else if (baseClass == Integer.TYPE) {
            return IntegerArrayTemplate.getInstance();
         } else if (baseClass == Long.TYPE) {
            return LongArrayTemplate.getInstance();
         } else if (baseClass == Float.TYPE) {
            return FloatArrayTemplate.getInstance();
         } else if (baseClass == Double.TYPE) {
            return DoubleArrayTemplate.getInstance();
         } else if (baseClass == Byte.TYPE) {
            return ByteArrayTemplate.getInstance();
         } else {
            Template baseTemplate = this.registry.lookup(genericBaseType);
            return new ObjectArrayTemplate(baseClass, baseTemplate);
         }
      } else if (dim == 2) {
         Class componentClass = Array.newInstance(baseClass, 0).getClass();
         Template componentTemplate = this.toTemplate(arrayType, genericBaseType, baseClass, dim - 1);
         return new ArrayTemplateBuilder.ReflectionMultidimentionalArrayTemplate(componentClass, componentTemplate);
      } else {
         ArrayTemplateBuilder.ReflectionMultidimentionalArrayTemplate componentTemplate = (ArrayTemplateBuilder.ReflectionMultidimentionalArrayTemplate)this.toTemplate(arrayType, genericBaseType, baseClass, dim - 1);
         Class componentClass = Array.newInstance(componentTemplate.getComponentClass(), 0).getClass();
         return new ArrayTemplateBuilder.ReflectionMultidimentionalArrayTemplate(componentClass, componentTemplate);
      }
   }

   public <T> Template<T> buildTemplate(Class<T> targetClass, FieldList flist) throws TemplateBuildException {
      throw new UnsupportedOperationException(targetClass.getName());
   }

   protected <T> Template<T> buildTemplate(Class<T> targetClass, FieldEntry[] entries) {
      throw new UnsupportedOperationException(targetClass.getName());
   }

   public void writeTemplate(Type targetType, String directoryName) {
      throw new UnsupportedOperationException(targetType.toString());
   }

   public <T> Template<T> loadTemplate(Type targetType) {
      return null;
   }

   static class ReflectionMultidimentionalArrayTemplate extends AbstractTemplate {
      private Class componentClass;
      private Template componentTemplate;

      public ReflectionMultidimentionalArrayTemplate(Class componentClass, Template componentTemplate) {
         this.componentClass = componentClass;
         this.componentTemplate = componentTemplate;
      }

      Class getComponentClass() {
         return this.componentClass;
      }

      public void write(Packer packer, Object v, boolean required) throws IOException {
         if (v == null) {
            if (required) {
               throw new MessageTypeException("Attempted to write null");
            } else {
               packer.writeNil();
            }
         } else if (v instanceof Object[] && this.componentClass.isAssignableFrom(v.getClass().getComponentType())) {
            Object[] array = (Object[])((Object[])v);
            int length = array.length;
            packer.writeArrayBegin(length);

            for(int i = 0; i < length; ++i) {
               this.componentTemplate.write(packer, array[i], required);
            }

            packer.writeArrayEnd();
         } else {
            throw new MessageTypeException();
         }
      }

      public Object read(Unpacker unpacker, Object to, boolean required) throws IOException {
         if (!required && unpacker.trySkipNil()) {
            return null;
         } else {
            int length = unpacker.readArrayBegin();
            Object[] array = (Object[])((Object[])Array.newInstance(this.componentClass, length));

            for(int i = 0; i < length; ++i) {
               array[i] = this.componentTemplate.read(unpacker, (Object)null, required);
            }

            unpacker.readArrayEnd();
            return array;
         }
      }
   }
}
