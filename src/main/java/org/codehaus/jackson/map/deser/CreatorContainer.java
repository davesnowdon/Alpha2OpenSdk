package org.codehaus.jackson.map.deser;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.util.ClassUtil;

public class CreatorContainer {
   final BasicBeanDescription _beanDesc;
   final boolean _canFixAccess;
   protected Constructor<?> _defaultConstructor;
   AnnotatedMethod _strFactory;
   AnnotatedMethod _intFactory;
   AnnotatedMethod _longFactory;
   AnnotatedMethod _delegatingFactory;
   AnnotatedMethod _propertyBasedFactory;
   SettableBeanProperty[] _propertyBasedFactoryProperties = null;
   AnnotatedConstructor _strConstructor;
   AnnotatedConstructor _intConstructor;
   AnnotatedConstructor _longConstructor;
   AnnotatedConstructor _delegatingConstructor;
   AnnotatedConstructor _propertyBasedConstructor;
   SettableBeanProperty[] _propertyBasedConstructorProperties = null;

   public CreatorContainer(BasicBeanDescription beanDesc, boolean canFixAccess) {
      this._beanDesc = beanDesc;
      this._canFixAccess = canFixAccess;
   }

   public void setDefaultConstructor(Constructor<?> ctor) {
      this._defaultConstructor = ctor;
   }

   public void addStringConstructor(AnnotatedConstructor ctor) {
      this._strConstructor = this.verifyNonDup(ctor, this._strConstructor, "String");
   }

   public void addIntConstructor(AnnotatedConstructor ctor) {
      this._intConstructor = this.verifyNonDup(ctor, this._intConstructor, "int");
   }

   public void addLongConstructor(AnnotatedConstructor ctor) {
      this._longConstructor = this.verifyNonDup(ctor, this._longConstructor, "long");
   }

   public void addDelegatingConstructor(AnnotatedConstructor ctor) {
      this._delegatingConstructor = this.verifyNonDup(ctor, this._delegatingConstructor, "long");
   }

   public void addPropertyConstructor(AnnotatedConstructor ctor, SettableBeanProperty[] properties) {
      this._propertyBasedConstructor = this.verifyNonDup(ctor, this._propertyBasedConstructor, "property-based");
      if (properties.length > 1) {
         HashMap<String, Integer> names = new HashMap();
         int i = 0;

         for(int len = properties.length; i < len; ++i) {
            String name = properties[i].getName();
            Integer old = (Integer)names.put(name, i);
            if (old != null) {
               throw new IllegalArgumentException("Duplicate creator property \"" + name + "\" (index " + old + " vs " + i + ")");
            }
         }
      }

      this._propertyBasedConstructorProperties = properties;
   }

   public void addStringFactory(AnnotatedMethod factory) {
      this._strFactory = this.verifyNonDup(factory, this._strFactory, "String");
   }

   public void addIntFactory(AnnotatedMethod factory) {
      this._intFactory = this.verifyNonDup(factory, this._intFactory, "int");
   }

   public void addLongFactory(AnnotatedMethod factory) {
      this._longFactory = this.verifyNonDup(factory, this._longFactory, "long");
   }

   public void addDelegatingFactory(AnnotatedMethod factory) {
      this._delegatingFactory = this.verifyNonDup(factory, this._delegatingFactory, "long");
   }

   public void addPropertyFactory(AnnotatedMethod factory, SettableBeanProperty[] properties) {
      this._propertyBasedFactory = this.verifyNonDup(factory, this._propertyBasedFactory, "property-based");
      this._propertyBasedFactoryProperties = properties;
   }

   public Constructor<?> getDefaultConstructor() {
      return this._defaultConstructor;
   }

   public Creator.StringBased stringCreator() {
      return this._strConstructor == null && this._strFactory == null ? null : new Creator.StringBased(this._beanDesc.getBeanClass(), this._strConstructor, this._strFactory);
   }

   public Creator.NumberBased numberCreator() {
      return this._intConstructor == null && this._intFactory == null && this._longConstructor == null && this._longFactory == null ? null : new Creator.NumberBased(this._beanDesc.getBeanClass(), this._intConstructor, this._intFactory, this._longConstructor, this._longFactory);
   }

   public Creator.Delegating delegatingCreator() {
      return this._delegatingConstructor == null && this._delegatingFactory == null ? null : new Creator.Delegating(this._beanDesc, this._delegatingConstructor, this._delegatingFactory);
   }

   public Creator.PropertyBased propertyBasedCreator() {
      return this._propertyBasedConstructor == null && this._propertyBasedFactory == null ? null : new Creator.PropertyBased(this._propertyBasedConstructor, this._propertyBasedConstructorProperties, this._propertyBasedFactory, this._propertyBasedFactoryProperties);
   }

   protected AnnotatedConstructor verifyNonDup(AnnotatedConstructor newOne, AnnotatedConstructor oldOne, String type) {
      if (oldOne != null) {
         throw new IllegalArgumentException("Conflicting " + type + " constructors: already had " + oldOne + ", encountered " + newOne);
      } else {
         if (this._canFixAccess) {
            ClassUtil.checkAndFixAccess(newOne.getAnnotated());
         }

         return newOne;
      }
   }

   protected AnnotatedMethod verifyNonDup(AnnotatedMethod newOne, AnnotatedMethod oldOne, String type) {
      if (oldOne != null) {
         throw new IllegalArgumentException("Conflicting " + type + " factory methods: already had " + oldOne + ", encountered " + newOne);
      } else {
         if (this._canFixAccess) {
            ClassUtil.checkAndFixAccess(newOne.getAnnotated());
         }

         return newOne;
      }
   }
}
