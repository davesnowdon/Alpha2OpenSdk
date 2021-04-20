package org.codehaus.jackson.mrbean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.org.objectweb.asm.ClassWriter;
import org.codehaus.jackson.org.objectweb.asm.FieldVisitor;
import org.codehaus.jackson.org.objectweb.asm.MethodVisitor;
import org.codehaus.jackson.org.objectweb.asm.Type;
import org.codehaus.jackson.type.JavaType;

public class BeanBuilder {
   protected Map<String, BeanBuilder.Property> _beanProperties = new LinkedHashMap();
   protected LinkedHashMap<String, Method> _unsupportedMethods = new LinkedHashMap();
   protected final Class<?> _implementedType;
   protected final TypeFactory _typeFactory;

   public BeanBuilder(DeserializationConfig config, Class<?> implType) {
      this._implementedType = implType;
      this._typeFactory = config.getTypeFactory();
   }

   public BeanBuilder implement(boolean failOnUnrecognized) {
      ArrayList<Class<?>> implTypes = new ArrayList();
      implTypes.add(this._implementedType);
      BeanUtil.findSuperTypes(this._implementedType, Object.class, implTypes);
      Iterator i$ = implTypes.iterator();

      while(i$.hasNext()) {
         Class<?> impl = (Class)i$.next();
         Method[] arr$ = impl.getDeclaredMethods();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Method m = arr$[i$];
            String methodName = m.getName();
            int argCount = m.getParameterTypes().length;
            if (argCount == 0) {
               if (methodName.startsWith("get") || methodName.startsWith("is") && returnsBoolean(m)) {
                  this.addGetter(m);
                  continue;
               }
            } else if (argCount == 1 && methodName.startsWith("set")) {
               this.addSetter(m);
               continue;
            }

            if (!BeanUtil.isConcrete(m) && !this._unsupportedMethods.containsKey(methodName)) {
               if (failOnUnrecognized) {
                  throw new IllegalArgumentException("Unrecognized abstract method '" + methodName + "' (not a getter or setter) -- to avoid exception, disable AbstractTypeMaterializer.Feature.FAIL_ON_UNMATERIALIZED_METHOD");
               }

               this._unsupportedMethods.put(methodName, m);
            }
         }
      }

