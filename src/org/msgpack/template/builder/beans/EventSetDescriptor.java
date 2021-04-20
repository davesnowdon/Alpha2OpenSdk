package org.msgpack.template.builder.beans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TooManyListenersException;
import org.apache.harmony.beans.internal.nls.Messages;

public class EventSetDescriptor extends FeatureDescriptor {
   private Class<?> listenerType;
   private ArrayList<MethodDescriptor> listenerMethodDescriptors;
   private Method[] listenerMethods;
   private Method getListenerMethod;
   private Method addListenerMethod;
   private Method removeListenerMethod;
   private boolean unicast;
   private boolean inDefaultEventSet;

   public EventSetDescriptor(Class<?> sourceClass, String eventSetName, Class<?> listenerType, String listenerMethodName) throws IntrospectionException {
      this.inDefaultEventSet = true;
      this.checkNotNull(sourceClass, eventSetName, listenerType, listenerMethodName);
      this.setName(eventSetName);
      this.listenerType = listenerType;
      Method method = this.findListenerMethodByName(listenerMethodName);
      checkEventType(eventSetName, method);
      this.listenerMethodDescriptors = new ArrayList();
      this.listenerMethodDescriptors.add(new MethodDescriptor(method));
      this.addListenerMethod = this.findMethodByPrefix(sourceClass, "add", "");
      this.removeListenerMethod = this.findMethodByPrefix(sourceClass, "remove", "");
      if (this.addListenerMethod != null && this.removeListenerMethod != null) {
         this.getListenerMethod = this.findMethodByPrefix(sourceClass, "get", "s");
         this.unicast = isUnicastByDefault(this.addListenerMethod);
      } else {
         throw new IntrospectionException(Messages.getString("custom.beans.38"));
      }
   }

   public EventSetDescriptor(Class<?> sourceClass, String eventSetName, Class<?> listenerType, String[] listenerMethodNames, String addListenerMethodName, String removeListenerMethodName) throws IntrospectionException {
      this(sourceClass, eventSetName, listenerType, listenerMethodNames, addListenerMethodName, removeListenerMethodName, (String)null);
   }

   public EventSetDescriptor(Class<?> sourceClass, String eventSetName, Class<?> listenerType, String[] listenerMethodNames, String addListenerMethodName, String removeListenerMethodName, String getListenerMethodName) throws IntrospectionException {
      this.inDefaultEventSet = true;
      this.checkNotNull(sourceClass, eventSetName, listenerType, listenerMethodNames);
      this.setName(eventSetName);
      this.listenerType = listenerType;
      this.listenerMethodDescriptors = new ArrayList();
      String[] arr$ = listenerMethodNames;
      int len$ = listenerMethodNames.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String element = arr$[i$];
         Method m = this.findListenerMethodByName(element);
         this.listenerMethodDescriptors.add(new MethodDescriptor(m));
      }

      if (addListenerMethodName != null) {
         this.addListenerMethod = this.findAddRemoveListenerMethod(sourceClass, addListenerMethodName);
      }

      if (removeListenerMethodName != null) {
         this.removeListenerMethod = this.findAddRemoveListenerMethod(sourceClass, removeListenerMethodName);
      }

      if (getListenerMethodName != null) {
         this.getListenerMethod = this.findGetListenerMethod(sourceClass, getListenerMethodName);
      }

