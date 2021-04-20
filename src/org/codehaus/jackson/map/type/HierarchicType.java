package org.codehaus.jackson.map.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class HierarchicType {
   protected final Type _actualType;
   protected final Class<?> _rawClass;
   protected final ParameterizedType _genericType;
   protected HierarchicType _superType;
   protected HierarchicType _subType;

   public HierarchicType(Type type) {
      this._actualType = type;
      if (type instanceof Class) {
         this._rawClass = (Class)type;
         this._genericType = null;
      } else {
         if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Type " + type.getClass().getName() + " can not be used to construct HierarchicType");
         }

         this._genericType = (ParameterizedType)type;
         this._rawClass = (Class)this._genericType.getRawType();
      }

   }

   public void setSuperType(HierarchicType sup) {
      this._superType = sup;
   }

   public HierarchicType getSuperType() {
      return this._superType;
   }

   public void setSubType(HierarchicType sub) {
      this._subType = sub;
   }

   public HierarchicType getSubType() {
      return this._subType;
   }

   public boolean isGeneric() {
      return this._genericType != null;
   }

   public ParameterizedType asGeneric() {
      return this._genericType;
   }

   public Class<?> getRawClass() {
      return this._rawClass;
   }

   public String toString() {
      return this._genericType != null ? this._genericType.toString() : this._rawClass.getName();
   }
}