      return this;
   }

   public byte[] build(String className) {
      ClassWriter cw = new ClassWriter(1);
      String internalClass = getInternalClassName(className);
      String implName = getInternalClassName(this._implementedType.getName());
      String superName;
      if (this._implementedType.isInterface()) {
         superName = "java/lang/Object";
         cw.visit(49, 33, internalClass, (String)null, superName, new String[]{implName});
      } else {
         superName = implName;
         cw.visit(49, 33, internalClass, (String)null, implName, (String[])null);
      }

      cw.visitSource(className + ".java", (String)null);
      generateDefaultConstructor(cw, superName);
      Iterator i$ = this._beanProperties.values().iterator();

      while(i$.hasNext()) {
         BeanBuilder.Property prop = (BeanBuilder.Property)i$.next();
         BeanBuilder.TypeDescription type = prop.selectType(this._typeFactory);
         createField(cw, prop, type);
         if (!prop.hasConcreteGetter()) {
            createGetter(cw, internalClass, prop, type);
         }

         if (!prop.hasConcreteSetter()) {
            createSetter(cw, internalClass, prop, type);
         }
      }

      i$ = this._unsupportedMethods.values().iterator();

      while(i$.hasNext()) {
         Method m = (Method)i$.next();
         createUnimplementedMethod(cw, internalClass, m);
      }

      cw.visitEnd();
      return cw.toByteArray();
   }

   private static String getPropertyName(String methodName) {
      int prefixLen = methodName.startsWith("is") ? 2 : 3;
      String body = methodName.substring(prefixLen);
      StringBuilder sb = new StringBuilder(body);
      sb.setCharAt(0, Character.toLowerCase(body.charAt(0)));
      return sb.toString();
   }

   private static String buildGetterName(String fieldName) {
      return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
   }

   private static String buildSetterName(String fieldName) {
      return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
   }

   private static String getInternalClassName(String className) {
      return className.replace(".", "/");
   }

   private void addGetter(Method m) {
      BeanBuilder.Property prop = this.findProperty(getPropertyName(m.getName()));
      if (prop.getGetter() == null) {
         prop.setGetter(m);
      }

   }

   private void addSetter(Method m) {
      BeanBuilder.Property prop = this.findProperty(getPropertyName(m.getName()));
      if (prop.getSetter() == null) {
         prop.setSetter(m);
      }

   }

   private BeanBuilder.Property findProperty(String propName) {
      BeanBuilder.Property prop = (BeanBuilder.Property)this._beanProperties.get(propName);
      if (prop == null) {
         prop = new BeanBuilder.Property(propName);
         this._beanProperties.put(propName, prop);
      }

      return prop;
   }

   private static final boolean returnsBoolean(Method m) {
      Class<?> rt = m.getReturnType();
      return rt == Boolean.class || rt == Boolean.TYPE;
   }

   private static void generateDefaultConstructor(ClassWriter cw, String superName) {
      MethodVisitor mv = cw.visitMethod(1, "<init>", "()V", (String)null, (String[])null);
      mv.visitCode();
      mv.visitVarInsn(25, 0);
      mv.visitMethodInsn(183, superName, "<init>", "()V");
      mv.visitInsn(177);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
   }

   private static void createField(ClassWriter cw, BeanBuilder.Property prop, BeanBuilder.TypeDescription type) {
      String sig = type.hasGenerics() ? type.genericSignature() : null;
      String desc = type.erasedSignature();
      FieldVisitor fv = cw.visitField(1, prop.getFieldName(), desc, sig, (Object)null);
      fv.visitEnd();
   }

   private static void createSetter(ClassWriter cw, String internalClassName, BeanBuilder.Property prop, BeanBuilder.TypeDescription propertyType) {
      Method setter = prop.getSetter();
      String methodName;
      String desc;
      if (setter != null) {
         desc = Type.getMethodDescriptor(setter);
         methodName = setter.getName();
      } else {
         desc = "(" + propertyType.erasedSignature() + ")V";
         methodName = buildSetterName(prop.getName());
      }

      String sig = propertyType.hasGenerics() ? "(" + propertyType.genericSignature() + ")V" : null;
      MethodVisitor mv = cw.visitMethod(1, methodName, desc, sig, (String[])null);
      mv.visitCode();
      mv.visitVarInsn(25, 0);
      mv.visitVarInsn(propertyType.getLoadOpcode(), 1);
      mv.visitFieldInsn(181, internalClassName, prop.getFieldName(), propertyType.erasedSignature());
      mv.visitInsn(177);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
   }

   private static void createGetter(ClassWriter cw, String internalClassName, BeanBuilder.Property prop, BeanBuilder.TypeDescription propertyType) {
      Method getter = prop.getGetter();
      String methodName;
      String desc;
      if (getter != null) {
         desc = Type.getMethodDescriptor(getter);
         methodName = getter.getName();
      } else {
         desc = "()" + propertyType.erasedSignature();
         methodName = buildGetterName(prop.getName());
      }

      String sig = propertyType.hasGenerics() ? "()" + propertyType.genericSignature() : null;
      MethodVisitor mv = cw.visitMethod(1, methodName, desc, sig, (String[])null);
      mv.visitCode();
      mv.visitVarInsn(25, 0);
      mv.visitFieldInsn(180, internalClassName, prop.getFieldName(), propertyType.erasedSignature());
      mv.visitInsn(propertyType.getReturnOpcode());
      mv.visitMaxs(0, 0);
      mv.visitEnd();
   }

   private static void createUnimplementedMethod(ClassWriter cw, String internalClassName, Method method) {
      String exceptionName = getInternalClassName(UnsupportedOperationException.class.getName());
      String sig = Type.getMethodDescriptor(method);
      String name = method.getName();
      MethodVisitor mv = cw.visitMethod(1, name, sig, (String)null, (String[])null);
      mv.visitTypeInsn(187, exceptionName);
      mv.visitInsn(89);
      mv.visitLdcInsn("Unimplemented method '" + name + "' (not a setter/getter, could not materialize)");
      mv.visitMethodInsn(183, exceptionName, "<init>", "(Ljava/lang/String;)V");
      mv.visitInsn(191);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
   }

   private static class TypeDescription {
      private final Type _asmType;
      private JavaType _jacksonType;

      public TypeDescription(JavaType type) {
         this._jacksonType = type;
         this._asmType = Type.getType(type.getRawClass());
      }

      public Class<?> getRawClass() {
         return this._jacksonType.getRawClass();
      }

      public String erasedSignature() {
         return this._jacksonType.getErasedSignature();
      }

      public String genericSignature() {
         return this._jacksonType.getGenericSignature();
      }

      public boolean hasGenerics() {
         return this._jacksonType.hasGenericTypes();
      }

      public int getLoadOpcode() {
         return this._asmType.getOpcode(21);
      }

      public int getReturnOpcode() {
         return this._asmType.getOpcode(172);
      }

      public String toString() {
         return this._jacksonType.toString();
      }

      public static BeanBuilder.TypeDescription moreSpecificType(BeanBuilder.TypeDescription desc1, BeanBuilder.TypeDescription desc2) {
         Class<?> c1 = desc1.getRawClass();
         Class<?> c2 = desc2.getRawClass();
         if (c1.isAssignableFrom(c2)) {
            return desc2;
         } else {
            return c2.isAssignableFrom(c1) ? desc1 : null;
         }
      }
   }

   private static class Property {
      protected final String _name;
      protected final String _fieldName;
      protected Method _getter;
      protected Method _setter;

      public Property(String name) {
         this._name = name;
         this._fieldName = "_" + name;
      }

      public String getName() {
         return this._name;
      }

      public void setGetter(Method m) {
         this._getter = m;
      }

      public void setSetter(Method m) {
         this._setter = m;
      }

      public Method getGetter() {
         return this._getter;
      }

      public Method getSetter() {
         return this._setter;
      }

      public String getFieldName() {
         return this._fieldName;
      }

      public boolean hasConcreteGetter() {
         return this._getter != null && BeanUtil.isConcrete(this._getter);
      }

      public boolean hasConcreteSetter() {
         return this._setter != null && BeanUtil.isConcrete(this._setter);
      }

      private BeanBuilder.TypeDescription getterType(TypeFactory tf) {
         Class<?> context = this._getter.getDeclaringClass();
         return new BeanBuilder.TypeDescription(tf.constructType(this._getter.getGenericReturnType(), context));
      }

      private BeanBuilder.TypeDescription setterType(TypeFactory tf) {
         Class<?> context = this._setter.getDeclaringClass();
         return new BeanBuilder.TypeDescription(tf.constructType(this._setter.getGenericParameterTypes()[0], context));
      }

      public BeanBuilder.TypeDescription selectType(TypeFactory tf) {
         if (this._getter == null) {
            return this.setterType(tf);
         } else if (this._setter == null) {
            return this.getterType(tf);
         } else {
            BeanBuilder.TypeDescription st = this.setterType(tf);
            BeanBuilder.TypeDescription gt = this.getterType(tf);
            BeanBuilder.TypeDescription specificType = BeanBuilder.TypeDescription.moreSpecificType(st, gt);
            if (specificType == null) {
               throw new IllegalArgumentException("Invalid property '" + this.getName() + "': incompatible types for getter/setter (" + gt + " vs " + st + ")");
            } else {
               return specificType;
            }
         }
      }
   }
}
