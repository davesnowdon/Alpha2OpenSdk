package org.msgpack.template.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.msgpack.template.TemplateRegistry;
import org.msgpack.util.android.DalvikVmChecker;

public class TemplateBuilderChain {
   private static final String JAVASSIST_TEMPLATE_BUILDER_CLASS_NAME = "org.msgpack.template.builder.JavassistTemplateBuilder";
   private static final String REFLECTION_TEMPLATE_BUILDER_CLASS_NAME = "org.msgpack.template.builder.ReflectionTemplateBuilder";
   protected List<TemplateBuilder> templateBuilders;
   protected TemplateBuilder forceBuilder;

   private static boolean enableDynamicCodeGeneration() {
      return !DalvikVmChecker.isDalvikVm();
   }

   public TemplateBuilderChain(TemplateRegistry registry) {
      this(registry, (ClassLoader)null);
   }

   public TemplateBuilderChain(TemplateRegistry registry, ClassLoader cl) {
      this.templateBuilders = new ArrayList();
      this.reset(registry, cl);
   }

   protected void reset(TemplateRegistry registry, ClassLoader cl) {
      if (registry == null) {
         throw new NullPointerException("registry is null");
      } else {
         String forceBuilderClassName = null;
         if (enableDynamicCodeGeneration()) {
            forceBuilderClassName = "org.msgpack.template.builder.JavassistTemplateBuilder";
         } else {
            forceBuilderClassName = "org.msgpack.template.builder.ReflectionTemplateBuilder";
         }

         this.forceBuilder = createForceTemplateBuilder(forceBuilderClassName, registry, cl);
         TemplateBuilder builder = this.forceBuilder;
         this.templateBuilders.add(new ArrayTemplateBuilder(registry));
         this.templateBuilders.add(new OrdinalEnumTemplateBuilder(registry));
         this.templateBuilders.add(builder);
         this.templateBuilders.add(new ReflectionBeansTemplateBuilder(registry));
      }
   }

   private static TemplateBuilder createForceTemplateBuilder(String className, TemplateRegistry registry, ClassLoader cl) {
      try {
         Class<?> c = Class.forName(className);
         Constructor<?> cons = c.getConstructor(TemplateRegistry.class, ClassLoader.class);
         return (TemplateBuilder)cons.newInstance(registry, cl);
      } catch (Exception var5) {
         var5.printStackTrace();
         return new ReflectionTemplateBuilder(registry, cl);
      }
   }

   public TemplateBuilder getForceBuilder() {
      return this.forceBuilder;
   }

   public TemplateBuilder select(Type targetType, boolean hasAnnotation) {
      Iterator i$ = this.templateBuilders.iterator();

      TemplateBuilder tb;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         tb = (TemplateBuilder)i$.next();
      } while(!tb.matchType(targetType, hasAnnotation));

      return tb;
   }
}
