package org.msgpack.template.builder.beans;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.harmony.beans.BeansUtils;
import org.apache.harmony.beans.internal.nls.Messages;

public class Statement {
   private Object target;
   private String methodName;
   private Object[] arguments;
   private static WeakHashMap<Class<?>, Method[]> classMethodsCache = new WeakHashMap();
   private static final String[][] pdConstructorSignatures = new String[][]{{"java.lang.Class", "new", "java.lang.Boolean", "", "", ""}, {"java.lang.Class", "new", "java.lang.Byte", "", "", ""}, {"java.lang.Class", "new", "java.lang.Character", "", "", ""}, {"java.lang.Class", "new", "java.lang.Double", "", "", ""}, {"java.lang.Class", "new", "java.lang.Float", "", "", ""}, {"java.lang.Class", "new", "java.lang.Integer", "", "", ""}, {"java.lang.Class", "new", "java.lang.Long", "", "", ""}, {"java.lang.Class", "new", "java.lang.Short", "", "", ""}, {"java.lang.Class", "new", "java.lang.String", "", "", ""}, {"java.lang.Class", "forName", "java.lang.String", "", "", ""}, {"java.lang.Class", "newInstance", "java.lang.Class", "java.lang.Integer", "", ""}, {"java.lang.reflect.Field", "get", "null", "", "", ""}, {"java.lang.Class", "forName", "java.lang.String", "", "", ""}};

   public Statement(Object target, String methodName, Object[] arguments) {
      this.target = target;
      this.methodName = methodName;
      this.arguments = arguments == null ? BeansUtils.EMPTY_OBJECT_ARRAY : arguments;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Class clazz;
      if (this.target == null) {
         sb.append("null");
      } else {
         clazz = this.target.getClass();
         sb.append(clazz == String.class ? "\"\"" : BeansUtils.idOfClass(clazz));
      }

      sb.append('.' + this.methodName + '(');
      if (this.arguments != null) {
         for(int index = 0; index < this.arguments.length; ++index) {
            if (index > 0) {
               sb.append(", ");
            }

            if (this.arguments[index] == null) {
               sb.append("null");
            } else {
               clazz = this.arguments[index].getClass();
               sb.append(clazz == String.class ? '"' + (String)this.arguments[index] + '"' : BeansUtils.idOfClass(clazz));
            }
         }
      }

      sb.append(')');
      sb.append(';');
      return sb.toString();
   }

   public String getMethodName() {
      return this.methodName;
   }

   public Object[] getArguments() {
      return this.arguments;
   }

   public Object getTarget() {
      return this.target;
   }

   public void execute() throws Exception {
      this.invokeMethod();
   }

   Object invokeMethod() throws Exception {
      Object result = null;

      try {
         Object target = this.getTarget();
         String methodName = this.getMethodName();
         Object[] arguments = this.getArguments();
         Class<?> targetClass = target.getClass();
         Method method;
         if (targetClass.isArray()) {
            method = this.findArrayMethod(methodName, arguments);
            Object[] copy = new Object[arguments.length + 1];
            copy[0] = target;
            System.arraycopy(arguments, 0, copy, 1, arguments.length);
            result = method.invoke((Object)null, copy);
         } else if ("newInstance".equals(methodName) && target == Array.class) {
            result = Array.newInstance((Class)arguments[0], (Integer)arguments[1]);
         } else if (!"new".equals(methodName) && !"newInstance".equals(methodName)) {
            if (methodName.equals("newArray")) {
               Class<?> clazz = (Class)target;

               for(int index = 0; index < arguments.length; ++index) {
                  Class<?> argClass = arguments[index] == null ? null : arguments[index].getClass();
                  if (argClass != null && !clazz.isAssignableFrom(argClass) && !BeansUtils.isPrimitiveWrapper(argClass, clazz)) {
                     throw new IllegalArgumentException(Messages.getString("custom.beans.63"));
                  }
               }

               result = Array.newInstance(clazz, arguments.length);
               if (clazz.isPrimitive()) {
                  this.arrayCopy(clazz, arguments, result, arguments.length);
               } else {
                  System.arraycopy(arguments, 0, result, 0, arguments.length);
               }

               return result;
            }

            if (target instanceof Class) {
               method = null;

               try {
                  if (target != Class.class) {
                     method = findMethod((Class)target, methodName, arguments, true);
                     result = method.invoke((Object)null, arguments);
                  }
               } catch (NoSuchMethodException var10) {
               }

               if (method == null) {
                  if ("forName".equals(methodName) && arguments.length == 1 && arguments[0] instanceof String) {
                     try {
                        result = Class.forName((String)arguments[0]);
                     } catch (ClassNotFoundException var9) {
                        result = Class.forName((String)arguments[0], true, Thread.currentThread().getContextClassLoader());
                     }
                  } else {
                     method = findMethod(targetClass, methodName, arguments, false);
                     result = method.invoke(target, arguments);
                  }
               }
            } else if (target instanceof Iterator) {
               final Iterator<?> iterator = (Iterator)target;
               final Method method = findMethod(targetClass, methodName, arguments, false);
               if (iterator.hasNext()) {
                  result = (new PrivilegedAction<Object>() {
                     public Object run() {
                        try {
                           method.setAccessible(true);
                           return method.invoke(iterator);
                        } catch (Exception var2) {
                           return null;
                        }
                     }
                  }).run();
               }
            } else {
               method = findMethod(targetClass, methodName, arguments, false);
               method.setAccessible(true);
               result = method.invoke(target, arguments);
            }
         } else if (target instanceof Class) {
            Constructor<?> constructor = this.findConstructor((Class)target, arguments);
            result = constructor.newInstance(arguments);
         } else {
            if ("new".equals(methodName)) {
               throw new NoSuchMethodException(this.toString());
            }

            method = findMethod(targetClass, methodName, arguments, false);
            result = method.invoke(target, arguments);
         }

         return result;
      } catch (InvocationTargetException var11) {
         Throwable t = var11.getCause();
         throw (Exception)(t != null && t instanceof Exception ? (Exception)t : var11);
      }
   }

