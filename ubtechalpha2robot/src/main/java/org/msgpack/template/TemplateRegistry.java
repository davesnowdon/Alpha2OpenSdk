package org.msgpack.template;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.msgpack.MessagePackable;
import org.msgpack.MessageTypeException;
import org.msgpack.template.builder.TemplateBuilder;
import org.msgpack.template.builder.TemplateBuilderChain;
import org.msgpack.type.Value;

public class TemplateRegistry {
   private TemplateRegistry parent = null;
   private TemplateBuilderChain chain;
   Map<Type, Template<Type>> cache;
   private Map<Type, GenericTemplate> genericCache;

   private TemplateRegistry() {
      this.parent = null;
      this.chain = this.createTemplateBuilderChain();
      this.genericCache = new HashMap();
      this.cache = new HashMap();
      this.registerTemplates();
      this.cache = Collections.unmodifiableMap(this.cache);
   }

   public TemplateRegistry(TemplateRegistry registry) {
      if (registry != null) {
         this.parent = registry;
      } else {
         this.parent = new TemplateRegistry();
      }

      this.chain = this.createTemplateBuilderChain();
      this.cache = new HashMap();
      this.genericCache = new HashMap();
      this.registerTemplatesWhichRefersRegistry();
   }

   protected TemplateBuilderChain createTemplateBuilderChain() {
      return new TemplateBuilderChain(this);
   }

   public void setClassLoader(ClassLoader cl) {
      this.chain = new TemplateBuilderChain(this, cl);
   }

   private void registerTemplates() {
      this.register((Type)Boolean.TYPE, (Template)BooleanTemplate.getInstance());
      this.register((Type)Boolean.class, (Template)BooleanTemplate.getInstance());
      this.register((Type)Byte.TYPE, (Template)ByteTemplate.getInstance());
      this.register((Type)Byte.class, (Template)ByteTemplate.getInstance());
      this.register((Type)Short.TYPE, (Template)ShortTemplate.getInstance());
      this.register((Type)Short.class, (Template)ShortTemplate.getInstance());
      this.register((Type)Integer.TYPE, (Template)IntegerTemplate.getInstance());
      this.register((Type)Integer.class, (Template)IntegerTemplate.getInstance());
      this.register((Type)Long.TYPE, (Template)LongTemplate.getInstance());
      this.register((Type)Long.class, (Template)LongTemplate.getInstance());
      this.register((Type)Float.TYPE, (Template)FloatTemplate.getInstance());
      this.register((Type)Float.class, (Template)FloatTemplate.getInstance());
      this.register((Type)Double.TYPE, (Template)DoubleTemplate.getInstance());
      this.register((Type)Double.class, (Template)DoubleTemplate.getInstance());
      this.register((Type)BigInteger.class, (Template)BigIntegerTemplate.getInstance());
      this.register((Type)Character.TYPE, (Template)CharacterTemplate.getInstance());
      this.register((Type)Character.class, (Template)CharacterTemplate.getInstance());
      this.register((Type)boolean[].class, (Template)BooleanArrayTemplate.getInstance());
      this.register((Type)short[].class, (Template)ShortArrayTemplate.getInstance());
      this.register((Type)int[].class, (Template)IntegerArrayTemplate.getInstance());
      this.register((Type)long[].class, (Template)LongArrayTemplate.getInstance());
      this.register((Type)float[].class, (Template)FloatArrayTemplate.getInstance());
      this.register((Type)double[].class, (Template)DoubleArrayTemplate.getInstance());
      this.register((Type)String.class, (Template)StringTemplate.getInstance());
      this.register((Type)byte[].class, (Template)ByteArrayTemplate.getInstance());
      this.register((Type)ByteBuffer.class, (Template)ByteBufferTemplate.getInstance());
      this.register((Type)Value.class, (Template)ValueTemplate.getInstance());
      this.register((Type)BigDecimal.class, (Template)BigDecimalTemplate.getInstance());
      this.register((Type)Date.class, (Template)DateTemplate.getInstance());
      this.registerTemplatesWhichRefersRegistry();
   }

   protected void registerTemplatesWhichRefersRegistry() {
      AnyTemplate anyTemplate = new AnyTemplate(this);
      this.register((Type)List.class, (Template)(new ListTemplate(anyTemplate)));
      this.register((Type)Set.class, (Template)(new SetTemplate(anyTemplate)));
      this.register((Type)Collection.class, (Template)(new CollectionTemplate(anyTemplate)));
      this.register((Type)Map.class, (Template)(new MapTemplate(anyTemplate, anyTemplate)));
      this.registerGeneric(List.class, new GenericCollectionTemplate(this, ListTemplate.class));
      this.registerGeneric(Set.class, new GenericCollectionTemplate(this, SetTemplate.class));
      this.registerGeneric(Collection.class, new GenericCollectionTemplate(this, CollectionTemplate.class));
      this.registerGeneric(Map.class, new GenericMapTemplate(this, MapTemplate.class));
   }

