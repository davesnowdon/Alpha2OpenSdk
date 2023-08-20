package org.msgpack.template.builder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

public abstract class BuildContext<T extends FieldEntry> {
   private static Logger LOG = Logger.getLogger(BuildContext.class.getName());
   protected JavassistTemplateBuilder director;
   protected String tmplName;
   protected CtClass tmplCtClass;
   protected StringBuilder stringBuilder = null;

   protected abstract Template buildTemplate(Class<?> var1, T[] var2, Template[] var3);

   protected abstract void setSuperClass() throws CannotCompileException, NotFoundException;

   protected abstract void buildConstructor() throws CannotCompileException, NotFoundException;

   public BuildContext(JavassistTemplateBuilder director) {
      this.director = director;
   }

   protected Template build(String className) {
      try {
         this.reset(className, false);
         LOG.fine(String.format("started generating template class %s for original class %s", this.tmplCtClass.getName(), className));
         this.buildClass();
         this.buildConstructor();
         this.buildMethodInit();
         this.buildWriteMethod();
         this.buildReadMethod();
         LOG.fine(String.format("finished generating template class %s for original class %s", this.tmplCtClass.getName(), className));
         return this.buildInstance(this.createClass());
      } catch (Exception var4) {
         String code = this.getBuiltString();
         if (code != null) {
            LOG.severe("builder: " + code);
            throw new TemplateBuildException("Cannot compile: " + code, var4);
         } else {
            throw new TemplateBuildException(var4);
         }
      }
   }

   protected void reset(String className, boolean isWritten) {
      String tmplName = null;
      if (!isWritten) {
         tmplName = className + "_$$_Template" + "_" + this.director.hashCode() + "_" + this.director.nextSeqId();
      } else {
         tmplName = className + "_$$_Template";
      }

      this.tmplCtClass = this.director.makeCtClass(tmplName);
   }

   protected void buildClass() throws CannotCompileException, NotFoundException {
      this.setSuperClass();
      this.tmplCtClass.addInterface(this.director.getCtClass(Template.class.getName()));
   }

   protected void buildMethodInit() {
   }

   protected abstract Template buildInstance(Class<?> var1) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;

   protected void buildWriteMethod() throws CannotCompileException, NotFoundException {
      LOG.fine(String.format("started generating write method in template class %s", this.tmplCtClass.getName()));
      String mbody = this.buildWriteMethodBody();
      int mod = 1;
      CtClass returnType = CtClass.voidType;
      String mname = "write";
      CtClass[] paramTypes = new CtClass[]{this.director.getCtClass(Packer.class.getName()), this.director.getCtClass(Object.class.getName()), CtClass.booleanType};
      CtClass[] exceptTypes = new CtClass[]{this.director.getCtClass(IOException.class.getName())};
      LOG.fine(String.format("compiling write method body: %s", mbody));
      CtMethod newCtMethod = CtNewMethod.make(mod, returnType, mname, paramTypes, exceptTypes, mbody, this.tmplCtClass);
      this.tmplCtClass.addMethod(newCtMethod);
      LOG.fine(String.format("finished generating write method in template class %s", this.tmplCtClass.getName()));
   }

   protected abstract String buildWriteMethodBody();

   protected void buildReadMethod() throws CannotCompileException, NotFoundException {
      LOG.fine(String.format("started generating read method in template class %s", this.tmplCtClass.getName()));
      String mbody = this.buildReadMethodBody();
      int mod = 1;
      CtClass returnType = this.director.getCtClass(Object.class.getName());
      String mname = "read";
      CtClass[] paramTypes = new CtClass[]{this.director.getCtClass(Unpacker.class.getName()), this.director.getCtClass(Object.class.getName()), CtClass.booleanType};
      CtClass[] exceptTypes = new CtClass[]{this.director.getCtClass(MessageTypeException.class.getName())};
      LOG.fine(String.format("compiling read method body: %s", mbody));
      CtMethod newCtMethod = CtNewMethod.make(mod, returnType, mname, paramTypes, exceptTypes, mbody, this.tmplCtClass);
      this.tmplCtClass.addMethod(newCtMethod);
      LOG.fine(String.format("finished generating read method in template class %s", this.tmplCtClass.getName()));
   }

   protected abstract String buildReadMethodBody();

   protected Class<?> createClass() throws CannotCompileException {
      return this.tmplCtClass.toClass(this.director.getClassLoader(), this.getClass().getProtectionDomain());
   }

   protected void saveClass(String directoryName) throws CannotCompileException, IOException {
      this.tmplCtClass.writeFile(directoryName);
   }

   protected void resetStringBuilder() {
      this.stringBuilder = new StringBuilder();
   }

   protected void buildString(String str) {
      this.stringBuilder.append(str);
   }

   protected void buildString(String format, Object... args) {
      this.stringBuilder.append(String.format(format, args));
   }

   protected String getBuiltString() {
      return this.stringBuilder == null ? null : this.stringBuilder.toString();
   }

   protected String primitiveWriteName(Class<?> type) {
      return "write";
   }

   protected String primitiveReadName(Class<?> type) {
      if (type == Boolean.TYPE) {
         return "readBoolean";
      } else if (type == Byte.TYPE) {
         return "readByte";
      } else if (type == Short.TYPE) {
         return "readShort";
      } else if (type == Integer.TYPE) {
         return "readInt";
      } else if (type == Long.TYPE) {
         return "readLong";
      } else if (type == Float.TYPE) {
         return "readFloat";
      } else if (type == Double.TYPE) {
         return "readDouble";
      } else {
         return type == Character.TYPE ? "readInt" : null;
      }
   }

   protected abstract void writeTemplate(Class<?> var1, T[] var2, Template[] var3, String var4);

   protected void write(String className, String directoryName) {
      try {
         this.reset(className, true);
         this.buildClass();
         this.buildConstructor();
         this.buildMethodInit();
         this.buildWriteMethod();
         this.buildReadMethod();
         this.saveClass(directoryName);
      } catch (Exception var5) {
         String code = this.getBuiltString();
         if (code != null) {
            LOG.severe("builder: " + code);
            throw new TemplateBuildException("Cannot compile: " + code, var5);
         } else {
            throw new TemplateBuildException(var5);
         }
      }
   }

   protected abstract Template loadTemplate(Class<?> var1, T[] var2, Template[] var3);

   protected Template load(String className) {
      String tmplName = className + "_$$_Template";

      try {
         Class<?> tmplClass = this.getClass().getClassLoader().loadClass(tmplName);
         return this.buildInstance(tmplClass);
      } catch (ClassNotFoundException var5) {
         return null;
      } catch (Exception var6) {
         String code = this.getBuiltString();
         if (code != null) {
            LOG.severe("builder: " + code);
            throw new TemplateBuildException("Cannot compile: " + code, var6);
         } else {
            throw new TemplateBuildException(var6);
         }
      }
   }
}
