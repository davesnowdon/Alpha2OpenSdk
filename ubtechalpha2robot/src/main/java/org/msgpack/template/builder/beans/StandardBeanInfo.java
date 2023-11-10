package org.msgpack.template.builder.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TooManyListenersException;
import java.util.Map.Entry;

class StandardBeanInfo extends SimpleBeanInfo {
   private static final String PREFIX_IS = "is";
   private static final String PREFIX_GET = "get";
   private static final String PREFIX_SET = "set";
   private static final String PREFIX_ADD = "add";
   private static final String PREFIX_REMOVE = "remove";
   private static final String SUFFIX_LISTEN = "Listener";
   private static final String STR_NORMAL = "normal";
   private static final String STR_INDEXED = "indexed";
   private static final String STR_VALID = "valid";
   private static final String STR_INVALID = "invalid";
   private static final String STR_PROPERTY_TYPE = "PropertyType";
   private static final String STR_IS_CONSTRAINED = "isConstrained";
   private static final String STR_SETTERS = "setters";
   private static final String STR_GETTERS = "getters";
   private boolean explicitMethods = false;
   private boolean explicitProperties = false;
   private boolean explicitEvents = false;
   private BeanInfo explicitBeanInfo = null;
   private EventSetDescriptor[] events = null;
   private MethodDescriptor[] methods = null;
   private PropertyDescriptor[] properties = null;
   private BeanDescriptor beanDescriptor = null;
   BeanInfo[] additionalBeanInfo = null;
   private Class<?> beanClass;
   private int defaultEventIndex = -1;
   private int defaultPropertyIndex = -1;
   private static StandardBeanInfo.PropertyComparator comparator = new StandardBeanInfo.PropertyComparator();
   private boolean canAddPropertyChangeListener;
   private boolean canRemovePropertyChangeListener;

   StandardBeanInfo(Class<?> beanClass, BeanInfo explicitBeanInfo, Class<?> stopClass) throws IntrospectionException {
      this.beanClass = beanClass;
      if (explicitBeanInfo != null) {
         this.explicitBeanInfo = explicitBeanInfo;
         this.events = explicitBeanInfo.getEventSetDescriptors();
         this.methods = explicitBeanInfo.getMethodDescriptors();
         this.properties = explicitBeanInfo.getPropertyDescriptors();
         this.defaultEventIndex = explicitBeanInfo.getDefaultEventIndex();
         if (this.defaultEventIndex < 0 || this.defaultEventIndex >= this.events.length) {
            this.defaultEventIndex = -1;
         }

         this.defaultPropertyIndex = explicitBeanInfo.getDefaultPropertyIndex();
         if (this.defaultPropertyIndex < 0 || this.defaultPropertyIndex >= this.properties.length) {
            this.defaultPropertyIndex = -1;
         }

         this.additionalBeanInfo = explicitBeanInfo.getAdditionalBeanInfo();
         if (this.events != null) {
            this.explicitEvents = true;
         }

         if (this.methods != null) {
            this.explicitMethods = true;
         }

         if (this.properties != null) {
            this.explicitProperties = true;
         }
      }

      if (this.methods == null) {
         this.methods = this.introspectMethods();
      }

      if (this.properties == null) {
         this.properties = this.introspectProperties(stopClass);
      }

      if (this.events == null) {
         this.events = this.introspectEvents();
      }

   }

   public BeanInfo[] getAdditionalBeanInfo() {
      return null;
   }

   public EventSetDescriptor[] getEventSetDescriptors() {
      return this.events;
   }

   public MethodDescriptor[] getMethodDescriptors() {
      return this.methods;
   }

   public PropertyDescriptor[] getPropertyDescriptors() {
      return this.properties;
   }

   public BeanDescriptor getBeanDescriptor() {
      if (this.beanDescriptor == null) {
         if (this.explicitBeanInfo != null) {
            this.beanDescriptor = this.explicitBeanInfo.getBeanDescriptor();
         }

         if (this.beanDescriptor == null) {
            this.beanDescriptor = new BeanDescriptor(this.beanClass);
         }
      }

      return this.beanDescriptor;
   }

   public int getDefaultEventIndex() {
      return this.defaultEventIndex;
   }

   public int getDefaultPropertyIndex() {
      return this.defaultPropertyIndex;
   }

   void mergeBeanInfo(BeanInfo beanInfo, boolean force) throws IntrospectionException {
      if (force || !this.explicitProperties) {
         PropertyDescriptor[] superDescs = beanInfo.getPropertyDescriptors();
         if (superDescs != null) {
            if (this.getPropertyDescriptors() != null) {
               this.properties = this.mergeProps(superDescs, beanInfo.getDefaultPropertyIndex());
            } else {
               this.properties = superDescs;
               this.defaultPropertyIndex = beanInfo.getDefaultPropertyIndex();
            }
         }
      }

      if (force || !this.explicitMethods) {
         MethodDescriptor[] superMethods = beanInfo.getMethodDescriptors();
         if (superMethods != null) {
            if (this.methods != null) {
               this.methods = this.mergeMethods(superMethods);
            } else {
               this.methods = superMethods;
            }
         }
      }

      if (force || !this.explicitEvents) {
         EventSetDescriptor[] superEvents = beanInfo.getEventSetDescriptors();
         if (superEvents != null) {
            if (this.events != null) {
               this.events = this.mergeEvents(superEvents, beanInfo.getDefaultEventIndex());
            } else {
               this.events = superEvents;
               this.defaultEventIndex = beanInfo.getDefaultEventIndex();
            }
         }
      }

   }

