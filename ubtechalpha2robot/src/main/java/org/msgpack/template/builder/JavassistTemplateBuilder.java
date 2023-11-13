package org.msgpack.template.builder;

import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.FieldOption;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;

public class JavassistTemplateBuilder extends AbstractTemplateBuilder {
   private static Logger LOG = Logger.getLogger(JavassistTemplateBuilder.class.getName());
   protected ClassPool pool;
   protected int seqId;
   protected ClassLoader loader;

   public JavassistTemplateBuilder(TemplateRegistry registry) {
      this(registry, (ClassLoader)null);
   }

   public JavassistTemplateBuilder(TemplateRegistry registry, ClassLoader cl) {
      super(registry);
      this.seqId = 0;
      this.pool = new ClassPool();
      this.pool.appendClassPath(new ClassClassPath(this.getClass()));
      boolean appended = false;
      this.loader = cl;
      if (this.loader == null) {
         this.loader = this.pool.getClassLoader();
      }

      try {
         if (this.loader != null) {
            this.pool.appendClassPath(new LoaderClassPath(this.loader));
            appended = true;
         }
      } catch (SecurityException var5) {
         LOG.fine("Cannot append a search path of classloader");
         var5.printStackTrace();
      }

      if (!appended) {
         this.pool.appendSystemPath();
      }

   }

   public boolean matchType(Type targetType, boolean hasAnnotation) {
      Class targetClass = (Class)targetType;
      boolean matched = matchAtClassTemplateBuilder(targetClass, hasAnnotation);
      if (matched && LOG.isLoggable(Level.FINE)) {
         LOG.fine("matched type: " + targetClass.getName());
      }

      return matched;
   }

   public void addClassLoader(ClassLoader cl) {
      this.pool.appendClassPath(new LoaderClassPath(cl));
   }

   protected CtClass makeCtClass(String className) {
      return this.pool.makeClass(className);
   }

   protected CtClass getCtClass(String className) throws NotFoundException {
      return this.pool.get(className);
   }

   protected int nextSeqId() {
      return this.seqId++;
   }

   protected BuildContext createBuildContext() {
      return new DefaultBuildContext(this);
   }

   public Objectemplate buildTemplate(Class ObjectargetClass, FieldEntry[] entries) {
      Template[] tmpls = this.toTemplate(entries);
      BuildContext bc = this.createBuildContext();
      return bc.buildTemplate(targetClass, entries, tmpls);
   }

   private Template[] toTemplate(FieldEntry[] from) {
      Template[] tmpls = new Template[from.length];

      for(int i = 0; i < from.length; ++i) {
         FieldEntry e = from[i];
         if (!e.isAvailable()) {
            tmpls[i] = null;
         } else {
            Template tmpl = this.registry.lookup(e.getGenericType());
            tmpls[i] = tmpl;
         }
      }

      return tmpls;
   }

   public void writeTemplate(Type targetType, String directoryName) {
      Class targetClass = (Class)targetType;
      this.checkClassValidation(targetClass);
      FieldOption implicitOption = this.getFieldOption(targetClass);
      FieldEntry[] entries = this.toFieldEntries(targetClass, implicitOption);
      this.writeTemplate(targetClass, entries, directoryName);
   }

   private void writeTemplate(Class targetClass, FieldEntry[] entries, String directoryName) {
      Template[] tmpls = this.toTemplate(entries);
      BuildContext bc = this.createBuildContext();
      bc.writeTemplate(targetClass, entries, tmpls, directoryName);
   }

   public Objectemplate loadTemplate(Type targetType) {
      Class targetClass = (Class)targetType;

      try {
         String tmplName = targetClass.getName() + "_$$_Template";
         ClassLoader cl = targetClass.getClassLoader();
         if (cl == null) {
            return null;
         }

         cl.loadClass(tmplName);
      } catch (ClassNotFoundException var7) {
         return null;
      }

      FieldOption implicitOption = this.getFieldOption(targetClass);
      FieldEntry[] entries = this.toFieldEntries(targetClass, implicitOption);
      Template[] tmpls = this.toTemplate(entries);
      BuildContext bc = this.createBuildContext();
      return bc.loadTemplate(targetClass, entries, tmpls);
   }

   protected ClassLoader getClassLoader() {
      return this.loader;
   }

   public abstract static class JavassistTemplate extends AbstractTemplate {
      public Class ObjectargetClass;
      public Template[] templates;

      public JavassistTemplate(Class ObjectargetClass, Template[] templates) {
         this.targetClass = targetClass;
         this.templates = templates;
      }
   }
}
