package org.msgpack.template.builder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;
import org.msgpack.unpacker.Unpacker;

public class ReflectionTemplateBuilder extends AbstractTemplateBuilder {
   private static Logger LOG = Logger.getLogger(ReflectionBeansTemplateBuilder.class.getName());

   public ReflectionTemplateBuilder(TemplateRegistry registry) {
      this(registry, (ClassLoader)null);
   }

   public ReflectionTemplateBuilder(TemplateRegistry registry, ClassLoader cl) {
      super(registry);
   }

   public boolean matchType(Type targetType, boolean hasAnnotation) {
      Class<?> targetClass = (Class)targetType;
      boolean matched = matchAtClassTemplateBuilder(targetClass, hasAnnotation);
      if (matched && LOG.isLoggable(Level.FINE)) {
         LOG.fine("matched type: " + targetClass.getName());
      }

      return matched;
   }

   public Objectemplate buildTemplate(Class targetClass, FieldEntry[] entries) {
      if (entries == null) {
         throw new NullPointerException("entries is null: " + targetClass);
      } else {
         ReflectionTemplateBuilder.ReflectionFieldTemplate[] tmpls = this.toTemplates(entries);
         return new ReflectionTemplateBuilder.ReflectionClassTemplate(targetClass, tmpls);
      }
   }

   protected ReflectionTemplateBuilder.ReflectionFieldTemplate[] toTemplates(FieldEntry[] entries) {
      FieldEntry[] arr$ = entries;
      int i = entries.length;

      for(int i$ = 0; i$ < i; ++i$) {
         FieldEntry entry = arr$[i$];
         Field field = ((DefaultFieldEntry)entry).getField();
         int mod = field.getModifiers();
         if (!Modifier.isPublic(mod)) {
            field.setAccessible(true);
         }
      }

      ReflectionTemplateBuilder.ReflectionFieldTemplate[] templates = new ReflectionTemplateBuilder.ReflectionFieldTemplate[entries.length];

      for(i = 0; i < entries.length; ++i) {
         FieldEntry entry = entries[i];
         Template template = this.registry.lookup(entry.getGenericType());
         templates[i] = new ReflectionTemplateBuilder.FieldTemplateImpl(entry, template);
      }

      return templates;
   }

   protected static class ReflectionClassTemplate extends AbstractTemplate {
      protected Class targetClass;
      protected ReflectionTemplateBuilder.ReflectionFieldTemplate[] templates;

      protected ReflectionClassTemplate(Class targetClass, ReflectionTemplateBuilder.ReflectionFieldTemplate[] templates) {
         this.targetClass = targetClass;
         this.templates = templates;
      }

      public void write(Packer packer, T target, boolean required) throws IOException {
         if (target == null) {
            if (required) {
               throw new MessageTypeException("attempted to write null");
            } else {
               packer.writeNil();
            }
         } else {
            try {
               packer.writeArrayBegin(this.templates.length);
               ReflectionTemplateBuilder.ReflectionFieldTemplate[] arr$ = this.templates;
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  ReflectionTemplateBuilder.ReflectionFieldTemplate tmpl = arr$[i$];
                  if (!tmpl.entry.isAvailable()) {
                     packer.writeNil();
                  } else {
                     Object obj = tmpl.entry.get(target);
                     if (obj == null) {
                        if (tmpl.entry.isNotNullable()) {
                           throw new MessageTypeException(tmpl.entry.getName() + " cannot be null by @NotNullable");
                        }

                        packer.writeNil();
                     } else {
                        tmpl.write(packer, obj, true);
                     }
                  }
               }

               packer.writeArrayEnd();
            } catch (IOException var9) {
               throw var9;
            } catch (Exception var10) {
               throw new MessageTypeException(var10);
            }
         }
      }

      public T read(Unpacker unpacker, T to, boolean required) throws IOException {
         if (!required && unpacker.trySkipNil()) {
            return null;
         } else {
            try {
               if (to == null) {
                  to = this.targetClass.newInstance();
               }

               unpacker.readArrayBegin();

               for(int i = 0; i < this.templates.length; ++i) {
                  ReflectionTemplateBuilder.ReflectionFieldTemplate tmpl = this.templates[i];
                  if (!tmpl.entry.isAvailable()) {
                     unpacker.skip();
                  } else if (!tmpl.entry.isOptional() || !unpacker.trySkipNil()) {
                     tmpl.read(unpacker, to, false);
                  }
               }

               unpacker.readArrayEnd();
               return to;
            } catch (IOException var6) {
               throw var6;
            } catch (Exception var7) {
               throw new MessageTypeException(var7);
            }
         }
      }
   }

   static final class FieldTemplateImpl extends ReflectionTemplateBuilder.ReflectionFieldTemplate {
      private Template template;

      public FieldTemplateImpl(FieldEntry entry, Template template) {
         super(entry);
         this.template = template;
      }

      public void write(Packer packer, Object v, boolean required) throws IOException {
         this.template.write(packer, v, required);
      }

      public Object read(Unpacker unpacker, Object to, boolean required) throws IOException {
         Object f = this.entry.get(to);
         Object o = this.template.read(unpacker, f, required);
         if (o != f) {
            this.entry.set(to, o);
         }

         return o;
      }
   }

   protected abstract static class ReflectionFieldTemplate extends AbstractTemplate<Object> {
      protected FieldEntry entry;

      ReflectionFieldTemplate(FieldEntry entry) {
         this.entry = entry;
      }

      void setNil(Object v) {
         this.entry.set(v, (Object)null);
      }
   }
}