   private PropertyDescriptor[] mergeProps(PropertyDescriptor[] superDescs, int superDefaultIndex) throws IntrospectionException {
      HashMap<String, PropertyDescriptor> subMap = internalAsMap(this.properties);
      String defaultPropertyName = null;
      if (this.defaultPropertyIndex >= 0 && this.defaultPropertyIndex < this.properties.length) {
         defaultPropertyName = this.properties[this.defaultPropertyIndex].getName();
      } else if (superDefaultIndex >= 0 && superDefaultIndex < superDescs.length) {
         defaultPropertyName = superDescs[superDefaultIndex].getName();
      }

      for(int i = 0; i < superDescs.length; ++i) {
         PropertyDescriptor superDesc = superDescs[i];
         String propertyName = superDesc.getName();
         if (!subMap.containsKey(propertyName)) {
            subMap.put(propertyName, superDesc);
         } else {
            Object value = subMap.get(propertyName);
            Method subGet = ((PropertyDescriptor)value).getReadMethod();
            Method subSet = ((PropertyDescriptor)value).getWriteMethod();
            Method superGet = superDesc.getReadMethod();
            Method superSet = superDesc.getWriteMethod();
            Class<?> superType = superDesc.getPropertyType();
            Class<?> superIndexedType = null;
            Class<?> subType = ((PropertyDescriptor)value).getPropertyType();
            Class<?> subIndexedType = null;
            if (value instanceof IndexedPropertyDescriptor) {
               subIndexedType = ((IndexedPropertyDescriptor)value).getIndexedPropertyType();
            }

            if (superDesc instanceof IndexedPropertyDescriptor) {
               superIndexedType = ((IndexedPropertyDescriptor)superDesc).getIndexedPropertyType();
            }

            String subGetName;
            Method method;
            if (superIndexedType == null) {
               PropertyDescriptor subDesc = (PropertyDescriptor)value;
               if (subIndexedType != null) {
                  if (superType != null && superType.isArray() && superType.getComponentType().getName().equals(subIndexedType.getName())) {
                     if (subGet == null && superGet != null) {
                        subDesc.setReadMethod(superGet);
                     }

                     if (subSet == null && superSet != null) {
                        subDesc.setWriteMethod(superSet);
                     }
                  }

                  if (subIndexedType == Boolean.TYPE && superType == Boolean.TYPE) {
                     Method subIndexedSet = ((IndexedPropertyDescriptor)subDesc).getIndexedWriteMethod();
                     if (subGet == null && subSet == null && subIndexedSet != null && superGet != null) {
                        try {
                           subSet = this.beanClass.getDeclaredMethod(subIndexedSet.getName(), Boolean.TYPE);
                        } catch (Exception var28) {
                        }

                        if (subSet != null) {
                           subDesc = new PropertyDescriptor(propertyName, superGet, subSet);
                        }
                     }
                  }
               } else if (subType != null && superType != null && subType.getName() != null && subType.getName().equals(superType.getName())) {
                  if (superGet != null && (subGet == null || superGet.equals(subGet))) {
                     subDesc.setReadMethod(superGet);
                  }

                  if (superSet != null && (subSet == null || superSet.equals(subSet))) {
                     subDesc.setWriteMethod(superSet);
                  }

                  if (subType == Boolean.TYPE && subGet != null && superGet != null && superGet.getName().startsWith("is")) {
                     subDesc.setReadMethod(superGet);
                  }
               } else if ((subGet == null || subSet == null) && superGet != null) {
                  subDesc = new PropertyDescriptor(propertyName, superGet, superSet);
                  if (subGet != null) {
                     subGetName = subGet.getName();
                     method = null;
                     MethodDescriptor[] introspectMethods = this.introspectMethods();
                     MethodDescriptor[] arr$ = introspectMethods;
                     int len$ = introspectMethods.length;

                     for(int i$ = 0; i$ < len$; ++i$) {
                        MethodDescriptor methodDesc = arr$[i$];
                        method = methodDesc.getMethod();
                        if (method != subGet && subGetName.equals(method.getName()) && method.getParameterTypes().length == 0 && method.getReturnType() == superType) {
                           subDesc.setReadMethod(method);
                           break;
                        }
                     }
                  }
               }

               subMap.put(propertyName, subDesc);
            } else if (subIndexedType == null) {
               if (subType != null && subType.isArray() && subType.getComponentType().getName().equals(superIndexedType.getName())) {
                  if (subGet != null) {
                     superDesc.setReadMethod(subGet);
                  }

                  if (subSet != null) {
                     superDesc.setWriteMethod(subSet);
                  }

                  subMap.put(propertyName, superDesc);
               } else {
                  if (subGet == null || subSet == null) {
                     Class<?> beanSuperClass = this.beanClass.getSuperclass();
                     subGetName = this.capitalize(propertyName);
                     method = null;
                     if (subGet == null) {
                        if (subType == Boolean.TYPE) {
                           try {
                              method = beanSuperClass.getDeclaredMethod("is" + subGetName);
                           } catch (Exception var27) {
                           }
                        } else {
                           try {
                              method = beanSuperClass.getDeclaredMethod("get" + subGetName);
                           } catch (Exception var26) {
                           }
                        }

                        if (method != null && !Modifier.isStatic(method.getModifiers()) && method.getReturnType() == subType) {
                           ((PropertyDescriptor)value).setReadMethod(method);
                        }
                     } else {
                        try {
                           method = beanSuperClass.getDeclaredMethod("set" + subGetName, subType);
                        } catch (Exception var25) {
                        }

                        if (method != null && !Modifier.isStatic(method.getModifiers()) && method.getReturnType() == Void.TYPE) {
                           ((PropertyDescriptor)value).setWriteMethod(method);
                        }
                     }
                  }

                  subMap.put(propertyName, (PropertyDescriptor)value);
               }
            } else if (subIndexedType.getName().equals(superIndexedType.getName())) {
               IndexedPropertyDescriptor subDesc = (IndexedPropertyDescriptor)value;
               if (subGet == null && superGet != null) {
                  subDesc.setReadMethod(superGet);
               }

               if (subSet == null && superSet != null) {
                  subDesc.setWriteMethod(superSet);
               }

               IndexedPropertyDescriptor superIndexedDesc = (IndexedPropertyDescriptor)superDesc;
               if (subDesc.getIndexedReadMethod() == null && superIndexedDesc.getIndexedReadMethod() != null) {
                  subDesc.setIndexedReadMethod(superIndexedDesc.getIndexedReadMethod());
               }

               if (subDesc.getIndexedWriteMethod() == null && superIndexedDesc.getIndexedWriteMethod() != null) {
                  subDesc.setIndexedWriteMethod(superIndexedDesc.getIndexedWriteMethod());
               }

               subMap.put(propertyName, subDesc);
            }

            mergeAttributes((PropertyDescriptor)value, superDesc);
         }
      }

      PropertyDescriptor[] theDescs = new PropertyDescriptor[subMap.size()];
      subMap.values().toArray(theDescs);
      if (defaultPropertyName != null && !this.explicitProperties) {
         for(int i = 0; i < theDescs.length; ++i) {
            if (defaultPropertyName.equals(theDescs[i].getName())) {
               this.defaultPropertyIndex = i;
               break;
            }
         }
      }

      return theDescs;
   }