   private void arrayCopy(Class<?> type, Object[] src, Object dest, int length) {
      int index;
      if (type == Boolean.TYPE) {
         boolean[] destination = (boolean[])((boolean[])dest);

         for(index = 0; index < length; ++index) {
            destination[index] = (Boolean)src[index];
         }
      } else if (type == Short.TYPE) {
         short[] destination = (short[])((short[])dest);

         for(index = 0; index < length; ++index) {
            destination[index] = (Short)src[index];
         }
      } else if (type == Byte.TYPE) {
         byte[] destination = (byte[])((byte[])dest);

         for(index = 0; index < length; ++index) {
            destination[index] = (Byte)src[index];
         }
      } else if (type == Character.TYPE) {
         char[] destination = (char[])((char[])dest);

         for(index = 0; index < length; ++index) {
            destination[index] = (Character)src[index];
         }
      } else if (type == Integer.TYPE) {
         int[] destination = (int[])((int[])dest);

         for(index = 0; index < length; ++index) {
            destination[index] = (Integer)src[index];
         }
      } else if (type == Long.TYPE) {
         long[] destination = (long[])((long[])dest);

         for(index = 0; index < length; ++index) {
            destination[index] = (Long)src[index];
         }
      } else if (type == Float.TYPE) {
         float[] destination = (float[])((float[])dest);

         for(index = 0; index < length; ++index) {
            destination[index] = (Float)src[index];
         }
      } else if (type == Double.TYPE) {
         double[] destination = (double[])((double[])dest);

         for(index = 0; index < length; ++index) {
            destination[index] = (Double)src[index];
         }
      }

   }

   private Method findArrayMethod(String methodName, Object[] args) throws NoSuchMethodException {
      boolean isGet = "get".equals(methodName);
      boolean isSet = "set".equals(methodName);
      if (!isGet && !isSet) {
         throw new NoSuchMethodException(Messages.getString("custom.beans.3C"));
      } else if (args.length > 0 && args[0].getClass() != Integer.class) {
         throw new ClassCastException(Messages.getString("custom.beans.3D"));
      } else if (isGet && args.length != 1) {
         throw new ArrayIndexOutOfBoundsException(Messages.getString("custom.beans.3E"));
      } else if (isSet && args.length != 2) {
         throw new ArrayIndexOutOfBoundsException(Messages.getString("custom.beans.3F"));
      } else {
         Class<?>[] paraTypes = isGet ? new Class[]{Object.class, Integer.TYPE} : new Class[]{Object.class, Integer.TYPE, Object.class};
         return Array.class.getMethod(methodName, paraTypes);
      }
   }