   public void register(Class<?> targetClass) {
      this.buildAndRegister((TemplateBuilder)null, targetClass, false, (FieldList)null);
   }

   public void register(Class<?> targetClass, FieldList flist) {
      if (flist == null) {
         throw new NullPointerException("FieldList object is null");
      } else {
         this.buildAndRegister((TemplateBuilder)null, targetClass, false, flist);
      }
   }

   public synchronized void register(Type targetType, Template tmpl) {
      if (tmpl == null) {
         throw new NullPointerException("Template object is null");
      } else {
         if (targetType instanceof ParameterizedType) {
            this.cache.put(((ParameterizedType)targetType).getRawType(), tmpl);
         } else {
            this.cache.put(targetType, tmpl);
         }

      }
   }

   public synchronized void registerGeneric(Type targetType, GenericTemplate tmpl) {
      if (targetType instanceof ParameterizedType) {
         this.genericCache.put(((ParameterizedType)targetType).getRawType(), tmpl);
      } else {
         this.genericCache.put(targetType, tmpl);
      }

   }

   public synchronized boolean unregister(Type targetType) {
      Template<Type> tmpl = (Template)this.cache.remove(targetType);
      return tmpl != null;
   }

   public synchronized void unregister() {
      this.cache.clear();
   }

   public synchronized Template lookup(Type targetType) {
      Template tmpl;
      if (targetType instanceof ParameterizedType) {
         ParameterizedType paramedType = (ParameterizedType)targetType;
         tmpl = this.lookupGenericType(paramedType);
         if (tmpl != null) {
            return tmpl;
         }

         targetType = paramedType.getRawType();
      }

      tmpl = this.lookupGenericArrayType(targetType);
      if (tmpl != null) {
         return tmpl;
      } else {
         tmpl = this.lookupCache(targetType);
         if (tmpl != null) {
            return tmpl;
         } else {
            AnyTemplate tmpl;
            if (!(targetType instanceof WildcardType) && !(targetType instanceof TypeVariable)) {
               Class<?> targetClass = (Class)targetType;
               if (MessagePackable.class.isAssignableFrom(targetClass)) {
                  Template tmpl = new MessagePackableTemplate(targetClass);
                  this.register((Type)targetClass, (Template)tmpl);
                  return tmpl;
               } else if (targetClass.isInterface()) {
                  tmpl = new AnyTemplate(this);
                  this.register((Type)targetType, (Template)tmpl);
                  return tmpl;
               } else {
                  tmpl = this.lookupAfterBuilding(targetClass);
                  if (tmpl != null) {
                     return tmpl;
                  } else {
                     tmpl = this.lookupInterfaceTypes(targetClass);
                     if (tmpl != null) {
                        return tmpl;
                     } else {
                        tmpl = this.lookupSuperclasses(targetClass);
                        if (tmpl != null) {
                           return tmpl;
                        } else {
                           tmpl = this.lookupSuperclassInterfaceTypes(targetClass);
                           if (tmpl != null) {
                              return tmpl;
                           } else {
                              throw new MessageTypeException("Cannot find template for " + targetClass + " class.  " + "Try to add @Message annotation to the class or call MessagePack.register(Type).");
                           }
                        }
                     }
                  }
               }
            } else {
               tmpl = new AnyTemplate(this);
               this.register((Type)targetType, (Template)tmpl);
               return tmpl;
            }
         }
      }
   }

   private Template<Type> lookupGenericType(ParameterizedType paramedType) {
      Template<Type> tmpl = this.lookupGenericTypeImpl(paramedType);
      if (tmpl != null) {
         return tmpl;
      } else {
         try {
            tmpl = this.parent.lookupGenericTypeImpl(paramedType);
            if (tmpl != null) {
               return tmpl;
            }
         } catch (NullPointerException var4) {
         }

         tmpl = this.lookupGenericInterfaceTypes(paramedType);
         if (tmpl != null) {
            return tmpl;
         } else {
            tmpl = this.lookupGenericSuperclasses(paramedType);
            return tmpl != null ? tmpl : null;
         }
      }
   }

   private Template lookupGenericTypeImpl(ParameterizedType targetType) {
      Type rawType = targetType.getRawType();
      return this.lookupGenericTypeImpl0(targetType, rawType);
   }

   private Template lookupGenericTypeImpl0(ParameterizedType targetType, Type rawType) {
      GenericTemplate gtmpl = (GenericTemplate)this.genericCache.get(rawType);
      if (gtmpl == null) {
         return null;
      } else {
         Type[] types = targetType.getActualTypeArguments();
         Template[] tmpls = new Template[types.length];

         for(int i = 0; i < types.length; ++i) {
            tmpls[i] = this.lookup(types[i]);
         }

         return gtmpl.build(tmpls);
      }
   }