   private String capitalize(String name) {
      if (name == null) {
         return null;
      } else if (name.length() != 0 && (name.length() <= 1 || !Character.isUpperCase(name.charAt(1)))) {
         char[] chars = name.toCharArray();
         chars[0] = Character.toUpperCase(chars[0]);
         return new String(chars);
      } else {
         return name;
      }
   }

   private static void mergeAttributes(PropertyDescriptor subDesc, PropertyDescriptor superDesc) {
      subDesc.hidden |= superDesc.hidden;
      subDesc.expert |= superDesc.expert;
      subDesc.preferred |= superDesc.preferred;
      subDesc.bound |= superDesc.bound;
      subDesc.constrained |= superDesc.constrained;
      subDesc.name = superDesc.name;
      if (subDesc.shortDescription == null && superDesc.shortDescription != null) {
         subDesc.shortDescription = superDesc.shortDescription;
      }

      if (subDesc.displayName == null && superDesc.displayName != null) {
         subDesc.displayName = superDesc.displayName;
      }

   }

   private MethodDescriptor[] mergeMethods(MethodDescriptor[] superDescs) {
      HashMap<String, MethodDescriptor> subMap = internalAsMap(this.methods);
      MethodDescriptor[] theMethods = superDescs;
      int len$ = superDescs.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MethodDescriptor superMethod = theMethods[i$];
         String methodName = getQualifiedName(superMethod.getMethod());
         MethodDescriptor method = (MethodDescriptor)subMap.get(methodName);
         if (method == null) {
            subMap.put(methodName, superMethod);
         } else {
            method.merge(superMethod);
         }
      }

