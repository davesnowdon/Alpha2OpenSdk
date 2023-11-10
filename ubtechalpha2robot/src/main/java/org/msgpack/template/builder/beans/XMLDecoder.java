package org.msgpack.template.builder.beans;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import javax.xml.parsers.SAXParserFactory;
import org.apache.harmony.beans.internal.nls.Messages;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLDecoder {
   private ClassLoader defaultClassLoader;
   private InputStream inputStream;
   private ExceptionListener listener;
   private Object owner;
   private Stack<XMLDecoder.Elem> readObjs;
   private int readObjIndex;
   private XMLDecoder.SAXHandler saxHandler;

   public XMLDecoder(InputStream inputStream) {
      this(inputStream, (Object)null, (ExceptionListener)null, (ClassLoader)null);
   }

   public XMLDecoder(InputStream inputStream, Object owner) {
      this(inputStream, owner, (ExceptionListener)null, (ClassLoader)null);
   }

   public XMLDecoder(InputStream inputStream, Object owner, ExceptionListener listener) {
      this(inputStream, owner, listener, (ClassLoader)null);
   }

   public XMLDecoder(InputStream inputStream, Object owner, ExceptionListener listener, ClassLoader cl) {
      this.defaultClassLoader = null;
      this.readObjs = new Stack();
      this.readObjIndex = 0;
      this.saxHandler = null;
      this.inputStream = inputStream;
      this.owner = owner;
      this.listener = (ExceptionListener)(listener == null ? new XMLDecoder.DefaultExceptionListener() : listener);
      this.defaultClassLoader = cl;
   }

   public void close() {
      if (this.inputStream != null) {
         try {
            this.inputStream.close();
         } catch (Exception var2) {
            this.listener.exceptionThrown(var2);
         }

      }
   }

   public ExceptionListener getExceptionListener() {
      return this.listener;
   }

   public Object getOwner() {
      return this.owner;
   }

   public Object readObject() {
      if (this.inputStream == null) {
         return null;
      } else {
         if (this.saxHandler == null) {
            this.saxHandler = new XMLDecoder.SAXHandler();

            try {
               SAXParserFactory.newInstance().newSAXParser().parse(this.inputStream, this.saxHandler);
            } catch (Exception var2) {
               this.listener.exceptionThrown(var2);
            }
         }

         if (this.readObjIndex >= this.readObjs.size()) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("custom.beans.70"));
         } else {
            XMLDecoder.Elem elem = (XMLDecoder.Elem)this.readObjs.get(this.readObjIndex);
            if (!elem.isClosed) {
               throw new ArrayIndexOutOfBoundsException(Messages.getString("custom.beans.70"));
            } else {
               ++this.readObjIndex;
               return elem.result;
            }
         }
      }
   }

   public void setExceptionListener(ExceptionListener listener) {
      if (listener != null) {
         this.listener = listener;
      }

   }

   public void setOwner(Object owner) {
      this.owner = owner;
   }

   private static class Elem {
      String id;
      String idref;
      boolean isExecuted;
      boolean isExpression;
      boolean isBasicType;
      boolean isClosed;
      Object target;
      String methodName;
      boolean fromProperty;
      boolean fromIndex;
      boolean fromField;
      boolean fromOwner;
      Attributes attributes;
      Object result;

      private Elem() {
      }
   }

   private class SAXHandler extends DefaultHandler {
      boolean inJavaElem;
      HashMap<String, Object> idObjMap;

      private SAXHandler() {
         this.inJavaElem = false;
         this.idObjMap = new HashMap();
      }

      public void characters(char[] ch, int start, int length) throws SAXException {
         if (this.inJavaElem) {
            if (XMLDecoder.this.readObjs.size() > 0) {
               XMLDecoder.Elem elem = (XMLDecoder.Elem)XMLDecoder.this.readObjs.peek();
               if (elem.isBasicType) {
                  String str = new String(ch, start, length);
                  elem.methodName = elem.methodName == null ? str : elem.methodName + str;
               }
            }

         }
      }

      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         if (!this.inJavaElem) {
            if ("java".equals(qName)) {
               this.inJavaElem = true;
            } else {
               XMLDecoder.this.listener.exceptionThrown(new Exception(Messages.getString("custom.beans.72", (Object)qName)));
            }

         } else {
            if ("object".equals(qName)) {
               this.startObjectElem(attributes);
            } else if ("array".equals(qName)) {
               this.startArrayElem(attributes);
            } else if ("void".equals(qName)) {
               this.startVoidElem(attributes);
            } else if ("boolean".equals(qName) || "byte".equals(qName) || "char".equals(qName) || "class".equals(qName) || "double".equals(qName) || "float".equals(qName) || "int".equals(qName) || "long".equals(qName) || "short".equals(qName) || "string".equals(qName) || "null".equals(qName)) {
               this.startBasicElem(qName, attributes);
            }

         }
      }

      private void startObjectElem(Attributes attributes) {
         XMLDecoder.Elem elem = new XMLDecoder.Elem();
         elem.isExpression = true;
         elem.id = attributes.getValue("id");
         elem.idref = attributes.getValue("idref");
         elem.attributes = attributes;
         if (elem.idref == null) {
            this.obtainTarget(elem, attributes);
            this.obtainMethod(elem, attributes);
         }

         XMLDecoder.this.readObjs.push(elem);
      }

      private void obtainTarget(XMLDecoder.Elem elem, Attributes attributes) {
         String className = attributes.getValue("class");
         if (className != null) {
            try {
               elem.target = this.classForName(className);
            } catch (ClassNotFoundException var5) {
               XMLDecoder.this.listener.exceptionThrown(var5);
            }
         } else {
            XMLDecoder.Elem parent = this.latestUnclosedElem();
            if (parent == null) {
               elem.target = XMLDecoder.this.owner;
               return;
            }

            elem.target = this.execute(parent);
         }

      }

      private void obtainMethod(XMLDecoder.Elem elem, Attributes attributes) {
         elem.methodName = attributes.getValue("method");
         if (elem.methodName == null) {
            elem.methodName = attributes.getValue("property");
            if (elem.methodName != null) {
               elem.fromProperty = true;
            } else {
               elem.methodName = attributes.getValue("index");
               if (elem.methodName != null) {
                  elem.fromIndex = true;
               } else {
                  elem.methodName = attributes.getValue("field");
                  if (elem.methodName != null) {
                     elem.fromField = true;
                  } else {
                     elem.methodName = attributes.getValue("owner");
                     if (elem.methodName != null) {
                        elem.fromOwner = true;
                     } else {
                        elem.methodName = "new";
                     }
                  }
               }
            }
         }
      }

      private Class<?> classForName(String className) throws ClassNotFoundException {
         if ("boolean".equals(className)) {
            return Boolean.TYPE;
         } else if ("byte".equals(className)) {
            return Byte.TYPE;
         } else if ("char".equals(className)) {
            return Character.TYPE;
         } else if ("double".equals(className)) {
            return Double.TYPE;
         } else if ("float".equals(className)) {
            return Float.TYPE;
         } else if ("int".equals(className)) {
            return Integer.TYPE;
         } else if ("long".equals(className)) {
            return Long.TYPE;
         } else {
            return "short".equals(className) ? Short.TYPE : Class.forName(className, true, XMLDecoder.this.defaultClassLoader == null ? Thread.currentThread().getContextClassLoader() : XMLDecoder.this.defaultClassLoader);
         }
      }

      private void startArrayElem(Attributes attributes) {
         XMLDecoder.Elem elem = new XMLDecoder.Elem();
         elem.isExpression = true;
         elem.id = attributes.getValue("id");
         elem.attributes = attributes;

         try {
            Class<?> compClass = this.classForName(attributes.getValue("class"));
            String lengthValue = attributes.getValue("length");
            if (lengthValue != null) {
               int length = Integer.parseInt(attributes.getValue("length"));
               elem.result = Array.newInstance(compClass, length);
               elem.isExecuted = true;
            } else {
               elem.target = compClass;
               elem.methodName = "newArray";
               elem.isExecuted = false;
            }
         } catch (Exception var6) {
            XMLDecoder.this.listener.exceptionThrown(var6);
         }

         XMLDecoder.this.readObjs.push(elem);
      }

      private void startVoidElem(Attributes attributes) {
         XMLDecoder.Elem elem = new XMLDecoder.Elem();
         elem.id = attributes.getValue("id");
         elem.attributes = attributes;
         this.obtainTarget(elem, attributes);
         this.obtainMethod(elem, attributes);
         XMLDecoder.this.readObjs.push(elem);
      }

      private void startBasicElem(String tagName, Attributes attributes) {
         XMLDecoder.Elem elem = new XMLDecoder.Elem();
         elem.isBasicType = true;
         elem.isExpression = true;
         elem.id = attributes.getValue("id");
         elem.idref = attributes.getValue("idref");
         elem.attributes = attributes;
         elem.target = tagName;
         XMLDecoder.this.readObjs.push(elem);
      }

      public void endElement(String uri, String localName, String qName) throws SAXException {
         if (this.inJavaElem) {
            if ("java".equals(qName)) {
               this.inJavaElem = false;
            } else {
               XMLDecoder.Elem toClose = this.latestUnclosedElem();
               if ("string".equals(toClose.target)) {
                  StringBuilder sb = new StringBuilder();

                  for(int index = XMLDecoder.this.readObjs.size() - 1; index >= 0; --index) {
                     XMLDecoder.Elem elem = (XMLDecoder.Elem)XMLDecoder.this.readObjs.get(index);
                     if (toClose == elem) {
                        break;
                     }

                     if ("char".equals(elem.target)) {
                        sb.insert(0, elem.methodName);
                     }
                  }

                  toClose.methodName = toClose.methodName != null ? toClose.methodName + sb.toString() : sb.toString();
               }

               this.execute(toClose);
               toClose.isClosed = true;

               while(XMLDecoder.this.readObjs.pop() != toClose) {
               }

               if (toClose.isExpression) {
                  XMLDecoder.this.readObjs.push(toClose);
               }

            }
         }
      }

      private XMLDecoder.Elem latestUnclosedElem() {
         for(int i = XMLDecoder.this.readObjs.size() - 1; i >= 0; --i) {
            XMLDecoder.Elem elem = (XMLDecoder.Elem)XMLDecoder.this.readObjs.get(i);
            if (!elem.isClosed) {
               return elem;
            }
         }

         return null;
      }

      private Object execute(XMLDecoder.Elem elem) {
         if (elem.isExecuted) {
            return elem.result;
         } else {
            try {
               if (elem.idref != null) {
                  elem.result = this.idObjMap.get(elem.idref);
               } else if (elem.isBasicType) {
                  elem.result = this.executeBasic(elem);
               } else {
                  elem.result = this.executeCommon(elem);
               }
            } catch (Exception var3) {
               XMLDecoder.this.listener.exceptionThrown(var3);
            }

            if (elem.id != null) {
               this.idObjMap.put(elem.id, elem.result);
            }

            elem.isExecuted = true;
            return elem.result;
         }
      }

      private Object executeCommon(XMLDecoder.Elem elem) throws Exception {
         ArrayList args = new ArrayList(5);

         while(XMLDecoder.this.readObjs.peek() != elem) {
            XMLDecoder.Elem argElem = (XMLDecoder.Elem)XMLDecoder.this.readObjs.pop();
            args.add(0, argElem.result);
         }

         String method = elem.methodName;
         if (elem.fromProperty) {
            method = (args.size() == 0 ? "get" : "set") + this.capitalize(method);
         }

         if (elem.fromIndex) {
            Integer index = Integer.valueOf(method);
            args.add(0, index);
            method = args.size() == 1 ? "get" : "set";
         }

         if (elem.fromField) {
            Field f = ((Class)elem.target).getField(method);
            return (new Expression(f, "get", new Object[]{null})).getValue();
         } else if (elem.fromOwner) {
            return XMLDecoder.this.owner;
         } else if (elem.target != XMLDecoder.this.owner) {
            Expression exp = new Expression(elem.target, method, args.toArray());
            return exp.getValue();
         } else if ("getOwner".equals(method)) {
            return XMLDecoder.this.owner;
         } else {
            Class<?>[] c = new Class[args.size()];

            for(int i = 0; i < args.size(); ++i) {
               Object arg = args.get(i);
               c[i] = arg == null ? null : arg.getClass();
            }

            Method mostSpecificMethod;
            try {
               mostSpecificMethod = XMLDecoder.this.owner.getClass().getMethod(method, c);
               return mostSpecificMethod.invoke(XMLDecoder.this.owner, args.toArray());
            } catch (NoSuchMethodException var7) {
               mostSpecificMethod = this.findMethod(XMLDecoder.this.owner instanceof Class ? (Class)XMLDecoder.this.owner : XMLDecoder.this.owner.getClass(), method, c);
               return mostSpecificMethod.invoke(XMLDecoder.this.owner, args.toArray());
            }
         }
      }

      private Method findMethod(Class<?> clazz, String methodName, Class<?>[] clazzes) throws Exception {
         Method[] methods = clazz.getMethods();
         ArrayList<Method> matchMethods = new ArrayList();
         Method[] arr$ = methods;
         int len$ = methods.length;

         int difference;
         for(int i$ = 0; i$ < len$; ++i$) {
            Method method = arr$[i$];
            if (methodName.equals(method.getName())) {
               Class<?>[] parameterTypes = method.getParameterTypes();
               if (parameterTypes.length == clazzes.length) {
                  boolean match = true;

                  for(difference = 0; difference < parameterTypes.length; ++difference) {
                     boolean isNull = clazzes[difference] == null;
                     boolean isPrimitive = this.isPrimitiveWrapper(clazzes[difference], parameterTypes[difference]);
                     boolean isAssignable = isNull ? false : parameterTypes[difference].isAssignableFrom(clazzes[difference]);
                     if (!isNull && !isPrimitive && !isAssignable) {
                        match = false;
                     }
                  }

                  if (match) {
                     matchMethods.add(method);
                  }
               }
            }
         }

         int size = matchMethods.size();
         if (size == 1) {
            return (Method)matchMethods.get(0);
         } else if (size == 0) {
            throw new NoSuchMethodException(Messages.getString("custom.beans.41", (Object)methodName));
         } else {
            Statement.MethodComparator comparator = new Statement.MethodComparator(methodName, clazzes);
            Method chosenOne = (Method)matchMethods.get(0);
            matchMethods.remove(0);
            int methodCounter = 1;
            Iterator i$x = matchMethods.iterator();

            while(i$x.hasNext()) {
               Method methodx = (Method)i$x.next();
               difference = comparator.compare(chosenOne, methodx);
               if (difference > 0) {
                  chosenOne = methodx;
                  methodCounter = 1;
               } else if (difference == 0) {
                  ++methodCounter;
               }
            }

            if (methodCounter > 1) {
               throw new NoSuchMethodException(Messages.getString("custom.beans.62", (Object)methodName));
            } else {
               return chosenOne;
            }
         }
      }

      private boolean isPrimitiveWrapper(Class<?> wrapper, Class<?> base) {
         return base == Boolean.TYPE && wrapper == Boolean.class || base == Byte.TYPE && wrapper == Byte.class || base == Character.TYPE && wrapper == Character.class || base == Short.TYPE && wrapper == Short.class || base == Integer.TYPE && wrapper == Integer.class || base == Long.TYPE && wrapper == Long.class || base == Float.TYPE && wrapper == Float.class || base == Double.TYPE && wrapper == Double.class;
      }

      private String capitalize(String str) {
         StringBuilder buf = new StringBuilder(str);
         buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
         return buf.toString();
      }

      private Object executeBasic(XMLDecoder.Elem elem) throws Exception {
         String tag = (String)elem.target;
         String value = elem.methodName;
         if ("null".equals(tag)) {
            return null;
         } else if ("string".equals(tag)) {
            return value == null ? "" : value;
         } else if ("class".equals(tag)) {
            return this.classForName(value);
         } else if ("boolean".equals(tag)) {
            return Boolean.valueOf(value);
         } else if ("byte".equals(tag)) {
            return Byte.valueOf(value);
         } else if ("char".equals(tag)) {
            if (value == null && elem.attributes != null) {
               String codeAttr = elem.attributes.getValue("code");
               if (codeAttr != null) {
                  Character character = new Character((char)Integer.valueOf(codeAttr.substring(1), 16));
                  elem.methodName = character.toString();
                  return character;
               }
            }

            return value.charAt(0);
         } else if ("double".equals(tag)) {
            return Double.valueOf(value);
         } else if ("float".equals(tag)) {
            return Float.valueOf(value);
         } else if ("int".equals(tag)) {
            return Integer.valueOf(value);
         } else if ("long".equals(tag)) {
            return Long.valueOf(value);
         } else if ("short".equals(tag)) {
            return Short.valueOf(value);
         } else {
            throw new Exception(Messages.getString("custom.beans.71", (Object)tag));
         }
      }

      public void error(SAXParseException e) throws SAXException {
         XMLDecoder.this.listener.exceptionThrown(e);
      }

      public void fatalError(SAXParseException e) throws SAXException {
         XMLDecoder.this.listener.exceptionThrown(e);
      }

      public void warning(SAXParseException e) throws SAXException {
         XMLDecoder.this.listener.exceptionThrown(e);
      }
   }

   private static class DefaultExceptionListener implements ExceptionListener {
      private DefaultExceptionListener() {
      }

      public void exceptionThrown(Exception e) {
         System.err.println(e.getMessage());
         System.err.println("Continue...");
      }
   }
}