   private <T> Template<T> lookupGenericInterfaceTypes(ParameterizedType targetType) {
      Type rawType = targetType.getRawType();
      Template tmpl = null;

      try {
         Class<?>[] infTypes = ((Class)rawType).getInterfaces();
         Class[] arr$ = infTypes;
         int len$ = infTypes.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Class<?> infType = arr$[i$];
            tmpl = this.lookupGenericTypeImpl0(targetType, infType);
            if (tmpl != null) {
               return tmpl;
            }
         }
      } catch (ClassCastException var9) {
      }

      return tmpl;
   }

   private <T> Template<T> lookupGenericSuperclasses(ParameterizedType targetType) {
      Type rawType = targetType.getRawType();
      Template tmpl = null;

      try {
         Class<?> superClass = ((Class)rawType).getSuperclass();
         if (superClass == null) {
            return null;
         }

         while(superClass != Object.class) {
            tmpl = this.lookupGenericTypeImpl0(targetType, superClass);
            if (tmpl != null) {
               this.register((Type)targetType, (Template)tmpl);
               return tmpl;
            }

            superClass = superClass.getSuperclass();
         }
      } catch (ClassCastException var5) {
      }

      return tmpl;
   }

   private Template<Type> lookupGenericArrayType(Type targetType) {
      if (!(targetType instanceof GenericArrayType)) {
         return null;
      } else {
         GenericArrayType genericArrayType = (GenericArrayType)targetType;
         Template<Type> tmpl = this.lookupGenericArrayTypeImpl(genericArrayType);
         if (tmpl != null) {
            return tmpl;
         } else {
            try {
               tmpl = this.parent.lookupGenericArrayTypeImpl(genericArrayType);
               if (tmpl != null) {
                  return tmpl;
               }
            } catch (NullPointerException var5) {
            }

            return null;
         }
      }
   }

   private Template lookupGenericArrayTypeImpl(GenericArrayType genericArrayType) {
      String genericArrayTypeName = "" + genericArrayType;
      int dim = genericArrayTypeName.split("\\[").length - 1;
      if (dim <= 0) {
         throw new MessageTypeException(String.format("fatal error: type=", genericArrayTypeName));
      } else if (dim > 1) {
         throw new UnsupportedOperationException(String.format("Not implemented template generation of %s", genericArrayTypeName));
      } else {
         String genericCompTypeName = "" + genericArrayType.getGenericComponentType();
         boolean isPrimitiveType = isPrimitiveType(genericCompTypeName);
         StringBuffer sbuf = new StringBuffer();

         for(int i = 0; i < dim; ++i) {
            sbuf.append('[');
         }

         if (!isPrimitiveType) {
            sbuf.append('L');
            sbuf.append(toJvmReferenceTypeName(genericCompTypeName));
            sbuf.append(';');
         } else {
            sbuf.append(toJvmPrimitiveTypeName(genericCompTypeName));
         }

         String jvmArrayClassName = sbuf.toString();
         Class jvmArrayClass = null;
         ClassLoader cl = null;

         try {
            cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
               jvmArrayClass = cl.loadClass(jvmArrayClassName);
               if (jvmArrayClass != null) {
                  return this.lookupAfterBuilding(jvmArrayClass);
               }
            }
         } catch (ClassNotFoundException var13) {
         }

         try {
            cl = this.getClass().getClassLoader();
            if (cl != null) {
               jvmArrayClass = cl.loadClass(jvmArrayClassName);
               if (jvmArrayClass != null) {
                  return this.lookupAfterBuilding(jvmArrayClass);
               }
            }
         } catch (ClassNotFoundException var12) {
         }

         try {
            jvmArrayClass = Class.forName(jvmArrayClassName);
            if (jvmArrayClass != null) {
               return this.lookupAfterBuilding(jvmArrayClass);
            }
         } catch (ClassNotFoundException var11) {
         }

         throw new MessageTypeException(String.format("cannot find template of %s", jvmArrayClassName));
      }
   }

   private Template<Type> lookupCache(Type targetType) {
      Template<Type> tmpl = (Template)this.cache.get(targetType);
      if (tmpl != null) {
         return tmpl;
      } else {
         try {
            tmpl = this.parent.lookupCache(targetType);
         } catch (NullPointerException var4) {
         }

         return tmpl;
      }
   }

   private <T> Template<T> lookupAfterBuilding(Class<T> targetClass) {
      TemplateBuilder builder = this.chain.select(targetClass, true);
      Template<T> tmpl = null;
      if (builder != null) {
         tmpl = this.chain.getForceBuilder().loadTemplate(targetClass);
         if (tmpl != null) {
            this.register((Type)targetClass, (Template)tmpl);
            return tmpl;
         }

         tmpl = this.buildAndRegister(builder, targetClass, true, (FieldList)null);
      }

      return tmpl;
   }

   private <T> Template<T> lookupInterfaceTypes(Class<T> targetClass) {
      Class<?>[] infTypes = targetClass.getInterfaces();
      Template<T> tmpl = null;
      Class[] arr$ = infTypes;
      int len$ = infTypes.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Class<?> infType = arr$[i$];
         tmpl = (Template)this.cache.get(infType);
         if (tmpl != null) {
            this.register((Type)targetClass, (Template)tmpl);
            return tmpl;
         }

         try {
            tmpl = this.parent.lookupCache(infType);
            if (tmpl != null) {
               this.register((Type)targetClass, (Template)tmpl);
               return tmpl;
            }
         } catch (NullPointerException var9) {
         }
      }

      return tmpl;
   }

   private <T> Template<T> lookupSuperclasses(Class<T> targetClass) {
      Class<?> superClass = targetClass.getSuperclass();
      Template<T> tmpl = null;
      if (superClass != null) {
         for(; superClass != Object.class; superClass = superClass.getSuperclass()) {
            tmpl = (Template)this.cache.get(superClass);
            if (tmpl != null) {
               this.register((Type)targetClass, (Template)tmpl);
               return tmpl;
            }

            try {
               tmpl = this.parent.lookupCache(superClass);
               if (tmpl != null) {
                  this.register((Type)targetClass, (Template)tmpl);
                  return tmpl;
               }
            } catch (NullPointerException var5) {
            }
         }
      }

      return tmpl;
   }

   private <T> Template<T> lookupSuperclassInterfaceTypes(Class<T> targetClass) {
      Class<?> superClass = targetClass.getSuperclass();
      Template<T> tmpl = null;
      if (superClass != null) {
         for(; superClass != Object.class; superClass = superClass.getSuperclass()) {
            tmpl = this.lookupInterfaceTypes(superClass);
            if (tmpl != null) {
               this.register((Type)targetClass, (Template)tmpl);
               return tmpl;
            }

            try {
               tmpl = this.parent.lookupCache(superClass);
               if (tmpl != null) {
                  this.register((Type)targetClass, (Template)tmpl);
                  return tmpl;
               }
            } catch (NullPointerException var5) {
            }
         }
      }

      return tmpl;
   }

   private synchronized Template buildAndRegister(TemplateBuilder builder, Class targetClass, boolean hasAnnotation, FieldList flist) {
      Template newTmpl = null;
      Template oldTmpl = null;

      Template var7;
      try {
         if (this.cache.containsKey(targetClass)) {
            oldTmpl = (Template)this.cache.get(targetClass);
         }

         Template newTmpl = new TemplateReference(this, targetClass);
         this.cache.put(targetClass, newTmpl);
         if (builder == null) {
            builder = this.chain.select(targetClass, hasAnnotation);
         }

         newTmpl = flist != null ? builder.buildTemplate(targetClass, flist) : builder.buildTemplate(targetClass);
         var7 = newTmpl;
      } catch (Exception var11) {
         if (oldTmpl != null) {
            this.cache.put(targetClass, oldTmpl);
         } else {
            this.cache.remove(targetClass);
         }

         newTmpl = null;
         if (var11 instanceof MessageTypeException) {
            throw (MessageTypeException)var11;
         }

         throw new MessageTypeException(var11);
      } finally {
         if (newTmpl != null) {
            this.cache.put(targetClass, newTmpl);
         }

      }

      return var7;
   }

   private static boolean isPrimitiveType(String genericCompTypeName) {
      return genericCompTypeName.equals("byte") || genericCompTypeName.equals("short") || genericCompTypeName.equals("int") || genericCompTypeName.equals("long") || genericCompTypeName.equals("float") || genericCompTypeName.equals("double") || genericCompTypeName.equals("boolean") || genericCompTypeName.equals("char");
   }

   private static String toJvmReferenceTypeName(String typeName) {
      return typeName.substring(6);
   }

   private static String toJvmPrimitiveTypeName(String typeName) {
      if (typeName.equals("byte")) {
         return "B";
      } else if (typeName.equals("short")) {
         return "S";
      } else if (typeName.equals("int")) {
         return "I";
      } else if (typeName.equals("long")) {
         return "J";
      } else if (typeName.equals("float")) {
         return "F";
      } else if (typeName.equals("double")) {
         return "D";
      } else if (typeName.equals("boolean")) {
         return "Z";
      } else if (typeName.equals("char")) {
         return "C";
      } else {
         throw new MessageTypeException(String.format("fatal error: type=%s", typeName));
      }
   }
}
