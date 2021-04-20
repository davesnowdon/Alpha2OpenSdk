package org.codehaus.jackson.mrbean;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.map.AbstractTypeResolver;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.util.VersionUtil;

public class AbstractTypeMaterializer extends AbstractTypeResolver implements Versioned {
   protected static final int DEFAULT_FEATURE_FLAGS = AbstractTypeMaterializer.Feature.collectDefaults();
   public static final String DEFAULT_PACKAGE_FOR_GENERATED = "org.codehaus.jackson.generated.";
   protected final AbstractTypeMaterializer.MyClassLoader _classLoader;
   protected int _featureFlags;
   protected String _defaultPackage;

   public AbstractTypeMaterializer() {
      this((ClassLoader)null);
   }

   public AbstractTypeMaterializer(ClassLoader parentClassLoader) {
      this._featureFlags = DEFAULT_FEATURE_FLAGS;
      this._defaultPackage = "org.codehaus.jackson.generated.";
      if (parentClassLoader == null) {
         parentClassLoader = this.getClass().getClassLoader();
      }

      this._classLoader = new AbstractTypeMaterializer.MyClassLoader(parentClassLoader);
   }

   public Version version() {
      return VersionUtil.versionFor(this.getClass());
   }

   public final boolean isEnabled(AbstractTypeMaterializer.Feature f) {
      return (this._featureFlags & f.getMask()) != 0;
   }

   public void enable(AbstractTypeMaterializer.Feature f) {
      this._featureFlags |= f.getMask();
   }

   public void disable(AbstractTypeMaterializer.Feature f) {
      this._featureFlags &= ~f.getMask();
   }

   public void set(AbstractTypeMaterializer.Feature f, boolean state) {
      if (state) {
         this.enable(f);
      } else {
         this.disable(f);
      }

   }

   public void setDefaultPackage(String defPkg) {
      if (!defPkg.endsWith(".")) {
         defPkg = defPkg + ".";
      }

      this._defaultPackage = defPkg;
   }

   public JavaType resolveAbstractType(DeserializationConfig config, JavaType type) {
      return !type.isContainerType() && !type.isPrimitive() && !type.isEnumType() && !type.isThrowable() ? config.constructType(this.materializeClass(config, type.getRawClass())) : null;
   }

   protected Class<?> materializeClass(DeserializationConfig config, Class<?> cls) {
      String newName = this._defaultPackage + cls.getName();
      BeanBuilder builder = new BeanBuilder(config, cls);
      byte[] bytecode = builder.implement(this.isEnabled(AbstractTypeMaterializer.Feature.FAIL_ON_UNMATERIALIZED_METHOD)).build(newName);
      Class<?> result = this._classLoader.loadAndResolve(newName, bytecode, cls);
      return result;
   }

   private static class MyClassLoader extends ClassLoader {
      public MyClassLoader(ClassLoader parent) {
         super(parent);
      }

      public Class<?> loadAndResolve(String className, byte[] byteCode, Class<?> targetClass) throws IllegalArgumentException {
         Class<?> old = this.findLoadedClass(className);
         if (old != null && targetClass.isAssignableFrom(old)) {
            return old;
         } else {
            Class impl;
            try {
               impl = this.defineClass(className, byteCode, 0, byteCode.length);
            } catch (LinkageError var7) {
               throw new IllegalArgumentException("Failed to load class '" + className + "': " + var7.getMessage(), var7);
            }

            this.resolveClass(impl);
            return impl;
         }
      }
   }

   public static enum Feature {
      FAIL_ON_UNMATERIALIZED_METHOD(false);

      final boolean _defaultState;

      protected static int collectDefaults() {
         int flags = 0;
         AbstractTypeMaterializer.Feature[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            AbstractTypeMaterializer.Feature f = arr$[i$];
            if (f.enabledByDefault()) {
               flags |= f.getMask();
            }
         }

         return flags;
      }

      private Feature(boolean defaultState) {
         this._defaultState = defaultState;
      }

      public boolean enabledByDefault() {
         return this._defaultState;
      }

      public int getMask() {
         return 1 << this.ordinal();
      }
   }
}
