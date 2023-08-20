package org.msgpack.template.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

public class DefaultBuildContext extends BuildContext<FieldEntry> {
   protected FieldEntry[] entries;
   protected Class<?> origClass;
   protected String origName;
   protected Template<?>[] templates;

   public DefaultBuildContext(JavassistTemplateBuilder director) {
      super(director);
   }

   public Template buildTemplate(Class targetClass, FieldEntry[] entries, Template[] templates) {
      this.entries = entries;
      this.templates = templates;
      this.origClass = targetClass;
      this.origName = this.origClass.getName();
      return this.build(this.origName);
   }

   protected void setSuperClass() throws CannotCompileException, NotFoundException {
      this.tmplCtClass.setSuperclass(this.director.getCtClass(JavassistTemplateBuilder.JavassistTemplate.class.getName()));
   }

   protected void buildConstructor() throws CannotCompileException, NotFoundException {
      CtConstructor newCtCons = CtNewConstructor.make(new CtClass[]{this.director.getCtClass(Class.class.getName()), this.director.getCtClass(Template.class.getName() + "[]")}, new CtClass[0], this.tmplCtClass);
      this.tmplCtClass.addConstructor(newCtCons);
   }

   protected Template buildInstance(Class<?> c) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
      Constructor<?> cons = c.getConstructor(Class.class, Template[].class);
      Object tmpl = cons.newInstance(this.origClass, this.templates);
      return (Template)tmpl;
   }

   protected void buildMethodInit() {
   }

   protected String buildWriteMethodBody() {
      this.resetStringBuilder();
      this.buildString("\n{\n");
      this.buildString("  if ($2 == null) {\n");
      this.buildString("    if ($3) {\n");
      this.buildString("      throw new %s(\"Attempted to write null\");\n", new Object[]{MessageTypeException.class.getName()});
      this.buildString("    }\n");
      this.buildString("    $1.writeNil();\n");
      this.buildString("    return;\n");
      this.buildString("  }\n");
      this.buildString("  %s _$$_t = (%s) $2;\n", new Object[]{this.origName, this.origName});
      this.buildString("  $1.writeArrayBegin(%d);\n", new Object[]{this.entries.length});

      for(int i = 0; i < this.entries.length; ++i) {
         FieldEntry e = this.entries[i];
         if (!e.isAvailable()) {
            this.buildString("  $1.writeNil();\n");
         } else {
            DefaultFieldEntry de = (DefaultFieldEntry)e;
            boolean isPrivate = Modifier.isPrivate(de.getField().getModifiers());
            Class<?> type = de.getType();
            if (type.isPrimitive()) {
               if (!isPrivate) {
                  this.buildString("  $1.%s(_$$_t.%s);\n", new Object[]{this.primitiveWriteName(type), de.getName()});
               } else {
                  this.buildString("  %s.writePrivateField($1, _$$_t, %s.class, \"%s\", templates[%d]);\n", new Object[]{DefaultBuildContext.class.getName(), de.getField().getDeclaringClass().getName(), de.getName(), i});
               }
            } else {
               if (!isPrivate) {
                  this.buildString("  if (_$$_t.%s == null) {\n", new Object[]{de.getName()});
               } else {
                  this.buildString("  if (%s.readPrivateField(_$$_t, %s.class, \"%s\") == null) {\n", new Object[]{DefaultBuildContext.class.getName(), de.getField().getDeclaringClass().getName(), de.getName()});
               }

               if (de.isNotNullable()) {
                  this.buildString("    throw new %s(\"%s cannot be null by @NotNullable\");\n", new Object[]{MessageTypeException.class.getName(), de.getName()});
               } else {
                  this.buildString("    $1.writeNil();\n");
               }

               this.buildString("  } else {\n");
               if (!isPrivate) {
                  this.buildString("    templates[%d].write($1, _$$_t.%s);\n", new Object[]{i, de.getName()});
               } else {
                  this.buildString("    %s.writePrivateField($1, _$$_t, %s.class, \"%s\", templates[%d]);\n", new Object[]{DefaultBuildContext.class.getName(), de.getField().getDeclaringClass().getName(), de.getName(), i});
               }

               this.buildString("  }\n");
            }
         }
      }

      this.buildString("  $1.writeArrayEnd();\n");
      this.buildString("}\n");
      return this.getBuiltString();
   }

   public static Object readPrivateField(Object target, Class targetClass, String fieldName) {
      Field field = null;

      Object var5;
      try {
         field = targetClass.getDeclaredField(fieldName);
         field.setAccessible(true);
         Object valueReference = field.get(target);
         var5 = valueReference;
      } catch (Exception var9) {
         throw new MessageTypeException(var9);
      } finally {
         if (field != null) {
            field.setAccessible(false);
         }

      }

      return var5;
   }

   public static void writePrivateField(Packer packer, Object target, Class targetClass, String fieldName, Template tmpl) {
      Field field = null;

      try {
         field = targetClass.getDeclaredField(fieldName);
         field.setAccessible(true);
         Object valueReference = field.get(target);
         tmpl.write(packer, valueReference);
      } catch (Exception var10) {
         throw new MessageTypeException(var10);
      } finally {
         if (field != null) {
            field.setAccessible(false);
         }

      }

   }

   protected String buildReadMethodBody() {
      this.resetStringBuilder();
      this.buildString("\n{\n");
      this.buildString("  if (!$3 && $1.trySkipNil()) {\n");
      this.buildString("    return null;\n");
      this.buildString("  }\n");
      this.buildString("  %s _$$_t;\n", new Object[]{this.origName});
      this.buildString("  if ($2 == null) {\n");
      this.buildString("    _$$_t = new %s();\n", new Object[]{this.origName});
      this.buildString("  } else {\n");
      this.buildString("    _$$_t = (%s) $2;\n", new Object[]{this.origName});
      this.buildString("  }\n");
      this.buildString("  $1.readArrayBegin();\n");

      for(int i = 0; i < this.entries.length; ++i) {
         FieldEntry e = this.entries[i];
         if (!e.isAvailable()) {
            this.buildString("  $1.skip();\n");
         } else {
            if (e.isOptional()) {
               this.buildString("  if ($1.trySkipNil()) {");
               this.buildString("  } else {\n");
            }

            DefaultFieldEntry de = (DefaultFieldEntry)e;
            boolean isPrivate = Modifier.isPrivate(de.getField().getModifiers());
            Class<?> type = de.getType();
            if (type.isPrimitive()) {
               if (!isPrivate) {
                  this.buildString("    _$$_t.%s = $1.%s();\n", new Object[]{de.getName(), this.primitiveReadName(type)});
               } else {
                  this.buildString("    %s.readPrivateField($1, _$$_t, %s.class, \"%s\", templates[%d]);\n", new Object[]{DefaultBuildContext.class.getName(), de.getField().getDeclaringClass().getName(), de.getName(), i});
               }
            } else if (!isPrivate) {
               this.buildString("    _$$_t.%s = (%s) this.templates[%d].read($1, _$$_t.%s);\n", new Object[]{de.getName(), de.getJavaTypeName(), i, de.getName()});
            } else {
               this.buildString("    %s.readPrivateField($1, _$$_t, %s.class, \"%s\", templates[%d]);\n", new Object[]{DefaultBuildContext.class.getName(), de.getField().getDeclaringClass().getName(), de.getName(), i});
            }

            if (de.isOptional()) {
               this.buildString("  }\n");
            }
         }
      }

      this.buildString("  $1.readArrayEnd();\n");
      this.buildString("  return _$$_t;\n");
      this.buildString("}\n");
      return this.getBuiltString();
   }

   public static void readPrivateField(Unpacker unpacker, Object target, Class targetClass, String fieldName, Template tmpl) {
      Field field = null;

      try {
         field = targetClass.getDeclaredField(fieldName);
         field.setAccessible(true);
         Object fieldReference = field.get(target);
         Object valueReference = tmpl.read(unpacker, fieldReference);
         if (valueReference != fieldReference) {
            field.set(target, valueReference);
         }
      } catch (Exception var11) {
         throw new MessageTypeException(var11);
      } finally {
         if (field != null) {
            field.setAccessible(false);
         }

      }

   }

   public void writeTemplate(Class<?> targetClass, FieldEntry[] entries, Template[] templates, String directoryName) {
      this.entries = entries;
      this.templates = templates;
      this.origClass = targetClass;
      this.origName = this.origClass.getName();
      this.write(this.origName, directoryName);
   }

   public Template loadTemplate(Class<?> targetClass, FieldEntry[] entries, Template[] templates) {
      this.entries = entries;
      this.templates = templates;
      this.origClass = targetClass;
      this.origName = this.origClass.getName();
      return this.load(this.origName);
   }
}