   private Constructor<?> findConstructor(Class<?> clazz, Object[] args) throws NoSuchMethodException {
      Class<?>[] argTypes = getTypes(args);
      Constructor<?> result = null;
      Constructor[] arr$ = clazz.getConstructors();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Constructor<?> constructor = arr$[i$];
         Class<?>[] paraTypes = constructor.getParameterTypes();
         if (match(argTypes, paraTypes)) {
            if (result == null) {
               result = constructor;
            } else {
               Class<?>[] resultParaTypes = result.getParameterTypes();
               boolean isAssignable = true;

               for(int index = 0; index < paraTypes.length && (argTypes[index] == null || (isAssignable &= resultParaTypes[index].isAssignableFrom(paraTypes[index]))) && (argTypes[index] != null || (isAssignable &= paraTypes[index].isAssignableFrom(resultParaTypes[index]))); ++index) {
               }

               if (isAssignable) {
                  result = constructor;
               }
            }
         }
      }

      if (result == null) {
         throw new NoSuchMethodException(Messages.getString("custom.beans.40", (Object)clazz.getName()));
      } else {
         return result;
      }
   }

   static Method findMethod(Class<?> clazz, String methodName, Object[] args, boolean isStatic) throws NoSuchMethodException {
      Class<?>[] argTypes = getTypes(args);
      Method[] methods = null;
      if (classMethodsCache.containsKey(clazz)) {
         methods = (Method[])classMethodsCache.get(clazz);
      } else {
         methods = clazz.getMethods();
         classMethodsCache.put(clazz, methods);
      }

      ArrayList<Method> fitMethods = new ArrayList();
      Method[] arr$ = methods;
      int len$ = methods.length;

      Method onlyMethod;
      for(int i$ = 0; i$ < len$; ++i$) {
         onlyMethod = arr$[i$];
         if (methodName.equals(onlyMethod.getName()) && (!isStatic || Modifier.isStatic(onlyMethod.getModifiers())) && match(argTypes, onlyMethod.getParameterTypes())) {
            fitMethods.add(onlyMethod);
         }
      }

      int fitSize = fitMethods.size();
      if (fitSize == 0) {
         throw new NoSuchMethodException(Messages.getString("custom.beans.41", (Object)methodName));
      } else if (fitSize == 1) {
         return (Method)fitMethods.get(0);
      } else {
         Statement.MethodComparator comparator = new Statement.MethodComparator(methodName, argTypes);
         Method[] fitMethodArray = (Method[])fitMethods.toArray(new Method[fitSize]);
         onlyMethod = fitMethodArray[0];

         for(int i = 1; i < fitMethodArray.length; ++i) {
            int difference;
            if ((difference = comparator.compare(onlyMethod, fitMethodArray[i])) == 0) {
               Class<?> onlyReturnType = onlyMethod.getReturnType();
               Class<?> fitReturnType = fitMethodArray[i].getReturnType();
               if (onlyReturnType == fitReturnType) {
                  throw new NoSuchMethodException(Messages.getString("custom.beans.62", (Object)methodName));
               }

               if (onlyReturnType.isAssignableFrom(fitReturnType)) {
                  onlyMethod = fitMethodArray[i];
               }
            }

            if (difference > 0) {
               onlyMethod = fitMethodArray[i];
            }
         }

         return onlyMethod;
      }
   }

   private static boolean match(Class<?>[] argTypes, Class<?>[] paraTypes) {
      if (paraTypes.length != argTypes.length) {
         return false;
      } else {
         for(int index = 0; index < paraTypes.length; ++index) {
            if (argTypes[index] != null && !paraTypes[index].isAssignableFrom(argTypes[index]) && !BeansUtils.isPrimitiveWrapper(argTypes[index], paraTypes[index])) {
               return false;
            }
         }

         return true;
      }
   }

   static boolean isStaticMethodCall(Statement stmt) {
      Object target = stmt.getTarget();
      String methodName = stmt.getMethodName();
      if (!(target instanceof Class)) {
         return false;
      } else {
         try {
            findMethod((Class)target, methodName, stmt.getArguments(), true);
            return true;
         } catch (NoSuchMethodException var4) {
            return false;
         }
      }
   }

   static boolean isPDConstructor(Statement stmt) {
      Object target = stmt.getTarget();
      String methodName = stmt.getMethodName();
      Object[] args = stmt.getArguments();
      String[] sig = new String[pdConstructorSignatures[0].length];
      if (target != null && methodName != null && args != null && args.length != 0) {
         sig[0] = target.getClass().getName();
         sig[1] = methodName;

         for(int i = 2; i < sig.length; ++i) {
            if (args.length > i - 2) {
               sig[i] = args[i - 2] != null ? args[i - 2].getClass().getName() : "null";
            } else {
               sig[i] = "";
            }
         }

         String[][] arr$ = pdConstructorSignatures;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String[] element = arr$[i$];
            if (Arrays.equals(sig, element)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static Class<?> getPrimitiveWrapper(Class<?> base) {
      Class<?> res = null;
      if (base == Boolean.TYPE) {
         res = Boolean.class;
      } else if (base == Byte.TYPE) {
         res = Byte.class;
      } else if (base == Character.TYPE) {
         res = Character.class;
      } else if (base == Short.TYPE) {
         res = Short.class;
      } else if (base == Integer.TYPE) {
         res = Integer.class;
      } else if (base == Long.TYPE) {
         res = Long.class;
      } else if (base == Float.TYPE) {
         res = Float.class;
      } else if (base == Double.TYPE) {
         res = Double.class;
      }

      return res;
   }

   private static Class<?>[] getTypes(Object[] arguments) {
      Class<?>[] types = new Class[arguments.length];

      for(int index = 0; index < arguments.length; ++index) {
         types[index] = arguments[index] == null ? null : arguments[index].getClass();
      }

      return types;
   }

   static class MethodComparator implements Comparator<Method> {
      static int INFINITY = 2147483647;
      private String referenceMethodName;
      private Class<?>[] referenceMethodArgumentTypes;
      private final Map<Method, Integer> cache;

      public MethodComparator(String refMethodName, Class<?>[] refArgumentTypes) {
         this.referenceMethodName = refMethodName;
         this.referenceMethodArgumentTypes = refArgumentTypes;
         this.cache = new HashMap();
      }

      public int compare(Method m1, Method m2) {
         Integer norm1 = (Integer)this.cache.get(m1);
         Integer norm2 = (Integer)this.cache.get(m2);
         if (norm1 == null) {
            norm1 = this.getNorm(m1);
            this.cache.put(m1, norm1);
         }

         if (norm2 == null) {
            norm2 = this.getNorm(m2);
            this.cache.put(m2, norm2);
         }

         return norm1 - norm2;
      }

      private int getNorm(Method m) {
         String methodName = m.getName();
         Class<?>[] argumentTypes = m.getParameterTypes();
         int totalNorm = 0;
         if (this.referenceMethodName.equals(methodName) && this.referenceMethodArgumentTypes.length == argumentTypes.length) {
            for(int i = 0; i < this.referenceMethodArgumentTypes.length; ++i) {
               if (this.referenceMethodArgumentTypes[i] != null) {
                  if (this.referenceMethodArgumentTypes[i].isPrimitive()) {
                     this.referenceMethodArgumentTypes[i] = Statement.getPrimitiveWrapper(this.referenceMethodArgumentTypes[i]);
                  }

                  if (argumentTypes[i].isPrimitive()) {
                     argumentTypes[i] = Statement.getPrimitiveWrapper(argumentTypes[i]);
                  }

                  totalNorm += getDistance(this.referenceMethodArgumentTypes[i], argumentTypes[i]);
               }
            }

            return totalNorm;
         } else {
            return INFINITY;
         }
      }

      private static int getDistance(Class<?> clz1, Class<?> clz2) {
         int superDist = INFINITY;
         if (!clz2.isAssignableFrom(clz1)) {
            return INFINITY;
         } else if (clz1.getName().equals(clz2.getName())) {
            return 0;
         } else {
            Class<?> superClz = clz1.getSuperclass();
            if (superClz != null) {
               superDist = getDistance(superClz, clz2);
            }

            if (clz2.isInterface()) {
               Class<?>[] interfaces = clz1.getInterfaces();
               int bestDist = INFINITY;
               Class[] arr$ = interfaces;
               int len$ = interfaces.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  Class<?> element = arr$[i$];
                  int curDist = getDistance(element, clz2);
                  if (curDist < bestDist) {
                     bestDist = curDist;
                  }
               }

               if (superDist < bestDist) {
                  bestDist = superDist;
               }

               return bestDist != INFINITY ? bestDist + 1 : INFINITY;
            } else {
               return superDist != INFINITY ? superDist + 2 : INFINITY;
            }
         }
      }
   }
}