      theMethods = new MethodDescriptor[subMap.size()];
      subMap.values().toArray(theMethods);
      return theMethods;
   }

   private EventSetDescriptor[] mergeEvents(EventSetDescriptor[] otherEvents, int otherDefaultIndex) {
      HashMap<String, EventSetDescriptor> subMap = internalAsMap(this.events);
      String defaultEventName = null;
      if (this.defaultEventIndex >= 0 && this.defaultEventIndex < this.events.length) {
         defaultEventName = this.events[this.defaultEventIndex].getName();
      } else if (otherDefaultIndex >= 0 && otherDefaultIndex < otherEvents.length) {
         defaultEventName = otherEvents[otherDefaultIndex].getName();
      }

      EventSetDescriptor[] theEvents = otherEvents;
      int i = otherEvents.length;

      for(int i$ = 0; i$ < i; ++i$) {
         EventSetDescriptor event = theEvents[i$];
         String eventName = event.getName();
         EventSetDescriptor subEvent = (EventSetDescriptor)subMap.get(eventName);
         if (subEvent == null) {
            subMap.put(eventName, event);
         } else {
            subEvent.merge(event);
         }
      }

      theEvents = new EventSetDescriptor[subMap.size()];
      subMap.values().toArray(theEvents);
      if (defaultEventName != null && !this.explicitEvents) {
         for(i = 0; i < theEvents.length; ++i) {
            if (defaultEventName.equals(theEvents[i].getName())) {
               this.defaultEventIndex = i;
               break;
            }
         }
      }

      return theEvents;
   }

   private static HashMap<String, PropertyDescriptor> internalAsMap(PropertyDescriptor[] propertyDescs) {
      HashMap<String, PropertyDescriptor> map = new HashMap();

      for(int i = 0; i < propertyDescs.length; ++i) {
         map.put(propertyDescs[i].getName(), propertyDescs[i]);
      }

      return map;
   }

   private static HashMap<String, MethodDescriptor> internalAsMap(MethodDescriptor[] theDescs) {
      HashMap<String, MethodDescriptor> map = new HashMap();

      for(int i = 0; i < theDescs.length; ++i) {
         String qualifiedName = getQualifiedName(theDescs[i].getMethod());
         map.put(qualifiedName, theDescs[i]);
      }

      return map;
   }

   private static HashMap<String, EventSetDescriptor> internalAsMap(EventSetDescriptor[] theDescs) {
      HashMap<String, EventSetDescriptor> map = new HashMap();

      for(int i = 0; i < theDescs.length; ++i) {
         map.put(theDescs[i].getName(), theDescs[i]);
      }

      return map;
   }

   private static String getQualifiedName(Method method) {
      String qualifiedName = method.getName();
      Class<?>[] paramTypes = method.getParameterTypes();
      if (paramTypes != null) {
         for(int i = 0; i < paramTypes.length; ++i) {
            qualifiedName = qualifiedName + "_" + paramTypes[i].getName();
         }
      }

      return qualifiedName;
   }

   private MethodDescriptor[] introspectMethods() {
      return this.introspectMethods(false, this.beanClass);
   }

   private MethodDescriptor[] introspectMethods(boolean includeSuper) {
      return this.introspectMethods(includeSuper, this.beanClass);
   }

   private MethodDescriptor[] introspectMethods(boolean includeSuper, Class<?> introspectorClass) {
      Method[] basicMethods = includeSuper ? introspectorClass.getMethods() : introspectorClass.getDeclaredMethods();
      if (basicMethods != null && basicMethods.length != 0) {
         ArrayList<MethodDescriptor> methodList = new ArrayList(basicMethods.length);

         int methodCount;
         for(methodCount = 0; methodCount < basicMethods.length; ++methodCount) {
            int modifiers = basicMethods[methodCount].getModifiers();
            if (Modifier.isPublic(modifiers)) {
               MethodDescriptor theDescriptor = new MethodDescriptor(basicMethods[methodCount]);
               methodList.add(theDescriptor);
            }
         }

         methodCount = methodList.size();
         MethodDescriptor[] theMethods = null;
         if (methodCount > 0) {
            theMethods = new MethodDescriptor[methodCount];
            theMethods = (MethodDescriptor[])methodList.toArray(theMethods);
         }

         return theMethods;
      } else {
         return null;
      }
   }

   private PropertyDescriptor[] introspectProperties(Class<?> stopClass) throws IntrospectionException {
      MethodDescriptor[] methodDescriptors = this.introspectMethods();
      if (methodDescriptors == null) {
         return null;
      } else {
         ArrayList<MethodDescriptor> methodList = new ArrayList();

         int methodCount;
         for(methodCount = 0; methodCount < methodDescriptors.length; ++methodCount) {
            int modifiers = methodDescriptors[methodCount].getMethod().getModifiers();
            if (!Modifier.isStatic(modifiers)) {
               methodList.add(methodDescriptors[methodCount]);
            }
         }

         methodCount = methodList.size();
         MethodDescriptor[] theMethods = null;
         if (methodCount > 0) {
            theMethods = new MethodDescriptor[methodCount];
            theMethods = (MethodDescriptor[])methodList.toArray(theMethods);
         }

         if (theMethods == null) {
            return null;
         } else {
            HashMap<String, HashMap> propertyTable = new HashMap(theMethods.length);

            for(int i = 0; i < theMethods.length; ++i) {
               introspectGet(theMethods[i].getMethod(), propertyTable);
               introspectSet(theMethods[i].getMethod(), propertyTable);
            }

            this.fixGetSet(propertyTable);
            MethodDescriptor[] allMethods = this.introspectMethods(true);
            if (stopClass != null) {
               MethodDescriptor[] excludeMethods = this.introspectMethods(true, stopClass);
               if (excludeMethods != null) {
                  ArrayList<MethodDescriptor> tempMethods = new ArrayList();
                  MethodDescriptor[] arr$ = allMethods;
                  int len$ = allMethods.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     MethodDescriptor method = arr$[i$];
                     if (!this.isInSuper(method, excludeMethods)) {
                        tempMethods.add(method);
                     }
                  }

                  allMethods = (MethodDescriptor[])tempMethods.toArray(new MethodDescriptor[0]);
               }
            }

            for(int i = 0; i < allMethods.length; ++i) {
               this.introspectPropertyListener(allMethods[i].getMethod());
            }

            ArrayList<PropertyDescriptor> propertyList = new ArrayList();
            Iterator i$ = propertyTable.entrySet().iterator();

            while(true) {
               String indexedTag;
               String propertyName;
               HashMap table;
               String normalTag;
               do {
                  do {
                     if (!i$.hasNext()) {
                        PropertyDescriptor[] theProperties = new PropertyDescriptor[propertyList.size()];
                        propertyList.toArray(theProperties);
                        return theProperties;
                     }

                     Entry<String, HashMap> entry = (Entry)i$.next();
                     propertyName = (String)entry.getKey();
                     table = (HashMap)entry.getValue();
                  } while(table == null);

                  normalTag = (String)table.get("normal");
                  indexedTag = (String)table.get("indexed");
               } while(normalTag == null && indexedTag == null);

               Method get = (Method)table.get("normalget");
               Method set = (Method)table.get("normalset");
               Method indexedGet = (Method)table.get("indexedget");
               Method indexedSet = (Method)table.get("indexedset");
               PropertyDescriptor propertyDesc = null;
               if (indexedTag == null) {
                  propertyDesc = new PropertyDescriptor(propertyName, get, set);
               } else {
                  try {
                     propertyDesc = new IndexedPropertyDescriptor(propertyName, get, set, indexedGet, indexedSet);
                  } catch (IntrospectionException var21) {
                     propertyDesc = new IndexedPropertyDescriptor(propertyName, (Method)null, (Method)null, indexedGet, indexedSet);
                  }
               }

               if (this.canAddPropertyChangeListener && this.canRemovePropertyChangeListener) {
                  ((PropertyDescriptor)propertyDesc).setBound(true);
               } else {
                  ((PropertyDescriptor)propertyDesc).setBound(false);
               }

               if (table.get("isConstrained") == Boolean.TRUE) {
                  ((PropertyDescriptor)propertyDesc).setConstrained(true);
               }

               propertyList.add(propertyDesc);
            }
         }
      }
   }

   private boolean isInSuper(MethodDescriptor method, MethodDescriptor[] excludeMethods) {
      MethodDescriptor[] arr$ = excludeMethods;
      int len$ = excludeMethods.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MethodDescriptor m = arr$[i$];
         if (method.getMethod().equals(m.getMethod())) {
            return true;
         }
      }

      return false;
   }

   private void introspectPropertyListener(Method theMethod) {
      String methodName = theMethod.getName();
      Class<?>[] param = theMethod.getParameterTypes();
      if (param.length == 1) {
         if (methodName.equals("addPropertyChangeListener") && param[0].equals(PropertyChangeListener.class)) {
            this.canAddPropertyChangeListener = true;
         }

         if (methodName.equals("removePropertyChangeListener") && param[0].equals(PropertyChangeListener.class)) {
            this.canRemovePropertyChangeListener = true;
         }

      }
   }

   private static void introspectGet(Method theMethod, HashMap<String, HashMap> propertyTable) {
      String methodName = theMethod.getName();
      int prefixLength = 0;
      if (methodName != null) {
         if (methodName.startsWith("get")) {
            prefixLength = "get".length();
         }

         if (methodName.startsWith("is")) {
            prefixLength = "is".length();
         }

         if (prefixLength != 0) {
            String propertyName = Introspector.decapitalize(methodName.substring(prefixLength));
            if (isValidProperty(propertyName)) {
               Class propertyType = theMethod.getReturnType();
               if (propertyType != null && propertyType != Void.TYPE) {
                  if (prefixLength != 2 || propertyType == Boolean.TYPE) {
                     Class[] paramTypes = theMethod.getParameterTypes();
                     if (paramTypes.length <= 1 && (paramTypes.length != 1 || paramTypes[0] == Integer.TYPE)) {
                        HashMap table = (HashMap)propertyTable.get(propertyName);
                        if (table == null) {
                           table = new HashMap();
                           propertyTable.put(propertyName, table);
                        }

                        ArrayList<Method> getters = (ArrayList)table.get("getters");
                        if (getters == null) {
                           getters = new ArrayList();
                           table.put("getters", getters);
                        }

                        getters.add(theMethod);
                     }
                  }
               }
            }
         }
      }
   }

   private static void introspectSet(Method theMethod, HashMap<String, HashMap> propertyTable) {
      String methodName = theMethod.getName();
      if (methodName != null) {
         Class returnType = theMethod.getReturnType();
         if (returnType == Void.TYPE) {
            if (methodName != null && methodName.startsWith("set")) {
               String propertyName = Introspector.decapitalize(methodName.substring("set".length()));
               if (isValidProperty(propertyName)) {
                  Class[] paramTypes = theMethod.getParameterTypes();
                  if (paramTypes.length != 0 && paramTypes.length <= 2 && (paramTypes.length != 2 || paramTypes[0] == Integer.TYPE)) {
                     HashMap table = (HashMap)propertyTable.get(propertyName);
                     if (table == null) {
                        table = new HashMap();
                        propertyTable.put(propertyName, table);
                     }

                     ArrayList<Method> setters = (ArrayList)table.get("setters");
                     if (setters == null) {
                        setters = new ArrayList();
                        table.put("setters", setters);
                     }

                     Class[] exceptions = theMethod.getExceptionTypes();
                     Class[] arr$ = exceptions;
                     int len$ = exceptions.length;

                     for(int i$ = 0; i$ < len$; ++i$) {
                        Class e = arr$[i$];
                        if (e.equals(PropertyVetoException.class)) {
                           table.put("isConstrained", Boolean.TRUE);
                        }
                     }

                     setters.add(theMethod);
                  }
               }
            }
         }
      }
   }

   private void fixGetSet(HashMap<String, HashMap> propertyTable) throws IntrospectionException {
      if (propertyTable != null) {
         Iterator i$ = propertyTable.entrySet().iterator();

         while(true) {
            label317:
            while(i$.hasNext()) {
               Entry<String, HashMap> entry = (Entry)i$.next();
               HashMap<String, Object> table = (HashMap)entry.getValue();
               ArrayList<Method> getters = (ArrayList)table.get("getters");
               ArrayList<Method> setters = (ArrayList)table.get("setters");
               Method normalGetter = null;
               Method indexedGetter = null;
               Method normalSetter = null;
               Method indexedSetter = null;
               Class<?> normalPropType = null;
               Class<?> indexedPropType = null;
               if (getters == null) {
                  getters = new ArrayList();
               }

               if (setters == null) {
                  setters = new ArrayList();
               }

               Class<?>[] paramTypes = null;
               String methodName = null;
               Iterator i$ = getters.iterator();

               while(true) {
                  Method setter;
                  do {
                     do {
                        do {
                           do {
                              if (!i$.hasNext()) {
                                 Method setter;
                                 Class propertyType;
                                 Iterator i$;
                                 if (normalGetter != null) {
                                    propertyType = normalGetter.getReturnType();
                                    i$ = setters.iterator();

                                    while(i$.hasNext()) {
                                       setter = (Method)i$.next();
                                       if (setter.getParameterTypes().length == 1 && propertyType.equals(setter.getParameterTypes()[0])) {
                                          normalSetter = setter;
                                          break;
                                       }
                                    }
                                 } else {
                                    i$ = setters.iterator();

                                    while(i$.hasNext()) {
                                       setter = (Method)i$.next();
                                       if (setter.getParameterTypes().length == 1) {
                                          normalSetter = setter;
                                       }
                                    }
                                 }

                                 if (indexedGetter != null) {
                                    propertyType = indexedGetter.getReturnType();
                                    i$ = setters.iterator();

                                    while(i$.hasNext()) {
                                       setter = (Method)i$.next();
                                       if (setter.getParameterTypes().length == 2 && setter.getParameterTypes()[0] == Integer.TYPE && propertyType.equals(setter.getParameterTypes()[1])) {
                                          indexedSetter = setter;
                                          break;
                                       }
                                    }
                                 } else {
                                    i$ = setters.iterator();

                                    while(i$.hasNext()) {
                                       setter = (Method)i$.next();
                                       if (setter.getParameterTypes().length == 2 && setter.getParameterTypes()[0] == Integer.TYPE) {
                                          indexedSetter = setter;
                                       }
                                    }
                                 }

                                 if (normalGetter != null) {
                                    normalPropType = normalGetter.getReturnType();
                                 } else if (normalSetter != null) {
                                    normalPropType = normalSetter.getParameterTypes()[0];
                                 }

                                 if (indexedGetter != null) {
                                    indexedPropType = indexedGetter.getReturnType();
                                 } else if (indexedSetter != null) {
                                    indexedPropType = indexedSetter.getParameterTypes()[1];
                                 }

                                 if (normalGetter != null && normalGetter.getReturnType().isArray()) {
                                 }

                                 if (normalGetter != null && normalSetter != null && (indexedGetter == null || indexedSetter == null)) {
                                    table.put("normal", "valid");
                                    table.put("normalget", normalGetter);
                                    table.put("normalset", normalSetter);
                                    table.put("normalPropertyType", normalPropType);
                                    continue label317;
                                 }

                                 if ((normalGetter != null || normalSetter != null) && indexedGetter == null && indexedSetter == null) {
                                    table.put("normal", "valid");
                                    table.put("normalget", normalGetter);
                                    table.put("normalset", normalSetter);
                                    table.put("normalPropertyType", normalPropType);
                                 } else {
                                    if ((normalGetter != null || normalSetter != null) && (indexedGetter != null || indexedSetter != null)) {
                                       if (normalGetter != null && normalSetter != null && indexedGetter != null && indexedSetter != null) {
                                          if (indexedGetter.getName().startsWith("get")) {
                                             table.put("normal", "valid");
                                             table.put("normalget", normalGetter);
                                             table.put("normalset", normalSetter);
                                             table.put("normalPropertyType", normalPropType);
                                             table.put("indexed", "valid");
                                             table.put("indexedget", indexedGetter);
                                             table.put("indexedset", indexedSetter);
                                             table.put("indexedPropertyType", indexedPropType);
                                             continue label317;
                                          }

                                          if (normalPropType != Boolean.TYPE && normalGetter.getName().startsWith("is")) {
                                             table.put("indexed", "valid");
                                             table.put("indexedset", indexedSetter);
                                             table.put("indexedPropertyType", indexedPropType);
                                             continue label317;
                                          }

                                          table.put("normal", "valid");
                                          table.put("normalget", normalGetter);
                                          table.put("normalset", normalSetter);
                                          table.put("normalPropertyType", normalPropType);
                                          continue label317;
                                       }

                                       if (normalGetter != null && normalSetter == null && indexedGetter != null && indexedSetter != null) {
                                          table.put("normal", "valid");
                                          table.put("normalget", normalGetter);
                                          table.put("normalset", normalSetter);
                                          table.put("normalPropertyType", normalPropType);
                                          table.put("indexed", "valid");
                                          if (indexedGetter.getName().startsWith("get")) {
                                             table.put("indexedget", indexedGetter);
                                          }

                                          table.put("indexedset", indexedSetter);
                                          table.put("indexedPropertyType", indexedPropType);
                                          continue label317;
                                       }

                                       if (normalGetter == null && normalSetter != null && indexedGetter != null && indexedSetter != null) {
                                          table.put("indexed", "valid");
                                          if (indexedGetter.getName().startsWith("get")) {
                                             table.put("indexedget", indexedGetter);
                                          }

                                          table.put("indexedset", indexedSetter);
                                          table.put("indexedPropertyType", indexedPropType);
                                          continue label317;
                                       }

                                       if (normalGetter != null && normalSetter == null && indexedGetter != null && indexedSetter == null) {
                                          if (indexedGetter.getName().startsWith("get")) {
                                             table.put("normal", "valid");
                                             table.put("normalget", normalGetter);
                                             table.put("normalset", normalSetter);
                                             table.put("normalPropertyType", normalPropType);
                                             table.put("indexed", "valid");
                                             table.put("indexedget", indexedGetter);
                                             table.put("indexedset", indexedSetter);
                                             table.put("indexedPropertyType", indexedPropType);
                                          } else {
                                             table.put("normal", "valid");
                                             table.put("normalget", normalGetter);
                                             table.put("normalset", normalSetter);
                                             table.put("normalPropertyType", normalPropType);
                                          }
                                          continue label317;
                                       }

                                       if (normalGetter == null && normalSetter != null && indexedGetter != null && indexedSetter == null) {
                                          if (indexedGetter.getName().startsWith("get")) {
                                             table.put("normal", "valid");
                                             table.put("normalget", normalGetter);
                                             table.put("normalset", normalSetter);
                                             table.put("normalPropertyType", normalPropType);
                                             table.put("indexed", "valid");
                                             table.put("indexedget", indexedGetter);
                                             table.put("indexedset", indexedSetter);
                                             table.put("indexedPropertyType", indexedPropType);
                                          } else {
                                             table.put("normal", "valid");
                                             table.put("normalget", normalGetter);
                                             table.put("normalset", normalSetter);
                                             table.put("normalPropertyType", normalPropType);
                                          }
                                          continue label317;
                                       }

                                       if (normalGetter != null && normalSetter == null && indexedGetter == null && indexedSetter != null) {
                                          table.put("indexed", "valid");
                                          table.put("indexedget", indexedGetter);
                                          table.put("indexedset", indexedSetter);
                                          table.put("indexedPropertyType", indexedPropType);
                                          continue label317;
                                       }

                                       if (normalGetter == null && normalSetter != null && indexedGetter == null && indexedSetter != null) {
                                          table.put("indexed", "valid");
                                          table.put("indexedget", indexedGetter);
                                          table.put("indexedset", indexedSetter);
                                          table.put("indexedPropertyType", indexedPropType);
                                          continue label317;
                                       }
                                    }

                                    if (normalSetter != null || normalGetter != null || indexedGetter == null && indexedSetter == null) {
                                       if ((normalSetter != null || normalGetter != null) && indexedGetter != null && indexedSetter != null) {
                                          table.put("indexed", "valid");
                                          table.put("indexedget", indexedGetter);
                                          table.put("indexedset", indexedSetter);
                                          table.put("indexedPropertyType", indexedPropType);
                                       } else {
                                          table.put("normal", "invalid");
                                          table.put("indexed", "invalid");
                                       }
                                    } else if (indexedGetter != null && indexedGetter.getName().startsWith("is")) {
                                       if (indexedSetter != null) {
                                          table.put("indexed", "valid");
                                          table.put("indexedset", indexedSetter);
                                          table.put("indexedPropertyType", indexedPropType);
                                       }
                                    } else {
                                       table.put("indexed", "valid");
                                       table.put("indexedget", indexedGetter);
                                       table.put("indexedset", indexedSetter);
                                       table.put("indexedPropertyType", indexedPropType);
                                    }
                                 }
                                 continue label317;
                              }

                              setter = (Method)i$.next();
                              paramTypes = setter.getParameterTypes();
                              methodName = setter.getName();
                              if ((paramTypes == null || paramTypes.length == 0) && (normalGetter == null || methodName.startsWith("is"))) {
                                 normalGetter = setter;
                              }
                           } while(paramTypes == null);
                        } while(paramTypes.length != 1);
                     } while(paramTypes[0] != Integer.TYPE);
                  } while(indexedGetter != null && !methodName.startsWith("get") && (!methodName.startsWith("is") || indexedGetter.getName().startsWith("get")));

                  indexedGetter = setter;
               }
            }

            return;
         }
      }
   }

   private EventSetDescriptor[] introspectEvents() throws IntrospectionException {
      MethodDescriptor[] theMethods = this.introspectMethods();
      if (theMethods == null) {
         return null;
      } else {
         HashMap<String, HashMap> eventTable = new HashMap(theMethods.length);

         for(int i = 0; i < theMethods.length; ++i) {
            introspectListenerMethods("add", theMethods[i].getMethod(), eventTable);
            introspectListenerMethods("remove", theMethods[i].getMethod(), eventTable);
            introspectGetListenerMethods(theMethods[i].getMethod(), eventTable);
         }

         ArrayList<EventSetDescriptor> eventList = new ArrayList();
         Iterator i$ = eventTable.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, HashMap> entry = (Entry)i$.next();
            HashMap table = (HashMap)entry.getValue();
            Method add = (Method)table.get("add");
            Method remove = (Method)table.get("remove");
            if (add != null && remove != null) {
               Method get = (Method)table.get("get");
               Class<?> listenerType = (Class)table.get("listenerType");
               Method[] listenerMethods = (Method[])((Method[])table.get("listenerMethods"));
               EventSetDescriptor eventSetDescriptor = new EventSetDescriptor(Introspector.decapitalize((String)entry.getKey()), listenerType, listenerMethods, add, remove, get);
               eventSetDescriptor.setUnicast(table.get("isUnicast") != null);
               eventList.add(eventSetDescriptor);
            }
         }

         EventSetDescriptor[] theEvents = new EventSetDescriptor[eventList.size()];
         eventList.toArray(theEvents);
         return theEvents;
      }
   }

   private static void introspectListenerMethods(String type, Method theMethod, HashMap<String, HashMap> methodsTable) {
      String methodName = theMethod.getName();
      if (methodName != null) {
         if (methodName.startsWith(type) && methodName.endsWith("Listener")) {
            String listenerName = methodName.substring(type.length());
            String eventName = listenerName.substring(0, listenerName.lastIndexOf("Listener"));
            if (eventName != null && eventName.length() != 0) {
               Class[] paramTypes = theMethod.getParameterTypes();
               if (paramTypes != null && paramTypes.length == 1) {
                  Class<?> listenerType = paramTypes[0];
                  if (EventListener.class.isAssignableFrom(listenerType)) {
                     if (listenerType.getName().endsWith(listenerName)) {
                        HashMap table = (HashMap)methodsTable.get(eventName);
                        if (table == null) {
                           table = new HashMap();
                        }

                        if (table.get("listenerType") == null) {
                           table.put("listenerType", listenerType);
                           table.put("listenerMethods", introspectListenerMethods(listenerType));
                        }

                        table.put(type, theMethod);
                        if (type.equals("add")) {
                           Class[] exceptionTypes = theMethod.getExceptionTypes();
                           if (exceptionTypes != null) {
                              for(int i = 0; i < exceptionTypes.length; ++i) {
                                 if (exceptionTypes[i].getName().equals(TooManyListenersException.class.getName())) {
                                    table.put("isUnicast", "true");
                                    break;
                                 }
                              }
                           }
                        }

                        methodsTable.put(eventName, table);
                     }
                  }
               }
            }
         }
      }
   }

   private static Method[] introspectListenerMethods(Class<?> listenerType) {
      Method[] methods = listenerType.getDeclaredMethods();
      ArrayList<Method> list = new ArrayList();

      for(int i = 0; i < methods.length; ++i) {
         Class<?>[] paramTypes = methods[i].getParameterTypes();
         if (paramTypes.length == 1 && EventObject.class.isAssignableFrom(paramTypes[0])) {
            list.add(methods[i]);
         }
      }

      Method[] matchedMethods = new Method[list.size()];
      list.toArray(matchedMethods);
      return matchedMethods;
   }

   private static void introspectGetListenerMethods(Method theMethod, HashMap<String, HashMap> methodsTable) {
      String type = "get";
      String methodName = theMethod.getName();
      if (methodName != null) {
         if (methodName.startsWith(type) && methodName.endsWith("Listeners")) {
            String listenerName = methodName.substring(type.length(), methodName.length() - 1);
            String eventName = listenerName.substring(0, listenerName.lastIndexOf("Listener"));
            if (eventName != null && eventName.length() != 0) {
               Class[] paramTypes = theMethod.getParameterTypes();
               if (paramTypes != null && paramTypes.length == 0) {
                  Class returnType = theMethod.getReturnType();
                  if (returnType.getComponentType() != null && returnType.getComponentType().getName().endsWith(listenerName)) {
                     HashMap table = (HashMap)methodsTable.get(eventName);
                     if (table == null) {
                        table = new HashMap();
                     }

                     table.put(type, theMethod);
                     methodsTable.put(eventName, table);
                  }
               }
            }
         }
      }
   }

   private static boolean isValidProperty(String propertyName) {
      return propertyName != null && propertyName.length() != 0;
   }

   void init() {
      if (this.events == null) {
         this.events = new EventSetDescriptor[0];
      }

      if (this.properties == null) {
         this.properties = new PropertyDescriptor[0];
      }

      if (this.properties != null) {
         String defaultPropertyName = this.defaultPropertyIndex != -1 ? this.properties[this.defaultPropertyIndex].getName() : null;
         Arrays.sort(this.properties, comparator);
         if (null != defaultPropertyName) {
            for(int i = 0; i < this.properties.length; ++i) {
               if (defaultPropertyName.equals(this.properties[i].getName())) {
                  this.defaultPropertyIndex = i;
                  break;
               }
            }
         }
      }

   }

   private static class PropertyComparator implements Comparator<PropertyDescriptor> {
      private PropertyComparator() {
      }

      public int compare(PropertyDescriptor object1, PropertyDescriptor object2) {
         return object1.getName().compareTo(object2.getName());
      }
   }
}