      this.unicast = isUnicastByDefault(this.addListenerMethod);
   }

   private Method findListenerMethodByName(String listenerMethodName) throws IntrospectionException {
      Method result = null;
      Method[] methods = this.listenerType.getMethods();
      Method[] arr$ = methods;
      int len$ = methods.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Method method = arr$[i$];
         if (listenerMethodName.equals(method.getName())) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 1 && paramTypes[0].getName().endsWith("Event")) {
               result = method;
               break;
            }
         }
      }

      if (null == result) {
         throw new IntrospectionException(Messages.getString("custom.beans.31", listenerMethodName, this.listenerType.getName()));
      } else {
         return result;
      }
   }

   public EventSetDescriptor(String eventSetName, Class<?> listenerType, Method[] listenerMethods, Method addListenerMethod, Method removeListenerMethod) throws IntrospectionException {
      this((String)eventSetName, (Class)listenerType, (Method[])listenerMethods, (Method)addListenerMethod, (Method)removeListenerMethod, (Method)null);
   }

   public EventSetDescriptor(String eventSetName, Class<?> listenerType, Method[] listenerMethods, Method addListenerMethod, Method removeListenerMethod, Method getListenerMethod) throws IntrospectionException {
      this.inDefaultEventSet = true;
      this.setName(eventSetName);
      this.listenerType = listenerType;
      this.listenerMethods = listenerMethods;
      if (listenerMethods != null) {
         this.listenerMethodDescriptors = new ArrayList();
         Method[] arr$ = listenerMethods;
         int len$ = listenerMethods.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Method element = arr$[i$];
            this.listenerMethodDescriptors.add(new MethodDescriptor(element));
         }
      }

      this.addListenerMethod = addListenerMethod;
      this.removeListenerMethod = removeListenerMethod;
      this.getListenerMethod = getListenerMethod;
      this.unicast = isUnicastByDefault(addListenerMethod);
   }

   public EventSetDescriptor(String eventSetName, Class<?> listenerType, MethodDescriptor[] listenerMethodDescriptors, Method addListenerMethod, Method removeListenerMethod) throws IntrospectionException {
      this((String)eventSetName, (Class)listenerType, (Method[])null, (Method)addListenerMethod, (Method)removeListenerMethod, (Method)null);
      if (listenerMethodDescriptors != null) {
         this.listenerMethodDescriptors = new ArrayList();
         MethodDescriptor[] arr$ = listenerMethodDescriptors;
         int len$ = listenerMethodDescriptors.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MethodDescriptor element = arr$[i$];
            this.listenerMethodDescriptors.add(element);
         }
      }

   }

   private void checkNotNull(Object sourceClass, Object eventSetName, Object alistenerType, Object listenerMethodName) {
      if (sourceClass == null) {
         throw new NullPointerException(Messages.getString("custom.beans.0C"));
      } else if (eventSetName == null) {
         throw new NullPointerException(Messages.getString("custom.beans.53"));
      } else if (alistenerType == null) {
         throw new NullPointerException(Messages.getString("custom.beans.54"));
      } else if (listenerMethodName == null) {
         throw new NullPointerException(Messages.getString("custom.beans.52"));
      }
   }

   private static void checkEventType(String eventSetName, Method listenerMethod) throws IntrospectionException {
      Class<?>[] params = listenerMethod.getParameterTypes();
      String firstParamTypeName = null;
      String eventTypeName = prepareEventTypeName(eventSetName);
      if (params.length > 0) {
         firstParamTypeName = extractShortClassName(params[0].getName());
      }

      if (firstParamTypeName == null || !firstParamTypeName.equals(eventTypeName)) {
         throw new IntrospectionException(Messages.getString("custom.beans.51", listenerMethod.getName(), eventTypeName));
      }
   }

   private static String extractShortClassName(String fullClassName) {
      int k = fullClassName.lastIndexOf(36);
      k = k == -1 ? fullClassName.lastIndexOf(46) : k;
      return fullClassName.substring(k + 1);
   }

   private static String prepareEventTypeName(String eventSetName) {
      StringBuilder sb = new StringBuilder();
      if (eventSetName != null && eventSetName.length() > 0) {
         sb.append(Character.toUpperCase(eventSetName.charAt(0)));
         if (eventSetName.length() > 1) {
            sb.append(eventSetName.substring(1));
         }
      }

      sb.append("Event");
      return sb.toString();
   }

   public Method[] getListenerMethods() {
      if (this.listenerMethods != null) {
         return this.listenerMethods;
      } else if (this.listenerMethodDescriptors == null) {
         return null;
      } else {
         this.listenerMethods = new Method[this.listenerMethodDescriptors.size()];
         int index = 0;

         MethodDescriptor md;
         for(Iterator i$ = this.listenerMethodDescriptors.iterator(); i$.hasNext(); this.listenerMethods[index++] = md.getMethod()) {
            md = (MethodDescriptor)i$.next();
         }

         return this.listenerMethods;
      }
   }

   public MethodDescriptor[] getListenerMethodDescriptors() {
      return this.listenerMethodDescriptors == null ? null : (MethodDescriptor[])this.listenerMethodDescriptors.toArray(new MethodDescriptor[0]);
   }

   public Method getRemoveListenerMethod() {
      return this.removeListenerMethod;
   }

   public Method getGetListenerMethod() {
      return this.getListenerMethod;
   }

   public Method getAddListenerMethod() {
      return this.addListenerMethod;
   }

   public Class<?> getListenerType() {
      return this.listenerType;
   }

   public void setUnicast(boolean unicast) {
      this.unicast = unicast;
   }

   public void setInDefaultEventSet(boolean inDefaultEventSet) {
      this.inDefaultEventSet = inDefaultEventSet;
   }

   public boolean isUnicast() {
      return this.unicast;
   }

   public boolean isInDefaultEventSet() {
      return this.inDefaultEventSet;
   }

   private Method findAddRemoveListenerMethod(Class<?> sourceClass, String methodName) throws IntrospectionException {
      try {
         return sourceClass.getMethod(methodName, this.listenerType);
      } catch (NoSuchMethodException var4) {
         return this.findAddRemoveListnerMethodWithLessCheck(sourceClass, methodName);
      } catch (Exception var5) {
         throw new IntrospectionException(Messages.getString("custom.beans.31", methodName, this.listenerType.getName()));
      }
   }

   private Method findAddRemoveListnerMethodWithLessCheck(Class<?> sourceClass, String methodName) throws IntrospectionException {
      Method[] methods = sourceClass.getMethods();
      Method result = null;
      Method[] arr$ = methods;
      int len$ = methods.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Method method = arr$[i$];
         if (method.getName().equals(methodName)) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 1) {
               result = method;
               break;
            }
         }
      }

      if (null == result) {
         throw new IntrospectionException(Messages.getString("custom.beans.31", methodName, this.listenerType.getName()));
      } else {
         return result;
      }
   }

   private Method findGetListenerMethod(Class<?> sourceClass, String methodName) {
      try {
         return sourceClass.getMethod(methodName);
      } catch (Exception var4) {
         return null;
      }
   }

   private Method findMethodByPrefix(Class<?> sourceClass, String prefix, String postfix) {
      String shortName = this.listenerType.getName();
      if (this.listenerType.getPackage() != null) {
         shortName = shortName.substring(this.listenerType.getPackage().getName().length() + 1);
      }

      String methodName = prefix + shortName + postfix;

      try {
         if ("get".equals(prefix)) {
            return sourceClass.getMethod(methodName);
         }
      } catch (NoSuchMethodException var9) {
         return null;
      }

      Method[] methods = sourceClass.getMethods();

      for(int i = 0; i < methods.length; ++i) {
         if (methods[i].getName().equals(methodName)) {
            Class<?>[] paramTypes = methods[i].getParameterTypes();
            if (paramTypes.length == 1) {
               return methods[i];
            }
         }
      }

      return null;
   }

   private static boolean isUnicastByDefault(Method addMethod) {
      if (addMethod != null) {
         Class<?>[] exceptionTypes = addMethod.getExceptionTypes();
         Class[] arr$ = exceptionTypes;
         int len$ = exceptionTypes.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Class<?> element = arr$[i$];
            if (element.equals(TooManyListenersException.class)) {
               return true;
            }
         }
      }

      return false;
   }

   void merge(EventSetDescriptor event) {
      super.merge(event);
      if (this.addListenerMethod == null) {
         this.addListenerMethod = event.addListenerMethod;
      }

      if (this.getListenerMethod == null) {
         this.getListenerMethod = event.getListenerMethod;
      }

      if (this.listenerMethodDescriptors == null) {
         this.listenerMethodDescriptors = event.listenerMethodDescriptors;
      }

      if (this.listenerMethods == null) {
         this.listenerMethods = event.listenerMethods;
      }

      if (this.listenerType == null) {
         this.listenerType = event.listenerType;
      }

      if (this.removeListenerMethod == null) {
         this.removeListenerMethod = event.removeListenerMethod;
      }

      this.inDefaultEventSet &= event.inDefaultEventSet;
   }
}
